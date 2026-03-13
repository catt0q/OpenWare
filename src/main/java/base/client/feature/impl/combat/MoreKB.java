package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventOnSprint;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.input.EventSprint;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.event.events.impl.packet.EventSendPacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class MoreKB extends Module {

    public TimerHelper timerr;

    int index1;
    int index2;
    int index3;
    int state=0;
    float yawf2;
    float pitchf2;
    boolean actkb=false;
    boolean wassprintreset=false;
  public static boolean issprintblocked=false;

    int attacktick=0;
    int mkdelay=0;
    int mkstartdelay=0;


    public NumberSetting HurtTime= new NumberSetting("HurtTime", 10, 1, 10, 1,() -> true);
    public NumberSetting Chance= new NumberSetting("Chance", 100, 1, 100, 1,() -> true);
    public static BooleanSetting ReverseKB = new BooleanSetting("Reverse", false,() -> true);
    public static NumberSetting ReverseAngle = new NumberSetting("Angle", 90.0F, -180F, 180.0F, 1.0F,()->ReverseKB.isEnabled());
    public NumberSetting MinDelay;
    public NumberSetting MaxDelay;

    public NumberSetting MinStartDelay;
    public NumberSetting MaxStartDelay;




    ModeSetting Mode = new ModeSetting("Mode", "PacketSimple",() -> true, "PacketSimple","PacketDouble","PacketHidden","SprintReset","WStop","STap","None");
    public static BooleanSetting OnlyInSprint,HasSprintOnAttack,CorrectSprint;


    public MoreKB() {
        super("MoreKB", "Вы откидываете противника более эффективно", Type.Combat);
        OnlyInSprint = new BooleanSetting("Only In Sprint", false,() ->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));
        HasSprintOnAttack = new BooleanSetting("HasSprintOnAttack", true,() ->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));
        CorrectSprint = new BooleanSetting("CorrectSprint","Спринтиться только если зажата клавиша вперёд", true,() ->true);


        this.MinDelay = new NumberSetting("Min Delay", 2, 1, 7, 1,()->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));
        this.MaxDelay = new NumberSetting("Max Delay", 2, 1, 7, 1,()->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));
        this.MinStartDelay = new NumberSetting("Min Start Delay", 0, 0, 7, 1,()->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));
        this.MaxStartDelay = new NumberSetting("Max Start Delay", 1, 0, 7, 1,()->this.Mode.getCurrentMode().equals("SprintReset") || this.Mode.getCurrentMode().equals("WStop") || this.Mode.getCurrentMode().equals("STap"));


        this.timerr = new TimerHelper();

        this.addSettings(Mode,
                MinDelay,MaxDelay,MinStartDelay,MaxStartDelay,
                OnlyInSprint,HasSprintOnAttack,
                ReverseKB,ReverseAngle,
                HurtTime,Chance );

    }



    @Override
    public void onEnable() {
        updatedelay();
        issprintblocked=false;
        wassprintreset=false;
        actkb=false;
        index2=0;
        index1=0;
        state=0;
        attacktick=0;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        issprintblocked=false;
        super.onDisable();
    }
    @EventTarget
    public void onEMI(EventMoveInput eventMove) {
        if(state>0) {

            switch(Mode.getCurrentMode()) {
                case("WStop"):
                    eventMove.setfor(0); mc.player.setSprinting(false);
                    eventMove.setSprint(false);
                    issprintblocked=true;
                    break;
                case("STap"):
                    eventMove.setfor(-1);mc.player.setSprinting(false);
                    eventMove.setSprint(false);
                    issprintblocked=true;
                    break;






            }
            state--;
        }else {
            issprintblocked=false;
        }



    }


    @EventTarget
    public void onPrePacket(EventSendPacketCancel event) {

        if(!event.isCancelled()) {
            Packet<?> packetp=event.getPacket();
            if (packetp instanceof ServerboundInteractPacket c02) {


                base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                ServerboundInteractPacket.Action action = ac2.getAction();
                Entity ent=mc.level.getEntity(ac2.getEntityId());
                if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK) &&
                        (ent instanceof LivingEntity )){

                    wassprintreset=mc.player.isSprinting();



                    if(isgoodtimer((LivingEntity) ent) && (mc.player.input.keyPresses.forward() || CorrectSprint.isEnabled())) {
                        if(Mode.getCurrentMode().equals("PacketDouble")) {
                            if (mc.player.isSprinting()) {
                                PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            }
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                            mc.player.setSprinting(true);

                        }else 	if(Mode.getCurrentMode().equals("PacketSimple")) {
                            if (mc.player.isSprinting() || !OnlyInSprint.isEnabled()) {
                                PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            }
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));  mc.player.setSprinting(true);
                        }else 	if(Mode.getCurrentMode().equals("PacketHidden")) {
                            if (mc.player.isSprinting()) {
                                PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                            }
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP));
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));  mc.player.setSprinting(true);
                            PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP));
                        }else 	if(Mode.getCurrentMode().equals("Legit")) {



                        }



                        if(ReverseKB.isEnabled()) {

                            yawf2= pc.LastYaw;  pitchf2= pc.LastPitch;

                            ACUtil.send117DuplPacket(pc.LastYaw+ReverseAngle.getValue(), pc.LastPitch);
                        }
                    }
                }




            }



        }




    }

    @EventTarget
    public void onSprint(EventSprint event) {

        if(Mode.getCurrentMode().equals("SprintReset")) {
            //if(state>0) {event.setsprint(false);}
        }

    }
    @EventTarget
    public void onSprint(EventOnSprint event) {
        if(Mode.getCurrentMode().equals("SprintReset")) {
            if(state>0) {state--;   event.setsprint(false); 	}
        }
if(issprintblocked){
    event.setsprint(false);
}


    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {
        PacketHelper.Values pc=Client.instance.packet;
        this.setSuffix(Mode.getCurrentMode());


        if(pc.lastattackentity!=null && pc.lastattackentity instanceof LivingEntity && isgoodtimer((LivingEntity) pc.lastattackentity) && (mc.player.input.keyPresses.forward() || CorrectSprint.isEnabled())) {

            if(Mode.getCurrentMode().equals("SprintReset")) {
                if(pc.attackticks>=mkstartdelay &&  pc.attackticks<2+mkstartdelay && (mc.player.isSprinting() || !OnlyInSprint.isEnabled()) && (wassprintreset || !HasSprintOnAttack.isEnabled())) {
                    state=mkdelay; updatedelay();  	 	updatestartdelay();
                }
            }
            if(Mode.getCurrentMode().equals("WStop")) {
                if(pc.attackticks>=mkstartdelay &&  pc.attackticks<2+mkstartdelay && (mc.player.isSprinting() || !OnlyInSprint.isEnabled()) && (wassprintreset || !HasSprintOnAttack.isEnabled())) {
                    state=mkdelay; updatedelay();  		updatestartdelay();
                }
            }
            if(Mode.getCurrentMode().equals("STap")) {
                if(pc.attackticks>=mkstartdelay &&  pc.attackticks<2+mkstartdelay && (mc.player.isSprinting() || !OnlyInSprint.isEnabled()) && (wassprintreset || !HasSprintOnAttack.isEnabled())) {
                    state=mkdelay; updatedelay();  		updatestartdelay();
                }
            }

        }







    }


    @EventTarget
    public void onPostPacket(EventSendPacketPost event) {
        PacketHelper.Values pc=Client.instance.packet;

            Packet<?> packetp=event.getPacket();
            if (packetp instanceof ServerboundInteractPacket && actkb) {
                if(ReverseKB.isEnabled()) {
                    ACUtil.send117DuplPacket(yawf2, pitchf2);
                }

                actkb=false;
            }

    }

    boolean isgoodtimer(LivingEntity e) {
        if(e.hurtTime>HurtTime.getValue()) return false;
        return (Math.random()*100<Chance.getValue());
    }








    public void updatedelay() {
        mkdelay=(int) MathematicHelper.randomizeFloat(MinDelay.getValue(),MaxDelay.getValue());
    }
    public void updatestartdelay() {
        mkstartdelay=(int) MathematicHelper.randomizeFloat(MinStartDelay.getValue(),MaxStartDelay.getValue());
    }
}
