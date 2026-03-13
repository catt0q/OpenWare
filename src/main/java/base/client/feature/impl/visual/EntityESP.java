package base.client.feature.impl.visual;

import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderBlockEntities;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.client.ClientHelper;
import base.client.helpers.impl.render.PaletteHelper;
import base.client.helpers.impl.render.render3d.RenderPresets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import base.client.helpers.impl.render.RenderHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.glfw.*;

public class EntityESP extends Module {
    public static ColorSetting colorEsp;
    public static BooleanSetting OnlyPlayers, IncludeYourself;
    public static ModeSetting espMode, chamsMode, colorMode;

    public EntityESP() {
        super("EntityESP", "Показывает игроков через стены", Type.Visuals);
        espMode = new ModeSetting("ESP Mode", "Box", () -> true, "Box", "2D", "Chams");

        // Chams
        chamsMode = new ModeSetting("Chams Mode", "Outline", () -> espMode.getCurrentMode().equals("Chams"), "Outline",
                "OutRender");
        // Chams

        colorMode = new ModeSetting("Color Box Mode", "Custom", () -> espMode.currentMode.equals("Box")
                || espMode.currentMode.equals("2D") || espMode.currentMode.equals("Glow"), "Astolfo", "Rainbow",
                "Client", "Custom");
        colorEsp = new ColorSetting("ESP Color", new Color(0xFFFFFF).getRGB(),
                () -> !colorMode.currentMode.equals("Client") && (espMode.currentMode.equals("2D")
                        || espMode.currentMode.equals("Box") || espMode.currentMode.equals("Glow")));

        OnlyPlayers = new BooleanSetting("Only Players", true, () -> true);
        IncludeYourself = new BooleanSetting("Include Yourself", false, () -> true);

        addSettings(espMode,
                chamsMode, colorMode, colorEsp,
                OnlyPlayers, IncludeYourself

        );
    }

    @Override
    public void onEnable() {

        super.onEnable();

    }

    @Override
    public void onDisable() {

        super.onDisable();

    }

    @EventTarget
    public void onRender3D(EventRenderBlockEntities e) {
        if (!getState())
            return;
        // null checks for rejoin safety
        if (mc.level == null || mc.player == null || e.getCamera() == null)
            return;

        int color = 0;

        switch (colorMode.currentMode) {
            case "Client":
                color = ClientHelper.getClientColor().getRGB();
                break;
            case "Custom":
                color = colorEsp.getColorValue();
                break;
            case "Astolfo":
                color = PaletteHelper.astolfo(10).getRGB();
                break;
            case "Rainbow":
                color = PaletteHelper.rainbow(300, 1, 1).getRGB();
                break;
        }

        if (espMode.currentMode.equals("Box")) {

            float partialTicks = Minecraft.getInstance().getFrameTimeNs();

            Color cl = new Color(color);
            int i = color;
            PoseStack poseStack = new PoseStack();

            Vec3 cameraPos = e.getCamera().position();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            for (Entity ent : mc.level.entitiesForRendering()) {

                if ((ent instanceof AbstractClientPlayer || !EntityESP.OnlyPlayers.isEnabled())
                        && !(ent == Minecraft.getInstance().player && !EntityESP.IncludeYourself.isEnabled())) {

                    VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(
                            RenderPresets.TWLines);

                    double x = ent.xOld + (ent.getX() - ent.xOld) * e.getPartial();
                    double y = ent.yOld + (ent.getY() - ent.yOld) * e.getPartial();
                    double z = ent.zOld + (ent.getZ() - ent.zOld) * e.getPartial();

                    AABB AABB = ent.getBoundingBox();

                    AABB aabb2 = new AABB(AABB.minX - ent.getX() + x - 0.05,
                            AABB.minY - ent.getY() + y, AABB.minZ - ent.getZ() + z - 0.05,
                            AABB.maxX - ent.getX() + x + 0.05,
                            AABB.maxY - ent.getY() + y + 0.15, AABB.maxZ - ent.getZ() + z + 0.05);

                    poseStack.pushPose();

                    RenderHelper.renderLineBox(
                            poseStack,
                            vertexConsumer,
                            aabb2

                            ,
                            cl.getRed() / 255.0f, cl.getGreen() / 255.0f, cl.getBlue() / 255.0f,
                            cl.getAlpha() / 255.0f);

                    poseStack.popPose();
                }
            }

            // GlStateManager.popMatrix();

        }

    }

    public static void test1() {
    }

    public static void test2() {
    }

    public static void test3() {

    }

    public static void test4() {

    }

}
