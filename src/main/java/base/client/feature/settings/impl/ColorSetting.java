package base.client.feature.settings.impl;


import base.client.feature.settings.Setting;

import java.util.function.Supplier;

public class ColorSetting extends Setting {

    private int defcolor;
    private int color;

    public ColorSetting(String name, int color, Supplier<Boolean> visible) {
        this.name = name;
        this.color = color;
        this.defcolor = color;
        setVisible(visible);
    }

    public int getDefcolor() {
        return defcolor;
    }

    public int getColorValue() {
        return color;
    }

    public void setColorValue(int color) {
        this.color = color;
    }
}

