package jmc.cas.operations;

import jmc.cas.Nameable;
import jmc.cas.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/17/18.
 * Named Manipulation
 */
public class Manipulation implements Nameable, Manipulable {
    private String name;
    private Manipulable manipulable;
    private Signature signature;

    Manipulation(String name, Signature signature, Manipulable manipulable) {
        this.manipulable = manipulable;
        this.signature = signature;
        this.name = name;
    }

    public Operable manipulate(ArrayList<Operable> operands) {
        return manipulable.manipulate(operands);
    }

    public Signature getSignature() {
        return signature;
    }

    public boolean equals(Manipulation other) {
        return other.getName().equals(getName()) && other.signature.equals(signature);
    }

    public String getName() {
        return name;
    }
}
