package jmc.cas;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jiachen on 3/15/18.
 * Manipulation
 */
public abstract class Manipulation implements Operable, Nameable {
    private ArrayList<Operable> operands;
    private String name;

    public Manipulation(String name, Operable operands) {
        this.operands = new ArrayList<>();
        Collections.addAll(this.operands, operands);
        this.name = name;
    }

    public ArrayList<Operable> getOperands() {
        return operands;
    }

    public String getName() {
        return this.name;
    }
}
