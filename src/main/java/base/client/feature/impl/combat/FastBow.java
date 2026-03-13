package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPostMotion;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.BowItem;

public class FastBow extends Module {
    ModeSetting Mode = new ModeSetting("Mode", "NewPacket",()->true,  "Vanilla","NewPacket"     );
    BooleanSetting AutoShot = new BooleanSetting("AutoShot", true,()->true);
     NumberSetting PacketCount=new NumberSetting("Packet Count" ,20, 1, 40, 1,()->true);
    NumberSetting MinPower=new NumberSetting("MinPower" ,40, 5, 60, 1,()->AutoShot.isEnabled());

     ModeSetting GroundCondition= new ModeSetting("Ground Condition", "Always", () -> true, "Only Ground", "Only Air", "Always");



    public FastBow() {
        super("FastBow", "При зажатии на ПКМ игрок быстро стреляет из лука", Type.Combat);

        addSettings(Mode,PacketCount,AutoShot,MinPower,GroundCondition);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.setTimerspeed(1);
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventPreMotion e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (mc.player.getTicksUsingItem() >= MinPower.getValue() && AutoShot.isEnabled()) {
            pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            mc.player.stopUsingItem();
        }

    }

    @EventTarget
    public void onUpdate(EventPostMotion event) {
        String mode = Mode.getOptions();
        this.setSuffix(mode);
        PacketHelper.Values pc = Client.instance.packet;

        if (isCondition(pc) && mc.player.isUsingItem()) {
            int amount = (int) Math.min(MinPower.getValue()-mc.player.getTicksUsingItem(), (int) PacketCount.getValue());
            if (mode.equals("NewPacket")) {

                    for (int i = 0; i < amount; i++) {
                        ACUtil.send117DuplPacket();
                        mc.player.useItemRemaining -= 1;
                    }
            } else if (mode.equals("Vanilla")) {

                for (int i = 0; i < amount; i++) {
                    pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(pc.LastGround, mc.player.horizontalCollision),10);
                    mc.player.useItemRemaining -= 1;
                }

            }

        }
    }
    private boolean isCondition(PacketHelper.Values pc) {
        if(!(mc.player.getItemInHand(mc.player.getUsedItemHand()).getItem() instanceof BowItem)) {
            return false;
        }
        if(GroundCondition.getCurrentMode().equals("Only Ground") && !ACUtil.isground()) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && ACUtil.isground()) { 	return false; 	}
        return true;
    }
    }





