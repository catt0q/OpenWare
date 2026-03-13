package base.client.event.events.impl.game;

import base.client.event.events.Event;

public class EventOnSprint implements Event {


    private boolean issprint;

    public EventOnSprint(  boolean issprint) {
        this.issprint = issprint;
    }

    public boolean issprint() {
        return issprint;
    }

    public void setsprint(boolean issprint) {
        this.issprint = issprint;
    }

}