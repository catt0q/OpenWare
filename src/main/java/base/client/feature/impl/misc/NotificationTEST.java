package base.client.feature.impl.misc;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;

public class NotificationTEST extends Module {
    public NotificationTEST() {
        super("NotifTest", "Govno", Type.Misc);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // send notifications
        NotificationManager.publicity("green", "green", 10,
                NotificationType.SUCCESS);
        NotificationManager.publicity("red", "red", 10,
                NotificationType.ERROR);
        NotificationManager.publicity("idk", "idk", 10,
                NotificationType.WARNING);

        // immediately disable itself
        this.toggle();  // or setEnabled(false) depending on your Module class
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
