package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.Command;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;

public class HelpCommand extends CommandAbstract {

    public HelpCommand() {
        super("Help", "cmd.help.desc", ".help", "help", "?");
    }

    @Override
    public void execute(String... args) {
        ChatHelper.addTranslatedMessage("cmd.help.header");
        for (Command cmd : Client.instance.commandManager.getCommands()) {
            CommandAbstract abstractCmd = (CommandAbstract) cmd;
            ChatHelper.addChatMessage(
                    "§e." + abstractCmd.getName().toLowerCase() + " §7- " + abstractCmd.getTranslatedDescription());
        }
    }
}
