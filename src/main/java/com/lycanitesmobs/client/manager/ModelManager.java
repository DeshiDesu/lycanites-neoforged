package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModel;
import com.lycanitesmobs.client.model.creature.base.CreatureObjModelOld;
import com.lycanitesmobs.client.model.projectile.base.ProjectileObjModel;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.client.model.creature.base.CreatureModel;
import com.lycanitesmobs.client.model.item.EquipmentModel;
import com.lycanitesmobs.client.model.item.ModelEquipmentPart;
import com.lycanitesmobs.client.model.projectile.base.ProjectileModel;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.creature.Subspecies;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.manager.EquipmentPartManager;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModelManager {
    private static ModelManager INSTANCE;

    public static ModelManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModelManager();
        }
        return INSTANCE;
    }

    public Map<CreatureInfo, CreatureModel> creatureModels = new HashMap<>();
    public Map<Subspecies, CreatureModel> creatureSubspeciesModels = new HashMap<>();
    public Map<ProjectileInfo, ProjectileModel> projectileModels = new HashMap<>();
    public Map<String, ProjectileModel> oldProjectileModels = new HashMap<>();
    public EquipmentModel equipmentModel;
    public Map<ItemEquipmentPart, ModelEquipmentPart> equipmentPartModels = new HashMap<>();

    public void reloadModels(ResourceManager resourceManager) {
        LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: begin");
        LMHelperClass.logDebug(
                "Resources",
                "ModelManager.reloadModels: base=" + this.creatureModels.size()
                        + " subspecies=" + this.creatureSubspeciesModels.size()
                        + " equipmentParts=" + this.equipmentPartModels.size()
        );

        for (CreatureModel model : this.creatureModels.values()) {
            if (model instanceof CreatureObjModel obj) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading base model " + obj.getClass().getName());
                obj.reloadModel(resourceManager);
            } else if (model instanceof CreatureObjModelOld old) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading base old model " + old.getClass().getName());
                old.reloadModel(resourceManager);
            }
        }

        for (CreatureModel model : this.creatureSubspeciesModels.values()) {
            if (model instanceof CreatureObjModel obj) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading subspecies model " + obj.getClass().getName());
                obj.reloadModel(resourceManager);
            } else if (model instanceof CreatureObjModelOld old) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading subspecies old model " + old.getClass().getName());
                old.reloadModel(resourceManager);
            }
        }

        for (ModelEquipmentPart itemModel : this.equipmentPartModels.values()) {
            if (itemModel != null) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading equipment model " + itemModel.getClass().getName());
                itemModel.reloadModel(resourceManager);
            }
        }
        for (ProjectileModel model : this.projectileModels.values()) {
            if (model instanceof ProjectileObjModel obj) {
                LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: reloading projectile model " + obj.getClass().getName());
                obj.reloadModel(resourceManager);
            }
        }

        LMHelperClass.logDebug("Resources", "ModelManager.reloadModels: end");
    }


    /**
     * Creates all models to be used.
     */
    public void createModels() {
        try {
            // Creature Models:
            for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
                if (creatureInfo.dummy) {
                    continue;
                }
                this.creatureModels.put(creatureInfo, (CreatureModel) Class.forName(creatureInfo.modelClassName).getConstructor().newInstance());
                for (Subspecies subspecies : creatureInfo.subspecies.values()) {
                    if (subspecies.modelClassName != null) {
                        this.creatureSubspeciesModels.put(subspecies, (CreatureModel) Class.forName(subspecies.modelClassName).getConstructor().newInstance());
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            LMHelperClass.logError("Unable to load a Creature model, check that the model class name(s) is correct in the associated creature json (check subspecies models if any too).");
            throw new RuntimeException(e);
        }

        try {
            // Projectile Models:
            for (ProjectileInfo projectileInfo : ProjectileManager.getInstance().projectiles.values()) {
                if (projectileInfo.modelClassName != null) {
                    this.projectileModels.put(projectileInfo, (ProjectileModel) Class.forName(projectileInfo.modelClassName).getConstructor().newInstance());
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            LMHelperClass.logError("Unable to load a Projectile model, check that the model class name is correct in the associated projectile json.");
            throw new RuntimeException(e);
        }

        // Equipment Models:
        this.equipmentModel = new EquipmentModel();
        for (String equipmentPartName : EquipmentPartManager.getInstance().equipmentParts.keySet()) {
            this.equipmentPartModels.put((ItemEquipmentPart) ObjectManager.getItem(equipmentPartName), new ModelEquipmentPart((ItemEquipmentPart) ObjectManager.getItem(equipmentPartName)));
        }
    }

    /**
     * Gets the model used by the provided Creature and Subspecies.
     *
     * @param creatureInfo The creature info to get the model for.
     * @param subspecies   The creature's subspecies, can be null for default model.
     * @return The Creature Model.
     */
    public CreatureModel getCreatureModel(CreatureInfo creatureInfo, @Nullable Subspecies subspecies) {
        if (subspecies != null && this.creatureSubspeciesModels.containsKey(subspecies)) {
            return this.creatureSubspeciesModels.get(subspecies);
        }
        if (this.creatureModels.containsKey(creatureInfo)) {
            return this.creatureModels.get(creatureInfo);
        }
        return null;
    }

    /**
     * Gets the model used by the provided Projectile.
     *
     * @param projectileInfo The projectile info to get the model for.
     * @return The Projectile Model.
     */
    public ProjectileModel getProjectileModel(ProjectileInfo projectileInfo) {
        if (this.projectileModels.containsKey(projectileInfo)) {
            return this.projectileModels.get(projectileInfo);
        }
        return null;
    }

    /**
     * Gets the model used by the provided Old Projectile Name, this should be phased out in favor of the new JSON based projectiles.
     *
     * @param projectileName The name of the old projectile to get the model for.
     * @return The Old Projectile Model.
     */
    @Deprecated
    public ProjectileModel getOldProjectileModel(String projectileName) {
        if (this.oldProjectileModels.containsKey(projectileName)) {
            return this.oldProjectileModels.get(projectileName);
        }
        return null;
    }

    /**
     * Gets the model used by assembled Equipment Pieces.
     *
     * @return The Equipment Model, the same model should always be used.
     */
    public EquipmentModel getEquipmentModel() {
        return this.equipmentModel;
    }

    /**
     * Gets the model used by the provided Equipment Part.
     *
     * @param equipmentPart The equipment part item to get the model for.
     * @return The Equipment Part Model.
     */
    public ModelEquipmentPart getEquipmentPartModel(ItemEquipmentPart equipmentPart) {
        if (this.equipmentPartModels.containsKey(equipmentPart)) {
            return this.equipmentPartModels.get(equipmentPart);
        }
        return null;
    }

    public int getLoadedModelCount() {
        return creatureModels.size();
    }
}
