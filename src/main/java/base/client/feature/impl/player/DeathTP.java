package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
public class DeathTP extends Module {
    public ModeSetting mode=new ModeSetting("Mode","Polar",()->true,"Polar");
    public NumberSetting health=new NumberSetting("Health",4,1,20,1,()->true);

    public DeathTP(){
        super("DeathTP", "Телепортирует вас на место смерти", Type.Player);
        this.addSettings(mode,health);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        TimerUtil.setTimerspeed(1);
    }

    Vec3 lastPos=new Vec3(0,0,0);

    @EventTarget
    public void oemp(EventReceivePacketPre e){
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket()instanceof ClientboundSetHealthPacket p){
            if (p.getHealth()<=0f){
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + 10, mc.player.getZ(), false, false));
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + 10, mc.player.getZ(), false, false));
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY() + 10, mc.player.getZ(), false, false));
                mc.player.setPos(mc.player.getX(), mc.player.getY() + 10, mc.player.getZ());
            }
        }
//        if (mc.player.getHealth()<=health.getValue()){
//            switch (mode.getCurrentMode()){
//                case("Polar"):
//                    if (mc.player.isDead()){
//                        mc.player.setPos(lastPos);
//                    }else {
//                        TimerUtil.setTimerspeed(0.75);
//                        mc.player.jumpFromGround();
//                        lastPos=mc.player.getPos();
//                    }
//
//                    break;
//            }
//        }
    }
}
