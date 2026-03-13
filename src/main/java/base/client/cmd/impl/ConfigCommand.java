package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.settings.config.Config;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.ConfigManager;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;

public class ConfigCommand extends CommandAbstract {

    public ConfigCommand() {
        super("config", "cmd.config.desc",
                "§6.config" + ChatFormatting.LIGHT_PURPLE + " save | load | delete " + "§3<name>", "config", "cfg");
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length >= 2) {
                String upperCase = args[1].toUpperCase();
                if (args.length == 3) {
                    switch (upperCase) {

                        case "LOAD":
                            if (Client.instance.configManager.loadConfig(args[2])) {
                                Client.instance.configname = args[2];
                                ChatHelper.addChatMessage(ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE
                                        + "loaded config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE
                                                + "loaded config: " + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.SUCCESS);
                            } else {
                                ChatHelper.addChatMessage(ChatFormatting.RED + "Failed " + ChatFormatting.WHITE
                                        + "load config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.RED + "Failed " + ChatFormatting.WHITE + "load config: "
                                                + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.ERROR);
                            }
                            break;
                        case "SAVE":
                            if (Client.instance.configManager.saveConfig(args[2])) {
                                Client.instance.configname = args[2];
                                ChatHelper.addChatMessage(ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE
                                        + "saved config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE + "saved config: "
                                                + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.SUCCESS);
                                ConfigManager.getLoadedConfigs().clear();
                                Client.instance.configManager.load();
                            } else {
                                ChatHelper.addChatMessage(ChatFormatting.RED + "Failed " + ChatFormatting.WHITE
                                        + "to save config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.RED + "Failed " + ChatFormatting.WHITE + "to save config: "
                                                + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.ERROR);
                            }
                            break;
                        case "DELETE":
                            if (Client.instance.configManager.deleteConfig(args[2])) {
                                ChatHelper.addChatMessage(ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE
                                        + "deleted config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE
                                                + "deleted config: " + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.SUCCESS);
                            } else {
                                ChatHelper.addChatMessage(ChatFormatting.RED + "Failed " + ChatFormatting.WHITE
                                        + "to delete config: " + ChatFormatting.RED + "\"" + args[2] + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.RED + "Failed " + ChatFormatting.WHITE + "to delete config: "
                                                + ChatFormatting.RED + "\"" + args[2] + "\"",
                                        4, NotificationType.ERROR);
                            }
                            break;
                    }
                } else if (args.length == 2) {
                    switch (upperCase.toLowerCase()) {
                        case ("list"):
                            ChatHelper.addChatMessage(ChatFormatting.DARK_GRAY + "Configs:");
                            for (Config config : Client.instance.configManager.getContents()) {

                                ChatHelper.addChatMessage(
                                        (Client.instance.configname.equals(config.getName()) ? ChatFormatting.GREEN
                                                : ChatFormatting.RED) + config.getName());
                            }
                            break;
                        case ("save"):
                            if (Client.instance.configManager.saveConfig(Client.instance.configname)) {
                                ChatHelper.addChatMessage(
                                        ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE + "saved config: "
                                                + ChatFormatting.RED + "\"" + Client.instance.configname + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.GREEN + "Successfully " + ChatFormatting.WHITE + "saved config: "
                                                + ChatFormatting.RED + "\"" + Client.instance.configname + "\"",
                                        4, NotificationType.SUCCESS);
                                ConfigManager.getLoadedConfigs().clear();
                                Client.instance.configManager.load();
                            } else {
                                ChatHelper.addChatMessage(
                                        ChatFormatting.RED + "Failed " + ChatFormatting.WHITE + "to save config: "
                                                + ChatFormatting.RED + "\"" + Client.instance.configname + "\"");
                                NotificationManager.publicity("Config",
                                        ChatFormatting.RED + "Failed " + ChatFormatting.WHITE + "to save config: "
                                                + ChatFormatting.RED + "\"" + Client.instance.configname + "\"",
                                        4, NotificationType.ERROR);
                            }
                            break;
                        case ("reload"):
                            boolean bl = Client.instance.configManager.loadConfig(Client.instance.configname);
                            if (bl) {
                                ChatHelper.addChatMessage(ChatFormatting.GREEN + "Config successfully reloaded");
                                NotificationManager.publicity("Config", ChatFormatting.GREEN + "Successfully reloaded",
                                        4, NotificationType.SUCCESS);

                            } else {
                                ChatHelper.addChatMessage(ChatFormatting.RED + "Failed to reload config");
                                NotificationManager.publicity("Config", ChatFormatting.GREEN + "Failed to reload", 4,
                                        NotificationType.ERROR);

                            }
                            break;

                    }

                }

            } else {
                ChatHelper.addChatMessage(this.getUsage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
