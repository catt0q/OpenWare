package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.world.level.block.AirBlock;

public class AirJump extends Module {
    int index1=0;
    int groundstate=0;
    ModeSetting Mode = new ModeSetting("Mode", "Matrix", ()->true, "NoGround" ,  "OnGround","Matrix");
    public NumberSetting jumpdelay = new NumberSetting("Jump Delay", 0, 0, 10, 1, () -> !Mode.getCurrentMode().equals("Matrix"));

    public AirJump() {
        super("AirJump", "Позволяет прыгать по воздуху", Type.Movement);
        this.addSettings(Mode,jumpdelay);
    }

    @Override
    public void onEnable() {
        index1=0;groundstate=0;
        super.onEnable();
    }

    @EventTarget
    public void onJump(EventOnJump event) {
        if(Mode.getCurrentMode().equals("OnGround")) {
            if((!mc.player.verticalCollision || !mc.player.onGround()) && index1!=0) {
                event.setCancelled(true);
            }
        }


    }
    @EventTarget
    public void onTick(EventTick e) {

        if(groundstate==1){
            mc.player.setOnGround(false); groundstate=0;
        }else if(groundstate==2){
            mc.player.setOnGround(true);groundstate=0;
        }
    
  }

    @EventTarget
    public void onPreUpdate(EventPreMotion event) {
        PacketHelper.Values pc= Client.instance.packet;
        if(Mode.getCurrentMode().equals("OnGround")) {
            if (mc.options.keyJump.isDown() && index1>jumpdelay.getValue()) { event.setOnGround(true); mc.player.setOnGround(true); groundstate=2; index1=0;  mc.player.jumpFromGround();         }
        }else 	if(Mode.getCurrentMode().equals("NoGround")) {
            if (mc.options.keyJump.isDown() && index1>jumpdelay.getValue()) {      index1=0;   mc.player.jumpFromGround();      }
        }else if (ACUtil.ismatrixonground() && mc.options.keyJump.isDown()
                && (mc.level.getBlockState(mc.player.blockPosition().above().above()).getBlock() instanceof AirBlock)
                && (MoveUtil.getspeed2()>0.1 || !pc.LastGround)) {
            event.setOnGround(true);    mc.player.setOnGround(true); groundstate=2; CombatUtil.fallDistance =0;
            index1=0;
        }



        index1++;
    }
}
