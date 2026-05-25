package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.helpers.utils.TimerUtil;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BedAura extends Module {

    private final TimerHelper timer = new TimerHelper();
    private final TimerHelper breakDelay = new TimerHelper();

    private boolean slowed = false;

    public BedAura() {
        super("BedAura", "Instantly breaks beds through walls", Type.Player);
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();
        slowed = false;
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick e) {

        if (slowed && timer.hasTimeElapsed(500, false)) {
            TimerUtil.reset();
            slowed = false;
        }

        if (!breakDelay.hasTimeElapsed(2500, false)) {
            return;
        }

        double radius = 6;

        for (int x = (int) -radius; x <= radius; x++) {
            for (int y = (int) -radius; y <= radius; y++) {
                for (int z = (int) -radius; z <= radius; z++) {

                    BlockPos playerPos = new BlockPos(
                            (int)Math.floor(mc.player.getX()),
                            (int)Math.floor(mc.player.getY()),
                            (int)Math.floor(mc.player.getZ())
                    );

                    BlockPos pos = playerPos.offset(x, y, z);

                    BlockState state = mc.level.getBlockState(pos);
                    Block block = state.getBlock();

                    if (block instanceof BedBlock) {

                        mc.gameMode.startDestroyBlock(pos, Direction.UP);
                        mc.gameMode.continueDestroyBlock(pos, Direction.UP);

                        mc.player.swing(InteractionHand.MAIN_HAND);
                        mc.player.jumpFromGround();

                        TimerUtil.setTickRate(0.5F);
                        timer.reset();
                        slowed = true;

                        breakDelay.reset();

                        return;
                    }
                }
            }
        }
    }
}