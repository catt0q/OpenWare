package base.client.feature.impl.minigames;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

public class BWJoinHelper extends Module {

    private boolean skipmess=false;
    public ModeSetting serverMode;
    public static BooleanSetting CancelMessage;

    public BWJoinHelper() {
        super("BWJoinHelper", "Помогает просто написав номер арены зайти на неё", Type.Minigames);
        serverMode = new ModeSetting("Server Mode", "ForsCraft", () -> true,"ForsCraft");
        CancelMessage = new BooleanSetting("Cancel Message", false, () -> true);
        this.addSettings(serverMode,CancelMessage);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onSendPacket(EventSendPacketCancel event) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp=event.getPacket();
        if(packetp instanceof ServerboundChatPacket) {
            if(skipmess) return;
            if(serverMode.getCurrentMode().equals("ForsCraft")) {
                ServerboundChatPacket sPacketChat=(ServerboundChatPacket) event.getPacket();  String rawMessage = sPacketChat.message();

                if(rawMessage.startsWith("/") || rawMessage.length()>6) return;   String number="";
                for(int i=rawMessage.length()-1;i>=0;i--) { 	if(Character.isDigit(rawMessage.charAt(i))) { 	number=rawMessage.charAt(i)+number; 	}else {break;}    }

                if(number.length()>0 && number.length()<3) { 	  skipmess=true;

                    Minecraft.getInstance().player.connection.sendChat("/bw rjoin BW-"+number);   skipmess=false;
                    if(CancelMessage.isEnabled()){
                        event.cancel();
                    }

                }



            }

        }

    }


}
