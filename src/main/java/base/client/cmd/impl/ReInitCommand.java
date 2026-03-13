package base.client.cmd.impl;

import base.client.Client;
import base.client.cmd.CommandAbstract;
import base.client.helpers.impl.misc.ChatHelper;
import net.minecraft.client.Minecraft;

public class ReInitCommand extends CommandAbstract {

    Minecraft mc = Minecraft.getInstance();

    public ReInitCommand() {
        super("ReInit", "cmd.reinit.desc", ".reinit", "ri", "reinit");
    }

    @Override
    public void execute(String... args) {
        if (args.length > 1) {
            return;
        }

        Client.instance.featureManager.initModules();
        Client.instance.configManager.loadConfig(Client.instance.configname);
        ChatHelper.addTranslatedMessage("cmd.modules_reloaded");
    }
}
