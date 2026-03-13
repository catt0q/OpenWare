package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class RenameCommand extends CommandAbstract {

    Minecraft mc = Minecraft.getInstance();

    public RenameCommand() {
        super("Rename", "cmd.rename.desc", "rename | <name>", "rename");
    }

    @Override
    public void execute(String... args) {
        if (args.length >= 2) {
            // Join all arguments from index 1 onwards with spaces
            StringBuilder newName = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                newName.append(args[i]);
                if (i < args.length - 1) {
                    newName.append(" ");
                }
            }

            String finalName = newName.toString();
            Client.clientName = finalName;
            ChatHelper.addTranslatedMessage("client.renamed", ChatFormatting.RED + finalName);
        } else {
            ChatHelper.addChatMessage(getUsage());
        }
    }
}