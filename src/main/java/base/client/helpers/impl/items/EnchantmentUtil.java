package base.client.helpers.impl.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentUtil {

    public static int getArmorEnchantmentScore(ItemStack stack) {
        int score = 0;
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);

        if (enchants != null) {
            for (var entry : enchants.entrySet()) {
                Enchantment enchant = entry.getKey().value();
                int level = entry.getIntValue();

                score += level * getEnchantWeightArmor(enchant);
            }
        }
        return score;
    }

    private static int getEnchantWeightArmor(Enchantment enchant) {

        if (enchant.equals(Enchantments.PROTECTION)) return 5;
        if (enchant.equals(Enchantments.FIRE_PROTECTION)) return 2;
        if (enchant.equals(Enchantments.BLAST_PROTECTION)) return 2;
        if (enchant.equals(Enchantments.PROJECTILE_PROTECTION)) return 2;
        if (enchant.equals(Enchantments.RESPIRATION)) return 2;
        if (enchant.equals(Enchantments.AQUA_AFFINITY)) return 2;

        if (enchant.equals(Enchantments.THORNS)) return 2;
        if (enchant.equals(Enchantments.DEPTH_STRIDER)) return 2;
        if (enchant.equals(Enchantments.FROST_WALKER)) return 2;
        if (enchant.equals(Enchantments.SOUL_SPEED)) return 2;
        if (enchant.equals(Enchantments.SWIFT_SNEAK)) return 2;

        if (enchant.equals(Enchantments.UNBREAKING)) return 5;
        if (enchant.equals(Enchantments.MENDING)) return 7;
        if (enchant.equals(Enchantments.BINDING_CURSE)) return -3;

        if (enchant.equals(Enchantments.FEATHER_FALLING)) return 2;

        return 1;
    }



}
