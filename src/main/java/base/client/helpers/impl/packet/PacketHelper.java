package base.client.helpers.impl.packet;


import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketBlink;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.exploit.Disabler;
import base.client.feature.impl.exploit.PacketFixer;
import base.client.feature.impl.movement.FastSneak;
import base.client.helpers.Helper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import base.client.helpers.utils.TimerHelper;
import base.mixin.client.accessors.ServerboundInteractPacketAccesor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;


public class PacketHelper {



    public static class Values implements Helper {

        public int ticknotusingitem=0;
        public int permissionidstate=0;
        public boolean noevent=false;
        public static boolean chatnoevent=false;

//boolean packetstates
        public static ServerboundPlayerCommandPacket.Action lastaction=null;


       static Minecraft mc= Minecraft.getInstance();

        public TimerHelper lastTptimer=new TimerHelper();
        public TimerHelper lastVeltimer=new TimerHelper();
        public TimerHelper lastC02timer=new TimerHelper();
        public TimerHelper lastPlacementtimer=new TimerHelper();

        public int airpackets=0;

        public int attackticks=0;

        public int LastTpNum=0;

        public double LastTpPosX=0;
        public double LastTpPosY=0;
        public double LastTpPosZ=0;

        public double LastPosX=0;
        public double LastPosY=0;
        public double LastPosZ=0;
        public double LastMotX=0;
        public double LastMotY=0;
        public double LastMotZ=0;
        public float LastYaw=0;
        public float LastPitch=0;
        public boolean LastGround=false;


        public double LastGPosX=0;
        public double LastGPosY=0;
        public double LastGPosZ=0;
        public double LastGMotX=0;
        public double LastGMotY=0;
        public double LastGMotZ=0;
        public float LastGYaw=0;
        public float LastGPitch=0;

        public double LastNoGPosX=0;
        public double LastNoGPosY=0;
        public double LastNoGPosZ=0;
        public double LastNoGMotX=0;
        public double LastNoGMotY=0;
        public double LastNoGMotZ=0;
        public float LastNoGYaw=0;
        public float LastNoGPitch=0;


        public int prevslotid=0;
        public Entity lastattackentity=null;

        public AABB lastaabb=null;

        @EventTarget
        private void onReceivePacket(EventReceivePacketPre event) {
            Packet packetp=event.getPacket();
            if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                ClientboundPlayerPositionPacket s08=(ClientboundPlayerPositionPacket) event.getPacket();
                LastTpPosX=s08.change().position().x;
                LastTpPosY=s08.change().position().y;
                LastTpPosZ=s08.change().position().z;
                LastTpNum=s08.id();
                lastTptimer.reset();
            }
            if (event.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                ClientboundSetEntityMotionPacket s12=(ClientboundSetEntityMotionPacket) event.getPacket();
                if(s12.getId()==mc.player.getId() && !event.isCanceled()) {
                    lastVeltimer.reset();
                }
            }
            if (event.getPacket() instanceof ClientboundLoginPacket) {
                Client.instance.flagsch.flags.clear(); ticknotusingitem=999;
            }


        }





        @EventTarget
         private void onSendPacketPost(EventSendPacketBlink event) {
            if(!event.isCancelled()) {
                Packet<?> packetp=event.getPacket();
if(event.getPermissionidstate()>9){
    return;
}

                if (packetp instanceof ServerboundUseItemOnPacket) {
                    ServerboundUseItemOnPacket c08 = (ServerboundUseItemOnPacket) packetp;
                    lastPlacementtimer.reset();
                }
                if (packetp instanceof ServerboundSetCarriedItemPacket) {
                    ServerboundSetCarriedItemPacket c09 = (ServerboundSetCarriedItemPacket) packetp;
                    prevslotid = c09.getSlot();
                }
                if (packetp instanceof ServerboundInteractPacket) {
                    ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;
                    ServerboundInteractPacketAccesor ac2 = (ServerboundInteractPacketAccesor) c02;
                    ServerboundInteractPacket.Action action = ac2.getAction();
                    if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)) {
                        lastC02timer.reset();attackticks=0;
                        lastattackentity=mc.level.getEntity(ac2.getEntityId());
                    }
                }



                if (packetp instanceof ServerboundMovePlayerPacket) {
                    ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp;



                    if (packetp instanceof ServerboundMovePlayerPacket.Pos) {
                        ServerboundMovePlayerPacket.Pos c04=(ServerboundMovePlayerPacket.Pos) packetp;





                        if(c04.isOnGround()) {
                            LastGPosX=c04.getX(0);
                            LastGPosY=c04.getY(0);
                            LastGPosZ=c04.getZ(0);
                            LastGMotX=mc.player.getDeltaMovement().x();
                            LastGMotY=mc.player.getDeltaMovement().y();
                            LastGMotZ=mc.player.getDeltaMovement().z();
                        }else {
                            LastNoGPosX=c04.getX(0);
                            LastNoGPosY=c04.getY(0);
                            LastNoGPosZ=c04.getZ(0);
                            LastNoGMotX=mc.player.getDeltaMovement().x();
                            LastNoGMotY=mc.player.getDeltaMovement().y();
                            LastNoGMotZ=mc.player.getDeltaMovement().z();
                        }
                        lastaabb=mc.player.getBoundingBox();

                        LastPosX=c04.getX(0);
                        LastPosY=c04.getY(0);
                        LastPosZ=c04.getZ(0);

                        LastMotX=mc.player.getDeltaMovement().x();
                        LastMotY=mc.player.getDeltaMovement().y();
                        LastMotZ=mc.player.getDeltaMovement().z();
                        LastGround=c04.isOnGround();
                    }else if(packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                        ServerboundMovePlayerPacket.PosRot c06=(ServerboundMovePlayerPacket.PosRot) packetp;

                        if(c06.isOnGround()) {
                            LastGPosX=c06.getX(0);
                            LastGPosY=c06.getY(0);
                            LastGPosZ=c06.getZ(0);
                            LastGMotX=mc.player.getDeltaMovement().x();
                            LastGMotY=mc.player.getDeltaMovement().y();
                            LastGMotZ=mc.player.getDeltaMovement().z();
                            LastGYaw=c06.getYRot(0);
                            LastGPitch=c06.getXRot(0);
                        }else {
                            LastNoGPosX=c06.getX(0);
                            LastNoGPosY=c06.getY(0);
                            LastNoGPosZ=c06.getZ(0);
                            LastNoGMotX=mc.player.getDeltaMovement().x();
                            LastNoGMotY=mc.player.getDeltaMovement().y();
                            LastNoGMotZ=mc.player.getDeltaMovement().z();
                            LastNoGYaw=c06.getYRot(0);
                            LastNoGPitch=c06.getXRot(0);
                        }
                        lastaabb=mc.player.getBoundingBox();

                        LastPosX=c06.getX(0);
                        LastPosY=c06.getY(0);
                        LastPosZ=c06.getZ(0);
                        LastMotX=mc.player.getDeltaMovement().x();
                        LastMotY=mc.player.getDeltaMovement().y();
                        LastMotZ=mc.player.getDeltaMovement().z();

                        LastYaw=c06.getYRot(0);
                        LastPitch=c06.getXRot(0);
                        LastGround=c06.isOnGround();


                    }else if(packetp instanceof ServerboundMovePlayerPacket.Rot) {
                        ServerboundMovePlayerPacket.Rot c05=(ServerboundMovePlayerPacket.Rot) packetp;

                        if(c05.isOnGround()) {
                            LastGYaw=c05.getYRot(0);
                            LastGPitch=c05.getXRot(0);
                        }else {
                            LastNoGYaw=c05.getYRot(0);
                            LastNoGPitch=c05.getXRot(0);
                        }


                        LastYaw=c05.getYRot(0);
                        LastPitch=c05.getXRot(0);
                        LastGround=c05.isOnGround();
                    }else {
                        LastGround=c03.isOnGround();
                    }











if(LastGround){
    airpackets=0;
}else{
    airpackets++;
}



                }










            }
        }

        @EventTarget
        private void onTick(EventTick event) {

        }
        @EventTarget
        private void onUpdate(EventPreMotion event) {
            attackticks++; ticknotusingitem++;
            if(mc.player.isUsingItem()){
                ticknotusingitem=0;
            }
        }


        @EventTarget
        private void onSendPacket(EventSendPacketCancel event) {







           if(!event.isCancelled() && event.getPermissionidstate()<3) {
                Packet<?> packetp=event.getPacket();




                if (packetp instanceof ServerboundMovePlayerPacket) {
                    ServerboundMovePlayerPacket c03 = (ServerboundMovePlayerPacket) packetp; double deviation=1e-6;
                    boolean needcancelump=(Client.instance.featureManager.getModuleByClass(Disabler.class).getState() && Disabler.MatrixTA188.isVisible() && Disabler.MatrixTA188.isEnabled())
                            || (Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState() && PacketFixer.UselessMovePacket.isEnabled() )  ;
if((Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState() && PacketFixer.C06OnItemUseFix.isEnabled() )){

    if(packetp instanceof ServerboundMovePlayerPacket.PosRot) {
        boolean yandpspoof=false;   boolean xyzspoof=false;
        ServerboundMovePlayerPacket.PosRot c06=(ServerboundMovePlayerPacket.PosRot) packetp;

        if((Math.abs(c06.getX(0)-this.LastPosX)<deviation && Math.abs(c06.getY(0)-this.LastPosY)<deviation && Math.abs(c06.getZ(0)-this.LastPosZ)<deviation)) {
            xyzspoof=true;

        }
         if((RotationUtils.pitchdiff(mc.player.getXRot(), c06.getXRot(0))<deviation && RotationUtils.yawdiff(mc.player.getYRot(), c06.getYRot(0))<deviation)) {
            yandpspoof=true;
        }




          if(xyzspoof && yandpspoof && c06.isOnGround()==this.LastGround) {
            event.setCancelled(true);
          }
    }

}



                    if(needcancelump && !event.isCanceled()) {
                        boolean nomove=(Client.instance.featureManager.getModuleByClass(Disabler.class).getState() && Disabler.MatrixTA188.isVisible() && Disabler.MatrixTA188.isEnabled())
                                || (Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState() && PacketFixer.NoMove.isEnabled() )  ;


                            if(packetp instanceof ServerboundMovePlayerPacket.StatusOnly) {
                                ServerboundMovePlayerPacket c03st=(ServerboundMovePlayerPacket) packetp;
                                if(c03st.isOnGround()==this.LastGround) {
                                    event.setCancelled(true);
                                }
                            }
                            else if(packetp instanceof ServerboundMovePlayerPacket.Pos ) {
                                ServerboundMovePlayerPacket.Pos c04=(ServerboundMovePlayerPacket.Pos) packetp;

                              //  ChatHelper.addChatMessage("Hic04 "+nomove + ""+(Math.abs(c04.getX(0)-this.LastPosX)<deviation)+" " +(Math.abs(c04.getY(0)-this.LastPosY)<deviation)+ " "+(Math.abs(c04.getZ(0)-this.LastPosZ)<deviation));
                                if((Math.abs(c04.getX(0)-this.LastPosX)<deviation && Math.abs(c04.getY(0)-this.LastPosY)<deviation && Math.abs(c04.getZ(0)-this.LastPosZ)<deviation  && (nomove || MoveUtil.getspeed3()>deviation))
                                ) {   event.setCancelled(true);     if(c04.isOnGround()!=this.LastGround) {  this.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(c04.isOnGround(),c04.horizontalCollision()));
                                }
                                }
                            }else 	  if(packetp instanceof ServerboundMovePlayerPacket.Rot) {
                                ServerboundMovePlayerPacket.Rot c05=(ServerboundMovePlayerPacket.Rot) packetp;
                                if(RotationUtils.pitchdiff(this.LastPitch, c05.getXRot(0))<deviation && RotationUtils.yawdiff(this.LastYaw, c05.getYRot(0))<deviation ) {
                                    event.setCancelled(true);
                                    if(c05.isOnGround()!=this.LastGround) { this.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(c05.isOnGround(),c05.horizontalCollision()));  }
                                }
                            }else 	  if(packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                                boolean yandpspoof=false;
                                boolean xyzspoof=false;
                                ServerboundMovePlayerPacket.PosRot c06=(ServerboundMovePlayerPacket.PosRot) packetp;
                                if((RotationUtils.pitchdiff(this.LastPitch, c06.getXRot(0))<deviation && RotationUtils.yawdiff(this.LastYaw, c06.getYRot(0))<deviation)) {
                                    yandpspoof=true;
                                }
                                if((Math.abs(c06.getX(0)-this.LastPosX)<deviation && Math.abs(c06.getY(0)-this.LastPosY)<deviation && Math.abs(c06.getZ(0)-this.LastPosZ)<deviation) && (nomove || MoveUtil.getspeed3()>deviation)) {
                                    xyzspoof=true;

                                }
                                 if(!yandpspoof && xyzspoof) {
                                    event.setCancelled(true);	  this.sendPacket(new ServerboundMovePlayerPacket.Rot(c06.getYRot(0),c06.getXRot(0),c06.isOnGround(),c06.horizontalCollision()));
                                }else if(yandpspoof && !xyzspoof) {
                                    event.setCancelled(true);  		 this.sendPacket(new ServerboundMovePlayerPacket.Pos(c06.getX(0),c06.getY(0),  c06.getZ(0),c06.isOnGround(),c06.horizontalCollision()));
                                }else if(yandpspoof && xyzspoof) {
                                    event.setCancelled(true);
                                    if(c06.isOnGround()!=this.LastGround) {
                                        this.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(c06.isOnGround(),c06.horizontalCollision()));
                                    }
                                }
                            }





                    }


                    if(!event.isCancelled() && event.getPermissionidstate()<3 && PacketFixer.InvalidMovePacketFix.isEnabled() && Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState()) {

                        if (packetp instanceof ServerboundMovePlayerPacket.Pos) {
                            ServerboundMovePlayerPacket.Pos c04=(ServerboundMovePlayerPacket.Pos) packetp;
                                c04=new ServerboundMovePlayerPacket.Pos(isInvalid(c04.getX(0)) ? this.LastPosX : c04.getX(0),
                                        isInvalid(c04.getY(0)) ? this.LastPosY : c04.getY(0),
                                        isInvalid(c04.getZ(0)) ? this.LastPosZ : c04.getZ(0),
                                        c04.isOnGround(),
                                        c04.horizontalCollision()

                                );

                        }else if(packetp instanceof ServerboundMovePlayerPacket.PosRot) {
                            ServerboundMovePlayerPacket.PosRot c06=(ServerboundMovePlayerPacket.PosRot) packetp;
                                 c06=new ServerboundMovePlayerPacket.PosRot(isInvalid(c06.getX(0)) ? this.LastPosX : c06.getX(0),
                                        isInvalid(c06.getY(0)) ? this.LastPosY : c06.getY(0),
                                        isInvalid(c06.getZ(0)) ? this.LastPosZ : c06.getZ(0),
                                        isInvalid(c06.getYRot(0)) ? this.LastYaw : c06.getYRot(0),
                                        isInvalid(c06.getXRot(0)) ? this.LastPitch : c06.getXRot(0),
                                        c06.isOnGround(),
                                        c06.horizontalCollision()

                                );


                        }else if(packetp instanceof ServerboundMovePlayerPacket.Rot) {
                            ServerboundMovePlayerPacket.Rot c05=(ServerboundMovePlayerPacket.Rot) packetp;

                                c05=new ServerboundMovePlayerPacket.Rot(
                                        isInvalid(c05.getYRot(0)) ? this.LastYaw : c05.getYRot(0),
                                        isInvalid(c05.getXRot(0)) ? this.LastPitch : c05.getXRot(0),
                                        c05.isOnGround(),
                                        c05.horizontalCollision()
                                );


                        }




                    }
                }
            }




           if(Client.instance.featureManager.getModuleByClass(PacketFixer.class).getState()){
               event=PacketFixer.proccesevent(event);


           }
            if(event.isCancelled()) {   return;    }
            if(event.getPacket() instanceof ServerboundPlayerCommandPacket){
                ServerboundPlayerCommandPacket c0b = (ServerboundPlayerCommandPacket) event.getPacket();
                pc.lastaction= c0b.getAction();


            }


        }


        public static void sendCloseInventoryPacket(){
            Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(Minecraft.getInstance().player.containerMenu.containerId));
        }

        public static void sendSilentChat(String message){
            chatnoevent=true;
            Minecraft.getInstance().getConnection().sendChat(message);
            chatnoevent=false;
        }

        public static void sendPacket(Packet<?> packet){    sendPacket(packet,0,false);   }
        public static void sendPacket(Packet<?> packet,int permissionidstate){     sendPacket(packet,permissionidstate,false);   }
        public static void sendPacket(Packet<?> packet,boolean noevent){     sendPacket(packet,noevent);    }
        public static void sendPacket(Packet<?> packet,int permissionidstate,boolean noevent){
            Client.instance.packet.permissionidstate=permissionidstate; Client.instance.packet.noevent=noevent;
            Minecraft.getInstance().getConnection().send(packet);
            Client.instance.packet.permissionidstate=0;   Client.instance.packet.noevent=false;
        }
        boolean isInvalid(float val) {
            if(Float.isNaN(val)) {  	return true;	}
            if(Float.isInfinite(val)) { 	return true; 	}
            return false;
        }
        boolean isInvalid(double val) {
            if(Double.isNaN(val)) { 	return true; 	}
            if(Double.isInfinite(val)) { 	return true; 	}
            return false;
        }





    }




  /*  public static Entity getEntity(@NotNull ServerboundInteractPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
       packet.write(packetBuf);
        return Minecraft.getInstance().level.getEntityById(packetBuf.readVarInt());
    }

    public static InteractType getInteractType(@NotNull ServerboundInteractPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      packet.write(packetBuf);
        packetBuf.readVarInt();
        return packetBuf.readEnumConstant(InteractType.class);
    }
*/





}

