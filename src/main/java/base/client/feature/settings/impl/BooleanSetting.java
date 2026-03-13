package base.client.feature.settings.impl;

import base.client.feature.settings.Setting;

import java.util.function.Supplier;



public class BooleanSetting extends Setting {


    private boolean defstate;
    private boolean state;
    private String desc;

    public BooleanSetting(String name, String desc, boolean state, Supplier<Boolean> visible) {
        this.name = name;
        this.desc = desc;
        this.state = state;
        this.defstate = state;
        setVisible(visible);
    }

    public BooleanSetting(String name, boolean state, Supplier<Boolean> visible) {
        this.name = name;
        this.state = state;
        this.defstate = state;
        setVisible(visible);
    }

    public boolean isDefstate() {
        return defstate;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isEnabled() {
        return state;
    }

    public void setBoolValue(boolean state) {
        this.state = state;
    }
}