package base.mixin.client.accessors;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface ServerboundMovePlayerPacketAccessor { 
    @Accessor("x")
    void setX(double x);

    @Accessor("y")
    void setY(double y);

    @Accessor("z")
    void setZ(double z);

    @Accessor("yRot")
    void setYaw(float yaw);

    @Accessor("xRot")
    void setPitch(float pitch);

    @Accessor("onGround")
    void setOnGround(boolean onGround);

    @Accessor("horizontalCollision")
    void setHorizontalCollision(boolean horizontalCollision);

    @Accessor("hasPos")
    void setChangePosition(boolean changePosition);

    @Accessor("hasRot")
    void setChangeLook(boolean changeLook);
}
