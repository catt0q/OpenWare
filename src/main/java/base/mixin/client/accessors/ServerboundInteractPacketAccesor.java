package base.mixin.client.accessors;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


 @Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccesor {
    @Accessor("action")
    ServerboundInteractPacket.Action getAction();
     @Accessor("entityId")
     int getEntityId();
}
