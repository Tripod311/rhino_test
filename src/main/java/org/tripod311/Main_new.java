package org.tripod311;

import java.util.Scanner;

public class Main_new {
    public static void main (String[] args) {
        String path_to_root = "/home/tripod/projects/testJS/";
        Interpreter i = new Interpreter(path_to_root);

//        i.executeString("ExternalFunctions.import_file(\"main.js\");");

        /*
            Поясняю
            Тут цикл, который ждет исполнения. EventLoop который я сделал - это не совсем EventLoop,
            это на самом деле небольшой костыль, который выносит работу со скриптами в отдельный поток.
            Если ты захочешь подробных разъяснений про многопоточность - напиши, я объясню тебе все.
            По сути мы тут делаем то же самое, только теперь можно попросить интерпретатор исполнить
            Async.setTimeout(fn, delay);
            Async.clearTimeout(timeoutId);
            Async.setInterval(fn, delay);
            Async.clearInterval(intervalId);
            и это будет аналогично (почти) джаваскриптовому setTimeout, setInterval
        * */
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            String script = sc.nextLine();

            if (script.equals("exit")) {
                exit = true;
            } else {
//                long startTime = System.nanoTime();
                i.executeString(script);
//                long endTime = System.nanoTime();
//                System.out.println("Evaluation time: " + (endTime - startTime)/1000000);
            }
        }

        i.close();
    }
}
