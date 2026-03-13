package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
public class NoDelay extends Module {

    BooleanSetting NoJumpDelay = new BooleanSetting("NoJumpDelay", true,()->true);
    BooleanSetting NoLeftClickDelay = new BooleanSetting("NoLeftClickDelay", true,()->true);
    BooleanSetting NoBlockHitDelay = new BooleanSetting("NoBlockHitDelay", true,()->true);
    NumberSetting BlockHitMaxDelay = new NumberSetting("BlockHitMaxDelay", 1, 0, 4, 1, () -> true);


    public NoDelay() {
        super("NoDelay", "Убирает задержку", Type.Player);
        this.addSettings(NoJumpDelay,NoLeftClickDelay,NoBlockHitDelay,BlockHitMaxDelay);
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
    public void onUpdate(EventTick e) {
   PacketHelper.Values pc = Client.instance.packet;
if(NoJumpDelay.isEnabled()){
    mc.player.noJumpDelay=0;
}
        if(NoLeftClickDelay.isEnabled()){
            mc.missTime=0;
        }

        if(NoBlockHitDelay.isEnabled()){

                mc.gameMode.destroyDelay= (int) Math.min( mc.gameMode.destroyDelay,BlockHitMaxDelay.getValue());


        }



    }




}
