package base.client.feature.impl.misc;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.HumanoidArm;

public class HandDerp extends Module {

    boolean starthandright=false;
    public TimerHelper ttimer= new TimerHelper();
    public NumberSetting delay;
    public ModeSetting eventMode = new ModeSetting("Mode", "OnAttack", () -> true, "OnAttack", "OnDelay");

    public HandDerp() {
        super("HandDerp", "Позволяет быстро визуально менять правую и левую руку", Type.Misc);
        delay = new NumberSetting("delay", 50, 10, 1000, 10, () -> eventMode.getCurrentMode().equals("OnDelay"));
        addSettings(eventMode,delay);
    }

    boolean lastright=false;

    @Override
    public void onEnable() {ttimer.reset();
        lastright=mc.options.mainHand().get().equals(HumanoidArm.RIGHT);
         starthandright=mc.options.mainHand().get().equals(HumanoidArm.RIGHT);
        super.onEnable();
    }
    @Override
    public void onDisable() {
        boolean realright=mc.options.mainHand().get().equals(HumanoidArm.RIGHT);
        if(realright!=starthandright) {
            changehand();
        }
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion eventUpdate) {
        if(eventMode.getCurrentMode().equals("OnDelay") && ttimer.hasTimeElapsed((long) delay.getValue(),true)) {
            changehand();
        }
    }
    @EventTarget
    public void onSendPacket(EventSendPacketPost event) {
        Packet<?> packetp=event.getPacket();
            if (packetp instanceof ServerboundInteractPacket) {
                ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;
                base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                ServerboundInteractPacket.Action action = ac2.getAction();
                if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)) {
                    changehand();
                }

        }
    }


    private void changehand() {
        OptionInstance<HumanoidArm> mainHandOption = Minecraft.getInstance().options.mainHand();
        mainHandOption.set(mainHandOption.get().getOpposite());
        Minecraft.getInstance().options.save();

        lastright=!lastright;


    }

}
