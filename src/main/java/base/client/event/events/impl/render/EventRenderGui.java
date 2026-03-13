package base.client.event.events.impl.render;


import base.client.event.events.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public record EventRenderGui(GuiGraphics dc, DeltaTracker rtc) implements Event {
}
