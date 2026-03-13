package base.client.cmd.impl;

import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;

public class VClipCommand extends CommandAbstract {

    public VClipCommand() {
        super("vclip", "Teleport vertically",
                "§6.vclip §3<blocks>",
                "vclip", "vc");
    }

    @Override
    public void execute(String... arguments) {
        if (mc.player == null) return;

        if (arguments.length < 2) {
            ChatHelper.addChatMessage(this.getUsage());
            return;
        }

        try {
            double blocks = Double.parseDouble(arguments[1]);
            double newY = mc.player.getY() + blocks;
            mc.player.setPos(mc.player.getX(), newY, mc.player.getZ());
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Teleported " + ChatFormatting.WHITE + blocks + " blocks vertically");
        } catch (NumberFormatException e) {
            ChatHelper.addChatMessage(ChatFormatting.RED + "Invalid number: " + arguments[1]);
        }
    }
}
