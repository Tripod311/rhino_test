package org.tripod311;

import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class DummyEventManager extends ScriptableObject {
    private final HashMap<String, ArrayList<BaseFunction>> handlers = new HashMap<>();

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public static void putIntoScope (Scriptable scope) {
        DummyEventManager em = new DummyEventManager();

        ArrayList<Method> methodsToAdd = new ArrayList<>();

        try {
            Method addEventListener = DummyEventManager.class.getMethod("addEventListener", String.class, BaseFunction.class);
            methodsToAdd.add(addEventListener);
            Method removeEventListener = DummyEventManager.class.getMethod("removeEventListener", String.class, BaseFunction.class);
            methodsToAdd.add(removeEventListener);
            Method runEvent = DummyEventManager.class.getMethod("runEvent", String.class, NativeArray.class);
            methodsToAdd.add(runEvent);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Method m : methodsToAdd) {
            FunctionObject methodInstance = new FunctionObject(m.getName(),
                    m, em);
            em.put(m.getName(), em, methodInstance);
        }

        scope.put("EventManager", scope, em);
    }

    public void addEventListener (String eventName, BaseFunction handler) {
        /*
        *   Здесь добавляется хендлер. Используй это как пример вообще любого сохранения
        *   JS функции в java объекте. И кажется я понял как их запускать из джавы
        *   Обрати внимание на то, что я запускаю все события в loop. Это важно.
        *   Если ты запустишь их не из loop, то получишь огромное количество невнятных багов
        *   Самый очевидный - это то что все может модифицироваться в разных потоках.
        * */
        EventLoop.getLoopInstance().runImmediate(() -> {
            if (!handlers.containsKey(eventName)) {
                handlers.put(eventName, new ArrayList<>());
            }

            ArrayList<BaseFunction> arr = handlers.get(eventName);
            arr.add(handler);
        });
    }

    public void removeEventListener (String eventName, BaseFunction handler) {
        // это чтобы убрать конкретный хендлер
        EventLoop.getLoopInstance().runImmediate(() -> {
            if (handlers.containsKey(eventName)) {
                if (handler == null) {
                    // если не сказали какую - удаляем все
                    handlers.remove(eventName);
                } else {
                    ArrayList<BaseFunction> arr = handlers.get(eventName);
                    for (int i=0; i<arr.size(); i++) {
                        BaseFunction fn = arr.get(i);
                        if (fn.equals(handler)) {
                            arr.remove(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    public void runEvent (String eventName, NativeArray eventParameters) {
        // этот метод запускает событие
        // ох, надеюсь это сработает
        EventLoop.getLoopInstance().runImmediate(() -> {
            if (handlers.containsKey(eventName)) {
                ArrayList<BaseFunction> arr = handlers.get(eventName);
                Context ctx = Context.getCurrentContext();
                for (int i=0; i<arr.size(); i++) {
                    arr.get(i).call(ctx, this, this, eventParameters.toArray());
                }
            }
        });
    }
}
