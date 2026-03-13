package base.client.feature.impl.info;

import base.client.feature.Module;
import base.client.event.EventTarget;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.ChatFormatting;

public class FlagDetector extends Module {
    public static ModeSetting messageSuffix = new ModeSetting("Message Suffix", "Default", () -> true, "Default",
            "FlagStep", "Detected", "Detectado", "Erkannt");
    public static BooleanSetting HideSkippedFlags = new BooleanSetting("Hide Skipped Flags", true, () -> true);
    public static BooleanSetting ShowTpPos = new BooleanSetting("Show Tp Pos", true, () -> true);
    public static BooleanSetting ShowTpId = new BooleanSetting("Show Tp Id", false, () -> true);
    public static BooleanSetting ShowFlagCount = new BooleanSetting("Show Flag Count", false, () -> true);
    public static int flagcounter = 0;

    public FlagDetector() {
        super("FlagDetector", TranslationManager.get("module.flagdetector.desc"), Type.Info);
        this.addSettings(messageSuffix, ShowTpPos, ShowTpId, HideSkippedFlags, ShowFlagCount);
    }

    @Override
    public void onEnable() {
        flagcounter = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacketPre event) {
        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket packetIn = (ClientboundPlayerPositionPacket) event.getPacket();
            if (!HideSkippedFlags.isEnabled()) {
                ChatHelper.addChatMessage(generatemessage(packetIn));
            }
        }
    }

    public static String generatemessage(ClientboundPlayerPositionPacket s08) {
        flagcounter++;
        String suffix;

        switch (messageSuffix.getCurrentMode()) {
            case ("FlagStep"):
                suffix = TranslationManager.get("flagdetector.flagstep");
                break;
            case ("Detected"):
                suffix = "Flag Detected";
                break;
            case ("Detectado"):
                suffix = "La caída detectado";
                break;
            case ("Erkannt"):
                suffix = "Rückschlag erkannt";
                break;
            default:
                suffix = TranslationManager.get("flagdetector.flag");
                break;
        }

        if (ShowTpPos.isEnabled() || ShowTpId.isEnabled() || ShowFlagCount.isEnabled()) {
            suffix += ": ";
        }
        String mess = ChatFormatting.WHITE + suffix;
        if (ShowTpPos.isEnabled()) {
            mess += ("" + ChatFormatting.GREEN + ((double) s08.change().position().x) + " " + ChatFormatting.GREEN
                    + ((double) s08.change().position().y) + " " + ChatFormatting.GREEN
                    + ((double) s08.change().position().z));
        }

        if (ShowTpId.isEnabled()) {
            mess += (" " + ChatFormatting.GREEN + s08.id());
        }
        if (ShowFlagCount.isEnabled()) {
            mess += (" " + ChatFormatting.GREEN + flagcounter);
        }

        return mess;
    }
}
