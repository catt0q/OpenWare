package base.client.cmd;

import base.client.helpers.Helper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;
import net.minecraft.client.Minecraft;

public abstract class CommandAbstract implements Command, Helper {

    protected final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final String usage;
    private final String[] aliases;

    public CommandAbstract(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.usage = usage;
    }

    public void usage() {
        ChatHelper.addTranslatedMessage("cmd.invalid_usage", usage);
    }

    public String getUsage() {
        return this.usage;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTranslatedDescription() {
        return TranslationManager.get(this.description);
    }

    public String[] getAliases() {
        return this.aliases;
    }
}
