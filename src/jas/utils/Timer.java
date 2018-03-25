package jas.utils;

/**
 * Created by Jiachen on 3/24/18.
 * Timer
 */
public class Timer {
    private long time;

    public Timer() {
        reset();
    }

    public void reset() {
        time = current();
    }

    private long current() {
        return System.currentTimeMillis();
    }

    public long millis() {
        return current() - time;
    }

    @Override
    public String toString() {
        return millis() + " ms";
    }
}
