package org.tripod311;

import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Asynchronous extends ScriptableObject {
    private long timeout_counter = 0;
    HashMap<Long, Timer> timeouts;

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public static void putIntoScope(Scriptable scope) {
        Asynchronous as = new Asynchronous();
        as.setParentScope(scope);

        ArrayList<Method> methodsToAdd = new ArrayList<Method>();

        try {
            Method setTimeout = Asynchronous.class.getMethod("setTimeout", Long.class);
            methodsToAdd.add(setTimeout);
            Method clearTimeout = Asynchronous.class.getMethod("clearTimeout", Object.class);
            methodsToAdd.add(clearTimeout);
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

    public Long setTimeout(Callable fn, Integer delay) {
        long id = this.timeout_counter++;

        return id;
    }

    public Object clearTimeout(Long id) {
        return Scriptable.NOT_FOUND;
    }
}
