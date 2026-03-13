package base.client.feature.impl.movement;

import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WebBlock;

public class NoWeb extends Module {

    public static ModeSetting Mode;
    boolean wasclimb=false;
    int ticks=0;
    public NoWeb() {
        super("NoWeb", "Позволяет быстро забираться по лестницам и лианам", Type.Movement);
        Mode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix", "Grim");

        addSettings(Mode);
    }

    @Override
    public void onEnable() {wasclimb=false;ticks=0;MoveUtil.lastInWeb=false;
        super.onEnable();
    }
    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void Move(EventOnMovePost e) {
        this.setSuffix(Mode.getCurrentMode());


        if (mc.player == null || mc.level == null)
            return;
        switch (Mode.getCurrentMode()) {
            case "Matrix":
                if(!MoveUtil.lastInWeb){
                    return;
                }
                if(mc.player.onGround()){
                    MoveUtil.setmotY(0.1);
                }else {
                    if(MoveUtil.motYstate()>0){
                        MoveUtil.setmotY(1.9);MoveUtil.minsmartstrafe(0.44);
                    }else if(MoveUtil.motYstate()<0){
                        MoveUtil.setmotY(-1.9);MoveUtil.minsmartstrafe(0.3);
                    }else {
MoveUtil.setmotY(0);
                        MoveUtil.minsmartstrafe(0.44);
                    }



                }
    break;
            case "Grim":
            if(MoveUtil.lastInWeb && ticks>0) {
                double yawt = Math.toRadians(mc.player.getYRot());
                yawt = Math.toRadians(MoveUtil.getdir());
                double xt = -Math.sin(yawt);
                double zt = Math.cos(yawt);
                if (MoveUtil.getdir() == -1) {
 xt = 0;     zt = 0;
                }
double grbst=(mc.player.isSprinting() && ticks>1) ? 0.4499 : 0.2499;
                if(mc.player.onGround()) {
                   MoveUtil.addmotXZ(xt*grbst,zt*grbst);
                    MoveUtil.setmotY(0.42);
                }else {
                    if(!(mc.level.getBlockState(new BlockPos(mc.player.getBlockX(), (int) Math.floor(mc.player.getY()-0.2),mc.player.getBlockZ())).getBlock() instanceof WebBlock)
                    && !mc.level.getBlockState(new BlockPos(mc.player.getBlockX(), (int) Math.floor(mc.player.getY()-0.2),mc.player.getBlockZ())).isAir()
                    ){
                        MoveUtil.setmotY(-1);
                    }else{
                        MoveUtil.setmotY(0);
                    }


                    MoveUtil.addmotXZ(xt*0.22,zt*0.22);
                    if (MoveUtil.motYstate()<0) {
                        MoveUtil.setmotY(-1);
                    }
                    else if (MoveUtil.motYstate()>0) {
                        MoveUtil.setmotY(1);
                    }


                }

            }
                ticks=MoveUtil.lastInWeb ? ticks+1 : 0;
            break;

        }
        MoveUtil.lastInWeb=false;
    }
}
