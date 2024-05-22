package org.tripod311;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ScriptableObject;

public class DummyMobController extends ScriptableObject {
    private String type;

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    public DummyMobController (String type) {
        /*
            Это не настоящий мобконтроллер, просто пример того, как из JS запускать java код
        * */
        this.type = type;

        FunctionObject methodInstance = null;
        try {
            methodInstance = new FunctionObject(
                    "moveTo",
                    DummyMobController.class.getMethod("moveTo", Double.class, Double.class, Double.class),
                    this);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.put("moveTo", this, methodInstance);
    }

    public void moveTo (Double x, Double y, Double z) {
        System.out.println("Moving to " + x + ", " + y + ", " + z);
    }
}
