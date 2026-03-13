package base.client.feature.impl.combat.anarchyhelper;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventKeyPress;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.anarchyhelper.impl.*;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.KeyBindSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;

public class AnarchyHelper extends Module {

    int useSlot = -1488;
    int tickDelay = 0;
    KeyBindSetting[] settings;

    AnarchyHelperComponent pearl;
    AnarchyHelperComponent trapka;
    AnarchyHelperComponent aura;
    AnarchyHelperComponent dezorent;
    AnarchyHelperComponent zamorozka;
    AnarchyHelperComponent plast;
    AnarchyHelperComponent yavnayaPil;
    AnarchyHelperComponent swap;
    AnarchyHelperComponent exp;

    public BooleanSetting render = new BooleanSetting("RenderItems", true, () -> true);
    public KeyBindSetting pearlSetting = new KeyBindSetting("Перл", 0, () -> true);
    public KeyBindSetting trapkaSetting = new KeyBindSetting("Трапка", 0, () -> true);
    public KeyBindSetting auraSetting = new KeyBindSetting("Божья аура", 0, () -> true);
    public KeyBindSetting dezorentSetting = new KeyBindSetting("Дезориентация", 0, () -> true);
    public KeyBindSetting zamorozkaSetting = new KeyBindSetting("Снежок заморозка", 0, () -> true);
    public KeyBindSetting plastSetting = new KeyBindSetting("Пласт", 0, () -> true);
    public KeyBindSetting yavnayaPilSetting = new KeyBindSetting("Явная пыль", 0, () -> true);
    public KeyBindSetting swapKrushSetting = new KeyBindSetting("Сменить талисман крушителя и карателя", 0, () -> true);
    public KeyBindSetting fastExp = new KeyBindSetting("Fast Exp", 0, () -> true);

    public AnarchyHelper() {
        super("AnarchyHelper", "Помогает использовать вещи с ft-подобных анархий по биндам", Type.Combat);
        this.addSettings(pearlSetting, auraSetting, trapkaSetting, dezorentSetting, zamorozkaSetting, plastSetting,
                yavnayaPilSetting, swapKrushSetting, fastExp);

        settings = new KeyBindSetting[] { pearlSetting, auraSetting, trapkaSetting, dezorentSetting, zamorozkaSetting,
                plastSetting, yavnayaPilSetting, swapKrushSetting, fastExp };

        pearl = new EnderPearlComponent();
        trapka = new TrapkaComponent();
        aura = new GodAuraComponent();
        dezorent = new DezorentComponent();
        zamorozka = new ZamorozkaComponent();
        plast = new PlastComponent();
        yavnayaPil = new YavnayaPilComponent();
        swap = new TotemSwapComponent();
        exp = new FastEXPComponent();
    }

    @EventTarget
    public void onKey(EventKeyPress e) {
        PacketHelper.Values pc = Client.instance.packet;
        for (KeyBindSetting setting : settings) {
            if (setting.getKeyCode() == e.getKey()) {
                useSlot = switch (setting.getName()) {
                    case "Перл" -> pearl.executeItem();
                    case "Трапка" -> trapka.executeItem();
                    case "Божья аура" -> aura.executeItem();
                    case "Дезориентация" -> dezorent.executeItem();
                    case "Снежок заморозка" -> zamorozka.executeItem();
                    case "Пласт" -> plast.executeItem();
                    case "Явная пыль" -> yavnayaPil.executeItem();
                    case "Сменить талисман крушителя и карателя" -> swap.executeItem();
                    case "Fast Exp" -> exp.executeItem();
                    default -> -1488;
                };

                if (useSlot == -1337) {
                    break;
                }

                if (useSlot == -1488) {
                    ChatHelper.addTranslatedMessage("item.not_found");
                    tickDelay = 0;
                } else {
                    tickDelay = 2;
                }
                break;
            }
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (tickDelay == 0 && useSlot != -1488) {
            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, useSlot,
                    mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            mc.getConnection()
                    .send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND,
                            mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw,
                            pc.LastPitch));
            useSlot = -1488;
        } else if (tickDelay > 0) {
            tickDelay--;
        }
    }
}
