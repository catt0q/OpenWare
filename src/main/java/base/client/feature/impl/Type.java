package base.client.feature.impl;

import java.awt.*;

public enum Type {

    Combat(new Color(220, 20, 60).getRGB(), new Color(137, 3, 42).getRGB(), "Combat"),
    Movement(new Color(123, 104, 238).getRGB(), new Color(73, 63, 151).getRGB(), "Movement"),
    Visuals(new Color(0, 206, 209).getRGB(), new Color(2, 121, 123).getRGB(), "Visuals"),
    Player(new Color(244, 164, 96).getRGB(), new Color(132, 68, 9).getRGB(), "Player"),
    Misc(new Color(60, 179, 113).getRGB(), new Color(28, 88, 57).getRGB(), "Misc"),
    //  Ghost(new Color(90, 10, 190).getRGB(), new Color(90, 10, 120).getRGB(), "Ghost"),
    Client(new Color(90, 10, 190).getRGB(), new Color(90, 10, 120).getRGB(), "Client"),
    Hud(new Color(186, 85, 211).getRGB(), new Color(91, 41, 102).getRGB(), "HUD"),
    Exploit(new Color(56, 56, 56).getRGB(), new Color(28, 28, 28).getRGB(), "Exploit"),
    Info(new Color(0, 191, 255).getRGB(), new Color(0, 191, 130).getRGB(), "Info"),
    Minigames(new Color(255, 191, 255).getRGB(), new Color(255, 191, 130).getRGB(), "Minigames");


    private final int color;
    private final int color2;

    public String name;

    Type(int color, int color2, String name) {
        this.color = color;
        this.color2 = color2;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.color;
    }

    public int getColor2() {
        return this.color2;
    }
}