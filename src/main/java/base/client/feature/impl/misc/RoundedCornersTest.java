package base.client.feature.impl.misc;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.gui.click.rounded.RoundedTestScreen;

public class RoundedCornersTest extends Module {

    public RoundedCornersTest() {
        super("RoundedCornersTest", "Opens a blank window with rounded corners and blur", Type.Misc);
    }

    @Override
    public void onEnable() {
        mc.setScreen(new RoundedTestScreen());
        this.setState(false);
        super.onEnable();
    }
}
