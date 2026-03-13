package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketModify;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.movement.Flight;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public class Criticals extends Module {

boolean matrixminimizespeed=false;
    boolean wascrit=false;
    private final ModeSetting Mode;
    private static final TimerHelper timer = new TimerHelper();

    public static NumberSetting Delay;
    private static ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Always", () -> true, "Only Ground", "Only Air", "Always");

    public Criticals() {
        super("Criticals", "Автоматически наносит сущности критичиский урон при ударе", Type.Combat);
        Mode = new ModeSetting("Mode", "MatrixV1", () -> true, "MatrixV1","MatrixV2","MatrixDamage","Nano1.17","Test");
        this.Delay = new NumberSetting("Delay", 200, 0, 2000, 10,()->true );

        addSettings(Mode,GroundCondition,Delay);
    }

    @Override
    public void onEnable() {timer.reset();
        matrixminimizespeed=false;wascrit=false;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void Move(EventOnMovePost e) {
        if(matrixminimizespeed){
            MoveUtil.limit2speed(ACUtil.matrixisusingitem() ? 0.1 : 0.15);

            matrixminimizespeed=false;
        }
        switch (Mode.getCurrentMode()) {
            case ("MatrixV2"):


                break;
        }

    }


    @EventTarget
    public void oncancel(EventSendPacketCancel e) {


    }
    @EventTarget
    public void onmodify(EventSendPacketModify e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp = e.getPacket();





        if (packetp instanceof ServerboundInteractPacket) {
            ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;
            base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
            ServerboundInteractPacket.Action action = ac2.getAction();
            if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)) {
                canCrit();
timer.reset();

switch (Mode.getCurrentMode()) {
    //ававхахав лол чё это????
    case ("MatrixV1"):
        if (mc.player.onGround() || mc.player.getDeltaMovement().y>0) {

      //  double nx=pc.LastPosX+1000+Math.random()*10000;   double nz=pc.LastPosZ+1000+Math.random()*10000;
double deltaY=0.0001;
     //EntityUtil.dtp(0,-deltaY,0);
          //  pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10 );

         //   pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);
       //     pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(),pc.LastYaw,pc.LastPitch, false, mc.player.horizontalCollision), 10);

            pc.sendPacket(new ServerboundMoveVehiclePacket(new Vec3(pc.LastPosX,pc.LastPosY-deltaY,pc.LastPosZ),pc.LastYaw,pc.LastPitch
                    ,pc.LastGround), 10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos (pc.LastPosX,pc.LastPosY-deltaY,pc.LastPosZ ,pc.LastGround, mc.player.horizontalCollision), 10,true);
            pc.sendPacket(new ServerboundMoveVehiclePacket(new Vec3(pc.LastPosX,pc.LastPosY-deltaY,pc.LastPosZ),pc.LastYaw,pc.LastPitch
                    ,pc.LastGround), 10,true);

            // EntityUtil.dtp(0,0.03,0);

wascrit=true;

    }
 break;

    case ("MatrixV2"):
        if (mc.player.onGround() || mc.player.getDeltaMovement().y>0) {
            double nx=pc.LastPosX+1000+Math.random()*10000;
            double nz=pc.LastPosZ+1000+Math.random()*10000;
            if(pc.LastGround) {
   pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

                  pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,
                        pc.LastPosY-0.001, pc.LastPosZ,pc.LastYaw,pc.LastPitch, false, mc.player.horizontalCollision), 10, true);
                pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

            }else if(mc.player.getDeltaMovement().y<0.3){



                pc.sendPacket(ACUtil.matrixflagpacket(),10,true);
                pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ,pc.LastTpNum,true,true));


             pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ,false,mc.player.horizontalCollision),10,true); Disabler.savedabusepacket--;

                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(nx,
                        pc.LastPosY, nz, pc.LastGround, mc.player.horizontalCollision), 10, true);
                pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
                Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ,pc.LastTpNum,true,true));
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY-0.06, pc.LastPosZ,false,mc.player.horizontalCollision),10,true); Disabler.savedabusepacket--;


                mc.player.setPos(pc.LastPosX, pc.LastPosY, pc.LastPosZ);
                MoveUtil.setmotY(-0.078);
                matrixminimizespeed=true;
   }




            wascrit = true;
        }
        break;

    case ("MatrixDamage"):
        if (mc.player.onGround() || mc.player.getDeltaMovement().y>0) {

            if(pc.lastVeltimer.hasTimeElapsed(1800,false)){
                 return;
            }
            if(mc.player.onGround()) {

                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY+0.023, pc.LastPosZ,pc.LastYaw,pc.LastPitch, false, mc.player.horizontalCollision), 10, true);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY+0.011, pc.LastPosZ,pc.LastYaw,pc.LastPitch, false, mc.player.horizontalCollision), 10, true);

            }else{
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY-0.011, pc.LastPosZ,pc.LastYaw,pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
MoveUtil.setmotY(-0.08);
            }
MoveUtil.stop2();





            wascrit = true;
        }
        break;
    case ("Nano1.17"):
        double diff=0.0001;
        if (ACUtil.isground()) {
            pc.LastPosY+=diff;
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,(float) (pc.LastYaw+(Math.random()*0.001)),pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
    }
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY-diff, pc.LastPosZ, (float) (pc.LastYaw+(Math.random()*0.001)),pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
 wascrit=true;

        break;
}

            }
        }

    }
    @EventTarget
    public void onmodify(EventSendPacketPost e) {
        Packet<?> packetp = e.getPacket();PacketHelper.Values pc= Client.instance.packet;


        if (packetp instanceof ServerboundInteractPacket) {
            ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;
            base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
            ServerboundInteractPacket.Action action = ac2.getAction();
            if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)) {
                canCrit();
                switch (Mode.getCurrentMode()) {
                    case ("MatrixV1"):
                        if (wascrit) {
                       /*     if(mc.player.onGround() && MoveUtil.motYstateh()) {
                                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision), 10, true);
                                MoveUtil.mult2ds(0.9);
                            }*/
                            double deltaY=0.0001;
                            if(mc.player.onGround()){
                                EntityUtil.dtp(0,deltaY,0);
                                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10);

                            }

                        }

                        break;
                    case ("MatrixV2"):
                        if (wascrit) {//ChatHelper.addChatMessage(" "+(mc.player.onGround() && MoveUtil.motYstateh()));
if(pc.LastGround && !mc.player.onGround()){
    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,
            pc.LastPosY, pc.LastPosZ,pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);
    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
}else{
   // pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10,true); Disabler.savedabusepacket--;

}

                         wascrit = false;
                        }

                        break;
                    case ("Nano1.17"):
                        if(wascrit) {
                        }
                        break;
                    case ("MatrixDamage"):
                        if(!mc.player.onGround()) {
                            //   pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, pc.LastYaw, pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
                        }
                        break;
                }
            }
        }

    }



    public static boolean canCrit() {
        PacketHelper.Values pc= Client.instance.packet;

        if((Client.instance.featureManager.getModuleByClass(Flight.class).getState())){
            return false;
        }

        if(mc.player.onClimbable() || mc.player.hasEffect(MobEffects.BLINDNESS)
                || mc.player.isInWater()
                || mc.player.isVehicle()){
            return false;
        }
        if(!isCondition(pc)){
            return false;
        }



        if(!timer.hasTimeElapsed((long)Delay.getValue(),false)){
            return false;
        }



        return true;
    }

    public static boolean isCondition(PacketHelper.Values pc) {
        if(GroundCondition.getCurrentMode().equals("Only Ground") && !pc.LastGround) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && pc.LastGround) { 	return false; 	}
        return true;
    }

}
