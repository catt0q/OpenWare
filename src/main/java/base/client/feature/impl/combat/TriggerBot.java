package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventOnMouseLeftClick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
public class TriggerBot extends Module {

    public TriggerBot() {
        super("TriggerBot", "Автоматически бьет людей когда целишься на них", Type.Combat);
    }

    @EventTarget
    public void onClick(EventOnMouseLeftClick e) {
        if (mc.crosshairPickEntity != null && mc.player.getAttackStrengthScale(0.5f) > 0.9) {
            mc.startAttack();
        }
    }
}
