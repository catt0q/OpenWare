package base.client.event.events.impl.render;

import base.client.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Getter
@Setter
@AllArgsConstructor
public class EventRender3D  implements Event {




Camera camera;
float partial;
Matrix4f matrix4fcamera;
    Matrix4f projection;
    Vector4f vector4f;





}
