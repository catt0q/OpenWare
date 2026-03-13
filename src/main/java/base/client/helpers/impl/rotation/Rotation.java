package base.client.helpers.impl.rotation;

import base.client.helpers.utils.ACUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;

@Getter
@Setter
@AllArgsConstructor
public class Rotation {
    public static final Rotation ZERO = new Rotation(0, 0);
    float x, y;

    public Rotation(Rotation src) {
        this.x = src.x;
        this.y = src.y;
    }

    public Rotation addRotation(final Rotation rotation) {
        return new Rotation(
                x + rotation.x,
                y + rotation.y
        );
    }

    public Rotation subtractRotation(final Rotation rotation) {
        return new Rotation(
                x - rotation.x,
                y - rotation.y
        );
    }

    @Override
    public String toString() {
        return x + " " + y;
    }

    public Rotation fixed(Rotation lastRotation) {
        float gcd = ACUtil.getMouseGCD();
        Rotation diff = new Rotation(
                Mth.wrapDegrees(this.getX() - lastRotation.getX()),
                Mth.wrapDegrees(this.getY() - lastRotation.getY())
        );
        diff.setX(Math.round(diff.getX() * gcd) / gcd);
        diff.setY(Math.round(diff.getY() * gcd) / gcd);
        return lastRotation.addRotation(diff);
    }
    public Rotation plusRotation(Rotation rotation) {
        return new Rotation(
                Mth.wrapDegrees(this.getX() + rotation.getX()),
                Mth.clamp(this.getY() + rotation.getY(), -90, 90)
        );
    }
    public Rotation plusRotation(Rotation rotation,boolean fix360check) {
        if(!fix360check) { return plusRotation(rotation);}

        return new Rotation(
                (this.getX() + rotation.getX()),
                Mth.clamp(this.getY() + rotation.getY(), -90, 90)
        );
    }


}
