package base.mixin.client;

import base.client.event.EventManager;
import base.client.event.events.impl.input.EventKeyPress;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    // keyPress signature in 1.21.11: (long window, int action, KeyEvent keyEvent)
    // action: 0 = release, 1 = press, 2 = repeat
    @Inject(method = "keyPress", at = @At(value = "HEAD"))
    private void injectKeyEvent(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
        // allow key events when no screen, or in main menu screens
        Screen screen = Minecraft.getInstance().screen;
        boolean allowed = screen == null
                || screen instanceof TitleScreen
                || screen instanceof SelectWorldScreen
                || screen instanceof JoinMultiplayerScreen;
        if (!allowed)
            return;

        // keyEvent.key() gives the key code
        int key = keyEvent.key();

        // action == 1 means key press (not release or repeat)
        if (action == 1) {
            EventKeyPress eventPost = new EventKeyPress(key);
            EventManager.call(eventPost);
        }
    }
}