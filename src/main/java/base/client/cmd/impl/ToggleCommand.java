package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.Module;
import base.client.feature.impl.LockedModule;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;

public class ToggleCommand extends CommandAbstract {

    public ToggleCommand() {
        super("toggle", "cmd.toggle.desc", "toggle | <module>", "t", "toggle");
    }

    @Override
    public void execute(String... args) {
        if (args.length == 2) {
            Module feature = Client.instance.featureManager.getModuleByPartialName(args[1]);
            if (feature == null) {
                ChatHelper.addChatMessage(ChatFormatting.RED + args[1] + ChatFormatting.WHITE + " not found");
                return;
            }
            if (feature instanceof LockedModule) {
                ChatHelper.addChatMessage(
                        ChatFormatting.RED + feature.getLabel() + ChatFormatting.WHITE + " cannot be enabled/disabled");
                NotificationManager.publicity(ChatFormatting.RED + feature.getLabel(),
                        ChatFormatting.WHITE + "Cannot be enabled/disabled", 4, NotificationType.ERROR);

                return;
            }

            ChatHelper.addChatMessage(ChatFormatting.WHITE + "Module " + ChatFormatting.BLUE + feature.getLabel()
                    + (feature.getState() ? (ChatFormatting.RED + " was disabled")
                            : (ChatFormatting.GREEN + " was enabled")));
            feature.toggle();

        } else {
            ChatHelper.addChatMessage(getUsage());
        }
    }
}
