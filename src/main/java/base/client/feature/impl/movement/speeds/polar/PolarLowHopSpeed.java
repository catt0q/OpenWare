package base.client.feature.impl.movement.speeds.polar;

import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;

import static base.client.helpers.Helper.mc;

public class PolarLowHopSpeed {

    static boolean jumping;
    static int index1,index2;

    public static void onenable(){
        jumping=false;
        index1=0;
        index2=-1;
    }
    public static EventOnJump onEventOnJump(EventOnJump e) {
        if (!jumping) {    e.cancel();  }
        return e;
    }

    public static EventOnMovePost onEventOnMovePost(EventOnMovePost e, double xt, double zt) {

        if (mc.player.isUsingItem() || mc.player.isSwimming() || mc.player.isShiftKeyDown()) return e;

        if(mc.player.onGround()){
            if(index2==0) {sjump(); }
            MoveUtil.addmotY(-0.00249);
            index2++;
        }else{
            index2=0;
            if (index1>8 && !(Minecraft.getInstance().level.getBlockState(new BlockPos(Minecraft.getInstance().player.getBlockX(),
                    (int) (Minecraft.getInstance().player.getY() +mc.player.getDeltaMovement().y()),
                    Minecraft.getInstance().player.getBlockZ())).getBlock() instanceof AirBlock)) {

            }
            if(index1<6) {
                MoveUtil.addmotY(-0.00249);
            }else{
                double bstn = 0.00249;
                bstn=0.00249; MoveUtil.addmotX(xt*bstn);      MoveUtil.addmotZ(zt*bstn);
            }
            index1++;
        }
        return e;
    }


    private static void sjump(){
        jumping=true; mc.player.jumpFromGround(); jumping=false;index1 = 0;
    }



}
