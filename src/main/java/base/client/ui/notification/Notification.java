package base.client.ui.notification;


import base.client.helpers.Helper;
import base.client.helpers.impl.render.FontHelper;
import base.client.helpers.impl.render.ScreenHelper;
import base.client.helpers.utils.TimerHelper;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class Notification implements Helper {
    private final ScreenHelper screenHelper;
    private final Font font;
     private final String title;
    private final String content;
    private final int time;
    private final NotificationType type;
    private final TimerHelper timer;

    public Notification(String title, String content, NotificationType type, int second, Font font) {
        this.title = title;
        this.content = content;
        this.time = second;
        this.type = type;
        this.timer = new TimerHelper();
        this.font = font;
        Window sr = Minecraft.getInstance().getWindow();
         this.screenHelper = new ScreenHelper((sr.getWidth() - getWidth() + getWidth()), (sr.getHeight() - 60));
    }

    public int getWidth() {
        return Math.max(100, Math.max(FontHelper.getStringWidth(this.title),
                FontHelper.getStringWidth(this.content)) + 40);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return this.content;
    }

    public int getTime() {
        return this.time;
    }

    public NotificationType getType() {
        return this.type;
    }

    public TimerHelper getTimer() {
        return this.timer;
    }

   public ScreenHelper getTranslate() {   return screenHelper;   }
}
