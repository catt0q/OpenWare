package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingCommand extends CommandAbstract {
    Minecraft mc = Minecraft.getInstance();

    public PingCommand() {
        super("ping", "cmd.ping.desc", "§6.ping" + ChatFormatting.LIGHT_PURPLE + " §3<nickname> | §6.ping", "ping");
    }

    @Override
    public void execute(String... arguments) {
        try {
            if (arguments.length == 1) {
                ChatHelper.addChatMessage(ChatFormatting.GREEN + "Your ping is: " + ChatFormatting.LIGHT_PURPLE
                        + mc.player.connection.getPlayerInfo(mc.player.getUUID()).getLatency());
            } else if (arguments.length == 2) {

                PlayerInfo pi = null;
                for (PlayerInfo playerInfo : mc.player.connection.getOnlinePlayers()) {
                    if (playerInfo.getProfile().name().toLowerCase().equals(arguments[1].toLowerCase())) {
                        pi = playerInfo;
                        break;
                    }
                }

                if (pi == null) {
                    ChatHelper.addChatMessage(
                            ChatFormatting.RED + "Can't find player data with nickname " + arguments[1]);
                } else {
                    ChatHelper.addChatMessage(ChatFormatting.AQUA + arguments[1] + ChatFormatting.GREEN + " has ping: "
                            + ChatFormatting.LIGHT_PURPLE + pi.getLatency());
                }

            } else {
                ChatHelper.addChatMessage(getUsage());
            }
        } catch (Exception e) {
            ChatHelper.addTranslatedMessage("cmd.invalid_usage", getUsage());
        }
    }
}
