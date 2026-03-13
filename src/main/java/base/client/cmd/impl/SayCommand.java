package base.client.cmd.impl;

import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.client.Minecraft;

public class SayCommand extends CommandAbstract {

    Minecraft mc = Minecraft.getInstance();

    public SayCommand() {
        super("Say", "cmd.say.desc", "say | <message>", "say");
    }

    @Override
    public void execute(String... args) {
        if (args.length > 1) {
            String ss = args[1];
            for (int i = 2; i < args.length; i++) {
                ss += " " + args[i];
            }
            ChatHelper.noevent = true;
            Minecraft.getInstance().player.connection.sendChat(ss);
            ChatHelper.noevent = false;
        } else {
            ChatHelper.addChatMessage(getUsage());
        }
    }
}
