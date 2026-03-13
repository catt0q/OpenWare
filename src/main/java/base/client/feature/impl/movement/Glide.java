package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.TeleportBack;
import base.client.feature.impl.movement.glides.MatrixDamageGlide;
import base.client.feature.impl.movement.speeds.grim.GrimExploitSpeed;
import base.client.feature.impl.movement.speeds.grim.GrimGroundSpeed;
import base.client.feature.impl.movement.speeds.grim.GrimLowHopSpeed;
import base.client.feature.impl.movement.speeds.matrix.*;
import base.client.feature.impl.movement.speeds.other.OtherGroundSpeed;
import base.client.feature.impl.movement.speeds.other.OtherSimpleHopSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarBasicSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarGroundStrafeSpeed;
import base.client.feature.impl.movement.speeds.polar.PolarLowHopSpeed;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.LatePacket;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerHelper;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;

public class Glide extends Module {
    public TimerHelper flyTimer = new TimerHelper();

    public static ArrayDeque<Packet> BlinkPackets = new ArrayDeque<>();
    ArrayDeque<LatePacket> SavedFlagPackets = new ArrayDeque<LatePacket>();
    boolean shouddelay = false;
    ArrayDeque<Packet> ServerPackets = new ArrayDeque<Packet>();

    public static ModeSetting Mode,MMode;




    public static int groundstate=0;//0=nothing 1=noground 2=ground
   public static int index1 = 0,index2 = 0,index3 = 0,index4 = 0,index5 = 0,index6 = 0,index7 = 0;

    public static boolean jumping;

    double indexd6 = 0;
    double indexd7 = 0;

    int flydelay = 0;
    int flytype = 0;

    double lasttpposX = 0;
    double lasttpposY = 0;
    double lasttpposZ = 0;


    int boostTick, flagskip, stage;

    double startposY = 0;
    public static BooleanSetting Ver188MM;

    public Glide() {
        super("Glide", "Медленное замедление при падении", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix");
          MMode = new ModeSetting("Matrix Mode", "Damage", () -> Mode.getCurrentMode().equals("Matrix"), "Damage" );
        Ver188MM = new BooleanSetting("1.8.8", false, () -> MMode.isVisible() && MMode.getCurrentMode().equals("Damage"));


        addSettings(Mode,
                MMode,Ver188MM
        );
    }

    public void onEnable() {
        jumping=true;
        boostTick = 0;groundstate=0;
        flagskip = 0;
        stage = 0;
        shouddelay = false;
        ServerPackets.clear();
        SavedFlagPackets.clear(); BlinkPackets.clear();
        flyTimer.reset();
        Block prevblock = null;
        index1 = 0;
        index2 = 0;
        index3 = 0;
        index4 = 0;
        index5 = 0;
        index6 = 0;
        index7 = 0;
        indexd6 = 0;
        indexd7 = 0;
        flydelay = 0;
        flytype = 0;
        lasttpposX = 0;
        lasttpposY = 0;
        lasttpposZ = 0;
        startposY = mc.player.getY();
        switch (Mode.getCurrentMode()) {


            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("BasicHop"):
                        break;
                  
                }
                break;



        }




        super.onEnable();
    }

    public void onDisable() {
        if(mc.player!=null)
            TimerUtil.setTimerspeed(1);
        PacketHelper.Values pc = Client.instance.packet;
        while(BlinkPackets.size()>0) {
            pc.sendPacket(BlinkPackets.pollFirst(),10,true);
        } BlinkPackets.clear();


        super.onDisable();
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
    public void onPostUpdate(EventPostMotion e) {
        if(MoveUtil.isFreezing) return;

    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        double yawt = Math.toRadians(mc.player.getYRot());    yawt = Math.toRadians(MoveUtil.getdir());    double xt = -Math.sin(yawt);   double zt = Math.cos(yawt);  if (MoveUtil.getdir() == -1) {   xt = 0;   zt = 0;   }
    PacketHelper.Values pc = Client.instance.packet;
        if(MoveUtil.isFreezing) return;
        switch (Mode.getCurrentMode()) {



            case ("Matrix"):
                if(Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()){
                    return;
                }
                switch (MMode.getCurrentMode()) {
                    case ("Damage"):
                   e= MatrixDamageGlide.onEventPreMotion(e);
break;

                }
                break;
        }


    }





    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc = Client.instance.packet;
        if(MoveUtil.isFreezing) return;
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }




        switch (Mode.getCurrentMode()) {








            case ("Matrix"):
                if(Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState()){
                    return;
                }


                switch (MMode.getCurrentMode()) {
                    case ("Damage"):
                        MatrixDamageGlide.onEventOnMovePost(xt,zt);
                        break;







                }

        }


    }

    @EventTarget
    public void oncancel(EventSendPacketBlink e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();

    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {

    }


    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPre e) {

        switch (Mode.getCurrentMode()) {


            case ("Matrix"):
                switch (MMode.getCurrentMode()) {
                    case ("OldFastHop"):
                        break;
                }
                break;


        }
    }
    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        switch (Mode.getCurrentMode()) {
            case ("Matrix"):
                switch (MMode.getCurrentMode()) {

                    case ("TravelHop"):
                break;
                }
                break;
        }

    }



    public static void sjump(){
  jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }




}
