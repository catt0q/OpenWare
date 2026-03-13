package base.client.feature.impl.info;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.hud.Notifications;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.managers.TranslationManager;
import base.client.helpers.utils.TimerHelper;
import base.client.ui.notification.NotificationManager;
import base.client.ui.notification.NotificationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

public class StaffDetector extends Module {

    private int index = 0;
    private int lastAdmCount = 0;
    private final Set<String> map = new HashSet();
    private static List<String> shotbowadmins = new ArrayList<>(
            Arrays.asList("0xCAFEFE", "Mistri", "Axyy", "lazertester", "Robertthegoat",
                    "97WaterPolo", "aet2505", "Galap", "McJeffr", "Mistri", "halowars91",
                    "Hilevi", "Pyachi2002", "Rafiki2085", "Red_Epicness", "_Silver",
                    "InstantLightning", "JACOBSMILE", "JonnyDvE", "kbsfe", "ru555e11",
                    "Selictove", "wmn", "sellejz", "Agypagy", "BasicAly", "Carrots386",
                    "DJ_Pedro", "FullAdmin", "ImbC", "JTGangsterLP6", "M4bi", "Mistri",
                    "MrJack", "GunOverdose", "pigplayer", "Pyachi2002", "Outra",
                    "Rinjani", "Sevy13", "SnowVi1liers", "naqare", "ACrispyTortilla", "Hughzaz",
                    "Moshyn", "Navarr", "ShadowLAX", "Brxnton", "ImAbbyy", "lPirlo", "Jarool", "Bupin",
                    "Xhat", "EnderMCx", "LangScott", "WTDpuddles", "Daggez", "TurtleCobra", "OrcaHedral"));

    private static List<String> forsadmins = new ArrayList<>(Arrays.asList("TimoG03", "MelodicBlaze", "interlina",
            "002zrxs9fq", "kot_1220", "xentrazwq", "69shymx7nq", "kairaqqw", "quedasae", "P0dp1vaSn1kkk",
            "sdfqqqqee_df", "Zaximiiss", "quedasae", "seqwissehell", "sausagge_OMG228", "Mikasaaz", "holyrevived",
            "qwertaim", "BestMark", "VapeShelf_", "1gn0re", "recentinfusion", "skyqwwrrs", "shxmure", "Jewaxskava",
            "чурка", "murdesst", "sycxphant", "BestDan", "1Kynntydynpkynby", "aweings", "wernerut", "saydexxs_",
            "01Artes_", "Sauron1385", "auf_Papiros", "liwyToo", "Emma", "KA-R-GA", "Blada", "Lerbim", "afina",
            "Mr_Kilir", "_Ambiguity_", "ItsRazum", "Rodion8920", "quelis_X", "l0nd0nmcc", "_BaNanaaaaaaaa_",
            "timofeybigdickov",
            "xueta5k", "kasai555", "dumplingmutit", "interdsciplinary", "delaytheeruption", "frvctal", "Cold_RemToxic",
            "your__tyanka___", "ego1liv", "4eRHa9I_CMeRTb", "06phasmophobia6", "AQW_ygaHVVAzkxk", "Aquariiuss",
            "vsolws", "TITANS", "131202", "bredz", "Lunuyac", "bloodcompetition", "_BLoOd_DeMoN_", "countesspainrose",
            "Reverine", "Whitte112", "6ENZZZO", "agitazya", "~WarDen~", "Uglystephan_loh", "broodmxther", "youshimura",
            "YOUTOP", "DeliciousCandy77",

            "deepintrusion", "PROFESIONAL2014", "potoke_smex", "yoniXD", "Zeoltsider", "myrieals", "778800"));

    private static List<String> mladmins = new ArrayList<>(
            Arrays.asList("TimoG03", "mirwiqon", "vano123", "_BuRdAlAk_"));

    public static final List<String> stafflist = new ArrayList<>();

    private TimerHelper timer = new TimerHelper();

    static ModeSetting ListMode = new ModeSetting("List Mode", "ShotBow", () -> true, "Saved", "ShotBow", "ForsCraft",
            "MLegacy");
    ModeSetting UpdateMode = new ModeSetting("Update Mode", "OnUpdate", () -> true, "Delay", "OnUpdate", "None");
    NumberSetting Delay = new NumberSetting("Delay", 1000.0F, 0.0F, 10000.0F, 100.0F,
            () -> (this.UpdateMode.getCurrentMode().equals("Delay")));

    BooleanSetting AlertNickNames = new BooleanSetting("Alert NickNames", true,
            () -> !UpdateMode.getCurrentMode().equals("None"));
    BooleanSetting AlertDecreasing = new BooleanSetting("Alert Decreasing", true,
            () -> UpdateMode.getCurrentMode().equals("OnUpdate"));

    BooleanSetting Sound = new BooleanSetting("Sound Alert", false, () -> !UpdateMode.getCurrentMode().equals("None"));
    BooleanSetting Notification = new BooleanSetting("Notification Alert", false,
            () -> !UpdateMode.getCurrentMode().equals("None"));
    BooleanSetting Chat = new BooleanSetting("Chat Alert", true, () -> !UpdateMode.getCurrentMode().equals("None"));

    public StaffDetector() {
        super("StaffDetector", "Искать админов", Type.Info);
        this.addSettings(ListMode, UpdateMode, Delay, AlertDecreasing, AlertNickNames, Sound, Notification, Chat);
    }

    @EventTarget
    public void onReceivePacket(EventTick event) {
        if (mc.player != null) {

            List<String> checklist = getStaffList();

            this.map.clear();
            for (PlayerInfo playerListEntry : mc.getConnection().getOnlinePlayers()) {

                for (String admin : checklist) {
                    if (admin == null)
                        continue;
                    if (!playerListEntry.getProfile().name().contains(admin))
                        continue;

                    this.map.add(admin);
                }
            }
            String ss = "";
            if (AlertNickNames.isEnabled() && this.map.size() > 0) {
                for (String admin : map) {
                    ss += ChatFormatting.LIGHT_PURPLE + admin + ChatFormatting.RESET;
                    ss += " | ";
                }
                ss = ss.substring(0, ss.length() - 2);
            }

            if (UpdateMode.getCurrentMode().equals("OnUpdate")
                    && (AlertDecreasing.isEnabled() ? lastAdmCount != this.map.size()
                            : lastAdmCount < this.map.size())) {

                if (Chat.isEnabled()) {
                    ChatHelper.addTranslatedMessage("admin.count", this.map.size());
                }
                if (Notification.isEnabled()) {
                    if (Notifications.state.isEnabled()) {
                        NotificationManager.publicity("Admin count:", this.map.size() + "", 2,
                                this.map.size() == 0 ? NotificationType.WARNING : NotificationType.SUCCESS);
                    }
                }
                if (AlertNickNames.isEnabled() && this.map.size() > 0) {
                    if (Chat.isEnabled()) {
                        ChatHelper.addTranslatedMessage("admin.list", ss);
                    }
                    if (Notification.isEnabled()) {
                        if (Notifications.state.isEnabled()) {
                            NotificationManager.publicity("Admins detected: ", ss, 2,
                                    this.map.size() == 0 ? NotificationType.WARNING : NotificationType.SUCCESS);
                        }
                    }

                }

            }

            // ChatHelper.addChatMessage(mc.getConnection().getOnlinePlayers().size()+"
            // "+this.map.size());

            if (UpdateMode.getCurrentMode().equals("Delay")) {
                if (this.timer.hasTimeElapsed((long) Delay.getValue(), false)) {
                    ++this.index;
                    if (this.index >= checklist.size()) {
                        if (this.map.isEmpty()) {

                            if (Chat.isEnabled()) {
                                ChatHelper.addTranslatedMessage("admin.none_found");
                            }
                            if (Notification.isEnabled()) {
                                if (Notifications.state.isEnabled()) {
                                    NotificationManager.publicity("No Admins", "", 2, NotificationType.SUCCESS);
                                }
                            }

                        } else {

                            if (Chat.isEnabled()) {
                                ChatHelper.addTranslatedMessage("admin.count", this.map.size());
                            }
                            if (Notification.isEnabled()) {
                                if (Notifications.state.isEnabled()) {
                                    NotificationManager.publicity("Admin count: ", this.map.size() + "", 2,
                                            NotificationType.SUCCESS);
                                }
                            }
                            if (AlertNickNames.isEnabled()) {
                                if (Chat.isEnabled()) {
                                    ChatHelper.addTranslatedMessage("admin.list", ss);
                                }
                                if (Notification.isEnabled()) {
                                    if (Notifications.state.isEnabled()) {
                                        NotificationManager.publicity("Admins detected: ", ss, 2,
                                                this.map.size() == 0 ? NotificationType.WARNING
                                                        : NotificationType.SUCCESS);
                                    }
                                }

                            }

                        }
                        this.map.clear();
                        this.index = 0;
                    }
                    this.timer.reset();
                }
            }
            lastAdmCount = this.map.size();
        }

    }

    public static List<String> getStaffList() {

        List<String> checklist = new ArrayList<>();

        switch (ListMode.getCurrentMode()) {
            case ("ShotBow"):
                checklist = shotbowadmins;
                break;
            case ("ForsCraft"):
                checklist = forsadmins;
                break;
            case ("MLegacy"):
                checklist = mladmins;
                break;
            case ("Saved"):
                checklist = stafflist;
                break;

        }
        return checklist;
    }

    public static boolean isPlayerStaff(String name) {
        List<String> list = getStaffList();
        return list.contains(name);
    }

}
