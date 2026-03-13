package base.client.helpers.impl.math;

import base.client.helpers.Helper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathematicHelper implements Helper {
    public static float wrapDegrees(float value)
    {
        value = value % 360.0F;

        if (value >= 180.0F)
        {
            value -= 360.0F;
        }

        if (value < -180.0F)
        {
            value += 360.0F;
        }

        return value;
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static double wrapDegrees(double value)
    {
        value = value % 360.0D;

        if (value >= 180.0D)
        {
            value -= 360.0D;
        }

        if (value < -180.0D)
        {
            value += 360.0D;
        }

        return value;
    }
    public static BigDecimal round(float f, int times) {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(times, RoundingMode.HALF_UP);
        return bd;
    }

    public static int getMiddle(int old, int newValue) {
        return (old + newValue) / 2;
    }

    public static double round(double num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    public static float round(float num, float increment) {
        float v = (float) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static float checkAngle(float one, float two, float three) {
        float f = Mth.wrapDegrees(one - two);
        if (f < -three) {
            f = -three;
        }
        if (f >= three) {
            f = three;
        }
        return one - f;
    }

    public static double getVecDist(Vec3 vec1, Vec3 vec2) {

        double x = vec1.x-vec2.x;
        double y = vec1.y-vec2.y;
        double z = vec1.z-vec2.z;
        return Math.sqrt((float) (x * x + y * y + z * z));
    }


    public static float randomizeFloat(float min, float max) {
        return (float) (min + (max - min) * Math.random());
    }
}

