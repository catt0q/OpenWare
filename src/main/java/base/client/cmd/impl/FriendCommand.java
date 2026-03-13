package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

public class FriendCommand extends CommandAbstract {
    Minecraft mc = Minecraft.getInstance();

    public FriendCommand() {
        super("friend", "cmd.friend.desc",
                "§6.friend" + ChatFormatting.LIGHT_PURPLE + " add " + "§3<nickname> | §6.friend"
                        + ChatFormatting.LIGHT_PURPLE + " del " + "§3<nickname> | §6.friend"
                        + ChatFormatting.LIGHT_PURPLE
                        + " list " + "| §6.friend" + ChatFormatting.LIGHT_PURPLE + " clear",
                "friend");
    }

    @Override
    public void execute(String... arguments) {
        try {
            if (arguments.length > 1) {
                if (arguments[0].equalsIgnoreCase("friend")) {
                    if (arguments[1].equalsIgnoreCase("add")) {
                        String name = arguments[2];
                        if (name.equals(mc.getInstance().player.getName())) {
                            ChatHelper.addTranslatedMessage("friend.cant_add_self");
                            NotificationManager.publicity("Friend Manager", "You can't add yourself!", 4,
                                    NotificationType.ERROR);
                            return;
                        }
                        if (!Client.instance.friendManager.isFriend(name)) {
                            Client.instance.friendManager.addFriend(name);
                            ChatHelper.addTranslatedMessage("friend.added",
                                    ChatFormatting.GREEN + name + ChatFormatting.WHITE);
                            NotificationManager
                                    .publicity(
                                            "Friend Manager", "Friend " + ChatFormatting.RED + name
                                                    + ChatFormatting.WHITE + " added to your friend list!",
                                            4, NotificationType.SUCCESS);
                        }
                    }
                    if (arguments[1].equalsIgnoreCase("del") || arguments[1].equalsIgnoreCase("remove")) {
                        String name = arguments[2];
                        if (Client.instance.friendManager.isFriend(name)) {
                            Client.instance.friendManager.removeFriend(name);
                            ChatHelper.addTranslatedMessage("friend.removed",
                                    ChatFormatting.RED + name + ChatFormatting.WHITE);
                            NotificationManager
                                    .publicity(
                                            "Friend Manager", "Friend " + ChatFormatting.RED + name
                                                    + ChatFormatting.WHITE + " deleted from your friend list!",
                                            4, NotificationType.SUCCESS);
                        }
                    }
                    if (arguments[1].equalsIgnoreCase("clear")) {
                        if (Client.instance.friendManager.getFriends().isEmpty()) {
                            ChatHelper.addTranslatedMessage("friend.list_empty");
                            NotificationManager.publicity("Friend Manager", "Your friend list is empty!", 4,
                                    NotificationType.ERROR);
                            return;
                        }
                        Client.instance.friendManager.getFriends().clear();
                        ChatHelper.addTranslatedMessage("friend.list_cleared");
                        NotificationManager.publicity("Friend Manager",
                                "Your " + ChatFormatting.GREEN + "friend list " + ChatFormatting.WHITE + "was cleared!",
                                4, NotificationType.SUCCESS);
                    }
                    if (arguments[1].equalsIgnoreCase("list")) {
                        if (Client.instance.friendManager.getFriends().isEmpty()) {
                            ChatHelper.addTranslatedMessage("friend.list_empty");
                            NotificationManager.publicity("Friend Manager", "Your friend list is empty!", 4,
                                    NotificationType.ERROR);
                            return;
                        }
                        Client.instance.friendManager.getFriends().forEach(friend -> ChatHelper
                                .addTranslatedMessage("friend.list_title", ChatFormatting.RED + friend.getName()));
                    }
                }
            } else {
                ChatHelper.addChatMessage(getUsage());
            }
        } catch (Exception e) {
            ChatHelper.addTranslatedMessage("cmd.invalid_usage", getUsage());
        }
    }
}
