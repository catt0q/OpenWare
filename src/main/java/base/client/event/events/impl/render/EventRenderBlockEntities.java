package base.client.event.events.impl.render;

import base.client.event.events.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;

@Getter
@Setter
@AllArgsConstructor
public class EventRenderBlockEntities implements Event {




Camera camera;
float partial;
PoseStack poseStack;
    MultiBufferSource.BufferSource bufferSource;




}
