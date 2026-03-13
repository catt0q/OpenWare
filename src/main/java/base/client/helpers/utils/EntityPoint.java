package base.client.helpers.utils;

public class EntityPoint {

    final double posX;
    final double posY;
    final double posZ;

    final double motX;
    final double motY;
    final double motZ;



    final float rotYaw;
    final float rotPitch;
    final boolean onground;
    long lastMs=0;

    public long getLastMs() {
        return lastMs;
    }
    public double getPosX() {
        return posX;
    }
    public double getPosY() {
        return posY;
    }
    public double getPosZ() {
        return posZ;
    }
    public float getRotYaw() {
        return rotYaw;
    }
    public float getRotPitch() {
        return rotPitch;
    }
    public boolean isOnground() {
        return onground;
    }
    public double getMotX() {
        return motX;
    }
    public double getMotY() {
        return motY;
    }
    public double getMotZ() {
        return motZ;
    }

    public EntityPoint(double posX,double posY,double posZ,double motX,double motY,double motZ,float rotYaw,float rotPitch,boolean onground,long lastMs) {
        this.posX=posX;
        this.posY=posY;
        this.posZ=posZ;
        this.motX=motX;
        this.motY=motY;
        this.motZ=motZ;
        this.rotYaw=rotYaw;
        this.rotPitch=rotPitch;
        this.onground=onground;
        this.lastMs=lastMs;
    }


}