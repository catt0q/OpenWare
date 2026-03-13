package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.feature.Module;
import base.client.feature.settings.Setting;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.ChatFormatting;

public class ReloadCommand extends CommandAbstract {

    public ReloadCommand() {
        super("reload", "cmd.reload.desc", "§6.reload " + ChatFormatting.LIGHT_PURPLE + " <Module name>", "reload");
    }

    @Override
    public void execute(String... arguments) {
        try {
            if (arguments.length == 2) {
                String action = arguments[1].toLowerCase();
                if (action.equals("all")) {
                    for (Module module : Client.instance.featureManager.modules) {
                        for (Setting setting : module.getSettings()) {
                            if (setting instanceof NumberSetting) {
                                NumberSetting set = (NumberSetting) setting;
                                set.setValueNumber(set.getDefvalue());
                            } else if (setting instanceof ModeSetting) {
                                ModeSetting set = (ModeSetting) setting;
                                set.currentMode = set.defMode;
                            } else if (setting instanceof KeyBindSetting) {
                                KeyBindSetting set = (KeyBindSetting) setting;
                                set.setKeyCode(0);
                            } else if (setting instanceof StringSetting) {
                                StringSetting set = (StringSetting) setting;
                                set.currentText = set.getDefaultText();
                            } else if (setting instanceof BooleanSetting) {
                                BooleanSetting set = (BooleanSetting) setting;
                                set.setState(set.isDefstate());
                            } else if (setting instanceof ColorSetting) {
                                ColorSetting set = (ColorSetting) setting;
                                set.setColorValue(set.getDefcolor());
                            }

                        }
                        if (module.getState()) {
                            module.toggle();
                        }

                        module.setVisible(true);
                        module.setSuffixVisible(true);
                    }
                    ChatHelper.addChatMessage(ChatFormatting.GREEN + "All modules reloaded");

                } else {
                    Module feature = Client.instance.featureManager.getModuleByPartialName(action);
                    if (feature == null) {
                        ChatHelper.addChatMessage(ChatFormatting.WHITE + "Module " + ChatFormatting.RED + arguments[2]
                                + ChatFormatting.WHITE + " is undefined");
                        return;
                    }
                    for (Setting setting : feature.getSettings()) {
                        if (setting instanceof NumberSetting) {
                            NumberSetting set = (NumberSetting) setting;
                            set.setValueNumber(set.getDefvalue());
                        } else if (setting instanceof ModeSetting) {
                            ModeSetting set = (ModeSetting) setting;
                            set.currentMode = set.defMode;
                        } else if (setting instanceof KeyBindSetting) {
                            KeyBindSetting set = (KeyBindSetting) setting;
                            set.setKeyCode(0);
                        } else if (setting instanceof StringSetting) {
                            StringSetting set = (StringSetting) setting;
                            set.currentText = set.getDefaultText();
                        } else if (setting instanceof BooleanSetting) {
                            BooleanSetting set = (BooleanSetting) setting;
                            set.setState(set.isDefstate());
                        } else if (setting instanceof ColorSetting) {
                            ColorSetting set = (ColorSetting) setting;
                            set.setColorValue(set.getDefcolor());
                        }

                    }

                    if (feature.getState()) {
                        feature.toggle();
                    }
                    feature.setVisible(true);
                    feature.setSuffixVisible(true);

                    ChatHelper.addChatMessage(ChatFormatting.WHITE + "Module " + ChatFormatting.LIGHT_PURPLE
                            + feature.getLabel() + " " + ChatFormatting.GREEN + "successfully reloaded");
                }

            }
        } catch (Exception ignored) {

        }
    }
}