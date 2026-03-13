package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

import static base.client.helpers.Helper.mc;

public class DesyncFix extends Module {

    public DesyncFix() {
        super("DesyncFix", "Fixes movement desync", Type.Misc);
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (mc.player == null) return;

        PacketHelper.Values pc = Client.instance.packet;

        Input ff = mc.player.input.keyPresses;

        pc.sendPacket(
                new ServerboundPlayerInputPacket(
                        new Input(
                                ff.forward(),
                                ff.backward(),
                                ff.left(),
                                ff.right(),
                                ff.jump(),
                                mc.player.isCrouching(),
                                ff.sprint()
                        )
                ),
                1,
                true
        );
    }
}
