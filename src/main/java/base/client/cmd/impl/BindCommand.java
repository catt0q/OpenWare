package base.client.cmd.impl;

import base.client.feature.Module;
import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.impl.LockedModule;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.KeyUtil;
import base.client.managers.TranslationManager;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;

import static base.client.managers.TranslationManager.t;

public class BindCommand extends CommandAbstract {

    public BindCommand() {
        super("bind", "cmd.bind.desc",
                "§6.bind §3<module> §3<key> §7| §6.bind §3<module> §3none §7(to unbind)",
                "§6.bind §3<module> §3<key> §7| §6.bind §3clearall",
                "bind", "b");
    }

    @Override
    public void execute(String... arguments) {
        try {
            if (arguments.length == 3) {
                // .bind clear all
                if (arguments[1].equalsIgnoreCase("clear") && arguments[2].equalsIgnoreCase("all")) {
                    for (Module module : Client.instance.featureManager.modules) {
                        module.setBind(0);
                    }
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + t("bind.all_cleared"));
                    return;
                }

                String moduleName = arguments[1];
                String bind = arguments[2].toUpperCase();
                Module feature = Client.instance.featureManager.getModuleByPartialName(moduleName);

                if (feature == null) {
                    ChatHelper.addChatMessage(t("bind.module_undefined", arguments[1]));
                    return;
                }
                if (feature instanceof LockedModule) {
                    ChatHelper.addChatMessage(t("bind.cannot_edit", feature.getLabel()));
                    NotificationManager.publicity("Bind Manager", t("bind.cannot_edit", feature.getLabel()), 4, NotificationType.ERROR);
                    return;
                }

                // If key is a single character, it's a valid key - bind it
                // If key is multiple characters (like "none", "clear", "remove"), unbind
                if (arguments[2].length() == 1) {
                    int keyValue = KeyUtil.StringToKey(bind).getValue();
                    feature.setBind(keyValue);
                    ChatHelper.addChatMessage(t("bind.set", feature.getLabel(), bind));
                    NotificationManager.publicity("Bind Manager", t("bind.set", feature.getLabel(), bind), 4, NotificationType.SUCCESS);
                } else {
                    // Multi-char key = unbind
                    feature.setBind(0);
                    ChatHelper.addChatMessage(t("bind.cleared", feature.getLabel()));
                    NotificationManager.publicity("Bind Manager", t("bind.cleared", feature.getLabel()), 4, NotificationType.SUCCESS);
                }

            } else if (arguments.length == 2) {
                if (arguments[1].equalsIgnoreCase("clearall")) {
                    for (Module module : Client.instance.featureManager.modules) {
                        module.setBind(0);
                    }
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + t("bind.all_cleared"));
                } else {
                    // .bind <module> - just show current bind or clear it
                    Module feature = Client.instance.featureManager.getModuleByPartialName(arguments[1]);
                    if (feature == null) {
                        ChatHelper.addChatMessage(t("bind.module_undefined", arguments[1]));
                        return;
                    }
                    feature.setBind(0);
                    ChatHelper.addChatMessage(t("bind.cleared", feature.getLabel()));
                }

            } else {
                ChatHelper.addChatMessage(this.getUsage());
            }
        } catch (Exception ignored) {

        }
    }
}