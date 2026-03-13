package base.client.cmd.impl;

import base.client.cmd.CommandAbstract;
import base.client.event.EventManager;
import base.client.event.EventTarget;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.helpers.Helper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.utils.WorldToScreenUtil;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GpsCommand extends CommandAbstract {

    private final List<GPSPoint> points = new ArrayList<>();

    public GpsCommand() {
        super("Gps", "cmd.gps.desc", ".gps", "gps");
    }

    @Override
    public void execute(String... args) {
        switch (args.length) {
            case 6 -> {
                if (args[1].equalsIgnoreCase("add")) {
                    GPSPoint point = new GPSPoint(args[2], new Vec3(Double.parseDouble(args[3]),
                            Double.parseDouble(args[4]), Double.parseDouble(args[5])), true);
                    points.add(point);
                    ChatHelper.addTranslatedMessage("gps.point_added", point.getName());
                }
            }
            case 5 -> {
                if (args[1].equalsIgnoreCase("add")) {
                    GPSPoint point = new GPSPoint(args[2],
                            new Vec3(Double.parseDouble(args[3]), 1488, Double.parseDouble(args[4])), false);
                    points.add(point);
                    ChatHelper.addTranslatedMessage("gps.point_added", point.getName());
                }
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase("remove")) {
                    points.removeIf(point -> {
                        if (point.getName().equalsIgnoreCase(args[2])) {
                            point.onDelete();
                            ChatHelper.addTranslatedMessage("gps.point_removed", point.getName());
                            return true;
                        }
                        return false;
                    });
                }
            }
            case 2 -> {
                if (args[1].equalsIgnoreCase("clear")) {
                    points.removeIf(point -> {
                        point.onDelete();
                        return true;
                    });
                    ChatHelper.addTranslatedMessage("gps.all_cleared");
                }
            }
        }
    }

    public static class GPSPoint implements Helper {
        @Getter
        private final String name;
        private final Vec3 pos;
        private final boolean hasY;

        public GPSPoint(String name, Vec3 pos, boolean hasY) {
            this.name = name;
            this.pos = pos;
            this.hasY = hasY;
            EventManager.register(this);
        }

        public void onDelete() {
            EventManager.unregister(this);
        }

        private Vector2f getPos() {
            Vector2f pos = WorldToScreenUtil.calculateScreenPos(this.pos);

            if (pos == null) {
                return null;
            }

            return new Vector2f(pos.x, hasY ? pos.y : mc.getWindow().getHeight() / 2f);
        }

        @EventTarget
        public void onRenderGui(EventRenderGui e) {
            Vector2f pos = this.getPos();
            GuiGraphics dc = e.dc();

            if (pos == null) {
                return;
            }

            double xdiff = mc.player.getX() - this.pos.x;
            double zdiff = mc.player.getZ() - this.pos.z;
            double distance = hasY ? Math.sqrt(mc.player.distanceToSqr(this.pos)) : Math.hypot(xdiff, zdiff);
            String distanceString = (int) Math.round(distance) + "m";
            int sizeX = Math.max(FontHelper.getStringWidth(distanceString), FontHelper.getStringWidth(name));
            int sizeY = 20;

            int halfSizeX = sizeX / 2;

            dc.fill(
                    Math.round(pos.x - halfSizeX - 2),
                    Math.round(pos.y),
                    Math.round(pos.x + halfSizeX + 2),
                    Math.round(pos.y + sizeY + 4),
                    Color.black.getRGB());

            FontHelper.drawString(dc, name, Math.round(pos.x), Math.round(pos.y + 2), -1, false);
            FontHelper.drawString(dc, distanceString, Math.round(pos.x), Math.round(pos.y + 2 + 12), -1, false);
        }

    }
}
