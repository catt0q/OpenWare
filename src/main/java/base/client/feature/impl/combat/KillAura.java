package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventOnMouseLeftClick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendSprintUpdatePacket;
import base.client.event.events.impl.render.EventRenderGui;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.rotation.Rotation;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import base.client.helpers.utils.TimerHelper;
import lombok.Getter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class KillAura extends Module {

    @Getter
    private LivingEntity target;

    public float lastYaw;
    public float lastPitch;

    private float lyd;
    private float lpd;

    private float nextFallDistance = 0f;
    private int ticks;
    private long index2 = 0L;

    private final TimerHelper timerHelper = new TimerHelper();

    ModeSetting mode = new ModeSetting("Mode", "Polar", () -> true, "Polar", "Intave", "TestNoise", "Vanilla");
    NumberSetting fluctuateSpeed = new NumberSetting("FluctuateSpeed", 5, 0.1f, 20, 1, () -> true);

    ModeSetting clickStyle = new ModeSetting("ClickStyle", "1.9+", () -> true, "1.9+", "1.8-");

    BooleanSetting randomizeFallDistance = new BooleanSetting("Randomize fall distance", false, () -> true);
    BooleanSetting forceCritIfBlindness = new BooleanSetting("Force crit if blindness", true, () -> true);
    BooleanSetting onlyCrit = new BooleanSetting("Only Crit", false, () -> true);

    NumberSetting minCps = new NumberSetting("minCps", 5, 1, 20, 1,
            () -> clickStyle.getCurrentMode().equalsIgnoreCase("1.8-"));
    NumberSetting maxCps = new NumberSetting("maxCps", 15, 1, 20, 1,
            () -> clickStyle.getCurrentMode().equalsIgnoreCase("1.8-"));

    public KillAura() {
        super("KillAura", "Автоматически бьет и целиться на противника ", Type.Combat);
        this.addSettings(mode, fluctuateSpeed, randomizeFallDistance, forceCritIfBlindness, onlyCrit, clickStyle, minCps, maxCps);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastYaw = mc.player.getYRot();
        lastPitch = mc.player.getXRot();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
    }

    @EventTarget
    public void onRender(EventRenderGui e) {
        assert mc.player != null;
        assert mc.level != null;
        if (clickStyle.getCurrentMode().equalsIgnoreCase("1.8-")) {
            if (timerHelper.hasTimeElapsed(index2, true)) {
                ticks++;
            }
        }
    }

    @EventTarget
    public void onTick(EventTick e) {
        // @obf-only start
        base.client.auth.TamperResponse.checkF();
        if (base.client.auth.TamperResponse.shouldDisable()) {
            target = null;
            return;
        }
        // @obf-only end

        assert mc.player != null;
        assert mc.level != null;
        updateTarget();

        if (target == null) {
            return;
        }

        updateVariables();
        updateRotation();
    }

    @EventTarget
    public void onClick(EventOnMouseLeftClick e) {
        assert mc.player != null;
        assert mc.level != null;

        if (target == null) {
            return;
        }
        switch (clickStyle.getCurrentMode()) {
            case ("1.9+"):
                if (canHit()) {
                    nextFallDistance = randomizeFallDistance.getState() ? random.nextFloat(0.5f) : 0.0F;
                    attackTarget();

                }
                break;
            case ("1.8-"):
                if (ticks == 0) {
                    break;
                }

                for (int i = 0; i < ticks; i++) {
                    attackTarget();
                    index2 = updateDelay(index2);
                }
                ticks = 0;
                break;
        }
    }

    private void attackTarget() {
        if (mc.player == null || mc.gameMode == null || target == null) {
            return;
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    @EventTarget
    public void onSprint(EventSendSprintUpdatePacket e) {
        assert mc.player != null;
        assert mc.level != null;
        if (clickStyle.getCurrentMode().equalsIgnoreCase("1.9+")) {
            if (canHit()) {
                mc.player.setSprinting(false);
            }
        }
    }

    @EventTarget
    public void onMotion(EventPreMotion e) {
        if (target != null) {
            e.setYaw(lastYaw);
            e.setPitch(lastPitch);
        }
    }

    @EventTarget
    public void onLook(EventLook e) {
        if (target != null) {
            e.setYaw(lastYaw);
            e.setPitch(lastPitch);
        }
    }

    @EventTarget
    public void onMoveFix(EventOnMove e) {
        if (target != null) {
            e.setYaw(lastYaw);
        }
    }

    @EventTarget
    public void onJump(EventOnJump e) {
        if (target != null) {
            e.setYaw(lastYaw);
        }
    }

    private void updateTarget() {
        assert mc.player != null;
        assert mc.level != null;

        target = null;

        float bestFov = Float.MAX_VALUE;

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player == mc.player || Client.instance.friendManager.isFriend(player.getName().getString())) {
                continue;
            }

            if (Client.instance.friendManager.isFriend(player.getName().getString())) {
                continue;
            }

            final double blockDistance = mc.player.getEyePosition().distanceTo(player.getEyePosition());

            if (blockDistance > 6) {
                continue;
            }

            float fov = Math.abs(RotationUtils.getFovToEntity(player));

            if (fov >= bestFov) {
                continue;
            }

            target = player;
            bestFov = fov;
        }
    }

    private void updateRotation() {
        assert mc.player != null;
        assert mc.level != null;

        switch (mode.getCurrentMode()) {
            case "Polar" -> {
                /// Получение уменьшенного хит бокса для
                /// 1. ротация может думать что попадает, а рей каст майнкрафта почему-то думает
                /// что ты не попадешь
                /// 2. Помогает в обходе, так как ты не будешь целиться в края реального
                /// хитбокса, а рядом с центром
                AABB box = target.getBoundingBox().contract(0.1, 0.1, 0.1);

                /// Использую Mth.wrapDegrees() для того чтобы нормально работал неарест хит век
                float normalizedLastYaw = Mth.wrapDegrees(lastYaw);
                float normalizedLastPitch = Mth.wrapDegrees(lastPitch);

                /// Тут идет сканирование всех уголов хитбокса, чтобы с их помошью найти
                /// минимальный и максимальный yaw и pitch при которых ты все еще попадешь по
                /// хит боксу
                /// (Нужно для неарест хит века)
                Vec3[] points = {
                        new Vec3(box.minX, box.minY, box.minZ),
                        new Vec3(box.maxX, box.minY, box.minZ),
                        new Vec3(box.minX, box.maxY, box.minZ),
                        new Vec3(box.minX, box.minY, box.maxZ),
                        new Vec3(box.maxX, box.maxY, box.minZ),
                        new Vec3(box.maxX, box.minY, box.maxZ),
                        new Vec3(box.minX, box.maxY, box.maxZ),
                        new Vec3(box.maxX, box.maxY, box.maxZ)
                };

                List<Rotation> rotations = Arrays.stream(points)
                        .map(RotationUtils::getRotationToPos)
                        .toList();

                Rotation minRotation = new Rotation(
                        (float) rotations.stream().mapToDouble(Rotation::getX).min().orElse(normalizedLastYaw),
                        (float) rotations.stream().mapToDouble(Rotation::getY).min().orElse(normalizedLastPitch));

                Rotation maxRotation = new Rotation(
                        (float) rotations.stream().mapToDouble(Rotation::getX).max().orElse(normalizedLastYaw),
                        (float) rotations.stream().mapToDouble(Rotation::getY).max().orElse(normalizedLastPitch));

                /// + random.nextFloat(-fluctuateSpeed.getValue(), fluctuateSpeed.getValue()
                /// Используется для как бы нойза,
                /// так как нужная ротация вычисляется без векторов, а по Math.clamp от
                /// нынешней,
                /// и по этому рандом в текущей ротации сохранится в следующей ротации
                /// и таким образом ротация будет выглядеть как небольшой Basic рандом который
                /// иногда двигается в случайные части тела
                Rotation randomOffset = new Rotation(
                        random.nextFloat(-fluctuateSpeed.getValue(), fluctuateSpeed.getValue()),
                        random.nextFloat(-fluctuateSpeed.getValue(), fluctuateSpeed.getValue()));

                if (MoveUtil.getspeed3() == 0) {
                    randomOffset.setX(0);
                    randomOffset.setY(0);
                }

                Rotation needRotation = new Rotation(
                        Math.clamp(normalizedLastYaw + randomOffset.getX(), minRotation.getX(), maxRotation.getX()),
                        Math.clamp(normalizedLastPitch + randomOffset.getY(), minRotation.getY(), maxRotation.getY()));

                Rotation delta = new Rotation(
                        Mth.wrapDegrees(needRotation.getX() - normalizedLastYaw + lyd * 0.5f),
                        Mth.wrapDegrees(needRotation.getY() - normalizedLastPitch + lpd * 0.5f));

                Rotation speed = new Rotation(
                        10 + random.nextFloat(15), /// Если использовать random.nextFloat или любой другой next, то если
                                                   /// вводить только 1 аргумент, будет рандомное значение от 0 до
                                                   /// твоего аргумента
                        5 + random.nextFloat(15));

                delta.setX(Math.clamp(delta.getX(), -speed.getX(), speed.getX()));
                delta.setY(Math.clamp(delta.getY(), -speed.getY(), speed.getY()));

                float gcd = RotationUtils.getMouseGCD();

                delta.setX(Math.round(delta.getX() / gcd) * gcd);
                delta.setY(Math.round(delta.getY() / gcd) * gcd);

                lyd = delta.getX();
                lpd = delta.getY();

                lastYaw += delta.getX(); /// = Mth.wrapDegrees(lastYaw + delta.getX());
                /// использовать закомментированный пример не стоит, потому что, если твой
                /// нынешний yaw -179.9f
                /// а ты повернулся на пару градусов влево (на градус примерно 179.0f),
                /// то сервер может подумать что разница между углами -179.9f и 179.0f равна
                /// около 360
                /// и это может сайлент флагаться
                lastPitch = Math.clamp(lastPitch + delta.getY(), -90, 90);
            }
            case "Vanilla" -> {
                Vec3 eyePos = mc.player.getEyePosition();
                AABB hitAABB = target.getBoundingBox().contract(0.1, 0.1, 0.1);

                Vec3 perfectVec = new Vec3(
                        Math.clamp(eyePos.x, hitAABB.minX, hitAABB.maxX),
                        Math.clamp(eyePos.y, hitAABB.minY, hitAABB.maxY),
                        Math.clamp(eyePos.x, hitAABB.minZ, hitAABB.maxZ));

                Rotation needRotation = RotationUtils.getRotation(perfectVec);

                Rotation delta = new Rotation(
                        Mth.wrapDegrees(needRotation.getX() - lastYaw),
                        Mth.wrapDegrees(needRotation.getY() - lastPitch));

                float gcd = RotationUtils.getMouseGCD();

                delta.setX(Math.round(delta.getX() / gcd) * gcd);
                delta.setY(Math.round(delta.getY() / gcd) * gcd);

                lastYaw += delta.getX();
                lastPitch = Math.clamp(lastPitch + delta.getY(), -90, 90);
            }
        }
    }

    private void updateVariables() {

    }

    public long updateDelay(long index2) {

        int currentCps = Math.round((float) 1000 / index2);

        int nextCps = currentCps + (random.nextBoolean() ? -1 : 1);
        nextCps = (int) Math.clamp(nextCps, minCps.getValue(), maxCps.getValue());

        return 1000L / nextCps;
    }

    private boolean canCrit() {
        if (mc.player == null) {
            return false;
        }

        return CombatUtil.fallDistance > nextFallDistance && !mc.player.onGround()
                && !mc.player.onClimbable() && !mc.player.hasEffect(MobEffects.BLINDNESS)
                && !mc.player.isInWater()
                && !mc.player.isVehicle()
                || (mc.player.hasEffect(MobEffects.BLINDNESS) && forceCritIfBlindness.getState());
    }

    private boolean canHit() {
        assert mc.player != null;
        assert mc.level != null;

        boolean cooldownReady = mc.player.getAttackStrengthScale(0.5f) > 0.9;
        if (!cooldownReady) {
            return false;
        }

        // If Only Crit is enabled, only attack when crit conditions are met.
        return !onlyCrit.isEnabled() || canCrit();
    }

}
