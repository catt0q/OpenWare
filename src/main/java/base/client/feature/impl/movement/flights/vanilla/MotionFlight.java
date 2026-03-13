package base.client.feature.impl.movement.flights.vanilla;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.movement.Flight;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AirBlock;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static base.client.helpers.Helper.mc;

public class MotionFlight {







    public static EventPreMotion onEventPreMotion(EventPreMotion e) {
if(Flight.SpoofGround.isEnabled()){
    e.setOnGround(true);
    Flight.groundstate=2;
}
if(Flight.NoCliping.isEnabled()) {
    mc.player.noPhysics = true;
}
        return e;
    }


    public static void onEventOnMovePost() {

        MoveUtil.ssmartstrafe(mc.player.isSprinting() ? Flight.SHorSpeed.getValue() : Flight.HorSpeed.getValue());
        MoveUtil.setmotY(0);
        if(MoveUtil.motYstate()>0) {  MoveUtil.setmotY(Flight.VerSpeed.getValue());    	}
        if(MoveUtil.motYstate()<0) {  MoveUtil.setmotY(-Flight.VerSpeed.getValue());  	}

    }




}
