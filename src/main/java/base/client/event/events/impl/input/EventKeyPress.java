package base.client.event.events.impl.input;


import base.client.event.events.Event;

public class EventKeyPress implements Event {

    private int key;

    public int getScancode() {
        return scancode;
    }

    public void setScancode(int scancode) {
        this.scancode = scancode;
    }

    private int scancode;

    public EventKeyPress(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
