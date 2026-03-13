package base.client.event.events.impl.packet;

import base.client.event.events.Event;
import net.minecraft.network.protocol.Packet;


public class EventSendPacketPost implements Event {



    private Packet<?> packet;

    public EventSendPacketPost(Packet<?> packet) {
        this.packet = packet;
    }
    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

}