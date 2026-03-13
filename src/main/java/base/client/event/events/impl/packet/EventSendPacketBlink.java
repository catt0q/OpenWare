package base.client.event.events.impl.packet;


import base.client.event.events.callables.EventCancellable;
import net.minecraft.network.protocol.Packet;

public class EventSendPacketBlink extends EventCancellable {

    public int getPermissionidstate() {
        return permissionidstate;
    }

    public void setPermissionidstate(int permissionidstate) {
        this.permissionidstate = permissionidstate;
    }

    public int permissionidstate=0;
    private final Packet<?> packet;

    public EventSendPacketBlink(Packet<?> packet,int permissionidstate) {
        this.packet = packet; this.permissionidstate=permissionidstate;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }
}
