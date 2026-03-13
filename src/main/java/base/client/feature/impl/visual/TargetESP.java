package base.client.feature.impl.visual;

import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.event.events.impl.render.EventRenderWorld;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.KillAura;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class TargetESP extends Module {

    private final KillAuraNew killAura;

    public TargetESP(KillAuraNew killAura) {
        super("TargetESP", "ПАКАЗЫВАЕТ ГДИ ЧИЛАВИК НАВЕРНАЕ", Type.Visuals);
        addSettings(mode, changeColor, speed, length);
        this.killAura = killAura;
    }

    private final ModeSetting mode = new ModeSetting("Mode", "Sigma", () -> true, "Sigma", "Nurik", "Sigma2");

    private final BooleanSetting changeColor = new BooleanSetting("ChangeColorToHit", true, () -> true);

    private final NumberSetting speed = new NumberSetting("Speed", 2, 0.1f, 5, 0.1f, () -> true);

    private final NumberSetting length = new NumberSetting("Length", 0.7f, 0.1f, 2, 0.1f, () -> true);

    private final Identifier govno = Identifier.fromNamespaceAndPath("quantum", "images/target.png");
    private final List<Test> poses = new ArrayList<>();

    @EventTarget
    private void onRenderWorld(EventRenderWorld e) {
        LivingEntity target = (LivingEntity) killAura.getTarget();

        /*
         * if (target == null) return;
         * 
         * switch (mode.getCurrentMode()) {
         * case "Sigma" -> {
         * GlStateManager._enableBlend();
         * GlStateManager._disableDepthTest();
         * GlStateManager._disableCull();
         * GL11.glEnable(GL11.GL_LINE_SMOOTH);
         * 
         * 
         * com.mojang.blaze3d.vertex.Tesselator tessellator =new
         * com.mojang.blaze3d.vertex.Tesselator(1536);
         * BufferBuilder builder = tessellator.begin(VertexFormat.Mode.TRIANGLE_STRIP,
         * DefaultVertexFormat.POSITION_COLOR);
         * 
         * e.matrixStack().pushPose();
         * Vec3 relative =
         * target.getLerpedPos(e.renderTickCounter().getTickDelta(true)).subtract(mc.
         * gameRenderer.getCamera().getPos());
         * float f = (float) sin(System.currentTimeMillis() / 1000.0 *
         * speed.getValue());
         * e.matrixStack().translate(relative.x, relative.y + (f + 1) / 2 *
         * target.getBbHeight(), relative.z);
         * 
         * Matrix4f matrix4f = e.matrixStack().peek().getPositionMatrix();
         * 
         * float f1 = target.hurtTime > 0 && changeColor.getState() ? 0 : 1;
         * for (int i = 0; i < 361; i++) {
         * double x = sin(Math.toRadians(i)) * 0.7F, z = Math.cos(Math.toRadians(i)) *
         * 0.7F;
         * builder.vertex(matrix4f, (float) x, 0, (float) z).color(0.3f, 0.3f, 1f,
         * 0.5f);
         * builder.vertex(matrix4f, (float) x, f * length.getValue(), (float)
         * z).color(0.3f, 0.3f, 1f, 0.0f);
         * }
         * 
         * for (int i = 0; i < 361; i++) {
         * double x = sin(Math.toRadians(i)) * 0.7F, z = Math.cos(Math.toRadians(i)) *
         * 0.7F;
         * builder.vertex(matrix4f, (float) x, 0, (float) z).color(0.3f, 0.3f, 1f, 1f);
         * builder.vertex(matrix4f, (float) x, 0.01f, (float) z).color(0.3f, 0.3f, 1f,
         * 1f);
         * }
         * 
         * RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         * BufferRenderer.drawWithGlobalProgram(builder.end());
         * 
         * e.matrixStack().pop();
         * 
         * RenderSystem.disableBlend();
         * RenderSystem.enableDepthTest();
         * RenderSystem.enableCull();
         * GL11.glDisable(GL11.GL_LINE_SMOOTH);
         * }
         * case "Sigma2" -> {
         * float f = (float) sin(System.currentTimeMillis() / 1000.0 *
         * speed.getValue());
         * f++;
         * f /= 2;
         * f *= target.getBbHeight();
         * poses.add(new Test(f, System.currentTimeMillis()));
         * poses.removeIf(pose -> System.currentTimeMillis() - pose.time() >= 500);
         * 
         * GlStateManager._enableBlend();
         * GlStateManager._disableDepthTest();
         * GlStateManager._disableCull();
         * GL11.glEnable(GL11.GL_LINE_SMOOTH);
         * 
         * Tessellator tessellator = RenderSystem.renderThreadTesselator();
         * BufferBuilder builder =
         * tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP,
         * VertexFormats.POSITION_COLOR);
         * 
         * e.matrixStack().push();
         * Vec3 relative =
         * target.getLerpedPos(e.renderTickCounter().getTickDelta(true)).subtract(mc.
         * gameRenderer.getCamera().getPos());
         * 
         * e.matrixStack().translate(relative.x, relative.y, relative.z);
         * 
         * Matrix4f matrix4f = e.matrixStack().peek().getPositionMatrix();
         * 
         * float f1 = target.hurtTime > 0 && changeColor.getState() ? 0 : 1;
         * for (int i = 0; i < 361; i++) {
         * double x = sin(Math.toRadians(i)) * 0.7F, z = Math.cos(Math.toRadians(i)) *
         * 0.7F;
         * builder.vertex(matrix4f, (float) x, poses.getLast().value(), (float)
         * z).color(0.3f, 0.3f, 1f, 0.5f);
         * builder.vertex(matrix4f, (float) x, poses.getFirst().value(), (float)
         * z).color(0.3f, 0.3f, 1f, 0.0f);
         * }
         * 
         * for (int i = 0; i < 361; i++) {
         * double x = sin(Math.toRadians(i)) * 0.7F, z = Math.cos(Math.toRadians(i)) *
         * 0.7F;
         * builder.vertex(matrix4f, (float) x, poses.getLast().value(), (float)
         * z).color(0.3f, 0.3f, 1f, 1f);
         * builder.vertex(matrix4f, (float) x, poses.getLast().value() + 0.01f, (float)
         * z).color(0.3f, 0.3f, 1f, 1f);
         * }
         * 
         * RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         * BufferRenderer.drawWithGlobalProgram(builder.end());
         * 
         * e.matrixStack().pop();
         * 
         * RenderSystem.disableBlend();
         * RenderSystem.enableDepthTest();
         * RenderSystem.enableCull();
         * GL11.glDisable(GL11.GL_LINE_SMOOTH);
         * }
         * }
         */
    }

    @EventTarget
    public void onRenderGui(EventRenderGui e) {
        LivingEntity target = (LivingEntity) killAura.getTarget();

        if (target == null)
            return;

        /*
         * switch (mode.getCurrentMode()) {
         * case "Nurik" -> {
         * PoseStack matrices = e.dc().getMatrices();
         * Vector2f pos =
         * WorldToScreenUtil.calculateScreenPos(target.getLerpedPos(e.rtc().getTickDelta
         * (true)).add(0, target.getBbHeight() / 2f, 0));
         * 
         * if (pos == null) {
         * break;
         * }
         * 
         * matrices.push();
         * 
         * for (int i = 0; i < 25; i++) {
         * double yaw = Mth.wrapDegrees(i * 36 + System.currentTimeMillis() / 10L);
         * yaw = Math.toRadians(yaw);
         * }
         * e.dc().drawTexture(RenderLayer::getGuiTextured, govno, (int) pos.x - 25,
         * (int) pos.y - 25, 0, 0, 50, 50, 1, 1, 1, 1, -1);
         * 
         * matrices.pop();
         * }
         * }
         */
    }

    private record Test(float value, long time) {
    }
}