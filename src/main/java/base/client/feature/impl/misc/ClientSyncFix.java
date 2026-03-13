package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

import static base.client.helpers.Helper.mc;

public class ClientSyncFix extends Module {

    private int lastSlot = -1;

    public ClientSyncFix() {
        super("SlotSyncFix", "Prevents client-side slot desync", Type.Misc);
    }

    @EventTarget
    public void onSendPacket(EventSendPacketPost e) {
        Packet<?> p = e.getPacket();
        if (p instanceof ServerboundSetCarriedItemPacket packet) {
            lastSlot = packet.getSlot();
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.player == null || lastSlot == -1) return;

        int current = mc.player.getInventory().getSelectedSlot();

        if (current != lastSlot) {
            PacketHelper.Values pc = Client.instance.packet;
            pc.sendPacket(
                    new ServerboundSetCarriedItemPacket(current),
                    1,
                    true
            );
            lastSlot = current;
        }
    }

    @Override
    public void onDisable() {
        lastSlot = -1;
        super.onDisable();
    }
}
