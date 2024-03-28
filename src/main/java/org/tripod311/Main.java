package org.tripod311;

import java.util.Scanner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

public class Main {
    public static void main(String[] args) {
        /*
            Здесь все начинается.
            Context - это контекст исполнения программы. Их может быть много, но в нашем случае нам хватит и одного
         */
        Context ctx = Context.enter();

        /*
            Scope - это пространство имен. Здесь мы описываем корневое.
            Думаю тебе не обязательно знать все детали и подробности, просто знай, что мы создаем
            разные стандартные имена для этого JS движка.
        */
        Scriptable scope = ctx.initStandardObjects();
        /*
            За разъяснениями следующей строки иди в файл ExternalFunctions.java
        */
        ExternalFunctions.putIntoScope(scope);

        /*
            Здесь я просто показываю как вызвать функцию руками из кода. Так тебе скорее всего не надо будет делать,
            но ты можешь тут экспериментировать.
            RhinoException - это общий класс для всех ошибок исполнения. Я так делаю, потому что мне пофигу, что
            именно сломалось. Если сломалось, я это просто логирую и живу дальше.
        */
        try {
            /*
                Этой функцией можно выполнить js код. Аргументы:
                Пространство имен
                Сам код
                Имя скрипта (для дебага)
                номер строки (для дебага)
                securityDomain (Нам не интересно, просто всегда оставляй null)
             */
            ctx.evaluateString(
                    scope,
                    "java.lang.System.out.println('Привет, Максим');",
                    "<Ручной вызов>",
                    1,
                    null
            );
        } catch (final RhinoException e) {
            System.out.println(e);
        }

        /*
            А тут я просто жду ввода от пользователя и исполняю каждую строку. У Intellij ублюдская консоль, надеюсь ты
            разберешься как ей пользоваться).
            Исполняю, пока в пространстве имен не появится переменная с именем EXIT. Чтобы закончить исполнение - напиши
            строку:
            var EXIT = true
            Тогда оно закончит цикл.

            Еще одно пояснение нужно - тут не совсем стандартный javascript.
            Это не браузер и не node.js, поэтому для лога нельзя использовать console.log как в обычных условиях.
            Надо либо java.lang.System.out.println, либо ту функцию, которую я описал в ExternalFunctions.java
            Мою функцию так: ExternalFunctions.log(<все что ты захочешь залогировать>);
         */
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            String script = sc.nextLine();

            try {
                ctx.evaluateString(scope, script, "<cmd>", 1, null);
            } catch (final RhinoException e) {
                System.out.println("ERROR: " + e);
            }

            if (scope.get("EXIT", scope) != Scriptable.NOT_FOUND) {
                exit = true;
            }
        }

        Context.exit();
    }
}