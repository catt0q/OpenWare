package base.client.feature.settings.impl;


import base.client.feature.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting {

    public final List<String> modes;
    public String defMode;
    public String currentMode;
    public int index;

    public ModeSetting(String name, String currentMode, Supplier<Boolean> visible, String... options) {
        this.name = name;
        this.modes = Arrays.asList(options);
        this.index = modes.indexOf(currentMode);
        this.defMode = currentMode;
        this.currentMode = modes.get(index);
        setVisible(visible);
        addSettings(this);
    }

    public ModeSetting(String name,String desc, String currentMode, Supplier<Boolean> visible, String... options) {
        this.name = name;
        this.desc = desc;
        this.modes = Arrays.asList(options);
        this.index = modes.indexOf(currentMode);
        this.defMode = currentMode;
        this.currentMode = modes.get(index);
        setVisible(visible);
        addSettings(this);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public void setListMode(String selected) {
        if(exsist(selected)) {
            this.currentMode = selected;
            this.index = this.modes.indexOf(selected);
        }else {
            this.index = this.modes.indexOf(currentMode);
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public String getOptions() {
        return this.modes.get(this.index);
    }
    public boolean exsist(String mode) {
        String md=mode.toLowerCase();
        for(String mods:modes) {
            if(mods.toLowerCase().contains(md)) {
                return true;
            }

        }
        return false;
    }

    public String exsistmode(String mode) {
        String md=mode.toLowerCase();
        for(String mods:modes) {
            if(mods.toLowerCase().equals(md)) {
                return mods;
            }

        }
        return null;
    }
    public String exsistpredmode(String mode) {
        String md=mode.toLowerCase();
        for(String mods:modes) {
            if(mods.toLowerCase().equals(md)) {
                return mods;
            }
        }
        for(String modc:modes) {
            if(modc.toLowerCase().contains(md)) {
                return modc;
            }
        }
        return null;
    }


    public String getDefMode() {
        return defMode;
    }


}