package base.client.helpers;

import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;

import java.util.Random;

public interface Helper {

    // Packet helper
    PacketHelper.Values pc = Client.instance.packet;

    // Minecraft instance
    Minecraft mc = Minecraft.getInstance();

    // Random utility
    Random random = new Random();

    // Safe access methods (optional but cleaner)

    static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    static Random getRandom() {
        return random;
    }

    static PacketHelper.Values getPacketHelper() {
        return Client.instance.packet;
    }

}