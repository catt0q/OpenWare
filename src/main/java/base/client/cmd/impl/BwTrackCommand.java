package base.client.cmd.impl;

import base.client.cmd.CommandAbstract;
import base.client.event.EventManager;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BwTrackCommand extends CommandAbstract {

    private static final Pattern ARENA_PATTERN = Pattern.compile("\\bbw-(\\d{1,4})\\b", Pattern.CASE_INSENSITIVE);

    private final TimerHelper requestTimer = new TimerHelper();
    private final TimerHelper joinTimer = new TimerHelper();

    private boolean waitingArena = false;
    private boolean waitingJoin = false;
    private String pendingPlayer;
    private String pendingArena;

    public BwTrackCommand() {
        super("bwtrack", "cmd.bwtrack.desc",
                "§6.bwtrack" + ChatFormatting.LIGHT_PURPLE + " §3<player>",
                "bwtrack");

        // needs packet/tick events
        EventManager.register(this);
    }

    @Override
    public void execute(String... arguments) {
        if (arguments.length != 2) {
            usage();
            return;
        }
        if (mc.player == null) {
            return;
        }

        pendingPlayer = arguments[1];
        pendingArena = null;
        waitingArena = true;
        waitingJoin = false;
        requestTimer.reset();

        mc.player.connection.sendChat("/bw pspectate " + pendingPlayer);
        ChatHelper.addChatMessage(ChatFormatting.GRAY + "Tracking " + ChatFormatting.AQUA + pendingPlayer
                + ChatFormatting.GRAY + "...");
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        if (!waitingArena || mc.player == null) {
            return;
        }
        // basic timeout so random BW messages don't trigger later
        if (requestTimer.hasTimeElapsed(7000, false)) {
            waitingArena = false;
            pendingPlayer = null;
            pendingArena = null;
            ChatHelper.addChatMessage(ChatFormatting.RED + "bwtrack timed out");
            return;
        }

        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundSystemChatPacket chatPacket)) {
            return;
        }

        String raw = chatPacket.content().getString();
        if (raw == null || raw.isEmpty()) {
            return;
        }

        // Only react to the specific "arena not started" response.
        // Example (RU): "Игра на арене BW-39 ещё не началась"
        String lower = raw.toLowerCase();
        boolean looksLikeBwNotStarted = lower.contains("bw-")
                && (lower.contains("игра на арене") || lower.contains("game") || lower.contains("arena"))
                && (lower.contains("не началась") || lower.contains("not started") || lower.contains("hasn't started")
                        || lower.contains("has not started"));
        if (!looksLikeBwNotStarted) {
            return;
        }

        // This server response usually looks like: "Игра на арене BW-39 ещё не началась"
        Matcher m = ARENA_PATTERN.matcher(raw);
        if (!m.find()) {
            return;
        }

        pendingArena = "BW-" + m.group(1);
        waitingArena = false;
        waitingJoin = true;
        joinTimer.reset();

        ChatHelper.addChatMessage(ChatFormatting.GRAY + "Found arena " + ChatFormatting.GOLD + pendingArena
                + ChatFormatting.GRAY + ", joining...");
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (!waitingJoin || mc.player == null || pendingArena == null) {
            return;
        }
        if (!joinTimer.hasTimeElapsed(1000, false)) {
            return;
        }

        waitingJoin = false;
        mc.player.connection.sendChat("/bw rjoin " + pendingArena);

        pendingPlayer = null;
        pendingArena = null;
    }
}
