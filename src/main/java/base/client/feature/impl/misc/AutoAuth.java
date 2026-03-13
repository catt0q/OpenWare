package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.feature.settings.impl.StringSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.inventory.ClickType;

public class AutoAuth extends Module {

    public static ModeSetting RegisterMode = new ModeSetting("Register Mode", "register", () -> true, "register",
            "reg");
    public static NumberSetting PasswordCount = new NumberSetting("Password Count", 1, 1, 4, 1, () -> true);
    public static StringSetting password = new StringSetting("Password", "regpass123", () -> true);

    public AutoAuth() {
        super("AutoAuth", "Автоматически регестрируется и логинится на серверах", Type.Player);
        this.addSettings(RegisterMode, password, PasswordCount);
    }

    @EventTarget
    public void onSendPacket(EventReceivePacketPre event) {
        Packet<?> packetp = event.getPacket();
        if (packetp instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket sPacketChat = (ClientboundSystemChatPacket) event.getPacket();
            String rawMessage = sPacketChat.content().getString();

            if (rawMessage.contains("/reg") || rawMessage.contains("/register")
                    || rawMessage.contains("Зарегистрируйтесь")) {
                String pass = "";
                for (int i = 0; i < PasswordCount.getValue(); i++) {
                    pass += " " + password.getCurrentText();
                }
                pc.sendSilentChat("/register" + pass);
                ChatHelper.addTranslatedMessage("password.display", ChatFormatting.RED + password.getCurrentText());
                NotificationManager.publicity("AutoAuth", "You are successfully registered!", 4,
                        NotificationType.SUCCESS);
            } else if (rawMessage.contains("Авторизуйтесь") || rawMessage.contains("/l")) {
                String pass = "";
                for (int i = 0; i < PasswordCount.getValue(); i++) {
                    pass += " " + password.getCurrentText();
                }
                pc.sendSilentChat("/login" + pass);
                NotificationManager.publicity("AutoAuth", "You are successfully login!", 4, NotificationType.SUCCESS);
            }

        }

    }

}
