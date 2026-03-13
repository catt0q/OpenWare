package base.client.feature.impl.visual;

import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import base.client.Client;
import base.client.effekseer.api.EffekseerManager;
import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRender3D;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.visual.hud.HudElement;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.MatrixUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Effekseer extends Module {

    private EffekseerManager manager;
    private final float[] CAMERA_TRANSFORM_DATA = new float[16];
    private final float[] PROJECTION_MATRIX_DATA = new float[16];

    public Effekseer() {
        super("Effekseer", "Отображает да", Type.Visuals);

    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.level == null || this.manager == null)
            return;

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.position();

        // Получаем матрицы из события
        Matrix4f projection = event.getProjection();
        Matrix4f viewMatrix = event.getMatrix4fcamera(); // Используем матрицу из события

        MatrixUtils.getCameraAndProjectionMatrices(
                projection,
                viewMatrix,
                CAMERA_TRANSFORM_DATA,
                PROJECTION_MATRIX_DATA);

        // Настройка рендеринга
        manager.setViewport(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        manager.setCameraMatrix(CAMERA_TRANSFORM_DATA);
        manager.setProjectionMatrix(PROJECTION_MATRIX_DATA);

        // Параметры камеры
        Vector3f lookVec = new Vector3f(camera.forwardVector());
        manager.setCameraParameter(
                lookVec.x, lookVec.y, lookVec.z,
                (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

        // Обновление и отрисовка
        manager.update(60 * getDeltaTime());
        manager.draw();
    }

    public void onEnable() {
        this.manager = new EffekseerManager(Client.instance.loadNatives.getEffekseerManagerCore());

        super.onEnable();
    }

    public void onDisable() {
        super.onDisable();
    }

    private static float getDeltaTime() {
        long last = lastDrawTime;
        if (last == 0) {
            lastDrawTime = System.nanoTime();
            return 1f / 60;
        }

        long now = System.nanoTime();
        lastDrawTime = now;
        return (float) ((now - last) * 1e-9);
    }

    private static long lastDrawTime = 0;

}
