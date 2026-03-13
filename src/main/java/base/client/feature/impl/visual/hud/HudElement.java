package base.client.feature.impl.visual.hud;

import base.client.feature.impl.visual.hud.impl.ArrayListElement;
import base.client.feature.impl.visual.hud.impl.BlockCounterElement;
import base.client.feature.impl.visual.theme.Theme;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class HudElement {

    // All HUD elements, auto-initialized
    private static final List<HudElement> hudElements = new ArrayList<>();

    public static List<HudElement> getElements() {
        return hudElements;
    }

    static {
        // Automatically add your HUD elements here
        hudElements.add(new ArrayListElement());
        hudElements.add(new BlockCounterElement());
    }

    // Position and size
    protected final Vector2f position = new Vector2f(200f, 200f);
    protected final Vector2f size = new Vector2f(0f, 0f);

    // Name and description
    protected final String name;
    protected final String desc;

    @Setter
    protected Theme theme;
    @Setter
    protected Theme colorTheme;

    public HudElement(String name, String desc) {
        this.name = name;
        this.desc = desc;
        this.theme = Theme.WHITE;
    }

    public HudElement(String name, String desc, Theme theme) {
        this.name = name;
        this.desc = desc;
        this.theme = theme;
    }

    /**
     * Draw method called every frame
     */
    public void draw(GuiGraphics dc, double mouseX, double mouseY) {
        int margin = 3;
        dc.fill((int) position.x - margin, (int) position.y - margin,
                (int) (position.x + size.x) + margin,
                (int) (position.y + size.y) + margin,
                theme.getColor().getRGB());
    }

    /**
     * Move HUD element
     */
    public void move(float dx, float dy) {
        position.set(position.x + dx, position.y + dy);
    }
}
