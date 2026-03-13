package base.client.feature.impl.client;
import base.client.Client;
import base.client.feature.impl.LockedModule;
import base.client.feature.impl.Type;
import base.client.feature.impl.combat.AntiBot;
import base.client.feature.settings.impl.BooleanSetting;
import base.client.friend.Friend;
import base.client.helpers.utils.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.Iterator;

public class CombatTargets extends LockedModule {

	public static BooleanSetting players = new BooleanSetting("Players", "Позволяет бить игроков", true, () -> true);
	public static BooleanSetting armorStands = new BooleanSetting("Armor Stands", "Позволяет бить армор-стенды", false, () -> true);
	public static BooleanSetting monsters = new BooleanSetting("Monsters", "Позволяет бить монстров", false, () -> true);
	public static BooleanSetting villagers = new BooleanSetting("Villagers", "Позволяет бить жителей", false, () -> true);
	public static BooleanSetting animals = new BooleanSetting("Animals", "Позволяет бить безобидных мобов", false, () -> true);
	public static BooleanSetting team = new BooleanSetting("Teams", "Позволяет бить тимейтов на мини-играх", false, () -> true);
	public static BooleanSetting invis = new BooleanSetting("Invisible", "Позволяет бить невидемых существ", true, () -> true);
	public static BooleanSetting nakedPlayer = new BooleanSetting("Naked Players", "Бить голых игроков", true, () -> true);


	@Override
	public void onDisable() {
	    super.onDisable();
	}

	@Override
	public void onEnable() {
	    super.onEnable();
	}

    public CombatTargets() {
        super("Targets", "Определяет какие сущности являются целями", Type.Client);
        this.addSettings(players,armorStands,monsters,villagers,animals,team,invis,nakedPlayer);
    }

	public static boolean isTarget(Entity ent){


if(!(ent instanceof LivingEntity) || !ent.isAlive()){
	return false;
}
LivingEntity entity= (LivingEntity) ent;
		if(AntiBot.isBotList(entity.getUUID())){
			return false;
		}
		Iterator<Friend> var2 = Client.instance.friendManager.getFriends().iterator();

		while (var2.hasNext()) {
			Friend friend = (Friend) var2.next();
			if (entity.getName().equals(friend.getName())) {
				return false;
			}
		}


			if (entity instanceof Player || entity instanceof Animal || entity instanceof Mob || entity instanceof Villager || entity instanceof ArmorStand) {
				if (entity instanceof Player ) {
					if(!players.isEnabled()) {
						return false;
					}else if (!nakedPlayer.isEnabled() && EntityUtil.getArmorCount(entity) <= 0) {
						return false;
					}
				}

				if (entity instanceof Animal && !animals.isEnabled()) {
					return false;
				}else if (entity instanceof Villager && !villagers.isEnabled()) {
					return false;
				}else if (entity instanceof ArmorStand && !armorStands.isEnabled()) {
					return false;
				}else if (entity instanceof Monster && !monsters.isEnabled()) {
					return false;
				}






			} else {
				return false;
			}
		if (ent.getTeam()==mc.player.getTeam() && !team.isEnabled()) {
		 	return false;
		}
		if (entity.isInvisible() && !invis.isEnabled()) {
			return false;
		}



		return  true;
	}


}
