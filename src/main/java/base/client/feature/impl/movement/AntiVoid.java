package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.render.EventCameraPosUpdate;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;

public class AntiVoid extends Module {

    int index1 = 0;
    int index2 = 0;
    int index4 = 0;
    int index8 = 0;
    int index5=0;
    int groundstate=0;
    Vec3 startpos;
    Vec3 startposcamera;
    double lastMotX = 0;
    double lastMotY = 0;
    double lastMotZ = 0;

    float prevYaw,prevPitch;

    ArrayDeque<Packet> LatePacketss = new ArrayDeque<Packet>();
    public static TimerHelper timerfromlastflaglong = new TimerHelper();
    int OldFlagskip = 0;

boolean blinkpackets=false;

    public ModeSetting Mode;
    public NumberSetting MaxFallDist;




    public AntiVoid() {
        super("AntiVoid", "Позволяет избегать падения в бездну", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix1", () -> true, "Matrix1", "Matrix2");
        MaxFallDist = new NumberSetting("Max Fall Dist", 5F, 1F, 10F, 0.1F, () -> true);





        addSettings(Mode,MaxFallDist);
    }





    @EventTarget
    public void onEMI(EventMoveInput e) {
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):

                break;
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
    public void onReceivePacket(EventReceivePacketPre event) {
        PacketHelper.Values pc = Client.instance.packet;

    }


    @EventTarget
    public void onPreUpdate(EventPreMotion event) {
        String longMode = Mode.getOptions();
        this.setSuffix(longMode);




    }


    @EventTarget
    public void onPrePacket(EventSendPacketCancel e) {
        Packet<?> packetp = e.getPacket();
        PacketHelper.Values pc = Client.instance.packet;





    }

    @EventTarget
    public void onPrePacket(EventSendPacketBlink e) {
        Packet<?> packetp = e.getPacket();
        PacketHelper.Values pc = Client.instance.packet;

    }


    @EventTarget
    public void onMove(EventOnMovePost event) {
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(Minecraft.getInstance().player.getYRot());
        if (MoveUtil.getdir() != -1) {
            yawt = Math.toRadians(MoveUtil.getdir());
        }
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        double mult=0.06249;

switch (Mode.getCurrentMode()){
    case ("Matrix1"):
        if(CombatUtil.fallDistance>MaxFallDist.getValue() && ACUtil.matrixisvoidcheck() && mc.player.getY()>0) {
            CombatUtil.resetfd();
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);

           pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY-30000+1000*Math.random(), pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false,mc.player.horizontalCollision ),10);

           pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY-30000+1000*Math.random(), pc.LastPosZ, false,mc.player.horizontalCollision ),10);
            pc.sendPacket(new ServerboundMovePlayerPacket.Pos(pc.LastPosX, pc.LastPosY, pc.LastPosZ, false,mc.player.horizontalCollision ),10);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);

            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY-30000+1000*Math.random(), pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);




        /*    pc.LastTpNum++;
            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum), 10, true);
            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastGPosX, pc.LastGPosY, pc.LastGPosZ,
                    pc.LastTpNum, false, false));*/




            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);








        }
     break;
    case ("Matrix2"):

        if(CombatUtil.fallDistance>MaxFallDist.getValue() && ACUtil.matrixisvoidcheck() && mc.player.getY()>0){
            CombatUtil.resetfd();
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);

            pc.sendPacket(ACUtil.matrixflagpacket(),10,true);
            pc.sendPacket(ServerboundMoveVehiclePacket.fromEntity(mc.player), 10, true);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY-30000, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY+30000, pc.LastPosZ,0,0,false,mc.player.horizontalCollision ),10);

            Client.instance.flagsch.getFlags().addFirst(new FlagHelper.SkippedFlag(pc.LastPosX, pc.LastPosY, pc.LastPosZ,pc.LastTpNum,false,false));


            pc.LastTpNum++;
            pc.sendPacket(new ServerboundAcceptTeleportationPacket(pc.LastTpNum),10,true);
            MoveUtil.setmotY(0);
        }

  break;



}





    }
    @EventTarget
    public void camerada(EventCameraPosUpdate e) {
        PacketHelper.Values pc = Client.instance.packet;


    }


    public void pushPackets() {
        PacketHelper.Values pc = Client.instance.packet;

        int j=0,sizee=LatePacketss.size()>45 ? 15 : 10;



        for (Packet pp : LatePacketss) {
            if(j>=sizee){
                break;
            }
            pc.sendPacket(pp, 10,true);
            j++;
   }
        for (int i=0;i<sizee;i++) {
            if(LatePacketss.size()<=0){
                break;
            }
LatePacketss.removeFirst();
        }




    }

    @Override
    public void onEnable() {
        blinkpackets=false;
        groundstate=0;
        LatePacketss.clear();
        index4 = 0;
        index8 = 0;
        index1 = 0;
        index2 = 0;
        index5=0;
        OldFlagskip = 0;
        if(mc.player!=null) {startpos=mc.player.position();

            lastMotX = mc.player.getDeltaMovement().x;
            lastMotY = mc.player.getDeltaMovement().y;
            lastMotZ = mc.player.getDeltaMovement().z;
        }
        if(mc.gameRenderer.getMainCamera()!=null){
            startposcamera=mc.gameRenderer.getMainCamera().position();
        }

        TimerUtil.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();



        pushPackets();
        super.onDisable();
    }


}
