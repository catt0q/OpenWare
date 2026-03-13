package base.client.feature.impl.movement;

import base.client.Client;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
public class TargetStrafe extends Module {

    static boolean spinright=false;
    public static boolean lastneedspoof = false;
    public static float lastyawspoof = 0;
    public static ModeSetting Mode;
    public static NumberSetting MinRange,MaxRange;
    public TargetStrafe() {
        super("TargetStrafe", "Стрейфит вокруг сущностей", Type.Movement);
        Mode = new ModeSetting("Mode", "Strafe", () -> true, "TargetCenter","Strafe");
        MinRange = new NumberSetting("Min Range", 1.5F, 0.1F, 6, 0.01F,()->Mode.getCurrentMode().equals("Strafe"));
        MaxRange = new NumberSetting("Max Range",3, 0.1F, 12, 0.01F,()->Mode.getCurrentMode().equals("Strafe"));
        addSettings(Mode,MinRange,MaxRange);
    }

    @Override
    public void onEnable() {spinright=false;
        lastneedspoof = false;
        lastyawspoof = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        lastneedspoof = false;
        lastyawspoof = 0;
        super.onDisable();
    }

    public static void updatets(Entity entity) {
        if (!Client.instance.featureManager.getModuleByClass(TargetStrafe.class).getState()) {
            lastneedspoof = false;
            lastyawspoof = 0;
            return;
        }

        switch (Mode.getCurrentMode()) {
            case "TargetCenter":
                if (entity != null) {
                    lastneedspoof = true;
                    lastyawspoof = RotationUtils.getRotationToEntity(entity).getX();
                } else {
                    lastneedspoof = false;
                    lastyawspoof = 0;
                }
                break;

                case("Strafe"):

                    if(entity!=null && entity.isAlive()) {
                        float rot2=RotationUtils.addyaw(getRotationsNoMot4(entity)[0], 180);

                        if( EntityUtil.getMinDistanceToEntity(entity,mc.player) <MaxRange.getValue() // &&

                        ) {

                            float diryaw=getRotationsNoMot4(entity)[0];
                            double bestdist=MinRange.getValue();
                            float diffyaw=90;

                            if(EntityUtil.getMinDistanceToEntity(entity,mc.player)>MinRange.getValue()) { 	  //diffyaw-=(diffyaw/2);
 diffyaw=45;
                            }
                            else if(EntityUtil.getMinDistanceToEntity(entity,mc.player)<MinRange.getValue()) {
 diffyaw=125;
                            }
                            if(spinright){
                                diffyaw*=-1;
                            }

                           // Vec3 newpos=mc.player.position().add(mc.player.getDeltaMovement().x,mc.player.getDeltaMovement().y,mc.player.getDeltaMovement().z);
                            Vec3 newpos2=mc.player.position().add(mc.player.getDeltaMovement().x,mc.player.getDeltaMovement().y,mc.player.getDeltaMovement().z);
                            double yawt = Math.toRadians(mc.player.getYRot());
                            yawt = Math.toRadians(lastyawspoof);
                            double xt = -Math.sin(yawt);
                            double zt = Math.cos(yawt);
double mult1=Math.min(MoveUtil.getspeed2()*1.1,0.8);
                            Vec3 newpos=mc.player.position().add(xt*mult1,mc.player.getDeltaMovement().y,zt*mult1);


                            BlockPos bp=new BlockPos((int) newpos.x, (int) newpos.y+1, (int) newpos.z); BlockPos bp2=new BlockPos((int) newpos.x, (int) (newpos.y+2.5), (int) newpos.z);
                            BlockState bs=Minecraft.getInstance().level.getBlockState(bp);BlockState bs2=Minecraft.getInstance().level.getBlockState(bp2);
                            Block bl=bs.getBlock(); Block bl2=bs.getBlock();

                            boolean badvoid=true;
                            for (int i = bp.getY(); i >(bp.getY()-5); i--) {
                                BlockState bs3=Minecraft.getInstance().level.getBlockState(new BlockPos(((int) newpos.x),  i, ((int) newpos.z)));
                                Block bl3=bs3.getBlock();

                                if(   !(bl3 instanceof AirBlock)
                                ) {

                                    badvoid=false;
                                            break;
                                }
                            }

                            if(   (!(bl instanceof AirBlock) || !(bl2 instanceof AirBlock))
                                    || (badvoid && !(ACUtil.matrixisvoidcheck() && CombatUtil.fallDistance>2.5)) || mc.player.horizontalCollision

                            ) {
                                spinright=!spinright;  diffyaw*=-1;
                            }
float nye=EntityUtil.getIntersectionYaw(new Vec3(mc.player.getX(),0,mc.player.getZ()),new Vec3(entity.getX(),0,entity.getZ()),MoveUtil.getspeed2(),MinRange.getValue(),spinright);
                            if(nye!=-1) {
                                lastyawspoof = nye;
                             }else {
                                lastyawspoof=RotationUtils.addyaw(diryaw, diffyaw);
                            }
                            lastneedspoof=true;
                            double mot= MoveUtil.getspeed2() ;
                        }else {
                            lastyawspoof=0;spinright=false;
                            lastneedspoof=false;
                        }


                    }else {
                        lastneedspoof=false;
                        spinright=false;
                    }

                    break;


            }
        }

        public void update(){

        }







    public static float[] getRotationsNoMot3(Entity e) {
        double yawt = Math.toRadians(RotationUtils.addyaw(e.getYRot(), 180));

        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);



        float ybest=(float) e.getY();
        float eyes=(float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        if((float) e.getBoundingBox().maxY<eyes) {
            ybest=(float) e.getBoundingBox().maxY;
        }else if((float) e.getBoundingBox().minY>eyes) {
            ybest=(float) e.getBoundingBox().minY;
        }else {
            ybest=(float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        }

        final float deltaX = (float)((e.getX() + e.getDeltaMovement().x()+xt*MinRange.getValue()) - mc.player.getX());
        final float deltaY = (float)((ybest + e.getDeltaMovement().y()) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())));
        final float deltaZ = (float)((e.getZ() + e.getDeltaMovement().z()+zt*MinRange.getValue()) - mc.player.getZ());
        final float distance = (float)(Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));
        float yaw = (float)Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float)(-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float)(90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float)(-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if(yaw<-360) {
            yaw+=360;
        }
        if(yaw>360) {
            yaw-=360;
        }

        return new float[] { yaw, pitch };
    }

    public static float[] getRotationsNoMot4(Entity e) {
        final float deltaX = (float)((e.getX()) - mc.player.getX());
        final float deltaY = (float)((e.getY()+1) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())));
        final float deltaZ = (float)((e.getZ() ) - mc.player.getZ());
        final float distance = (float)(Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));
        float yaw = (float)Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float)(-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float)(90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float)(-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if(yaw<-360) {
            yaw+=360;
        }
        if(yaw>360) {
            yaw-=360;
        }

        return new float[] { yaw, pitch };
    }

    public static float[] getRotationsMot1(Entity e) {
        final float deltaX = (float)((e.getX()+ e.getDeltaMovement().x()) - mc.player.getX()+ e.getDeltaMovement().x());
        final float deltaY = (float)((e.getY()+1+ e.getDeltaMovement().y()) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())+ e.getDeltaMovement().y()));
        final float deltaZ = (float)((e.getZ()+ e.getDeltaMovement().z() ) - mc.player.getZ()+ e.getDeltaMovement().z());
        final float distance = (float)(Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));
        float yaw = (float)Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float)(-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float)(90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float)(-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if(yaw<-360) {
            yaw+=360;
        }
        if(yaw>360) {
            yaw-=360;
        }

        return new float[] { yaw, pitch };
    }


}
