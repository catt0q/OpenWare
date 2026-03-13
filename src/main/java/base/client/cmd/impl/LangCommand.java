package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;

public class LangCommand extends CommandAbstract {

    public LangCommand() {
        super("Lang", "cmd.lang.desc", ".lang <ru/en>", "lang");
    }

    @Override
    public void execute(String... args) {
        if (args.length >= 2) {
            String lang = args[1].toLowerCase();

            if (lang.equals("ru") || lang.equals("en")) {
                TranslationManager.setLanguage(lang);
                ChatHelper.addTranslatedMessage("lang.changed", lang);
            } else {
                ChatHelper.addTranslatedMessage("lang.available");
            }
        } else {
            ChatHelper.addTranslatedMessage("lang.current", TranslationManager.getCurrentLanguage());
            ChatHelper.addChatMessage(getUsage());
        }
    }
}
