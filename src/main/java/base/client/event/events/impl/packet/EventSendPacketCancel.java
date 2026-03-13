package base.client.event.events.impl.packet;


import base.client.event.events.Cancellable;
import net.minecraft.network.protocol.Packet;

public class EventSendPacketCancel extends Cancellable {

    public int getPermissionidstate() {
        return permissionidstate;
    }

    public void setPermissionidstate(int permissionidstate) {
        this.permissionidstate = permissionidstate;
    }

    public int permissionidstate=0;

    public Packet<?> packet;

    public EventSendPacketCancel(Packet<?> packet,int permissionidstate) {
        this.packet = packet; this.permissionidstate=permissionidstate;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

}