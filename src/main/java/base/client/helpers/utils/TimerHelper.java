package base.client.helpers.utils;

public class TimerHelper
{
    public long lastMS;

    public long getLastMS() {
        return lastMS;
    }

    public void setLastMS(long lastMS) {
        this.lastMS = lastMS;
    }

    public TimerHelper() {
        this.lastMS = System.currentTimeMillis();
    }

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed( long time, final boolean reset) {
        if (System.currentTimeMillis() - this.lastMS > time) {
            if (reset==true) {
                this.lastMS = System.currentTimeMillis();
            }
            return true;
        }else {
            return false;
        }

    }
    public void setMs(long ms) {
        this.lastMS = System.nanoTime() - ms * 1000000L;
    }

    public boolean hasReached(float milliseconds) {
        return System.currentTimeMillis() - lastMS > milliseconds;
    }
    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }
}