package base.client.cmd;

import base.client.cmd.impl.*;
import base.client.event.EventManager;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final ArrayList<Command> commands = new ArrayList<>();

    public CommandManager() {
        commands.add(new BindCommand());
        commands.add(new ConfigCommand());
        commands.add(new FriendCommand());
        commands.add(new HideCommand());
        commands.add(new UnHideCommand());
        commands.add(new SetCommand());
        commands.add(new ToggleCommand());
        commands.add(new MacroCommand());
        commands.add(new ReloadCommand());
        commands.add(new GpsCommand());
        commands.add(new CheckCommand());
        commands.add(new ReInitCommand());
        commands.add(new RenameCommand());
        commands.add(new SayCommand());
        commands.add(new PingCommand());
        commands.add(new LangCommand());
        commands.add(new WpExploitCommand());
        commands.add(new HelpCommand());
        commands.add(new VClipCommand());
        commands.add(new HClipCommand());
        commands.add(new CloudCommand());
        commands.add(new BwTrackCommand());
        EventManager.register(new CommandHandler(this));

    }

    public List<Command> getCommands() {
        return this.commands;
    }

    public boolean execute(String args) {
        String noPrefix = args.substring(1);
        String[] split = noPrefix.split(" ");
        if (split.length > 0) {
            for (Command command : commands) {
                CommandAbstract abstractCommand = (CommandAbstract) command;
                String[] commandAliases = abstractCommand.getAliases();
                for (String alias : commandAliases) {
                    if (split[0].equalsIgnoreCase(alias)) {
                        abstractCommand.execute(split);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
