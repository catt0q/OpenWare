package base.mixin.client;

import base.client.Client;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public class TickDynamicMixin {
    @Shadow
    private float deltaTicks;
    @Shadow
    private float deltaTickResidual;
    @Shadow private long lastMs;
    @Final
    @Shadow private float msPerTick;

    @Inject(method = "Lnet/minecraft/client/DeltaTracker$Timer;advanceGameTime(J)I", at = @At("HEAD"), cancellable = true)
    private void beginRenderTickHook(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if(Client.timerspeed == 1)
            return;

        this.deltaTicks = ((timeMillis - this.lastMs) / this.msPerTick) * Client.timerspeed;
        this.lastMs = timeMillis;
        this.deltaTickResidual += this.deltaTicks;
        int i = (int) this.deltaTickResidual;
        this.deltaTickResidual -= i;
        cir.setReturnValue(i);
    }
}