package base.client.feature.impl.misc;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.feature.settings.impl.StringSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.NicknameGeneratorUtil;
import base.client.helpers.utils.TimerHelper;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Spammer extends Module {

    private NumberSetting delay,rsymbols,mcount;
    private ModeSetting SMode,RMode,RULCase;
    private StringSetting message;
    private NumberSetting PlayerLimit;


    TimerHelper timerr=new TimerHelper();

    public Spammer() {
        super("Spammer", "Автоматически спамит сообщениями в чат", Type.Misc);
        SMode = new ModeSetting("Mode", "Default", () -> true, "Default","Direct");


        message = new StringSetting("Message", "useless useless useless", () -> true);

        delay = new NumberSetting("Delay", 3000, 50, 10000, 10, () -> true);
        mcount = new NumberSetting("Message Count", 1, 1, 100, 1, () -> true);
        PlayerLimit = new NumberSetting("PlayerLimit", 5, 0, 200, 1,()->(this.SMode.getCurrentMode().equals("Direct")));

        RMode = new ModeSetting("Random Mode", "RandomAlphabet", () -> true, "None","RandomAlphabet","RandomNickName");
        rsymbols = new NumberSetting("Random Symbols", 3, 1, 100, 1, () -> RMode.getCurrentMode().equals("RandomAlphabet"));
      RULCase = new ModeSetting("Random Case", "Low", () -> !RMode.getCurrentMode().equals("None"), "Low","Every","Upper");



        addSettings(SMode,PlayerLimit,message,delay,mcount,RMode,  rsymbols,RULCase);
    }

    @Override
    public void onEnable() {
        String mode = SMode.getOptions();
        timerr.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventRunGameLoop event) throws IOException {
        String mode = SMode.getCurrentMode();
        this.setSuffix(mode + ", " + (int) delay.getValue());
        if (timerr.hasTimeElapsed((long) delay.getValue(),true)) {
        if (mode.equalsIgnoreCase("Default")) {

            for(int i=0;i<mcount.getValue();i++){
                Minecraft.getInstance().player.connection.sendChat(getnextessage()+" "+ getRandom() );

            }


        } else if (mode.equalsIgnoreCase("Direct")) {

                String str="";



int sz=0;
            for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                    if (info == null)
                        continue;




                //    e.getDisplayName().getString()

if(info.getProfile().id().equals(mc.player.getUUID())) continue;


if(sz>PlayerLimit.getValue()){
    return;
}

                        for(int i=0;i<mcount.getValue();i++) {

                            Minecraft.getInstance().player.connection.sendChat("/message " + info.getProfile().name() + " " + getnextessage()+" "+getRandom() );
                                     }

                sz++;
            }
        }
        }
    }


    private String getRandom(){
        String ss="";
        switch (RMode.getCurrentMode()){
            case ("RandomAlphabet"):
                ss+=(RandomStringUtils.randomAlphabetic((int)rsymbols.getValue()));
      break;
            case ("RandomNickName"):
                ss+= NicknameGeneratorUtil.generatenickname();
                break;

    }


        if(RULCase.getCurrentMode().equals("Low")){
            ss=ss.toLowerCase();
        }
        else if(RULCase.getCurrentMode().equals("Upper")){
            ss=ss.toUpperCase();
        }

        return ss;
    }

    private String getnextessage(){
    return message.getCurrentText();
    }


    private List<Player> getPlayerByTab() {
        ArrayList<Player> list = new ArrayList<>();
        for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
            if (info == null) {
                continue;
            }
            list.add((Player) mc.level.getEntity(info.getProfile().id()));
        }
        return list;
    }
    
    
}
