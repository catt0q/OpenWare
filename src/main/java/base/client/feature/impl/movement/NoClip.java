package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventCollideEntity;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.motion.EventPushOutOfBlocks;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.player.FreeCam;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WebBlock;

public class NoClip extends Module {
    public NumberSetting verspeed;
    public NumberSetting horspeed;
    public ModeSetting Mode;
    int ticks=0,fallticks=0,upticks=0;

    public static int groundstate=0;//0=nothing 1=noground 2=ground

    public NoClip() {
        super("NoClip", "Позволяет быстро забираться по лестницам и лианам", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix7.16.2", () -> true, "Matrix7.16.2", "Vanilla", "Intave");
            verspeed= new NumberSetting("Vertical Speed", 0.2F, 0.01F, 5F, 0.01F, () -> Mode.getCurrentMode().equals("Vanilla"));
            horspeed= new NumberSetting("Horizontal Speed", 0.2F, 0.01F, 5F, 0.01F, () -> Mode.getCurrentMode().equals("Vanilla"));


        addSettings(Mode, verspeed,horspeed);
    }
    @EventTarget
    public void onCollide(EventCollideEntity event) {
        event.cancel();
    }
    @EventTarget
    public void onPush(EventPushOutOfBlocks event) {
        event.cancel();
    }
    @Override
    public void onEnable() { ticks=0;fallticks=0;upticks=0;
        super.onEnable();
    }
    @Override
    public void onDisable() {MoveUtil.NoCliping=false;
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void Move(EventOnMovePost e) {
        this.setSuffix(Mode.getCurrentMode());

        switch (Mode.getCurrentMode()){
            case ("Vanilla"):MoveUtil.NoCliping=true;
                if(MoveUtil.motYstate()!=0) {
                    MoveUtil.setmotY(MoveUtil.motYstate()>0 ? verspeed.getValue() : -verspeed.getValue());
                }else {
                    MoveUtil.setmotY(0);
                }
                MoveUtil.ssmartstrafe(horspeed.getValue());
             break;

            case ("Matrix7.16.2"):
                MoveUtil.NoCliping=false;




if(ticks>1){
    TimerUtil.setTimerspeed(2);ticks--;
    MoveUtil.NoCliping=true;
    MoveUtil.smartstrafe(0.1);
    groundstate=2;
    MoveUtil.setmotY(0);
}else if(ticks==1){
    TimerUtil.setTimerspeed(1); ticks--;
}

                    if(fallticks==2){
                        ACUtil.send117DuplPacket();

                        for(int i=0;i<4;i++) {
                            EntityUtil.dtp(0, -0.06249, 0);
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, mc.player.getY(), pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
                        }
                        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                        TimerUtil.setTimerspeed(0.2F);fallticks--;
                    }
                   else if(fallticks==1){
                       TimerUtil.setTimerspeed(1F);fallticks--;
                    }

                if(upticks==2){
                    MoveUtil.NoCliping=true;upticks--;
                }
                else if(upticks==1){
                    upticks--;
                }




                    if(MoveUtil.motYstate()==0) {
                if(MoveUtil.getdir()==-1){
                    return;
                }


                    if(mc.player.horizontalCollision && mc.player.verticalCollision && ticks<1){
                        MoveUtil.NoCliping=true;
                        MoveUtil.smartstrafe(0.1);
                        groundstate=2;
                        MoveUtil.setmotY(0);
                        ticks=2;fallticks=0; upticks=0;
                    }



                }else if(MoveUtil.motYstate()==-1){
if(mc.player.verticalCollisionBelow && mc.player.onGround() && pc.lastTptimer.hasTimeElapsed(75,false)){
    MoveUtil.stop3();fallticks=2; upticks=0; ticks=0;
}

                }else if(MoveUtil.motYstate()==1){
                        BlockPos sp=mc.player.blockPosition();
    Block b=mc.level.getBlockState(sp.below()).getBlock();
                        b=mc.level.getBlockState(sp).getBlock();     if((b instanceof AirBlock || b instanceof LiquidBlock)){  return;  }
                        b=mc.level.getBlockState(sp.above()).getBlock();     if((b instanceof AirBlock || b instanceof LiquidBlock)){  return;  }

                        if(  mc.player.verticalCollisionBelow && mc.player.onGround() && pc.lastTptimer.hasTimeElapsed(75,false)){

                            fallticks=0; upticks=2; ticks=0;

                            EntityUtil.dtp(0, 0.42, 0);
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, mc.player.getY(), pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);

                            EntityUtil.dtp(0, 0.333, 0);
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, mc.player.getY(), pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
                            EntityUtil.dtp(0, 0.248, 0);
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, mc.player.getY(), pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
                            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

                            MoveUtil.setmotY(0.165);MoveUtil.stop2();
                        }

                    }

                break;





            case ("Intave"):
               MoveUtil.NoCliping=false;

 if(MoveUtil.motYstate()==0){

     if (!mc.level.getBlockState(mc.player.blockPosition().above()).isAir()) {
         pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                 mc.player.blockPosition().above(), Direction.UP
         ), 10);
     }
     if (!mc.level.getBlockState(mc.player.blockPosition()).isAir()) {
         pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                 mc.player.blockPosition(), Direction.UP
         ), 10);
     }
     pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
             mc.player.blockPosition().below(), Direction.UP
     ), 10);
     pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
     if(mc.player.onGround()){


     }else{
     }





 }else if(MoveUtil.motYstate()<0){



 }







                break;








        }



    }



    @EventTarget
    public void ontick(EventTick event) {
        switch (Mode.getCurrentMode()){
            case ("Matrix"):

                break;
        }



    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {

        switch (Mode.getCurrentMode()){
            case ("Vanilla"):

                break;
        }


    }







}
