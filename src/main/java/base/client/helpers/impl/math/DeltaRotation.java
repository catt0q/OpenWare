package base.client.helpers.impl.math;

import base.client.helpers.impl.rotation.Rotation;
import base.client.helpers.utils.ACUtil;
import net.minecraft.util.Mth;

public class DeltaRotation extends Rotation {
    public DeltaRotation(float yaw, float pitch) {
        super(yaw, pitch);
    }

    public DeltaRotation(Rotation start, Rotation end) {
        super(Mth.wrapDegrees(end.getX() - start.getX()), end.getY() - start.getY());
    }

    public DeltaRotation plusYaw(float yaw) {
        return new DeltaRotation(
                Mth.wrapDegrees(this.getX() + yaw),
                getY()
        );
    }

    public DeltaRotation plusPitch(float pitch) {
        return new DeltaRotation(
                getX(),
                Mth.clamp(this.getY() + pitch, -90, 90)
        );
    }

    public DeltaRotation plusRotation(Rotation rotation) {
        return new DeltaRotation(
                Mth.wrapDegrees(this.getX() + rotation.getX()),
                Mth.clamp(this.getY() + rotation.getY(), -90, 90)
        );
    }

    public DeltaRotation limit(float yaw, float pitch) {
        return new DeltaRotation(
                Mth.clamp(super.getX(), -yaw, yaw),
                Mth.clamp(super.getY(), -pitch, pitch)
        );
    }

    public DeltaRotation multi(float yaw, float pitch) {
        return new DeltaRotation(
                super.getX() * yaw,
                super.getY() * pitch
        );
    }

    public DeltaRotation fix() {
        float gcd = ACUtil.getMouseGCD();
        return new DeltaRotation(
        		MathematicHelper.round(getX(), gcd),
        		MathematicHelper.round(getY(), gcd)
        );
    }

    public void limitVoid(float yaw, float pitch) {
        super.setX(Mth.clamp(super.getX(), -yaw, yaw));
        super.setY(Mth.clamp(super.getY(), -pitch, pitch));
    }

    public void multiVoid(float yaw, float pitch) {
        super.setX(super.getX()*yaw);
        super.setY(super.getY()*pitch);
    }

    public void fixVoid() {
        float gcd = ACUtil.getMouseGCD();
        setX(MathematicHelper.round(getX(), gcd)); setY(MathematicHelper.round(getY(), gcd));
    }
}
