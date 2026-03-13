package base.client.event.events.impl.render;


import base.client.event.events.Event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;

public record EventRenderWorld(PoseStack matrixStack, DeltaTracker renderTickCounter) implements Event {
}
