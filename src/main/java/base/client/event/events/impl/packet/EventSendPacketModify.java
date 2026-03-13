package base.client.event.events.impl.packet;
import base.client.event.events.Event;
import net.minecraft.network.protocol.Packet;

public class EventSendPacketModify implements Event {
    public int getPermissionidstate() {
        return permissionidstate;
    }

    public void setPermissionidstate(int permissionidstate) {
        this.permissionidstate = permissionidstate;
    }

    public int permissionidstate=0;
    private Packet<?> packet;

    public EventSendPacketModify(Packet<?> packet,int permissionidstate) {
        this.packet = packet; this.permissionidstate=permissionidstate;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
