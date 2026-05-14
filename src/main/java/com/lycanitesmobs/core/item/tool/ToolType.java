package com.lycanitesmobs.core.item.tool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class ToolType {
    private static final Pattern VALID_NAME = Pattern.compile("[^a-z_]");
    private static final Map<String, ToolType> VALUES = new ConcurrentHashMap();
    public static final ToolType AXE = get("axe");
    public static final ToolType HOE = get("hoe");
    public static final ToolType PICKAXE = get("pickaxe");
    public static final ToolType SHOVEL = get("shovel");
    private final String name;

    public static ToolType get(String name) {
        return (ToolType)VALUES.computeIfAbsent(name, (k) -> {
            if (VALID_NAME.matcher(name).find()) {
                throw new IllegalArgumentException("ToolType.get() called with invalid name: " + name);
            } else {
                return new ToolType(name);
            }
        });
    }

    private ToolType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
