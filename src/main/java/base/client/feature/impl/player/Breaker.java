package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.exploit.TeleportBack;
import base.client.feature.impl.movement.Flight;
import base.client.feature.impl.movement.LongJump;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Breaker extends Module {
    public TimerHelper waitforbrek= new TimerHelper();
    public TimerHelper lastBreak= new TimerHelper();
    boolean findblock = false;
    int frozenticks=0;
    int index1=0;



    boolean activatemovefix=false;

    public static NumberSetting rad;
    private final ModeSetting mode,bypass;

    Vec3 savedMot=new Vec3(0.0D,0.0D,0.0D);

    private int xPos;
    private int yPos;
    private int zPos;
    public Block prevblock=null;
    public BlockPos lastbp=BlockPos.ZERO;
    public BlockPos mlastbp=BlockPos.ZERO;

    private final NumberSetting brde;
public static float lastyaw=0,lastpitch=0;

    public Breaker() {
        super("Breaker", "Автоматически рушит кровати сквозь блоки", Type.Player);
        mode = new ModeSetting("Block", "Bed", () -> true, "Bed");
        bypass = new ModeSetting("Bypass", "Matrix1", () -> true, "Matrix1", "Vanilla");
        rad = new NumberSetting("Radius", 4, 1, 6, 0.5F, () -> true);
        brde = new NumberSetting("Delay", 1000, 0, 5000, 1F, () -> true);

        addSettings(mode, bypass, rad, brde);
    }
    @Override
    public void onDisable() {
        TimerUtil.reset();
        MoveUtil.isFreezing=false;
        if(activatemovefix){
            activatemovefix=false;
            MoveFix.needspoofyaw = false;
            MoveFix.RealYaw = lastyaw;
        }
        super.onDisable();
    }
    @Override
    public void onEnable() {MoveUtil.isFreezing=false;
        activatemovefix=false;
        resetBlockData();
        super.onEnable();
    }




    @EventTarget
    public void onTick(EventTick e) {
        float radius = rad.getValue();
        if (bypass.getCurrentMode().equals("Matrix1")) {
            radius = 7.0f;
        }
        PacketHelper.Values pc = Client.instance.packet;


if(findblock) {
    if (MathematicHelper.getVecDist(lastbp.getCenter(),mc.player.position())>radius){
        resetBlockData();
    }
    BlockState bss = mc.level.getBlockState(lastbp);
      prevblock = bss.getBlock();
     if (!goodBlock(prevblock)) {
         long maxWait = bypass.getCurrentMode().equals("Matrix1") ? 50L : 0L;
         if((!bypass.getCurrentMode().equals("Matrix1")) || waitforbrek.hasTimeElapsed(maxWait,false)) {
             MoveUtil.isFreezing=false;  resetBlockData();
         }
     }else{
         waitforbrek.reset();
     }

}

        if(!findblock) {
            long delay = bypass.getCurrentMode().equals("Matrix1") ? 555L : (long) brde.getValue();
            if(lastBreak.hasTimeElapsed(delay,false)){
            for (int x = (int) -radius; x < radius; x++) {
                for (int y = (int) radius; y > -radius; y--) {
                    for (int z = (int) -radius; z < radius; z++) {
                        this.xPos = (int) Math.floor(mc.player.getX() + x);
                        this.yPos = (int) Math.floor(mc.player.getY() + y);
                        this.zPos = (int) Math.floor(mc.player.getZ() + z);
                        BlockPos blockPos = new BlockPos(this.xPos, this.yPos, this.zPos);
                        if (MathematicHelper.getVecDist(blockPos.getCenter(), mc.player.getEyePosition()) > radius) {
                            continue;
                        }

if(bypass.getCurrentMode().equals("Matrix1") && mlastbp.equals(blockPos)){
    continue;
}


                        BlockState bs = mc.level.getBlockState(blockPos);
                        Block block = bs.getBlock();


                        if (!goodBlock(block)) {
                            continue;
                        }

                        resetBlockData();
                        mlastbp=blockPos;
                        lastbp = blockPos;
                        prevblock = block;
                        findblock = true;


                        if (findblock) {
                            break;
                        }

                    }
                    if (findblock) {
                        break;
                    }
                }
                if (findblock) {
                    break;
                }
            }
            }
        }else{
            lastBreak.reset();
        }






        if(!findblock) {
            if(activatemovefix){
                activatemovefix=false;
                MoveFix.needspoofyaw = false;
                MoveFix.RealYaw = lastyaw;
            }

            return;
        }





        switch (bypass.getCurrentMode()){
            case ("None"):
                activatemovefix=true;
                float[] rotations = RotationUtils.getRotationVector(new Vec3(lastbp.getCenter().x, lastbp.getCenter().y+0.5, lastbp.getCenter().z), false, 0, 0, 360);
                lastyaw=rotations[0];
                lastpitch=rotations[1];
                MoveFix.needspoofyaw = true;
                MoveFix.RealYaw = lastyaw;

                mc.gameMode.continueDestroyBlock(lastbp, Direction.UP);
                mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            case ("Matrix1"):

                if(MoveUtil.isFreezing){
                    mc.gameMode.continueDestroyBlock(lastbp, Direction.UP);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    if (mc.gameMode.destroyProgress> 0) {
                        mc.gameMode.destroyProgress = 2F;
                        for(int i=0;i<30;i++){
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }

                        mc.gameMode.continueDestroyBlock(lastbp, Direction.UP);
                  resetBlockData();
    }



                }else{
                    if(!mc.gameMode.isDestroying()) {
                        if(mc.player.verticalCollision &&  !Client.instance.featureManager.getModuleByClass(LongJump.class).getState() &&  !Client.instance.featureManager.getModuleByClass(Flight.class).getState() && (!Client.instance.featureManager.getModuleByClass(TeleportBack.class).getState() || !TeleportBack.Mode.getCurrentMode().equals("Matrix"))) {
                            mc.player.jumpFromGround();
                        }
                    }

                }
         break;

            case ("Matrix2"):

                if(index1>0){

                    if (index1==1) {
                      //  pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, lastbp, Direction.DOWN), 10,true);

                        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, lastbp, Direction.DOWN), 10,true);

                           pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ZERO, Direction.DOWN), 10, true);


                    }else if(index1==3){
ChatHelper.addChatMessage("hi");
                        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, lastbp, Direction.UP), 10, true);


                        //  mc.gameMode.continueDestroyBlock(lastbp, Direction.DOWN);
                    //   mc.player.swing(InteractionHand.MAIN_HAND);
                    }



                    index1++;
                }else{
                    if(!mc.gameMode.isDestroying()) {
                        if(mc.player.verticalCollision) {
                            mc.player.jumpFromGround();
                        }
                    }

                }
                break;






        }




//mc.player.setYRot(lastyaw); mc.player.setXRot(lastpitch);



    }

    @EventTarget
    public void onMotion(EventPreMotion e) {
        if(findblock) {
            switch (bypass.getCurrentMode()) {
                case ("None"):
                    e.setYaw(lastyaw);
                    e.setPitch(lastpitch);
                    break;

            }

        }

    }

    @EventTarget
    public void onLook(EventLook e) {
        if(findblock) {
            switch (bypass.getCurrentMode()) {
                case ("None"):
                    e.setYaw(lastyaw);
                    e.setPitch(lastpitch);
                    break;

            }



        }
    }

    @EventTarget
    public void onMove(EventOnMovePost e) {
        PacketHelper.Values pc= Client.instance.packet;
        switch (bypass.getCurrentMode()){
            case ("Vanilla"):      break;
            case ("Matrix1"):

                if(!MoveUtil.isFreezing && goodBlock(prevblock) && Math.abs(mc.player.getDeltaMovement().y)<0.4 && !ACUtil.ismatrixonground()){
                 //   pc.sendPacket(new ServerboundMovePlayerPacket.Pos(mc.player.getX(),mc.player.getY() ,mc.player.getZ(), false, mc.player.horizontalCollision), 10, true);

                    savedMot=mc.player.getDeltaMovement();
                    MoveUtil.isFreezing=true;
                }


                if(MoveUtil.isFreezing){
                    MoveUtil.stop3();
 if(!goodBlock(prevblock)){
    if(waitforbrek.hasTimeElapsed(50L,false)) {
        MoveUtil.isFreezing = false;
        MoveUtil.setmotXYZ(savedMot.x, savedMot.y, savedMot.z); MoveUtil.limit2speed(0.2);
    }
 }else {
    waitforbrek.reset();
 }

                }
     break;

        }
    }


    @EventTarget
    public void onSendPacket(EventSendPacketCancel event) {
        if(MoveUtil.isFreezing) {
            if (event.getPacket() instanceof ServerboundMovePlayerPacket) {
                event.setCancelled(true);
            }


            if (event.getPacket() instanceof ServerboundPlayerCommandPacket) {
                event.setCancelled(true);
            }
        }
    }


    private void resetBlockData(){
        lastyaw=0;lastpitch=0;
        waitforbrek.reset();
        lastBreak.reset();

        findblock = false;
index1=0;

        lastbp=BlockPos.ZERO;
        frozenticks=0;
        prevblock=null;
     }

    private boolean goodBlock(Block bl){
        switch (mode.getOptions()) {
            case "Bed":
                if (bl instanceof BedBlock) {
              return true;
                }
        }
        return false;
    }




}
