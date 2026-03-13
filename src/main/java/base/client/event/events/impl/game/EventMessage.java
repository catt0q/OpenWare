package base.client.event.events.impl.game;


import base.client.event.events.Event;
import base.client.event.events.callables.EventCancellable;

public class EventMessage extends EventCancellable implements Event {

    public String message;

    public EventMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

