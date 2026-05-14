package com.lycanitesmobs.core.data.info.altar;

import com.lycanitesmobs.core.data.config.ConfigMobEvent;
import com.lycanitesmobs.core.event.mobevent.effects.StructureBuilder;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.event.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.mobevents.AmalgalichStructureBuilder;
import com.lycanitesmobs.core.worldgen.mobevents.AsmodeusStructureBuilder;
import com.lycanitesmobs.core.worldgen.mobevents.RahovartStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class AltarInfo {
    // ========== Global Settings ==========
    /** If false, altars wont work. **/
    public static boolean altarsEnabled = true;

    /** If set to true, Altars will only activate in dimensions that the monster spawned or event started is allowed in. **/
    public static boolean checkDimensions = false;

    /** A list of all Altars, typically cycled through when a Soulkey is used. **/
    public static Map<String, AltarInfo> altars = new HashMap<String, AltarInfo>();


    // ========== Properties ==========
    public String name = "Altar";

    public AltarMobEventTrigger mobEventTrigger;


    // ==================================================
    //        Load Global Settings From Config
    // ==================================================
    public static void loadGlobalSettings() {
        altarsEnabled = ConfigMobEvent.INSTANCE.altarsEnabled.get();
        checkDimensions = ConfigMobEvent.INSTANCE.altarsCheckDimension.get();
    }


    // ==================================================
    //                    Enabled
    // ==================================================
    public static boolean checkAltarsEnabled() {
        if(altars.size() < 1)
            return false;
        return altarsEnabled;
    }


    // ==================================================
    //                    Altar List
    // ==================================================
    public static void addAltar(AltarInfo altarInfo) {
        altars.put(altarInfo.name.toLowerCase(), altarInfo);
    }

    public static AltarInfo getAltar(String name) {
        if(altars.containsKey(name.toLowerCase())) {
            return altars.get(name.toLowerCase());
        }
        return null;
    }


    // ==================================================
    //                   Create Altars
    // ==================================================
    public static void createAltars() {
        AltarInfo ebonMalwrathAltar = new AltarInfoEbonMalwrath("EbonMalwrathAltar");
        addAltar(ebonMalwrathAltar);

        AltarInfo rahovartAltar = new AltarInfoRahovart("RahovartAltar");
        addAltar(rahovartAltar);
        StructureBuilder.addStructureBuilder(new RahovartStructureBuilder());

        AltarInfo asmodeusAltar = new AltarInfoAsmodeus("AsmodeusAltar");
        addAltar(asmodeusAltar);
        StructureBuilder.addStructureBuilder(new AsmodeusStructureBuilder());

        AltarInfo amalgalichAltar = new AltarInfoAmalgalich("AmalgalichAltar");
        addAltar(amalgalichAltar);
        StructureBuilder.addStructureBuilder(new AmalgalichStructureBuilder());

        AltarInfo umberCherufeAltar = new AltarInfoUmberCherufe("UmberCherufeAltar");
        addAltar(umberCherufeAltar);

        AltarInfo celestialGeonachAltar = new AltarInfoCelestialGeonach("CelestialGeonachAltar");
        addAltar(celestialGeonachAltar);

        AltarInfo lunarGrueAltar = new AltarInfoLunarGrue("LunarGrueAltar");
        addAltar(lunarGrueAltar);

        AltarInfo mottledAbaiaAltar = new AltarInfoMottleAbaia("MottledAbaia");
        addAltar(mottledAbaiaAltar);

        AltarInfo crimsonEpionAltar = new AltarInfoCrimsonEpion("CrimsonEpion");
        addAltar(crimsonEpionAltar);

        AltarInfo royalApollyonAltar = new AltarInfoRoyalApollyon("RoyalApollyon");
        addAltar(royalApollyonAltar);

        AltarInfo phosphorescentChupacabraAltar = new AltarInfoPhosphorescentChupacabra("PhosphorescentChupacabra");
        addAltar(phosphorescentChupacabraAltar);
    }


    // ==================================================
    //                    Constructor
    // ==================================================
    public AltarInfo(String name) {
        this.name = name;
    }


    // ==================================================
    //                     Checking
    // ==================================================
    /** Called before all other checks to see if the player has permission to build in the area of this Altar using the BlockPlaceEvent. **/
    public boolean checkBlockEvent(Entity entity, Level world, BlockPos pos) {
        return true;
    }

    /** Called first when checking for a valid altar, this should be fairly lightweight such as just checking if the first block checked is valid, a more in depth check if then done after. **/
    public boolean quickCheck(Entity entity, Level world, BlockPos pos) {
        return false;
    }

    /** Called if the QuickCheck() is passed, this should check the entire altar structure and if true is returned, the altar will activate. **/
    public boolean fullCheck(Entity entity, Level world, BlockPos pos) {
        return false;
    }


    // ==================================================
    //                     Activate
    // ==================================================
    /** Called when this Altar should activate. This will typically destroy the Altar and summon a rare mob or activate an event such as a boss event. If false is returned then the activation did not work, this is the place to check for things like dimensions. **/
    public boolean activate(Entity entity, Level world, BlockPos pos, int variant) {
    	if(this.mobEventTrigger == null)
        	return false;
    	return this.mobEventTrigger.onActivate(entity, world, pos, variant);
    }


    // ==================================================
    //                     Utility
    // ==================================================
    public BlockPos getFacingPosition(BlockPos pos, double distance, double angle) {
        double xAmount = -Math.sin(Math.toRadians(angle));
        double zAmount = Math.cos(Math.toRadians(angle));
        BlockPos facingPos = new BlockPos(
            LMHelperClass.convertToInteger(Math.round((double)pos.getX() + (distance * xAmount))),
            pos.getY(),
                LMHelperClass.convertToInteger(Math.round((double) pos.getZ() + (distance * zAmount)))
        );
        return facingPos;
    }

    protected boolean spawnDeferred(Level world, BlockPos pos, String key, Entity entity, Runnable afterSpawn) {
        if (world instanceof ServerLevel serverLevel) {
            return DeferredLevelActionManager.enqueue(serverLevel, pos, key, level -> {
                level.addFreshEntity(entity);
                if (afterSpawn != null) {
                    afterSpawn.run();
                }
            });
        }

        world.addFreshEntity(entity);
        if (afterSpawn != null) {
            afterSpawn.run();
        }
        return true;
    }
}
