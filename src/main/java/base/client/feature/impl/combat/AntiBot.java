package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.BotCheckPlayer;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.TimerHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AntiBot extends Module {

    public ModeSetting Mode = new ModeSetting("Mode", "MatrixZero", () -> true, "MatrixZero", "MatrixUUID");

    public BooleanSetting MCK = new BooleanSetting("Matrix Check Combat", false,
            () -> Mode.getCurrentMode().equals("MatrixZero") || Mode.getCurrentMode().equals("MatrixUUID"));

    public ModeSetting NMode = new ModeSetting("Notify Mode", "Notification", () -> true, "Notification", "Chat",
            "None");

    public BooleanSetting DeleteBot = new BooleanSetting("Delete Bot", false, () -> true);

    public static List<Entity> isBotPlayer = new ArrayList<>();

    ArrayDeque<BotCheckPlayer> neeedcheck = new ArrayDeque<BotCheckPlayer>();

    public static TimerHelper jointimer = new TimerHelper();

    public AntiBot() {
        super("AntiBot", "Автоматически удаляет(исключает бота)", Type.Combat);
        this.addSettings(
                Mode, MCK, DeleteBot, NMode);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        isBotPlayer.clear();
        neeedcheck.clear();
        jointimer.reset();
        super.onEnable();
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket() instanceof ClientboundLoginPacket) {
            isBotPlayer.clear();
            neeedcheck.clear();
            jointimer.reset();
        }
        if (e.getPacket() instanceof ClientboundTabListPacket) {

        }
        if (e.getPacket() instanceof ClientboundPlayerInfoUpdatePacket) {

            ClientboundPlayerInfoUpdatePacket p = (ClientboundPlayerInfoUpdatePacket) e.getPacket();

            switch (Mode.getCurrentMode()) {
                case ("MatrixZero"):
                    if (!jointimer.hasTimeElapsed(1000, false)
                            || (pc.lastC02timer.hasTimeElapsed(1500, false) && MCK.isEnabled())) {
                        return;
                    }

                    Iterator var2 = p.newEntries().iterator();

                    ClientboundPlayerInfoUpdatePacket.Entry entry;

                    while (var2.hasNext()) {
                        entry = (ClientboundPlayerInfoUpdatePacket.Entry) var2.next();

                        Iterator var5 = p.actions().iterator();

                        while (var5.hasNext()) {
                            ClientboundPlayerInfoUpdatePacket.Action action = (ClientboundPlayerInfoUpdatePacket.Action) var5
                                    .next();
                            if (action.equals(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                                Player pl = mc.level.getPlayerByUUID(entry.profileId());
                                if (entry.gameMode().equals(GameType.SURVIVAL) && entry.displayName() != null
                                        && entry.displayName().getSiblings().size() == 0) {
                                    long diffmillis = 2000;
                                    neeedcheck.add(new BotCheckPlayer(entry.profile(),
                                            System.currentTimeMillis() + diffmillis));
                                }

                            }

                        }

                    }

                    break;

                case ("MatrixUUID"):
                    if (!jointimer.hasTimeElapsed(1000, false)
                            || (pc.lastC02timer.hasTimeElapsed(1500, false) && MCK.isEnabled())) {
                        return;
                    }

                    Iterator var22 = p.newEntries().iterator();

                    ClientboundPlayerInfoUpdatePacket.Entry entry2;

                    while (var22.hasNext()) {
                        entry2 = (ClientboundPlayerInfoUpdatePacket.Entry) var22.next();

                        Iterator var5 = p.actions().iterator();

                        while (var5.hasNext()) {
                            ClientboundPlayerInfoUpdatePacket.Action action = (ClientboundPlayerInfoUpdatePacket.Action) var5
                                    .next();
                            if (action.equals(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                                Player pl = mc.level.getPlayerByUUID(entry2.profileId());
                                if (entry2.gameMode().equals(GameType.SURVIVAL)
                                        && (entry2.profile().id().toString().contains("FakePlayer")
                                                || entry2.profile().id().toString().contains("OfflinePlayer"))) {
                                    long diffmillis = 2000;
                                    neeedcheck.add(new BotCheckPlayer(entry2.profile(),
                                            System.currentTimeMillis() + diffmillis));
                                }

                            }

                        }

                    }

                    break;

            }

        }

    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {

        switch (Mode.getCurrentMode()) {
            case ("MatrixZero"), ("MatrixUUID"):
                boolean findss1 = false;
                while (neeedcheck.size() > 0 && !findss1) {
                    if (neeedcheck.peekFirst().getRequiredMs() <= System.currentTimeMillis()) {
                        neeedcheck.pollFirst();
                    } else {
                        findss1 = true;
                    }
                }
                List<Entity> targets1 = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                        .filter(LivingEntity.class::isInstance).collect(Collectors.toList());
                targets1 = targets1.stream().filter(entity -> entity != null && entity != mc.player &&
                        entity instanceof Player &&
                        entity.isAlive() && entity.tickCount > 0 && entity.tickCount < 120

                ).collect(Collectors.toList());

                if (!targets1.isEmpty() && neeedcheck.size() > 0) {

                    targets1.sort(Comparator.comparingDouble(entity -> entity.distanceTo(mc.player)));
                    for (int i = 0; i < targets1.size(); i++) {
                        Player target = (Player) targets1.get(i);
                        boolean findsdds = false;
                        boolean findsdds2 = false;
                        for (int qi = 0; qi < AntiBot.isBotPlayer.size(); qi++) {
                            if (isBotPlayer.get(qi) != null && isBotPlayer.get(qi).getId() == target.getId()) {
                                findsdds = true;
                                break;
                            }
                        }
                        Iterator var6 = neeedcheck.iterator();
                        while (var6.hasNext()) {
                            BotCheckPlayer var7 = (BotCheckPlayer) var6.next();

                            if (target.getUUID().equals(var7.getProfile().id())) {
                                findsdds2 = true;
                                break;
                            }

                        }
                        if (findsdds || !findsdds2) {
                            break;
                        }
                        int armors = EntityUtil.getArmorCount(target);

                        if (armors == 4) {
                            proccesbot(target);
                        }
                    }
                }
                break;

        }

    }

    public static boolean isBotList(UUID entity) {
        Iterator<Entity> var3 = AntiBot.isBotPlayer.iterator();
        while (var3.hasNext()) {
            Entity bot = (Entity) var3.next();
            if (entity.equals(bot.getUUID())) {
                return true;
            }
        }

        return false;
    }

    private void proccesbot(Entity target) {

        neeedcheck.clear();
        isBotPlayer.clear();
        if (DeleteBot.isEnabled()) {
            mc.level.removeEntity(target.getId(), Entity.RemovalReason.KILLED);
        } else {
            isBotPlayer.add(target);
        }
        switch (NMode.getCurrentMode()) {
            case ("Chat"):
                ChatHelper.addTranslatedMessage(DeleteBot.isEnabled() ? "bot.deleted" : "bot.neutralized",
                        target.getDisplayName().getString());
                break;
            case ("Notification"):
                NotificationManager.publicity("Bot " + target.getDisplayName().getString(),
                        (DeleteBot.isEnabled() ? "Deleted" : "Neutralized"), 5, NotificationType.WARNING);

                break;

        }

    }

}
