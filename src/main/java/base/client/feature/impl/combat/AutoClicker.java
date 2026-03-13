package base.client.feature.impl.combat;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.TimerHelper;

public class AutoClicker extends Module {
    public TimerHelper lastattacktimer = new TimerHelper();


    int index=0;
    int index1=0;
    int index2=0;
    int index3=0;
    long lastms1=System.currentTimeMillis();
    long lastms=System.currentTimeMillis();
    int completeclicks=0;
    float nextcps = 0;

    boolean cpsup=false;

    ModeSetting CPSMode = new ModeSetting("CPS Mode", "Random", () -> true, "Random", "Smooth", "UpDown");
    NumberSetting MaxClicksPerTick = new NumberSetting("Max Clicks Per Tick", 2, 1, 20, 1, () -> true);
    NumberSetting CpsMin = new NumberSetting("Min Cps", 12, 1, 40, 1, () -> true);
    NumberSetting CpsMax = new NumberSetting("Max Cps", 15, 1, 40, 1, () -> true);
    BooleanSetting DoubleClicking = new BooleanSetting("DoubleClicking", false, () -> true);
    NumberSetting DoubleClickChance = new NumberSetting("DoubleClick Chance", 60, 0, 100, 1, () -> (true) && DoubleClicking.isVisible() && DoubleClicking.isEnabled());

    public AutoClicker() {
        super("AutoClicker", "Автоматически кликает вместо вас", Type.Combat);
        this.addSettings(
                CPSMode,MaxClicksPerTick,CpsMin,CpsMax,DoubleClicking,DoubleClickChance
        );
    }

    @Override
    public void onEnable() {index1=0;
        index2=0;
        completeclicks=0;
          nextcps = 0;

          cpsup=false;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick e){

        int currcps = 0;
             currcps = (int) Math.min(completeclicks,MaxClicksPerTick.getValue());
            completeclicks -= currcps;

        if (usingautoclicker()) {

            nextcps = CpsMin.getValue();
        }


        while (currcps > 0) {
            currcps--;

            mc.startAttack();
               mc.missTime=0;
        }

        completeclicks = (int) Math.min(completeclicks, MaxClicksPerTick.getValue());


    }

    @EventTarget
    public void oRGL(EventRunGameLoop event) {

            if (!usingautoclicker()) {
                completeclicks = Math.min(completeclicks, 1);
            } else if (this.lastattacktimer.hasTimeElapsed((long) (1000.0 / nextcps), true)) {
                completeclicks++;
                if (DoubleClicking.isEnabled() && Math.random() * 100 < DoubleClickChance.getValue()) {
                    completeclicks++;
                }
                updatecps();
            }
            completeclicks = (int) Math.min(completeclicks, MaxClicksPerTick.getValue());


    }

    private void updatecps() {

        switch (CPSMode.getCurrentMode()) {
            case ("Random"):
                nextcps = MathematicHelper.randomizeFloat(CpsMin.getValue(), CpsMax.getValue());
                break;
            case ("Smooth"):
                double chancetoincrease = 0.4D;
                double chancetodincrease = 0.4D;
                boolean smt = false;
                if (nextcps > this.CpsMin.getValue()) {
                    if (Math.random() <= chancetodincrease) {
                        nextcps--;
                        smt = true;
                    }
                }
                if (nextcps < this.CpsMax.getValue() && !smt) {
                    if (Math.random() <= chancetoincrease) {
                        nextcps++;
                        smt = true;
                    }
                }

                break;

            case ("UpDown"):
                float step=1;
                float rand121=MathematicHelper.randomizeFloat(0.5F, 1F);
                if(cpsup){
                    float delta=this.CpsMax.getValue()-nextcps;
                    step+=Math.round((delta-1)*rand121);
                    if (nextcps < this.CpsMax.getValue()) {    nextcps+=step;  }else{  nextcps-=step;     cpsup=false;  }
                }else{
                    float delta=nextcps-this.CpsMin.getValue();
                    step+=Math.round((delta-1)*rand121);
                    if (nextcps > this.CpsMin.getValue()) {    nextcps-=step;  }else{   nextcps+=step;    cpsup=true;  }
                }



                break;

        }

    }


private static boolean usingautoclicker(){
        return mc.options.keyAttack.isDown();
}


}
