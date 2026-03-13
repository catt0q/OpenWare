package base.client.feature.impl.misc;

import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class BetterFightSound extends Module {

    public BetterFightSound() {
        super("BetterFightSound", "Звуки убрать да", Type.Misc);
    }
    SoundEvent[] sounds = new SoundEvent[]{
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundEvents.PLAYER_ATTACK_WEAK,
            SoundEvents.PLAYER_ATTACK_STRONG,
            SoundEvents.PLAYER_ATTACK_NODAMAGE,
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundEvents.PLAYER_ATTACK_KNOCKBACK
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre e2) {
        Packet<?> p = e2.getPacket();
        if (p instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket p2 = (ClientboundSoundPacket) p;
            for (SoundEvent sound : sounds) {
                if (!p2.getSound().equals(sound)) continue;
                e2.setCancelled(true);
                break;
            }
        }
    }
}
