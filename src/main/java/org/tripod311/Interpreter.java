package org.tripod311;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;


public class Interpreter {
    private final Context ctx;
    private final Scriptable scope;
    public Interpreter (String rootDir) {
        // Вот это все ты уже видел
        this.ctx = Context.enter();
        this.scope = this.ctx.initStandardObjects();
        ExternalFunctions.putIntoScope(this.scope, rootDir);
        Asynchronous.putIntoScope(this.scope);
    }

    public void close () {
        // Вот это надо вызвать когда ты закрываешь программу, иначе получишь ошибки и проблемы
        Context.exit();
    }

    public void executeString (String str) {
        try {
            this.ctx.evaluateString(
                    this.scope,
                    str,
                    "<cmd>",
                    1,
                    null
            );
        } catch (final RhinoException e) {
            System.out.println("Ошибка на старте: " + e);
        }
    }
}
