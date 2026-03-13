package base.mixin.client;

import base.client.helpers.impl.misc.ChatHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import base.client.Client;
import base.client.feature.impl.player.FreeCam;
import base.client.feature.impl.visual.ClientRotations;
import base.client.feature.impl.visual.EntityESP;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.RotationUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.opengl.GlStateManager;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    private T currentEntity;
    public float prevYaw = 0;
    public float prevPitch = 0;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onUpdateRenderState(T livingEntity, S livingEntityRenderState, float deltaTickResidual,
            CallbackInfo ci) {
        if (!(Minecraft.getInstance().player != null
                && ((Entity) (Object) livingEntity).getId() == Minecraft.getInstance().player.getId()))
            return;
        if (!Client.instance.featureManager.getModuleByClass(ClientRotations.class).getState())
            return;
        if (Client.instance.featureManager.getModuleByClass(FreeCam.class).getState())
            return;

        if (Minecraft.getInstance().screen instanceof InventoryScreen)
            return;
        PacketHelper.Values pc = Client.instance.packet;
        if (ClientRotations.visualYaw.isEnabled()) {

            float newyaw = RotationUtils.lerpAngle(deltaTickResidual, prevYaw, pc.LastYaw);

            livingEntity.yHeadRotO = newyaw;
            livingEntity.yHeadRot = newyaw;
            if (ClientRotations.bodyLockYaw.isEnabled()) {
                livingEntity.yBodyRot = newyaw;
            }
            prevYaw = newyaw;
        }

    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onUpdateRenderStatePost(T livingEntity, S livingEntityRenderState, float deltaTickResidual,
            CallbackInfo ci) {
        if (!(Minecraft.getInstance().player != null
                && ((Entity) (Object) livingEntity).getId() == Minecraft.getInstance().player.getId()))
            return;
        if (!Client.instance.featureManager.getModuleByClass(ClientRotations.class).getState())
            return;
        if (Client.instance.featureManager.getModuleByClass(FreeCam.class).getState())
            return;
        if (Minecraft.getInstance().screen instanceof InventoryScreen)
            return;
        if (ClientRotations.visualPitch.isEnabled()) {

            PacketHelper.Values pc = Client.instance.packet;
            livingEntityRenderState.xRot = pc.LastPitch;
            if (livingEntityRenderState.isUpsideDown) {
                livingEntityRenderState.xRot *= -1.0F;
            }
            prevPitch = pc.LastPitch;
        }
        this.currentEntity = livingEntity;
    }

    // render method replaced by submit in 1.21.11
    // new signature: submit(S state, PoseStack stack, SubmitNodeCollector
    // collector, CameraRenderState cameraState)
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    private void beforeSubmit(S state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraState,
            CallbackInfo ci) {

        if (!shouldApplyChams())
            return;

        // todo: GL state manipulation may need updating for new render pipeline
        // currently disabled pending investigation of new rendering system
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("RETURN"))
    private void afterSubmit(S state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState, CallbackInfo ci) {

        if (!shouldApplyChams())
            return;
        // todo: chams cleanup for new render pipeline
    }

    private boolean shouldApplyChams() {
        return (Client.instance.featureManager.getModuleByClass(EntityESP.class).getState()
                && EntityESP.espMode.getCurrentMode().equals("Chams")
                && EntityESP.chamsMode.currentMode.equals("OutRender")
                && (currentEntity instanceof AbstractClientPlayer || !EntityESP.OnlyPlayers.isEnabled())
                && !(currentEntity == Minecraft.getInstance().player && !EntityESP.IncludeYourself.isEnabled())

        );
    }

}
