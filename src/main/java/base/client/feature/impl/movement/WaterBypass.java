package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBypass extends Module {

    int index1 =0; 
    double lastmot=0;
    private float acceleration = 0f;
    boolean lastinwater=false;
int packetticks=0;

    public static ModeSetting Mode;

    public WaterBypass() {
        super("WaterBypass", "Ускорение во время плавплва", Type.Movement);
        Mode = new ModeSetting("Mode", "PolarNew", () -> true,"PolarNew","Funtime","Matrix"
        );
        this.addSettings(Mode);
    }
    @Override
    public void onDisable() {

        super.onDisable();
    }

    @Override
    public void onEnable() {
        packetticks=0;
        index1=0; lastmot=0;
        super.onEnable();
    }

    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc= Client.instance.packet;
        double yawt = Math.toRadians(mc.player.getYRot());  yawt = Math.toRadians( MoveUtil.getdir()); double xt = -Math.sin(yawt);    double zt = Math.cos(yawt); if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }
        double pitcht = Math.toRadians( mc.player.getXRot());double yt = -Math.sin(pitcht);


            switch (Mode.getCurrentMode()) {
                case "Funtime" -> {
                    if (mc.player.isSwimming()) {
                        if (mc.player.onGround()) {
                            mc.player.jumpFromGround();
                            MoveUtil.setmotY(0.001);
                        } else if (MoveUtil.getspeed2() > 0) {
                            double speed = 0.0175;
                            MoveUtil.addmotX(xt * speed);
                            MoveUtil.addmotZ(zt * speed);
                            index1 = 1;
                        }
                    }
                }
                case "Matrix" -> {
                    if (mc.player.isInWater()) {
                        if (xt!=0 || zt!=0) {
                            double speed = mc.player.onGround() ? 0.015 : 0.0199;
                            MoveUtil.addmotX(xt * speed);
                            MoveUtil.addmotZ(zt * speed);
                            index1 = 1;
                        }
                    }
                }

                case "PolarNew" -> {
                    if(mc.player.isInWater()) {

                    if(!lastinwater){
                            MoveUtil.addmotY(0.062);
                        }else{
                            if(MoveUtil.motYstate()>0){
                                MoveUtil.addmotY(0.04);
                            }else if(MoveUtil.motYstate()<0){
                                MoveUtil.addmotY(-0.05);
                            }


                        }
                    }else if(lastinwater){
                        MoveUtil.addmotXZ(xt*0.03,zt*0.03);
                    }

                    lastinwater=mc.player.isInWater();
                }
            }

    }


    @EventTarget
    public void onUpdate(EventPreMotion e) {

            switch (Mode.getCurrentMode()) {
                case "Funtime" -> {
                    if (mc.player.isSwimming()) {
                        if (!mc.player.onGround() && MoveUtil.getspeed2() > 0) {
                            if (packetticks == 0) {
                                packetticks = 3;
                                Vec3 vec = mc.player.calculateViewVector(mc.player.getYRot(), mc.player.getXRot());
                                BlockHitResult blockHitResult = new BlockHitResult(vec, mc.player.getDirection(), mc.player.blockPosition(), true);
                                PacketHelper.Values.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
                            } else if (packetticks > 0) {
                                packetticks--;
                            }
                        }
                    }
                }
                case "Matrix" -> {
                    if (mc.player.isInWater()) {
                        PacketHelper.Values pc = Client.instance.packet;

                    }
                }

            }


//        PacketHelper.Values pc= Client.instance.packet;
//pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,true),5,true);
//
//        if (mc.player.isSwimming() && MoveUtil.getspeed2() > 0 && !mc.player.onGround() && packetticks==0) {
//            BlockPos bp=new BlockPos((int)(mc.player.getX()+mc.player.getDeltaMovement().x()),
//                    (int)(mc.player.getY()+mc.player.getDeltaMovement().y()),(int)(mc.player.getX()+mc.player.getDeltaMovement().z()));
//
//            Vec3 vec3d=mc.player.getPos().add(mc.player.calculateViewVector().multiply(1D).normalize());
//
//            BlockPos behindPlayer = new BlockPos((int) vec3d.getX(), (int) vec3d.getY(), (int) vec3d.getZ());
//
//            BlockHitResult bhr=new BlockHitResult(mc.player.calculateViewVector(mc.player.getYRot(),mc.player.getXRot()), mc.player.getDirection(),mc.player.blockPosition(),true);
//            pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,bhr,mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence()));
//
//
//
//            //   ChatHelper.addChatMessage("send packet");
//            //  BlockHitResult bhr=new BlockHitResult(mc.player.calculateViewVector(mc.player.getYRot(),mc.player.getXRot()), Direction.UP,mc.player.blockPosition(),true);
//            //  pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND,bhr,0));
//
//            //   BlockHitResult bhr2=new BlockHitResult(new Vec3(Double.NaN,Double.NaN,Double.NaN), Direction.UP,new BlockPos(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE),true);
//            //  pc.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.OFF_HAND,bhr2,0));
//            packetticks=3;
//        }else{
//            if(packetticks>0) packetticks--;
//        }
    }
}
