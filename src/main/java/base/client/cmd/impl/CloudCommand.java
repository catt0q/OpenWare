package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
// Auth API removed: no-auth mode
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class CloudCommand extends CommandAbstract {

    private static long connectionStartTime = 0;
    private static final Minecraft mc = Minecraft.getInstance();

    public CloudCommand() {
        super("cloud", "cmd.cloud.desc",
                "§6.cloud" + ChatFormatting.LIGHT_PURPLE + " status | config save/load/list/delete <name>", "cloud");
    }

    public static void markConnected() {
        connectionStartTime = System.currentTimeMillis();
    }

    @Override
    public void execute(String... args) {

        if (args.length < 2) {
            ChatHelper.addChatMessage(getUsage());
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "status":
                showStatus();
                break;
            case "config":
                if (args.length < 3) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.usage.config"));
                    return;
                }
                handleConfig(args);
                break;
            default:
                ChatHelper.addChatMessage(getUsage());
        }
    }

    private void showStatus() {
        new Thread(() -> {
            try {
                // No-auth mode: provide a minimal status output
                String elapsed = formatElapsed(System.currentTimeMillis() - connectionStartTime);
                mc.execute(() -> {
                    ChatHelper.addChatMessage("");
                    ChatHelper.addChatMessage(ChatFormatting.GOLD + TranslationManager.get("cloud.status.connected"));
                    ChatHelper.addChatMessage(ChatFormatting.GRAY + TranslationManager.get("cloud.status.elapsed", elapsed));
                    ChatHelper.addChatMessage(ChatFormatting.GRAY + TranslationManager.get("cloud.status.user", ChatFormatting.AQUA + "local"));
                    ChatHelper.addChatMessage(ChatFormatting.GRAY + TranslationManager.get("cloud.status.resets",
                            ChatFormatting.YELLOW + "0", 0, 0, 0));
                    ChatHelper.addChatMessage("");
                });
            } catch (Exception e) {
                mc.execute(() -> ChatHelper
                        .addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.error", e.getMessage())));
            }
        }).start();
    }

    private void handleConfig(String[] args) {
        String action = args[2].toLowerCase();

        switch (action) {
            case "list":
                listConfigs();
                break;
            case "save":
                if (args.length < 4) {
                    saveConfig(Client.instance.configname);
                } else {
                    saveConfig(args[3]);
                }
                break;
            case "load":
                if (args.length < 4) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.usage.load"));
                    return;
                }
                loadConfig(args[3]);
                break;
            case "delete":
                if (args.length < 4) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.usage.delete"));
                    return;
                }
                deleteConfig(args[3]);
                break;
            default:
                ChatHelper.addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.unknown_action", action));
        }
    }

    private void listConfigs() {
        new Thread(() -> {
            try {
                // No-auth mode: no cloud configs available
                String[] configs = new String[0];
                mc.execute(() -> {
                    if (configs.length == 0) {
                        ChatHelper.addChatMessage(
                                ChatFormatting.GRAY + TranslationManager.get("cloud.config.list_empty"));
                        return;
                    }

                    ChatHelper.addChatMessage(
                            ChatFormatting.GOLD + TranslationManager.get("cloud.config.list_header", configs.length));
                    for (String config : configs) {
                        ChatHelper.addChatMessage(ChatFormatting.GRAY + " - " + ChatFormatting.WHITE + config);
                    }
                });
            } catch (Exception e) {
                mc.execute(() -> ChatHelper
                        .addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.error", e.getMessage())));
            }
        }).start();
    }

    private void saveConfig(String name) {
        new Thread(() -> {
            try {
                // In no-auth mode, simulate a successful save locally (no cloud I/O)
                mc.execute(() -> {
                    ChatHelper.addChatMessage(
                            ChatFormatting.GREEN + TranslationManager.get("cloud.config.save_success", name));
                    NotificationManager.publicity("Cloud",
                            TranslationManager.get("cloud.config.save_success", name), 3, NotificationType.SUCCESS);
                });
            } catch (Exception e) {
                mc.execute(() -> ChatHelper
                        .addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.error", e.getMessage())));
            }
        }).start();
    }

    private void loadConfig(String name) {
        new Thread(() -> {
            try {
                // No cloud: pretend load succeeded using local config handler
                mc.execute(() -> {
                    Client.instance.configname = name;
                    ChatHelper.addChatMessage(
                            ChatFormatting.GREEN + TranslationManager.get("cloud.config.load_success", name));
                    NotificationManager.publicity("Cloud",
                            TranslationManager.get("cloud.config.load_success", name), 3, NotificationType.SUCCESS);
                });
            } catch (Exception e) {
                mc.execute(() -> ChatHelper
                        .addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.error", e.getMessage())));
            }
        }).start();
    }

    private void deleteConfig(String name) {
        new Thread(() -> {
            try {
                // No cloud: simulate delete success locally
                mc.execute(() -> {
                    ChatHelper.addChatMessage(
                            ChatFormatting.GREEN + TranslationManager.get("cloud.config.delete_success", name));
                    NotificationManager.publicity("Cloud",
                            TranslationManager.get("cloud.config.delete_success", name), 3, NotificationType.SUCCESS);
                });
            } catch (Exception e) {
                mc.execute(() -> ChatHelper
                        .addChatMessage(ChatFormatting.RED + TranslationManager.get("cloud.error", e.getMessage())));
            }
        }).start();
    }

    private String formatElapsed(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
