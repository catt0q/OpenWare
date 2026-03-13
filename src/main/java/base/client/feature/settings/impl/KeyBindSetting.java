package base.client.feature.settings.impl;



import base.client.feature.settings.Setting;

import java.util.function.Supplier;

public class KeyBindSetting extends Setting
{
    public int code;

    public KeyBindSetting(String name, int code, Supplier<Boolean> visible) {
        this.name = name;
        this.code = code;
        setVisible(visible);
    }



    public int getKeyCode() {
        return this.code;
    }

    public void setKeyCode(final int code) {
        this.code = code;
    }
}
