package base.client.feature.impl.visual.hud.impl;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.visual.HUD;
import base.client.feature.impl.visual.hud.HudElement;
import base.client.helpers.impl.render.RectHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.Window;

import java.awt.Color;
import java.util.*;
import org.joml.Matrix3x2fStack;
import java.util.stream.Collectors;

public class ArrayListElement extends HudElement {

    private static final Minecraft mc = Minecraft.getInstance();

    // Light legacy look (still modern): thin stripe, subtle row bg, tight spacing
    private static final int STRIPE_WIDTH = 1;
    private static final int STRIPE_VERTICAL_PADDING = 1;
    private static final float STRIPE_RADIUS = 1f;
    private static final int PADDING_X = 2;
    private static final int PADDING_Y = 1;
    private static final int BG_ALPHA = 110;
    private static final float TEXT_SCALE = 1f;

    // Animation
    private static final float POP_SPEED = 0.2f;
    private static final float Y_SPEED = 0.2f;

    private final Map<String, Float> popProgress = new HashMap<>();
    private final Map<String, Float> yPositions = new HashMap<>();
    private final Map<String, Long> lastUpdate = new HashMap<>();
    private final Map<String, Boolean> activeState = new HashMap<>();

    public ArrayListElement() {
        super("ArrayList", "Module list display");
    }

    private static int desaturateTo(int argb, float saturation) {
        saturation = Math.max(0f, Math.min(1f, saturation));
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        int rgb = Color.HSBtoRGB(hsb[0], saturation, Math.min(1f, hsb[2] * 1.05f));
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    @Override
    public void draw(GuiGraphics dc, double mouseX, double mouseY) {
        Window window = mc.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        float y = 2;
        long time = System.currentTimeMillis();

        // Minecraft font only
        Font font = mc.font;

        // Get enabled modules sorted by width (longest first)
        List<Module> enabledModules = Client.instance.featureManager.getModuleList().stream()
                .filter(Module::getState)
                .filter(m -> !m.isHidden())
                .filter(m -> !m.getLabel().equalsIgnoreCase("HUD"))
                .sorted(Comparator.comparingInt((Module m) -> font.width(Component.literal(getDisplayText(m)))).reversed())
                .collect(Collectors.toList());

        Set<String> enabledNames = enabledModules.stream()
                .map(Module::getLabel)
                .collect(Collectors.toSet());

        // Update active states
        for (String name : new HashSet<>(activeState.keySet())) {
            if (!enabledNames.contains(name)) {
                activeState.put(name, false);
            }
        }

        for (Module m : enabledModules) {
            activeState.putIfAbsent(m.getLabel(), true);
            if (!activeState.get(m.getLabel())) {
                activeState.put(m.getLabel(), true);
            }
        }

        // Build render list
        List<String> renderList = new ArrayList<>();
        for (Module m : enabledModules) {
            renderList.add(m.getLabel());
        }

        // Add animating out modules
        for (Map.Entry<String, Boolean> entry : activeState.entrySet()) {
            if (!entry.getValue() && popProgress.containsKey(entry.getKey())) {
                float pop = popProgress.get(entry.getKey());
                if (pop > 0.01f) {
                    renderList.add(entry.getKey());
                }
            }
        }

        // Cleanup finished animations
        List<String> toRemove = new ArrayList<>();
        for (String name : popProgress.keySet()) {
            Boolean active = activeState.get(name);
            if (active != null && !active && popProgress.get(name) <= 0.01f) {
                toRemove.add(name);
            }
        }
        for (String name : toRemove) {
            popProgress.remove(name);
            yPositions.remove(name);
            lastUpdate.remove(name);
            activeState.remove(name);
        }

        // Render each module
        int index = 0;
        for (String label : renderList) {
            Module module = findModule(label, enabledModules);
            String displayText = module != null ? getDisplayText(module) : label;

            // Apply text scaling to width/height calculations
            int textWidth = (int) (font.width(Component.literal(displayText)) * TEXT_SCALE);
            int textHeight = (int) (font.lineHeight * TEXT_SCALE);

            boolean isActive = activeState.getOrDefault(label, false);

            // Delta time calculation
            long lastTime = lastUpdate.getOrDefault(label, time);
            float delta = Math.min((time - lastTime) / 16.67f, 3f);
            lastUpdate.put(label, time);

            // Pop animation (zoom)
            float pop = popProgress.getOrDefault(label, 0f);
            float targetPop = isActive ? 1f : 0f;
            pop += (targetPop - pop) * POP_SPEED * delta;
            if (isActive && pop > 0.99f) pop = 1f;
            if (!isActive && pop < 0.01f) pop = 0f;
            popProgress.put(label, pop);

            // Skip if fully hidden
            if (pop <= 0.01f && !isActive) {
                continue;
            }

// Y position animation
            float targetY;

// Only advance y for active modules
            if (isActive) {
                targetY = y; // normal position for active modules
            } else {
                // keep previous y for inactive (sliding-out)
                targetY = yPositions.getOrDefault(label, y);
            }

// Animate Y smoothly
            float animY = yPositions.getOrDefault(label, targetY);
            animY += (targetY - animY) * Y_SPEED * delta;
            if (Math.abs(targetY - animY) < 0.5f) animY = targetY;

// Store updated position
            yPositions.put(label, animY);

            // Calculate position with easing
            float eased = easeOutCubic(pop);
            int totalWidth = textWidth + PADDING_X * 2 + STRIPE_WIDTH;
            int xRight = screenWidth;
            int xLeft = screenWidth - totalWidth;
            int yTop = (int) animY;
            int yBottom = yTop + textHeight + PADDING_Y * 2 + 2;

            // Get accent color for this module
            int accentColor = getColorForIndex(index, time);

            // Row background (subtle)
            int bgAlpha = (int) (BG_ALPHA * eased);
            dc.pose().pushMatrix();
            dc.pose().translate(xRight, (yTop + yBottom) / 2f);
            dc.pose().scale(eased);
            dc.pose().translate(-xRight, -((yTop + yBottom) / 2f));

            dc.fill(xLeft, yTop, xRight, yBottom, new Color(0, 0, 0, bgAlpha).getRGB());

            // Thin stripe on the right (legacy vibe)
            int stripeTop = yTop + STRIPE_VERTICAL_PADDING;
            int stripeBottom = yBottom - STRIPE_VERTICAL_PADDING;
            int stripeHeight = stripeBottom - stripeTop;
            RectHelper.drawRoundedRect(dc, xRight - STRIPE_WIDTH, stripeTop, STRIPE_WIDTH, stripeHeight, STRIPE_RADIUS, accentColor);

            // Draw text with scaling
            int textX = xLeft + PADDING_X;
            int textY = yTop + PADDING_Y;

            // Get module name and suffix separately
            String moduleName = module != null ? module.getLabel() : label;
            String suffix = module != null ? module.getSuffix() : null;
            boolean hasSuffix = HUD.arrayListSuffix.isEnabled() && suffix != null && !suffix.isEmpty() && !suffix.equals(moduleName);

            // Apply text scaling using matrix transformation
            Matrix3x2fStack matrices = dc.pose();
            matrices.pushMatrix();
            matrices.translate(textX, textY);
            matrices.scale(TEXT_SCALE);

            // Text: flowing accent like stripe, but desaturated for legacy look
            dc.drawString(font, Component.literal(moduleName), 0, 0, desaturateTo(accentColor, 0.5f), true);

            // Draw suffix in gray if present
            if (hasSuffix) {
                int suffixX = font.width(Component.literal(moduleName + " "));
                int grayColor = 0xFF9A9A9A;
                dc.drawString(font, Component.literal(suffix), suffixX, 0, grayColor, true);
            }

            matrices.popMatrix();

            dc.pose().popMatrix();

            // Advance Y position only for active modules
            if (isActive) {
                y += textHeight + PADDING_Y * 2 + 2;
            }
            index++;
        }
    }

    private String getDisplayText(Module module) {
        if (!HUD.arrayListSuffix.isEnabled()) {
            return module.getLabel();
        }
        String suffix = module.getSuffix();
        // getSuffix() returns label when suffix is null, so check for that
        if (suffix != null && !suffix.isEmpty() && !suffix.equals(module.getLabel())) {
            return module.getLabel() + " " + suffix;
        }
        return module.getLabel();
    }

    private Module findModule(String label, List<Module> modules) {
        for (Module m : modules) {
            if (m.getLabel().equals(label)) {
                return m;
            }
        }
        return null;
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private int getColorForIndex(int index, long time) {
        // Fixed custom colors with flowing gradient
        int color1 = new Color(38, 191, 81).getRGB(); //
        int color2 = new Color(48, 126, 250).getRGB(); //
        // Flowing gradient with fixed speed (65ms per cycle)
        double t = (System.currentTimeMillis() / 700.0) % 1.0;
        float frac = (float) t;
        frac = (frac + index * 0.067f) % 1.0f;
        return interpolateColor(color1, color2, frac);
    }

    private int interpolateColor(int color1, int color2, float progress) {
        progress = Math.max(0f, Math.min(1f, progress));
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
}
