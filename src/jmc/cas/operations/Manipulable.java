package jmc.cas.operations;

import jmc.cas.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/17/18.
 * Manipulable
 */
public interface Manipulable {
    Operable manipulate(ArrayList<Operable> operands);
}
