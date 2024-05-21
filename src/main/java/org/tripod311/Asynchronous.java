package org.tripod311;

import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class Asynchronous extends ScriptableObject {
    private final EventLoop loop;

    public Asynchronous (EventLoop loop) {
        this.loop = loop;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public static void putIntoScope(Scriptable scope, EventLoop loop) {
        Asynchronous as = new Asynchronous(loop);
        as.setParentScope(scope);

        ArrayList<Method> methodsToAdd = new ArrayList<>();

        try {
            Method setTimeout = Asynchronous.class.getMethod("setTimeout", BaseFunction.class, Integer.class);
            methodsToAdd.add(setTimeout);
            Method clearTimeout = Asynchronous.class.getMethod("clearTimeout", Integer.class);
            methodsToAdd.add(clearTimeout);
            Method setInterval = Asynchronous.class.getMethod("setInterval", BaseFunction.class, Integer.class);
            methodsToAdd.add(setInterval);
            Method clearInterval = Asynchronous.class.getMethod("clearInterval", Integer.class);
            methodsToAdd.add(clearInterval);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Method m : methodsToAdd) {
            FunctionObject methodInstance = new FunctionObject(m.getName(),
                    m, as);
            as.put(m.getName(), as, methodInstance);
        }

        scope.put("Async", scope, as);
    }

    public Integer setTimeout(BaseFunction fn, Integer delay) {
        Runnable callback = () -> {
            Context ctx = Context.getCurrentContext();
            fn.call(ctx, this, this, new Object[0]);
        };

        return loop.runTimeout(callback, delay);
    }

    public void clearTimeout(Integer id) {
        loop.resetTimeout(id);
    }

    public Integer setInterval(BaseFunction fn, Integer delay) {
        Runnable callback = () -> {
            Context ctx = Context.getCurrentContext();
            fn.call(ctx, this, this, new Object[0]);
        };

        return loop.runInterval(callback, delay);
    }

    public void clearInterval(Integer id) {
        loop.resetInterval(id);
    }
}
