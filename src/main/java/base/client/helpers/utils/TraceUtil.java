package base.client.helpers.utils;
 
import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TraceUtil {
    static Minecraft mc=Minecraft.getInstance();
    public static boolean KillauraTraceFind(double range, float yaw, float pitch, Entity tar, double addsize, int points) {
        if(tar==null) { 	return false; 	}

        double yawt = Math.toRadians(yaw%360);
        double pitcht = Math.toRadians(pitch);

        double mult=1D/((double)points);

        double xt = -Math.sin(yawt)*mult;
        double zt = Math.cos(yawt)*mult;
        double yt = -Math.sin(pitcht)*mult;



        boolean find=false;
        for(double q=0;q<=range*points;q++) {
            double renderposX= mc.player.getX()+(double)(xt* q );
            double renderposY= mc.player.getY()+mc.player.getEyeHeight(mc.player.getPose())  +(double)(yt*q);
            double renderposZ= mc.player.getZ()+(double)(zt*q);

            Entity target = tar;
            AABB ab=target.getBoundingBox();

            double lenx=target.getBbWidth()+addsize;
            double lenz=target.getBbWidth()+addsize;
            ab=ab.expandTowards(0,addsize,0);
            double height=0.2;
            if(mc.level.getBlockState(new BlockPos((int) renderposX,(int) renderposY,(int) renderposZ))!=null &&
                     !(mc.level.getBlockState(new BlockPos((int) renderposX,(int) renderposY,(int) renderposZ)).getBlock() instanceof AirBlock)
            ) {
                return false;
            }

            if(ab.intersects(new Vec3(renderposX,renderposY,renderposZ),new Vec3(renderposX,renderposY,renderposZ)))
            return  true;

        }

        return false;
    }

    public static boolean isLookingAtEntity(float yaw, float pitch, float xExp, float yExp, float zExp, Entity entity, double range) {
        Vec3 src = mc.player.getEyePosition();
        Vec3 vectorForRotation = entity.calculateViewVector(pitch, yaw);
        Vec3 dest = src.add(vectorForRotation.x * range, vectorForRotation.y * range, vectorForRotation.z * range);
      float yawo=yaw;  float pitcho=pitch;

        HitResult hitResult = mc.player.pick(range, 1, false);

        if (hitResult == null) {
            return false;
        }

        return (entity.getBoundingBox().expandTowards(xExp, yExp, zExp).intersects(src,dest));
    }

    public static HitResult ClientpickNew(double d, float f, boolean bl, float yaw, float pitch) {
        PacketHelper.Values pc = Client.instance.packet;
         Vec3 vec3 = new Vec3(pc.LastPosX,pc.LastPosY+(double) mc.player.getEyeHeight(),pc.LastPosZ);
      //  Vec3 vec32 = getViewVector3(yaw,pitch);
      //  Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        Vec3 vec32 = vec3.add(mc.player.calculateViewVector(pitch, yaw).scale(mc.player.blockInteractionRange()));

        return mc.player.level()
                .clip(
                        new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, bl ? net.minecraft.world.level.ClipContext.Fluid.ANY : net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player)
                );

    }
    public static final Vec3 getViewVector3(float yaw, float pitch) {
        return mc.player.calculateViewVector(pitch,yaw);
    }


}
