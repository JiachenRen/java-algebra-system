package jas.core;


import jas.Function;
import jas.core.operations.Binary;
import jas.core.operations.Custom;
import jas.core.operations.Manipulation;
import jas.core.operations.Unary;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Jiachen on 3/2/18.
 * Assets
 */
public interface Assets {
    String DIGITS = "0123456789.";
    String VARS = "abcdfghjklmnopqrstuvwxyz_";
    String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    static String operators() {
        return Binary.operators();
    }

    static String symbols() {
        return operators() + ",()<>'";
    }

    static boolean isSymbol(char c) {
        return symbols().contains(Character.toString(c));
    }

    static boolean isValidVarName(String s) {
        if (s.isEmpty()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static ArrayList<String> reservedNames() {
        ArrayList<String> names = new ArrayList<>();
        names.addAll(Unary.registeredOperations().stream()
                .map(Function::getName)
                .collect(Collectors.toCollection(ArrayList::new)));
        names.addAll(Custom.registeredManipulations().stream()
                .map(Manipulation::getName)
                .collect(Collectors.toCollection(ArrayList::new)));
        names.add("list");
        return names;
    }
}
