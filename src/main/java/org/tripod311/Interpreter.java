package org.tripod311;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class Interpreter {
    private Scriptable scope;
    public Interpreter (String rootDir) {
        EventLoop.getLoopInstance().runImmediate(() -> {
            Context ctx = Context.enter();
            scope = ctx.initStandardObjects();
            ExternalFunctions.putIntoScope(scope, rootDir);
            Asynchronous.putIntoScope(scope);
            DummyEventManager.putIntoScope(scope);
            DummyMobFactory.putIntoScope(scope);
        });
    }

    public void close () {
        EventLoop.getLoopInstance().close();
    }

    public void executeString (String str) {
        EventLoop.getLoopInstance().runImmediate(() -> {
            Context.getCurrentContext().evaluateString(
                    scope,
                    str,
                    "<cmd>",
                    1,
                    null
            );
        });
    }

    public void executeString (String str, String sourceName) {
        EventLoop.getLoopInstance().runImmediate(() -> {
            Context.getCurrentContext().evaluateString(
                    scope,
                    str,
                    sourceName,
                    1,
                    null
            );
        });
    }
}
