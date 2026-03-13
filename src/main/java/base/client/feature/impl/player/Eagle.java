package base.client.feature.impl.player;

import base.client.event.EventTarget;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;

public class Eagle extends Module {
    public BooleanSetting Predict = new BooleanSetting("Predict", false, () -> true);
    public Eagle() {
        super("Eagle", "Нажимает шифт когда строишься", Type.Player);
        this.addSettings(Predict);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @EventTarget
    public void onUpdate(EventMoveInput e) {
        BlockPos pos = new BlockPos((int)  Math.floor(mc.player.getX()), (int) (mc.player.getY() - 1), (int)Math.floor(mc.player.getZ()));
        if(Predict.isEnabled()) {
            double yawt = Math.toRadians(Minecraft.getInstance().player.getYRot());  yawt = Math.toRadians( MoveUtil.getdir()); double xt = -Math.sin(yawt);    double zt = Math.cos(yawt); if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }

            pos = new BlockPos((int)Math.floor (mc.player.getX()-xt*0.3), (int) (mc.player.getY() - 1), (int)Math.floor(mc.player.getZ()-zt*0.3));
        }
        if(!e.sneak) {
            e.setSneak(mc.level.getBlockState(pos).isAir());
        }
    }
    @EventTarget
    public void onUpdate(EventPreMotion event) {

    }

}
