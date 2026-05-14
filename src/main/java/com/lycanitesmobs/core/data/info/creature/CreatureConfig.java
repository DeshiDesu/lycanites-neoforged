package com.lycanitesmobs.core.data.info.creature;

import com.lycanitesmobs.core.data.config.ConfigCreatures;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.data.info.item.ItemDrop;
import com.lycanitesmobs.core.data.info.Variant;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Loads global creature configs. **/
public class CreatureConfig {

	// Client:
	/** If true, all mobs that are a subspecies will always show their nametag. **/
	public boolean subspeciesTags = true;

	/** The minimum interval in ticks between random idle sounds. **/
	public int idleSoundTicks = 100;

	/** If true, alpha is disabled on mob textures, this can make them look undesirable but can increase performance on low end systems. **/
	public boolean disableModelAlpha = false;

	/** If true, block particles are not spawned by mobs (useful for visual mods that create 3D block particles which can cause lag in high numbers). **/
	public boolean disableBlockParticles = false;

	// Stats:
	/** The minimum base starting level of every mob. Cannot be less than 1. **/
	public int startingLevelMin = 1;

	/** The maximum base starting level of every mob. Ignored when not greater than the min level. **/
	public int startingLevelMax = 5;

	/** Increases the base start level by this amount of every world day that has gone by, use this to slowly level up mobs as the world gets older. Fractions can be used such as 0.05 levels per day. The levels are rounded down so +0.9 would be +0 levels. **/
	public double levelPerDay = 0;

	/** The maximum level to be able gain from levels per day. **/
	public int levelPerDayMax = 100;

	/** How many levels a mob gains multiplied by the local area difficulty level. Staying in an area for a while slowly increases the difficulty of that area ranging from 0.00 to 6.75. So 1.5 means level 10 at full local area difficulty. **/
	public double levelPerLocalDifficulty = 1.5;


	// Pets:
	/** If true, the name of a pet's owner will be shown in it's name tag. **/
	public boolean ownerTags = true;

	/** Whether mob taming is allowed. **/
	public boolean tamingEnabled = true;

	/** Whether mob mounting is allowed. **/
	public boolean mountingEnabled = true;

	/** Whether mob mounting for flying mobs is allowed. **/
	public boolean mountingFlightEnabled = true;

	/** If true, tamed mobs wont harm their owners. **/
	public boolean friendlyFire = true;

	/** The time in tics that it takes for a pet to respawn. **/
	public int petRespawnTime = 3 * 60 * 20;

	/** How far in blocks pets stray from their owner when set to follow. **/
	public int petFollowDistance = 8;

	/** A global list of dimension ids for restricting where soulbound pets and mounts are allowed. **/
	public String[] soulboundDimensionList;

	/** If set to true the soulbound dimension list acts as a whitelist, otherwise it is a blacklist. **/
	public boolean soulboundDimensionListWhitelist = false;

	/** A global list of dimension ids for restricting where minion summoning is allowed. **/
	public String[] summonDimensionList;

	/** If set to true the summon dimension list acts as a whitelist, otherwise it is a blacklist. **/
	public boolean summonDimensionListWhitelist = false;


	// Beastiary:
	/** If true, a chat message will be displayed when gaining Beastiary Knowledge. **/
	public boolean beastiaryKnowledgeMessages = true;

	/** How much knowledge experience standing near a creature gives per second. **/
	public int creatureProximityKnowledge = 0;

	/** How much knowledge experience killing a creature gives. **/
	public int creatureKillKnowledge = 50;

	/** How much knowledge experience breeding a creature gives. **/
	public int creatureBreedKnowledge = 400;

	/** How much knowledge experience feeding a treat to a creature gives. **/
	public int creatureTreatKnowledge = 100;

	/** How much knowledge experience studying (using a Soulgazer on) a creature gives. **/
	public int creatureStudyKnowledge = 100;

	/** The time in ticks it takes to be able to use a Soulgazer for knowledge again. Default is 200 (10 seconds). **/
	public int creatureStudyCooldown = 200;

	/** The knowledge experience scale for variant creatures. **/
	public double creatureVariantKnowledgeScale = 2;

	/** The knowledge experience scale for boss creatures. **/
	public double creatureBossKnowledgeScale = 5;


	// Bosses:
	/** How much higher players must be relative to a boss' y position (feet) to trigger anti flight measures. **/
	public double bossAntiFlight = 10;

	/** Caps how much damage a boss can take per tick, this also affects Rare Variants and Dungeon Bosses. **/
	public int bossDamageCap = 50;


	// Interaction:
	/** If true, some elemental mobs will fuse with each other on sight into a stronger different elemental. **/
	public boolean elementalFusion = true;

	/** Controls how fused mobs combine their levels. Can be 'both' (default) where both levels are added together, 'highest' where the higher level is used and 'lowest' where the lowest level is used. **/
	public String elementalFusionLevelMix = "both";

	/** The level of a mob created via fusion is multiplied by this value, set to 1 for no changes. Tamed fusions aren't multiplied by this value. **/
	public double elementalFusionLevelMultiplier = 10;

	/** If true, when a mob picks up a player, the player will be positioned where the mob is rather than offset to where the mob is holding the player at. **/
	public boolean disablePickupOffsets = false;

	/** If true, all mobs will be immune to suffocation damage. **/
	public boolean suffocationImmunity = false;

	/** If true, all mobs will be immune to damage from running out of air. **/
	public boolean drownImmunity = false;

	/** If true, mobs can be lured with treats even if they are in a pack. **/
	public boolean packTreatLuring = false;


	// Variations:
	/** If true, mobs will have a chance of becoming a subspecies when spawned. **/
	public boolean variantsSpawn = true;

	/** If true, mobs will vary in sizes when spawned. **/
	public boolean randomSizes = true;

	/** The minimum size scale mobs can randomly spawn at. **/
	public double randomSizeMin = 0.85D;

	/** The maximum size scale mobs can randomly spawn at. **/
	public double randomSizeMax = 1.15D;


	// Drops:
	/** A string of global drops to add to every mob. **/
	public String globalDropsString = "";

	/** A list of global drops to add to every mob. **/
	protected List<ItemDrop> globalDrops;


	/**
	 * Loads settings from the config.
	 */
	public void loadConfig() {
		// Client:
		this.subspeciesTags = ConfigCreatures.INSTANCE.subspeciesTags.get();
		this.idleSoundTicks = ConfigCreatures.INSTANCE.idleSoundTicks.get();
		this.disableModelAlpha = ConfigCreatures.INSTANCE.disableModelAlpha.get();
		this.disableBlockParticles = ConfigCreatures.INSTANCE.disableBlockParticles.get();

		// Stats:
		this.startingLevelMin = ConfigCreatures.INSTANCE.startingLevelMin.get();
		this.startingLevelMax = ConfigCreatures.INSTANCE.startingLevelMax.get();
		this.levelPerDay = ConfigCreatures.INSTANCE.levelPerDay.get();
		this.levelPerDayMax = ConfigCreatures.INSTANCE.levelPerDayMax.get();
		this.levelPerLocalDifficulty = ConfigCreatures.INSTANCE.levelPerLocalDifficulty.get();

		// Pets:
		this.ownerTags = ConfigCreatures.INSTANCE.ownerTags.get();
		this.tamingEnabled = ConfigCreatures.INSTANCE.tamingEnabled.get();
		this.mountingEnabled = ConfigCreatures.INSTANCE.mountingEnabled.get();
		this.mountingFlightEnabled = ConfigCreatures.INSTANCE.mountingFlightEnabled.get();
		this.friendlyFire = ConfigCreatures.INSTANCE.friendlyFire.get();
		this.petRespawnTime = ConfigCreatures.INSTANCE.petRespawnTime.get();
		this.petFollowDistance = ConfigCreatures.INSTANCE.petFollowDistance.get();
		this.soulboundDimensionList = ConfigCreatures.INSTANCE.soulboundDimensionList.get().replace(" ", "").split(",");
		this.soulboundDimensionListWhitelist = ConfigCreatures.INSTANCE.soulboundDimensionWhitelist.get();
		this.summonDimensionList = ConfigCreatures.INSTANCE.summonDimensionList.get().replace(" ", "").split(",");
		this.summonDimensionListWhitelist = ConfigCreatures.INSTANCE.summonDimensionWhitelist.get();

		// Beastiary:
		this.beastiaryKnowledgeMessages = ConfigCreatures.INSTANCE.beastiaryKnowledgeMessages.get();
		this.creatureProximityKnowledge = ConfigCreatures.INSTANCE.creatureProximityKnowledge.get();
		this.creatureKillKnowledge = ConfigCreatures.INSTANCE.creatureKillKnowledge.get();
		this.creatureBreedKnowledge = ConfigCreatures.INSTANCE.creatureBreedKnowledge.get();
		this.creatureTreatKnowledge = ConfigCreatures.INSTANCE.creatureTreatKnowledge.get();
		this.creatureStudyKnowledge = ConfigCreatures.INSTANCE.creatureStudyKnowledge.get();
		this.creatureStudyCooldown = ConfigCreatures.INSTANCE.creatureStudyCooldown.get();
		this.creatureVariantKnowledgeScale = ConfigCreatures.INSTANCE.creatureVariantKnowledgeScale.get();
		this.creatureBossKnowledgeScale = ConfigCreatures.INSTANCE.creatureBossKnowledgeScale.get();

		// Bosses:
		this.bossAntiFlight = ConfigCreatures.INSTANCE.bossAntiFlight.get();
		this.bossDamageCap = ConfigCreatures.INSTANCE.bossDamageCap.get();
		BaseCreatureEntity.BOSS_DAMAGE_LIMIT = this.bossDamageCap;

		// Interaction:
		this.elementalFusion = ConfigCreatures.INSTANCE.elementalFusion.get();
		this.elementalFusionLevelMix = ConfigCreatures.INSTANCE.elementalFusionLevelMix.get();
		this.elementalFusionLevelMultiplier = ConfigCreatures.INSTANCE.elementalFusionLevelMultiplier.get();
		this.disablePickupOffsets = ConfigCreatures.INSTANCE.disablePickupOffsets.get();
		this.suffocationImmunity = ConfigCreatures.INSTANCE.suffocationImmunity.get();
		this.drownImmunity = ConfigCreatures.INSTANCE.drownImmunity.get();
		this.packTreatLuring = ConfigCreatures.INSTANCE.packTreatLuring.get();

		// Variations:
		this.variantsSpawn = ConfigCreatures.INSTANCE.variantsSpawn.get();
		this.randomSizes = ConfigCreatures.INSTANCE.randomSizes.get();
		this.randomSizeMin = ConfigCreatures.INSTANCE.randomSizeMin.get();
		this.randomSizeMax = ConfigCreatures.INSTANCE.randomSizeMax.get();

		// Drops:
		this.globalDropsString = ""; // Disabled for now.

		Variant.loadGlobalSettings();
	}


	/**
	 * Returns a list of item drops to be drops by all mobs.
	 * @return Global item drops list.
	 */
	public List<ItemDrop> getGlobalDrops() {
		if(this.globalDrops == null) {
			this.globalDrops = new ArrayList<>();
			if(this.globalDropsString != null && this.globalDropsString.length() > 0) {
				for (String customDropEntryString : this.globalDropsString.replace(" ", "").split(";")) {
					ItemDrop itemDrop = ItemDrop.createFromConfigString(customDropEntryString);
					if(itemDrop != null) {
						this.globalDrops.add(itemDrop);
					}
				}
			}
		}
		return this.globalDrops;
	}

	public boolean isSoulboundAllowed(Level world) {
		if(this.soulboundDimensionList.length > 0) {
			boolean inDimensionList = false;
			for (String dimensionId : this.soulboundDimensionList) {
				if (dimensionId.equals(world.dimension().location().toString())) {
					inDimensionList = true;
					break;
				}
			}
			if (inDimensionList && !this.soulboundDimensionListWhitelist) {
				return false;
			}
			if (!inDimensionList && this.soulboundDimensionListWhitelist) {
				return false;
			}
		}

		return true;
	}

	public boolean isSummoningAllowed(Level world) {
		if(this.summonDimensionList.length > 0) {
			boolean inDimensionList = false;
			for (String dimensionId : this.summonDimensionList) {
				if (dimensionId.equals(world.dimension().location().toString())) {
					inDimensionList = true;
					break;
				}
			}
			if (inDimensionList && !this.summonDimensionListWhitelist) {
				return false;
			}
			if (!inDimensionList && this.summonDimensionListWhitelist) {
				return false;
			}
		}

		return true;
	}
}
