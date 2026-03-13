package base.client.feature.impl.info;

import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.StringSetting;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ChatNotifier extends Module {
     public static BooleanSetting Sound;
    public static StringSetting CustomMessage;
    public static ModeSetting Mode;
    public ChatNotifier() {
        super("ChatNotifier", "Оповещает о сообщении в чате", Type.Info);
        Mode = new ModeSetting("Mode", "Nickname", () -> true, "Nickname","Custom");
        CustomMessage = new StringSetting("Custom", "text", () -> Mode.getCurrentMode().equals("Custom"));
        Sound = new BooleanSetting("Sound", true, () -> true);

        this.addSettings(Mode,CustomMessage,Sound);
    }

    @Override
    public void onEnable() {      super.onEnable();  }
    @Override
    public void onDisable() {     super.onDisable();    }


    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        if (event.getPacket() instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket s01=(ClientboundSystemChatPacket) event.getPacket();
            String mess="";
            switch (Mode.getCurrentMode()){
                case ("Nickname"):
                    mess=mc.getGameProfile().name();
                    break;
                case ("CustomMessage"):
                    mess=CustomMessage.getCurrentText();
                    break;

            }

            MutableComponent cm = Component.translatable("");
            Style prevstyle=Style.EMPTY; int count=0;
            for(Component d:s01.content().getSiblings()) {
                String newstr= ChatHelper.legacySanitize(d.getString());
                if((newstr.startsWith(mess+" ") || newstr.contains(" "+mess) || newstr.contains(" "+mess+" ") || newstr.equals(mess))) {
                    count++;
                }
                }

            for(Component d:s01.content().getSiblings()) {



                String newstr= ChatHelper.legacySanitize(d.getString());
                if((newstr.startsWith(mess+" ") || newstr.contains(" "+mess) || newstr.contains(" "+mess+" ") || newstr.equals(mess))) {
                    if(count>1){
                        count=0;
                        cm.append(Component.translatable(newstr).withStyle(d.getStyle())); continue;
                    }

                    Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, Minecraft.getInstance().player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1f);
                       prevstyle=d.getStyle();

                    String[] words = newstr.split(" ");
                    for (String word : words) {
                         if(word.equals(mess)){
                             Style newstyle=d.getStyle().applyFormat(ChatFormatting.YELLOW).withBold(true).withItalic(true);
                             cm.append(Component.translatable(word+" ").withStyle(newstyle));
                         }else{
                             cm.append(Component.translatable(word+" ").withStyle(prevstyle));
                         }

                    }



                }else{

                    cm.append(Component.translatable(newstr).withStyle(d.getStyle()));


                }


            }

            cm.contents=s01.content().getContents();
            s01=new ClientboundSystemChatPacket(cm,s01.overlay());

            event.setPacket(s01);

        }
    }


}
