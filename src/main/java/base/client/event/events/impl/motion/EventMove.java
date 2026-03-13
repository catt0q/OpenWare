package base.client.event.events.impl.motion;

import base.client.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
@AllArgsConstructor
public class EventMove implements Event {
    private Vec3 motion;
    private boolean cancelled;
    private boolean safeWalk;

    public EventMove(Vec3 motion) {
        this.motion = motion;
        this.cancelled = false;
        this.safeWalk = false;
    }
}
