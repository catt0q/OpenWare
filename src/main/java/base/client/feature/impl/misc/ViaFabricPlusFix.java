package base.client.feature.impl.misc;

import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class ViaFabricPlusFix extends Module {

    public ViaFabricPlusFix() {
        super("ViaFabricPlusFix", "Fix that nigga", Type.Misc);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }



}
