package base.client.feature.impl.misc;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class PacketLimitTest extends Module {

    private final NumberSetting packets = new NumberSetting("Packets", 100, 1, 1000, 1, () -> true);
    private final NumberSetting ticks = new NumberSetting("Ticks", 20, 1, 100, 1, () -> true);

    private int currentTick = 0;
    private int packetsSent = 0;
    private double testPosX, testPosY, testPosZ;
    private float testYaw, testPitch;
    private boolean testStarted = false;

    public PacketLimitTest() {
        super("PacketLimitTest", "Tests server packet rate limits", Type.Misc);
        addSettings(packets, ticks);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) {
            setState(false);
            return;
        }

        // Capture exact current position
        PacketHelper.Values pc = Client.instance.packet;
        testPosX = pc.LastPosX;
        testPosY = pc.LastPosY;
        testPosZ = pc.LastPosZ;
        testYaw = pc.LastYaw;
        testPitch = pc.LastPitch;

        currentTick = 0;
        packetsSent = 0;
        testStarted = true;

        int totalPackets = (int) packets.getValue();
        int totalTicks = (int) ticks.getValue();
        int packetsPerTick = totalPackets / totalTicks;

        NotificationManager.publicity("PacketLimitTest",
            "Starting test: " + totalPackets + " packets over " + totalTicks + " ticks (" + packetsPerTick + " pkt/tick)",
            3, NotificationType.INFO);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        testStarted = false;

        if (packetsSent > 0) {
            NotificationManager.publicity("PacketLimitTest",
                "Test complete: Sent " + packetsSent + " packets over " + currentTick + " ticks",
                3, NotificationType.SUCCESS);
        }
    }

    @EventTarget
    public void onPreMotion(EventPreMotion e) {
        if (!testStarted) return;

        // Force player to stand still at test position
        e.setPosX(testPosX);
        e.setPosY(testPosY);
        e.setPosZ(testPosZ);
        e.setYaw(testYaw);
        e.setPitch(testPitch);
        e.setOnGround(mc.player.onGround());

        // Cancel motion
        mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
    }

    @EventTarget
    public void onTick(EventTick e) {
        if (!testStarted) return;

        PacketHelper.Values pc = Client.instance.packet;
        int totalPackets = (int) packets.getValue();
        int totalTicks = (int) ticks.getValue();

        if (currentTick >= totalTicks) {
            // Test complete
            setState(false);
            return;
        }

        // Calculate packets to send this tick
        int remainingPackets = totalPackets - packetsSent;
        int remainingTicks = totalTicks - currentTick;
        int packetsThisTick = (remainingTicks > 0) ? (int) Math.ceil((double) remainingPackets / remainingTicks) : remainingPackets;

        // Send packets for this tick
        for (int i = 0; i < packetsThisTick && packetsSent < totalPackets; i++) {
            pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(
                testPosX, testPosY, testPosZ, testYaw, testPitch, mc.player.onGround(), false
            ), 10, true);
            packetsSent++;
        }

        currentTick++;
    }
}
