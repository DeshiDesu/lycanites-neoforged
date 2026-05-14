package com.lycanitesmobs.core.item.equipment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Generates dynamic names for assembled equipment pieces based on the parts used.
 * Each part defines a bank of thematic name fragments in its JSON config via the "names" array.
 * The combination of fragments produces names like "Venomous Apollyon Hellscythe".
 *
 * <p>Name format: [PREFIX] [CREATURE] [WEAPON TYPE]
 * <ul>
 *   <li>PREFIX — from jewel (arcane/magical) or pommel (effect-based) parts</li>
 *   <li>CREATURE — from the head part's creature identity</li>
 *   <li>WEAPON TYPE — from the base part's structural form</li>
 * </ul>
 *
 * <p>Terminal parts (blade/axe/pike) replace the creature name with their own
 * if present, since they are the active striking component.
 */
public class EquipmentNameGenerator {

    /**
     * Generates a dynamic name for an assembled equipment piece based on its parts.
     *
     * @param equipmentStack The equipment ItemStack.
     * @return A themed name Component, or null if the equipment has no parts.
     */
    public static Component generateName(ItemStack equipmentStack) {
        if (!(equipmentStack.getItem() instanceof ItemEquipment equipment)) return null;

        ItemEquipmentPart prefixPart = null;
        ItemEquipmentPart creaturePart = null;
        ItemEquipmentPart headPart = null;
        ItemEquipmentPart weaponPart = null;
        long hashSeed = 0;

        for (ItemStack partStack : equipment.getEquipmentPartStacks(equipmentStack)) {
            if (partStack.isEmpty()) continue;
            if (!(partStack.getItem() instanceof ItemEquipmentPart part)) continue;

            String rawName = part.itemName.replace("equipmentpart_", "");
            hashSeed = hashSeed * 31 + rawName.hashCode();

            switch (part.slotType) {
                case "base" -> weaponPart = part;
                case "head" -> headPart = part;
                case "blade", "axe", "pike" -> creaturePart = part;
                case "jewel" -> prefixPart = part;
                case "pommel" -> {
                    if (prefixPart == null) prefixPart = part;
                }
            }
        }

        if (weaponPart == null && creaturePart == null && headPart == null) return null;

        if (creaturePart == null) {
            creaturePart = headPart;
        }

        StringBuilder name = new StringBuilder();
        Random rng = new Random(hashSeed);

        if (prefixPart != null) {
            String prefix = pickFragment(prefixPart.equipmentNames, rng);
            if (prefix != null) {
                name.append(prefix).append(" ");
            }
        }

        if (creaturePart != null) {
            String creature = pickFragment(creaturePart.equipmentNames, rng);
            if (creature != null) {
                name.append(creature).append(" ");
            }
        }

        if (weaponPart != null) {
            String weapon = pickFragment(weaponPart.equipmentNames, rng);
            if (weapon != null) {
                name.append(weapon);
            }
        }

        String result = name.toString().trim();
        if (result.isEmpty()) return null;

        return Component.literal(result);
    }

    /**
     * Picks a name fragment from the provided list.
     * Uses the provided Random for deterministic selection based on the equipment's composition.
     */
    private static String pickFragment(List<String> fragments, Random rng) {
        if (fragments == null || fragments.isEmpty()) return null;
        return fragments.get(Math.abs(rng.nextInt()) % fragments.size());
    }
}
