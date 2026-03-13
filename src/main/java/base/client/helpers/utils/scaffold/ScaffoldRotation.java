package base.client.helpers.utils.scaffold;

import base.client.Client;
import base.client.helpers.impl.packet.PacketHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * handles rotation state for scaffold
 */
public class ScaffoldRotation {
    private static final Minecraft mc = Minecraft.getInstance();

    private float yaw;
    private float pitch;
    private float rotationSpeed = 10f;

    public void init() {
        if (mc.player == null) return;
        PacketHelper.Values pc = Client.instance.packet;
        yaw = pc.LastYaw;
        pitch = pc.LastPitch;
    }

    public void setSpeed(float speed) {
        this.rotationSpeed = speed;
    }

    /**
     * update rotation towards target hit vector
     */
    public void update(Vec3 target) {
        if (target == null) return;

        PacketHelper.Values pc = Client.instance.packet;

        // calculate target rotation
        float[] targetRot = ScaffoldUtils.calculateRotation(target);
        float targetYaw = targetRot[0];
        float targetPitch = targetRot[1];

        // smooth rotation
        float speed = rotationSpeed * 18f;
        float[] smoothed = ScaffoldUtils.smoothRotation(yaw, pitch, targetYaw, targetPitch, speed);

        // apply gcd fix
        float[] fixed = ScaffoldUtils.applyGCDFix(pc.LastYaw, pc.LastPitch, smoothed[0], smoothed[1]);
        yaw = fixed[0];
        pitch = fixed[1];
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
}
