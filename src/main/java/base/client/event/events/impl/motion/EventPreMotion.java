package base.client.event.events.impl.motion;

import base.client.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventPreMotion extends EventCancellable {
    private double posX, posY, posZ;
    private float yaw, pitch;
    private boolean onGround;
    private boolean horcol;
}
