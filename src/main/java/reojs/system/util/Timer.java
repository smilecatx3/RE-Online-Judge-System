package reojs.system.util;

import java.util.concurrent.TimeUnit;


public class Timer {
    private long startTime = -1;
    private long elapsedTime = -1;


    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * @throws IllegalStateException If the timer has not been started.
     */
    public void stop() {
        checkTimerState();
        elapsedTime = System.nanoTime() - startTime;
    }

    /**
     * Gets the elapsed time in the specified time unit.
     * @throws IllegalStateException If the timer has not been started.
     */
    public long getElapsedTime(TimeUnit t) {
        checkTimerState();
        return t.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    private void checkTimerState() {
        if (startTime < 0) {
            throw new IllegalStateException("The timer has not been started.");
        }
    }
}
