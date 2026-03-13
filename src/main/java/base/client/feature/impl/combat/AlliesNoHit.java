package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AlliesNoHit extends Module {
    public BooleanSetting Teams = new BooleanSetting("Teams", true,()->true);
    public BooleanSetting Bots = new BooleanSetting("Bots", true,()->true);
    public BooleanSetting Friends = new BooleanSetting("Friends", true,()->true);
    public BooleanSetting CancelUselessSwing = new BooleanSetting("Cancel Useless Swing", true,()->true);

    boolean cancelswing=false;

    public AlliesNoHit() {
        super("AlliesNoHit", "Не даёт совершить нежелательный удар", Type.Combat);
        this.addSettings(Teams,Bots,Friends,CancelUselessSwing );
    }
    @Override
    public void onEnable() {
        cancelswing=false;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onSendPacket(EventSendPacketCancel e) {
        Packet<?> packetp = e.getPacket();
        if (packetp instanceof ServerboundInteractPacket) {

            ServerboundInteractPacket c02 = (ServerboundInteractPacket) packetp;


            base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
            ServerboundInteractPacket.Action action = ac2.getAction();
            Entity ent = mc.level.getEntity(ac2.getEntityId());
            if (action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK) &&
                    (ent instanceof LivingEntity)
            && (
                    (Friends.isEnabled() && Client.instance.friendManager.isFriend(ent.getName().getString()))
                            || (Bots.isEnabled() && AntiBot.isBotList(ent.getUUID()))
                            || (Teams.isEnabled() && mc.player.getTeam()==ent.getTeam())
            )

            ) {
                if(CancelUselessSwing.isEnabled()) {
                    cancelswing = true;
                }
                e.cancel();
            }



        }else if (e.getPacket() instanceof ServerboundSwingPacket) {
            if(cancelswing) {  e.cancel();	cancelswing=false; }
        }else {
            cancelswing=false;
        }




    }
}
