package org.tripod311;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
    Небольшое пояснение.
    Гипотетически можно все вынести в пространство имен верхнего уровня. Тогда все функции будут доступны сразу.
    Практически, лучше отделять мух от котлет и распихивать функции тематически. Я здесь создал класс ExternalFunctions
    с разнымы функциями. Я совместил мух и котлет, но мне нужно было понять, как должен создаваться такой объект.

    ScriptableObject - это общий класс для всего в JS движке. По сути так можно описать любой объект, который
    ты потом захочешь там использовать, надо просто расширить интерфейс ScriptableObject.
*/

public class ExternalFunctions extends ScriptableObject {
    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /*
    Метод getClassName просто надо было переопределить. Забей и повторяй, тут нет особой премудрости, скорее это просто
    формальность.
    */
    @Override
    public String getClassName() {
        return getClass().getName();
    }

    /*
    В статическом методе putIntoScope, этот объект создается и укладывается в пространство имен, которое ему передают.
    Расценивай putIntoScope как экзотический конструктор объекта.
    */
    public static void putIntoScope (Scriptable scope) {
        // Создаем объект, к которому потом будем обращаться
        ExternalFunctions ef = new ExternalFunctions();
        // Это не обязательно, но я указываю, что у этого объекта есть объект выше уровнем
        ef.setParentScope(scope);

        // Это список функций, которые потом будут добавлены в объект ExternalFunctions
        // Здесь просто повторяй, я и сам не стал сильно глубоко копаться в деталях
        ArrayList<Method> methodsToAdd = new ArrayList<Method>();

        try {
            Method factorial = ExternalFunctions.class.getMethod("factorial", Integer.class);
            methodsToAdd.add(factorial);
            Method log = ExternalFunctions.class.getMethod("log", Object.class);
            methodsToAdd.add(log);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Здесь функции укладываются в ExternalFunctions
        for (Method m : methodsToAdd) {
            FunctionObject methodInstance = new FunctionObject(m.getName(),
                    m, ef);
            ef.put(m.getName(), ef, methodInstance);
        }

        // Здесь ExternalFunctions укладывается в пространство имен верхнего уровня
        scope.put("ExternalFunctions", scope, ef);
    }

    public Integer factorial (Integer target) {
        if (target <= 1) {
            return target;
        } else {
            return target * factorial(target-1);
        }
    }

    public void log (Object object) {
        Date now = Calendar.getInstance().getTime();
        System.out.println("[" + ExternalFunctions.DEFAULT_DATE_FORMAT.format(now) + "] " + object.toString());
    }
}
