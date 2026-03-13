package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayDeque;

public class AntiAfk extends Module {

    ModeSetting AfkCheck = new ModeSetting("AfkCheck", "NoMoving", () -> true, "NoMoving","MoveAmount");
    ModeSetting Bypass = new ModeSetting("Bypass", "RotatePacket", () -> true, "Jump", "RotatePacket", "AntiGroundPacket");
    NumberSetting Duration = new NumberSetting("Duration", 60, 5, 1200, 1, () -> true);
    NumberSetting PacketAmount = new NumberSetting("PacketAmount/min", 600, 300, 12000, 20, () -> AfkCheck.getCurrentMode().equals("MoveAmount"));

    BooleanSetting СonsiderGroundPackets = new BooleanSetting("Сonsider Ground Packets", false, () -> AfkCheck.getCurrentMode().equals("MoveAmount"));

    ArrayDeque<LatePacket> movepackets=new ArrayDeque<LatePacket>();
    public TimerHelper afkTimer= new TimerHelper();
    public TimerHelper startTimer= new TimerHelper();


    public AntiAfk() {

        super("AntiAfk", "Чтобы не кикало за афк", Type.Misc);
        this.addSettings(AfkCheck,Bypass,Duration,PacketAmount,СonsiderGroundPackets);
    }

    public void onEnable() {
          afkTimer.reset(); startTimer.reset();
        super.onEnable();
    }
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }
    @EventTarget
    public void onUpdate(EventPreMotion e) {

        switch (AfkCheck.getCurrentMode()){
            case("NoMoving"):
                if(MoveUtil.ismovingmfms() || MoveUtil.getspeed3()>0.0001){
                    afkTimer.reset();
                }
                 break;
            case("MoveAmount"):
                while(movepackets.size()>0) {

                    LatePacket lp=movepackets.getFirst();
                    if(lp.getRequiredMs()<System.currentTimeMillis()){
                        movepackets.removeFirst();
                    }else {
                        break;
                    }

                }
                double koef=Duration.getValue()/60;
                if(movepackets.size()/koef>PacketAmount.getValue()){
                    afkTimer.reset();
                }
  break;

        }


    if(afkTimer.hasTimeElapsed((long)(Duration.getValue()*1000),false)){
        PacketHelper.Values pc = Client.instance.packet;
        switch (Bypass.getCurrentMode()) {
            case ("Jump"):
                if (pc.LastGround && mc.player.verticalCollision) {
e.setYaw(pc.LastYaw+180); ACUtil.send117DuplPacket((float) (e.getYaw()), (float) (pc.LastPitch*0.01));
                }
                break;
        }
        startantiafk();
        afkTimer.reset();


    }

    }

    @EventTarget
    public void onpost(EventSendPacketPost e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        switch (AfkCheck.getCurrentMode()) {
            case ("MoveAmount"):
                if (packetp instanceof ServerboundMovePlayerPacket) {
                    if (СonsiderGroundPackets.isEnabled() || !(packetp instanceof ServerboundMovePlayerPacket.StatusOnly)) {
                        movepackets.add(new LatePacket(packetp, (long) (System.currentTimeMillis() + Duration.getValue() * 1000)));
                    }


                }
                break;
        }
    }

    @EventTarget
    public void onReceivePacketPre(EventReceivePacketPre e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket() instanceof ClientboundLoginPacket) {
            afkTimer.reset(); startTimer.reset();
            movepackets.clear();
        }
    }

    void startantiafk(){
        PacketHelper.Values pc = Client.instance.packet;
        switch (Bypass.getCurrentMode()) {
            case ("Jump"):
if(pc.LastGround && mc.player.verticalCollision){
    mc.player.jumpFromGround(); MoveUtil.strafe(0.1);
///    mc.player.setYRot(mc.player.getYRot()-180);

}else{
    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10);
}
                 break;
            case ("RotatePacket"):
                ACUtil.send117DuplPacket((float) (pc.LastYaw+Math.random()*0.01), (float) (pc.LastPitch*0.01));
                break;
            case ("AntiGroundPacket"):
                pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(!pc.LastGround,mc.player.horizontalCollision),10);
                break;

        }

    }




}

