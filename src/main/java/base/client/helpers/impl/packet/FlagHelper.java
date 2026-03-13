package base.client.helpers.impl.packet;




import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.Helper;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;

import java.util.ArrayDeque;

public class FlagHelper {
    public static class Flags implements Helper {


        ArrayDeque<SkippedFlag> flags= new ArrayDeque<>();


        public ArrayDeque<SkippedFlag> getFlags() {
            return flags;
        }
        @EventTarget
        public void onReceivePacket(EventReceivePacketPre event) {
            if (event.getPacket() instanceof ClientboundLoginPacket) {
                ClientboundLoginPacket sjp=(ClientboundLoginPacket) event.getPacket();
                flags.clear();
            }
        }

    }





    public static class SkippedFlag implements Helper {
        double PosX=0;
        double PosY=0;
        double PosZ=0;
        int tpid=0;
        boolean breakifbadpos=true;
        boolean breakifbadteleport=true;

        public SkippedFlag(double posx,double posy,double posz,int tpid,boolean bibp,boolean bibt) {
            this.PosX=posx;  	this.PosY=posy; 	this.PosZ=posz;
            this.tpid=tpid;    	this.breakifbadpos=bibp; 	this.breakifbadteleport=bibt;
        }

        public SkippedFlag(double posx,double posy,double posz,int tpid) {
            this.PosX=posx;  	this.PosY=posy; 	this.PosZ=posz; 	this.tpid=tpid;
        }



        public boolean isBreakifbadpos() {
            return breakifbadpos;
        }

        public boolean isBreakifbadteleport() {
            return breakifbadteleport;
        }

        public double getPosX() {
            return PosX;
        }


        public double getPosY() {
            return PosY;
        }

        public double getPosZ() {
            return PosZ;
        }


        public int getTpid() {
            return tpid;
        }




    }

}
