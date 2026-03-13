package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPost;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.player.NoFall;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.MoveUtil;
import base.client.helpers.utils.TimerUtil;
import net.minecraft.network.protocol.game.*;

public class VoidBouncer extends Module {

    boolean wasdamage=false;
    public ModeSetting hmode;
    NumberSetting MotionY = new NumberSetting("MotionY", 8f, 1f, 10f, 0.05f,()->true);
    BooleanSetting Ver188MM;

    private int ticks = 0;
    int index1=0;
    int index2=0;
    int index3=0;
    int index4=0;
    int index5=0;
    double indexd1=0;
    double indexd2=0;
    int groundstate=0;//0=nothing 1=noground 2=ground

    public VoidBouncer() {
        super("VoidBouncer", "Вы подлетаете, избегая бездну", Type.Movement);
        hmode = new ModeSetting("VoidBouncer Mode", "Matrix", () -> true, "Matrix", "Vanilla", "Vanilla");
        Ver188MM = new BooleanSetting("1.8.8", false, () ->  hmode.getCurrentMode().equals("Matrix"));
        addSettings(hmode,MotionY,Ver188MM);
    }

    @EventTarget
    public void onEMI(EventMoveInput eventMove) {
        String mode = hmode.getCurrentMode();
        if(mode.equals("Matrix")) {
            if(mc.player.getY()<-10) {

                //	eventMove.moveforward=0; 	eventMove.movestrafe=0;

            }
        }
    }

    @EventTarget
    public void Move(EventOnMovePost eventMove) {
        PacketHelper.Values pc= Client.instance.packet;
        if(ticks>0) {
            MoveUtil.limit2speed(0.15);
        }

    }
    @EventTarget
    public void Tick(EventTick eventMove) {
        if(groundstate==1){
            mc.player.setOnGround(false); groundstate=0;
        }else if(groundstate==2){
            mc.player.setOnGround(true);groundstate=0;
        }

    }
    @EventTarget
    public void onPreMotion(EventPreMotion event) {
        String mode = hmode.getCurrentMode();
        PacketHelper.Values pc=Client.instance.packet;
        if (mode.equalsIgnoreCase("Matrix")) {
            if(mc.player.getY()<-55 ) {
                TimerUtil.setTimerspeed(0.5);
              }
            if(ticks==1) {
                groundstate=1;mc.player.setOnGround(false);
                event.setOnGround(false);
                if(index1==0) {
                 MoveUtil.setmotY(MotionY.getValue());
                    indexd1=0;
                    index4=0;
                    TimerUtil.setTimerspeed(0.75);
                    index5=1;
                }
                if(index1==1 && index4==0) {
                    indexd1=mc.player.getDeltaMovement().y;
                } 
                if(mc.player.getY()>-35) {
                    TimerUtil.reset(); wasdamage=false;   ticks = 0; index1=0;     this.index1=0;   this.index2=0;  this.index3=0;  this.index4=0;   this.index5=0;  this.indexd1=0;    this.indexd2=0;
                }


                index4++;
                index1++;
            }else {
                this.index1=0;   this.index2=0;  this.index3=0;  this.index4=0;   this.index5=0;  this.indexd1=0;    this.indexd2=0;
            }


        }

    }

    @EventTarget
    public void onReceivePacketPost(EventReceivePacketPost event) {
        String mode = hmode.getOptions();
        PacketHelper.Values pc= Client.instance.packet;


        if (mode.equalsIgnoreCase("Vanilla")) {
            boolean iss12=((event.getPacket() instanceof ClientboundExplodePacket) || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player.getId()));
            if(mc.player.getY()<-60 && iss12 && wasdamage) {
                wasdamage=false;  	MoveUtil.setmotY(MotionY.getValue());
            }
        }


    }

    @EventTarget
    public void onReceivePacketPre(EventReceivePacketPre event) {
        String mode = hmode.getOptions();
        PacketHelper.Values pc= Client.instance.packet;
        if (event.getPacket() instanceof ClientboundSetHealthPacket) {
            wasdamage=true;
        }

        if (mode.equalsIgnoreCase("Matrix")) {
            boolean iss12=((event.getPacket() instanceof ClientboundExplodePacket) || ((event.getPacket() instanceof ClientboundSetEntityMotionPacket) && ((ClientboundSetEntityMotionPacket) event.getPacket()).getId() == mc.player.getId()));


            if(iss12 && mc.player.getY()<10) {
                event.setCancelled(true);
            }
            if( mc.player.getY()<-60 &&
                    iss12 && wasdamage) {
                ticks=1;
            }



            if(event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                ClientboundPlayerPositionPacket p=(ClientboundPlayerPositionPacket) event.getPacket();
                if(ticks==1 ) {

                    event.setCancelled(true);
                    mc.player.setPos(p.change().position().x, p.change().position().y, p.change().position().z);
                     if(Ver188MM.isEnabled()){
                        pc.sendPacket(new ServerboundMovePlayerPacket.Pos(p.change().position().x, p.change().position().y, p.change().position().z,  false, false), 10, true);
                    }else{
                        pc.LastTpNum++;   pc.sendPacket(new ServerboundAcceptTeleportationPacket(p.id()));
                    }
                    if(indexd1!=0) {
                        MoveUtil.setmotY(indexd1);


                    }
                    if(Client.instance.featureManager.getModuleByClass(NoFall.class).getState()){
                        pc.sendPacket(new ServerboundMovePlayerPacket.StatusOnly(false,mc.player.horizontalCollision),10);
                    }


                    MoveUtil.strafe(0.1);
                    MoveUtil.smartstrafe();
                    index5=0;
                    groundstate=1;mc.player.setOnGround(false);
                    index4=0;
                    index3++;
                }
                if(index3>=2) {
               TimerUtil.reset();	wasdamage=false;   ticks = 0; index1=0;     this.index1=0;   this.index2=0;  this.index3=0;  this.index4=0;   this.index5=0;  this.indexd1=0;    this.indexd2=0;
                }


            }

        }

    }





    @Override
    public void onEnable() {
        ticks = 0; index1=0;
        this.index1=0;
        this.index2=0;
        this.index3=0;
        this.index4=0;
        this.index5=0;
        this.indexd1=0;
        this.indexd2=0;


        wasdamage=false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        TimerUtil.reset();

        super.onDisable();
    }
}
