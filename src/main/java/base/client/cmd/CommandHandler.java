package base.client.cmd;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventMessage;
import base.client.helpers.impl.misc.ChatHelper;

public class CommandHandler {

    public CommandManager commandManager;

    public CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @EventTarget
    public void onMessage(EventMessage event) {
        String msg = event.getMessage();
        if (msg.startsWith(".")) {
            event.setCancelled(true);

            // @obf-only start
            base.client.auth.TamperResponse.checkC();
            if (base.client.auth.TamperResponse.shouldDisable()) {
                return;
            }
            // @obf-only end

            if (!this.commandManager.execute(msg)) {
                ChatHelper.addTranslatedMessage("cmd.unknown");
            }
        }
    }
}