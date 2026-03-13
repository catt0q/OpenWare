package base.client.feature.impl.movement;

import base.client.Client;
import base.client.event.EventTarget;
import base.client.event.events.impl.motion.EventOnMovePost;
import base.client.feature.Module;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.AntiBot;
import base.client.feature.impl.combat.KillAuraNew;
import base.client.feature.settings.impl.ModeSetting;
import base.client.feature.settings.impl.NumberSetting;
import base.client.helpers.impl.packet.PacketHelper;
import base.client.helpers.utils.EntityUtil;
import base.client.helpers.utils.MoveUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CollideBoost extends Module {

    public static ModeSetting Mode = new ModeSetting("Mode", "Funtime", () -> true,"Funtime","Matrix","Grim");


    public CollideBoost(KillAuraNew killAura) {
        super("CollideBoost", "Ускорение при коллайде", Type.Movement);
        this.addSettings(Mode,speed);
    }

    //String name, String desc, float current, float minimum, float maximum, float increment, Supplier<Boolean> visible
    public NumberSetting speed = new NumberSetting("Speed", "Ускорение (0.17 можеть флагаться)", 0.16f, 0.01f, 0.15f, 0.01f, ()->true);

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @EventTarget
    public void eom(EventOnMovePost e) {
        PacketHelper.Values pc= Client.instance.packet;

        double yawt = Math.toRadians(MoveUtil.getdir());


        //if(MoveUtil.getdir()==-1) { xt=0; zt=0;	   	 }
        double xt = -Math.sin(yawt);    double zt = Math.cos(yawt);
        if (MoveUtil.getdir() == -1) {
            xt = 0;
            zt = 0;
        }
//yawt = Math.toRadians( MoveUtil.getdir());
        java.util.List<Entity> targets=getTargets();
        switch (Mode.getCurrentMode()){
            case("Funtime"):
                for (Entity target : targets) {
                    if (mc.player.getBoundingBox().intersects(target.getBoundingBox())) {
                        mc.player.addDeltaMovement(new Vec3(xt*speed.getValue(),0,zt*speed.getValue()));

                        break;
                    }
                }
//                int collideAmoount=0;
//                for (Entity target : targets) {
//                    AABB bx = mc.player.getBoundingBox();
//                    if (bx.intersects(target.getBoundingBox())) {
//                        collideAmoount++;
//                    }
//                }
//            if ((collideAmoount>0)) {
//                mc.player.addDeltaMovement(new Vec3(xt*speed.getValue()*collideAmoount,0,zt*speed.getValue()*collideAmoount);
//            }
            break;


            case("Matrix"):
                for (Entity target : targets) {

double resize=1;



                        double csz=(resize-1)/2;
                    AABB pb=new AABB(pc.LastPosX-mc.player.getBbWidth()*csz,pc.LastPosY,pc.LastPosZ-mc.player.getBbWidth()*csz,
                                pc.LastPosX+mc.player.getBbWidth()*(1+csz),pc.LastPosY+mc.player.getBbHeight(),pc.LastPosZ+mc.player.getBbWidth()*(1+resize)
                        );





                    AABB tb=new AABB(target.xOld,target.yOld,target.zOld,
                            target.xOld+target.getBbWidth(),target.yOld+target.getBbHeight(),target.zOld+target.getBbWidth()
                            );
                    AABB ntb=new AABB(target.getX(),target.getY(),target.getZ(),
                            target.getX()+target.getBbWidth(),target.getY()+target.getBbHeight(),target.getZ()+target.getBbWidth()
                    );
                    AABB ntb2=new AABB(target.getX()+target.getDeltaMovement().x,
                            target.getY()+target.getDeltaMovement().y,
                            target.getZ()+target.getDeltaMovement().z,
                            target.getX()+target.getDeltaMovement().x+target.getBbWidth(),
                            target.getY()+target.getBbHeight()+target.getDeltaMovement().y,
                            target.getZ()+target.getBbWidth()+target.getDeltaMovement().z
                    );

                    AABB c1=pb,c2=target.getBoundingBox();

                    double mult=EntityUtil.getTotalXYZIntersectionLength(c1,c2)*0.045;


                    if (
                            //  c1.minY < c2.maxY && c1.maxY > c2.minY &&
                            //mc.player.getBoundingBox().intersects(target.getBoundingBox())
                            //  pb.intersects(tb)
                            mult>0
                    ) { //ChatHelper.addChatMessage(""+mult);
                       mc.player.addDeltaMovement(new Vec3(xt*mult,0,zt*mult));

                        break;
                    }

            /*        if (!ACUtil.isground() &&
                  //  c1.minY < c2.maxY && c1.maxY > c2.minY &&
                            //mc.player.getBoundingBox().intersects(target.getBoundingBox())
                    //  pb.intersects(tb)
                            mult>0
                    ) {
                        mc.player.addDeltaMovement(new Vec3(xt*mult,0,zt*mult));
   break;
                    }*/




                }

                break;

            case("Grim"):
                double ents=0;
                for (Entity target : targets) {
                    if (mc.player.getBoundingBox().intersects(target.getBoundingBox())) {
                  ents++;
           break;
                    }
                }
                double bstn=0.05;


                bstn*=ents;
                MoveUtil.addmotXZ(xt*bstn,zt*bstn);




                break;

          }
      }
    java.util.List<Entity> getTargets() {

        java.util.List<Entity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(),false).filter(LivingEntity.class::isInstance).collect(Collectors.toList());
        targets = targets.stream().filter(entity -> entity != null && entity.getId() != mc.player.getId() &&
                entity instanceof LivingEntity && !(entity instanceof ArmorStand) &&
                entity.isAlive() && !AntiBot.isBotList(entity.getUUID())).collect(Collectors.toList());


        return targets;
    }
}
