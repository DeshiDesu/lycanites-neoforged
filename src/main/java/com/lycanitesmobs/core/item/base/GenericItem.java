package com.lycanitesmobs.core.item.base;


public class GenericItem extends BaseItem {
    public String modelName;
    public final Properties properties;

    public GenericItem(Properties properties, String itemName, String modelName) {
        super(properties);
        this.properties = properties;
        this.itemName = itemName;
        this.modelName = modelName;
        super.setup();
    }
}
