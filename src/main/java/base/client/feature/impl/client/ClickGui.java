package base.client.feature.impl.client;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Module {

    public ClickGui() {
        super("ClickGui", "!ЛУЧШЕ КЛИК ГУИ, КОТОРОЕ КОГДА ЛИБО СДЕЛАНО И КОТОРОЕ СДЕЛАЛ САМЫЙ ЛУЧШИЙ ЧЕЛОВЕК ВО ВСЕЛЕННОЙ И ВООБЩЕ НЕ НАДО МНЕ ТУТ!", Type.Client);;
        setBind(344);//GLFW.GLFW_KEY_RIGHT_SHIFT
    }


    @Override
    public void onEnable() {
        super.onEnable();
        mc.setScreen(Client.instance.clickGuiScreen);
        toggle();
    }
}
