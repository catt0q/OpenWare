package base.client.event.events.impl.motion;

import base.client.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventOnMove implements Event
{
    float yaw;
    float speed;

}