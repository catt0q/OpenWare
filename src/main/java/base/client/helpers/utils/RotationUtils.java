package base.client.helpers.utils;


import base.client.helpers.Helper;
import base.client.helpers.impl.math.DeltaRotation;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;

public class RotationUtils implements Helper {

   public static float lastyaw=0,lastpitch=0;
    public static boolean lockedrots=false;

    public static boolean lockedRots(){
        return lockedrots;
    }


    public static float[] getRotationVector(Vec3 vec, boolean randomRotation, float yawRandom, float pitchRandom, float speedRotation) {
        Vec3 eyesPos = mc.player.getEyePosition();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - (mc.player.getY() + mc.player.getEyeHeight() + 0.5);
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float randomYaw = 0;
        if (randomRotation) {
            randomYaw = MathematicHelper.randomizeFloat(-yawRandom, yawRandom);
        }
        float randomPitch = 0;
        if (randomRotation) {
            randomPitch = MathematicHelper.randomizeFloat(-pitchRandom, pitchRandom);
        }

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f) + randomYaw;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ))) + randomPitch;

        float yaw2=yaw; float pitch2=pitch;
            yaw = ACUtil.GCDFix( mc.player.getYRot(),mc.player.getYRot(), yaw2, pitch2)[0];
            pitch = ACUtil.GCDFix( mc.player.getYRot(),  mc.player.getYRot(), yaw2, pitch2)[1];

       pitch = Math.clamp(pitch, -90F, 90F);
        yaw = RotationUtils.updateRotation(mc.player.getYRot(), yaw, speedRotation);
        pitch = RotationUtils.updateRotation(mc.player.getXRot(), pitch, speedRotation);

        return new float[]{yaw, pitch};
    }

    public static float updateRotation(float current, float newValue, float speed) {
        float f = MathematicHelper.wrapDegrees(newValue - current);
        if (f > speed) {
            f = speed;
        }
        if (f < -speed) {
            f = -speed;
        }
        return current + f;
    }







    public static float lerpAngle(float delta, float start, float end) {
        start = Mth.wrapDegrees(start);
        end = Mth.wrapDegrees(end);
 float shortestAngle = Mth.wrapDegrees(end - start);
    return start + delta * shortestAngle;
    }



    public static float todeffyaw(float yaw) {
        //180 � -180 ��� 0 � 360
        //0 ��� 180
        float yawn=yaw;
        if(yawn<0) {
            yawn*=-1;
            yawn+=180;
        }else {
            yawn=360-180-yaw;
        }
        if(yawn<0) {
            yawn*=-1;
            yawn=yawn%360;
            yawn=360-yawn;
        }
        return yawn%360;
    }

    public static float tomineyaw(float yaw) {
        yaw=mody(yaw);
        if(yaw<0) {
            yaw=360-(yaw*-1);
        }
        float yawn=yaw;
        if(yaw<=180) {
            yawn=180-yaw;
        }
        else {
            yawn=yaw-180;
            yawn*=-1;
        }


        return yawn%360;
    }

    public static float getRotToPos(double PosX,double PosZ) {
        final float deltaX = (float)(PosX  -  Minecraft.getInstance().player.getX());

        final float deltaZ = (float)(PosZ  -  Minecraft.getInstance().player.getZ());


        final float distance = (float)(Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));

        float yaw = (float)Math.toDegrees(-Math.atan(deltaX / deltaZ));

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

        return yaw;
    }

    public static float getFovToEntity(Entity entity) {
        double diffX = entity.getX() - mc.player.getX();
        double diffZ = entity.getZ() - mc.player.getZ();

        return Mth.wrapDegrees((float) (toDegrees(atan2(diffZ, diffX)) - 90) - mc.player.getYRot());
    }

    public static Rotation getRotationToEntity(Entity entity) {
        double diffX = entity.getX() - mc.player.getX();
        double diffY = entity.getY() - mc.player.getY();
        double diffZ = entity.getZ() - mc.player.getZ();

        double distance = Math.sqrt(mc.player.distanceToSqr(diffX, diffY, diffZ));

        float yaw = (float) Mth.wrapDegrees(toDegrees(atan2(diffZ, diffX)) - 90);
        float pitch = (float) -toDegrees(atan2(diffY, distance));

        return new Rotation(
                yaw,
                (float) toDegrees(pitch)
        );
    }


    public static Rotation getRotationToPos(Vec3 pos) {
        Vec3 eyes = mc.player.getEyePosition();
        Vec3 diff = pos.subtract(eyes);

        double distance = eyes.distanceTo(pos);

        float yaw = (float) Mth.wrapDegrees(toDegrees(atan2(diff.z, diff.x)) - 90);
        float pitch = (float) -toDegrees(atan2(diff.y, diff.horizontalDistance()));

        return new Rotation(
                yaw,
                pitch
        );
    }

    public static float pitchdiff(float pitch1,float pitch2) {
        return Math.abs(pitch1-pitch2);
    }
    //OLD    public static float yawdiff(float yaw1,float yaw2) { 	return yawdeffdiff(todeffyaw(yaw1),todeffyaw(yaw2)); }
    //New
    public static double yawdiff(double yaw1, double yaw2) {
        float yaw360 = (float)(Math.abs(yaw1 - yaw2) % 360.0D);
        if (yaw360 > 180.0F) {
            yaw360 = 360.0F - yaw360;
        }
        return (double)yaw360;
    }
    public static float yawdiff(float yaw1, float yaw2) {
        float yaw360 = (float)(Math.abs(yaw1 - yaw2) % 360.0D);
        if (yaw360 > 180.0F) {
            yaw360 = 360.0F - yaw360;
        }
        return yaw360;
    }

    public static float getMouseGCD() {
        double d = mc.options.sensitivity().get() * (double)0.6F + (double)0.2F;
        double e = d * d * d;
        double f = e * (double)8.0F;
        return (float) f * 0.15F;
    }

    public static float yawdeffdiff(float yaw1,float yaw2) {
        yaw1=mody(yaw1);
        if(yaw1<0) {
            yaw1=360-(yaw1*-1);
        }
        yaw2=mody(yaw2);
        if(yaw2<0) {
            yaw2=360-(yaw2*-1);
        }

        float ans1=Math.abs(yaw1-yaw2);
        float fyawto360=Math.min(360-yaw1,yaw1);
        float syawto360=Math.min(360-yaw2,yaw2);
        float ans2=Math.abs(fyawto360+syawto360);

        float ans=Math.min(ans1,ans2);

        return ans;
    }


    public static float addpitch(float pitch1,float pitch2,float value,boolean over) {
        float absvalue=Math.abs(value);

        if(pitch1>pitch2) {
            if(over && pitch1-value<pitch2) {
                pitch1=pitch2;
            }else {
                pitch1-=value;
            }
        }else {
            if(over && pitch1+value>pitch2) {
                pitch1=pitch2;
            }else {
                pitch1+=value;
            }
        }
        return pitch1;
    }







    public static float addyaw(float yaw1,float yaw2,float value,boolean over,boolean rotlimitfix) {
        float resultuaw=tomineyaw(adddeffyaw(todeffyaw(yaw1),todeffyaw(yaw2),value,over));

        float lastyawdiff=RotationUtils.yawdiff(yaw1, resultuaw);              if(Math.abs(RotationUtils.yawdiff(addyaw(yaw1, lastyawdiff),resultuaw))>0.001  ) {    lastyawdiff*=-1;  }


        if(rotlimitfix) {
            return (yaw1-lastyawdiff);
        }else {
            return tomineyaw(adddeffyaw(todeffyaw(yaw1),todeffyaw(yaw2),value,over));
        }
    }


    public static float addyaw(float yaw1,float yaw2,float value,boolean over) {
        return tomineyaw(adddeffyaw(todeffyaw(yaw1),todeffyaw(yaw2),value,over));
    }
    public static float addyaw(float yaw1, float value ) {
        return tomineyaw(adddeffyaw(todeffyaw(yaw1) ,value ));
    }

    public static float addyawWithDeffArg(float yaw1,float yaw2,float value,boolean over) {
        return tomineyaw(adddeffyaw(yaw1,yaw2,value,over));
    }
    public static float addyawWithDeffArg(float yaw1, float value ) {
        return tomineyaw(adddeffyaw(yaw1, value ));
    }

    public static float adddeffyaw(float yaw1,float yaw2,float value,boolean over) {
        yaw1=mody(yaw1);
        if(yaw1<0) {
            yaw1=360-(yaw1*-1);
        }
        yaw2=mody(yaw2);
        if(yaw2<0) {
            yaw2=360-(yaw2*-1);
        }

        value=mody(value);

        if(over && yawdeffdiff(yaw1,yaw2)<=value) {
            return yaw2;
        }

        if(yaw1>=yaw2) {
            float ans1=Math.abs(yaw1-yaw2);
            float fyawto360=360-yaw1;
            if(yaw1<=180) {
                fyawto360=yaw1;
            }
            float syawto360=360-yaw2;
            if(yaw2<=180) {
                syawto360=yaw2;
            }

            float ans2=Math.abs(fyawto360+syawto360);
            float ans=0;
            if(ans1<=ans2) {
                ans=yaw1-value;
                ans=mody(ans);
                if(ans<0) {
                    ans=360-(ans*-1);
                }
                return ans;
            }else {

                ans=yaw1+value;
                ans=mody(ans);
                if(ans<0) {
                    ans=360-(ans*-1);
                }
                return ans;
            }
        }else {
            float ans1=Math.abs(yaw1-yaw2);
            float fyawto360=360-yaw1;
            if(yaw1<=180) {
                fyawto360=yaw1;
            }
            float syawto360=360-yaw2;
            if(yaw2<=180) {
                syawto360=yaw2;
            }
            float ans2=Math.abs(fyawto360+syawto360);
            float ans=0;


            if(ans1<ans2) {
                ans=yaw1+value;
                ans=mody(ans);
                if(ans<0) {
                    ans=360-(ans*-1);
                }
                return ans;
            }else {
                ans=yaw1-value;
                ans=mody(ans);
                if(ans<0) {
                    ans*=-1;
                    ans=360-ans;
                }
                return ans;
            }
        }
    }



    public static float adddeffyaw(float yaw1,float value) {
        yaw1=mody(yaw1);
        if(yaw1<0) {
            yaw1=360-(yaw1*-1);
        }
        value=mody(value);
        float ans=yaw1+value;
        ans=mody(ans);
        if(ans<0) {
            ans=360-(ans*-1);
        }
        return ans%360;
    }




    public static float mody(float yaw) {
        boolean min=yaw<0;
        float ans=yaw%360;
        if(min) {
            return ans;
        }
        else {
            return ans;
        }

    }

    public static boolean canVectorBeSeen(Vec3 vector)
    {
        Vec3 vec3d = mc.player.getEyePosition();
        return !(mc.level.clip(new ClipContext(vec3d, vector, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, mc.player)).getType() == HitResult.Type.BLOCK);
   }

    public static Rotation getRotation(Vec3 to) {
        Vec3 diff = to.subtract(mc.player.getEyePosition());
        float distance = (float) Math.hypot(diff.x, diff.z);
        return new Rotation(
                Mth.wrapDegrees((float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90)),
                (float) -Math.toDegrees(Math.atan2(diff.y, distance))
        );
    }
    public static Vec3 getVectorFromRotation(Rotation rotation) {
        float yawCos = Mth.cos(-rotation.getY() * Mth.DEG_TO_RAD - Mth.PI);
        float yawSin = Mth.sin(-rotation.getY() * Mth.DEG_TO_RAD - Mth.PI);
        float pitchCos = -Mth.cos(-rotation.getX() * Mth.DEG_TO_RAD);
        float pitchSin = Mth.sin(-rotation.getX() * Mth.DEG_TO_RAD);

        return new Vec3(
                yawSin * pitchCos,
                pitchSin,
                yawCos * pitchCos
        );
    }

    public static Vec3 getNearestVec(AABB box, Rotation currentRotation) {
        Vec3 lastPoint ;
        List<Vec3> visiblePoints = new ArrayList<>();
        for (double x = box.minX; x <= box.maxX; x += 0.025) {
            for (double y = box.minY; y <= box.maxY; y += 0.025) {
                for (double z = box.minZ; z <= box.maxZ; z += 0.025) {
                    Vec3 temp = new Vec3(x, y, z);
                    if (!canVectorBeSeen(temp))
                        continue;
                    visiblePoints.add(temp);
                }
            }
        }

        if (visiblePoints.isEmpty()) return getVectorFromRotation(currentRotation);


        lastPoint = visiblePoints.get(0);

        for (Vec3 point : visiblePoints) {
            DeltaRotation currentDelta = new DeltaRotation(currentRotation, getRotation(lastPoint));
            DeltaRotation tempDelta = new DeltaRotation(currentRotation, getRotation(point));

            if (Math.hypot(currentDelta.getX(), currentDelta.getY()) < Math.hypot(tempDelta.getX(), tempDelta.getY()))
                continue;

            lastPoint = point;
        }

        return lastPoint;
    }

}