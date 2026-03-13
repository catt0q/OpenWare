package base.client.event.events.impl.game;

import base.client.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventLook implements Event {
    float yaw, pitch;
}
