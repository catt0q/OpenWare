package base.mixin.client;

import base.client.Client;
import base.client.event.EventManager;
import base.client.event.events.impl.game.EventMessage;
import base.client.feature.impl.exploit.NoRotate;
import base.client.feature.impl.info.FlagDetector;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.FlagHelper;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.block.Rotation;

import static base.client.helpers.Helper.pc;
import static base.client.helpers.Helper.mc;
import static net.minecraft.client.multiplayer.ClientPacketListener.setValuesFromPositionPacket;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    boolean skip = false;

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void injectmes(String content, CallbackInfo ci) {
        if (pc.chatnoevent || ChatHelper.noevent)
            return;

        EventMessage event = new EventMessage(content);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        if (content != event.getMessage()) {
            pc.chatnoevent = true;
            Minecraft.getInstance().getConnection().sendChat(event.getMessage());
            pc.chatnoevent = false;
            ci.cancel();
        }

    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void injectonppl(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (skip)
            return;

        FlagHelper.Flags fh = Client.instance.flagsch;
        if (fh.getFlags().size() > 0) {
            FlagHelper.SkippedFlag sf = fh.getFlags().pollLast();
            if ((sf.isBreakifbadpos() && (sf.getPosX() != packet.change().position().x
                    || sf.getPosY() != packet.change().position().y || sf.getPosZ() != packet.change().position().z))
                    || (sf.isBreakifbadteleport() && sf.getTpid() != packet.id())) {
                fh.getFlags().clear();
            } else {
                ci.cancel();
                return;
            }

        }

    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"), cancellable = true)
    private void injectonpplpost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (mc.player == null)
            return;

        if (NoRotate.TpOnS08) {
            if (!NoRotate.S08ChangeRot) {
                mc.player.setYRot(NoRotate.lastyaw);
                mc.player.setXRot(NoRotate.lastpitch);
                mc.player.yRotO = NoRotate.lastpyaw;
                mc.player.xRotO = NoRotate.lastppitch;

            }
        } else {
            if (!NoRotate.S08ChangeRot) {
                mc.player.setYRot(NoRotate.lastyaw);
                mc.player.setXRot(NoRotate.lastpitch);
                mc.player.yRotO = NoRotate.lastpyaw;
                mc.player.xRotO = NoRotate.lastppitch;

            }

        }

        if (Client.instance.featureManager.getModuleByClass(FlagDetector.class).getState()
                && FlagDetector.HideSkippedFlags.isEnabled()) {
            ChatHelper.addChatMessage(FlagDetector.generatemessage(packet));
        }
    }

}