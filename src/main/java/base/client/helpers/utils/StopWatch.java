package base.client.helpers.utils;

public class StopWatch {
    long lastTime;

    public StopWatch() {
        reset();
    }

    public long reached() {
        return System.currentTimeMillis() - lastTime;
    }

    public boolean reached(long time) {
        return reached() >= time;
    }

    public void reset() {
        lastTime = System.currentTimeMillis();
    }
}
