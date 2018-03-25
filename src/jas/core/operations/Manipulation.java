package jas.core.operations;

import jas.core.Nameable;
import jas.core.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/17/18.
 * Named Manipulation
 */
public class Manipulation implements Nameable, Manipulable {
    private String name;
    private Manipulable manipulable;
    private Signature signature;

    public Manipulation(String name, Signature signature, Manipulable manipulable) {
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

    public boolean equals(String name, Signature signature) {
        return name.equals(getName()) && getSignature().equals(signature);
    }

    public String toString() {
        return getName() + "(" + signature.toString() + ")";
    }

    public String getName() {
        return name;
    }
}
