package base.client.cmd.impl;

import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;

public class HClipCommand extends CommandAbstract {

    public HClipCommand() {
        super("hclip", "Teleport horizontally in look direction",
                "§6.hclip §3<blocks>",
                "hclip", "hc");
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
            float yaw = mc.player.getYRot();
            double radians = Math.toRadians(yaw);

            // negative sin for X, positive cos for Z (Minecraft coordinate system)
            double deltaX = -Math.sin(radians) * blocks;
            double deltaZ = Math.cos(radians) * blocks;

            mc.player.setPos(mc.player.getX() + deltaX, mc.player.getY(), mc.player.getZ() + deltaZ);
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Teleported " + ChatFormatting.WHITE + blocks + " blocks horizontally");
        } catch (NumberFormatException e) {
            ChatHelper.addChatMessage(ChatFormatting.RED + "Invalid number: " + arguments[1]);
        }
    }
}
