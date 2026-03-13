package base.client.event.events.impl.render;

import base.client.event.events.Cancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
@AllArgsConstructor
public class EventCameraPosUpdate  extends Cancellable {

    Vec3 position;

}
