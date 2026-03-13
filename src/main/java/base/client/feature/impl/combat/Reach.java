package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;

public class Reach extends Module {

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }
    public static BooleanSetting Attack = new BooleanSetting("Attack", "", true, () -> true);
    public static BooleanSetting Interact = new BooleanSetting("Interact", "", false, () -> true);

    public static NumberSetting Attackrange= new NumberSetting("Attack Range", 3.3F, 0, 6, 0.01F,()->true);
    public static NumberSetting Interactrange= new NumberSetting("Interact Range", 4.5F, 0, 8, 0.01F,()->true);
    public Reach() {
        super("Reach", "Увеличивает дистанцию взаимодействия/атаки", Type.Combat);
        this.addSettings(
                Attack,Attackrange,Interact,Interactrange
        );
    }

    public static double getattackrange(double currreach){
        if(Client.instance.featureManager.getModuleByClass(Reach.class).getState() && Attack.isEnabled()){
            return Attackrange.getValue();
        }
        return currreach;
    }

    public static double getinteractrange(double currreach){
        if(Client.instance.featureManager.getModuleByClass(Reach.class).getState() && Interact.isEnabled()){
            return Interactrange.getValue();
        }
        return currreach;
    }



}
