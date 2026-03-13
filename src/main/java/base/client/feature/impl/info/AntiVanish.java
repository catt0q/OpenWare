package base.client.feature.impl.info;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.AntiBot;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.BotCheckPlayer;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AntiVanish extends Module {

    public static ModeSetting Mode = new ModeSetting("Mode", "CPIUP Data", () -> true, "CPIUP Data");

    public BooleanSetting NoInVis = new BooleanSetting("Try to UnVanish", true, () -> true);

    public static ArrayDeque<BotCheckPlayer> isSpecPlayer = new ArrayDeque<>();

    ArrayDeque<lateMess> chatmess = new ArrayDeque<lateMess>();
    ArrayDeque<BotCheckPlayer> neeedcheck = new ArrayDeque<BotCheckPlayer>();

    public static TimerHelper jointimer = new TimerHelper();

    public AntiVanish() {
        super("AntiVanish", "Ищет нищих", Type.Info);
        this.addSettings(Mode);
    }

    @Override
    public void onEnable() {
        chatmess.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket() instanceof ClientboundLoginPacket) {
            isSpecPlayer.clear();
            chatmess.clear();
            neeedcheck.clear();
            jointimer.reset();
        }
        if (e.getPacket() instanceof ClientboundPlayerInfoUpdatePacket) {

            ClientboundPlayerInfoUpdatePacket p = (ClientboundPlayerInfoUpdatePacket) e.getPacket();

            if (!jointimer.hasTimeElapsed(300, false)) {
                return;
            }

            switch (Mode.getCurrentMode()) {

                case ("CPIUP Data"):

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

                                if ((pl == null)) {
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

        if (Mode.getCurrentMode().equals("CPIUP Data")) {
            if (e.getPacket() instanceof ClientboundSystemChatPacket) {
                ClientboundSystemChatPacket s01 = (ClientboundSystemChatPacket) e.getPacket();
                String ss = s01.content().getSiblings().get(0).getString();
                long diffmillis = 500;
                chatmess.add(new lateMess(ss, System.currentTimeMillis() + diffmillis));

                /*
                 * if(.contains("You cannot leave the arena.")){
                 * event.cancel();
                 * }
                 */

            }
        }

    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {

        switch (Mode.getCurrentMode()) {
            case ("CPIUP Data"):
                boolean findsss1 = false;
                while (isSpecPlayer.size() > 0 && !findsss1) {
                    if (isSpecPlayer.peekFirst().getRequiredMs() <= System.currentTimeMillis()) {
                        isSpecPlayer.pollFirst();
                    } else {
                        findsss1 = true;
                    }
                }

                boolean findssss1 = false;
                while (chatmess.size() > 0 && !findssss1) {
                    if (chatmess.peekFirst().getRequiredMs() <= System.currentTimeMillis()) {
                        chatmess.pollFirst();
                    } else {
                        findssss1 = true;
                    }
                }

                boolean findss1 = false;

                while (neeedcheck.size() > 0 && !findss1) {

                    if (neeedcheck.peekFirst().getRequiredMs() <= System.currentTimeMillis()) {
                        neeedcheck.pollFirst();
                    } else {
                        if (neeedcheck.peekFirst().getRequiredMs() - System.currentTimeMillis() > 1800) {
                            return;
                        }
                        findss1 = true;
                    }
                }
                for (BotCheckPlayer bcp : neeedcheck) {

                    for (lateMess lm : chatmess) {

                        if (lm.mess.contains(bcp.getProfile().name().toString())) {

                            neeedcheck.remove(bcp);
                        }
                    }
                }

                if (neeedcheck.size() > 0) {

                    Iterator var6 = neeedcheck.iterator();
                    while (var6.hasNext()) {

                        boolean findsdds2 = false;
                        BotCheckPlayer var7 = (BotCheckPlayer) var6.next();
                        Player pl = mc.level.getPlayerByUUID(var7.getProfile().id());
                        if (pl != null) {
                            neeedcheck.remove(var7);
                        }

                        if (isSpecList(var7.getProfile().id())) {
                            neeedcheck.remove(var7);
                            continue;
                        }
                        if (AntiBot.isBotList(var7.getProfile().id())) {
                            neeedcheck.remove(var7);
                            continue;
                        }

                        for (PlayerInfo info : mc.player.connection.getOnlinePlayers()) {
                            if (info == null) {
                                continue;
                            }
                            if (info.getProfile().id().equals(var7.getProfile().id())
                                    ||
                                    info.getProfile().name().toString()
                                            .equals(var7.getProfile().name().toString())) {
                                neeedcheck.remove(var7);
                                findsdds2 = true;
                                break;
                            }

                        }

                        List<Entity> targets1 = StreamSupport
                                .stream(mc.level.entitiesForRendering().spliterator(), false)
                                .filter(LivingEntity.class::isInstance).collect(Collectors.toList());
                        targets1 = targets1.stream().filter(entity -> entity != null && entity != mc.player &&
                                entity instanceof Player &&
                                entity.isAlive() && entity.getUUID().equals(var7.getProfile().id())

                        ).collect(Collectors.toList());
                        if (targets1.isEmpty()) {
                            if (!findsdds2) {

                                String staffStatus = Client.instance.featureManager
                                        .getModuleByClass(StaffDetector.class).getState()
                                                ? (StaffDetector.isPlayerStaff(var7.getProfile().name())
                                                        ? (ChatFormatting.RED
                                                                + TranslationManager.get("staff.is_admin"))
                                                        : ChatFormatting.GREEN
                                                                + TranslationManager.get("staff.not_admin"))
                                                : "";
                                ChatHelper.addTranslatedMessage("vanish.admin_spectating", var7.getProfile().name(),
                                        staffStatus);

                                isSpecPlayer
                                        .add(new BotCheckPlayer(var7.getProfile(), System.currentTimeMillis() + 500));
                                neeedcheck.remove(var7);
                            }
                        }

                    }

                }
                break;

        }

    }

    public static boolean isSpecList(UUID entity) {
        Iterator<BotCheckPlayer> var3 = AntiVanish.isSpecPlayer.iterator();
        while (var3.hasNext()) {
            BotCheckPlayer bot = (BotCheckPlayer) var3.next();
            if (entity == bot.getProfile().id()) {
                return true;
            }
        }

        return false;
    }

    public class lateMess {

        public long getRequiredMs() {
            return RequiredMs;
        }

        public void setRequiredMs(long requiredMs) {
            RequiredMs = requiredMs;
        }

        public String mess = "";

        public long RequiredMs = 0;

        public lateMess(String mess1, long requiredMs2) {
            mess = mess1;
            RequiredMs = requiredMs2;
        }

        public String getMess() {
            return mess;
        }

    }

}
