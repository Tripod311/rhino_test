package org.tripod311;

import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class Asynchronous extends ScriptableObject {
    private static class ScheduledTask {
        public TimerTask task;
        public BaseFunction fn;

        public ScheduledTask(TimerTask task, BaseFunction fn) {
            this.task = task;
            this.fn = fn;
        }
    }

    private final Timer timer = new Timer();
    private final ReentrantLock contextLock = new ReentrantLock();
    private final Context ctx;
    private final Scriptable scope;
    private int timeout_counter = 0;
    private final ReentrantLock mapLock = new ReentrantLock();
    private final HashMap<Integer, ScheduledTask> timeouts = new HashMap<>();

    public Asynchronous (Context ctx, Scriptable scope) {
        this.ctx = ctx;
        this.scope = scope;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    public static void putIntoScope(Scriptable scope) {
        Asynchronous as = new Asynchronous(Context.getCurrentContext(), scope);
        as.setParentScope(scope);

        ArrayList<Method> methodsToAdd = new ArrayList<>();

        try {
            Method setTimeout = Asynchronous.class.getMethod("setTimeout", BaseFunction.class, Integer.class);
            methodsToAdd.add(setTimeout);
            Method clearTimeout = Asynchronous.class.getMethod("clearTimeout", Integer.class);
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

    public Integer setTimeout(BaseFunction fn, Integer delay) {
        this.mapLock.lock();
        int id = this.timeout_counter++;

        try {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    callTimeout(id);
                }
            };
            ScheduledTask newTask = new ScheduledTask(task, fn);
            this.timeouts.put(id, newTask);
            this.timer.schedule(task, delay);
        } finally {
            this.mapLock.unlock();
        }
        System.out.println(fn);

        return id;
    }

    public Object clearTimeout(Integer id) {
        this.mapLock.lock();

        try {
            ScheduledTask task = this.timeouts.get(id);
            if (task != null) {
                task.task.cancel();
                this.timeouts.remove(id);
            }
        } finally {
            this.mapLock.unlock();
        }

        return Scriptable.NOT_FOUND;
    }

    private void callTimeout(Integer id) {
        this.mapLock.lock();
        this.contextLock.lock();

        try {
            ScheduledTask task = this.timeouts.get(id);

            if (task != null) {
                task.fn.call(this.ctx, this.scope, this.scope, null);
            }
        } catch (Exception e) {
            System.out.println("ASYNC ERROR: " + e);
        } finally {
            this.mapLock.unlock();
            this.contextLock.unlock();
        }
    }
}
