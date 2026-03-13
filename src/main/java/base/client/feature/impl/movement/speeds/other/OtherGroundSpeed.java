package base.client.feature.impl.movement.speeds.other;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.impl.combat.Criticals;
import base.client.feature.impl.movement.AirStuck;
import base.client.feature.impl.movement.Sprint;
import base.client.feature.impl.movement.Step;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static base.client.feature.impl.movement.Speed.*;
import static base.client.helpers.Helper.mc;

public class OtherGroundSpeed {


    public static EventOnJump onEventOnJump(EventOnJump e) {
           e.cancel();
        return e;
    }

    public static void onEventOnMovePost(double xt,double zt) {
        PacketHelper.Values pc = Client.instance.packet;
     if(mc.player.onGround() && mc.player.verticalCollision){
         switch (GrMode.getCurrentMode()){
             case ("Strafe"):
                 MoveUtil.smartstrafe(GroundStrafe.getValue());
                 break;
             case ("MotionBoost"):
                 MoveUtil.addmotXZ(GroundBst.getValue()*xt,GroundBst.getValue()*zt);
                 break;
             case ("MotionMult"):
                 MoveUtil.mult2ds(GroundMult.getValue());
                 break;



         }



     }




    }



}
