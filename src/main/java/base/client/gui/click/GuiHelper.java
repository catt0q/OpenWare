package base.client.gui.click;

import base.client.helpers.impl.render.PaletteHelper;
import base.client.helpers.utils.ColorUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
// InputUtilMixin removed - not available in 1.21.11
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class GuiHelper {
    // Dark Sprite theme (green/blue tint, but not pitch black)
    public static int bgColor = new Color(14, 18, 18, 235).getRGB();
    public static int bgColor2 = new Color(22, 28, 28, 235).getRGB();
    public static int bgColor3 = new Color(30, 38, 38, 235).getRGB(); // hover/active

    // Text colors
    public static int textPrimary = new Color(255, 255, 255, 255).getRGB();
    public static int textSecondary = new Color(180, 180, 190, 255).getRGB();
    public static int textMuted = new Color(120, 120, 130, 255).getRGB();

    // Spacing constants for consistent layout
    public static final int PADDING_SM = 4;
    public static final int PADDING_MD = 8;
    public static final int PADDING_LG = 12;
    public static final int BORDER_RADIUS = 4;

    // Get accent color from global HUD settings
    public static int getGlobalColor(int index) {
        // Sprite accent (static; no flowing)
        return new Color(56, 176, 168).getRGB();
    }

    // Convenience method for getting color without index (uses time-based
    // animation)
    public static int getGlobalColor() {
        return getGlobalColor(0);
    }

    // Legacy static color field - now dynamically returns global color
    public static int color = new Color(140, 120, 255).getRGB();
    public static int colorSecondary = new Color(100, 180, 255).getRGB();

    public static void rectFilled(GuiGraphics graphics, float x, float y, float width, float height, int color) {
        graphics.fill((int) x, (int) y, (int) (width + x), (int) (height + y), color);
    }

    public static void rectFilledRounded(GuiGraphics graphics, float x, float y, float width, float height,
            float radius, int color) {
        base.client.helpers.impl.render.RectHelper.drawRoundedRect(graphics, x, y, width, height, radius, color);
    }

    public static void rectFilledGradient(GuiGraphics graphics, float x, float y, float width, float height, int color1,
            int color2, int color3, int color4) {
        // graphics.fillGradient((int) x, (int) y, (int) (width + x), (int) (height +
        // y), color1, color2, color3, color4);

        // graphics.guiRenderState.submitGuiElement(new
        // FourColorGradientRenderState(RenderPipelines.GUI, TextureSetup.noTexture(),
        // graphics.pose(), x, y, width + x, height + y, 1, 1, 1, 1,
        // graphics.scissorStack));
        graphics.guiRenderState.submitGuiElement(new FourColorGradientRenderState(RenderPipelines.GUI,
                TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), (int) x, (int) y, (int) (width + x),
                (int) (height + y), color1, color2, color3, color4, graphics.scissorStack.peek()));

        // Matrix3x2f matrix = graphics.pose();
        // Tesselator tesselator = Tesselator.getInstance();
        // VertexConsumer vertexconsumer =
        // tesselator.begin(RenderPipelines.GUI.getVertexFormatMode(),
        // RenderPipelines.GUI.getVertexFormat());
        // vertexconsumer.addVertexWith2DPose(matrix, x, y, 0).setColor(color1);
        // vertexconsumer.addVertexWith2DPose(matrix, x, height + y,
        // 0).setColor(color2);
        // vertexconsumer.addVertexWith2DPose(matrix, width + x, height + y,
        // 0).setColor(color3);
        // vertexconsumer.addVertexWith2DPose(matrix, width + x, y, 0).setColor(color4);

    }

    public static boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static boolean isHovering(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static boolean isHovering(double x, double y, double width, double height, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static boolean isHovering(double x, double y, double width, double height, double mouseX, double mouseY,
            double mouseWidth, double mouseHeight) {
        return mouseX + mouseWidth >= x && mouseY + mouseHeight >= y && mouseX < x + width && mouseY < y + height;
    }

    public static String getKeyName(int key) {
        return getBind(key).toUpperCase()
                .replace("KEY.KEYBOARD", "").replace(".", " ");
        // return getKeyName(key, 0);
    }

    public static String getBind(int key) {
        if (key <= 0)
            return " None";
        String name = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, 0);
        return capitalise(name != null ? name : "key." + key);
    }

    public static String capitalise(String str) {
        if (str.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(str.charAt(0)) + (str.length() != 1 ? str.substring(1).toLowerCase() : "");
    }

    public static String getKeyName(int keycode, int scancode) {
        return switch (keycode) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCONTROL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCONTROL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";

            // Mouse Buttons
            case 1000 -> "MOUSE0";
            case 1001 -> "MOUSE1";
            case 1002 -> "MOUSE2";
            case 1003 -> "MOUSE3";
            case 1004 -> "MOUSE4";
            case 1005 -> "MOUSE5";
            case 1006 -> "MOUSE6";
            case 1007 -> "MOUSE7";
            case 1008 -> "MOUSE8";
            case 1009 -> "MOUSE9";
            default -> GLFW.glfwGetKeyName(keycode, scancode);
        };
    }

    public static int getKeyCode(String key) {
        if (key.equalsIgnoreCase("none")) {
            return -1;
        }
        // Keyboard Keys
        for (int i = 32; i < 97; i++) {
            if (key.equalsIgnoreCase(getKeyName(i,
                    GLFW.glfwGetKeyScancode(i)))) {
                return i;
            }
        }
        for (int i = 256; i < 349; i++) {
            if (key.equalsIgnoreCase(getKeyName(i,
                    GLFW.glfwGetKeyScancode(i)))) {
                return i;
            }
        }
        // Mouse Buttons
        for (int i = 1000; i < 1010; i++) {
            if (key.equalsIgnoreCase(getKeyName(i,
                    GLFW.glfwGetKeyScancode(i)))) {
                return i;
            }
        }
        return GLFW.GLFW_KEY_UNKNOWN;
    }

    public static int rgba(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int rgba(double r, double g, double b, double a) {
        return rgba((int) r, (int) g, (int) b, (int) a);
    }

    public static int getRed(int color) {
        return color >> 16 & 255;
    }

    public static int getGreen(int color) {
        return color >> 8 & 255;
    }

    public static int getBlue(int color) {
        return color & 255;
    }

    public static int getAlpha(int color) {
        return color >> 24 & 255;
    }

    public static int applyOpacity(int color, double alpha) {
        return rgba(getRed(color), getGreen(color), getBlue(color), (int) (alpha * getAlpha(color) / 255f));
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Mth.clamp(amount, 0, 1);
        return rgba(interpolateInt(getRed(color1), getRed(color2), amount),
                interpolateInt(getGreen(color1), getGreen(color2), amount),
                interpolateInt(getBlue(color1), getBlue(color2), amount),
                interpolateInt(getAlpha(color1), getAlpha(color2), amount));
    }

    public static float lerp(float a, float b, float f) {
        f = Mth.clamp(f, 0, 1);
        return a + f * (b - a);
    }

    public static float lerpNoClamp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double lerp(double a, double b, double f) {
        f = Mth.clamp(f, 0, 1);
        return a + f * (b - a);
    }
}
