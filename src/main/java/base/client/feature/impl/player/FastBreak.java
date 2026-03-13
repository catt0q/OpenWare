package base.client.feature.impl.player;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.ACUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;

public class FastBreak extends Module {

    int index1=0;
    int index2;
    double indexd1;
    double indexd2;
    public BlockState b;
    public BlockPos bp;
    public double blockdamage=0;
    public double maxdamage=0;
    public int ticks=0;
    boolean skippacket=false;

    NumberSetting multVT = new NumberSetting("Boost With Tool","100 is instant, 0 has no effect", 40, 0, 100, 1F,()->true);
    NumberSetting multVWT = new NumberSetting("Boost Without Tool","100 is instant, 0 has no effect", 40, 0, 100, 1F,()->true);
    BooleanSetting ABB = new BooleanSetting("Abort Break Bypass",false,()->true);
    BooleanSetting AnimPacketBy = new BooleanSetting("Matrix Bypass",true,()->true);
    NumberSetting APBC = new NumberSetting("Matrix Packets", 2, 0, 10, 1,()->AnimPacketBy.isEnabled());
    BooleanSetting MB = new BooleanSetting("Multiple breaks",false,()->true);
    NumberSetting MBP = new NumberSetting("Multiple break packets", 10, 0, 100, 1,()->MB.isEnabled());
    BooleanSetting AddPackBy = new BooleanSetting("Addition bypass",false,()->true);
    NumberSetting AddPack = new NumberSetting("Addition Packets", 2, 0, 10, 1,()->AddPackBy.isEnabled());
    BooleanSetting StartBreakingBy = new BooleanSetting("StartBreaking bypass",false,()->true);


    public FastBreak() {
        super("FastBreak", "позволяет более быстро ломать блоки", Type.Player);

        this.addSettings( multVT,multVWT,MB,MBP,AnimPacketBy,APBC,ABB,AddPackBy,AddPack,StartBreakingBy);
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        skippacket=false;
        index1=0;
        blockdamage=0;
        super.onEnable();
    }

    @EventTarget
    public void onUpdate(EventTick event) {
        this.setSuffix(((int)multVT.getValue())+"% : "+((int)multVWT.getValue())+"%");
        PacketHelper.Values pc= Client.instance.packet;

        if(mc.gameMode.destroyProgress>0) {
            if(AddPackBy.isEnabled()) {
                for(int i=0;i<AddPack.getValue();i++) {
                    ACUtil.send117DuplPacket();
                }
            }
            if(AnimPacketBy.isEnabled()) {
                for(int i=0;i<APBC.getValue();i++) {
                    pc.sendPacket(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                }
            }


            ticks++;
            blockdamage=b.getDestroyProgress(mc.player, mc.player.level(), bp);
            maxdamage= 1/b.getDestroyProgress(mc.player, mc.player.level(), bp);

            double tboost=1-(multVT.getValue()/100);
            double notboost=1-(multVWT.getValue()/100);
            if(mc.player.getMainHandItem().get(DataComponents.TOOL)!=null && mc.player.getMainHandItem().get(DataComponents.TOOL).defaultMiningSpeed()>0) {
                if (mc.gameMode.destroyProgress >= tboost) {
                    mc.gameMode.destroyProgress= 2F;
                    mc.gameMode.continueDestroyBlock(mc.gameMode.destroyBlockPos, Direction.UP);
                }
            }else {
                if (mc.gameMode.destroyProgress>= notboost) {
                    mc.gameMode.destroyProgress = 2F;
                    mc.gameMode.continueDestroyBlock(mc.gameMode.destroyBlockPos, Direction.UP);
                }
            }




        }else {
            blockdamage=0; ticks=0; maxdamage=0;
        }
    }

    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        PacketHelper.Values pc= Client.instance.packet;
        Packet<?> packetp = e.getPacket();
        if(!e.isCancelled()) {
            if((e.getPacket() instanceof ServerboundPlayerActionPacket) && !skippacket) {
                ServerboundPlayerActionPacket c07 = (ServerboundPlayerActionPacket) packetp;

                if(c07.getAction()==ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && StartBreakingBy.isEnabled()) {
                    skippacket=true;  pc.sendPacket(c07); skippacket=false;
                }

                if(c07.getAction()==ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && ABB.isEnabled()) {
                    skippacket=true; 	  pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,new BlockPos(c07.getPos().getX(),c07.getPos().getY()+1,c07.getPos().getZ()),c07.getDirection())); skippacket=false;
                }
                if(c07.getAction()==ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK && MB.isEnabled()) {
                    for(int i=0;i<MBP.getValue();i++) {
                        skippacket=true;   pc.sendPacket(c07); skippacket=false;
                    }
                }
                if(c07.getAction()==ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                    b=  mc.level.getBlockState(c07.getPos());
                    bp=c07.getPos(); blockdamage=0; ticks=0; maxdamage=0;
                }
            }
        }
        }



}
