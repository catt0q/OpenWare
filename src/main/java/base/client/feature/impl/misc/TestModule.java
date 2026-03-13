package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.*;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.KeyBindSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.*;
import base.client.feature.Module;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static base.client.feature.impl.exploit.Disabler.C0Fs;
import static base.client.feature.impl.exploit.Disabler.savedabusepacket;
import static base.client.feature.impl.movement.Flight.index2;
import static base.client.helpers.Helper.pc;

public class TestModule extends Module {
    //Топ 16 Лучших переменных
    int index1;
    int index2;
    int index3;
    int index4;
    int index5;
    int index6;
    int index7;
    int index8;
    double indexd1;
    double indexd2;
    double indexd3;
    double indexd4;
    double indexd5;
    double indexd6;
    double indexd7;
    double indexd8;

    long lastpong=0;
    ArrayDeque<LatePacket> C00s=new ArrayDeque<LatePacket>();
    ArrayDeque<Packet> C0Fs=new ArrayDeque<Packet>();
    TimerHelper timerr=new TimerHelper();

    float prevYaw,prevPitch,newYaw,newPitch;
    KeyBindSetting testkey = new KeyBindSetting("testkey", 0, () -> true);
    public TestModule() {

        super("Test", "Чтобы тестить что-то", Type.Misc);
        this.addSettings(testkey);
    }

    public void onEnable() { PacketHelper.Values pc = Client.instance.packet;
        this.index1=0;    this.index2=0;   this.index3=0;    this.index4=0;     this.index5=0;  this.index6=0;     this.index7=0;this.index8=0;
        this.indexd1=0;    this.indexd2=0;  this.indexd3=0;  this.indexd4=0;   this.indexd5=0;    this.indexd6=0;  this.indexd7=0;   this.indexd8=0;
        prevYaw=pc.LastYaw; prevPitch=pc.LastPitch;
        newYaw=pc.LastYaw; newPitch=pc.LastPitch;lastpong=0;
        C0Fs.clear(); C00s.clear();
        super.onEnable();
    }
    public void onDisable() {Client.instance.flagsch.getFlags().clear();
        timerr.reset();
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }


    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {

            yawt = Math.toRadians(mc.player.getYRot());
            xt = -Math.sin(yawt);
            zt = Math.cos(yawt);
            //   xt = 0;     zt = 0;
        }

        double nx = pc.LastPosX + 1000 + Math.random() * 10000;
        double nz = pc.LastPosZ + 1000 + Math.random() * 10000;



        /*
if(mc.player.verticalCollision){
    index1=0;
}
 if (index1==0){
mc.player.jumpFromGround();
    MoveUtil.addmotY(0.13);
MoveUtil.mult2ds(1.4);
}
        */
      /*  if (CombatUtil.fallDistance>3){
            TimerUtil.setTimerspeed(0.1);
            index1=0;
        }
      if (index1==0){
     //       MoveUtil.setmotY(0.75);
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,true),5,true);
        }
index1++;*/


        //    MoveUtil.setmotY(0.01);
        //     pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,true),5);

      /*  if(index1==0){

            TimerUtil.setTimerspeed(0.1);
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,true),5);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
                    0, mc.player.getZ(),pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);
 for(int i=0;i<20;i++){
     pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,true),5);
 }
MoveUtil.stop3();
  }

        if(index1==1){
            TimerUtil.setTimerspeed(1);
       if(index2<5){     MoveUtil.setmotY(0.99);
       }else{   MoveUtil.setmotY(0.42-index2*0.08);  }
     MoveUtil.ssmartstrafe(1.5);
   index2++;
   }
        if(index1==2){
       index1=-1;
        }*/


     /*   if(mc.player.verticalCollision){
            index1=0;
        }
   if(index1==0){
 mc.player.jumpFromGround();
     }
  if(index1%2==0)
        EntityUtil.dtp(xt*1,0,zt*1);*/

      /*  if(mc.player.verticalCollision){
            index1=0;
        }
        if(index1==0){
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true,true),5);
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,true),5);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

        }
 if(index1==2){
     index1=-1;
 }
MoveUtil.setmotY(-0.0001);*/




// if(CombatUtil.fallDistance>2){
   /*  for(int i=0;i<10;i++){
      yawt = Math.toRadians(mc.player.getYRot());  xt = -Math.sin(yawt);     zt = Math.cos(yawt);
double mult=0.062;
     pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
     pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX()+xt*mult,
             mc.player.getY(), mc.player.getZ()+zt*mult,pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);

     pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
  pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX()+xt*mult,
          mc.player.getY(), mc.player.getZ()+zt*mult,pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);

  mc.player.setPos(mc.player.getX()+xt*mult,   mc.player.getY(), mc.player.getZ()+zt*mult);
}*/
  /*   pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
     pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
             mc.player.getY(), mc.player.getZ(),pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);

     pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
     pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),
             mc.player.getY(), mc.player.getZ(),pc.LastYaw,pc.LastPitch, true, mc.player.horizontalCollision), 10, true);
     CombatUtil.fallDistance=0;
 }*/






       /* if(mc.player.verticalCollision){     index3=1;        }

        if(index3==1) {
            if (index1 == 0) {
                TimerUtil.setTimerspeed(index2 < 2 ? 0.1 : 0.8);
                index2++;
            }
            if (index1 == 1) {
                MoveUtil.stop2();

                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX,pc.LastPosY,pc.LastPosZ,true,true),5,true);

                mc.player.jumpFromGround();
            }
            if (index1 == 6) {
                index1 = -1;
            }

            index1++;
        }
*/

      /*  if(index1%20==0){
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX,pc.LastPosY,pc.LastPosZ,true,true),5,false);
            MoveUtil.setmotY(0);
        }else if(mc.player.getDeltaMovement().y<0){
            MoveUtil.setmotY(-0.01);
        }*/


        //       pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX,pc.LastPosY,pc.LastPosZ,true,true),5,false);

      /*  if (index1 == 0) {
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,pc.LastPosY,pc.LastPosZ,pc.LastYaw,pc.LastPitch,true,true),5,true);
MoveUtil.setmotY(0.42);
ChatHelper.addChatMessage(""+mc.player.getY());
   indexd3=mc.player.getY();
        }else{
            MoveUtil.addmotY(0.05);
        }

        if (CombatUtil.fallDistance>2.5) {
     index1=-1;
        }

        MoveUtil.smartstrafe();
        */

      /* else if (index1 == 1) {

            MoveUtil.setmotY(0.42);
        }else{

           if(mc.player.getDeltaMovement().y<-0.1){
               MoveUtil.setmotY(mc.player.getDeltaMovement().y*0.9);
           }else{
               MoveUtil.addmotY(0.05);
           }


        }*/

       /* if(mc.player.horizontalCollision){
EntityUtil.dtp(0,0.42,0);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY(),mc.player.getZ(),false,false),10,true);

            EntityUtil.dtp(0,0.333,0);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY(),mc.player.getZ(),false,false),10,true);


            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY(),mc.player.getZ(),true,false),10,true);
index8=1;

        }*/
      /*  if(mc.player.verticalCollision){
            TimerUtil.setTimerspeed(0.5F);
            double speed=MoveUtil.getspeed2()/Math.sqrt(2);
         //   speed+=0.19;
           // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*speed,mc.player.getY(),mc.player.getZ()+zt*speed, false, mc.player.horizontalCollision), 10, true);

            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*speed,mc.player.getY(),mc.player.getZ()+zt*speed, false, mc.player.horizontalCollision), 10, true);

            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*9,mc.player.getY(),mc.player.getZ()+zt*9, false, mc.player.horizontalCollision), 10, true);
              pc.LastTpNum++;
            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ, pc.LastTpNum, true, false));

            MoveUtil.mult2ds(1.5);      MoveUtil.setmotY(0.42);
            // MoveUtil.setmotY(0.42);
           //pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( false,false),10,true);
          //  MoveUtil.setmotY(0.245); MoveUtil.mult2ds(1.5);

//          speed*=1.6;
//            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*speed,mc.player.getY()+0.42,mc.player.getZ()+zt*speed, false, mc.player.horizontalCollision), 10, true);
//
//            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*9,mc.player.getY()+0.42,mc.player.getZ()+zt*9, true, mc.player.horizontalCollision), 10, true);
//            pc.LastTpNum++;
//            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
//            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ, pc.LastTpNum, true, false));
//
         // MoveUtil.setmotY(0.245);
           // MoveUtil.setmotY(0.42); MoveUtil.mult2ds(1.8);

//MoveUtil.strafe(speed*1.2);



        }*/

        //V1
/*
if(index1==0){
   double speed=MoveUtil.getspeed2()/Math.sqrt(2);
    mc.player.jumpFromGround();
   pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

    EntityUtil.dtp(xt*speed,0,zt*speed); pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY() ,mc.player.getZ(), false, mc.player.horizontalCollision), 10, true);
 speed*=2.5;
   EntityUtil.dtp(xt*speed,0,zt*speed); pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY() ,mc.player.getZ(), true, mc.player.horizontalCollision), 10, true);
   MoveUtil.mult2ds(1.5);
}*/

        /*
if(index1==0){
    for(int i=0;i<1;i++) {
        EntityUtil.dtp(0, 0.42, 0);
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

        EntityUtil.dtp(0, 0.334, 0);
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

        EntityUtil.dtp(0, 0.246, 0);
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

        EntityUtil.dtp(0, 0.163, 0);
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

        EntityUtil.dtp(0, -0.163, 0);
        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);
        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision), 10);

    }
    MoveUtil.stop3();
    MoveUtil.limit2speed(0);    MoveUtil.setmotY(0.42);

    TimerUtil.setTimerspeed(0.1);
}*/


     /*           if(index1==0){
            for(int i=0;i<5;i++) {
                EntityUtil.dtp(0, 0.42, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true), 10);

                EntityUtil.dtp(0, 0.334, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false,true), 10);

                EntityUtil.dtp(0, 0.246, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true), 10);

                EntityUtil.dtp(0, 0.163, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false,true), 10);

//            EntityUtil.dtp(0, 0.081, 0);
//                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true), 10);
//
//                EntityUtil.dtp(0, -0.081, 0);
//                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true), 10);

                EntityUtil.dtp(0, -0.163, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true), 10);
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true, true), 10);
            }
            MoveUtil.stop3();
            MoveUtil.limit2speed(0);    MoveUtil.setmotY(0.42);
MoveUtil.strafe(0);
            TimerUtil.setTimerspeed(0.1);
        }
        index1++;
        */



        //  double speed=0.999;
        // pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()+xt*speed,mc.player.getY(),mc.player.getZ()+zt*speed, false, mc.player.horizontalCollision), 10, true);


      /*  if(index1==0){       TimerUtil.setTimerspeed(3);
    mc.player.jumpFromGround();
    MoveUtil.minsmartstrafe(0.47);
 }

        if(index1==34){
            TimerUtil.setTimerspeed(0.1);
        }

        if(index1==36){
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true, true), 10);
   MoveUtil.setmotY(0.42);    MoveUtil.strafe(2);
       }
if(index1==37){
mc.player.jumpFromGround();
    MoveUtil.setmotY(0.4234);
    MoveUtil.strafe(9.5);
    TimerUtil.setTimerspeed(1);
}
index1++;
*/

        /*if(mc.player.verticalCollision){     index3=1;        }

        if(index3==1) {
            if (index1 == 0) {
                TimerUtil.setTimerspeed(index2 < 2 ? 0.1 : 0.8);
                index2++;
            }
            if (index1 == 1) {
                MoveUtil.stop2();
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, true), 10);
                mc.player.jumpFromGround();

            }
            if (index1 == 11) {
                index1 = -1;
            }

            index1++;
        }
*/
      /*  if(index1==0){       TimerUtil.setTimerspeed(3);
            mc.player.jumpFromGround();
            MoveUtil.minsmartstrafe(0.47);
        }

        if(index1==34){
            TimerUtil.setTimerspeed(0.1);
        }

        if(index1==36){
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(true, true), 10);
            MoveUtil.setmotY(0.42);    MoveUtil.strafe(2);
        }
        if(index1==37){
            mc.player.jumpFromGround();
            MoveUtil.setmotY(0.42);
            MoveUtil.strafe(9.9);
            TimerUtil.setTimerspeed(1);
        }
        if(index1>=37){
    MoveUtil.addmotY(0.0034);
        }

        index1++;*/
     /*   Input ff = mc.player.input.keyPresses;
        pc.sendPacket(new ServerboundPlayerInputPacket(new Input(true, false, ff.left(), ff.right(), true, false,true)));
*/
/*
BlockPos bp=mc.player.blockPosition();
if(index1==0){
ChatHelper.addChatMessage("hi");
    pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, bp, Direction.UP), 10, true);
    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false, false), 10, true);
    for(int i=0;i<30;i++){
        mc.player.swing(InteractionHand.MAIN_HAND);
    }



}*/
       /* if (index1==0) {
            EntityUtil.dtp(xt * 0.01, 0, zt * 0.01);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10, true);
            EntityUtil.dtp(xt * 0.01, 0, zt * 0.01);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10, true);

            double mult1 = 0.025;
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(0.5, 0, 0.5, true, mc.player.horizontalCollision), 10, true);

             pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
      Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));


            EntityUtil.dtp(xt * 0.2, 0, zt * 0.2);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10, true);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10, true);


             pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));
            EntityUtil.dtp(xt * 10, 0, zt * 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10, true);



        }*/




     /*  if(index1%1==0 ){
            double speed=index2!=0 ? 0.06 : 0;


            float prevYaw=mc.player.getYRot();float prevPitch=mc.player.getXRot();
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, pc.LastYaw,pc.LastPitch,  false, mc.player.horizontalCollision), 10, true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, mc.player.getYRot(),mc.player.getXRot(),  false, mc.player.horizontalCollision), 10, true);



            pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),mc.player.getYRot(),mc.player.getXRot(),true), 10);
            pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),mc.player.getYRot(),mc.player.getXRot(),false), 10);


            for(int i=0;i<5;i++) {
    EntityUtil.dtp(xt * speed, 0, zt * speed);
    pc.LastPosX = mc.player.getX();
    pc.LastPosY = mc.player.getY();
    pc.LastPosZ = mc.player.getZ();

    pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),pc.LastYaw,pc.LastPitch,true), 10);

    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, mc.player.getYRot(),mc.player.getXRot(),  pc.LastGround, mc.player.horizontalCollision), 10, true);


}                                 pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10);

            pc.LastPosX = mc.player.getX();
            pc.LastPosY = mc.player.getY();
            pc.LastPosZ = mc.player.getZ();

index2++;
        }
        if(xt!=0 || zt!=0) {
            MoveUtil.minstrafe(0.1);
            MoveUtil.smartstrafe();
            MoveUtil.limit2speed(0.1);
        }*/

        /*if(index1<3){
            double speed=0;


            float prevYaw=mc.player.getYRot();float prevPitch=mc.player.getXRot();
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, pc.LastYaw,pc.LastPitch,  false, mc.player.horizontalCollision), 10, true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, mc.player.getYRot(),mc.player.getXRot(),  false, mc.player.horizontalCollision), 10, true);



            pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),mc.player.getYRot(),mc.player.getXRot(),true), 10);
            pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),mc.player.getYRot(),mc.player.getXRot(),false), 10);


            for(int i=0;i<5;i++) {
                EntityUtil.dtp(xt * speed, 0, zt * speed);
                pc.LastPosX = mc.player.getX();
                pc.LastPosY = mc.player.getY();
                pc.LastPosZ = mc.player.getZ();

                pc.sendPacket(new ServerboundMoveVehiclePacket(mc.player.position(),pc.LastYaw,pc.LastPitch,true), 10);

                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ, mc.player.getYRot(),mc.player.getXRot(),  pc.LastGround, mc.player.horizontalCollision), 10, true);


            }                                 pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, mc.player.horizontalCollision), 10);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10);
MoveUtil.setmotY(0.42);

        }
*/
        /*for(int i=0;i<5;i++) {
            EntityUtil.dtp(xt * 0.01, 0, zt * 0.01);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(), pc.LastYaw, pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
        }
 MoveUtil.stop3();
     TimerUtil.setTimerspeed(0.1F);*/


        //   pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()-4, mc.player.getY()-4, mc.player.getZ()-4, false, mc.player.horizontalCollision), 10);
        //   pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX()-4, 0, mc.player.getZ()-4, true, mc.player.horizontalCollision), 10);

/*
TimerUtil.setTimerspeed(1);
if(index1%1==0) {
double mult1=0.18;
    EntityUtil.dtp(xt * mult1, 0, zt*mult1);
   // pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

    pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), pc.LastGround, mc.player.horizontalCollision), 10, true);

}
            MoveUtil.stop3();*/






        /*
if(mc.player.onGround()) {
 if(MoveUtil.getspeed2()<0.18) {
        MoveUtil.minssmartstrafe(0.18);
    }
index1=0;


}else{
    if(!pc.lastVeltimer.hasTimeElapsed(100,false)) {ChatHelper.addChatMessage("hi");
        MoveUtil.addmotX(xt*0.01);
        MoveUtil.addmotZ(zt*0.01);
    }
}*/

        // pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING), 5, true);


        /*if(CombatUtil.fallDistance>3){  pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING),10,true);

    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( true,false),10,true);
    TimerUtil.setTimerspeed(0.2);
index2=1; CombatUtil.fallDistance=0;MoveUtil.stop3();
}
if(index2==1){MoveUtil.stop3();
    index2=0;
}*/




        // pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING),10,true);

   /* if(mc.player.hurtTime>=8) {
        pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX,pc.LastPosY ,pc.LastPosZ,pc.LastYaw, (float) (pc.LastPitch+0.001), false, mc.player.horizontalCollision), 10);

        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( true,false),10,true);

        pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);

        pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);
        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( false,false),10,true);


    }*/


        //   pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);

        //MoveUtil.stop3(); MoveUtil.smartstrafe(0.03);
      /*  if(index1==0){
   TimerUtil.setTimerspeed(0.1F);
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( false,false),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( true,false),10,true);
            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);

        }
        if(index1==1){


        }*/

      /*  MoveUtil.setmotY(0.42);
        for(int i=0;i<2;i++){
            pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( true,false),10,true);

        }*/


        Input ff=mc.player.input.keyPresses;



        //  pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward() ,ff.backward(),ff.left(),ff.right(),true,ff.shift(),ff.sprint())));

        //  mc.player.jumpFromGround();

       /* HitResult hr=TraceUtil.ClientpickNew(4, 1, false,mc.player.getYRot(),mc.player.getXRot());
        BlockHitResult    bhr = (BlockHitResult)hr ;
        bhr=bhr.withPosition(mc.player.blockPosition().below().below()).withDirection(Direction.DOWN);
    mc.player.swing(InteractionHand.MAIN_HAND);
        pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,

                bhr,

                mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
          hr=TraceUtil.ClientpickNew(4, 1, false,mc.player.getYRot(),mc.player.getXRot());
             bhr = (BlockHitResult)hr ;
        bhr=bhr.withPosition(mc.player.blockPosition().below().below()).withDirection(Direction.UP);

        pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,

                bhr,

                mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
            */

        /*

            InteractionHand hand=InteractionHand.MAIN_HAND;

    Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(),  mc.player.getXRot());
    BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
    PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(hand, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
    mc.player.swing(hand);
    vec = mc.player.calculateViewVector(mc.player.getYRot(), 90);
    blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
    PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(hand, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
    mc.player.swing(hand);


    pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP), 10, true);
    pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP), 10, true);
    pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mc.player.blockPosition().below(), Direction.UP), 10, true);
    pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mc.player.blockPosition().below(), Direction.UP), 10, true);


        */














       /*pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP), 10, true);
  pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mc.player.blockPosition(), Direction.UP), 10, true);
        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, mc.player.blockPosition().below(), Direction.UP), 10, true);
        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, mc.player.blockPosition().below(), Direction.UP), 10, true);
               */



        /*
if(index2==0 && !ACUtil.isground()){
index2=1; index1=0;
}else if(index2==0){
    MoveUtil.stop2();
}*/

        if(index1%2==0){
            //pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision), 10,true);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    pc.LastYaw ,pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);

            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                   pc.LastYaw ,pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
     EntityUtil.dtp(0,0.01,0);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    (float) (pc.LastYaw ),pc.LastPitch, pc.LastGround, mc.player.horizontalCollision), 10, true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);


         }



       /* if(index1==0){
            MoveUtil.setmotY(0.84);
        }
        if(index1==1){
            ChatHelper.addChatMessage(""+mc.player.getDeltaMovement().y);
        }*/





        index1++;



    }//Azarat-Azarat

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        PacketHelper.Values pc= Client.instance.packet;
        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) e.getPacket();


        }
        if (e.getPacket() instanceof ClientboundLoginPacket) {
            C0Fs.clear();C00s.clear();
        }
    }
    @EventTarget
    public void onReceivePacketPre(EventReceivePacketPre e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp=e.getPacket();

        if (packetp instanceof ClientboundSetEntityMotionPacket) {
            if(((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()){
                ClientboundSetEntityMotionPacket s12=(ClientboundSetEntityMotionPacket) e.getPacket();
                //   ChatHelper.addChatMessage(""+s12.getMovement().y);


            }
        }
        if (packetp instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket s08=(ClientboundPlayerPositionPacket) e.getPacket();


        }

    }
    @EventTarget
    public void onTad(EventRunGameLoop e) {
        PacketHelper.Values pc= Client.instance.packet;

    }
    @EventTarget
    public void onTick(EventTick e) {

        if(index8==1){
            mc.player.setOnGround(false); index8=0;
        }else if(index8==2){
            mc.player.setOnGround(true);index8=0;
        }
    }

    @EventTarget
    public void onUpdatee(EventPostMotion e) {
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {

            yawt = Math.toRadians(mc.player.getYRot());
            xt = -Math.sin(yawt);
            zt = Math.cos(yawt);
            //   xt = 0;     zt = 0;
        }

        PacketHelper.Values pc= Client.instance.packet;
     /*  pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);
        Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(), mc.player.getXRot());
        BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
        PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));

        pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10,true);

*/

    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getmovedir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        //TimerUtil.setTimerspeed(0.4F);
        PacketHelper.Values pc= Client.instance.packet;

    /*   Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(), mc.player.getXRot());
        BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
        PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
*/
        if(index1==2) {


            //    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly( false,true),10,true);

          //  ChatHelper.addChatMessage("1 "+index1);
        }



/*
Abilities ab=new Abilities();
ab.apply(new Abilities.Packed(false,false,false,false,false,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY));


      //  PacketHelper.Values.sendPacket(  new ServerboundPlayerAbilitiesPacket(ab) );

        ab.apply(new Abilities.Packed(true,false,true,true,true,1F,1F));

    PacketHelper.Values.sendPacket(
            new ServerboundPlayerAbilitiesPacket(ab)
    );

        PacketHelper.Values.sendPacket(
                new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN,null)
        );*/






        //    PacketHelper.Values.sendPacket(
        //           new ServerboundPlayerAbilitiesPacket(null) );

       /* Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(), 90);
       BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
        PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
*/

       /*   vec = mc.player.calculateViewVector(mc.player.getYRot(), 90);
          blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
        PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
*/



    }

    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp = e.getPacket();

        if(e.getPacket() instanceof ServerboundPongPacket && e.getPermissionidstate()<9) {
            ServerboundPongPacket c0f = (ServerboundPongPacket) packetp;

            lastpong=c0f.getId();


    //ChatHelper.addChatMessage(""+c0f.getId()+ " "+(c0f.getId()%Short.MAX_VALUE));

          //PacketHelper.Values.sendPacket(   new ServerboundPongPacket(-(c0f.getId()%Short.MAX_VALUE)),10 );



        }

        if(e.getPacket() instanceof ServerboundKeepAlivePacket && e.getPermissionidstate()<9) {
            ServerboundKeepAlivePacket c00 = (ServerboundKeepAlivePacket) packetp;

        }

        if(e.getPacket() instanceof ServerboundMovePlayerPacket ) {
            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;

            if(!e.isCanceled() && e.permissionidstate<10){
                if(index5==1){
                    index5=0;
                    e.cancel();

                }

            }




            if(!(c03 instanceof ServerboundMovePlayerPacket.PosRot)) {

            }
        }
        if(e.getPacket() instanceof ServerboundPlayerActionPacket ) {
            ServerboundPlayerActionPacket c07 = (ServerboundPlayerActionPacket) packetp;
            if(c07.getAction().equals(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) && e.permissionidstate<5){
    //e.cancel();
            }else {
              //  ChatHelper.addChatMessage(" C07" );
            }

        }
        if(e.getPacket() instanceof ServerboundUseItemPacket ) {
            ServerboundUseItemPacket c0u = (ServerboundUseItemPacket) packetp;
             if(e.permissionidstate<5){
           //   e.cancel();
   }else{
               //  ChatHelper.addChatMessage(" "+c0u.getHand());
             }

        }




        if(e.getPacket() instanceof ServerboundUseItemOnPacket ) {
            if(e.getPermissionidstate()>0) return;

            ServerboundUseItemOnPacket c08 = (ServerboundUseItemOnPacket) packetp;

            //pc.sendPacket(c08,10,true);

   /*         pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    c08.getHitResult().getBlockPos(), c08.getHitResult().getDirection()
            ),10);
*/


            //   pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player,
            //           ServerboundPlayerCommandPacket.Action.START_FALL_FLYING,0),10);

        }


        if (packetp instanceof ServerboundInteractPacket) {
            ServerboundInteractPacket c02=(ServerboundInteractPacket) packetp;

            base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
            ServerboundInteractPacket.Action action = ac2.getAction();
            if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)){
                Input ff=mc.player.input.keyPresses;
                //  pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),true,ff.sprint())));
                //       pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),false,ff.sprint())));
                //PacketHelper.Values.sendPacket(   new ServerboundPongPacket(-(((int)lastpong)%Short.MAX_VALUE)),10 );

            }




        }



    }
    @EventTarget
    public void onmodify(EventSendPacketModify e) {


    }
    @EventTarget
    public void onmodify(EventSendPacketPost e) {
        Packet<?> packetp = e.getPacket();PacketHelper.Values pc= Client.instance.packet;


        if (packetp instanceof ServerboundInteractPacket) {
            ServerboundInteractPacket c02=(ServerboundInteractPacket) packetp;

            base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
            ServerboundInteractPacket.Action action = ac2.getAction();
            if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)){
             }




        }


        if(e.getPacket() instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;



        /*    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);
            Input ff = mc.player.input.keyPresses;
            pc.sendPacket(new ServerboundPlayerInputPacket(new Input(ff.forward(),ff.backward(), ff.left(), ff.right(),
                    ff.jump(), ff.sprint(),ff.shift())));
            pc.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING), 5, true);
*/
        }
    }
    @EventTarget
    public void onre(EventRenderGui e) {
        GuiGraphics dc=e.dc();
//dc.drawBorder(100,100,200,200,-1);

    }

    public void sendAllC00s() {
        if(C00s.size()==0) return;


        PacketHelper.Values pc= Client.instance.packet;
        while(C00s.size()>0) {
            pc.sendPacket(C00s.pollFirst().getPacket(),10);
        }
    }

    java.util.List<Entity> getTargets() {

        java.util.List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(),false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null && entity.getId() != mc.player.getId() &&
                entity instanceof Player && EntityUtil.getMinDistanceToEntity(entity,mc.player)<2 &&
                entity.isAlive() ).collect(Collectors.toList());


        return targets;
    }

}
