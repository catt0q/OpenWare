package base.client.feature.impl.combat.velocities;

import base.client.event.events.impl.packet.EventReceivePacketPre;

public class VelocityNoKB {
    public static EventReceivePacketPre proccesPacket(EventReceivePacketPre e){

        e.cancel();
        return e;
    }


}
