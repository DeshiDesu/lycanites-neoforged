package com.lycanitesmobs.client.model.projectile;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.projectile.base.ProjectileObjModel;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LobDarklingsModel extends ProjectileObjModel {

    // ==================================================
    //                    Constructors
    // ==================================================
    public LobDarklingsModel() {
        this(1.0F);
    }

    public LobDarklingsModel(float shadowSize) {

        // Load Model:
        this.initModel("lobdarklings", LycanitesMobs.modInfo, "projectile/lobdarklings");
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, BaseProjectileEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        this.rotate(0, loop * 8, 0);
    }
}
