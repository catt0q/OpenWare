package base.client.feature.impl;
import base.client.feature.Module;

public class LockedModule extends Module {



    public LockedModule(String label, String desc, Type type) {
  super(label,desc,type);
     this.state = true;
    }

    public LockedModule(String label, String desc, Type type, int startbind) {
        super(label,desc,type,startbind);
        this.setBind(0);
        this.state = true;
    }

    @Override
    public boolean getState() {
        return true;
    }
    @Override
    public void setState(boolean state) {

    }
    @Override
    public void toggle() {

    }
    @Override
    public int getBind() {
        return 0;
    }
    @Override
    public void setBind(int bind) {

    }
    @Override
    public void onEnable() {


    }
    @Override
    public void onDisable() {

    }


}