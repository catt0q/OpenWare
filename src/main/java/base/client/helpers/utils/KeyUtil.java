package base.client.helpers.utils;

import com.mojang.blaze3d.platform.InputConstants;

public class KeyUtil {

    public static String KeyToString(int key, int scancode) {
        // use GLFW key name directly since InputConstants.getKey(int,int) removed
        String glfwName = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, scancode);
        if (glfwName != null)
            return glfwName;
        return "key." + key;
    }

    public static String KeyToString(int key) {
        String glfwName = org.lwjgl.glfw.GLFW.glfwGetKeyName(key, 0);
        if (glfwName != null)
            return glfwName;
        return "key." + key;
    }

    public static InputConstants.Key StringToKey(String key) {
        if (key.startsWith("key.keyboard.")) {
            return InputConstants.getKey(key);
        }
        return InputConstants.getKey(("key.keyboard." + key.toLowerCase()));
    }

}
