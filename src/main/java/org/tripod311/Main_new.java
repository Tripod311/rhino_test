package org.tripod311;

public class Main_new {
    public static void main (String[] args) {
        String path_to_root = "/home/tripod/projects/testJS/";
        Interpreter i = new Interpreter(path_to_root);

        i.executeString("ExternalFunctions.import_file(\"main.js\");");

        i.close();
    }
}
