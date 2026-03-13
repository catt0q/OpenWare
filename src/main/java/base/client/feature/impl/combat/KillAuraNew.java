package base.client.feature.impl.combat;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.game.EventLook;
import base.client.event.events.impl.game.EventRunGameLoop;
import base.client.event.events.impl.game.EventTick;
import base.client.event.events.impl.input.EventMoveInput;
import base.client.event.events.impl.motion.EventOnJump;
import base.client.event.events.impl.motion.EventOnMove;
import base.client.event.events.impl.motion.EventPreMotion;
import base.client.event.events.impl.packet.EventReceivePacketPre;
import base.client.event.events.impl.packet.EventSendPacketCancel;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.movement.MoveFix;
import base.client.feature.impl.movement.TargetStrafe;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.friend.Friend;
import base.client.helpers.impl.math.DeltaRotation;
import base.client.helpers.impl.math.MathematicHelper;
import base.client.helpers.impl.misc.ChatHelper;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.impl.rotation.Rotation;
import base.client.helpers.utils.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static base.client.feature.impl.client.CombatTargets.isTarget;
import static base.client.helpers.utils.RotationUtils.*;

public class KillAuraNew extends Module {
    public TimerHelper lastattacktimer = new TimerHelper();
    public TimerHelper rottimer = new TimerHelper();


    public static Entity lasttarget = null;

    float nextcps = 0;

    boolean lastyawfocus = false;
    boolean lastaccepted = false;

    float lastyawdiff = 0;
    float lastpitchdiff = 0;

    int ticksfromlasttargetupdate = 0;

    int attackattempts = 0;

    int index2 = 0;

    int startcircles = 0;

    boolean nextC04 = false;

    boolean cpsup=false;

    int completeclicks = 0;


    boolean isautoblocking=false,wasblocking=false;

    int blockingticks=0;


    List<Float> cpsarray=new LinkedList<>();

    List<Rotation> Rotations=new LinkedList<>();
    public static NumberSetting range = new NumberSetting("AttackRange", 3f, 2.0f, 6f, 0.001f, () -> true);
    BooleanSetting PerfectHit = new BooleanSetting("PerfectHit","Бьёт в зоне AttackRange, игнорируя ClickRange", false, () -> true);
    NumberSetting Clickrange = new NumberSetting("ClickRange", 1.0f, 0.0f, 4.0f, 0.001f, () -> (!PerfectHit.isEnabled()));
    NumberSetting Prerange = new NumberSetting("PreRange", 2.0f, 0.0f, 4.0f, 0.001f, () -> true);

    ModeSetting swingMode = new ModeSetting("Swing Mode", "Default", () -> true, "Default", "None", "Packet");
    ModeSetting AInteractionHand = new ModeSetting("InteractionHand", "Right", () -> (swingMode.getCurrentMode() != "None"), "Right", "Left");

    ModeSetting Mode = new ModeSetting("Mode", "Switch", () -> true, "Switch", "Fickle", "None");
    NumberSetting TicksToChangeTarget = new NumberSetting("Ticks To Change Target", 3, 0, 10, 1, () -> !Mode.getCurrentMode().equals("Multi"));

    ModeSetting Raycast = new ModeSetting("Raycast", "Old", () -> true, "Old", "New", "Minecraft", "None");

    BooleanSetting mfix= new BooleanSetting("MoveFix","Также требуется включить модуль MoveFix", true, () -> true);

    ModeSetting autoblock = new ModeSetting("AutoBlock", "None", () -> true, "Shield","Sword","RightClick",  "None");
    NumberSetting abrange = new NumberSetting("AutoBlock Range","Не учитывается дальность атаки", 5f, 2.0f, 8f, 0.001f, () -> !autoblock.getCurrentMode().equals("None"));
    ModeSetting abmode = new ModeSetting("AutoBlock Mode", "ReBlock", () -> autoblock.getCurrentMode().equals("Sword"), "ReBlock", "Vanilla");
    ModeSetting  asmode = new ModeSetting("AutoShield Mode", "ReBlock", () -> autoblock.getCurrentMode().equals("Shield"), "ReBlock", "Vanilla","MLUnbreak1");

    ModeSetting ClickType = new ModeSetting("ClickType", "OnCooldown", () -> true, "OnCooldown", "Clicking", "Auto");

    ModeSetting CPSMode = new ModeSetting("CPS Mode", "Random", () -> ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto"), "Random", "Smooth", "UpDown");
    NumberSetting MaxClicksPerTick = new NumberSetting("Max Clicks Per Tick", 2, 1, 20, 1, () -> ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto"));
    NumberSetting CpsMin = new NumberSetting("Min Cps", 12, 1, 40, 1, () -> ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto"));
    NumberSetting CpsMax = new NumberSetting("Max Cps", 15, 1, 40, 1, () -> ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto"));
    BooleanSetting DoubleClicking = new BooleanSetting("DoubleClicking", false, () -> ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto"));
    NumberSetting DoubleClickChance = new NumberSetting("DoubleClick Chance", 60, 0, 100, 1, () -> (ClickType.getCurrentMode().equals("Clicking") || ClickType.getCurrentMode().equals("Auto")) && DoubleClicking.isVisible() && DoubleClicking.isEnabled());







    BooleanSetting ResetHurtTime = new BooleanSetting("ResetHurtTime", false, () -> true);
    NumberSetting   NewHurtTime = new NumberSetting("New HurtTime", 10, 0, 10, 1, () -> ResetHurtTime.isEnabled());


    NumberSetting   MaxHurtTime = new NumberSetting("Max HurtTime", 10, 0, 10, 1, () -> true);
    NumberSetting  HitChance = new NumberSetting("Hit Chance", 100, 0, 100, 1, () -> true);


    ModeSetting Sort = new ModeSetting("Sort", "Distance", () -> true, "Distance", "Health", "Angle","HurtTime", "None");

    ModeSetting RotMode = new ModeSetting("Rotation Mode", "Polar", () -> true, "Polar", "Matrix1","NewMatrix","GrimPacket","GrimSnap", "Grim", "Intave", "Intave2","NoRot");
    NumberSetting FOV = new NumberSetting("FOV", 180, 1, 180, 0.1F, () -> true);
    ModeSetting LockMode = new ModeSetting("Lock Mode", "None", () -> !Mode.getCurrentMode().equals("Multi"), "None", "Post", "Always", "Silent");

    ModeSetting GroundCondition = new ModeSetting("Ground Condition", "Always", () -> true, "Only Ground", "Only Air","Smart", "Always");


    BooleanSetting GCDFix = new BooleanSetting("GCD Fix", true, () -> RotMode.isVisible() && (RotMode.getCurrentMode().equals("Custom")));
    BooleanSetting RotLimitFix = new BooleanSetting("RotLimit Fix", true, () -> RotMode.isVisible() && (RotMode.getCurrentMode().equals("Custom")));

    BooleanSetting OnlyCrit = new BooleanSetting("Only Crit", false, () -> true);
    ModeSetting AutoOnlyCrit = new ModeSetting("Auto Only Crit","ОнлиКрит вкл/выкл в зависимости от системы кликов", "Always", () -> OnlyCrit.isEnabled(), "Always","Only 1.9+", "Only 1.8-");
    ModeSetting OnlyCritSR = new ModeSetting("OnlyCrit Sprint Reset","Если не None, то попытается выключить спринт чтобы дать крит", "None", () -> OnlyCrit.isEnabled(), "None", "SimplePacket","Legit");


  BooleanSetting walls = new BooleanSetting("Walls", "Позволяет бить сквозь стены", true, () -> true);
// BooleanSetting usingItemCheck = new BooleanSetting("Using Item", "Не бьет если вы используете меч, еду и т.д", false, () -> true);

    ModeSetting usingItemCheck = new ModeSetting("Using Item","Не бьет если вы используете меч, еду и т.д", "Always", () -> true, "Only Consumable","Always",
            "None");

    BooleanSetting openInventoryCheck = new BooleanSetting("Opened Inventory", "Не крутиться когда открыт инвентарь", true, () -> true);


    BooleanSetting weaponOnly = new BooleanSetting("Weapon Only", "Позволяет бить только с оружием в руках", false, () -> true);

    BooleanSetting autoDisable = new BooleanSetting("Auto Disable", "Автоматически выключает киллаура при смерти и т.д", true, () -> true);
    BooleanSetting ShowSwingNoHit = new BooleanSetting("ShowSwingNoHit","Покажет анимацию взмаха, даже если удара не было", true, () -> (!PerfectHit.isEnabled()));



    public KillAuraNew() {
        super("Aura", "Автоматически бьет сущностей вокруг тебя", Type.Combat);
        this.addSettings(

//========================================================================================
//Настройка выбора цели
                //Настройка дальностей
                range, PerfectHit, Clickrange, Prerange,

                //Определение цели
                Mode, TicksToChangeTarget, Sort,   FOV, LockMode,

//========================================================================================

//========================================================================================
                Raycast,
                mfix,

//========================================================================================
                autoblock,abrange,abmode,asmode,

//========================================================================================
//Настройки вращения руки
                swingMode, AInteractionHand,
//========================================================================================


//========================================================================================
//Настройки ротации
                RotMode, GCDFix, RotLimitFix,

                OnlyCrit,AutoOnlyCrit,OnlyCritSR,


//Настройки мультиротации


//========================================================================================

//========================================================================================
//Настройки поведения камеры при вращении

//========================================================================================


//========================================================================================
//Настройки корректности наведения для атаки

//========================================================================================


//========================================================================================
//Настройка промежутков атаки
                //Тип пвп и кпс мод
                ClickType, CPSMode, MaxClicksPerTick, CpsMin, CpsMax, DoubleClicking, DoubleClickChance,
                ResetHurtTime,NewHurtTime,MaxHurtTime,HitChance,

                GroundCondition,ShowSwingNoHit,
                //Условия совершения клика

               walls, openInventoryCheck, weaponOnly, usingItemCheck, autoDisable


//=======================================================================================


        );

    }

    @Override
    public void onEnable() {
        nextC04 = false;
        lasttarget = null;
        lastyawfocus = false;
        lastaccepted = false;
        ticksfromlasttargetupdate = 0;
        lastyawdiff = 0;
        cpsup=false;
        lastpitchdiff = 0;
        lastattacktimer.reset();
        index2 = 0;
        completeclicks = 0;
        isautoblocking=false;wasblocking=false;
         blockingticks=0;

        if (mc.player != null) {
            lastyaw = mc.player.getYRot();
            lastpitch = mc.player.getXRot();
            startcircles = (int) (mc.player.getYRot() / 360);
        }
        Rotations.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        lasttarget = null;
        if (mc.player != null) {
            if(mfix.isEnabled()) {
                MoveFix.needspoofyaw = false;
            }

            if (lastaccepted && !LockMode.getCurrentMode().equals("None")) {
                mc.player.setYRot(lastyaw);
                mc.player.setXRot(lastpitch);
                lastaccepted = false;
            }

        }
        lockedrots=false;


        if(wasblocking){
            mc.options.keyUse.setDown(false);
        }


        TargetStrafe.updatets(lasttarget);
        super.onDisable();
    }




    @EventTarget
    public void onReceivePacketPre(EventReceivePacketPre e) {
        PacketHelper.Values pc = Client.instance.packet;
        if (e.getPacket() instanceof ClientboundEntityEventPacket) {
            ClientboundEntityEventPacket c1= (ClientboundEntityEventPacket) e.getPacket();


        }
    }




    @EventTarget
    public void onTick(EventTick e) {
        boolean invcheck = !openInventoryCheck.isEnabled() || (mc.screen == null || !(mc.screen instanceof ContainerScreen));
        PacketHelper.Values pc = Client.instance.packet;

        //find target start
        List<Entity> targets = getTargets();

        if (!targets.isEmpty()) {
            Entity prevlasttarget = lasttarget;

            if (lasttarget == null || !lasttarget.isAlive() || !targets.contains(lasttarget)) {
                lasttarget = null;
                nextcps = CpsMin.getValue();
            }
            Entity target = null;
            target = targets.get(0);


            boolean wasnull = (lasttarget == null);
            if (wasnull) {
                attackattempts = 0;
                lasttarget = target;
            }

            if (Mode.getCurrentMode().equals("None")) {
                lasttarget = target;
            } else if (Mode.getCurrentMode().equals("Fickle") && !wasnull && (attackattempts >= 1 || !canAttack((LivingEntity) lasttarget)) || (EntityUtil.getMinDistanceToEntity(lasttarget, mc.player) > range.getValue())) {
                for (int qqi = 0; qqi < targets.size(); qqi++) {
                    if (lasttarget.getId() != targets.get(qqi).getId()) {
                        attackattempts = 0;
                        lasttarget = targets.get(qqi);
                        break;
                    }
                }
            }
            if (prevlasttarget != lasttarget) {
                attackattempts = 0;
                ticksfromlasttargetupdate = (int) TicksToChangeTarget.getValue();
            }
            target = lasttarget;

        } else {
            lasttarget = null;
        }
          //find target end

        //Calc cps start(чтобы использовать для расчёт ротации если надо)
  int currcps = 0;
        if(isClicking(mc.player.getMainHandItem())){
            currcps = (int) Math.min(completeclicks,MaxClicksPerTick.getValue());
            completeclicks -= currcps;
        }else{
            currcps = (CombatUtil.passcooldown() ? 1 : 0);completeclicks=currcps;
        }
 //Calc cps end





        lockedrots=lasttarget != null && !(RotMode.getCurrentMode().equals("NoRot") || RotMode.getCurrentMode().toLowerCase().contains("packet"));

        //Rotations start
        float newyaw=mc.player.getYRot(),newpitch = mc.player.getXRot();
        if (lasttarget != null && invcheck) {


            float yaw1 = this.getPerfectRot(lasttarget)[0];
            float pitch1 = this.getPerfectRot(lasttarget)[1];
            double diffyaw = RotationUtils.yawdiff(lastyaw, yaw1);
            double diffpitch = RotationUtils.pitchdiff(lastpitch, pitch1);
            float yaw2 = yaw1;
            float pitch2 = pitch1;
              newyaw = yaw1;
              newpitch = pitch1;
            double a = 0, b = 0;


            switch (RotMode.getCurrentMode()) {
                case ("Polar"):
                    if(RotationUtils.pitchdiff(mc.player.getXRot(),lastpitch)>0) {
                        //   ChatHelper.addChatMessage("1 " + RotationUtils.pitchdiff(mc.player.getXRot(), lastpitch));
                    }

                yaw1 = this.getFtRot(lasttarget)[0];
                    pitch1 = this.getFtRot(lasttarget)[1];
                    float pitch4 = this.getPerfectRot(lasttarget)[1];
                    yaw1 = (float) (this.getPerfectRot(lasttarget)[0]-1+Math.random()*2);
                    double diffpitchft4 = RotationUtils.pitchdiff(lastpitch, pitch1);
                    double inetiakoefy=0;
                    double inetiakoefp=0.1;
                    double diffyawft = RotationUtils.yawdiff(lastyaw, yaw1);
                    double diffpitchft = RotationUtils.pitchdiff(lastpitch, pitch1);

                    double baseval = (TraceUtil.KillauraTraceFind(7D, lastyaw, lastpitch, lasttarget, 0D, 10)) ? 0 : 0;

                    double speedyaw = Math.pow((diffyawft), 1+Math.random()*0.1);

                    double diffy = Math.min(Math.random()*5+55,speedyaw);
                    double diffp=7+Math.random()*2;
if(MoveUtil.getspeed2()>0.281){
    diffy/=6; diffp/=6;
}


                   //   diffp=Math.abs(mc.player.getDeltaMovement().y())>0.1 ? Math.pow((diffpitchft), 1.5+Math.random()*0.05) : Math.pow((diffpitchft), 0.6+Math.random()*0.2);

                  //  diffp= (float) Math.max(3+Math.random()*1,diffp);
                 //   diffy= (float) Math.max(9+Math.random()*3,diffy);

                      if(diffpitchft4>5){
                       //   diffp+=4*Math.random()*4;
                      }
                    //       diffp = 4; lastpitchdiff=0;


                    //  diffy = diffyawft*(0.8);




                    float noise = ((float) Math.sin(diffp) * 0.5f + 0.5f) * 2 - 1;


double minkoefy=0.0; double minkoefyl=0.0;
double minkoefp=0.0; double minkoefpl=0.0;




                    //  if((diffy<minkoef && diffp<minkoef)){      diffy=0;diffp=0; }
                  if((diffy<minkoefy)){      diffy=0; }   if((diffp<minkoefp)){   diffp=0; }
                    lastyawdiff *= inetiakoefy;
                    lastpitchdiff *= inetiakoefp;

    yaw2 = yaw1 + (float) a;
    pitch2 = pitch1 + (float) b;
    yaw2 = (float) (yaw1);
    pitch2 = pitch1 + (float) b;
    yaw2 = RotationUtils.addyaw(lastyaw, yaw2, (float) (diffy + a), false, true) + (float) a;
    pitch2 = RotationUtils.addpitch(lastpitch, pitch2, (float) diffp, false) + (float) b;
 //  if (lastyawdiff<minkoef && lastpitchdiff< minkoef) {   lastyawdiff = 0;     lastpitchdiff = 0;   }
                    if (lastyawdiff<minkoefyl) {   lastyawdiff = 0;  }   if (lastpitchdiff< minkoefpl) { lastpitchdiff = 0;   }


  //ChatHelper.addChatMessage("diffy "+diffy+ "         diffp "+diffp);          ChatHelper.addChatMessage("lastyawdiff "+lastyawdiff+ "         lastpitchdiff "+lastpitchdiff);
    yaw2 += lastyawdiff;
    pitch2 += lastpitchdiff;

    if(lastpitchdiff==0 && diffp==0){
        noise=0;
    }

                 //   pitch2+= noise * 0.1f;





                  //  yaw2=newyaw;  pitch2=newpitch;
                    if (GCDFix.isEnabled()) {
                        newyaw = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[0];
                        newpitch = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[1];
                    }

                    lastyawdiff = RotationUtils.yawdiff(lastyaw, newyaw);
                    if (Math.abs(RotationUtils.yawdiff(RotationUtils.addyaw(lastyaw, lastyawdiff), newyaw)) > 0.001) {
                        lastyawdiff *= -1;
                    }
                    lastpitchdiff = RotationUtils.pitchdiff(lastpitch, newpitch);
                    if (Math.abs(RotationUtils.pitchdiff((lastpitch + lastpitchdiff), newpitch)) > 0.001) {
                        lastpitchdiff *= -1;
                    }

                    if(RotationUtils.pitchdiff(newpitch,lastpitch)>0) {
                    //    ChatHelper.addChatMessage("2 " + RotationUtils.pitchdiff(newpitch, lastpitch));
                    }

                    int h=0; boolean findfrt=false;
                    Rotations.add(new Rotation(newyaw,newpitch));
if(h>2) {
    for (int i = Rotations.size() - 1; i >= 0; i--) {
        h--;
        if (h == 0) {
           //    lastyaw = Rotations.get(i).getX();
            lastpitch = Rotations.get(i).getY();
            findfrt = true;
            break;
        }
    }
}
                    if(!findfrt){
                        //    lastyaw = mc.player.getYRot();
                             //lastpitch = mc.player.getXRot();
                    }
                    lastyaw = newyaw;
                    lastpitch = newpitch;
                    // lastpitch=mc.player.getXRot();
          //   lastyaw=mc.player.getYRot();
//ChatHelper.addChatMessage(""+pc.LastPitch);
break;

                case ("Intave"):


                    AABB ioriginBB = lasttarget.getBoundingBox();
                    AABB ibox = new AABB(ioriginBB.minX + lasttarget.getBbWidth() * 0.4, ioriginBB.minY + lasttarget.getBbHeight() * 0.6, ioriginBB.minZ + lasttarget.getBbWidth() * 0.4,
                            ioriginBB.minX + lasttarget.getBbWidth() * 0.6, ioriginBB.minY + lasttarget.getBbHeight() * 0.8, ioriginBB.minZ + lasttarget.getBbWidth() * 0.6
                    );


                    Rotation iLastRot = new Rotation(lastyaw, lastpitch);
                    Vec3 ineedPoint = RotationUtils.getNearestVec(ibox, iLastRot);

                    float ddy=1;float ddp=1;
                    if(mc.player.hurtTime>7 || (mc.player.onGround() && MoveUtil.getspeed2()>0.25)){
                        ddy/=3; ddp/=3;
                    }

                    DeltaRotation idelta = new DeltaRotation(
                            iLastRot,
                            RotationUtils.getRotation(ineedPoint)
                    )
                            .multi(0.7F - (float) (Math.random() * 0.1), 0.1F + (float) (Math.random() * 0.1))

                            .multi(ddy,ddp)

                            .limit((float) (41 - Math.random() * 0.5), (float) (21 - Math.random() * 0.5))
                            .fix();
                    if (Math.hypot(idelta.getX(), idelta.getY()) == 0) {
                        rottimer.reset();
                    }

                    if (!rottimer.hasTimeElapsed(110, false)) {
                    idelta.setX(0);
                      idelta.setY(0);
                    }

                    iLastRot = iLastRot.plusRotation(idelta, true);
                    lastyaw = iLastRot.getX();  lastpitch = iLastRot.getY();
     break;



                case ("GrimSnap"):

                    if(currcps==0 || !invcheck || !needhit((LivingEntity) lasttarget) || EntityUtil.getMinDistanceToEntity(lasttarget,mc.player)>(range.getValue()+Prerange.getValue())){
                        yaw1=mc.player.getYRot();pitch1=mc.player.getXRot();
                        yaw2=mc.player.getYRot();pitch2=mc.player.getXRot();
                    }



                    float diffygs = RotationUtils.yawdiff(lastyaw, yaw1); float diffpgs =RotationUtils.pitchdiff(lastpitch, pitch1);
                    yaw2 = RotationUtils.addyaw(lastyaw, yaw2,   diffygs, true,true);
                    pitch2=RotationUtils.addpitch(lastpitch,pitch2,  diffpgs,true);
                   if (GCDFix.isEnabled()) {    newyaw = ACUtil.GCDFix(lastyaw, lastpitch, yaw2, pitch2)[0];  newpitch = ACUtil.GCDFix( lastyaw, lastpitch, yaw2, pitch2)[1];   }
                    newpitch=Math.clamp(newpitch,-90,90);       lastyaw = newyaw; lastpitch = newpitch;
                    break;
                case ("Grim"):

                    float diffyg = RotationUtils.yawdiff(lastyaw, yaw1); float diffpg =RotationUtils.pitchdiff(lastpitch, pitch1);
                    yaw2 = RotationUtils.addyaw(lastyaw, yaw2,  diffyg, true,true);
                    pitch2=RotationUtils.addpitch(lastpitch,pitch2, diffpg,true);
            if (GCDFix.isEnabled()) {    newyaw = ACUtil.GCDFix(lastyaw,  lastpitch, yaw2, pitch2)[0];  newpitch = ACUtil.GCDFix(lastyaw,lastpitch, yaw2, pitch2)[1];   }
                    newpitch=Math.clamp(newpitch,-90,90);       lastyaw = newyaw; lastpitch = newpitch;
   break;

                case ("GrimPacket"):

                    float diffygp = RotationUtils.yawdiff(lastyaw, yaw1); float diffpgp =RotationUtils.pitchdiff(lastpitch, pitch1);
                    yaw2 = RotationUtils.addyaw(lastyaw, yaw2, diffygp, true,true);
                    pitch2=RotationUtils.addpitch(lastpitch,pitch2,diffpgp,true);
                    if (GCDFix.isEnabled()) {    newyaw = ACUtil.GCDFix(lastyaw, lastpitch, yaw2, pitch2)[0];  newpitch = ACUtil.GCDFix(lastyaw, lastpitch, yaw2, pitch2)[1];   }
                    newpitch=Math.clamp(newpitch,-90,90);

                    lastyaw=mc.player.getYRot();
                    lastpitch=mc.player.getXRot();
                    break;

                case ("NewMatrix"):
                    double diffym = RotationUtils.yawdiff(lastyaw, yaw1)*0.9; double diffpm =RotationUtils.pitchdiff(lastpitch, pitch1)*0.75;
                    diffym=Math.min(diffym,90*(1/Math.sqrt(TimerUtil.getTimerspeed())));diffpm=Math.min(diffpm,18*(1/Math.sqrt(TimerUtil.getTimerspeed())));
                   yaw2 = RotationUtils.addyaw(lastyaw, yaw2, (float) diffym, true,false);
                  pitch2=RotationUtils.addpitch(lastpitch,pitch2,(float) diffpm,true);
              yaw2=newyaw; pitch2=newpitch;

                    AABB ioriginBB3 = lasttarget.getBoundingBox();
                    AABB ibox3 = new AABB(ioriginBB3.minX + lasttarget.getBbWidth() * 0.4, ioriginBB3.minY + lasttarget.getBbHeight() * 0.7, ioriginBB3.minZ + lasttarget.getBbWidth() * 0.4,
                            ioriginBB3.minX + lasttarget.getBbWidth() * 0.6, ioriginBB3.minY + lasttarget.getBbHeight() * 0.9, ioriginBB3.minZ + lasttarget.getBbWidth() * 0.6
                    );


                    Rotation iLastRot3 = new Rotation(lastyaw, (float) (lastpitch+Math.random()));
                    Vec3 ineedPoint3 = RotationUtils.getNearestVec(ibox3, iLastRot3);

                    DeltaRotation idelta3 = new DeltaRotation(
                            iLastRot3,
                            RotationUtils.getRotation(ineedPoint3)
                    )
                             .multi(0.75F - (float) (Math.random() * 0.5), 0.5F - (float) (Math.random() * 0.5))
                             .limit((float) (80*(1/(TimerUtil.getTimerspeed())) - Math.random() * 0.5), (float) (15*(1/(TimerUtil.getTimerspeed())) - Math.random() * 0.5))
                            .fix()
                            ;

                    if (Math.hypot(idelta3.getX(), idelta3.getY()) == 0) {    rottimer.reset();   }

                    iLastRot3 = iLastRot3.plusRotation(idelta3, true);
                    float[] rots=ACUtil.GCDFix( lastyaw, lastpitch, yaw2, iLastRot3.getY());
                   if (GCDFix.isEnabled()) {    newyaw = rots[0];  newpitch = rots[1];   }
                    lastyaw = newyaw;lastpitch=newpitch;
     break;

                case ("Intave2"):

                    double diffym2 = RotationUtils.yawdiff(lastyaw, yaw1)*0.9; double diffpm2 =RotationUtils.pitchdiff(lastpitch, pitch1)*0.75;
                    diffym2=Math.min(diffym2,90*(1/Math.sqrt(TimerUtil.getTimerspeed())));diffpm2=Math.min(diffpm2,18*(1/Math.sqrt(TimerUtil.getTimerspeed())));
                    yaw2 = RotationUtils.addyaw(lastyaw, yaw2, (float) diffym2, true,false);
                    pitch2=RotationUtils.addpitch(lastpitch,pitch2,(float) diffpm2,true);
                    yaw2=newyaw;  pitch2=newpitch;
                    if (GCDFix.isEnabled()) {    newyaw = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[0];  newpitch = ACUtil.GCDFix((float) lastyaw, (float) lastpitch, yaw2, pitch2)[1];   }
                    lastyaw = newyaw;

                    AABB ioriginBB4 = lasttarget.getBoundingBox();
                    AABB ibox4 = new AABB(ioriginBB4.minX + lasttarget.getBbWidth() * 0.4, ioriginBB4.minY + lasttarget.getBbHeight() * 0.7, ioriginBB4.minZ + lasttarget.getBbWidth() * 0.4,
                            ioriginBB4.minX + lasttarget.getBbWidth() * 0.6, ioriginBB4.minY + lasttarget.getBbHeight() * 0.9, ioriginBB4.minZ + lasttarget.getBbWidth() * 0.6
                    );


                    Rotation iLastRot4 = new Rotation(lastyaw, lastpitch);
                    Vec3 ineedPoint4 = RotationUtils.getNearestVec(ibox4, iLastRot4);

                    DeltaRotation idelta4 = new DeltaRotation(
                            iLastRot4,
                            RotationUtils.getRotation(ineedPoint4)
                    )
                            .multi(0.75F - (float) (Math.random() * 0.5), 0.5F - (float) (Math.random() * 0.5))
                            .limit((float) (60*(1/(TimerUtil.getTimerspeed())) - Math.random() * 0.5), (float) (22*(1/(TimerUtil.getTimerspeed())) - Math.random() * 0.5))
                            .fix();
                    if (Math.hypot(idelta4.getX(), idelta4.getY()) == 0) {    rottimer.reset();   }
                    if (!rottimer.hasTimeElapsed(55, false)) {
                        idelta4.setX(0);     idelta4.setY(0);
                    }
                    iLastRot4 = iLastRot4.plusRotation(idelta4, true);
                    lastpitch = iLastRot4.getY();
                    break;




                case ("Matrix1"):
                    AABB ioriginBB2 = lasttarget.getBoundingBox();
                    AABB ibox2 = new AABB(ioriginBB2.minX + lasttarget.getBbWidth() * 0.45, ioriginBB2.minY + lasttarget.getBbHeight() * 0.7, ioriginBB2.minZ + lasttarget.getBbWidth() * 0.4,
                            ioriginBB2.minX + lasttarget.getBbWidth() * 0.55, ioriginBB2.minY + lasttarget.getBbHeight() * 0.9, ioriginBB2.minZ + lasttarget.getBbWidth() * 0.55
                    );


                    Rotation iLastRot2 = new Rotation(lastyaw, lastpitch);
                    Vec3 ineedPoint2 = RotationUtils.getNearestVec(ibox2, iLastRot2);

                    DeltaRotation idelta2 = new DeltaRotation(
                            iLastRot2,
                            RotationUtils.getRotation(ineedPoint2)
                    )
                            .multi(0.75F - (float) (Math.random() * 0.5), 0.5F - (float) (Math.random() * 0.5))
                            .limit((float) (80*(1/Math.sqrt(TimerUtil.getTimerspeed())) - Math.random() * 0.5), (float) (21*(1/Math.sqrt(TimerUtil.getTimerspeed())) - Math.random() * 0.5))
                            .fix();
                    if (Math.hypot(idelta2.getX(), idelta2.getY()) == 0) {
                        rottimer.reset();
                    }

                    if (!rottimer.hasTimeElapsed(55, false)) {
                        // idelta2.setX(0);     idelta2.setY(0);
                    }

                    iLastRot2 = iLastRot2.plusRotation(idelta2, false);
                    lastyaw = iLastRot2.getX();  lastpitch = iLastRot2.getY();

                    break;
            }

            //Rotations

//Post Rotation Actions
            if (mc.player.getYRot() != lastyaw || mc.player.getXRot() != lastpitch) {
                lastaccepted = true;
            }
            if (lastaccepted && (LockMode.getCurrentMode().equals("Always") || LockMode.getCurrentMode().equals("Silent"))) {
                mc.player.setYRot(lastyaw);
                mc.player.setXRot(lastpitch);
            }
            if(mfix.isEnabled()) {
                MoveFix.needspoofyaw = true;
                MoveFix.RealYaw = lastyaw;
            }

//Post Rotation Actions
        } else {
            if (lasttarget == null) {
                Rotations.clear();
                if (lastaccepted && !LockMode.getCurrentMode().equals("None")) {
                    mc.player.setYRot(lastyaw);
                    mc.player.setXRot(lastpitch);
                    lastaccepted = false;
                }
              //  lastyaw = mc.player.getYRot(); lastpitch = mc.player.getXRot();

                if(mfix.isEnabled()) {
                    MoveFix.needspoofyaw = false;
                    MoveFix.RealYaw = lastyaw;
                }
            }

        }
        //Rotations end


        //Sync target with other modules start
        TargetStrafe.updatets(lasttarget);

        //Sync target with other modules end










        if (lasttarget != null && invcheck) {

            //Autblock start
if(EntityUtil.getMinDistanceToEntity(mc.player,lasttarget)<abrange.getValue() && EntityUtil.canBlock(!autoblock.getCurrentMode().equals("Shield")
        ,!autoblock.getCurrentMode().equals("Sword"))){
    isautoblocking=true;pc.ticknotusingitem=0;
}else{
    isautoblocking=false; blockingticks=0;
}

            switch(autoblock.getCurrentMode()) {
                case ("Shield"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"):

                            break;
                    }
                    break;


                case ("Sword"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"):

                            break;
                    }
                    break;

            }



            //Autblock end





            //Attack,swing start
            if (!needhit((LivingEntity) lasttarget)) return;

            boolean wasswing = false;
            boolean wasattack = false;







float prevLastYaw=mc.player.getYRot();float prevLastPitch=mc.player.getXRot();

            switch (RotMode.getCurrentMode()){
                case ("GrimPacket"):
                  pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,pc.LastPosZ, newyaw,newpitch, pc.LastGround, Minecraft.getInstance().player.horizontalCollision),10);
pc.LastYaw=newyaw; pc.LastPitch=newpitch;
                    break;

            }

int resaveclicks=0;

            while (currcps > 0) {
                currcps--;

                boolean ispassraycast = (Raycast.getCurrentMode().equals("New") && TraceUtil.isLookingAtEntity(pc.LastYaw, pc.LastPitch, 0, 0, 0, lasttarget, range.getValue())
|| (Raycast.getCurrentMode().equals("Old") && (TraceUtil.KillauraTraceFind(range.getValue(),pc.LastYaw,pc.LastPitch,lasttarget,0.0D,20)   ))
                        || (Raycast.getCurrentMode().equals("Minecraft") && mc.crosshairPickEntity!=null && mc.crosshairPickEntity.getId() == lasttarget.getId())
              || Raycast.getCurrentMode().equals("None")
                );

               // boolean ispassraycastclick=(TraceUtil.KillauraTraceFind(Clickrange.getValue() + range.getValue(),lastyaw,lastpitch,lasttarget,0.0D,20));

                if ( ispassraycast && (Math.random()*100<HitChance.getValue())
                ) {



                    //Autblock start
                    if(EntityUtil.getMinDistanceToEntity(mc.player,lasttarget)<abrange.getValue() && EntityUtil.canBlock()){
                        isautoblocking=true;
                    }else{
                        isautoblocking=false; blockingticks=0;
                    }

                    switch(autoblock.getCurrentMode()) {
                        case ("Shield"):
                            switch(asmode.getCurrentMode()) {
                                case ("ReBlock"):
                                    if(isautoblocking) {
                                        blockingticks = -1;
                                        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN), 5, true);
                                    }
                                    break;
                            }
                            break;


                        case ("Sword"):
                            switch(autoblock.getCurrentMode()) {
                                case ("ReBlock"):
                                    if(isautoblocking) {
                                        blockingticks = -1;
                                        pc.sendPacket(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN), 5, true);
                                    }
                                    break;
                            }
                            break;

                    }



                    //Autblock end




                    switch (OnlyCritSR.getCurrentMode()){
                        case ("SimplePacket"):
                            if(mc.player.isSprinting()){
                                PacketHelper.Values.sendPacket(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                                mc.player.setSprinting(false);

                            }
                            break;
      }






                     attackattempts++;
                    mc.gameMode.attack(mc.player, lasttarget);

if(ResetHurtTime.isEnabled()){
    ((LivingEntity)lasttarget).hurtTime= (int) NewHurtTime.getValue();
}


                    pc.attackticks = 0;
                    wasattack = true;

                }

                if ((EntityUtil.getMinDistanceToEntity(lasttarget, mc.player) < (Clickrange.getValue() + range.getValue()) && !PerfectHit.isEnabled()) || wasattack) {
                    proccesswing(wasattack);
                    wasswing = true;
                }
                if(!wasswing && !wasattack){
                    resaveclicks++;
                }

            }
            completeclicks+=resaveclicks;
            completeclicks = (int) Math.min(completeclicks, MaxClicksPerTick.getValue());
            switch (RotMode.getCurrentMode()){
                case ("GrimPacket"):
                    pc.sendPacket(new ServerboundMovePlayerPacket.PosRot(pc.LastPosX, pc.LastPosY,pc.LastPosZ, prevLastYaw,prevLastPitch, pc.LastGround, Minecraft.getInstance().player.horizontalCollision),10);
                    pc.LastYaw=prevLastYaw; pc.LastPitch=prevLastPitch;
                    break;

            }


            //Attack,swing end



            //Autblock start
            if(EntityUtil.getMinDistanceToEntity(mc.player,lasttarget)<abrange.getValue() && EntityUtil.canBlock()){
                isautoblocking=true;
            }else{
                isautoblocking=false; blockingticks=0;
            }

            switch(autoblock.getCurrentMode()) {
                case ("Shield"):
                    switch(asmode.getCurrentMode()) {
                        case ("ReBlock"),("Vanilla"):
                            if(blockingticks<=0) {
                                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw, pc.LastPitch),5);
                                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.OFF_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw, pc.LastPitch),5);

                            }
                            break;

                        case ("MLUnbreak1"):
                            if(blockingticks<=0) {
                                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw, pc.LastPitch),5);
                                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.OFF_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw, pc.LastPitch),5);

                            }
                            break;


                    }
                    break;


                case ("Sword"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"),("Vanilla"):
                            if(blockingticks<=0) {
                                pc.sendPacket(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, mc.level.getBlockStatePredictionHandler().startPredicting().currentSequence(), pc.LastYaw, pc.LastPitch),5);
                       }
                            break;
                    }
                    break;

            }



            //Autblock end


        }else {
            isautoblocking=false; blockingticks=0;
        }






    }

    @EventTarget
    public void onMoveFix(EventOnMove e) {

    }

    @EventTarget
    public void onJump(EventOnJump e) {

    }



    @EventTarget
    public void onMotion(EventPreMotion e) {
        if (autoDisable.isEnabled() && (mc.player == null || !mc.player.isAlive())) {
            this.toggle();
            return;
        }
        //Rots
        if(lockedRots()) {
            e.setYaw(lastyaw);
            e.setPitch(lastpitch);
        }
        //Rots

        //Autoblock






            switch(autoblock.getCurrentMode()){
                case ("RightClick"):
                    if ((EntityUtil.canBlock(true,true) && isautoblocking)) {
   mc.options.keyUse.setDown(true);
                    } else if(wasblocking){
                        mc.options.keyUse.setDown(false);
                    }
     break;

                case ("Shield"):
                    switch(asmode.getCurrentMode()) {
                        case ("ReBlock"):
                            if ((EntityUtil.canBlock(false,true) && isautoblocking)) {
                                mc.options.keyUse.setDown(true);
                            } else if(wasblocking){
                                mc.options.keyUse.setDown(false);
                            }
                            break;
                        case ("MLUnbreak1"),("Vanilla"):
                            if ((EntityUtil.canBlock(true,true) && isautoblocking)) {
                                mc.options.keyUse.setDown(true);
                            }  else if(wasblocking){
                                mc.options.keyUse.setDown(false);
                            }

                             break;

                    }
                    break;


                case ("Sword"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"),("Vanilla"):
                            if ((EntityUtil.canBlock(true,false) && isautoblocking)) {
                                mc.options.keyUse.setDown(true);
                            } else if(wasblocking){
                                mc.options.keyUse.setDown(false);
                            }
                            break;
                    }
                    break;

            }









        //AutoBlock








        if (ticksfromlasttargetupdate > 0) {
            ticksfromlasttargetupdate--;
        }
        if(isautoblocking){
            blockingticks++;
        }
        wasblocking=isautoblocking;
        index2++;


    }

    @EventTarget
    public void oncancel(EventSendPacketCancel e) {
        PacketHelper.Values pc = Client.instance.packet;
        Packet<?> packetp = e.getPacket();

        if(e.getPacket() instanceof ServerboundPlayerActionPacket) {
            ServerboundPlayerActionPacket c07 = (ServerboundPlayerActionPacket) packetp;

            switch(autoblock.getCurrentMode()) {
                case ("Shield"):
                    switch(asmode.getCurrentMode()) {
                        case ("ReBlock"),("Vanilla"),("MLUnbreak1"):
                            if(isautoblocking || wasblocking) {
                                if (c07.getAction().equals(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) && e.permissionidstate < 5) {
                                    e.cancel();
                                }
                            }
                        break;
                    }
                    break;


                case ("Sword"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"),("Vanilla"):
                            if(isautoblocking || wasblocking) {
                                if (c07.getAction().equals(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) && e.permissionidstate < 5) {
                                    e.cancel();
                                }
                            }
                            break;
                    }
                    break;

            }


        }
        if(e.getPacket() instanceof ServerboundUseItemPacket ) {
            ServerboundUseItemPacket c0u = (ServerboundUseItemPacket) packetp;
            switch(autoblock.getCurrentMode()) {
                case ("Shield"):
                    switch(asmode.getCurrentMode()) {
                        case ("ReBlock"):
                            if(isautoblocking || wasblocking) {
                                if(e.permissionidstate<5){
                                    e.cancel();
                                }
                            }
                            break;
                    }
                    break;


                case ("Sword"):
                    switch(autoblock.getCurrentMode()) {
                        case ("ReBlock"):
                            if(isautoblocking || wasblocking) {
                                if(e.permissionidstate<5){
                                    e.cancel();
                                }
                            }
                            break;
                    }
                    break;

            }


        }
    }




    @EventTarget
    public void onLook(EventLook e) {
        if(lockedRots()) {
            e.setYaw(lastyaw);
            e.setPitch(lastpitch);
        }
    }
    @EventTarget
    public void onInput(EventMoveInput e) {
        //Onlycrit features============================================================

        switch (OnlyCritSR.getCurrentMode()){
            case ("Legit"):
                if(lasttarget!=null && mc.player.isSprinting() && (completeclicks>0 || (isClicking(mc.player.getMainHandItem()) && mc.player.getAttackStrengthScale(0.5f) >= 0.9f))){
                    mc.player.setSprinting(false);
                    e.setSprint(false);
                }


                break;


        }

        //=========================================================================
    }

    @EventTarget
    public void oRGL(EventRunGameLoop event) {
        if (isClicking(mc.player.getMainHandItem())) {
            if (lasttarget == null || (EntityUtil.getMinDistanceToEntity(lasttarget, mc.player) > (Clickrange.getValue() + range.getValue()) && !PerfectHit.isEnabled()) || (EntityUtil.getMinDistanceToEntity(lasttarget, mc.player) > (range.getValue()) && PerfectHit.isEnabled())) {
                completeclicks = Math.min(completeclicks, 1);
            } else if (this.lastattacktimer.hasTimeElapsed((long) (1000.0 / nextcps), true)) {
                completeclicks++;
                if (DoubleClicking.isEnabled() && Math.random() * 100 < DoubleClickChance.getValue()) {
                    completeclicks++;
                }
                updatecps();
            }
            completeclicks = (int) Math.min(completeclicks, MaxClicksPerTick.getValue());
        }

    }



















    List<Entity> getTargets() {

//7886iq сортировка дада

        Comparator<Entity> distanceComparator = Comparator
                .comparing((Entity entity) -> (EntityUtil.getMinDistanceToEntity(entity,mc.player)-0.03D) <= range.getValue())
                .reversed();

        Comparator<Entity> finalComparator;
        switch (Sort.getCurrentMode()) {
            case "Health":
                finalComparator = distanceComparator.thenComparingDouble(entity ->
                        ((LivingEntity)entity).getHealth() * ((LivingEntity)entity).getArmorValue());
                break;
            case "Angle":
                finalComparator = distanceComparator.thenComparingDouble(entity ->
                        RotationUtils.yawdiff((float) lastyaw, this.getPerfectRot(entity)[0]));
                break;
            case "Distance":
                finalComparator = distanceComparator.thenComparingDouble(entity -> EntityUtil.getMinDistanceToEntity((Entity) entity, mc.player));
                break;
            case "HurtTime":
                finalComparator = distanceComparator.thenComparingDouble(entity -> ((LivingEntity)entity).hurtTime);
                break;
            case "None":
                finalComparator = distanceComparator;
                break;
            default:
                finalComparator = distanceComparator;
                break;
        }

        List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null
             && EntityUtil.canSeeEntityAtFov(entity, FOV.getValue()) &&
                canAttack((LivingEntity) entity)

        ) .sorted(finalComparator).collect(Collectors.toList());








        return targets;
    }

    private boolean canAttack(Entity ent) {
        if(!isTarget(ent)){
            return false;
        }
        LivingEntity player= (LivingEntity) ent;
    if (EntityUtil.getMinDistanceToEntity(player, mc.player) > (Prerange.getValue() + range.getValue())) {
            return false;
        } else if (!player.hasLineOfSight(mc.player) && !walls.isEnabled()) {
            return false;
        } else {
            return player != mc.player;
        }
    }

    public float[] getFtRot(Entity e) {
        double entX = e.getX();
        double entY = e.getY();
        double entZ = e.getZ();
        double ybest = (float) entY + e.getEyeHeight(e.getPose());
        double ymin = (float) entY;
        double ymax = (float) entY + e.getEyeHeight(e.getPose());

        double entcenterX = (float) (entX);
        double entcenterZ = (float) (entZ);
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        double xcentr = mc.player.getX();
        double zcentr = mc.player.getZ();
        if ((entX + e.getBbWidth() / 2) < xcentr) {
            entcenterX = (entX + e.getBbWidth() / 2);
        } else if ((entX - e.getBbWidth() / 2) > xcentr) {
            entcenterX = (entX - e.getBbWidth() / 2);
        } else {
            entcenterX = mc.player.getX();
            if (entcenterZ < mc.player.getZ()) entcenterX += 0.05;
        }
        if ((entZ + e.getBbWidth() / 2) < zcentr) {
            entcenterZ = (entZ + e.getBbWidth() / 2);
        } else if ((entZ - e.getBbWidth() / 2) > zcentr) {
            entcenterZ = (entZ - e.getBbWidth() / 2);
        } else {
            entcenterZ = mc.player.getZ();
        }

        //  float eyes=(float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));if(ymax<eyes) { 	ybest=ymax-0.01D;}else if(ymin>eyes) {  		ybest=ymin+0.01D;    }else { 	ybest=(float) (mc.player.getY() +mc.player.getEyeHeight(mc.player.getPose())); }
        float minyaw = 0F;
        float maxyaw = 0F;
        float randomyaw = (float) ((Math.random() * (maxyaw - minyaw + 1)) + minyaw);
        if (Math.random() > 0.5) randomyaw *= -1;

        float minpitch = 0F;
        float maxpitch = 0F;
        float randompitch = (float) ((Math.random() * (maxpitch - minpitch + 1)) + minpitch);
        if (Math.random() > 0.5) randompitch *= -1;


        double spredictmult = 0;
        double tpredictmult = 0;
        float deffval = (float) (Math.random() * 0.0001);
        float eyes = (float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        if (ymax < eyes) {
            ybest = ymax - deffval;
        } else if (ymin > eyes) {
            ybest = ymin + deffval;
        } else {
            if ((ymin + ymax) / 2 > eyes) {
                deffval *= -1;
            }
            ybest = (float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - deffval);
        }

        float deltaX = (float) ((entcenterX + e.getDeltaMovement().x() * tpredictmult) - (playerX + mc.player.getDeltaMovement().x() * spredictmult));
        float deltaY = (float) ((ybest + e.getDeltaMovement().y() * tpredictmult) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) + mc.player.getDeltaMovement().y() * spredictmult));
        float deltaZ = (float) ((entcenterZ + e.getDeltaMovement().z() * tpredictmult) - (playerZ + mc.player.getDeltaMovement().z() * spredictmult));

        final float distance = (float) (Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));

        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        float pitch = (float) (-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float) (90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float) (-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }

       AABB ioriginBB = lasttarget.getBoundingBox();
        AABB ibox = new AABB(ioriginBB.minX + lasttarget.getBbWidth() * 0.35, ioriginBB.minY + lasttarget.getBbHeight() * 0.4, ioriginBB.minZ + lasttarget.getBbWidth() * 0.35,
                ioriginBB.minX + lasttarget.getBbWidth() * 0.65, ioriginBB.minY + lasttarget.getBbHeight() * 0.9, ioriginBB.minZ + lasttarget.getBbWidth() * 0.65
        );


       Rotation iLastRot = new Rotation(lastyaw, lastpitch);
        Vec3 ineedPoint = RotationUtils.getNearestVec(ibox, iLastRot);

   //  yaw=RotationUtils.addyaw(RotationUtils.getRotation(ineedPoint).getX(),yaw, (float) (RotationUtils.yawdiff(RotationUtils.getRotation(ineedPoint).getX(),yaw)*0.5),false);
    // pitch=RotationUtils.addpitch(RotationUtils.getRotation(ineedPoint).getY(),pitch, (float) (RotationUtils.pitchdiff(RotationUtils.getRotation(ineedPoint).getY(),pitch)*0.5),false);

        yaw=RotationUtils.getRotation(ineedPoint).getX();
         pitch=RotationUtils.getRotation(ineedPoint).getY();

     //   yaw += randomyaw;    pitch += randompitch;

        if (yaw < -360) {
            yaw += 360;
        }
        if (yaw > 360) {
            yaw -= 360;
        }
        pitch = Math.max(Math.min(pitch, 90), -90);




        if (Float.isNaN(yaw)) {
            yaw = lastyaw;
        }
        if (Float.isNaN(pitch)) {
            pitch = lastpitch;
        }
        return new float[]{yaw, pitch};
    }

    public float[] getPerfectRot(Entity e) {
        double entX = e.getX();
        double entY = e.getY();
        double entZ = e.getZ();
        double ybest = (float) entY + e.getEyeHeight(e.getPose());
        double ymin = (float) entY;
        double ymax = (float) entY + e.getEyeHeight(e.getPose());

        double entcenterX = (float) (entX);
        double entcenterZ = (float) (entZ);
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        double xcentr = mc.player.getX();
        double zcentr = mc.player.getZ();
        if ((entX + e.getBbWidth() / 2) < xcentr) {
            entcenterX = (entX + e.getBbWidth() / 2);
        } else if ((entX - e.getBbWidth() / 2) > xcentr) {
            entcenterX = (entX - e.getBbWidth() / 2);
        } else {
            entcenterX = mc.player.getX();
            if (entcenterZ < mc.player.getZ()) entcenterX += 0.01;
        }
        if ((entZ + e.getBbWidth() / 2) < zcentr) {
            entcenterZ = (entZ + e.getBbWidth() / 2);
        } else if ((entZ - e.getBbWidth() / 2) > zcentr) {
            entcenterZ = (entZ - e.getBbWidth() / 2);
        } else {
            entcenterZ = mc.player.getZ();
        }

        float eyes = (float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        if (ymax < eyes) {
            ybest = ymax - 0.01D;
        } else if (ymin > eyes) {
            ybest = ymin + 0.01D;
        } else {
            ybest = (float) (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        }

        float deltaX = (float) ((entcenterX) - (playerX));
        float deltaY = (float) ((ybest) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())));
        float deltaZ = (float) ((entcenterZ) - (playerZ));

        final float distance = (float) (Math.sqrt(Math.pow(deltaX, 2.0)) + Math.sqrt(Math.pow(deltaZ, 2.0)));

        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        float pitch = (float) (-Math.toDegrees(Math.atan(deltaY / distance)));
        if (deltaX < 0.0f && deltaZ < 0.0f) {
            yaw = (float) (90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        } else if (deltaX > 0.0f && deltaZ < 0.0f) {
            yaw = (float) (-90.0 + Math.toDegrees(Math.atan(deltaZ / deltaX)));
        }
        if (yaw < -360) {
            yaw += 360;
        }
        if (yaw > 360) {
            yaw -= 360;
        }


        if (Float.isNaN(yaw)) {
            yaw = lastyaw;
        }
        if (Float.isNaN(pitch)) {
            pitch = lastpitch;
        }
        return new float[]{yaw, pitch};
    }


    private void updatecps() {

        switch (CPSMode.getCurrentMode()) {
            case ("Random"):
                nextcps = MathematicHelper.randomizeFloat(CpsMin.getValue(), CpsMax.getValue());
                break;
            case ("Smooth"):
                double chancetoincrease = 0.4D;
                double chancetodincrease = 0.4D;
                boolean smt = false;
                if (nextcps > this.CpsMin.getValue()) {
                    if (Math.random() <= chancetodincrease) {
                        nextcps--;
                        smt = true;
                    }
                }
                if (nextcps < this.CpsMax.getValue() && !smt) {
                    if (Math.random() <= chancetoincrease) {
                        nextcps++;
                        smt = true;
                    }
                }

                break;

            case ("UpDown"):
                float step=1;
float rand121=MathematicHelper.randomizeFloat(0.5F, 1F);
           if(cpsup){
               float delta=this.CpsMax.getValue()-nextcps;
               step+=Math.round((delta-1)*rand121);
               if (nextcps < this.CpsMax.getValue()) {    nextcps+=step;  }else{  nextcps-=step;     cpsup=false;  }
           }else{
               float delta=nextcps-this.CpsMin.getValue();
               step+=Math.round((delta-1)*rand121);
               if (nextcps > this.CpsMin.getValue()) {    nextcps-=step;  }else{   nextcps+=step;    cpsup=true;  }
           }



                break;

        }
        cpsarray.add(nextcps);
    }

    private void cpsreset() {
        cpsup=true;
        cpsarray.clear();

    }


    private void proccesswing(boolean wasattack) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (AInteractionHand.getCurrentMode().equals("Left")) {
            hand = InteractionHand.OFF_HAND;
        }
        if (swingMode.getCurrentMode().equals("Packet")) {
            mc.getConnection().send(new ServerboundSwingPacket(hand));
        } else if (this.swingMode.getCurrentMode().equals("Default")) {
            if(ShowSwingNoHit.isEnabled() || wasattack){        mc.player.swing(hand);
            }else{   mc.getConnection().send(new ServerboundSwingPacket(hand));      }
         }
    }

    boolean needhit(LivingEntity ltarget) {
        if (!(mc.player.getMainHandItem().is(ItemTags.SWORDS)) && !(mc.player.getMainHandItem().getItem() instanceof AxeItem) && weaponOnly.isEnabled()) {
            return false;
        }
        if (pc.ticknotusingitem<2 && (usingItemCheck.getCurrentMode().equals("None") || (usingItemCheck.getCurrentMode().equals("Only Consumable") && EntityUtil.isConsuming(mc.player)))) {
            return false;
        }
        if (!isCondition(Client.instance.packet)) {
            return false;
        }
        if((ltarget).hurtTime>MaxHurtTime.getValue()){
            return false;
        }
        if(ticksfromlasttargetupdate > 0){
            return false;
        }
        boolean critweapon=true;
        if ((isClicking(mc.player.getMainHandItem()) && AutoOnlyCrit.getCurrentMode().equals("Only 1.9+")) ||
                (!isClicking(mc.player.getMainHandItem()) && AutoOnlyCrit.getCurrentMode().equals("Only 1.8-"))) {
            critweapon= false;
        }



        if(OnlyCrit.isEnabled() && critweapon && !CombatUtil.canCrit(true,!(OnlyCritSR.getCurrentMode().equals("None"))) || OnlyCritSR.getCurrentMode().equals("Legit")){
            return false;
        }

        return true;
    }
    private boolean isClicking(ItemStack item) {
  if (ClickType.getCurrentMode().equals("Clicking")) {
            return true;
        }else if (ClickType.getCurrentMode().equals("OnCooldown")) {
            return false;
        }else {
            return (mc.player.getAttributeValue(Attributes.ATTACK_SPEED)>16);
        }
        }

    private boolean isCondition(PacketHelper.Values pc) {

        if(GroundCondition.getCurrentMode().equals("Only Ground") && !pc.LastGround) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Only Air") && pc.LastGround) { 	return false; 	}
        else if(GroundCondition.getCurrentMode().equals("Smart") && ((pc.LastGround || (mc.player.onGround() && mc.player.verticalCollision)) && MoveUtil.motYstateh())) { 	return false; 	}


        return true;
    }

    public Entity getTarget() {
        return lasttarget;
    }
}
