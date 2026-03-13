package base.client.feature.impl.visual;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventAttackEntity;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import base.client.helpers.impl.render.RectHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

import java.awt.Color;

public class TargetHUD extends Module {

    // Settings
    private final BooleanSetting requireKillAura = new BooleanSetting("Require KillAura", true, () -> true);
    private final ModeSetting healthMethod = new ModeSetting("Health Method", "Default", () -> true, "Default", "Scoreboard");
    private final ModeSetting healthBarMode = new ModeSetting("Health Bar", "Global", () -> true, "Global", "Classic");
    private final BooleanSetting showDistance = new BooleanSetting("Show Distance", true, () -> true);
    private final BooleanSetting showHead = new BooleanSetting("Show Head", true, () -> true);
    private final NumberSetting animSpeed = new NumberSetting("Animation Speed", 0.15f, 0.05f, 0.5f, 0.01f, () -> true);
    private final NumberSetting xPos = new NumberSetting("X Position", 10, 0, 500, 1, () -> true);
    private final NumberSetting yPos = new NumberSetting("Y Position", 100, 0, 500, 1, () -> true);

    // State
    private LivingEntity target = null;
    private long lastHitTime = 0;
    private float scale = 0f;
    private float alphaMul = 0f;
    private float displayedHealth = 0f;
    private float healthBarProgress = 0f;
    private static final long FADE_OUT_DELAY = 3000;

    // Dimensions (more classic)
    private static final int WIDTH = 150;
    private static final int HEIGHT = 46;
    private static final int HEAD_SIZE = 24;
    private static final int PADDING = 6;
    private static final int BAR_HEIGHT = 8;
    private static final float CORNER_RADIUS = 5;

    public TargetHUD() {
        super("TargetHUD", "Displays target health information", Type.Visuals);
        addSettings(requireKillAura, healthMethod, healthBarMode, showDistance, showHead, animSpeed, xPos, yPos);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        target = null;
        scale = 0f;
        alphaMul = 0f;
        displayedHealth = 0f;
        healthBarProgress = 0f;
        lastHitTime = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
        scale = 0f;
        alphaMul = 0f;
    }

    @EventTarget
    public void onAttack(EventAttackEntity event) {
        if (event.getTarget() instanceof LivingEntity livingTarget) {
            if (requireKillAura.isEnabled()) {
                KillAuraNew killAura = (KillAuraNew) Client.instance.featureManager.getModuleByClass(KillAuraNew.class);
                if (killAura == null || !killAura.getState()) {
                    return;
                }
            }
            target = livingTarget;
            lastHitTime = System.currentTimeMillis();
            if (displayedHealth == 0f) {
                displayedHealth = getTargetHealth();
            }
        }
    }

    @EventTarget
    public void onRenderGui(EventRenderGui event) {
        if (mc.player == null || mc.level == null) return;

        long timeSinceHit = System.currentTimeMillis() - lastHitTime;
        boolean shouldShow = target != null && target.isAlive() && timeSinceHit < FADE_OUT_DELAY;

        // Zoom in/out animation
        float targetScale = shouldShow ? 1f : 0f;
        float speed = animSpeed.getValue();
        if (scale < targetScale) {
            scale = Math.min(scale + speed, targetScale);
        } else if (scale > targetScale) {
            scale = Math.max(scale - speed, targetScale);
        }

        // Fade in/out synced with zoom
        float targetAlpha = shouldShow ? 1f : 0f;
        if (alphaMul < targetAlpha) {
            alphaMul = Math.min(alphaMul + speed, targetAlpha);
        } else if (alphaMul > targetAlpha) {
            alphaMul = Math.max(alphaMul - speed, targetAlpha);
        }

        // Don't render if fully zoomed out
        if (scale <= 0.01f && alphaMul <= 0.01f) {
            if (!shouldShow) {
                target = null;
            }
            return;
        }

        // Update health animation
        if (target != null && target.isAlive()) {
            float actualHealth = getTargetHealth();
            float healthDiff = actualHealth - displayedHealth;
            displayedHealth += healthDiff * 0.12f;

            // Animate health bar progress
            float targetProgress = Math.clamp(displayedHealth / target.getMaxHealth(), 0f, 1f);
            float progressDiff = targetProgress - healthBarProgress;
            healthBarProgress += progressDiff * 0.1f;
        }

        render(event.dc());
    }

    private float getTargetHealth() {
        if (target == null) return 0f;

        if (healthMethod.getCurrentMode().equals("Scoreboard") && target instanceof Player player) {
            Scoreboard scoreboard = mc.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);

            if (objective != null) {
                ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(player, objective);
                if (score != null) {
                    return score.value();
                }
            }
        }

        return target.getHealth();
    }


    private void render(GuiGraphics dc) {
        if (target == null) return;

        int baseX = (int) xPos.getValue();
        int baseY = (int) yPos.getValue();

        // Apply scale transformation using matrix stack
        dc.pose().pushMatrix();
        float centerX = baseX + WIDTH / 2f;
        float centerY = baseY + HEIGHT / 2f;
        dc.pose().translate(centerX, centerY);
        dc.pose().scale(scale);
        dc.pose().translate(-centerX, -centerY);

        int alpha = (int) (alphaMul * 200);
        int textAlpha = (int) (alphaMul * 255);

        // Classic panel + subtle border
        RectHelper.drawRoundedRect(dc, baseX, baseY, WIDTH, HEIGHT, CORNER_RADIUS,
                new Color(0, 0, 0, alpha).getRGB());
        RectHelper.drawRoundedOutline(dc, baseX, baseY, WIDTH, HEIGHT, CORNER_RADIUS, 1.0f,
                new Color(255, 255, 255, (int) (alphaMul * 50)).getRGB());

        int contentX = baseX + PADDING;
        int contentY = baseY + PADDING;

        // Player head
        if (showHead.isEnabled() && target instanceof Player player) {
            int headSize = HEAD_SIZE;
            renderPlayerHead(dc, player, contentX, contentY, headSize);
            contentX += HEAD_SIZE + 4;
        }

        // Name
        String name = target.getName().getString();
        dc.drawString(Minecraft.getInstance().font, Component.literal(name), contentX, contentY,
                new Color(255, 255, 255, textAlpha).getRGB(), true);

        // Health text
        float maxHealth = target.getMaxHealth();
        String healthText = String.format("%.1f / %.0f", displayedHealth, maxHealth);
        dc.drawString(Minecraft.getInstance().font, Component.literal(healthText), contentX, contentY + 12,
                new Color(200, 200, 200, textAlpha).getRGB(), true);

        // Distance
        if (showDistance.isEnabled()) {
            double distance = mc.player.distanceTo(target);
            String distText = String.format("%.1fm", distance);
            int distWidth = Minecraft.getInstance().font.width(Component.literal(distText));
            dc.drawString(Minecraft.getInstance().font, Component.literal(distText),
                    baseX + WIDTH - PADDING - distWidth, contentY,
                    new Color(180, 180, 180, textAlpha).getRGB(), true);
        }

        // Health bar
        int barX = baseX + PADDING;
        int barY = baseY + HEIGHT - PADDING - BAR_HEIGHT;
        int barWidth = WIDTH - PADDING * 2;

        // Bar background
        RectHelper.drawRoundedRect(dc, barX, barY, barWidth, BAR_HEIGHT, 3, new Color(25, 25, 25, alpha).getRGB());

        // Filled portion
        int filledWidth = (int) (barWidth * healthBarProgress);
        if (filledWidth > 0) {
            if (healthBarMode.getCurrentMode().equals("Classic")) {
                renderClassicGradientBar(dc, barX, barY, filledWidth, BAR_HEIGHT, textAlpha);
            } else {
                renderGlobalColorBar(dc, barX, barY, filledWidth, BAR_HEIGHT, textAlpha);
            }
        }

        dc.pose().popMatrix();
    }

    private void renderPlayerHead(GuiGraphics dc, Player player, int x, int y, int size) {
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(player.getUUID());
        if (playerInfo == null) return;

        PlayerSkin skin = playerInfo.getSkin();
        if (skin == null) return;

        ClientAsset.Texture bodyTexture = skin.body();
        if (bodyTexture == null) return;

        Identifier texture = bodyTexture.texturePath();
        if (texture == null) return;

        // Skin texture is 64x64, head is at (8,8) with size 8x8
        // Hat overlay is at (40,8) with size 8x8
        int texWidth = 64;
        int texHeight = 64;

        // Draw main head (8,8 to 16,16 in texture)
        dc.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 8, 8, size, size, 8, 8, texWidth, texHeight);

        // Draw hat overlay (40,8 to 48,16 in texture)
        dc.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 40, 8, size, size, 8, 8, texWidth, texHeight);
    }

    private void renderClassicGradientBar(GuiGraphics dc, int x, int y, int width, int height, int alpha) {
        // Red -> Yellow -> Green gradient based on position
        for (int i = 0; i < width; i++) {
            float percent = (float) i / (float) Math.max(width, 1);
            Color color = getClassicGradientColor(percent, alpha);
            dc.fill(x + i, y, x + i + 1, y + height, color.getRGB());
        }
    }

    private Color getClassicGradientColor(float percent, int alpha) {
        int r, g, b;
        if (percent < 0.5f) {
            // Red to Yellow
            float t = percent * 2f;
            r = 220;
            g = (int) (220 * t);
            b = 0;
        } else {
            // Yellow to Green
            float t = (percent - 0.5f) * 2f;
            r = (int) (220 * (1 - t));
            g = 220;
            b = 0;
        }
        return new Color(r, g, b, alpha);
    }

    private void renderGlobalColorBar(GuiGraphics dc, int x, int y, int width, int height, int alpha) {
        int color1 = new Color(38, 191, 81).getRGB();
        int color2 = new Color(48, 126, 250).getRGB();

        for (int i = 0; i < width; i++) {
            float progress = (float) i / Math.max(width - 1, 1);
            int color = interpolateColor(color1, color2, progress);
            dc.fill(x + i, y, x + i + 1, y + height, withAlpha(color, alpha));
        }
    }

    private int interpolateColor(int color1, int color2, float progress) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    // Removed font/color helpers

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
