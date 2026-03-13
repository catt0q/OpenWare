package base.client.managers;

import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.anarchyhelper.*;
import base.client.feature.impl.hud.*;
import base.client.feature.impl.minigames.AntiFireball;
import base.client.feature.impl.minigames.BWAutoLeave;
import base.client.feature.impl.minigames.BWJoinHelper;
import base.client.feature.impl.movement.*;
import base.client.feature.impl.client.*;
import base.client.feature.impl.combat.*;
import base.client.feature.impl.misc.*;
import base.client.feature.impl.player.*;
import base.client.feature.impl.exploit.*;
import base.client.feature.impl.info.*;
import base.client.feature.impl.visual.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleManager {

    public CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<>();

    public ModuleManager() {
        initModules();
    }

    public void initModules() {
        if (!modules.isEmpty()) {
            modules.clear();
        }
        // final KillAura killAura = new KillAura(); modules.add(killAura);
        KillAuraNew killAura = new KillAuraNew();
        modules.add(killAura);
        modules.add(new TargetStrafe());
        modules.add(new Flight());
        modules.add(new MoveFix());
        modules.add(new TestModule());
        modules.add(new RoundedCornersTest());
        modules.add(new PacketLimitTest());
        modules.add(new ClientSounds());
        // modules.add(new TargetESP(killAura));
        modules.add(new Optimizator());
        modules.add(new NoSlow());

        modules.add(new FastClimb());
        modules.add(new PacketFixer());
        modules.add(new Disabler());
        modules.add(new CollideBoost(killAura));
        modules.add(new WaterBypass());
        modules.add(new NotificationTEST());
        modules.add(new BackRotate(killAura));
        modules.add(new InvMove());
        modules.add(new Speed());
        modules.add(new Blink());
        modules.add(new FastLatency());
        modules.add(new NoDelay());
        modules.add(new MoreKB());
        modules.add(new OldScaffold());
        // modules.add(new MovementCorrection(killAura));
        modules.add(new TriggerBot());
        modules.add(new FlagDetector());
        modules.add(new HUD());
        modules.add(new HighJump());
        modules.add(new Timer());
        modules.add(new SelfDamage());
        modules.add(new FastSlabs());
        modules.add(new FastClick());
        modules.add(new AirStuck());
        modules.add(new LongJump());
        modules.add(new Strafe());
        modules.add(new Notifications());
        modules.add(new Velocity());
        modules.add(new NoFall());
        modules.add(new TeleportBack());
        modules.add(new KeepSprint());
        modules.add(new Sprint());
        modules.add(new FastUse());
        modules.add(new ChatSettings());
        modules.add(new AntiBot());
        modules.add(new Phase());
        modules.add(new FastBow());
        modules.add(new FastBreak());
        modules.add(new AutoJump());
        modules.add(new AirJump());
        modules.add(new AutoGapple());
        modules.add(new GodMode());
        modules.add(new FakeItem());
        modules.add(new NoWeb());
        modules.add(new Criticals());
        modules.add(new VoidBouncer());
        modules.add(new MoreSwing());
        modules.add(new AlliesNoHit());
        modules.add(new Zoot());
        modules.add(new SwordBlockHit());
        modules.add(new AutoTool());
        modules.add(new NoSlowBreak());
        modules.add(new AutoArmor());
        modules.add(new ChestStealer());
        modules.add(new BWJoinHelper());
        modules.add(new BWAutoLeave());
        modules.add(new HandDerp());
        modules.add(new Spammer());
        modules.add(new FreeCam());
        modules.add(new AntiPush());
        modules.add(new AntiVanish());
        modules.add(new AutoRespawn());
        modules.add(new Step());
        modules.add(new NoRender());
        modules.add(new ClientRotations());
        modules.add(new Brightness());
        modules.add(new Animations());
        modules.add(new AntiServerItemSwap());
        modules.add(new NoRotate());
        // modules.add(new Effekseer());
        // modules.add(new KillEffect());
        modules.add(new ChatNotifier());
        modules.add(new BetterFightSound());
        modules.add(new StaffDetector());
        modules.add(new AntiFireball());
        modules.add(new PersonViewer());
        modules.add(new CameraNoClip());
        modules.add(new DesyncFix());
        modules.add(new ClientSyncFix());
        modules.add(new FastSneak());
        modules.add(new Ambience());
        modules.add(new Breaker());

        modules.add(new EntityESP());
        modules.add(new Watermark());
        modules.add(new TargetHUD());
        // modules.add(new ViaFabricPlusFix());
        modules.add(new CombatTargets());
        modules.add(new SmartReselect());
        modules.add(new Scaffold());
        modules.add(new SilentGui());
        modules.add(new AntiExploit());
        modules.add(new CommandSettings());
        modules.add(new ChatBypass());
        modules.add(new Reach());
        modules.add(new AutoClicker());
        modules.add(new NoClip());
        modules.add(new Eagle());
        modules.add(new AntiVoid());
        modules.add(new Glide());

        // new modules from meteor/trouser-streak
        modules.add(new AutoTotem());
        modules.add(new AttributeSwap());
        modules.add(new MaceKill());
        modules.add(new SpearKill());
        modules.add(new WaypointCoordExploit());

        modules.add(new ClickGui());

    }

    public List<Module> getModuleList() {
        return this.modules;
    }

    public List<Module> getModulesForCategory(Type category) {
        List<Module> featureList = new ArrayList<>();
        for (Module feature : getModuleList()) {
            if (feature.getType() == category) {
                featureList.add(feature);
            }
        }
        return featureList;
    }

    public Module getModuleByClass(Class<? extends Module> classFeature) {
        for (Module feature : getModuleList()) {
            if (feature != null && classFeature.isAssignableFrom(feature.getClass())) {
                return feature;
            }
        }

        // Debug logging for missing module
        System.err.println("[DEBUG] getModuleByClass returned null for: " + classFeature.getName());
        Thread.dumpStack(); // optional: prints the call stack to see who requested it

        return null;
    }

    public Module getModuleByLabelNoSpace(String name) {
        if (name.isEmpty()) {
            return null;
        }
        String nname = name.replaceAll(" ", "");
        for (Module feature : getModuleList()) {
            if (feature.getLabel().replaceAll(" ", "").toLowerCase().equals(nname.toLowerCase())) {
                return feature;
            }
        }
        return null;
    }

    public Module getModuleByLabel(String name) {
        for (Module feature : getModuleList()) {
            if (feature.getLabel().toLowerCase().equals(name.toLowerCase())) {
                return feature;
            }
        }
        return null;
    }

    public Module getModuleByPartialName(String partialName) {
        if (partialName == null || partialName.isEmpty()) {
            return null;
        }

        partialName = partialName.toLowerCase();
        Module bestMatch = null;
        int bestScore = Integer.MIN_VALUE;

        for (Module module : getModuleList()) {
            String moduleName = module.getLabel().replaceAll(" ", "").toLowerCase();

            // Вычисляем "очки" совпадения
            int score = 0;

            // Проверяем, содержит ли имя модуля частичный запрос
            if (moduleName.contains(partialName)) {
                score = partialName.length(); // Чем длиннее совпадение, тем лучше
            }

            // Дополнительные критерии можно добавить здесь:
            // - Совпадение в начале слова дает больше очков
            // - Полное совпадение дает максимальные очки
            if (moduleName.equals(partialName)) {
                score = Integer.MAX_VALUE; // Абсолютное совпадение
            } else if (moduleName.startsWith(partialName)) {
                score += 10; // Бонус за совпадение в начале
            }

            // Если текущий модуль имеет лучший результат, сохраняем его
            if (score > bestScore) {
                bestScore = score;
                bestMatch = module;
            }
        }

        return bestScore > 0 ? bestMatch : null; // Возвращаем null если совпадений нет
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    private int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

}