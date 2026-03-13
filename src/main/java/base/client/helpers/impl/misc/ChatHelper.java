package base.client.helpers.impl.misc;

import base.client.Client;
import base.client.feature.impl.client.ChatSettings;
import base.client.helpers.Helper;
import base.client.managers.TranslationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ChatHelper implements Helper {
    public static boolean noevent = false;

    public static void addChatMessage(String message) {
        mc.gui.getChat().addMessage(Component.translatable(generatechatprefix() + message));
    }

    // send a translated message by key
    public static void addTranslatedMessage(String key) {
        addChatMessage(TranslationManager.get(key));
    }

    // send a translated message with format args
    public static void addTranslatedMessage(String key, Object... args) {
        addChatMessage(TranslationManager.get(key, args));
    }

    public static String generatechatprefix() {
        ChatFormatting cf = ChatFormatting.RESET;
        String brackets1 = "", brackets2 = "";

        switch (ChatSettings.CommandBrackets.getCurrentMode()) {
            case ("[]"):
                brackets1 = "[";
                brackets2 = "]";
                break;
            case ("<>"):
                brackets1 = "<";
                brackets2 = ">";
                break;
            case ("{}"):
                brackets1 = "{";
                brackets2 = "}";
                break;
        }
        switch (ChatSettings.ClientNameColor.getCurrentMode()) {
            case ("Blue"):
                cf = ChatFormatting.BLUE;
                break;
            case ("DarkBlue"):
                cf = ChatFormatting.DARK_BLUE;
                break;
            case ("Red"):
                cf = ChatFormatting.RED;
                break;
            case ("DarkRed"):
                cf = ChatFormatting.DARK_RED;
                break;
            case ("Yellow"):
                cf = ChatFormatting.YELLOW;
                break;
            case ("Green"):
                cf = ChatFormatting.GREEN;
                break;
            case ("DarkGreen"):
                cf = ChatFormatting.DARK_GREEN;
                break;
            case ("Gray"):
                cf = ChatFormatting.GRAY;
                break;
            case ("DarkGray"):
                cf = ChatFormatting.DARK_GRAY;
                break;
        }

        return ("\2477" + brackets1 + cf + Client.clientName + ChatFormatting.RESET + "\2477" + brackets2 + " "
                + ChatFormatting.RESET);

    }

    public static String legacySanitize(String input) {// УБИРАЕМ НИЩИЕ СИМВОЛЫ
        return input.replaceAll(
                "[^\\p{L}\\p{N}\\p{P}\\p{S}" + // Базовые категории Unicode (буквы, цифры, пунктуация, символы)
                        "›«»„“”‘’–—§©®±°µ·×÷←↑→↓↔↕↨∂∆∏∑√∞≈≠≤≥" + // Доп. символы
                        "⌂⌐⌠⌡─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬" + // Рамки и псевдографика
                        "▀▄█▌▐░▒▓■□▪▫◊○●☺☻☼♀♂♠♣♥♦♪♫ " + // Символы из Minecraft и других игр
                        "]",
                "");
    }

}
