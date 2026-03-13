package base.client.helpers.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntityUtil {
    static Minecraft mc = Minecraft.getInstance();

    public static double getMinDistanceToEntity(Entity entity1, Entity entity2) {
        double distX1 = Math.abs(entity1.getBoundingBox().minX - entity2.getBoundingBox().minX);
        double distX2 = Math.abs(entity1.getBoundingBox().maxX - entity2.getBoundingBox().minX);

        double distY1 = Math.abs(entity1.getBoundingBox().minY - entity2.getBoundingBox().minY);
        double distY2 = Math.abs(entity1.getBoundingBox().maxY - entity2.getBoundingBox().minY);

        double distZ1 = Math.abs(entity1.getBoundingBox().minZ - entity2.getBoundingBox().minZ);
        double distZ2 = Math.abs(entity1.getBoundingBox().maxZ - entity2.getBoundingBox().minZ);

        double f = Math.min(distX1, distX2);
        double f1 = Math.min(distY1, distY2);
        double f2 = Math.min(distZ1, distZ2);
        return Math.sqrt((float) (f * f + f1 * f1 + f2 * f2));
    }

    public static double getMaxDistanceToEntity(Entity entity1, Entity entity2) {
         double distX = Math.max(
                Math.abs(entity1.getBoundingBox().maxX - entity2.getBoundingBox().minX),
                Math.abs(entity1.getBoundingBox().minX - entity2.getBoundingBox().maxX)
        );

        double distY = Math.max(
                Math.abs(entity1.getBoundingBox().maxY - entity2.getBoundingBox().minY),
                Math.abs(entity1.getBoundingBox().minY - entity2.getBoundingBox().maxY)
        );

        double distZ = Math.max(
                Math.abs(entity1.getBoundingBox().maxZ - entity2.getBoundingBox().minZ),
                Math.abs(entity1.getBoundingBox().minZ - entity2.getBoundingBox().maxZ)
        );

         return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    public static int getArmorCount(LivingEntity entityIn) {
        int armors = 0;

        for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack itemStack = entityIn.getItemBySlot(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    armors++;
                }
            }
        }

        return armors;
    }
    public static boolean canBlock(boolean sword,boolean shield) {
        return canBlock(mc.player.getMainHandItem(),mc.player.getOffhandItem(),sword,shield);
    }
    public static boolean canBlock() {
        return canBlock(mc.player.getMainHandItem(),mc.player.getOffhandItem(),true,true);
    }
    public static boolean canBlock(ItemStack itemMain,ItemStack itemOff,boolean sword,boolean shield) {
        return (itemMain.is(ItemTags.SWORDS) || itemOff.getItem() instanceof ShieldItem);
   }


    public static boolean canSeeEntityAtFov(Entity entityIn, float scope) {
        double diffX = entityIn.getX() - mc.player.getX();
        double diffZ = entityIn.getZ() - mc.player.getZ();
        float newYaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0D);
        double difference = RotationUtils.yawdiff((double) newYaw, (double) mc.player.getYRot());
        return difference <= (double) scope;
    }

    public static void tp(Entity entityIn, double x, double y, double z) {
        entityIn.setPos(x, y, z);
    }

    public static void tp(double x, double y, double z) {
        mc.player.setPos(x, y, z);
    }

    public static void dtp(Entity entityIn, double dx, double dy, double dz) {
        entityIn.setPos(entityIn.getX() + dx, entityIn.getY() + dy, entityIn.getZ() + dz);
    }

    public static void dtp(double dx, double dy, double dz) {
        mc.player.setPos(mc.player.getX() + dx, mc.player.getY() + dy, mc.player.getZ() + dz);
    }

    public static void dtp(Vec3 dvec3) {
        dtp(dvec3.x, dvec3.y, dvec3.z);
    }

    public static void dtp(Entity entityIn, Vec3 dvec3) {
        dtp(entityIn, dvec3.x, dvec3.y, dvec3.z);
    }

    public static boolean isEating(LivingEntity entityIn) {
        return entityIn.isUsingItem() && entityIn.getUseItem().get(DataComponents.FOOD) != null && entityIn.getUseItem().get(DataComponents.FOOD).saturation() > 0;
    }

    public static boolean isDrinking(LivingEntity entityIn) {
        return entityIn.isUsingItem() && entityIn.getUseItem().getItem() instanceof PotionItem;
    }
    public static boolean isConsuming(LivingEntity entityIn) {
        return (entityIn.isUsingItem() && ((entityIn.getUseItem().getItem() instanceof PotionItem) || (entityIn.getUseItem().get(DataComponents.FOOD) != null && entityIn.getUseItem().get(DataComponents.FOOD).saturation() > 0)));
    }



    public static boolean hasSpeedPotion(LivingEntity entityIn) {
        if (mc.player.getActiveEffects().size() > 0) {
            for (MobEffectInstance mobeffectinstance : mc.player.getActiveEffects()) {

                if (mobeffectinstance.getEffect().equals(MobEffects.SPEED)) {
                    return true;
                }

            }
        }
        return false;
    }


    public static ItemStack armorItemInSlot(int slot) {

   int qi = 0;

        for(EquipmentSlot equipmentSlot :EquipmentSlotGroup.ARMOR) {
            if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack itemStack = mc.player.getItemBySlot(equipmentSlot);

                if (!itemStack.isEmpty() && qi == slot) {
                    return itemStack;
                }
                qi++;
            }
        }
        return null;
}

public static int getFirstEmptyStack(){
    for (int i = 0; i < mc.player.containerMenu.slots.size(); ++i)
    {
        if (((ItemStack)mc.player.containerMenu.slots.get(i).getItem()).isEmpty())
        {
            return i;
        }
    }

    return -1;
}



    public static boolean circlintersect(LivingEntity entity1,double radius1,LivingEntity entity2,double radius2) {

        double dx = entity2.getX() - entity1.getX();
        double dz = entity2.getZ() - entity1.getZ();
        double distance = Math.sqrt(dx*dx + dz*dz);

        double nx = dx / distance;
        double nz = dz / distance;

        double a = (radius1*radius1 - radius2*radius2 + distance*distance) / (2 * distance);

        double h = Math.sqrt(radius1*radius1 - a*a);

        double p3x = entity1.getX() + a * nx;
        double p3z = entity1.getZ() + a * nz;

        double intersection1x = p3x - h * nz;
        double intersection1z = p3z + h * nx;

        double intersection2x = p3x + h * nz;
        double intersection2z = p3z - h * nx;







        return false;
    }




    public static List<Vec3> findIntersectionPoints(double x1, double z1, double r1,
                                                    double x2, double z2, double r2) {
        List<Vec3> points = new ArrayList<>();

        double dx = x2 - x1;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx*dx + dz*dz);

        if (distance > r1 + r2 || distance < Math.abs(r1 - r2)) {
            return points;
        }

        double nx = dx / distance;
        double nz = dz / distance;

        double a = (r1*r1 - r2*r2 + distance*distance) / (2 * distance);
        double h = Math.sqrt(r1*r1 - a*a);

        double p3x = x1 + a * nx;
        double p3z = z1 + a * nz;

        points.add(new Vec3(p3x - h * nz, 0, p3z + h * nx));

        if (h > 0) {
            points.add(new Vec3(p3x + h * nz, 0, p3z - h * nx));
        }

        return points;
    }

    public static float getIntersectionYaw(Vec3 playerPos, Vec3 entityPos,
                                           double playerRadius, double entityRadius,
                                           boolean spinRight) {
        List<Vec3> intersections = findIntersectionPoints(
                playerPos.x, playerPos.z, playerRadius,
                entityPos.x, entityPos.z, entityRadius);

        if (intersections.size() < 2) {
            return -1;
        }

        Vec3 baseDirection = entityPos.subtract(playerPos).normalize();
        Vec3 toPoint1 = intersections.get(0).subtract(playerPos).normalize();
        Vec3 toPoint2 = intersections.get(1).subtract(playerPos).normalize();

        double cross1 = baseDirection.x * toPoint1.z - baseDirection.z * toPoint1.x;
        double cross2 = baseDirection.x * toPoint2.z - baseDirection.z * toPoint2.x;

        Vec3 selectedPoint = (spinRight == (cross1 > cross2))
                ? intersections.get(0)
                : intersections.get(1);

        Vec3 direction = selectedPoint.subtract(playerPos);

        double x = direction.x;
        double z = direction.z;
        float yaw = (float) Math.toDegrees(Math.atan2(-x, z));

        yaw = (yaw + 360) % 360;

        return yaw;
    }



    public static double getXZIntersectionLength(AABB aabb1, AABB aabb2) {
         double xOverlap = Math.max(0, Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX));

         double zOverlap = Math.max(0, Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ));

         return Math.sqrt(xOverlap * xOverlap + zOverlap * zOverlap);
    }

      public static double[] getXZIntersection(AABB aabb1, AABB aabb2) {
        double xOverlap = Math.max(0, Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX));
        double zOverlap = Math.max(0, Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ));

        return new double[]{xOverlap, zOverlap};
    }


    public static double getMaxXZIntersection(AABB aabb1, AABB aabb2) {
        if (!intersectsXZ(aabb1, aabb2)) {
            return 0.0;
        }

        double xOverlap = Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX);
        double zOverlap = Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ);

        return Math.max(xOverlap, zOverlap);
    }

    public static double getTotalXZIntersectionLength(AABB aabb1, AABB aabb2) {
        if (!intersectsXZ(aabb1, aabb2)) {
            return 0.0;
        }

        double xOverlap = Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX);
        double zOverlap = Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ);

        return Math.sqrt(xOverlap * xOverlap + zOverlap * zOverlap);
    }

    public static boolean intersectsXZ(AABB aabb1, AABB aabb2) {
        return aabb1.minX < aabb2.maxX && aabb1.maxX > aabb2.minX &&
                aabb1.minZ < aabb2.maxZ && aabb1.maxZ > aabb2.minZ;
    }


    public static double getTotalXYZIntersectionLength(AABB aabb1, AABB aabb2) {
        if (!intersectsXYZ(aabb1, aabb2)) {
            return 0.0;
        }

        double xOverlap = Math.min(aabb1.maxX, aabb2.maxX) - Math.max(aabb1.minX, aabb2.minX);
        double yOverlap = Math.min(aabb1.maxY, aabb2.maxY) - Math.max(aabb1.minY, aabb2.minY);
        double zOverlap = Math.min(aabb1.maxZ, aabb2.maxZ) - Math.max(aabb1.minZ, aabb2.minZ);

        return Math.sqrt(xOverlap * xOverlap + yOverlap * yOverlap + zOverlap * zOverlap);
    }

    public static boolean intersectsXYZ(AABB aabb1, AABB aabb2) {
        return aabb1.minX < aabb2.maxX && aabb1.maxX > aabb2.minX &&
                aabb1.minY < aabb2.maxY && aabb1.maxY > aabb2.minY &&
                aabb1.minZ < aabb2.maxZ && aabb1.maxZ > aabb2.minZ;
    }



}
