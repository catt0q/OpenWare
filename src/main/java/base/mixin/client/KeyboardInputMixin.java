package base.mixin.client;

import base.client.event.EventManager;
import base.client.event.events.impl.input.EventMoveInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KeyboardInput.class)
    public abstract class KeyboardInputMixin {



    @Redirect(
            method = "tick",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/entity/player/Input"
            )
    )
    public Input redirectInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {

        Input go=((KeyboardInput) (Object) this).keyPresses;
        EventMoveInput event = new EventMoveInput(forward,
                backward,left,right,jump,sneak,sprint);
        EventManager.call(event);
        return new Input(event.isForward(), event.isBackward(), event.isLeft(),
                event.isRight(), event.isJump(), event.isSneak(), event.isSprint());
    }

       //
}
