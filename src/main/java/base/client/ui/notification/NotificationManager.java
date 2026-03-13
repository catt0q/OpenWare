package base.client.ui.notification;


import base.client.helpers.Helper;
import net.minecraft.client.gui.Font;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements Helper {

    public static final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void publicity(String title, String content, int second, NotificationType type) {
        Font fontRenderer = mc.font;
     notifications.add(new Notification(title, content, type, second * 1000, fontRenderer));
    }


}
