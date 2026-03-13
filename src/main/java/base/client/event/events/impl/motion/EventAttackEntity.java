package base.client.event.events.impl.motion;

import base.client.event.events.Event;
import net.minecraft.world.entity.Entity;

/**
 * Fired before the player attacks an entity.
 * Cancel this event to prevent the attack.
 */
public class EventAttackEntity implements Event {
    private final Entity target;
    private boolean cancelled = false;

    public EventAttackEntity(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
