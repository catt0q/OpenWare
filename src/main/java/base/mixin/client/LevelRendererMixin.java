package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.render.EventRender3D;
import base.client.event.events.impl.render.EventRenderBlockEntities;
import base.client.feature.impl.visual.EntityESP;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    // main render hook - fires after level rendering completes
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelLast(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker,
            boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3,
            GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        EventRender3D event = new EventRender3D(camera, deltaTracker.getGameTimeDeltaPartialTick(false), matrix4f,
                matrix4f2, vector4f);
        EventManager.call(event);
    }

    // hook before entities are submitted for rendering (for chams setup)
    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void beforeSubmitEntities(PoseStack poseStack, LevelRenderState levelRenderState,
            SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (!shouldApplyChams())
            return;

        // enable polygon offset for chams effect
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0F, -1000000F);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    // hook after entities are submitted (chams cleanup)
    @Inject(method = "submitEntities", at = @At("RETURN"))
    private void afterSubmitEntities(PoseStack poseStack, LevelRenderState levelRenderState,
            SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (!shouldApplyChams())
            return;

        // restore GL state
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPolygonOffset(0, 0);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    // hook for block entities - fires after block entities are submitted
    @Inject(method = "submitBlockEntities", at = @At("RETURN"))
    private void afterSubmitBlockEntities(PoseStack poseStack, LevelRenderState levelRenderState,
            SubmitNodeStorage submitNodeStorage, CallbackInfo ci) {
        // fire event for ESP and other visual features that need block entity info
        // note: we don't have direct access to camera/partial tick here
        // but LevelRenderState contains the necessary render state info
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null && mc.level != null && mc.player != null) {
            Camera camera = mc.gameRenderer.getMainCamera();
            if (camera == null)
                return;
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

            // create event with available info
            EventRenderBlockEntities event = new EventRenderBlockEntities(camera, partialTick, poseStack, null);
            EventManager.call(event);
        }
    }

    private boolean shouldApplyChams() {
        return (Client.instance.featureManager.getModuleByClass(EntityESP.class).getState()
                && EntityESP.espMode.getCurrentMode().equals("Chams")
                && EntityESP.chamsMode.currentMode.equals("OutRender"));
    }
}