package base.client.feature.impl.minigames;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.inventory.ClickType;

public class BWAutoLeave extends Module {
  int state=0;
    public TimerHelper timerr= new TimerHelper();
    public ModeSetting serverMode,MessageMode;
    public static NumberSetting relodams;
    public static NumberSetting Delay;
    public BWAutoLeave() {
        super("BWAutoLeave", "Помогает выйти из в лобби по оканчанию игры", Type.Minigames);
        serverMode = new ModeSetting("Server Mode", "ForsCraft", () -> true,"ForsCraft");
        MessageMode = new ModeSetting("Message Mode", "HUB", () -> true,"HUB","Leave");
        relodams = new NumberSetting("Reload ms", 1000, 0, 5000, 100,()->true);
        Delay= new NumberSetting("Delay", 2, 0, 15, 1F, () -> true );

        this.addSettings(serverMode,Delay,MessageMode,relodams);
    }
    @Override
    public void onEnable() {
        state=0;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        if(state>=4+Delay.getValue()){
            state=-1;
            mc.player.closeContainer();
        }
        if(state>=3+Delay.getValue()){
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 2, 0, ClickType.PICKUP, mc.player);

  }

        if(state>=2){
            state++;
        }


    }

    @EventTarget
    public void onSendPacket(EventReceivePacketPre event) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp=event.getPacket();
        if(packetp instanceof ClientboundSystemChatPacket) {
            if(serverMode.getCurrentMode().equals("ForsCraft")) {
                ClientboundSystemChatPacket sPacketChat=(ClientboundSystemChatPacket) event.getPacket();  String rawMessage = sPacketChat.content().getString();

            //    if(rawMessage.startsWith("/")) return;   String number="";


if(rawMessage.contains("Индивидуальная статистика за игру") || rawMessage.contains("Game statistics")) {
    Minecraft.getInstance().player.connection.sendChat("/leave");

    state=1;
}




            }

        }
       if (event.getPacket() instanceof ClientboundContainerSetContentPacket) {
            ClientboundContainerSetContentPacket cont=(ClientboundContainerSetContentPacket) event.getPacket();
           if(state>0){
               String mes=cont.items().get(2).getDisplayName().getString();
            if(cont.items().size()<46 && (mes.contains("Подтвердить") || mes.contains("Confirm"))){
                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 2, 0, ClickType.PICKUP, mc.player);

                state=2;
                event.setCanceled(true);
            }

           }


        }
    }
}
