package jmc.cas.operations;

import jmc.cas.Nameable;
import jmc.cas.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/17/18.
 * Registered Manipulation
 */
public class RegisteredManipulation implements Nameable, Manipulation {
    private String name;
    private Manipulation manipulation;
    private Signature signature;

    RegisteredManipulation(String name, Signature signature, Manipulation manipulation) {
        this.manipulation = manipulation;
        this.signature = signature;
        this.name = name;
    }

    public Operable manipulate(ArrayList<Operable> operands) {
        return manipulation.manipulate(operands);
    }

    public Signature getSignature() {
        return signature;
    }

    public boolean equals(RegisteredManipulation other) {
        return other.getName().equals(getName()) && other.signature.equals(signature);
    }

    public String getName() {
        return name;
    }
}
