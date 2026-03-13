package base.client.helpers.utils;

import base.client.event.EventManager;
import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderWorld;
import base.client.helpers.Helper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class WorldToScreenUtil implements Helper {
    public static final WorldToScreenUtil INSTANCE = new WorldToScreenUtil();
    private static Matrix4f matrix4f, projectionMatrix;

    private WorldToScreenUtil() {
        EventManager.register(this);
    }

    public static Vector2f calculateScreenPos(Vec3 pos) {
        pos = pos.subtract(mc.gameRenderer.getMainCamera().position());
        Vector3f transformedPos = new Matrix4f(projectionMatrix).mul(matrix4f).transformProject((float) pos.x,
                (float) pos.y, (float) pos.z, new Vector3f());
        Vector3f screenPos = transformedPos.mul(1, -1, 1).add(1, 1, 0);

        screenPos = screenPos.mul(mc.getMainRenderTarget().width * 0.5F, mc.getMainRenderTarget().height * 0.5F, 1)
                .div((float) mc.getWindow().getGuiScale(), (float) mc.getWindow().getGuiScale(), 1);

        return transformedPos.z < 1 ? new Vector2f(screenPos.x, screenPos.y) : null;
    }

    @EventTarget
    public void onRenderWorld(EventRenderWorld event) {
        matrix4f = new Matrix4f(event.matrixStack().last().pose());
        projectionMatrix = new Matrix4f(event.matrixStack().last().pose());
    }
}