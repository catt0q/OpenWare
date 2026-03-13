package base.client.feature.impl.client;

import base.client.feature.impl.LockedModule;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;

public class ChatSettings extends LockedModule {

   public static ModeSetting CommandBrackets = new ModeSetting("CommandBrackets", "{}", () -> true, "<>", "{}", "[]");
    public static ModeSetting ClientNameColor = new ModeSetting("ClientNameColor", "Red", () -> true,
            "Blue","DarkBlue", "Red","DarkRed","Green","DarkGreen", "Yellow","Gray","DarkGray");


    public ChatSettings() {
        super("ChatSettings", "Настройки внутренних сообщений(этот модуль не обязательно включать)", Type.Client);
        this.addSettings(CommandBrackets,ClientNameColor);
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

}
