package org.tripod311;

import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class DummyMobFactory extends ScriptableObject {
    private final HashMap<String, DummyMobController> mobControllers = new HashMap<>();

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public static void putIntoScope (Scriptable scope) {
        DummyMobFactory mf = new DummyMobFactory();

        ArrayList<Method> methodsToAdd = new ArrayList<>();

        try {
            Method createMobController = DummyMobFactory.class.getMethod("createMobController", String.class, String.class);
            methodsToAdd.add(createMobController);
            Method getMobController = DummyMobFactory.class.getMethod("getMobController", String.class);
            methodsToAdd.add(getMobController);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Method m : methodsToAdd) {
            FunctionObject methodInstance = new FunctionObject(m.getName(),
                    m, mf);
            mf.put(m.getName(), mf, methodInstance);
        }

        scope.put("MobFactory", scope, mf);
    }

    public DummyMobController createMobController (String id, String type) {
        /*
        Это просто условный пример нормальной архитектуры. Ты должен научиться сам ее продумывать.
        Хотя бы потому, что я не очень знаю, что именно тебе нужно, я просто делаю так, как делал
        во множестве других проектов. Я смешиваю паттерны проектирования здесь, Factory и Singleton.
        Наверное еще какие-нибудь тоже примешиваю.
        На самом деле не обязательно знать паттерны, если ты просто умеешь придумывать работоспособную архитектуру.
        Я сначала научился строить, потом уже узнал названия.
        * */
        DummyMobController mc = new DummyMobController(type);
        mobControllers.put(id, mc);
        return mc;
    }

    public DummyMobController getMobController (String id) {
        return mobControllers.get(id);
    }
}
