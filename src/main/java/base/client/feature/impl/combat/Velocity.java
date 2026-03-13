package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.velocities.*;
import base.client.feature.impl.combat.velocities.grim.VelocityGrim2371;
import base.client.feature.impl.combat.velocities.grim.VelocityGrim2372;
import base.client.feature.impl.movement.Flight;
import base.client.feature.impl.movement.Speed;
import base.client.feature.settings.impl.*;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import base.client.feature.Module;
public class Velocity extends Module {

    public static int ticks = 0;
    public static int polarticks=0;
    public static int skippedvelos=0;
    public static boolean modifynexts12 = false;
    public static int flagskip = 0;
    public static double prevvelmotX = 0;
    public static double prevvelmotZ = 0;
    public static Vec3 prevMot = Vec3.ZERO;
    public static TimerHelper veltimer = new TimerHelper();

    public static BooleanSetting triggerVelAfterHP = new BooleanSetting("Damage Check", false, () -> true);

    public static BooleanSetting cancelOtherDamage;
    public static ModeSetting velocityMode, GrimMode,MatrixMode,PolarMode;


    public static BooleanSetting LegitJump;
    public static ModeSetting LJM;
    public static NumberSetting LegitJJumps;
    public static NumberSetting LegitJChance;


    public static BooleanSetting DoubleReduce;
    public static ModeSetting DRM;
    public static NumberSetting LegitReduceMult;

    public static BooleanSetting LegitTimer;
    public static NumberSetting LegitTimerVal;
    public static NumberSetting LegitTimerTicks;


    public static NumberSetting revtick = new NumberSetting("Reverse Tick", 1, 0, 10, 1, () -> velocityMode.getCurrentMode().equals("Reverse"));
    public static NumberSetting revmult = new NumberSetting("Reverse Mult", 0.7f, 0f, 5f, 0.01f, () -> velocityMode.getCurrentMode().equals("Reverse"));
    public static BooleanSetting reverseStrafe = new BooleanSetting("Reverse Strafe", true, () -> velocityMode.getCurrentMode().equals("Reverse"));
    public static BooleanSetting DownMR = new BooleanSetting("LowMotionY ", true, () -> velocityMode.getCurrentMode().equals("Reverse"));

    public static NumberSetting LTimeFromKB = new NumberSetting("Time From KB", 100, 50, 2000, 1, () -> DRM.isVisible() && (DRM.getCurrentMode().equals("Simple1")));
    public static NumberSetting LTimeFromLastAttack = new NumberSetting("Time From Last Attack", 100, 50f, 2000, 1f, () -> DRM.isVisible() && (DRM.getCurrentMode().equals("Simple1") || DRM.getCurrentMode().equals("Simple2")));

    public static ModeSetting BstMode;
    public static NumberSetting MultBst;
    public static NumberSetting MotionBst;
    public static NumberSetting BstTicks;

    public static NumberSetting SimpleHor;
    public static NumberSetting SimpleVer;

    public static int jumpcounter = 0;

    public static int ticksfromkb = 0;
    public static boolean wasdamage = false;

    public Velocity() {
        super("Velocity", "Уменьшает кнокбэк при ударе", Type.Combat);
        velocityMode = new ModeSetting("Mode", "Ignore Horizontal", () -> true, "Matrix", "Reverse","BlockmcNoKB", "Ignore Horizontal", "Boost", "NoKB", "Legit", "Simple", "Grim","Polar");
        GrimMode = new ModeSetting("Grim Mode", "2.3.71", () -> velocityMode.getCurrentMode().equals("Grim"), "2.3.71","2.3.72", "Simple");
        MatrixMode = new ModeSetting("Matrix Mode", "Motion1", () -> velocityMode.getCurrentMode().equals("Matrix"), "Motion1", "Motion1", "Motion2", "Packet");

        PolarMode = new ModeSetting("Polar Mode", "Test", () -> velocityMode.getCurrentMode().equals("Polar"), "Test");

        cancelOtherDamage = new BooleanSetting("Cancel Other Damage", true, () -> true);

        SimpleHor = new NumberSetting("Simple Horizontal", 100, 0, 300, 1, () -> velocityMode.getCurrentMode().equals("Simple"));
        SimpleVer = new NumberSetting("Simple Vertical", 100, 0, 300, 1, () -> velocityMode.getCurrentMode().equals("Simple"));

        boolean boostmode = velocityMode.getCurrentMode().equals("Ignore Horizontal") || velocityMode.getCurrentMode().equals("Boost");

        BstMode = new ModeSetting("Boost Mode", "Mult", () -> boostmode, "Motion", "Mult");
        MultBst = new NumberSetting("Mult Boost", 1, 1f, 3, 0.1f, () -> boostmode && BstMode.getCurrentMode().equals("Mult"));
        MotionBst = new NumberSetting("Motion Boost", 0.3F, 0, 1, 0.01f, () -> boostmode && BstMode.getCurrentMode().equals("Motion"));
        BstTicks = new NumberSetting("Boost Ticks", 1, 1, 10, 1, () -> boostmode);


        LegitJump = new BooleanSetting("Legit Jump", true, () -> velocityMode.getCurrentMode().equals("Legit"));
        LJM = new ModeSetting("Legit Jump Chance Mode", "Jumps", () -> LegitJump.isVisible() && LegitJump.isEnabled(), "Jumps", "Chance");
        LegitJJumps = new NumberSetting("Jumps", 2, 1, 10, 1, () -> LJM.isVisible() && LJM.getCurrentMode().equals("Jumps"));
        LegitJChance = new NumberSetting("Jump chance", 50, 1, 100, 1, () -> LJM.isVisible() && LJM.getCurrentMode().equals("Chance"));


        DoubleReduce = new BooleanSetting("Double Reduce", true, () -> velocityMode.getCurrentMode().equals("Legit"));
        DRM = new ModeSetting("Double Reduce Mode", "Simple1", () -> DoubleReduce.isVisible() && DoubleReduce.isEnabled(), "Simple1", "Simple2");
        LegitReduceMult = new NumberSetting("LegitReduceMult", 0.6f, 0f, 2, 0.05f, () -> DRM.isVisible());

        LegitTimer = new BooleanSetting("Use Timer", true, () -> velocityMode.getCurrentMode().equals("Legit"));
        LegitTimerVal = new NumberSetting("Timer", 1.1f, 0.25f, 3, 0.05f, () -> velocityMode.getCurrentMode().equals("Legit") && LegitTimer.isEnabled());
        LegitTimerTicks = new NumberSetting("Timer Ticks", 1, 1, 10, 1, () -> velocityMode.getCurrentMode().equals("Legit") && LegitTimer.isEnabled());


        addSettings(velocityMode, GrimMode,MatrixMode,PolarMode, triggerVelAfterHP,
                SimpleHor, SimpleVer,
                BstMode, MotionBst, MultBst, BstTicks,

                LegitJump, LJM, LegitJJumps, LegitJChance,
                LegitTimer, LegitTimerVal, LegitTimerTicks,
                DoubleReduce, LegitReduceMult, DRM, LTimeFromKB, LTimeFromLastAttack,


                revtick, revmult, reverseStrafe, DownMR,

                cancelOtherDamage);
    }


    @Override
    public void onEnable() {
        ticks = 0;
        prevMot = Vec3.ZERO;
        jumpcounter = 0;
        flagskip = 0;
        prevvelmotX = 0;
        prevvelmotZ = 0;
        wasdamage = false;
        modifynexts12 = false;
        polarticks=0;
        skippedvelos=0;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost e) {
        String mode = velocityMode.getCurrentMode();
        if (mode.equals("Legit")) {
            e = VelocityLegit.proccesPacket(e);
        }


    }


    @EventTarget
    public void onReceivePacket(EventReceivePacketPre e) {
        PacketHelper.Values pc = Client.instance.packet;
        String mode = velocityMode.getCurrentMode();

        if (Client.instance.featureManager.getModuleByClass(Flight.class).getState()) {
            return;
        }


        if (e.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ClientboundPlayerPositionPacket s08 = (ClientboundPlayerPositionPacket) e.getPacket();
            if (flagskip > 0) {
                e.setCancelled(true);
                flagskip--;
                return;
            }
        }

        boolean iss12 = ((e.getPacket() instanceof ClientboundExplodePacket) || ((e.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()));

        if (iss12) {
            pc.lastVeltimer.reset();
            if (//Client.instance.featureManager.getModuleByClass(GodMode.class).getState() ||
                    (Client.instance.featureManager.getModuleByClass(Speed.class).getState() && Speed.Ver188MM.isVisible())

            ) {
                e.cancel();
                return;
            }
            if (mc.player.getY() < -30) {
                return;
            }
        }
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }


        if (e.getPacket() instanceof ClientboundSetHealthPacket) {
            ClientboundSetHealthPacket shu = (ClientboundSetHealthPacket) e.getPacket();
            if (shu.getHealth() < mc.player.getHealth())
                wasdamage = true;
            return;
        }


        if (cancelOtherDamage.isEnabled()) {
            if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {

                if (mc.player.getActiveEffects().size() > 0) {
                    for (MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                        if (mobeffectinstance.getEffect().equals(MobEffects.POISON) || mobeffectinstance.getEffect().equals(MobEffects.WITHER)) {
                            e.cancel();
                            return;
                        }

                    }
                }

                if (mc.player.isOnFire() || mc.player.getY() < 0) {
                    e.cancel();
                    return;
                }
            }

        }


        if (!e.isCancelled() && (wasdamage || !triggerVelAfterHP.isEnabled()) && iss12) {
            veltimer.reset();
            ticksfromkb = 0;

            wasdamage = false;

            switch (mode) {
                case ("Grim"):
                    switch (GrimMode.getCurrentMode()) {
                        case ("2.3.71"):

                            e = VelocityGrim2371.proccesPacketPre(e);

                            break;

                        case ("2.3.72"):

                            e = VelocityGrim2372.proccesPacketPre(e);

                            break;
                    }
                    break;
                case ("Polar"):
                    switch (PolarMode.getCurrentMode()) {
                        case ("Test"):
        e = VelocityPolarTest.proccesPacketPre(e);
                             break;
                    }
                    break;
                case ("Legit"):
                    e = VelocityLegit.proccesPacketPre(e);
                    break;
                case ("NoKB"):
                    if (e.getPacket() instanceof ClientboundSetEntityMotionPacket || e.getPacket() instanceof ClientboundExplodePacket) {
                        if (((ClientboundSetEntityMotionPacket) e.getPacket()).getId() == mc.player.getId()) {
                            e = VelocityNoKB.proccesPacket(e);
                        }
                    }
                    break;
                case ("Matrix"):
                    switch (MatrixMode.getCurrentMode()){
                        case ("Motion1"):
                            if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                                e = VelocityMotMatrix.proccesPacket(e, xt, zt);
                            }
                        break;
                        case ("Motion2"):
                            if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                                e = VelocityMotMatrix2.proccesPacket(e);
                            }
                            break;
                        case ("Packet"):
                            if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                                e = VelocityMatrix.proccesPacket(e);
                            }
                            break;
                    }



                    break;

                case ("BlockmcNoKB"):
                       e.cancel();
                    Input ff=mc.player.input.keyPresses;

                    pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),true,ff.sprint())));
                    pc.sendPacket(new ServerboundPlayerInputPacket( new Input(ff.forward(),ff.backward(),ff.left(),ff.right(),ff.jump(),false,ff.sprint())));

                    break;

                case ("Ignore Horizontal"):
                    if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                        e = VelocityIgnoreHorizontal.proccesPacket(e);
                    }
                    break;
                case ("Simple"):
                    if (e.getPacket() instanceof ClientboundSetEntityMotionPacket) {
                        ClientboundSetEntityMotionPacket s12 = (ClientboundSetEntityMotionPacket) e.getPacket();
                        e.setPacket(new ClientboundSetEntityMotionPacket(mc.player.getId(), new Vec3((1 - s12.getMovement().x * SimpleHor.getValue() / 100),
                                s12.getMovement().y * (1 - SimpleVer.getValue() / 100), (1 - s12.getMovement().z * SimpleHor.getValue() / 100))));
                        e.cancel();
                    }
                    break;
            }


        }

    }

    @EventTarget
    public void Tick(EventTick e) {
        String mode = velocityMode.getCurrentMode();

        switch (mode) {
            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("2.3.71"):
                        VelocityGrim2371.proccesPacketsend();
                        break;
                    case ("2.3.72"):
                        VelocityGrim2372.proccesPacketsend();
                        break;
                }
                break;
        }
}

    @EventTarget
    public void esend(EventSendPacketCancel e) {
        String mode = velocityMode.getCurrentMode();

        switch (mode) {
            case ("Grim"):
                switch (GrimMode.getCurrentMode()) {
                    case ("2.3.71"):
                       e= VelocityGrim2371.proccesPacketsend(e);
                        break;
                    case ("2.3.72"):
                        e= VelocityGrim2372.proccesPacketsend(e);
                        break;
                }
                break;
        }
    }
    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        PacketHelper.Values pc=Client.instance.packet;
        String mode = velocityMode.getCurrentMode();
        this.setSuffix(mode);
        double yawt = Math.toRadians(mc.player.getYRot());
        yawt = Math.toRadians(MoveUtil.getdir());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }
        switch (mode){
            case ("Grim"):
                switch (GrimMode.getCurrentMode()){
                    case("2.3.71"):
     VelocityGrim2371.proccesMove();
   break;
                    case("2.3.72"):
                        VelocityGrim2371.proccesMove();
                        break;

                }
                break;
            case ("Boost"):
                VelocityBoost.proccesMove(yawt,xt,zt);
                 break;
            case ("Ignore Horizontal"):
                VelocityIgnoreHorizontal.proccesMove(yawt,xt,zt);
                break;
            case ("Reverse"):
                VelocityReverse.proccesMove(yawt,xt,zt);
                break;
            case ("Legit"):
                VelocityLegit.proccesMove(yawt,xt,zt);
                break;
            case ("Polar"):
                switch (PolarMode.getCurrentMode()) {
                    case ("Test"):
                        VelocityPolarTest.proccesMove(yawt,xt,zt);
                        break;
                }
                break;
            case ("MotMatrix"):

                break;
            case ("Matrix"):
                switch (MatrixMode.getCurrentMode()){
                    case ("Motion1"):
                        VelocityMotMatrix.proccesMove();
                        break;
                }
     break;
        }





        ticksfromkb++;


    }


    public static boolean isgoodmotion() {
        if(    RotationUtils.yawdiff(MoveUtil.getdir(true), MoveUtil.getmovedir())<45) {
            return true;
        }
        return false;
    }

    public static boolean legitjump() {
        if(LJM.getCurrentMode().equals("Jumps")) {
            jumpcounter++;  if(jumpcounter>=LegitJJumps.getValue()) {
                mc.player.jumpFromGround();	 jumpcounter=0;return true; }
        }else {
            if(Math.random()*100>LegitJChance.getValue()) { 	mc.player.jumpFromGround();
           return true; }  jumpcounter=0;
        }
        return false;
    }








}
