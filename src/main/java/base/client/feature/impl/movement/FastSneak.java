package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.player.ClientInput;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

public class FastSneak extends Module {
    public static NumberSetting mult;
    public static ModeSetting Bypass;


    public FastSneak() {
        super("FastSneak", "Позволяет быстрее двигаться вприсяде", Type.Movement);
        mult = new NumberSetting("Mult", 0.5F, 0, 1, 0.01F, () -> true);
         Bypass = new ModeSetting("Bypass", "None", () -> true, "None", "Packet");

        this.addSettings(mult,Bypass);
    }

    boolean wassprint=false;
    boolean wassneak=false;
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if(Bypass.getCurrentMode().equals("Packet") ) {
            PacketHelper.Values pc= Client.instance.packet;
            if(mc.player.isCrouching()) {

                Input ff=mc.player.input.keyPresses;
                
                pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),true,ff.sprint())));
            }
        }
        super.onDisable();
    }




    @EventTarget
    public void onSendPacketre(EventSendPacketCancel e) {
        PacketHelper.Values pc=Client.instance.packet;
        Packet<?> packetp=e.getPacket();
        boolean issneak=mc.player.isCrouching();
        if(!e.isCancelled()) {
            if (packetp instanceof ServerboundMovePlayerPacket) {
                if(Bypass.getCurrentMode().equals("Packet")  ) {
                    if(issneak) {
                        Input ff = mc.player.input.keyPresses;
                        pc.sendPacket(new ServerboundPlayerInputPacket(new Input(ff.forward(), ff.backward(), ff.left(), ff.right(), ff.jump(), false, ff.sprint())));
                        wassneak = false;
                    }else if(wassneak){
                        Input ff = mc.player.input.keyPresses;
                        pc.sendPacket(new ServerboundPlayerInputPacket(new Input(ff.forward(), ff.backward(), ff.left(), ff.right(), ff.jump(), false, ff.sprint())));

                        wassneak = false;
                    }

                }

            }
        }


    }


    @EventTarget
    public void onSendPacketost(EventSendPacketPost e) {
        PacketHelper.Values pc=Client.instance.packet;
        Packet<?> packetp=e.getPacket();
        boolean issneak=mc.player.isCrouching();
            if (packetp instanceof ServerboundMovePlayerPacket) {
                if(Bypass.getCurrentMode().equals("Packet") && issneak ) {   Input ff=mc.player.input.keyPresses;
                    pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),true,ff.sprint())));
                    wassneak=true;
                }

            }


    }

    @EventTarget
    public void onMInput(EventMoveInput event) {
        if(!this.state) return;



    }

}
