package org.tripod311;

import org.mozilla.javascript.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Новое тут. Это путь до корневой директории со скриптами. Зачем это нужно смотри в import_file здесь же
    * */
    private final Path rootDir;

    public ExternalFunctions (String rootDir) {
        this.rootDir = Paths.get(rootDir).toAbsolutePath();
    }

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
    public static void putIntoScope (Scriptable scope, String rootDir) {
        // Создаем объект, к которому потом будем обращаться
        ExternalFunctions ef = new ExternalFunctions(rootDir);
        // Это не обязательно, но я указываю, что у этого объекта есть объект выше уровнем
        ef.setParentScope(scope);

        // Это список функций, которые потом будут добавлены в объект ExternalFunctions
        // Здесь просто повторяй, я и сам не стал сильно глубоко копаться в деталях
        ArrayList<Method> methodsToAdd = new ArrayList<>();

        try {
            Method factorial = ExternalFunctions.class.getMethod("factorial", Integer.class);
            methodsToAdd.add(factorial);
            Method log = ExternalFunctions.class.getMethod("log", Object.class);
            methodsToAdd.add(log);
            Method import_file = ExternalFunctions.class.getMethod("import_file", String.class);
            methodsToAdd.add(import_file);
            Method passArray = ExternalFunctions.class.getMethod("passArray", NativeArray.class);
            methodsToAdd.add(passArray);
            Method passDouble = ExternalFunctions.class.getMethod("passDouble", Double.class);
            methodsToAdd.add(passDouble);
            Method waitingFunction = ExternalFunctions.class.getMethod("waitingFunction", Double.class, BaseFunction.class);
            methodsToAdd.add(waitingFunction);
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

    public void import_file (String path_to_file) {
        /* Тут внимательно
            Надо обязательно проверять, не пытаются ли вылезти за пределы допустимых границ.
            В интерпретаторе указыватся rootDir - это корневая директория скриптов.
            Если кто-то написал скрипт, который лезет за пределы этой директории - мы его шлем нахрен с ошибкой.
            Остальное ты уже видел
         */

        Path fullPath = Paths.get(this.rootDir + "/" + path_to_file).toAbsolutePath();
        if (fullPath.startsWith(this.rootDir)) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fullPath.toString()), StandardCharsets.UTF_8);
//                FileReader reader = new FileReader(fullPath.toString(), StandardCharsets.UTF_8);
                Context ctx = Context.getCurrentContext();
                ctx.evaluateReader(
                        this.getParentScope(),
                        reader,
                        fullPath.toString(),
                        1,
                        null
                );
            } catch (final FileNotFoundException e) {
                System.out.println("Invalid path (" + path_to_file + "): file not found");
            } catch (final IOException e) {
                System.out.println("Invalid path (" + path_to_file + "): IOException " + e);
            } catch (final RhinoException e) {
                System.out.println("Script error: " + e);
            }
        } else {
            System.out.println("Invalid path (" + path_to_file + "): path ends outside root script directory");
        }
    }

    public void passArray (NativeArray arr) {
        System.out.println("Received array " + arr.size());
        System.out.println("First element is " + arr.get(0).toString());
        System.out.println("Fourth element is " + arr.get(3).toString());
    }

    public void passDouble (Double d) {
        System.out.println("This is double: " + d.toString());
    }

    /*
        Это пример функции, которая ожидает выполнения какой-то долгой задачи и вызывает callBack
     */
    public void waitingFunction (Double wTime, BaseFunction callback) {
        /*
            Перый параметр - это время, которое надо подождать.
            Если бы мне надо было просто подождать время, то я бы использовал setTimeout,
            но тут нужно сымитировать бурную деятельность, которая происходит в другом потоке.

            Так, переменную timeElapsed пришлось так сделать, потому что компилятору что-то не понравилось.
            Тебе так делать не надо, и скорее всего никогда не понадобится, потому что у тебя не ожидание, а проверки

            Видимо мне все же придется что-то тебе объяснять на звонке)
            Вот эти костыли с final массивами не стоит повторять по-хорошему. У меня просто не production код, а
            эксперимент, поэтому я могу писать плохой код, тебе наверное надо будет куда-нибудь в другое место прятать
            эти цифры.
        */
        final Double[] timeElapsed = {0.0};
        final Integer[] intervalId = {0};
        intervalId[0] = EventLoop.getLoopInstance().runInterval(() -> {
            /*
                Вот тут у тебя будет другое. По-хорошему, ты должен в своем коде использовать что-то типа
                if (myMoveToBlockGoal.isTargetReached())

                Ну и так далее. Суть ясна, я думаю, но все равно напишу.
                Этот код будет исполняться раз в какой-то промежуток времени (Eventloop.runInterval это делает)
                Как только выполнится условие в if, этот код будет убран из исполнения и будет вызвана функция callback

                Использовать это нужно так:
                ExternalFunctions.waitingFunction(1000, () => {
                    ExternalFunctions.log("Task completed!");
                });
            * */

            timeElapsed[0] += 100;

            if (timeElapsed[0] >= wTime) {
                EventLoop.getLoopInstance().resetInterval(intervalId[0]);
                callback.call(Context.getCurrentContext(), this, this, new Object[0]);
            }
        }, 100);
    }
}
