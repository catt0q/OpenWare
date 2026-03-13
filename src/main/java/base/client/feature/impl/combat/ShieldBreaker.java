package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.TimerHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.AxeItem;

public class ShieldBreaker extends Module {

    double xf2;
    double yf2;
    double zf2;
    public TimerHelper timerr= new TimerHelper();
    int index1;
    int index2;
    int index3;
    int state=0;
    int axeslot=0;
    int prevslot=0;

    float yawf2;
    float pitchf2;

    public static NumberSetting relodams;
    public static NumberSetting tickstochange;

    int slpacketsd=-1;

  //  Packet<?> slpacketsd=null;

    public static BooleanSetting InvCheck;
    public static ModeSetting Mode;
//item.get(DataComponents.USE_COOLDOWN)


    public ShieldBreaker() {
        super("ShieldBreaker", "Ломает щиты", Type.Combat);

        Mode = new ModeSetting("Mode", "Always", () -> true, "Check", "Always" );


        relodams = new NumberSetting("Reload ms", 200, 0, 2000, 100,()->true);
        tickstochange = new NumberSetting("Change Time Ticks", 2, 0, 5, 1,()->true);

        InvCheck = new BooleanSetting("Check Inventory",false,()->true);



        this.addSettings(Mode,tickstochange,relodams,InvCheck);
    }

    public void onEnable() {
        index1=100;
        index2=0;
        index3=0; axeslot=0; prevslot=0; state=0;
        this.xf2 = 0.0;
        this.yf2 = 0.0;
        this.zf2 = 0.0;
        this.yawf2 = 0.0f;
        this.pitchf2 = 0.0f;    slpacketsd=-1;
        timerr.reset();
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();

    }





    @EventTarget
    public void onPrePacket(EventSendPacketCancel event) {

        if(!event.isCancelled()) {
            Packet<?> packetp=event.getPacket();
            if (packetp instanceof ServerboundSetCarriedItemPacket) {
                if(index1<tickstochange.getValue() && index2!=0) {
                    event.setCancelled(true);
                }
            }
            if (packetp instanceof ServerboundInteractPacket) {
                PacketHelper.Values pc = Client.instance.packet;
                ServerboundInteractPacket c02=(ServerboundInteractPacket) packetp;

                if (packetp instanceof ServerboundInteractPacket) {
                    base.mixin.client.accessors.ServerboundInteractPacketAccesor ac2 = (base.mixin.client.accessors.ServerboundInteractPacketAccesor) c02;
                    ServerboundInteractPacket.Action action = ac2.getAction();
                    if(action.getType().equals(ServerboundInteractPacket.ActionType.ATTACK)) {
                    Entity ent=mc.level.getEntity(ac2.getEntityId());
                    boolean needreason=true;
                     //   ChatHelper.addChatMessage("yes + "+(ent!=null)+" "+(ent instanceof Player)+" "+((Player)ent).isUsingItem());

                  boolean check=Mode.getCurrentMode().equals("Check");

                    if((ent!=null && ent instanceof Player  && (((Player)ent).isBlocking() || !check)
                            //&&    RotationUtils.yawdiff(ent.getYaw(),  RotationUtils.addyaw(getRotationsNoMot2(ent)[0], 180) )<=125
                    ) || !needreason) {



                        if(!(mc.player.getMainHandItem().getItem() instanceof AxeItem)) {
                            if(timerr.hasTimeElapsed((long) relodams.getValue(), false) && state==0) {
                                boolean hasaxeinhotbar=false;
                                boolean hasaxeininventory=false;
                                prevslot=mc.player.getInventory().getSelectedSlot();
                                for(int i=0;i<9;i++) {
                                    if(mc.player.getInventory().getItem(i).getItem() instanceof AxeItem) {
                                        index1=0;
                                        axeslot=i;
                                        pc.sendPacket(new ServerboundSetCarriedItemPacket(i));
                                        hasaxeinhotbar=true;
                                        state=1;

                                        break;
                                    }


                                }

                                if(!hasaxeinhotbar && InvCheck.isEnabled()) {

                                    for(int i=0;i<mc.player.containerMenu.slots.size();i++) {



                                        if(mc.player.containerMenu.getSlot(i).getItem().getItem() instanceof AxeItem) {
                                            index1 = 0;
                                            axeslot = i;
                                            hasaxeininventory = true;


                                            mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, i, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                                            mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));

                                            slpacketsd=i;
                                            index3 = axeslot;

                                            state = 2;



                                            break;

                                        }
                                    }
                                }
                                if(hasaxeinhotbar || hasaxeininventory) {
                                   // ChatHelper.addChatMessage("Break Shield");
                                }
                            }
                        }

                    }

                }




            }



        }

        }


    }


    @EventTarget
    public void onPostPacket(EventPreMotion event) {
        if(state==1) {PacketHelper.Values pc = Client.instance.packet;
            if(index1>=tickstochange.getValue()) {
              //  mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slpacketsd, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
             //   mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));

                pc.sendPacket(new ServerboundSetCarriedItemPacket(prevslot));
                timerr.reset(); state=0; index1=0;
            }
            index1++;
        }

        else if(state==2) {PacketHelper.Values pc = Client.instance.packet;
            if(index1>=tickstochange.getValue()) {
                mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slpacketsd, mc.player.getInventory().getSelectedSlot(), ClickType.SWAP, mc.player);
                mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));

                timerr.reset(); state=0; index1=0;

            }
            index1++;
        }

    }







    public float[] getRotationsNoMot2(Entity e) {


        final float deltaX = (float)((e.getX()) - mc.player.getX());
        final float deltaY = (float)((e.getY()+1) - ( mc.player.getY() + mc.player.getEyeHeight()));
        final float deltaZ = (float)((e.getZ() ) - mc.player.getZ());
        final float distance = (float)(Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));
        float yaw = (float)Math.toDegrees(-Math.atan(deltaX / deltaZ));
        final float pitch = (float)(-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float)(90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float)(-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if(yaw<-360) {
            yaw+=360;
        }
        if(yaw>360) {
            yaw-=360;
        }

        return new float[] { yaw, pitch };
    }






}
