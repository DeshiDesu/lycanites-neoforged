package com.lycanitesmobs.core.tabs;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.equipment.features.*;
import com.lycanitesmobs.core.manager.EquipmentPartManager;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

import java.util.*;

/**
 * Creative tab that computationally generates the best possible equipment
 * combinations from all available entity parts, organized by category.
 */
public class LMBestEquipmentGroup {
    public CreativeModeTab.Builder builder = CreativeModeTab.builder();

    public LMBestEquipmentGroup() {
        init();
    }

    public static CreativeModeTab.Builder getBuilder() {
        return new LMBestEquipmentGroup().builder;
    }

    public void init() {
        builder = builder
                .title(Component.translatable("itemGroup." + LycanitesMobs.MODID + ".bestequipment"))
                .icon(this::getIconItem)
                .displayItems((enabledFeatures, entries) -> {
                    List<ItemStack> bestEquipment = computeBestEquipment();
                    for (ItemStack stack : bestEquipment) {
                        entries.accept(stack);
                    }
                });
    }

    private ItemStack cachedIcon = null;

    public ItemStack getIconItem() {
        if (cachedIcon != null && !cachedIcon.isEmpty()) return cachedIcon;

        ItemEquipment equipmentItem = (ItemEquipment) ObjectManager.getItem("equipment");
        if (equipmentItem == null) return new ItemStack(Items.DIAMOND_SWORD);

        ItemStack icon = new ItemStack(equipmentItem);
        String[] partNames = {"equipmentpart_xaphanspine", "equipmentpart_ironguard", "equipmentpart_ironpaxel", "equipmentpart_vespidstinger"};
        int slot = 0;
        for (String name : partNames) {
            var item = ObjectManager.getItem(name);
            if (item instanceof ItemEquipmentPart part) {
                ItemStack partStack = new ItemStack(part);
                part.setLevel(partStack, part.levelMax);
                part.setSharpness(partStack, ItemEquipment.SHARPNESS_MAX);
                part.setMana(partStack, ItemEquipment.MANA_MAX);
                equipmentItem.addEquipmentPart(icon, partStack, slot++);
            }
        }

        cachedIcon = icon;
        return icon;
    }

    /**
     * Represents a candidate equipment configuration (list of parts to assemble).
     */
    private static class EquipmentCandidate {
        final List<PartEntry> parts = new ArrayList<>();
        String label;

        double meleeDps;
        double totalDamage;
        double totalSweep;
        double totalKnockback;
        double totalRange;
        float pickaxeSpeed;
        float axeSpeed;
        float shovelSpeed;
        int pickaxeLevel;
        int axeLevel;
        int shovelLevel;
        int harvestTypeCount;
        float totalHarvestSpeed;
        double projectileScore;
        int effectCount;
    }

    private static class PartEntry {
        final ItemEquipmentPart part;
        final String name;
        final int level;

        PartEntry(ItemEquipmentPart part, String name, int level) {
            this.part = part;
            this.name = name;
            this.level = level;
        }
    }

    /**
     * Computes all valid equipment combinations and returns the best ones per category.
     */
    private List<ItemStack> computeBestEquipment() {
        Map<String, Lazy<ItemEquipmentPart>> allParts = EquipmentPartManager.getInstance().equipmentParts;
        if (allParts.isEmpty()) return Collections.emptyList();

        Map<String, List<PartEntry>> bySlot = new HashMap<>();
        for (Map.Entry<String, Lazy<ItemEquipmentPart>> entry : allParts.entrySet()) {
            ItemEquipmentPart part = entry.getValue().get();
            if (part == null) continue;
            PartEntry pe = new PartEntry(part, entry.getKey(), part.levelMax);
            bySlot.computeIfAbsent(part.slotType, k -> new ArrayList<>()).add(pe);
        }

        List<PartEntry> bases = bySlot.getOrDefault("base", Collections.emptyList());
        List<PartEntry> heads = bySlot.getOrDefault("head", Collections.emptyList());
        List<PartEntry> blades = bySlot.getOrDefault("blade", Collections.emptyList());
        List<PartEntry> axes = bySlot.getOrDefault("axe", Collections.emptyList());
        List<PartEntry> pikes = bySlot.getOrDefault("pike", Collections.emptyList());
        List<PartEntry> pommels = bySlot.getOrDefault("pommel", Collections.emptyList());
        List<PartEntry> jewels = bySlot.getOrDefault("jewel", Collections.emptyList());

        Map<String, List<PartEntry>> terminalParts = new HashMap<>();
        terminalParts.put("blade", blades);
        terminalParts.put("axe", axes);
        terminalParts.put("pike", pikes);
        terminalParts.put("jewel", jewels);

        List<EquipmentCandidate> allCandidates = new ArrayList<>();

        for (PartEntry base : bases) {
            List<String> baseSlots = getSlotsFromPart(base);
            boolean hasPommelSlot = baseSlots.contains("pommel");
            boolean hasHeadSlot = baseSlots.contains("head");

            if (!hasHeadSlot) continue;

            List<PartEntry> pommelOptions = hasPommelSlot ? pommels : Collections.emptyList();
            List<PartEntry> pommelChoices = new ArrayList<>();
            pommelChoices.add(null);
            pommelChoices.addAll(pommelOptions);

            for (PartEntry head : heads) {
                List<String> headSlots = getSlotsFromPart(head);

                Map<String, Integer> slotCounts = new HashMap<>();
                for (String slot : headSlots) {
                    slotCounts.merge(slot, 1, Integer::sum);
                }

                List<List<PartEntry>> terminalCombos = buildTerminalCombos(slotCounts, terminalParts);

                for (List<PartEntry> terminals : terminalCombos) {
                    for (PartEntry pommel : pommelChoices) {
                        EquipmentCandidate candidate = new EquipmentCandidate();
                        candidate.parts.add(base);
                        candidate.parts.add(head);
                        candidate.parts.addAll(terminals);
                        if (pommel != null) {
                            candidate.parts.add(pommel);
                        }
                        scoreCandidateFromParts(candidate);
                        allCandidates.add(candidate);
                    }
                }
            }
        }

        List<ItemStack> results = new ArrayList<>();

        addBestN(results, allCandidates, "Best Melee DPS",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.meleeDps).reversed(), 3);

        addBestN(results, allCandidates, "Best Pickaxe",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.pickaxeSpeed + c.pickaxeLevel * 100).reversed(), 3);

        addBestN(results, allCandidates, "Best Axe",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.axeSpeed + c.axeLevel * 100).reversed(), 3);

        addBestN(results, allCandidates, "Best Shovel",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.shovelSpeed + c.shovelLevel * 100).reversed(), 3);

        addBestN(results, allCandidates, "Best AoE/Sweep",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.totalSweep * c.totalDamage).reversed(), 3);

        addBestN(results, allCandidates, "Best Ranged",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.projectileScore).reversed(), 3);

        addBestN(results, allCandidates, "Best Multi-Tool",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.harvestTypeCount * 1000.0 + c.totalHarvestSpeed).reversed(), 3);

        addBestN(results, allCandidates, "Best Knockback",
                Comparator.comparingDouble((EquipmentCandidate c) -> c.totalKnockback).reversed(), 2);

        addBestN(results, allCandidates, "Most Effects",
                Comparator.comparingDouble((EquipmentCandidate c) -> (double) c.effectCount).reversed(), 2);

        return results;
    }

    /**
     * Gets the slot types that a part's slot features open.
     */
    private List<String> getSlotsFromPart(PartEntry pe) {
        List<String> slots = new ArrayList<>();
        ItemStack fakeStack = new ItemStack(pe.part);
        pe.part.setLevel(fakeStack, pe.level);
        for (EquipmentFeature feature : pe.part.features) {
            if (feature instanceof SlotEquipmentFeature slotFeature) {
                if (feature.isActive(fakeStack, pe.level)) {
                    slots.add(slotFeature.slotType);
                }
            }
        }
        return slots;
    }

    /**
     * Builds all combinations of terminal parts for the given slot counts.
     * For example, if a head opens 2 axe slots, we try all pairs of axe parts.
     */
    private List<List<PartEntry>> buildTerminalCombos(
            Map<String, Integer> slotCounts,
            Map<String, List<PartEntry>> terminalParts) {

        List<List<PartEntry>> combos = new ArrayList<>();
        combos.add(new ArrayList<>());

        for (Map.Entry<String, Integer> slotEntry : slotCounts.entrySet()) {
            String slotType = slotEntry.getKey();
            int count = slotEntry.getValue();
            List<PartEntry> available = terminalParts.getOrDefault(slotType, Collections.emptyList());

            if (available.isEmpty()) {
                continue;
            }

            List<List<PartEntry>> newCombos = new ArrayList<>();
            for (List<PartEntry> existing : combos) {
                if (count == 1) {
                    for (PartEntry part : available) {
                        List<PartEntry> combo = new ArrayList<>(existing);
                        combo.add(part);
                        newCombos.add(combo);
                    }
                } else if (count == 2) {
                    for (int i = 0; i < available.size(); i++) {
                        for (int j = i; j < available.size(); j++) {
                            List<PartEntry> combo = new ArrayList<>(existing);
                            combo.add(available.get(i));
                            combo.add(available.get(j));
                            newCombos.add(combo);
                        }
                    }
                }
            }
            combos = newCombos;
        }

        return combos;
    }

    /**
     * Scores a candidate by computing aggregate stats from all parts' features at max level.
     */
    private void scoreCandidateFromParts(EquipmentCandidate candidate) {
        double totalDamage = 0;
        double totalCooldown = 0;
        int damageFeatureCount = 0;
        double totalSweep = 0;
        double totalKnockback = 0;
        double totalRange = 0;

        float pickaxeSpeed = 0, axeSpeed = 0, shovelSpeed = 0, hoeSpeed = 0, swordSpeed = 0, shearsSpeed = 0;
        int pickaxeLevel = -1, axeLevel = -1, shovelLevel = -1;
        Set<String> harvestTypes = new HashSet<>();
        float totalHarvestSpeed = 0;

        double projectileScore = 0;
        int effectCount = 0;

        for (PartEntry pe : candidate.parts) {
            ItemStack fakeStack = new ItemStack(pe.part);
            pe.part.setLevel(fakeStack, pe.level);

            for (EquipmentFeature feature : pe.part.features) {
                if (!feature.isActive(fakeStack, pe.level)) continue;

                if (feature instanceof DamageEquipmentFeature dmg) {
                    totalDamage += dmg.damageAmount;
                    totalCooldown += dmg.damageCooldown;
                    damageFeatureCount++;
                    totalSweep += dmg.damageSweep;
                    totalKnockback += dmg.damageKnockback;
                    totalRange += dmg.damageRange;
                } else if (feature instanceof HarvestEquipmentFeature harvest) {
                    String type = harvest.harvestType.toLowerCase();
                    harvestTypes.add(type);
                    totalHarvestSpeed += harvest.harvestSpeed;
                    switch (type) {
                        case "pickaxe" -> {
                            pickaxeSpeed += harvest.harvestSpeed;
                            pickaxeLevel = Math.max(pickaxeLevel, harvest.harvestLevel);
                        }
                        case "axe" -> {
                            axeSpeed += harvest.harvestSpeed;
                            axeLevel = Math.max(axeLevel, harvest.harvestLevel);
                        }
                        case "shovel" -> {
                            shovelSpeed += harvest.harvestSpeed;
                            shovelLevel = Math.max(shovelLevel, harvest.harvestLevel);
                        }
                    }
                } else if (feature instanceof ProjectileEquipmentFeature proj) {
                    double cd = Math.max(proj.cooldown, 1);
                    projectileScore += (proj.bonusDamage + 1.0) * proj.count / cd * 20.0;
                } else if (feature instanceof EffectEquipmentFeature) {
                    effectCount++;
                }
            }
        }

        double avgCooldown = damageFeatureCount > 0 ? totalCooldown / damageFeatureCount : 4.0;
        double attackSpeed = Math.max(4.0 - avgCooldown, 0.5);
        candidate.meleeDps = (totalDamage + 1.0) * attackSpeed;
        candidate.totalDamage = totalDamage;
        candidate.totalSweep = Math.min(totalSweep, 360);
        candidate.totalKnockback = totalKnockback;
        candidate.totalRange = totalRange;
        candidate.pickaxeSpeed = pickaxeSpeed;
        candidate.axeSpeed = axeSpeed;
        candidate.shovelSpeed = shovelSpeed;
        candidate.pickaxeLevel = pickaxeLevel;
        candidate.axeLevel = axeLevel;
        candidate.shovelLevel = shovelLevel;
        candidate.harvestTypeCount = harvestTypes.size();
        candidate.totalHarvestSpeed = totalHarvestSpeed;
        candidate.projectileScore = projectileScore;
        candidate.effectCount = effectCount;
    }

    /**
     * Selects the best N candidates by the given comparator (that aren't already in results)
     * and builds their equipment ItemStacks.
     */
    private void addBestN(List<ItemStack> results, List<EquipmentCandidate> candidates,
                          String categoryName, Comparator<EquipmentCandidate> comparator, int n) {
        List<EquipmentCandidate> filtered = new ArrayList<>(candidates);
        switch (categoryName) {
            case "Best Pickaxe" -> filtered.removeIf(c -> c.pickaxeLevel < 0);
            case "Best Axe" -> filtered.removeIf(c -> c.axeLevel < 0);
            case "Best Shovel" -> filtered.removeIf(c -> c.shovelLevel < 0);
            case "Best Ranged" -> filtered.removeIf(c -> c.projectileScore <= 0);
            case "Best Multi-Tool" -> filtered.removeIf(c -> c.harvestTypeCount < 2);
            case "Best Knockback" -> filtered.removeIf(c -> c.totalKnockback <= 0);
            case "Most Effects" -> filtered.removeIf(c -> c.effectCount <= 0);
        }
        filtered.sort(comparator);

        Set<String> existingKeys = new HashSet<>();
        for (ItemStack existing : results) {
            existingKeys.add(getEquipmentKey(existing));
        }

        int added = 0;
        for (EquipmentCandidate candidate : filtered) {
            if (added >= n) break;
            ItemStack stack = buildEquipmentStack(candidate, categoryName + " #" + (added + 1));
            String key = getEquipmentKey(stack);
            if (existingKeys.contains(key)) continue;
            existingKeys.add(key);
            results.add(stack);
            added++;
        }
    }

    private String getEquipmentKey(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemEquipment equipment)) return "";
        StringBuilder sb = new StringBuilder();
        for (ItemStack partStack : equipment.getEquipmentPartStacks(stack)) {
            if (!partStack.isEmpty() && partStack.getItem() instanceof ItemEquipmentPart part) {
                sb.append(part.itemName).append("@").append(part.getPartLevel(partStack)).append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Builds an assembled equipment ItemStack from a candidate configuration.
     */
    private ItemStack buildEquipmentStack(EquipmentCandidate candidate, String label) {
        ItemEquipment equipmentItem = (ItemEquipment) ObjectManager.getItem("equipment");
        if (equipmentItem == null) return ItemStack.EMPTY;

        ItemStack equipmentStack = new ItemStack(equipmentItem);

        int slotIndex = 0;
        for (PartEntry pe : candidate.parts) {
            ItemStack partStack = new ItemStack(pe.part);
            pe.part.setLevel(partStack, pe.level);
            pe.part.setSharpness(partStack, ItemEquipment.SHARPNESS_MAX);
            pe.part.setMana(partStack, ItemEquipment.MANA_MAX);
            equipmentItem.addEquipmentPart(equipmentStack, partStack, slotIndex);
            slotIndex++;
        }

        return equipmentStack;
    }
}
