package base.client.event.events.impl.packet;


import base.client.event.events.callables.EventCancellable;
import net.minecraft.network.protocol.Packet;

public class EventReceivePacketPost extends EventCancellable {

    private Packet<?> packet;

    public EventReceivePacketPost(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
