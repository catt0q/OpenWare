package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.Module;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class UnHideCommand extends CommandAbstract {

    Minecraft mc = Minecraft.getInstance();

    public UnHideCommand() {
        super("unhide", "cmd.unhide.desc", "hide | <module>", "unhide");
    }

    @Override
    public void execute(String... args) {
        if (args.length == 2) {
            if (args[0].toLowerCase().equals("unhide")) {
                Module feature = Client.instance.featureManager.getModuleByPartialName(args[1]);
                if (feature == null) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + args[1] + ChatFormatting.WHITE + " not found");
                } else {

                    ChatHelper
                            .addChatMessage(ChatFormatting.WHITE + "Module " + ChatFormatting.BLUE + feature.getLabel()
                                    + (!feature.isHidden() ? (ChatFormatting.GREEN + " is visible")
                                            : (ChatFormatting.GREEN + " was unhiden")));
                    feature.setHidden(false);
                }

            }

        } else {
            ChatHelper.addChatMessage(getUsage());
        }
    }
}
