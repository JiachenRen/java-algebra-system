package jas.core.operations;

import jas.core.Operable;

import java.util.ArrayList;

/**
 * Created by Jiachen on 3/17/18.
 * Manipulable
 */
public interface Manipulable {
    Operable manipulate(ArrayList<Operable> operands);
}
