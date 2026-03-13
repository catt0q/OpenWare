package base.client.feature.impl.visual.theme;

import java.awt.*;

public enum Theme {
    WHITE(Color.WHITE),
    BLACK(Color.BLACK);

    private final Color color;

    Theme(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
