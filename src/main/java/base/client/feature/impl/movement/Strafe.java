package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.CombatUtil;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SlabBlock;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
public class Strafe extends Module {


    public ModeSetting Mode = new ModeSetting("Mode", "Vanilla", () -> true, "Vanilla","Matrix","Grim","None");
    public ModeSetting strafeDirection = new ModeSetting("Direction", "MoveKeys", () -> true, "Yaw","MoveKeys");

    public NumberSetting StrafePercnt = new NumberSetting("Strafe %", 100, 0, 100, 1,()-> Mode.getCurrentMode().equals("Vanilla"));
    public BooleanSetting MatrixCollide = new BooleanSetting("Matrix Collide","For 1.9+ servers", false, () -> Mode.getCurrentMode().equals("Matrix"));
    public BooleanSetting MatrixAdditional = new BooleanSetting("Matrix Additional","Additional reasons", false, () -> Mode.getCurrentMode().equals("Matrix"));

    double prevMotx=0;
    double prevMotz=0;
    double airticks;
    boolean prevStrafe=false;

    public Strafe() {
        super("Strafe", "Ты можешь стрейфиться", Type.Movement);
        addSettings(Mode,strafeDirection,StrafePercnt,MatrixCollide,MatrixAdditional);
    }


    @Override
    public void onEnable() {
        prevMotx=0; prevStrafe=false;
        prevMotz=0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }



    @EventTarget
    public void MovePost(EventOnMovePost eventMove) {
        PacketHelper.Values pc= Client.instance.packet;
        this.setSuffix(this.Mode.getCurrentMode());


        double yawt =0; float fyaw=0,playerdir=Minecraft.getInstance().player.getYRot(),mvdir=MoveUtil.getdir();
        boolean gooddir=RotationUtils.yawdiff(mvdir,pc.LastYaw)<5;
        switch (strafeDirection.getCurrentMode()){
            case ("Yaw"):
                fyaw=playerdir;
                break;
            case ("MoveKeys"):

                fyaw=mvdir==-1 ? playerdir : mvdir;

                break;
        }
        yawt = Math.toRadians(fyaw);
        if(this.Mode.getCurrentMode().equals("Grim")) {
            airticks++;



if(mc.player.isInLiquid() || mc.level.getBlockState(new BlockPos(mc.player.getBlockX(), (int) Math.floor(mc.player.getY()-0.5),mc.player.getBlockZ())).getBlock().getFriction()!=0.6F
|| Client.instance.featureManager.getModuleByClass(Speed.class).getState()  || mvdir==-1
){
    prevMotx=mc.player.getDeltaMovement().x();
    prevMotz=mc.player.getDeltaMovement().z();if(mc.player.verticalCollision && mc.player.onGround()) {airticks=0;}
     return;
}


            float deltaYaw= RotationUtils.yawdiff(mc.player.getYRot(),pc.LastYaw);





if(!mc.player.onGround() || mc.player.isUsingItem()){
    prevMotx=mc.player.getDeltaMovement().x();
    prevMotz=mc.player.getDeltaMovement().z();if(mc.player.verticalCollision && mc.player.onGround()) {airticks=0;}
    return;
}




boolean isjump=(MoveUtil.motYstateh() && gooddir && airticks>9);

double psp=Math.sqrt(prevMotx*prevMotx+prevMotz*prevMotz);

            double grimeasyvalue2=mc.player.isSprinting() ? (0.2) : 0.183;


            //     ChatHelper.addChatMessage(grimeasyvalue+" "+MoveUtil.getspeed2());

            if(isjump && mc.player.isSprinting()){
                if( MoveUtil.getspeed2()<0.41) {

                    final double speed = MoveUtil.getspeed2();
                    MoveUtil.minsmartstrafe(0.41);
                    MoveUtil.strafewdir(MoveUtil.getspeed2(), fyaw);
                    prevMotx=mc.player.getDeltaMovement().x();
                    prevMotz=mc.player.getDeltaMovement().z();if(mc.player.verticalCollision && mc.player.onGround()) {airticks=0;}
return;
                }
            }
if(psp>0.07) {
    if (MoveUtil.getspeed2() < grimeasyvalue2) {
        final double speed = MoveUtil.getspeed2();
        MoveUtil.minsmartstrafe(grimeasyvalue2);
        MoveUtil.smartstrafe(MoveUtil.getspeed2(),MoveFix.getfixedyaw(playerdir));
    }
}
           /*  if( MoveUtil.getspeed2()<grimeasyvalue2) {
                 double xt = -Math.sin(yawt);    double zt = Math.cos(yawt); if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }
      double bst=mc.player.isSprinting() ? 0.07 : 0.05;
          MoveUtil.addmotXZ(xt*bst,zt*bst);
            }*/
            prevMotx=mc.player.getDeltaMovement().x();
            prevMotz=mc.player.getDeltaMovement().z(); if(mc.player.verticalCollision && mc.player.onGround()) {airticks=0;}
        }else  if(this.Mode.getCurrentMode().equals("Vanilla")) {
        float deltaYaw= RotationUtils.yawdiff(mc.player.getYRot(),pc.LastYaw);


            double horval=(100/StrafePercnt.getValue());
        /*    double bxt=MoveUtil.getspeed2()*horval*xt; double bzt=MoveUtil.getspeed2()*horval*zt;
            MoveUtil.mult2ds(1-horval);
            MoveUtil.addmotX(bxt); MoveUtil.addmotZ(bzt);*/

            final double speed = MoveUtil.getspeed2();
MoveUtil.strafewdir(speed * horval, (float) fyaw);


        }
        else if(this.Mode.getCurrentMode().equals("Matrix")) {

            if(FastClimb.strafefastladdermatrixexempt){
                FastClimb.strafefastladdermatrixexempt=false;
                return;
            }

            double deltamove=Math.sqrt((mc.player.getDeltaMovement().x()-prevMotx)*(mc.player.getDeltaMovement().x()-prevMotx)+(mc.player.getDeltaMovement().z()-prevMotz)*(mc.player.getDeltaMovement().z()-prevMotz));
            float deltaYaw=RotationUtils.yawdiff(mc.player.getYRot(),pc.LastYaw);

            boolean diag=MoveUtil.getmf()!=0 && MoveUtil.getms()!=0;

            double diff=0;
            boolean findcoll=false;
            java.util.List<Entity> targets=getTargets();
            for (Entity target : targets) {
                if (mc.player.getBoundingBox().intersects(target.getBoundingBox())) {
                    findcoll=true;
                    break;
                }


            }






  double xt = -Math.sin(yawt);    double zt = Math.cos(yawt); if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }
            if(xt==0 && zt==0) {
                return;
            }
            float deltaYawmove=RotationUtils.yawdiff(MoveUtil.getdir(),pc.LastYaw);
boolean speedpot=false;

            if (mc.player.getActiveEffects().size()>0) {
                for(MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                    if(mobeffectinstance.getEffect().equals(MobEffects.SPEED)) {
                        speedpot=true;
                    }

                }
            }


boolean notoppos=deltaYawmove<80;



            if(


                     !LongJump.timerfromlastflaglong.hasTimeElapsed(1600,false)   ||
            (MatrixCollide.isEnabled() && findcoll)
                            || (MoveUtil.getspeed2()<0.2) || (MoveUtil.getspeed2()>0.35)

                        || (notoppos && MatrixAdditional.isEnabled() && ACUtil.ismatrixonground() && !mc.player.onGround() && !(mc.level.getBlockState(new BlockPos((int) (mc.player.getX()+mc.player.getDeltaMovement().x()), (int) (mc.player.getY()+mc.player.getDeltaMovement().y()), (int) (mc.player.getZ()+mc.player.getDeltaMovement().z()))).getBlock() instanceof AirBlock) && airticks>8  && airticks<12)

                           || ((airticks==0 && !mc.player.onGround()) && notoppos && MatrixAdditional.isEnabled())
                            ||  (mc.player.onGround() && pc.lastVeltimer.hasTimeElapsed(100,false))
                          || (mc.player.horizontalCollision && notoppos && MatrixAdditional.isEnabled())
                            || speedpot
            ) {
                prevStrafe=true;
                double horval=1;
                double bxt=MoveUtil.getspeed2()*horval*xt; double bzt=MoveUtil.getspeed2()*horval*zt;
                MoveUtil.mult2ds(1-horval);
                MoveUtil.addmotX(bxt); MoveUtil.addmotZ(bzt);


            }else {
   prevStrafe=false;
            }








            prevMotx=mc.player.getDeltaMovement().x();
            prevMotz=mc.player.getDeltaMovement().z();



            airticks++;
            if(mc.player.verticalCollision && mc.player.onGround()) {airticks=0;}
        }

    }




    java.util.List<Entity> getTargets() {

        java.util.List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(),false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null && entity.getId() != mc.player.getId() &&
                entity instanceof LivingEntity &&
                entity.isAlive() ).collect(Collectors.toList());


        return targets;
    }
}
