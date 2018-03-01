package jmc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jiachen on 9/12/17.
 * Element class that holds info for elements
 */
public enum Element {
    H(1, "Hydrogen", 1.008),
    He(2, "Helium", 4.003),
    Li(3, "Lithium", 6.941),
    C(6, "Carbon", 12.01),
    O(8, "Oxygen", 16.00),

    // ... 90+ others
    ;

    private static class Holder {
        static Map<Integer, Element> map = new HashMap<>();
    }

    private final int atomicNumber;
    private final String fullName;
    private final double atomicMass;

    private Element(int atomicNumber, String fullName, double atomicMass) {
        this.atomicNumber = atomicNumber;
        this.fullName = fullName;
        this.atomicMass = atomicMass;
        Holder.map.put(atomicNumber, this);
    }

    public static ArrayList<Element> getList() {
        ArrayList<Element> elements = new ArrayList<>();
        Holder.map.values().forEach(elements::add);
        return elements;
    }

    public static Element forAtomicNumber(int atomicNumber) {
        return Holder.map.get(atomicNumber);
    }

    public int getAtomicNumber() {
        return atomicNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public double getAtomicMass() {
        return atomicMass;
    }
}
