package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.feature.Module;
import base.client.feature.impl.Type;

public class AutoJump extends Module {
    public AutoJump() {
        super("AutoJump", "Авто прыжок", Type.Player);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }


    @EventTarget
    public void onMoveButton(EventMoveInput e) {
   e.setJump(true);
    }

}
