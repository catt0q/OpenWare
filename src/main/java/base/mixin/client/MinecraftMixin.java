package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.event.events.impl.game.EventShutdownClient;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.game.EventTickPost;
import base.client.event.events.impl.input.EventOnMouseLeftClick;
import base.client.feature.impl.visual.EntityESP;
import base.client.helpers.impl.render.ScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class  MinecraftMixin {
private long tickstarttime=0;
    @Inject(method = "stop", at = @At(value = "HEAD"))
    private void injectStop(CallbackInfo ci) {
        EventManager.call(new EventShutdownClient());
   }

    @Inject(method = "run()V",  at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;handleDelayedCrash()V",shift = At.Shift.AFTER))
    private void hookRunGameLoopEvent(CallbackInfo ci) {
        EventManager.call(new EventRunGameLoop());tickstarttime=System.nanoTime();
    }
    @Inject(method = "run()V",  at = @At(value = "HEAD"))
    private void hookRunGameLoop(CallbackInfo ci) {
        ScreenHelper.frameTime=(System.nanoTime() - tickstarttime) / 1000000D;
    }


    @Inject(method = "tick()V", at = @At(value = "HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        EventTick eventTick = new EventTick();
        EventManager.call(eventTick);

        if (eventTick.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick()V", at = @At(value = "TAIL"), cancellable = true)
    private void tickp(CallbackInfo ci) {
        EventTickPost eventTick = new EventTickPost();
        EventManager.call(eventTick);


    }

    @Inject(method = "handleKeybinds()V", at = @At(value = "HEAD"), cancellable = true)
    private void handleclick(CallbackInfo ci) {
        EventOnMouseLeftClick eventClick = new EventOnMouseLeftClick();
        EventManager.call(eventClick);

    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
    if(!shouldApplyChams(entity)) return;
            cir.setReturnValue(true);
    }
    private boolean shouldApplyChams(Entity ent) {
        return  (
                Client.instance.featureManager.getModuleByClass(EntityESP.class).getState() && EntityESP.espMode.getCurrentMode().equals("Chams") && EntityESP.chamsMode.currentMode.equals("Outline")
                        && (ent instanceof AbstractClientPlayer || !EntityESP.OnlyPlayers.isEnabled()) && !(ent == Minecraft.getInstance().player && !EntityESP.IncludeYourself.isEnabled())

        )
                ;
    }
}
