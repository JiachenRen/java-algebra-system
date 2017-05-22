package jmc_lib;

/**
 * Created by Jiachen on 19/05/2017.
 */
public class InterpretedFunction extends Function {
    private Operable operable;

    public InterpretedFunction(Operable operable) {
        this(operable, "");
    }


    public InterpretedFunction(Operable operable, String name) {
        this(operable, name, false);
    }

    public InterpretedFunction(Operable operable, String name, boolean dynamic) {
        super(name, dynamic);
        this.operable = operable;
    }

    @Override
    public double eval(double val) {
        return operable.eval(val);
    }

    public Operable getOperable() {
        return operable;
    }

    public void setOperable(Operable operable) {
        this.operable = operable;
    }
}
