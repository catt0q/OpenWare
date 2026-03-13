package base.client.feature.impl.movement.flights.matrix;

import base.client.Client;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.impl.movement.Flight;
import base.client.helpers.impl.misc.ChatHelper;
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

import static base.client.feature.impl.movement.Flight.MRVNOKICK;
import static base.client.helpers.Helper.mc;

public class MatrixRiksFlight {

    static boolean spoofingvanillahardflightcheck;
    public static void onenable(){ 
       spoofingvanillahardflightcheck=false;
    }




    public static EventPreMotion onEventPreMotion(EventPreMotion e) {
        if(MoveUtil.motYstate()>0 ) {
            spoofingvanillahardflightcheck=false;
            mc.player.setOnGround(true);

            MoveUtil.setmotY(0.4234);

            MoveUtil.minsmartstrafe(0.4);
            e.setOnGround(true);
            Flight.groundstate=2;
        }
        return e;
    }


    public static void onEventOnMovePost(EventOnMovePost e, double xt, double zt) {
        List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null && entity!=mc.player &&
                entity instanceof Player &&
                entity.isAlive() && mc.player.distanceTo(entity)<5


        ).collect(Collectors.toList());


        if(mc.player.onGround() && mc.player.verticalCollision) {
            MoveUtil.setmotY(0.42);
            spoofingvanillahardflightcheck=false;
        }else  {
            PacketHelper.Values pc = Client.instance.packet;
            if (MRVNOKICK.isEnabled() && pc.airpackets>50) {

                double posy = ACUtil.findnearestgroundY10();

                if (posy != -1) {

                    if(mc.player.getDeltaMovement().y>-0.999) {
                        MoveUtil.setmotY(-0.999);
                        MoveUtil.limit2speed(0.06);


                    }

                    spoofingvanillahardflightcheck=true;
                }


            }

            if(MoveUtil.motYstate()>=0 && !spoofingvanillahardflightcheck){
                if(!mc.player.onGround() && mc.player.getDeltaMovement().y<0) {
                    if(Flight.index5==1) {

                        MoveUtil.ssmartstrafe(Flight.RFHorSpeed.getValue());

                        if(targets.isEmpty()) {

                                MoveUtil.ssmartstrafe(Flight.RFHorSpeedBoost.getValue());


                        }

                        TimerUtil.reset();
                    }

                    if(MoveUtil.motYstate()<0 &&  mc.level.getBlockState(new BlockPos((int) mc.player.getX(), (int) Math.round(mc.player.getY()-0.5D), (int) mc.player.getZ())).getBlock() instanceof AirBlock) {

                    }else {
                        MoveUtil.setmotY(-0.00000001D);
                    }
                    Flight.index5=1;
                    Flight.index4=1;
                }
            }

        }
        if(mc.player.onGround()) {
            Flight.index3=1;
        }else {
            Flight.index3=0;
        }
    }
    
    
    
}
