package base.client.cmd.impl;

import base.client.feature.Module;
import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.KeyUtil;
import net.minecraft.ChatFormatting;

import java.awt.*;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import base.client.feature.impl.client.CommandSettings;

public class CheckCommand extends CommandAbstract {

    public CheckCommand() {
        super("check", "cmd.check.desc", "§6.check " + ChatFormatting.LIGHT_PURPLE + " <Module name>", "check");
    }

    @Override
    public void execute(String... arguments) {
        try {

            String action = arguments[1].toLowerCase();
            if (action.equals("all")) {

                CopyOnWriteArrayList<Module> originalOrder = new CopyOnWriteArrayList<>(
                        Client.instance.featureManager.getModuleList());

                Client.instance.featureManager.getModuleList().sort(Comparator.comparing(Module::getLabel));

                for (Module feature : Client.instance.featureManager.modules) {
                    String ena = feature.getState() ? (ChatFormatting.GREEN + "enabled")
                            : (ChatFormatting.RED + "disabled");
                    String vis = feature.isVisible() ? (ChatFormatting.GREEN + "visible")
                            : (ChatFormatting.RED + "hidden");
                    String svis = feature.isSuffixVisible() ? (ChatFormatting.GREEN + "visible")
                            : (ChatFormatting.RED + "hidden");
                    ChatHelper.addChatMessage(ChatFormatting.LIGHT_PURPLE + feature.getLabel() + ChatFormatting.BLUE
                            + " : state: " + ena);

                }
                Client.instance.featureManager.getModuleList().clear();
                Client.instance.featureManager.getModuleList().addAll(originalOrder);
            } else {
                int len = arguments.length;

                if (len < 2) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                    return;
                }
                int currlen = 2;

                String moduleName = "";
                moduleName += arguments[1];

                // Будем считать всё хорошо т.е. getModuleByPartialName
                // Finding Module
                /*
                 * if(Client.instance.featureManager.getModuleByLabelNoSpace(moduleName)==null)
                 * {
                 * for(int i=currlen;i<len;i++) {
                 * moduleName+=" "; moduleName+=arguments[i]; currlen=i;
                 * if(Client.instance.featureManager.getModuleByLabelNoSpace(moduleName)!=null)
                 * { break; }
                 * }
                 * }
                 */

                // if(Client.instance.featureManager.getModuleByLabelNoSpace(moduleName)==null)
                // { ChatHelper.addChatMessage(ChatFormatting.RED +"Module not found"); return;
                // }

                // Module
                // module=Client.instance.featureManager.getModuleByLabelNoSpace(moduleName);
                Module module = Client.instance.featureManager.getModuleByPartialName(moduleName);
                boolean findsetting = false;
                String settingName = "";
                if (currlen < len) {

                    for (int i = currlen; i < len; i++) {

                        settingName += " ";
                        settingName += arguments[i];
                        currlen = i;
                        if (module.getSettingByLabelNoSpace(settingName) != null) {
                            findsetting = true;
                            break;
                        }
                    }

                }

                if (findsetting) {
                    Setting setting = module.getSettingByLabelNoSpace(settingName);
                    processchecksetting(setting);

                } else {
                    processcheckmodule(module);
                }

            }

        } catch (Exception ignored) {

        }
    }

    private void processcheckmodule(Module feature) {

        String ena = feature.getState() ? (ChatFormatting.GREEN + "enabled") : (ChatFormatting.RED + "disabled");
        String vis = feature.isVisible() ? (ChatFormatting.GREEN + "visible") : (ChatFormatting.RED + "hidden");
        String svis = feature.isSuffixVisible() ? (ChatFormatting.GREEN + "visible") : (ChatFormatting.RED + "hidden");
        String fbind = feature.getBind() == 0 ? ("NONE") : (KeyUtil.KeyToString(feature.getBind()).toUpperCase());
        ChatHelper.addChatMessage(
                ChatFormatting.LIGHT_PURPLE + feature.getLabel() + ChatFormatting.WHITE + " | " + ChatFormatting.BLUE
                        + "State: " + ena + ChatFormatting.BLUE + " ModuleList: " + vis + ChatFormatting.BLUE
                        + " Suffix: " + svis + ChatFormatting.BLUE + " Bind: " + ChatFormatting.GREEN + fbind);
        for (Setting setting : feature.getSettings()) {
            if (setting == null)
                continue;
            if (setting.isVisible() || CommandSettings.FullCheck.isEnabled()) {

                if (setting instanceof NumberSetting) {
                    NumberSetting set = (NumberSetting) setting;
                    ChatHelper.addChatMessage(set.getName() + ": " + ChatFormatting.GREEN + set.getValue());
                } else if (setting instanceof ModeSetting) {
                    ModeSetting set = (ModeSetting) setting;
                    ChatHelper.addChatMessage(set.getName() + ": " + ChatFormatting.GREEN + set.getCurrentMode());
                } else if (setting instanceof KeyBindSetting) {
                    KeyBindSetting set = (KeyBindSetting) setting;

                    ChatHelper.addChatMessage(set.getName() + ": " + ChatFormatting.GREEN
                            + (set.getKeyCode() == 0 ? "NONE" : KeyUtil.KeyToString(set.getKeyCode()).toUpperCase()));
                } else if (setting instanceof StringSetting) {
                    StringSetting set = (StringSetting) setting;
                    ChatHelper.addChatMessage(set.getName() + ": " + set.getCurrentText());
                } else if (setting instanceof BooleanSetting) {
                    BooleanSetting set = (BooleanSetting) setting;
                    String enas = set.isEnabled() ? (ChatFormatting.GREEN + "enabled")
                            : (ChatFormatting.RED + "disabled");
                    ChatHelper.addChatMessage(set.getName() + ": " + enas);
                } else if (setting instanceof ColorSetting) {
                    ColorSetting set = (ColorSetting) setting;
                    Color awtColor = new Color(set.getColorValue(), true);
                    ChatHelper.addChatMessage(set.getName() + ": RGB " + ChatFormatting.DARK_RED + awtColor.getRed()
                            + ChatFormatting.RESET + " | " + ChatFormatting.DARK_GREEN + awtColor.getGreen()
                            + ChatFormatting.RESET + " | " + ChatFormatting.DARK_BLUE + awtColor.getBlue());
                }
            }

        }

    }

    private void processchecksetting(Setting setting) {
        String vis = setting.isVisible() ? (ChatFormatting.GREEN + "Visible") : (ChatFormatting.RED + "Hidden");
        ChatHelper.addChatMessage(ChatFormatting.LIGHT_PURPLE + setting.getName() + ChatFormatting.RESET + " | " + vis
                + ((setting.getDesc() != null)
                        ? (ChatFormatting.RESET + " | " + ChatFormatting.LIGHT_PURPLE + " " + setting.getDesc())
                        : ChatFormatting.RESET));

        if (setting instanceof NumberSetting) {
            NumberSetting set = (NumberSetting) setting;
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Current Value: " + ChatFormatting.GRAY + set.getValue());
            ChatHelper.addChatMessage(ChatFormatting.GREEN
                    + " Min - Max Value " + ChatFormatting.GRAY + set.getMinValue() + " - " + set.getMaxValue());
            ChatHelper.addChatMessage(ChatFormatting.GREEN
                    + " Default Value " + ChatFormatting.GRAY + set.getDefvalue() + ChatFormatting.RESET + " | "
                    + ChatFormatting.GRAY + " Increment " + set.getIncrement());
        } else if (setting instanceof ModeSetting) {
            ModeSetting set = (ModeSetting) setting;
            ChatHelper.addChatMessage(
                    ChatFormatting.GREEN + "Current Mode: " + ChatFormatting.GRAY + set.getCurrentMode());
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Default Mode: " + ChatFormatting.GRAY + set.getDefMode());
            String modes = "";
            for (String mode : set.modes) {
                modes += mode + ", ";
            }
            modes = modes.substring(0, modes.length() - 2);
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "All Modes: " + ChatFormatting.GRAY + modes);
        } else if (setting instanceof KeyBindSetting) {
            KeyBindSetting set = (KeyBindSetting) setting;

            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Binded to: " + ChatFormatting.GRAY
                    + (set.getKeyCode() == 0 ? "NONE" : KeyUtil.KeyToString(set.getKeyCode()).toUpperCase()));
        } else if (setting instanceof StringSetting) {
            StringSetting set = (StringSetting) setting;

            ChatHelper.addChatMessage(
                    ChatFormatting.GREEN + "Current Text: " + ChatFormatting.GRAY + set.getCurrentText());
            ChatHelper.addChatMessage(
                    ChatFormatting.GREEN + "Default Text: " + ChatFormatting.GRAY + set.getDefaultText());
        } else if (setting instanceof BooleanSetting) {
            BooleanSetting set = (BooleanSetting) setting;

            String enas = set.isEnabled() ? (ChatFormatting.GREEN + "Enabled") : (ChatFormatting.RED + "Disabled");
            String denas = set.isDefstate() ? (ChatFormatting.GREEN + "Enabled") : (ChatFormatting.RED + "Disabled");
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Current State: " + enas);
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Default State: " + denas);
        } else if (setting instanceof ColorSetting) {
            ColorSetting set = (ColorSetting) setting;

            Color awtColor = new Color(set.getColorValue(), true);
            Color dawtColor = new Color(set.getDefcolor(), true);

            ChatHelper.addChatMessage(
                    ChatFormatting.GREEN + "Current Color Value(RGB): " + ChatFormatting.DARK_RED + awtColor.getRed()
                            + ChatFormatting.RESET + " | " + ChatFormatting.DARK_GREEN + awtColor.getGreen()
                            + ChatFormatting.RESET + " | " + ChatFormatting.DARK_BLUE + awtColor.getBlue());
            ChatHelper.addChatMessage(ChatFormatting.GREEN + "Default Color Value(RGB): " +
                    ChatFormatting.RESET + " | " + ChatFormatting.DARK_RED + dawtColor.getRed()
                    + ChatFormatting.DARK_GREEN +
                    ChatFormatting.RESET + " | " + dawtColor.getGreen() + ChatFormatting.DARK_BLUE
                    + dawtColor.getBlue());

        }

    }

}