package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.motion.EventStep;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Step extends Module {

    static boolean instep = false;

    public static float stepHeight = 0.6F;

    public static TimerHelper time = new TimerHelper();
    public static NumberSetting delay;
    public static NumberSetting heightStep, timer;
    public static ModeSetting stepMode;
    public BooleanSetting reverseStep;
    public boolean jump;
    boolean resetTimer;
    int index1 = 0;
    int index2 = 0;
    int index3 = 0;
    int index4 = 0;
    int groundstate = 0;// 0=nothing 1=noground 2=ground
    double prevposY = 0;
    double prevposX = 0;
    double prevposZ = 0;

    public Step() {
        super("Step", "Автоматически взбирается на блоки", Type.Movement);
        stepMode = new ModeSetting("Mode", "MatrixMotion", () -> true, "Motion", "Vanilla", "MatrixGround",
                "MatrixMotion");
        timer = new NumberSetting("Timer", 1, 1, 10, 0.1F, () -> stepMode.getCurrentMode().equals("MatrixMotion"));

        delay = new NumberSetting("Delay", 0, 0, 1, 0.1F, () -> !stepMode.getCurrentMode().equals("MatrixGround"));
        heightStep = new NumberSetting("Height", 1F, 1, 10, 0.5F,
                () -> !stepMode.getCurrentMode().equals("MatrixGround"));
        reverseStep = new BooleanSetting("Reverse", false, () -> true);
        addSettings(stepMode, timer, heightStep, delay, reverseStep);
    }

    @Override
    public void onEnable() {
        index1 = 0;
        index2 = 0;
        index3 = 0;
        index4 = 0;
        prevposY = 0;
        prevposX = 0;
        prevposZ = 0;
        stepHeight = 0.6F;
        instep = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        instep = false;
        stepHeight = 0.6F;
        TimerUtil.reset();
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick e) {

        if (groundstate == 1) {
            mc.player.setOnGround(false);
            groundstate = 0;
        } else if (groundstate == 2) {
            mc.player.setOnGround(true);
            groundstate = 0;
        }
    }

    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (isstepping()) {
            e.cancel();
        }
        return e;
    }

    @EventTarget
    public void onStep(EventStep step) {
        String mode = stepMode.getCurrentMode();
        float delayValue = delay.getValue() * 1000;
        float stepValue = heightStep.getValue();

        // if
        // (CattoWare.instance.featureManager.getModuleByClass(NoClip.class).getState()) {
        // return; }
        double height = mc.player.getBoundingBox().minY - mc.player.getY();
        boolean canStep = height >= 0.625F;
        if (canStep) {
            time.reset();
        }
        if (resetTimer) {
            resetTimer = false;
            TimerUtil.reset();
        }
        if (mode.equalsIgnoreCase("Motion")) {
            if (mc.player.verticalCollision && !mc.options.keyJump.isDown() && time.hasReached(delayValue)) {
                stepHeight = stepValue;
            }
            if (canStep) {
                TimerUtil.setTimerspeed(height > 1 ? 0.12F : 0.4F);
                resetTimer = true;
                ncpStep(height);
            }
        } else if (mode.equalsIgnoreCase("Vanilla")) {
            stepHeight = heightStep.getValue();
        }

    }

    private void ncpStep(double height) {
        PacketHelper.Values pc = Client.instance.packet;
        double[] offset = { 0.42, 0.333, 0.248, 0.083, -0.078 };
        double posX = mc.player.getX();
        double posZ = mc.player.getZ();
        double y = mc.player.getY();
        if (height < 1.1) {
            double first = 0.42;
            double second = 0.75;
            pc.sendPacket(
                    new ServerboundMovePlayerPacket.Pos(posX, y + first, posZ, false, mc.player.horizontalCollision));
            if (y + second < y + height)
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(posX, y + second, posZ, true,
                        mc.player.horizontalCollision));
        } else if (height < 1.6) {
            for (double off : offset) {
                y += off;
                pc.sendPacket(new ServerboundMovePlayerPacket.Pos(posX, y, posZ, true, mc.player.horizontalCollision));
            }
        } else if (height < 2.1) {
            double[] heights = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869 };
            for (double off : heights) {
                pc.sendPacket(
                        new ServerboundMovePlayerPacket.Pos(posX, y + off, posZ, true, mc.player.horizontalCollision));
            }
        } else {
            double[] heights = { 0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907 };
            for (double off : heights) {
                pc.sendPacket(
                        new ServerboundMovePlayerPacket.Pos(posX, y + off, posZ, true, mc.player.horizontalCollision));
            }
        }
    }

    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (stepMode.getCurrentMode().equals("MatrixMotion")) {
            if (index2 == 1) {

                if (index1 == 1) {
                    MoveUtil.setmotY(0.334);

                }
                if (index1 == 2) {
                    MoveUtil.setmotY(0.246);

                }
                if (index1 == 3) {
                    MoveUtil.setmotY(0);
                    MoveUtil.limit2speed(0.08);
                    TimerUtil.setTimerspeed(1);

                }
                if (index1 == 4) {
                    MoveUtil.ssmartstrafe(mc.player.isUsingItem() ? 0.1 : 0.198);
                }
                if (index1 == 5) {
                    instep = false;
                    index2 = 0;
                }
                index1++;

            }

            if (mc.player.verticalCollision &&
                    mc.player.onGround() &&
                    (index1 > 3 || index2 == 0) &&
                    MoveUtil.getdir() != -1) {
                float startfloat = mc.player.getYRot();
                if (MoveUtil.getdir() != -1) {
                    startfloat = MoveUtil.getdir();
                }
                double yawt = Math.toRadians(startfloat);
                double xt = -Math.sin(yawt);
                double zt = Math.cos(yawt);
                // if(MoveUtil.getdir()==-1) {xt=0; zt=0; }

                double mot = 0.4D;

                if (

                mc.level.getBlockState(
                        new BlockPos(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ())).isAir()
                        && checki(mc.player.getX(), mc.player.getBlockY(), mc.player.getZ())

                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) (mc.player.getY() + 1.4), (int) Math.floor((mc.player.getZ() + zt * mot))))
                                .isAir()
                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) (mc.player.getY() + 2.1), (int) Math.floor((mc.player.getZ() + zt * mot))))
                                .isAir()
                // &&
                // true

                ) {
                    index2 = 1;
                    index1 = 1;
                    MoveUtil.setmotY(0.42);
                    MoveUtil.limit2speed(0.2);
                    TimerUtil.setTimerspeed(timer.getValue());
                    instep = true;

                }

            }

        }

    }

    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        String mode = stepMode.getCurrentMode();

        this.setSuffix(mode);
        if (mc.player.getDeltaMovement().y > 0) {
            jump = true;
        } else if (mc.player.onGround() || groundstate == 2) {
            jump = false;
        }

        if (stepMode.getCurrentMode().equals("MatrixGround")) {

            if (reverseStep.isEnabled() && !mc.options.keyJump.isDown() && !mc.player.onGround()
                    && mc.player.getDeltaMovement().y < 0 && mc.player.fallDistance < 1F && !jump) {
                MoveUtil.setmotY(-0.999);
            }
            float startfloat = mc.player.getYRot();
            if (MoveUtil.getdir() != -1) {
                startfloat = MoveUtil.getdir();
            }
            double yawt = Math.toRadians(startfloat);
            double xt = -Math.sin(yawt);
            double zt = Math.cos(yawt);
            if (MoveUtil.getdir() == -1) {
                xt = 0;
                zt = 0;
            }
            double mot = 0.4D;

            if (mc.player.verticalCollision &&
                    mc.player.onGround() &&
                    CombatUtil.fallDistance >= -0.01 &&

                    MoveUtil.getdir() != -1) {
                if (

                mc.level.getBlockState(
                        new BlockPos(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ())).isAir()
                        && checki(mc.player.getX(), mc.player.getBlockY(), mc.player.getZ())

                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) (mc.player.getY() + 1.4), (int) Math.floor((mc.player.getZ() + zt * mot))))
                                .isAir()
                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) (mc.player.getY() + 2.1), (int) Math.floor((mc.player.getZ() + zt * mot))))
                                .isAir()
                // &&
                // true

                ) {

                    index2 = 0;
                    index1 = 1;
                    prevposY = mc.player.getY() + 1;
                    prevposX = mc.player.getX() + xt * mot;
                    prevposZ = mc.player.getZ() + zt * mot;

                    if (!mc.options.keyJump.isDown()) {
                        mc.player.jumpFromGround();
                    }
                    if (!mc.player.isSprinting())
                        mc.player.setSprinting(true);
                    // mc.timer.timerSpeed=1.1f;
                    index4 = 0;
                } else if (mc.level
                        .getBlockState(new BlockPos(mc.player.getBlockX(), (int) Math.floor(mc.player.getY()),
                                mc.player.getBlockZ()))
                        .isAir() &&
                        !(mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) Math.floor((mc.player.getY() + 1.4)),
                                (int) Math.floor((mc.player.getZ() + zt * mot)))).isAir())
                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) Math.floor((mc.player.getY() + 2.1)),
                                (int) Math.floor((mc.player.getZ() + zt * mot)))).isAir()
                        && mc.level.getBlockState(new BlockPos((int) Math.floor((mc.player.getX() + xt * mot)),
                                (int) Math.floor((mc.player.getY() + 3.1)),
                                (int) Math.floor((mc.player.getZ() + zt * mot)))).isAir()

                        && true

                ) {
                    index2 = 1;
                    index1 = 1;
                    prevposY = mc.player.getY() + 2;
                    prevposX = mc.player.getX() + xt * mot;
                    prevposZ = mc.player.getZ() + zt * mot;
                    if (!mc.options.keyJump.isDown()) {
                        mc.player.jumpFromGround();
                    }
                    if (!mc.player.isSprinting())
                        mc.player.setSprinting(true);
                    index4 = 0;
                }

            } else if (index1 > 0) {

                instep = true;
                if (index2 == 0) {
                    if (!mc.player.onGround() && index1 == 1 && ACUtil.ismatrixonground()) {
                        event.setOnGround(true);
                        MoveUtil.setmotY(0.42);
                        groundstate = 2;

                    }

                    index1++;
                } else if (index2 == 1) {

                    if (index1 == 2 && ACUtil.ismatrixonground()) {
                        event.setOnGround(true);
                        MoveUtil.setmotY(0.42);
                        groundstate = 2;

                    }

                    index1++;
                }

            }

            if ((mc.player.getY() >= prevposY || index1 >= 15 || (mc.player.verticalCollision && index1 > 1))
                    && index4 == 0) {
                index1 = 0;
                instep = false;
                TimerUtil.reset();
                MoveUtil.minssmartstrafe(0.15);
                index4 = 1;
            }

        }

    }

    boolean checki(double x, double y, double z) {
        float startfloat = mc.player.getYRot();
        if (MoveUtil.getdir() != -1) {
            startfloat = MoveUtil.getdir();
        }
        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        double mot = 0.4D;

        BlockPos bp = new BlockPos((int) Math.floor((x + xt * mot)), (int) Math.floor(y),
                (int) Math.floor((z + zt * mot)));
        BlockState bs = mc.level.getBlockState(bp);
        Block bl = bs.getBlock();
        if ( /*
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y , z+zt*mot)).isAir()) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y , z+zt*mot)).getBlock()
              * instanceof BlockSlab) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y , z+zt*mot)).getBlock()
              * instanceof BlockStairs) &&
              * ! (mc.level.getBlockState(new BlockPos(x+xt*mot, y, z+zt*mot)).getBlock()
              * instanceof BlockSign) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y, z+zt*mot)).getBlock()
              * instanceof BlockPressurePlate) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y, z+zt*mot)).getBlock()
              * instanceof BlockFlower) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y, z+zt*mot)).getBlock()
              * instanceof BlockTallGrass) &&
              * !(mc.level.getBlockState(new BlockPos(x+xt*mot, y, z+zt*mot)).getBlock()
              * instanceof BlockButton)
              */
        !(bs.isAir()) && bs.isCollisionShapeFullBlock(Minecraft.getInstance().level, new BlockPos(bp))) {
            return true;
        }

        return false;
    }

    public static boolean isstepping() {

        return instep;
    }

}
