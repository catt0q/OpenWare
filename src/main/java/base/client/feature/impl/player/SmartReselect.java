package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventOnStopUsingItem;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.EntityUtil;
import net.minecraft.world.item.*;

public class SmartReselect extends Module {
    int prevslot1 = 0;
    int prevslot = 0;

    public SmartReselect() {
        super("SmartReselect", "Автоматически возвращает нужный слот после использования предмета", Type.Player);

    }
    @Override
    public void onEnable() {
        PacketHelper.Values pc= Client.instance.packet;
        prevslot = -1;
        prevslot1 = pc.prevslotid;
        super.onEnable();
    }
    @Override
    public void onDisable() {

        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {
        PacketHelper.Values pc= Client.instance.packet;
if(prevslot1!=mc.player.getInventory().getSelectedSlot()){
    prevslot=prevslot1;
}
        prevslot1=mc.player.getInventory().getSelectedSlot();

    }

    @EventTarget
    public void onusing(EventOnStopUsingItem event) {
if(prevslot!=-1 && EntityUtil.isDrinking(mc.player) || EntityUtil.isEating(mc.player)){
    mc.player.getInventory().setSelectedSlot(prevslot);
}


    }


}
