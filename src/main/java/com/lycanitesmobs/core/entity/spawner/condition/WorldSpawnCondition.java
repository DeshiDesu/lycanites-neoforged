package com.lycanitesmobs.core.entity.spawner.condition;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldSpawnCondition extends SpawnCondition {

    /**
     * How the dimension ID list works. Can be whitelist or blacklist.
     **/
    public String dimensionListType = "whitelist";

    /**
     * The dimension IDs that the world must or must not match depending on the list type.
     **/
    public List<String> dimensionIds = new ArrayList<>();

    /**
     * The dimensions that the world must or must not match depending on the list type.
     **/
    public List<DimensionType> dimensions = null;

    /**
     * How the biomes from the biome tags list works. Can be whitelist or blacklist.
     **/
    public String biomeTagListType = "whitelist";

    /**
     * The list of biome tags to filter this condition by.
     **/
    public List<String> biomeTags = new ArrayList<>();
    /**
     * The list of biomes generated from the list of biome tags.
     **/
    public Set<String> biomesFromTags = null;
    public List<String> biomeTagBlacklist = new ArrayList<>();

    public Set<String> biomesFromTagBlacklist = null;

    /**
     * How the biomes from the biome ids list works. Can be whitelist or blacklist.
     **/
    public String biomeIdListType = "whitelist";

    /**
     * The list of specific biome ids that this creature spawns in.
     **/
    public List<String> biomeIds = new ArrayList<>();

    /**
     * The minimum world days that must have gone by, can accept fractions such as 5.25 for 5 and a quarter days.
     **/
    public double worldDayMin = -1;

    /**
     * The maximum world days that this condition is true up to.
     **/
    public double worldDayMax = -1;

    /**
     * The interval of days this condition is true such as every 7 days.
     **/
    public double worldDayN = -1;

    /**
     * The minimum time of the current world day.
     **/
    public int dayTimeMin = -1;

    /**
     * The maximum time of the current world day.
     **/
    public int dayTimeMax = -1;

    /**
     * The weather, can be: any, clear, rain, storm, rainstorm (raining and thundering) or notclear (raining or thundering).
     **/
    public String weather = "any";

    /**
     * The minimum difficulty level.
     **/
    public short difficultyMin = -1;

    /**
     * The maximum difficulty level.
     **/
    public short difficultyMax = -1;

    /**
     * The required moon phase. 0 is a full moon.
     **/
    public float moonPhase = -1;

    public long salt = 0L;
    public int spacing = 32;
    public int separation = 8;
    public int minY = 4;
    public int maxY = 64;
    public int maxDistanceFromCenter = 48;
    public int estimatedHeight = 32;

    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("dimensionIds")) {
            this.dimensionIds.clear();
            this.dimensions = null;
            this.dimensionIds = JSONHelper.getJsonStrings(json.get("dimensionIds").getAsJsonArray());
        }

        if (json.has("dimensionListType"))
            this.dimensionListType = json.get("dimensionListType").getAsString();

        if (json.has("biomeTags")) {
            this.biomeTags.clear();
            this.biomeTagBlacklist.clear();
            this.biomesFromTags = null;
            this.biomesFromTagBlacklist = null;
            JSONHelper.clearBiomeTagCache();

            List<String> rawTags = JSONHelper.getJsonStrings(json.get("biomeTags").getAsJsonArray());
            for (String raw : rawTags) {
                if (raw != null && !raw.isEmpty() && raw.charAt(0) == '-') {
                    String stripped = raw.substring(1);
                    if (!stripped.isEmpty()) {
                        this.biomeTagBlacklist.add(stripped);
                    }
                }
                this.biomeTags.add(raw);
            }
        } else if (json.has("biomes")) {
            this.biomeTags.clear();
            this.biomeTagBlacklist.clear();
            this.biomesFromTags = null;
            this.biomesFromTagBlacklist = null;
            JSONHelper.clearBiomeTagCache();

            List<String> rawTags = JSONHelper.getJsonStrings(json.get("biomes").getAsJsonArray());
            for (String raw : rawTags) {
                if (raw != null && !raw.isEmpty() && raw.charAt(0) == '-') {
                    String stripped = raw.substring(1);
                    if (!stripped.isEmpty()) {
                        this.biomeTagBlacklist.add(stripped);
                    }
                }
                this.biomeTags.add(raw);
            }
        }

        if (json.has("biomeTagListType"))
            this.biomeTagListType = json.get("biomeTagListType").getAsString();

        if (json.has("biomeIds")) {
            this.biomeIds.clear();
            this.biomeIds = JSONHelper.getJsonStrings(json.get("biomeIds").getAsJsonArray());
        }

        if (json.has("biomeIdListType"))
            this.biomeIdListType = json.get("biomeIdListType").getAsString();

        if (json.has("worldDayMin"))
            this.worldDayMin = json.get("worldDayMin").getAsInt();

        if (json.has("worldDayMax"))
            this.worldDayMax = json.get("worldDayMax").getAsInt();

        if (json.has("worldDayN"))
            this.worldDayN = json.get("worldDayN").getAsInt();

        if (json.has("dayTimeMin"))
            this.dayTimeMin = json.get("dayTimeMin").getAsInt();

        if (json.has("dayTimeMax"))
            this.dayTimeMax = json.get("dayTimeMax").getAsInt();

        if (json.has("weather"))
            this.weather = json.get("weather").getAsString();

        if (json.has("difficultyMin"))
            this.difficultyMin = json.get("difficultyMin").getAsShort();

        if (json.has("difficultyMax"))
            this.difficultyMax = json.get("difficultyMax").getAsShort();

        if (json.has("moonPhase"))
            this.moonPhase = json.get("moonPhase").getAsFloat();

        if (json.has("salt"))
            this.salt = json.get("salt").getAsLong();

        if (json.has("spacing"))
            this.spacing = json.get("spacing").getAsInt();

        if (json.has("separation"))
            this.separation = json.get("separation").getAsInt();

        if (json.has("minY"))
            this.minY = json.get("minY").getAsInt();

        if (json.has("maxY"))
            this.maxY = json.get("maxY").getAsInt();

        if (json.has("maxDistanceFromCenter"))
            this.maxDistanceFromCenter = json.get("maxDistanceFromCenter").getAsInt();

        if (json.has("estimatedHeight"))
            this.estimatedHeight = json.get("estimatedHeight").getAsInt();

        super.loadFromJSON(json);
    }


    public long getSalt() {
        return this.salt;
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public int getMaxDistanceFromCenter() {
        return this.maxDistanceFromCenter;
    }

    public int getEstimatedHeight() {
        return this.estimatedHeight;
    }

    @Override
    public boolean isMet(Level world, Player player, BlockPos position) {
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);

        long baseTime = worldExt != null && worldExt.useTotalWorldTime
                ? world.getGameTime()
                : world.getDayTime();

        int time = (int) (world.getDayTime() % 24000L);
        int day = (int) (baseTime / 24000L);

        if (this.worldDayMin >= 0 && day < this.worldDayMin) {
            return false;
        }
        if (this.worldDayMax >= 0 && day > this.worldDayMax) {
            return false;
        }
        if (this.worldDayN >= 0 && (day == 0 || day % this.worldDayN != 0)) {
            return false;
        }

        if (this.dayTimeMin >= 0 && time < this.dayTimeMin) {
            return false;
        }
        if (this.dayTimeMax >= 0 && time > this.dayTimeMax) {
            return false;
        }

        if ("clear".equalsIgnoreCase(this.weather) && (world.isRaining() || world.isThundering())) {
            return false;
        } else if ("rain".equalsIgnoreCase(this.weather) && (!world.isRaining() || world.isThundering())) {
            return false;
        } else if ("storm".equalsIgnoreCase(this.weather) && !world.isThundering()) {
            return false;
        } else if ("rainstorm".equalsIgnoreCase(this.weather) && (!world.isRaining() || !world.isThundering())) {
            return false;
        } else if ("notclear".equalsIgnoreCase(this.weather) && (!world.isRaining() && !world.isThundering())) {
            return false;
        }

        if (this.difficultyMin >= 0 && world.getDifficulty().getId() < this.difficultyMin) {
            return false;
        }
        if (this.difficultyMax >= 0 && world.getDifficulty().getId() > this.difficultyMax) {
            return false;
        }

        if (this.moonPhase >= 0 && world.dimensionType().moonPhase(world.dayTime()) != this.moonPhase) {
            return false;
        }

        if (!this.isAllowedDimension(world)) {
            return false;
        }

        if (!this.isAllowedBiome(world, position)) {
            return false;
        }

        return super.isMet(world, player, position);
    }


    /**
     * Returns if the dimension of the provided world passes this condition.
     *
     * @param world The world to get the biome from.
     * @return True if the biome is allowed, false if not.
     */
    public boolean isAllowedDimension(Level world) {
        String dimension = world.dimension().location().toString();

        if (this.dimensionIds.isEmpty()) {
            return true;
        }

        for (String dimensionId : this.dimensionIds) {
            if (dimensionId.equals(dimension)) {
                boolean allowed = !"blacklist".equalsIgnoreCase(this.dimensionListType);
                return allowed;
            }
        }

        boolean allowed = "blacklist".equalsIgnoreCase(this.dimensionListType);
        return allowed;
    }


    /**
     * Returns if the biome of the provided position passes this condition.
     *
     * @param world    The world to get the biome from.
     * @param position The position to get the biome from, can be null.
     * @return True if the biome is allowed, false if not.
     */
    public boolean isAllowedBiome(Level world, BlockPos position) {
        if (position != null) {
            ResourceLocation biomeId = world
                    .getBiome(position)
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);

            if (biomeId == null) {
                LMHelperClass.logDebug("Dungeon", "WorldSpawnCondition biome check: biome ResourceLocation is null at " + position);
                if (this.biomeIds.isEmpty() && this.biomeTags.isEmpty()) {
                    return true;
                }
                return false;
            }

            String biomeIdString = biomeId.toString();

            if (!this.biomeTagBlacklist.isEmpty()) {
                if (this.biomesFromTagBlacklist == null) {
                    this.biomesFromTagBlacklist = new HashSet<>(JSONHelper.getBiomesFromTags(world, this.biomeTagBlacklist));
                }
                if (this.biomesFromTagBlacklist.contains(biomeIdString)) {
                    return false;
                }
            }

            if (!this.biomeIds.isEmpty()) {
                if (this.biomeIds.contains(biomeIdString)) {
                    return !"blacklist".equalsIgnoreCase(this.biomeIdListType);
                }
            }

            if (!this.biomeTags.isEmpty()) {
                if (this.biomesFromTags == null) {
                    this.biomesFromTags = new HashSet<>(JSONHelper.getBiomesFromTags(world, this.biomeTags));
                }
                if (this.biomesFromTags.contains(biomeIdString)) {
                    return !"blacklist".equalsIgnoreCase(this.biomeTagListType);
                }
            }
        }

        if (this.biomeIds.isEmpty() && this.biomeTags.isEmpty()) {
            return true;
        }

        return "blacklist".equalsIgnoreCase(this.biomeIdListType)
                && "blacklist".equalsIgnoreCase(this.biomeTagListType);
    }


    public void collectDebugFailures(Level world, Player player, BlockPos position, List<String> reasons) {
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);

        long baseTime = worldExt != null && worldExt.useTotalWorldTime
                ? world.getGameTime()
                : world.getDayTime();

        int time = (int) (world.getDayTime() % 24000L);
        int day = (int) (baseTime / 24000L);

        if (this.worldDayMin >= 0 && day < this.worldDayMin) {
            reasons.add("day " + day + " < worldDayMin " + this.worldDayMin);
        }
        if (this.worldDayMax >= 0 && day > this.worldDayMax) {
            reasons.add("day " + day + " > worldDayMax " + this.worldDayMax);
        }
        if (this.worldDayN >= 0 && (day == 0 || day % this.worldDayN != 0)) {
            reasons.add("day " + day + " not on interval worldDayN " + this.worldDayN);
        }

        if (this.dayTimeMin >= 0 && time < this.dayTimeMin) {
            reasons.add("time " + time + " < dayTimeMin " + this.dayTimeMin);
        }
        if (this.dayTimeMax >= 0 && time > this.dayTimeMax) {
            reasons.add("time " + time + " > dayTimeMax " + this.dayTimeMax);
        }

        if ("clear".equalsIgnoreCase(this.weather) && (world.isRaining() || world.isThundering())) {
            reasons.add("weather=clear but raining=" + world.isRaining() + " thundering=" + world.isThundering());
        } else if ("rain".equalsIgnoreCase(this.weather) && (!world.isRaining() || world.isThundering())) {
            reasons.add("weather=rain but raining=" + world.isRaining() + " thundering=" + world.isThundering());
        } else if ("storm".equalsIgnoreCase(this.weather) && !world.isThundering()) {
            reasons.add("weather=storm but not thundering");
        } else if ("rainstorm".equalsIgnoreCase(this.weather) && (!world.isRaining() || !world.isThundering())) {
            reasons.add("weather=rainstorm but raining=" + world.isRaining() + " thundering=" + world.isThundering());
        } else if ("notclear".equalsIgnoreCase(this.weather) && (!world.isRaining() && !world.isThundering())) {
            reasons.add("weather=notclear but world is clear");
        }

        if (this.difficultyMin >= 0 && world.getDifficulty().getId() < this.difficultyMin) {
            reasons.add("difficulty " + world.getDifficulty().getId() + " < difficultyMin " + this.difficultyMin);
        }
        if (this.difficultyMax >= 0 && world.getDifficulty().getId() > this.difficultyMax) {
            reasons.add("difficulty " + world.getDifficulty().getId() + " > difficultyMax " + this.difficultyMax);
        }

        if (this.moonPhase >= 0 && world.dimensionType().moonPhase(world.dayTime()) != this.moonPhase) {
            reasons.add("moonPhase " + world.dimensionType().moonPhase(world.dayTime()) + " != required " + this.moonPhase);
        }

        if (!this.isAllowedDimension(world)) {
            reasons.add("dimension " + world.dimension().location() + " disallowed (dimensionIds=" + this.dimensionIds + ", type=" + this.dimensionListType + ")");
        }

        if (position != null && !this.isAllowedBiome(world, position)) {
            reasons.add("biome disallowed at " + position +
                    " (biomeIds=" + this.biomeIds +
                    ", biomeTags=" + this.biomeTags +
                    ", biomeIdListType=" + this.biomeIdListType +
                    ", biomeTagListType=" + this.biomeTagListType + ")");
        }
    }


}
