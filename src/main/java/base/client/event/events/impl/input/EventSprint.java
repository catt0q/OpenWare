package base.client.event.events.impl.input;

import base.client.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventSprint extends EventCancellable {
   boolean sprint;
}
