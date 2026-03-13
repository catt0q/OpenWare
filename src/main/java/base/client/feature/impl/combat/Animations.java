package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Animations extends Module {

    public static int spin=0;

    public static NumberSetting speed;
    public static NumberSetting spinSpeed;
    public static BooleanSetting animation,customspeed;
    public static BooleanSetting itemAnimation = new BooleanSetting("Item Animation", false, () -> true);
    public static BooleanSetting smallItem,needtarget,onlysword;
    public static ModeSetting swordAnim;
    public static ModeSetting itemAnim;

    public static NumberSetting x = new NumberSetting("X", 0, -1, 1, 0.01F, () -> swordAnim.currentMode.equals("Custom"));
    public static NumberSetting y = new NumberSetting("Y", 0, -1, 1, 0.01F, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting z = new NumberSetting("Z", 0, -1, 1, 0.01F, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting rotate = new NumberSetting("Rotate 1", 360, -360, 360, 1, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting rotate2 = new NumberSetting("Rotate 2", 0, -360, 360, 1, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting rotate3 = new NumberSetting("Rotate 3", 0, -360, 360,1, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting angle = new NumberSetting("Angle", 0, -50, 100, 1, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting scale = new NumberSetting("Scale", 1, -10, 10, 0.1F, () -> swordAnim.currentMode.equals("Custom"));;
    public static NumberSetting smooth = new NumberSetting("Smooth", 3, -10, 10, 0.1F, () -> swordAnim.currentMode.equals("Custom"));;

    public Animations() {
        super("Animations", "Добавляет анимацию на меч", Type.Visuals);
        animation = new BooleanSetting("Animation", false, () -> true);
        customspeed = new BooleanSetting("Custom Speed", true, () -> true);
        speed = new NumberSetting("Smooth Attack", 8, 1, 20, 1, () -> customspeed.isEnabled());
        spinSpeed = new NumberSetting("Spin Speed", 4, 1, 10, 1, () -> (animation.isEnabled() && swordAnim.currentMode.equals("Astolfo") || swordAnim.currentMode.equals("Spin")) || (itemAnimation.isEnabled()));
        smallItem = new BooleanSetting("Mini Item", false, () -> true);
        needtarget = new BooleanSetting("Need Target", true, () -> animation.isEnabled());
        onlysword = new BooleanSetting("Only Sword", true, () -> animation.isEnabled());
           swordAnim = new ModeSetting("Mode", "NeverHook", () -> animation.isEnabled(), "NeverHook", "Spin", "Astolfo",
                "Custom", "Neutral","Exhibition","Gothaj","Swong","Stab","Coocking");
        itemAnim = new ModeSetting("Item Animation Mode", "Spin", () -> itemAnimation.isEnabled(), "360", "Spin");
        addSettings(needtarget,animation,
                 swordAnim,
                itemAnimation,
                itemAnim,
                speed,
                spinSpeed,
                 x, y, z, rotate, rotate2, rotate3, angle, scale, smooth,
                smallItem
        );
    }

    @Override
    public void onEnable() { spin=0;   super.onEnable();  }
    @Override
    public void onDisable() {     super.onDisable();    }

    @EventTarget
    public void onUpdate(EventPreMotion event) {
        setSuffix(swordAnim.getCurrentMode());
    }

    public static int animspeed(){
        if(Client.instance.featureManager.getModuleByClass(Animations.class).getState()){
            if(customspeed.isEnabled()){
                return (int) speed.getValue();
            }

        }
           return -1;
    }

    public static void generateswing( ItemInHandRenderer instance,
                                      float p_187457_5_,
                                      float p_187457_7_,
                                      PoseStack ps,
                                      int sided,
                                      HumanoidArm humanoidArm,
                                      float p_187457_2_, float oMainHandHeight,float mainHandHeight,boolean bl2){
        if(mc.player==null) return;

        float f = -0.4F * Mth.sin(Mth.sqrt(p_187457_5_) * (float) Math.PI);
        float f1 = 0.2F * Mth.sin(Mth.sqrt(p_187457_5_) * ((float) Math.PI * 2F));
        float f2 = -0.2F * Mth.sin(p_187457_5_ * (float) Math.PI);
        int i = bl2 ? 1 : -1;
        float equipProgress = 1.0F - (oMainHandHeight + (mainHandHeight - oMainHandHeight) * p_187457_2_);
        float swingProgress = mc.player.getAttackAnim(p_187457_2_);
        final float convertedProgress = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);



        String mode = Animations.swordAnim.getCurrentMode();

        switch (mode) {
            case "NeverHook":

                transformFirstPersonItem(ps,equipProgress / 3, swingProgress);
                applyCustomTransform(ps);
                break;

            case "Coocking":
                transformFirstPersonItem(ps,0, 0);
                applyCustomTransform(ps);
                ps.translate(0.35F, 0.3F, 0.3F);
                float spinAngle = Animations.spin * 16;
                Vector3f spinAxis = new Vector3f(1, 1, 0).normalize();
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(spinAngle),
                        spinAxis.x(), spinAxis.y(), spinAxis.z()
                )));
                Animations.spin++;




                break;
            case "Spin":

                transformFirstPersonItem(ps,0, 0);
                applyCustomTransform(ps);
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(45),
                        0, 1, 0
                )));

                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(spin * Animations.spinSpeed.getValue()),
                        1, 0, 0
                )));
                spin++;
                break;
            case "Astolfo":
                Vector3f spinAxis2 = new Vector3f(0, 0, -0.1F).normalize();
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians((float) (System.currentTimeMillis() / 16 * (int) Animations.spinSpeed.getValue() % 360)),
                        spinAxis2.x(), spinAxis2.y(), spinAxis2.z()
                )));


                transformFirstPersonItem(ps,0, 0);

                applyCustomTransform(ps);
                break;

            case "Neutral":
                transformFirstPersonItem(ps,0, 0);
                applyCustomTransform(ps);
                break;

            case "Custom":
                // Кастомное вращение
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(Animations.angle.getValue()),
                        Animations.rotate.getValue(),
                        Animations.rotate2.getValue(),
                        Animations.rotate3.getValue()
                )));

                // Кастомное смещение
                ps.translate(
                        Animations.x.getValue(),
                        Animations.y.getValue(),
                        Animations.z.getValue()
                );

                transformFirstPersonItem(ps,equipProgress / Animations.smooth.getValue(), swingProgress);
                applyCustomTransform(ps);
                break;

            case "Exhibition":
                transformFirstPersonItem(ps,equipProgress / 2.0F, 0.0F);

                ps.translate(0.35F, -0.2F, -0.5F);
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(-convertedProgress * 31.0F),
                        1.0F, 0.0F, 2.0F
                )));
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(-convertedProgress * 33.0F),
                        1.5F, (convertedProgress / 1.1F), 0.0F
                )));

                applyCustomTransform(ps);
                break;

            case "Gothaj":
                transformFirstPersonItem(ps,equipProgress / 2.0F, 0.0F);

                ps.translate(0.35F, -0.2F, -0.5F);
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(-convertedProgress * 30.0F),
                        1.0F, 0.0F, 2.0F
                )));
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(-convertedProgress * 44.0F),
                        1.5F, (convertedProgress / 1.2F), 0.0F
                )));

                applyCustomTransform(ps);
                break;

            case "Swong":
                transformFirstPersonItem(ps,equipProgress / 2.0F, swingProgress);

                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(convertedProgress * 15.0F), // 30/2
                        -convertedProgress, -0.0F, 9.0F
                )));
                ps.mulPose(new Quaternionf(new AxisAngle4f(
                        (float)Math.toRadians(convertedProgress * 40.0F),
                        1.0F, -convertedProgress / 2.0F, -0.0F
                )));
                ps.translate(0.0F, 0.2F, 0.0F);

                applyCustomTransform(ps);
                break;

            case "Stab":
                float stabSpin = Mth.sin(Mth.sqrt(swingProgress) * (float)Math.PI);

                ps.translate(-0.5f, 0.3f, -0.6f + -stabSpin * 0.7);




                Vector3f axis = new Vector3f(0.0f, 0.0f, 0.1f);
                ps.mulPose(new Quaternionf().rotationAxis(
                        (float) Math.toRadians(330), // Угол в радианах
                        axis
                ));
                axis = new Vector3f(0.0f, 0.1f, 0.0f);
                ps.mulPose(new Quaternionf().rotationAxis(
                        (float) Math.toRadians(325), // Угол в радианах
                        axis
                ));
                axis = new Vector3f(0.1f, 0.0f, 0.0f);
                ps.mulPose(new Quaternionf().rotationAxis(
                        (float) Math.toRadians(350), // Угол в радианах
                        axis
                ));
                transformFirstPersonItem(ps,0.0F, 0.0f);
                applyCustomTransform(ps);
                break;
        }
    }




    private static void applyCustomTransform(PoseStack ps) {
        ps.translate(-0.5f, 0.08f, 0.0f);

        ps.mulPose(new Quaternionf(new AxisAngle4f(
                (float)Math.toRadians(20),
                0.0f, 1.0f, 0.0f
        )));
        ps.mulPose(new Quaternionf(new AxisAngle4f(
                (float)Math.toRadians(-80),
                1.0f, 0.0f, 0.0f
        )));
        ps.mulPose(new Quaternionf(new AxisAngle4f(
                (float)Math.toRadians(20),
                0.0f, 1.0f, 0.0f
        )));


        if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()) {
            if (Animations.smallItem.isEnabled()) {
                ps.scale(0.7f, 0.7f, 0.7f);
            } else if (Animations.swordAnim.currentMode.equals("Custom")) {
                float scale = Animations.scale.getValue();
                ps.scale(scale, scale, scale);
            }
        }
    }

    private boolean shouldUseCustomAnimation() {
        return Client.instance.featureManager.getModuleByClass(Animations.class).getState()
                && Animations.animation.isEnabled()
                && Client.instance.featureManager.getModuleByClass(KillAuraNew.class).getState()
                && KillAuraNew.lasttarget != null
                && mc.player.distanceTo(KillAuraNew.lasttarget) <= KillAuraNew.range.getValue();
    }


    private static void transformFirstPersonItem(PoseStack poseStack, float equipProgress, float swingProgress) {
        // Переводы остаются без изменений
        poseStack.translate(0.56f, -0.44F, -0.71999997f);
        poseStack.translate(0.0f, equipProgress * -0.6f, 0.0f);

        poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));

        final float f = Mth.sin(swingProgress * swingProgress * (float)Math.PI);
        final float f2 = Mth.sin(Mth.sqrt(swingProgress) * (float)Math.PI);

        poseStack.mulPose(Axis.ZP.rotationDegrees(f2 * -20.0f));
        if (f2 != 0) {
            Vector3f axis = new Vector3f(0.01f, 0.0f, 0.0f);
            poseStack.mulPose(new Quaternionf().rotationAxis(
                    f2 * -80.0f * ((float)Math.PI/180), // Угол в радианах
                    axis
            ));
        }

        poseStack.translate(0.4f, 0.2f, 0.2f);
    }




    private void translate(PoseStack poseStack) {
        poseStack.translate(-0.5f, 0.08f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(20.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-80.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(20.0f));

        if (Client.instance.featureManager.getModuleByClass(Animations.class).getState()) {
            if (Animations.smallItem.isEnabled()) {
                poseStack.scale(0.7f, 0.7f, 0.7f);
            } else if ("Custom".equals(Animations.swordAnim.getCurrentMode())) {
                float scale = Animations.scale.getValue();
                poseStack.scale(scale, scale, scale);
            }
        }
    }

    private void applyEquipOffset(@NotNull PoseStack matrices, HumanoidArm arm, float equipProgress) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        matrices.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }




}
