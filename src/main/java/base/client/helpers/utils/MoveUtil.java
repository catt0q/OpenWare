package base.client.helpers.utils;


import base.client.Client;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.impl.movement.Speed;
import base.client.feature.impl.movement.TargetStrafe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MoveUtil {

    static Minecraft mc=Minecraft.getInstance();

    public static boolean lastInWeb=false;
    public static boolean isHoping=false;
    public static boolean isFreezing=false;
    public static boolean NoCliping=false;

    public static boolean YawAimNeedChange() {
          return TargetStrafe.lastneedspoof;
    }

    public static float getBestYaw(float yaw) {
        if(TargetStrafe.lastneedspoof) { //TargetStrafe.updatets(KillAuraNew.lasttarget);
            yaw=TargetStrafe.lastyawspoof; }
    return yaw;
    }



    public static float getDirection(float yaw) {
        return getDirection(yaw, mc.player.input.getMoveVector().x, mc.player.input.getMoveVector().y);
    }

    public static boolean isdiag(){
        return (MoveUtil.getmf() != 0 && MoveUtil.getms() != 0);
     }


    public static float getDirection(float yaw, float movementForward, float movementSideways) {
        if (movementForward > 0) {
            yaw += 0;
        } else if (movementForward < 0) {
            yaw += 180;
        }

        float forward = 1f;

        if (movementForward != 0) {
            forward = 0.5f;
        }

        if (movementSideways > 0) {
            yaw += 90 * forward;
        } else if (movementSideways < 0) {
            yaw -= 90 * forward;
        }

        return Mth.wrapDegrees(yaw);
    }


    public static double[] forward(final double d) {
        float f = mc.player.input.getMoveVector().x;
        float f2 = mc.player.input.getMoveVector().y;
        float f3 = mc.player.getYRot();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static void addmotX(double motx){mc.player.addDeltaMovement(new Vec3(motx,0,0));}
    public static void addmotY(double moty){mc.player.addDeltaMovement(new Vec3(0,moty,0));}
    public static void addmotZ(double motz){mc.player.addDeltaMovement(new Vec3(0,0,motz));}

    public static void addmotXZ(double motx,double motz){mc.player.addDeltaMovement(new Vec3(motx,0,motz));}
    public static void addmotXYZ(double motx,double moty,double motz){mc.player.addDeltaMovement(new Vec3(motx,moty,motz));}

    public static void setmotX(double motx){mc.player.setDeltaMovement(motx,mc.player.getDeltaMovement().y(),mc.player.getDeltaMovement().z());}
    public static void setmotY(double moty){ mc.player.setDeltaMovement(mc.player.getDeltaMovement().x(),moty,mc.player.getDeltaMovement().z());}
    public static void setmotZ(double motz){mc.player.setDeltaMovement(mc.player.getDeltaMovement().x(), mc.player.getDeltaMovement().y(), motz);}
    public static void setmotXZ(double motx,double motz){mc.player.setDeltaMovement(motx,mc.player.getDeltaMovement().y(),motz);}
    public static void setmotXYZ(double motx,double moty,double motz){mc.player.setDeltaMovement(motx,moty,motz);}
    public static void setmotXYZ(Vec3 mot){mc.player.setDeltaMovement(mot);}


public static boolean ismovinginput(){
        return mc.player.input.getMoveVector().x!=0 || mc.player.input.getMoveVector().y!=0;
}
    public static boolean ismovingmfms(){
        return (MoveUtil.getmf()!=0 || MoveUtil.getms()!=0);
    }

    public static float getCurrMoveAngle() {
        if(YawAimNeedChange()) {
            return getBestYaw(mc.player.getYRot());
        }else {
            return mc.player.getYRot();
        }
  }

    public static float getCurrMoveAngle2() {
        if(YawAimNeedChange()) {
            return mc.player.getYRot();
        }else {

            boolean isr=isRightDown();
            boolean isl=isLeftDown();
            boolean isf=isForDown();
            boolean isb=isBackDown();

            float startfloat=mc.player.getYRot();
            if(isb) {
                startfloat-=180;
            }


            boolean willmove=(isb || isf) && !(isb && isf);
            if(!willmove) {
                if(isl) {
                    startfloat-=90;
                }
                if(isr) {
                    startfloat+=90;
                }
            }else {

                if(startfloat==mc.player.getYRot()) {
                    if(isl) {
                        startfloat-=45;
                    }
                    if(isr) {
                        startfloat+=45;
                    }
                }else {
                    if(isl) {
                        startfloat+=45;
                    }
                    if(isr) {
                        startfloat-=45;
                    }
                }

            }


            return startfloat;
        }

    }

    public static void minssmartstrafe(double motion) {
        if(MoveUtil.getspeed2()<motion) {
            MoveUtil.ssmartstrafe(motion);
        }
    }
    public static void minsmartstrafe(double motion) {
        if(MoveUtil.getspeed2()<motion) {
            MoveUtil.smartstrafe(motion);
        }
    }

    public static void minstrafe(double motion) {
        if(MoveUtil.getspeed2()<motion) {
            MoveUtil.strafe(motion);
        }
    }


    public static void strafe(double motion) {
        double yawt = Math.toRadians(getCurrMoveAngle());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        setmotX(xt * motion);
        setmotZ(zt * motion);
    }
    public static void strafe() {
        double yawt = Math.toRadians(getCurrMoveAngle());
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        double motx=mc.player.getDeltaMovement().x();
        double motz=mc.player.getDeltaMovement().z();
        double sqrtt=Math.sqrt(motx*motx+motz*motz);
        setmotX(xt * sqrtt); setmotZ(zt * sqrtt);
         
    }
    public static void strafe2(double speed) {
        float yaw = mc.player.getYRot();

        double forward = 0;
        double strafe = 0;

        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;

        if (forward == 0 && strafe == 0) {
            setmotX(0);
            setmotZ(0);
            return;
        }

        double length = Math.sqrt(forward * forward + strafe * strafe);
        forward /= length;
        strafe /= length;

        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double motionX = (forward * -sin + strafe * cos) * speed;
        double motionZ = (forward *  cos - strafe * sin) * speed;

        setmotX(motionX);
        setmotZ(motionZ);
    }

    public static double getspeed2() {
        double motx=mc.player.getDeltaMovement().x();
        double motz=mc.player.getDeltaMovement().z();
        double sqrtt=Math.sqrt(motx*motx+motz*motz);
        return sqrtt;
    }

    public static double getspeed3() {
        double motx=mc.player.getDeltaMovement().x();
        double moty=mc.player.getDeltaMovement().y();
        if(mc.player.onGround()) { 		moty=0; 	}
        double motz=mc.player.getDeltaMovement().z();
        double sqrtt=Math.sqrt(motx*motx+motz*motz+moty*moty);
        return sqrtt;
    }






    public static void stop3() {
        mc.player.setDeltaMovement(0,0,0); 
    }
    public static void stop2() {
        setmotXZ(0,0); 
    }



    public static void maximize2speed(double speedd) {
        while(MoveUtil.getspeed2()<speedd) {
            mult2ds(1.01);
        }
    }
    public static void maximize2speed(double speedd,double accuracity) {
        if(accuracity<=1) {
            accuracity=1.01;
        }
        while(MoveUtil.getspeed2()<speedd) {
            mult2ds(accuracity);
        }
    }
    public static void limit2speed(double speedd) {
        while(MoveUtil.getspeed2()>speedd) {
            mult2ds(0.999);
        }
    }

    public static void limit2speed(double speedd,double accuracity) {
        if(accuracity>=1) {
            accuracity=0.999;
        }


        while(MoveUtil.getspeed2()>speedd) {
            mult2ds(accuracity);
        }
    }



    public static void limitY(double speedd,boolean otr) {
        if(!otr) {
            if( mc.player.getDeltaMovement().y()>speedd) {
                setmotY(speedd);
            }
        }else {
            if( mc.player.getDeltaMovement().y()<speedd) {
                setmotY(speedd);
            }
        }
    }
    public static void limitY(double speedd ) {
        if( mc.player.getDeltaMovement().y()>speedd) {
            setmotY(speedd);
        }
    }

public static void mult2ds(double mult) {
        setmotXZ(mc.player.getDeltaMovement().x()*mult,mc.player.getDeltaMovement().z()*mult);
    }

    public static void mult3ds(double mult) {
      setmotXYZ(mc.player.getDeltaMovement().x()*mult,mc.player.getDeltaMovement().y()*mult,mc.player.getDeltaMovement().z()*mult);
     }

    public static int motYstate() {
        int indexctnj=0;

        if(mc.options.keyJump.isDown()) {
            indexctnj++;
        }

        boolean foufd=false;
        foufd=mc.options.keyShift.isDown();
        if(foufd) {
            indexctnj--;
        }
        return indexctnj;
    }

    public static boolean motYstateh() {

            return (mc.options.keyJump.isDown() || checkhoping());



    }


    public static float getmf() {
        int indexctnj=0;
        if(isForDown()) {
            indexctnj++;
        }
        if(isBackDown()) {
            indexctnj--;
        }
        return indexctnj;
    }

    public static float getms() {
        int indexctnj=0;
        if(isRightDown()) {
            indexctnj++;
        }
        if(isLeftDown()) {
            indexctnj--;
        }
        return indexctnj;
    }

    public static float getmovedir() {
        return (float) Mth.wrapDegrees(mc.player.getDeltaMovement().z()>0 ? Math.toDegrees(Math.atan(-mc.player.getDeltaMovement().x()/mc.player.getDeltaMovement().z())) : (Math.toDegrees(Math.atan(-mc.player.getDeltaMovement().x()/mc.player.getDeltaMovement().z()))-180));
    }

    public static float getdir() {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();
        float startfloat=mc.player.getYRot();

        if(!isr && !isb && !isl && !isf) {
            return -1;
        }


        if(isb) {
            startfloat-=180;
        }

        boolean willmove=(isb || isf) && !(isb && isf) ;
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }



        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);

        if(!willmove) {
            return -1;
        }

        return startfloat;
    }
    public static Vec3 getmovedir3(float yaw, float pitch, double length) {


        // Конвертация углов в радианы
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        // Вычисление компонентов единичного вектора
        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        // Нормализация (гарантия единичной длины)
        double norm = Math.sqrt(x*x + y*y + z*z);
        if(norm > 0) {
            x /= norm;
            y /= norm;
            z /= norm;
        }

        // Масштабирование до точной длины
        return new Vec3(x * length, y * length, z * length);
   }

    public static float getdir(boolean yawdir) {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();
        float startfloat=mc.player.getYRot();
        if((YawAimNeedChange() && yawdir)) {
            return getCurrMoveAngle();
        }



        if(!isr && !isb && !isl && !isf) {
            return -1;
        }


        if(isb) {
            startfloat-=180;
        }

        boolean willmove=(isb || isf) && !(isb && isf) ;
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }



        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange() && yawdir) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);

        if(!willmove) {
            return -1;
        }

        return startfloat;
    }

    public static void strafewdir(double motion,float dir) {
        double yawt = Math.toRadians(dir);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        setmotXZ(xt * motion,zt * motion);
    }


    public static void smartstrafe(double motion) {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();

        float startfloat=mc.player.getYRot();
        if(isb) {
            startfloat-=180;
        }


        boolean willmove=(isb || isf) && !(isb && isf);
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }



        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(willmove) {
            setmotXZ(xt * motion,zt * motion);
        }
    }

    public static void smartstrafe(double motion,float yaw) {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();

        float startfloat=yaw;
        if(isb) {
            startfloat-=180;
        }


        boolean willmove=(isb || isf) && !(isb && isf);
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==yaw) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }



        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(willmove) {
            setmotXZ(xt * motion,zt * motion);
        }
    }






    public static void smartstrafe() {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();

        float startfloat=mc.player.getYRot();
        if(isb) {
            startfloat-=180;
        }


        boolean willmove=(isb || isf) && !(isb && isf);
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }

        double motion=MoveUtil.getspeed2();

        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(willmove) {
            setmotXZ(xt * motion,zt * motion);
        }
    }

    public static void ssmartstrafe(double motion) {

        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();

        float startfloat=mc.player.getYRot();
        if(isb) {
            startfloat-=180;
        }


        boolean willmove=(isb || isf) && !(isb && isf);
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }
        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }
        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }
        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(willmove) {
            setmotXZ(xt * motion,zt * motion);
        }else {
          stop2();
        }
    }
    public static void ssmartstrafe() {
        boolean isr=isRightDown();
        boolean isl=isLeftDown();
        boolean isf=isForDown();
        boolean isb=isBackDown();

        float startfloat=mc.player.getYRot();
        if(isb) {
            startfloat-=180;
        }

        boolean willmove=(isb || isf) && !(isb && isf);
        if(!willmove) {
            if(isl) {
                startfloat-=90;
            }
            if(isr) {
                startfloat+=90;
            }
        }else {

            if(startfloat==mc.player.getYRot()) {
                if(isl) {
                    startfloat-=45;
                }
                if(isr) {
                    startfloat+=45;
                }
            }else {
                if(isl) {
                    startfloat+=45;
                }
                if(isr) {
                    startfloat-=45;
                }
            }

        }



        if(!willmove) {
            willmove=(isl || isr) && !(isl && isr);
        }

        if(YawAimNeedChange()) { startfloat=getCurrMoveAngle(); }


        double yawt = Math.toRadians(startfloat);
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        if(willmove) {
            double motion=MoveUtil.getspeed2();
            setmotXZ(xt * motion,zt * motion);
        }else {
stop2();
        }
    }
    public static boolean checkhoping() {
   if(!Client.instance.featureManager.getModuleByClass(Speed.class).getState()){
       isHoping=false; return false;
   }
        switch (Speed.Mode.getCurrentMode()){
            case("Matrix"):
                if(Speed.MatrixMode.getCurrentMode().toLowerCase().contains("hop")){
                    isHoping=true; return true;
                }
                  break;
            case("Grim"):
                if(Speed.GrimMode.getCurrentMode().toLowerCase().contains("hop")){
                    isHoping=true; return true;
                }
                break;
            case("Polar"):
                if(Speed.PolarMode.getCurrentMode().toLowerCase().contains("hop")){
                    isHoping=true; return true;
                }
                break;

        }


        isHoping=false;    return false;
    }

    static boolean isForDown() {
     if(weirdscreen()){   return false;  }

          return mc.options.keyUp.isDown();
    }

    static boolean isBackDown() {
        if(weirdscreen()){   return false;  }
        return mc.options.keyDown.isDown();
    }
    static boolean isLeftDown() {
        if(weirdscreen()){   return false;  }
        return mc.options.keyLeft.isDown();
    }

    static  boolean isRightDown() {
        if(weirdscreen()){   return false;  }
        return mc.options.keyRight.isDown();
    }

static boolean weirdscreen(){
    if (mc.screen instanceof ChatScreen || mc.screen instanceof SignEditScreen) {
        return true;
    }
    return false;
}




}
