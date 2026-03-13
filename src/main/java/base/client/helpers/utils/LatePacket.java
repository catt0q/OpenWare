package base.client.helpers.utils;



import net.minecraft.network.protocol.Packet;
public class LatePacket {

    public long RequiredMs=0;
    public Packet<?> packet=null;

    public long getRequiredMs() {
        return RequiredMs;
    }

    public void setRequiredMs(long requiredMs) {
        RequiredMs = requiredMs;
    }

    public Packet<?> getPacket() {
        return packet;
    }
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public LatePacket(Packet<?> packet2, long requiredMs2) {
        packet=packet2;
        RequiredMs=requiredMs2;
    }


}
