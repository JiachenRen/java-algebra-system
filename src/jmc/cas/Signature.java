package jmc.cas;

/**
 * Created by Jiachen on 3/17/18.
 * Signature
 */
public class Signature {
    private Argument[] args;

    Signature(Argument... args) {
        this.args = args;
    }

    public boolean equals(Signature other) {
        if (other.args.length != this.args.length) return false;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].equals(other.args[i]))
                return false;
        }
        return true;
    }

}
