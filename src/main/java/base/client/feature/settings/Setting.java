package base.client.feature.settings;

import java.util.function.Supplier;

public class Setting extends Configurable {
    protected String name;
    protected Supplier<Boolean> visible;
    protected String desc;
    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
