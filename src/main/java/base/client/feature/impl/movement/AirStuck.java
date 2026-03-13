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
import base.client.feature.impl.player.NoFall;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayDeque;

public class AirStuck extends Module {
    float yaw111;
    float pitch111;
    double motX;
    double motY;
    double motZ;
    int index1;
    int index3;
    double index2;
    int flagcount=0;


    public BooleanSetting OnGround = new BooleanSetting("OnGround", true,() -> true);
    public BooleanSetting SaveMotion = new BooleanSetting("Save Motion", false,() -> true);
    public BooleanSetting NoMove = new BooleanSetting("No Move", false,() -> true);
    public BooleanSetting NoC03= new BooleanSetting("NoC03", false,() -> true);
    public ModeSetting RotMode = new ModeSetting("Rotation Bypass", "1.17 Packet",() -> true, "None", "No Rotate",  "1.17 Packet",  "Flag"    );

    ArrayDeque<Packet> LatePacketss=new ArrayDeque<Packet>();

    public AirStuck() {
        super("AirStuck", "Вы зависаете в воздухе", Type.Movement);
        this.addSettings(RotMode,this.OnGround, this.SaveMotion,this.NoMove,this.NoC03  );
    }



    @Override
    public void onEnable() {

        LatePacketss.clear();
        motX = mc.player.getDeltaMovement().x();
        motY =mc.player.getDeltaMovement().y();
        motZ = mc.player.getDeltaMovement().z();

        yaw111 = mc.player.getYRot();
        pitch111 = mc.player.getXRot();
        index2 = 0;
        index1=0;
        index3=0;
        flagcount=0;
        if(NoMove.isEnabled()) {
            MoveUtil.stop3();
        }
        super.onEnable();
    }
    @Override
    public void onDisable() {
        pushPackets();
        index2 = 0;
        if (SaveMotion.isEnabled()) {
            MoveUtil.setmotXYZ(motX,motY,motZ);
        }
        TimerUtil.reset();
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick eventMotion) {
        if (this.OnGround.isEnabled()) {
            mc.player.setOnGround(true);
        }
    }



    @EventTarget
    public void onPreMotion(EventPreMotion eventMotion) {
        MoveUtil.setmotY(0);
        if (!this.OnGround.isEnabled() ) {    	eventMotion.setOnGround(false);    }
        index3++;
        if (this.OnGround.isEnabled()) {
            mc.player.fallDistance=0f;
            eventMotion.setOnGround(true);
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        Packet packetp = event.getPacket();
        if(!event.isCancelled()) {
            if(packetp instanceof ClientboundPlayerPositionPacket) {
                ClientboundPlayerPositionPacket p=(ClientboundPlayerPositionPacket) packetp;
                final double x = p.change().position().x -  mc.player.getX();
                final double y = p.change().position().y -  mc.player.getY();
                final double z = p.change().position().z - mc.player.getZ();
                final double diff = Math.sqrt(x * x + y * y + z * z);
                if(diff<100) {
                    if(flagcount>0) {
                        event.setCancelled(true);
                        flagcount--;
                    }
                }



            }

            if(packetp instanceof ClientboundSetEntityMotionPacket) {
                ClientboundSetEntityMotionPacket p=(ClientboundSetEntityMotionPacket) packetp;
                if(p.getId()==mc.player.getId()) {
                   // this.motX = (double)p.getVelocityX()/8000D;  this.motY = (double)p.getVelocityY()/8000D; this.motZ = (double)p.getVelocityZ()/8000D;

                }
            }


        }

    }


    @EventTarget
    public void onSendPacket(EventSendPacketCancel e) {
        final Packet packetp = e.getPacket();
         if(!e.isCancelled() && e.getPermissionidstate()<5) {
             PacketHelper.Values pc= Client.instance.packet;
            switch(RotMode.getCurrentMode()) {
                case("1.17 Packet"):

                    if (packetp instanceof ServerboundMovePlayerPacket.Rot ) {
                        ServerboundMovePlayerPacket.Rot c05=(ServerboundMovePlayerPacket.Rot) packetp;
                        index1=0;
                        pc.LastYaw=c05.getYRot(pc.LastYaw); pc.LastPitch=c05.getXRot(pc.LastPitch);
                        if(!Client.instance.featureManager.getModuleByClass(NoFall.class).getState() &&
                                CombatUtil.fallDistance<0.000001){
                            ACUtil.send117DuplPacket();
                        }



                        e.setCancelled(true);
                    }

                    break;

                case("No Rotate"):
                    if(packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                        ServerboundMovePlayerPacket.PosRot c06=(ServerboundMovePlayerPacket.PosRot) packetp;
                        if(e.permissionidstate<6) {
                            ServerboundMovePlayerPacket duplc04=new ServerboundMovePlayerPacket.Pos(c06.getX(pc.LastPosX),c06.getY(pc.LastPosY),  c06.getZ(pc.LastPosZ),c06.isOnGround(),mc.player.horizontalCollision); 	 pc.sendPacket(duplc04,10);    e.setCancelled(true);
                        }
                    }
                    if(packetp instanceof ServerboundMovePlayerPacket.Rot) {
                        e.setCancelled(true);
                    }

                    break;


                case("Flag"):
                    if(!e.isCancelled()) {
                        if(packetp instanceof ServerboundMovePlayerPacket && e.getPermissionidstate()<5) {
                            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX+114+Math.random()*10, pc.LastPosY, pc.LastPosZ+114+Math.random()*10,  true,mc.player.horizontalCollision),10,true);
                            pc.LastTpNum++;  pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
                            flagcount++; index1=0;
                            pc.sendPacket(e.getPacket(),10,true);
                        }
                    }

                    break;
            }
        }
        if(NoC03.isEnabled()) {    if (packetp instanceof ServerboundMovePlayerPacket) { 	   ServerboundMovePlayerPacket c03=(ServerboundMovePlayerPacket) packetp;   if(e.permissionidstate<6) { e.setCancelled(true);   }    }       }

    }


    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        if(NoMove.isEnabled()) {
            MoveUtil.stop3();
        }
    }

    public void pushPackets() {
        PacketHelper.Values pc= Client.instance.packet;
        while(LatePacketss.size()>0) {
            if(LatePacketss.getFirst()==null) { break;}
            pc.sendPacket(LatePacketss.pollFirst(),false);

        }
        LatePacketss.clear();
    }

}
