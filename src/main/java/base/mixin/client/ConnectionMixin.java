package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.packet.*;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Pls dont ask why
@Mixin(value = Connection.class, priority = 1000)
public abstract class ConnectionMixin {


  private static boolean skippost=false;
    boolean skip=false;





  @Inject (method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void injectESPC(Packet<?> packet, ChannelFutureListener callbacks, boolean flush, CallbackInfo ci) {
      if(skip || Client.instance.packet.noevent) return;

        EventSendPacketCancel eventCancel = new EventSendPacketCancel(packet,Client.instance.packet.permissionidstate);
        EventManager.call(eventCancel);
      if(eventCancel.isCancelled()) {ci.cancel();  return; }
      EventSendPacketModify eventModify = new EventSendPacketModify(eventCancel.getPacket(),eventCancel.getPermissionidstate());
      EventManager.call(eventModify);

      EventSendPacketBlink eventBlink = new EventSendPacketBlink(eventModify.getPacket(),eventModify.getPermissionidstate());
      EventManager.call(eventBlink);
      if(eventBlink.isCancelled()) {ci.cancel();  return; }


        if(packet != eventBlink.getPacket()){
            skip=true;
            Minecraft.getInstance().getConnection().getConnection().send(eventBlink.getPacket(),callbacks,flush);

            skip=false;
            ci.cancel();
    }

    }


  @Inject(at = @At("TAIL"), method = "sendPacket")
    private void injectEventSendPacketPost(Packet<?> packet, ChannelFutureListener channelFutureListener, boolean bl, CallbackInfo ci) {
   EventSendPacketPost eventPost = new EventSendPacketPost(packet);
        EventManager.call(eventPost);
  }


   @Redirect(method = "genericsFtw", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
   private static <T extends PacketListener> void redirectInteractionHandlePacket(Packet<T> packet, PacketListener listener) {




            
           if (packet instanceof ClientboundBundlePacket packs) {
               for (Packet<? super ClientGamePacketListener> packsPacket : packs.subPackets()) {
                   EventReceivePacketPre event = new EventReceivePacketPre(packet);
                   EventManager.call(event);
                   Packet packetp = event.getPacket();
                   skippost=true;
                   if(!event.isCanceled()) {
                       skippost=false;
                       packetp.handle(listener);
                   }
               }
           } else {
               EventReceivePacketPre event = new EventReceivePacketPre(packet);
               EventManager.call(event);
               Packet packetp = event.getPacket();
               skippost=true;
               if(!event.isCanceled()) {
                   skippost=false;
                   packetp.handle(listener);

               }

           }

    
   }

    @Shadow
    public abstract boolean isConnected();

    @Inject (method = "channelRead0", at = @At("TAIL"))
    private void inject4(ChannelHandlerContext channelInteractionHandlerContext, Packet<?> packet, CallbackInfo ci) {
if(skippost){
   // skippost=false;
    return;
}


        Connection connection = (Connection) (Object) this;

        if (connection.isConnected()) {
            if (packet instanceof ClientboundBundlePacket packs) {
                for (Packet<? super ClientGamePacketListener> packsPacket : packs.subPackets()) {
                    EventReceivePacketPost eventPost = new EventReceivePacketPost(packsPacket);
                    EventManager.call(eventPost);
                }
            }else{
                EventReceivePacketPost eventPost = new EventReceivePacketPost(packet);
                EventManager.call(eventPost);
            }




        }

    }

}