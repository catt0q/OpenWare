package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
public class Timer extends Module {


    public ModeSetting Mode,LerpDirection;
    public NumberSetting timer,lerpupspeed,lerpdownspeed,lerpmin,lerpmax;

    BooleanSetting InfLerp,BreakLerp,OnlyMove,OnlyHorizontal;
boolean lerpup=false;
float lastsetedtimer=1;
boolean wasenabled=false;
boolean nowstable=false;

    public Timer() {
        super("Timer", "Увеличивает скорость игры", Type.Player);

        Mode = new ModeSetting("Mode", "Stable", () -> true, "Stable", "Lerp");
        timer = new NumberSetting("Timer", 2.0F, 0.1F, 10.0F, 0.1F, () -> !Mode.getCurrentMode().equals("Lerp"));
        LerpDirection = new ModeSetting("LerpDirection", "MinToMax", () -> Mode.getCurrentMode().equals("Lerp"), "MinToMax", "MaxToMin");

        lerpupspeed = new NumberSetting("Lerp Speed Up", 0.25F, 0.1F, 10.0F, 0.1F, () -> Mode.getCurrentMode().equals("Lerp"));
        lerpdownspeed = new NumberSetting("Lerp Speed Down", 0.25F, 0.1F, 10.0F, 0.1F, () -> Mode.getCurrentMode().equals("Lerp"));


        lerpmin = new NumberSetting("LerpMin", 1.0F, 0.1F, 10.0F, 0.1F, () -> Mode.getCurrentMode().equals("Lerp"));
        lerpmax = new NumberSetting("LerpMax", 5.0F, 0.1F, 10.0F, 0.1F, () -> Mode.getCurrentMode().equals("Lerp"));

        BreakLerp = new BooleanSetting("BreakLerp", false, () -> Mode.getCurrentMode().equals("Lerp"));
        InfLerp = new BooleanSetting("InfiniteLerp", false, () -> Mode.getCurrentMode().equals("Lerp") && !BreakLerp.isEnabled());
        OnlyMove = new BooleanSetting("Only Move", false, () -> true);
        OnlyHorizontal = new BooleanSetting("Only Horizontal", false, () -> OnlyMove.isEnabled());


        addSettings(Mode,timer,



                LerpDirection,lerpmin,lerpmax,lerpupspeed,lerpdownspeed,

                InfLerp,BreakLerp,

                OnlyMove,OnlyHorizontal);
    }

    @EventTarget
    public void onUpdate(EventOnMovePost event) {
        if (!getState())
            return;
        this.setSuffix("" + timer.getValue());

        if(OnlyMove.isEnabled()){

            if(OnlyHorizontal.isEnabled()){
if(MoveUtil.getspeed2()<0.000001D){
    reenable();
    return;
}

            }else{
                if(MoveUtil.getspeed3()<0.000001D){
                    reenable();
                    return;
                }
            }

        }
        wasenabled=true;

        switch (Mode.getCurrentMode()) {
            case ("Stable"):
                TimerUtil.setTimerspeed(timer.getValue());
                break;
            case ("Lerp"):
if(nowstable) return;

                if(lerpup){
                    float lerpdiff=lerpmax.getValue()-lastsetedtimer;
                      if(lerpupspeed.getValue()>=lerpdiff){
                        TimerUtil.setTimerspeed(lerpmax.getValue());
                        if(BreakLerp.isEnabled()){this.toggle();}
                        if(InfLerp.isEnabled()){
                            lerpup=!lerpup;
                        }else{
                            nowstable=true;
                        }
                    }else{
                          lastsetedtimer+=lerpupspeed.getValue();
                          TimerUtil.setTimerspeed(lastsetedtimer);
                        }

                  }else {
                    float lerpdiff=lastsetedtimer-lerpmin.getValue();
                    if(lerpdownspeed.getValue()>=lerpdiff){

                        TimerUtil.setTimerspeed(lerpmin.getValue());

                        if(BreakLerp.isEnabled()){this.toggle();}
                        if(InfLerp.isEnabled()){
                            lerpup=!lerpup;
                        }else{
                            nowstable=true;
                        }
                    }else{
                        lastsetedtimer-=lerpdownspeed.getValue();
                        TimerUtil.setTimerspeed(lastsetedtimer);
                    }



                }







                break;

        }
        lastsetedtimer=TimerUtil.getTimerspeed();
    }

    private void reenable(){
        if(wasenabled) {
            TimerUtil.reset();
            wasenabled=false;
        }
        lastsetedtimer=1;
        switch (Mode.getCurrentMode()) {
            case ("Lerp"):
                if(LerpDirection.getCurrentMode().equals("MinToMax")){
                    lastsetedtimer=lerpmin.getValue();
                }else {
                    lastsetedtimer=lerpmax.getValue();
                }

                break;
        }

        lerpup=LerpDirection.getCurrentMode().equals("MinToMax");


    }


    public void onEnable() {nowstable=false;
        lastsetedtimer=1;
        switch (Mode.getCurrentMode()) {
            case ("Lerp"):
                if(LerpDirection.getCurrentMode().equals("MinToMax")){
                    lastsetedtimer=lerpmin.getValue();
                }else {
                    lastsetedtimer=lerpmax.getValue();
                }

                break;
        }

        lerpup=LerpDirection.getCurrentMode().equals("MinToMax");
       super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();

        TimerUtil.setTimerspeed(1);
    }

}
