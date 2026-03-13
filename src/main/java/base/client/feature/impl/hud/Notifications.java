package base.client.feature.impl.hud;

import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.RectHelper;
import base.client.helpers.impl.render.ScreenHelper;
import base.client.ui.notification.Notification;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Notifications extends Module {
    public static BooleanSetting state;

    NumberSetting Size = new NumberSetting("Size", 2F, 0.5F, 10F, 0.25F, () -> true);
    NumberSetting MinGap = new NumberSetting("MinGap", 200F, 0F, 1000F, 1F, () -> true);
    NumberSetting Gap = new NumberSetting("Gap", 20F, 0F, 1000F, 1F, () -> true);
    NumberSetting FloatOffset = new NumberSetting("Float Offset", 10F, 0F, 50F, 1F, () -> true);

    // legacy-modern palette (slightly tinted)
    private static final Color BG_COLOR = new Color(10, 14, 14, 210);
    private static final Color SUCCESS_COLOR = new Color(72, 207, 133);
    private static final Color ERROR_COLOR = new Color(239, 83, 80);
    private static final Color INFO_COLOR = new Color(100, 181, 246);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);

    // Track animated Y positions for each notification
    private final Map<Notification, Float> animatedYPositions = new HashMap<>();

    public Notifications() {
        super("Notifications", "Показывает необходимую информацию о модулях", Type.Hud);
        state = new BooleanSetting("Module State", true, () -> true);
        addSettings(state, Size, MinGap, Gap, FloatOffset);
    }

    private Color getAccentColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> SUCCESS_COLOR;
            case ERROR -> ERROR_COLOR;
            case WARNING -> WARNING_COLOR;
            default -> INFO_COLOR;
        };
    }

    private String getIcon(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "✔";
            case ERROR -> "❗";
            case WARNING -> "⚠";
            default -> "ℹ";
        };
    }
    private String getFontIcon(NotificationType type) {
        return switch (type) {
            case SUCCESS -> "a";
            case ERROR -> "X";
            case WARNING -> "!";
            default -> "I";
        };
    }

    @EventTarget
    private void onRenderWorld(EventRenderGui e) {
        GuiGraphics context = e.dc();

        // Clean up animated positions for removed notifications
        animatedYPositions.keySet().removeIf(n -> !NotificationManager.notifications.contains(n));

        if (!NotificationManager.notifications.isEmpty()) {
            Window window = mc.getWindow();
            int srScaledHeight = window.getGuiScaledHeight();
            double sc = Size.getValue() / window.getGuiScale();
            float offset = FloatOffset.getValue();
            int scaledWidth = (int) (window.getGuiScaledWidth() - offset * sc);
            int baseY = (int) (srScaledHeight - 50 * sc - offset * sc);

            // Calculate target Y for each notification based on its index
            int notifIndex = 0;
            for (Notification notification : NotificationManager.notifications) {
                ScreenHelper screenHelper = notification.getTranslate();
                int textWidth = FontHelper.getStringWidth(notification.getContent());
                float gapp = Math.min(MinGap.getValue() - (notification.getWidth() + textWidth) / 2f, Gap.getValue());
                if (gapp < 0)
                    gapp = 0;

                int width = (int) (((notification.getWidth() + textWidth) / 2 + gapp + 10) * sc);
                int height = (int) (32 * sc);

                // Calculate target Y position based on notification index
                float targetY = baseY - (notifIndex * (int) (38 * sc));

                // Initialize or animate Y position
                if (!animatedYPositions.containsKey(notification)) {
                    animatedYPositions.put(notification, targetY);
                } else {
                    float currentY = animatedYPositions.get(notification);
                    // Smooth lerp towards target Y
                    float newY = currentY + (targetY - currentY) * 0.15f;
                    animatedYPositions.put(notification, newY);
                }

                float animatedY = animatedYPositions.get(notification);

                 // animation (pop + fade)
                 float progress = 1.0f;
                 long elapsed = notification.getTimer().getTime();
                 int fadeInTime = 250;
                 int fadeOutTime = 120;
                 int totalTime = notification.getTime();

                 if (elapsed < fadeInTime) {
                     progress = elapsed / (float) fadeInTime;
                 } else if (elapsed > totalTime - fadeOutTime) {
                     progress = (totalTime - elapsed) / (float) fadeOutTime;
                 }
                 progress = Math.max(0, Math.min(1, progress));

                 // remove after it fully popped out
                 if (elapsed > totalTime + 250) {
                     NotificationManager.notifications.remove(notification);
                 }

                // Keep vertical stacking slide (animatedY), but use pop only for appear/disappear
                float translateX = scaledWidth - width;
                float translateY = animatedY;

                Matrix3x2fStack matrices = context.pose();
                matrices.pushMatrix();
                matrices.scale((float) sc);

                float rx = translateX / (float) sc;
                float ry = translateY / (float) sc;
                float rw = width / (float) sc;
                float rh = height / (float) sc;

                // Pop from right edge, centered vertically (while Y keeps sliding)
                matrices.pushMatrix();
                matrices.translate(rx + rw, ry + rh / 2f);
                matrices.scale(progress);
                matrices.translate(-(rx + rw), -(ry + rh / 2f));

                Color accent = getAccentColor(notification.getType());
                int alpha = (int) (240 * progress);

                // background + subtle border
                RectHelper.drawRoundedRect(context, rx, ry, rw, rh, 4,
                        new Color(BG_COLOR.getRed(), BG_COLOR.getGreen(), BG_COLOR.getBlue(), alpha).getRGB());
                RectHelper.drawRoundedOutline(context, rx, ry, rw, rh, 4, 1.0f,
                        new Color(255, 255, 255, (int) (35 * progress)).getRGB());

                // thin accent stripe
                RectHelper.drawRoundedRect(context, rx, ry + 1, 2, rh - 2, 1.0f,
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (220 * progress)).getRGB());

// set X offset depending on type
                float finalx;

                switch (notification.getType()) {
                    case SUCCESS -> finalx = rx + 11f;       // normal
                    case WARNING -> finalx = rx + 10f;       // normal
                    case ERROR   -> finalx = rx + 13f;     // slightly adjusted / narrower
                    default      -> finalx = rx + 11f;       // fallback
                }



                // title with text scaling for long module names i shat in zqcox's fridge
                int titleAlpha = (int) (255 * progress);
                String title = notification.getTitle();
                int titleWidth = FontHelper.getStringWidth(title);
                float maxTitleWidth = rw - 32; // available space for title (accounting for icon and padding)
                float titleScale = 1.0f;

                if (titleWidth > maxTitleWidth && maxTitleWidth > 0) {
                    titleScale = maxTitleWidth / titleWidth;
                    titleScale = Math.max(titleScale, 0.5f); // minimum scale of 0.5
                }

                if (titleScale < 1.0f) {
                    matrices.pushMatrix();
                    matrices.translate(rx + 26, ry + 5);
                    matrices.scale(titleScale);
                    FontHelper.drawString(context, title,
                            0, 0,
                            new Color(255, 255, 255, titleAlpha).getRGB(), false);
                    matrices.popMatrix();
                } else {
                    FontHelper.drawString(context, title,
                            (int) (rx + 7), (int) (ry + 5),
                            new Color(255, 255, 255, titleAlpha).getRGB(), false);
                }

                // content
                int contentAlpha = (int) (180 * progress);
                FontHelper.drawString(context, notification.getContent(),
                        (int) (rx + 6), (int) (ry + 17),
                        new Color(200, 200, 205, contentAlpha).getRGB(), false);

                 // Legacy progress bar (thin, minimal)
                 float barY = ry + rh - 2;
                 float barWidth = rw - 6;
                 float timeProgress = 1.0f - (elapsed / (float) totalTime);
                 timeProgress = Math.max(0, Math.min(1, timeProgress));

                 context.fill((int) (rx + 3), (int) barY, (int) (rx + 3 + barWidth), (int) (barY + 1),
                         new Color(0, 0, 0, (int) (120 * progress)).getRGB());
                 context.fill((int) (rx + 3), (int) barY, (int) (rx + 3 + barWidth * timeProgress), (int) (barY + 1),
                         new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int) (180 * progress)).getRGB());

                matrices.popMatrix();

                matrices.popMatrix();

                notifIndex++;
            }
        }
    }
}
