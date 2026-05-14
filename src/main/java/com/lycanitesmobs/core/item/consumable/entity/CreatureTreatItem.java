package com.lycanitesmobs.core.item.consumable.entity;

import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.item.base.CreatureTypeItem;


public class CreatureTreatItem extends CreatureTypeItem {

    public CreatureTreatItem(Properties properties, CreatureType creatureType) {
        super(properties, creatureType.getTreatName(), creatureType);
        this.setup();
    }
}
