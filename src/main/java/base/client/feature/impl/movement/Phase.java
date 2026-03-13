package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Phase extends Module {
    public static ModeSetting Mode;
    int index1;
    int index2;
    int index4;
    int flagskip;
    int groundstate=0;
    boolean skipnextc03=false;
    public BooleanSetting Ver188MM;

    public Phase() {
        super("Phase", "Ходим сквозь блоки", Type.Movement);
        Mode = new ModeSetting("Mode", "OldMatrix", () -> true, "OldMatrix",
                  "NewMatrix","MatrixAutoSW");
        Ver188MM = new BooleanSetting("1.8", false, () ->  (Mode.getCurrentMode().equals("OldMatrix")
        || Mode.getCurrentMode().equals("NewMatrix")
        ));

        this.addSettings(Mode,Ver188MM);
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();
        super.onDisable();
    }
    @Override
    public void onEnable() {skipnextc03=false;
        flagskip=0;groundstate=0;
        index1=0;
        index2=0;
        index4=0;

        super.onEnable();
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
    public void onReceivePacket(EventReceivePacketPre event) {
        PacketHelper.Values pc= Client.instance.packet;

        if (this.getState()) {
            if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                ClientboundPlayerPositionPacket s08=(ClientboundPlayerPositionPacket) event.getPacket();
                if(flagskip>0) {
                    pc.sendPacket(new ServerboundAcceptTeleportationPacket(s08.id()));
                    event.setCancelled(true);
                    flagskip--;
                }
            }
        }
    }


    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        // public void Move(EventOnPlayerMovePost eventMove) {
        PacketHelper.Values pc=Client.instance.packet;

        switch (Mode.getCurrentMode()){
            case ("OldMatrix"):
                if(!Ver188MM.isEnabled()) {
                    if (mc.player.horizontalCollision) {
                        MoveUtil.strafe(0);
                        float yaw = (float) Math.toRadians(mc.player.getYRot());
                        double x = mc.player.getX() + -Math.sin(yaw) * 0.00000001;
                        double z = mc.player.getZ() + Math.cos(yaw) * 0.00000001;
                        double mt = 1.9;
                        double x2 = mc.player.getX() + -Math.sin(yaw) * mt;
                        double z2 = mc.player.getZ() + Math.cos(yaw) * mt;

                        index1 = 5;
                        double y = mc.player.getY();
                        TimerUtil.setTimerspeed(0.1);
                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y, z, false, mc.player.horizontalCollision), 10, true);
                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y, z, true, mc.player.horizontalCollision), 10, true);


                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x2, y, z2, false, mc.player.horizontalCollision), 10, true);
                        pc.LastTpNum++;
                        pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x2, y, z2, false, mc.player.horizontalCollision), 10, true);
                        pc.LastTpNum++;
                        pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));

                        mc.player.setPos(x2, y, z2);
                        mc.player.setOnGround(true);
                        groundstate = 2;
                        MoveUtil.strafe(0);
                        index2 = 1;
                        flagskip = 4;
                    } else {
                    index1--;
                    }

                    break;
                   }
            case ("NewMatrix"):
                if(!Ver188MM.isEnabled()) {
if(index1<10){
    event.setYaw(pc.LastYaw); event.setPitch(pc.LastPitch);
}


                }

                break;
        }







    }


    @EventTarget
    public void DDMove(EventOnMovePost eventMove) {
        PacketHelper.Values pc=Client.instance.packet;
switch (Mode.getCurrentMode()){

    case ("OldMatrix"):
        if(Ver188MM.isEnabled()){
            float yaw = (float) Math.toRadians(mc.player.getYRot());



            if (mc.player.horizontalCollision && index1==0) {
                MoveUtil.stop3();

                double x = mc.player.getX() + -Math.sin(yaw) *0.04;
                double z = mc.player.getZ() + Math.cos(yaw) *0.04;


                index1=4;
                double y = pc.LastPosY;
                TimerUtil.setTimerspeed(0.1);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false,mc.player.horizontalCollision),10,true);
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x, y , z, false,mc.player.horizontalCollision),10,true);

                mc.player.setPos(x, y, z);

                index2=1;    index4=4;
            }else {
                if(index4==0) {
                    TimerUtil.reset();
                }else {
                    index4--;
                }

                if(index1>1) {
                    MoveUtil.stop3();
                }
    }
            if(index1==1) {
                MoveUtil.stop3();
                double y = pc.LastPosY;
                double mt=1.9;      double x2 = pc.LastPosX + -Math.sin(yaw) * mt;     double z2 = pc.LastPosZ + Math.cos(yaw) * mt;

                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(x2, y , z2,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);       pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false,mc.player.horizontalCollision),10,true);     pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));

                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(x2, y , z2,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);       pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false,mc.player.horizontalCollision),10,true);     pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));

                mc.player.setPos(x2, y, z2);
                flagskip=1;
            }
            if(index1==2) {
                MoveUtil.stop3();
                double y = pc.LastPosY;
                double mt=0.04;      double x2 = pc.LastPosX + -Math.sin(yaw) * mt;     double z2 = pc.LastPosZ + Math.cos(yaw) * mt;
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(x2, y , z2, false,mc.player.horizontalCollision),10,true);   pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum));

                mc.player.setPos(x2, y, z2);
            }



            if(index1>0) {index1--;}

        }


   break;

    case("NewMatrix"):

        if(Ver188MM.isEnabled()){
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ, pc.LastTpNum, false, false));


            EntityUtil.dtp(0,-0.0624,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);

            EntityUtil.dtp(0,-0.01,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);
             MoveUtil.stop2();
            toggle();
        }else{
            if(index1==0){
                MoveUtil.stop3();
            } else if(index1==1){
     ACUtil.send117DuplPacket();

    for(int i=0;i<4;i++) {
        EntityUtil.dtp(0, -0.06249, 0);
        pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, mc.player.getY(), pc.LastPosZ, false, mc.player.horizontalCollision), 10, true);
    }
    pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10);

    TimerUtil.setTimerspeed(0.2F);
}else if(index1==2){
    toggle();
}

            index1++;

        }







        break;






    case("MatrixAutoSW"):
        if(index2>0){
            index2--;






            if(index2==1) {
                TimerUtil.setTimerspeed(1);
                EntityUtil.dtp(0, -0.062, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ, pc.LastYaw, pc.LastPitch, true, mc.player.horizontalCollision), 10, true);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ, pc.LastYaw, pc.LastPitch, false, mc.player.horizontalCollision), 10, true);

                EntityUtil.dtp(0, -0.01, 0);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ, pc.LastYaw, pc.LastPitch, true, mc.player.horizontalCollision), 10, true);
                pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ, pc.LastYaw, pc.LastPitch, false, mc.player.horizontalCollision), 10, true);

                index1 = 5;
            }

            MoveUtil.stop3();
            return;

        }




        if(index1>0){
            index1--;
        }else{
            if(!mc.player.onGround() || !ACUtil.isground()){
return;
            }

            BlockPos sp=mc.player.blockPosition();



            Block b=mc.level.getBlockState(sp.below()).getBlock();
            if(b instanceof AirBlock || b instanceof LiquidBlock){   return;  }
     b=mc.level.getBlockState(sp.east()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.west()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.north()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.south()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }

            b=mc.level.getBlockState(sp).getBlock();     if(!(b instanceof AirBlock || b instanceof LiquidBlock)){  return;  }


            b=mc.level.getBlockState(sp.above().east()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.above().west()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.above().north()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }
            b=mc.level.getBlockState(sp.above().south()).getBlock();     if(b instanceof AirBlock || b instanceof LiquidBlock){  return;  }

           b=mc.level.getBlockState(sp.above()).getBlock();     if(!(b instanceof AirBlock || b instanceof LiquidBlock)){  return;  }

            if(!pc.lastTptimer.hasTimeElapsed(2000,false)){
                return;
            }

       /*that work btw but not above air     EntityUtil.dtp(0,-0.0624,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);
            flagskip++;

            EntityUtil.dtp(0,-0.01,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);
            flagskip++;
            MoveUtil.stop2();*/

            EntityUtil.dtp(0,-0.062,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);

            EntityUtil.dtp(0,-0.01,0);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, true,mc.player.horizontalCollision),10,true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, mc.player.getY(), pc.LastPosZ,pc.LastYaw,pc.LastPitch, false,mc.player.horizontalCollision),10,true);

index2=10;
             MoveUtil.stop3();



        }

















        break;



}


    }


    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        if (e.getPacket() instanceof ServerboundMovePlayerPacket) {

            switch (Mode.getCurrentMode()){
                case ("NewMatrix"):
                    if(!Ver188MM.isEnabled()){
                        skipnextc03=true;
                    }


                    break;

            }

            if(skipnextc03){skipnextc03=false;
                e.setCancelled(true);
                return;
            }

        }

    }





  

}
