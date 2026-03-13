package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.Module;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.KeyUtil;
import net.minecraft.ChatFormatting;

public class SetCommand extends CommandAbstract {

    public SetCommand() {
        super("set", "cmd.set.desc", "§6.set", "§6.set", "set", "set");
    }

    @Override
    public void execute(String... arguments) {
        try {
            int len = arguments.length;

            if (!arguments[0].equals("set")) {
                return;
            }
            if (len < 4) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                return;
            }
            int currlen = 2;

            String moduleName = "";
            moduleName += arguments[1];

            // Finding Module
            if (Client.instance.featureManager.getModuleByLabelNoSpace(moduleName) == null) {
                for (int i = 2; i < len; i++) {
                    moduleName += " ";
                    moduleName += arguments[i];
                    currlen = i;
                    if (Client.instance.featureManager.getModuleByLabelNoSpace(moduleName) != null) {
                        break;
                    }
                }
                currlen++;
            }

            if (Client.instance.featureManager.getModuleByLabelNoSpace(moduleName) == null) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Module not found");
                return;
            }

            Module module = Client.instance.featureManager.getModuleByLabelNoSpace(moduleName);
            String settingName = "";

            int deltal = len - currlen;
            if (deltal < 2) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                return;
            }
            deltal--;
            settingName += arguments[currlen];
            currlen++; // ChatHelper.addChatMessage(moduleName+" "+settingName);
            // Finding Setting
            if (module.getSettingByLabelNoSpace(settingName) == null) {
                for (int i = currlen; i < len; i++) {
                    settingName += " ";
                    settingName += arguments[i];
                    currlen = i;
                    if (module.getSettingByLabelNoSpace(settingName) != null) {
                        break;
                    }

                }
                currlen++;
            }

            if (module.getSettingByLabelNoSpace(settingName) == null) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Setting not found");
                return;
            }

            Setting setting = module.getSettingByLabelNoSpace(settingName);
            String value = "";

            if (deltal < 1) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                return;
            }
            deltal--;
            value += arguments[currlen];
            currlen++;
            deltal = len - currlen;

            if (value.length() < 1) {
                ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                return;
            }

            if (setting instanceof BooleanSetting) {
                String lc = value.toLowerCase();
                if (lc.equals("1") || lc.equals("true") || lc.equals("да") || lc.equals("вкл") || lc.equals("activate")
                        || lc.equals("yes") || lc.equals("enable") || lc.equals("enabled") || lc.equals("on")) {
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Set " + setting.getName() + " to " + "true"));
                    ((BooleanSetting) setting).setBoolValue(true);
                } else if (lc.equals("0") || lc.equals("false") || lc.equals("нет") || lc.equals("выкл")
                        || lc.equals("diactivate") || lc.equals("no") || lc.equals("disable") || lc.equals("disabled")
                        || lc.equals("on")) {
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Set " + setting.getName() + " to " + "false"));
                    ((BooleanSetting) setting).setBoolValue(false);
                }
            }
            if (setting instanceof NumberSetting) {
                value = value.replaceAll("[^-0123456789., ]", "");
                if (value.length() < 1) {
                    ChatHelper.addChatMessage(ChatFormatting.RED + "Not Enough Arguments");
                    return;
                }
                Float newvalf = parsefl(value, ((NumberSetting) setting).getValue());
                ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Set " + setting.getName() + " to " + newvalf));
                ((NumberSetting) setting).setValueNumber(newvalf);
            }

            if (setting instanceof ModeSetting) {
                String lc = value.toLowerCase();
                ModeSetting mset = (ModeSetting) setting;
                String smode = mset.exsistpredmode(lc);
                if (smode != null && smode.length() > 0) {
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Set " + setting.getName() + " to " + smode));
                    ((ModeSetting) setting).currentMode = smode;
                } else {
                    ChatHelper.addChatMessage(ChatFormatting.RED + "Mode not found");
                    return;
                }
            }
            if (setting instanceof KeyBindSetting) {
                KeyBindSetting kset = (KeyBindSetting) setting;
                String lc = value.toLowerCase();
                if (lc.equalsIgnoreCase("remove")) {
                    kset.setKeyCode(0);
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Bind " + setting.getName() + " removed"));

                    return;
                }
                kset.setKeyCode(KeyUtil.StringToKey(lc).getValue());
                ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Bind " + setting.getName() + " to " + lc));
            }
            if (setting instanceof StringSetting) {
                StringSetting sset = (StringSetting) setting;
                for (int i = currlen; i < len; i++) {
                    value += " ";
                    value += arguments[currlen];
                }
                sset.setCurrentText(value);
                ChatHelper.addChatMessage(ChatFormatting.GREEN + ("Set " + setting.getName() + " to " + value));
            }

        } catch (Exception ignored) {

        }
    }

    public static float parsefl(String input, Float oldnum) {
        if (input == null || input.trim().isEmpty()) {
            return oldnum;
        }
        String trimmed = input.trim();
        if (!trimmed.matches("-?\\d+(\\.\\d+)?")) {
            return oldnum;
        }
        try {
            return Float.parseFloat(trimmed);
        } catch (NumberFormatException e) {
            return oldnum;
        }
    }

}
