package com.lycanitesmobs.core.item.equipment;

import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.item.base.CreatureTypeItem;


public class CreatureSaddleItem extends CreatureTypeItem {

    public CreatureSaddleItem(Properties properties, CreatureType creatureType) {
        super(properties, creatureType.getSaddleName(), creatureType);
        this.setup();
        setup();
    }
}
