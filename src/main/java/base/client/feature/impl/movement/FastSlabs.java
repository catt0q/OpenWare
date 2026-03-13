package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.ModeSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
public class FastSlabs extends Module {

    public ModeSetting fastslabsMode = new ModeSetting("Mode", "Matrix", () -> true, "Matrix","Polar");

    boolean lastboost = false;
    double airticks = 0;

    public FastSlabs() {
        super("FastSlabs", "Ускоряет на полублоках", Type.Movement);
        addSettings(fastslabsMode);
    }


    @Override
    public void onEnable() {
        airticks = 0;
        lastboost = false;
        super.onEnable();
    }

    @EventTarget
    public void onJump(EventOnJump e) {

        if (lastboost) {
            e.setCancelled(true);
            airticks = 1;
        }


    }


    @EventTarget
    public void MovePost(EventOnMovePost eventMove) {
        PacketHelper.Values pc = Client.instance.packet;
        double yawt = Math.toRadians(Minecraft.getInstance().player.getYRot());
        if (MoveUtil.getdir() != -1) {
            yawt = Math.toRadians(MoveUtil.getdir());
        }
        double xt = -Math.sin(yawt);
        double zt = Math.cos(yawt);
        switch (fastslabsMode.getCurrentMode()){
            case ("Polar"):
                lastboost=false;
                if ( !mc.player.isShiftKeyDown() &&  MoveUtil.motYstate() < 1 && (MoveUtil.getmf() != 0 || MoveUtil.getms() != 0) && (mc.player.verticalCollision || (ACUtil.isground() && airticks==1 ))
               && !(mc.level.getBlockState(mc.player.blockPosition()).getBlock() instanceof AirBlock)
                        && !(mc.level.getBlockState(mc.player.blockPosition()).getBlock() instanceof LiquidBlock)
                ) {
                    lastboost=true;

                    if (airticks % 2 == 0) {
                        MoveUtil.setmotY(0.000001);
                     MoveUtil.minsmartstrafe(0.319);
                    } else {MoveUtil.minsmartstrafe(0.248);
                        MoveUtil.setmotY(-0.001);
                    }
                    airticks++;
                }else {
                    airticks=0;
                }

                break;

            case ("Matrix"):

                lastboost = false;
                if (airticks > 0) {
                    MoveUtil.setmotY(0.42);
                }

                if (airticks == 0 && !mc.player.isShiftKeyDown() && mc.player.getY() != (int) mc.player.getY() && MoveUtil.motYstate() < 1 && (MoveUtil.getmf() != 0 || MoveUtil.getms() != 0) && mc.player.onGround() && mc.player.verticalCollision &&
                        (mc.level.getBlockState(mc.player.blockPosition()).getBlock() instanceof SlabBlock
                                || mc.level.getBlockState(mc.player.blockPosition()).getBlock() instanceof net.minecraft.world.level.block.StairBlock
                        )

                ) {



                    MoveUtil.addmotX(xt * 0.199);
                    MoveUtil.addmotZ(zt * 0.199);
                    MoveUtil.smartstrafe();
                    lastboost = true;
                }
                if (airticks > 0
                ) {
                    airticks = 0;
                }


                break;





        }





    }


}
