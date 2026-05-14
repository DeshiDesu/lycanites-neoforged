package com.lycanitesmobs.core.worldgen.dungeon.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class SectorSegment {
    /** Sector Segments are the floors, walls and ceilings of a sector. **/

	/** A list of layers that make up this segment. **/
	public Map<Integer, SectorLayer> layers = new HashMap<>();

	/** The padding of this segment, increased when negative layers are added. **/
	public int padding = 0;


    /** Loads this Dungeon Sector Segment from the provided JSON data. **/
	public void loadFromJSON(JsonElement json) {
		for(JsonElement jsonElement : json.getAsJsonArray()) {
			JsonObject layerJson = jsonElement.getAsJsonObject();
			if(!layerJson.has("layer") || !layerJson.has("pattern"))
				continue;
			String layerNumber = layerJson.get("layer").getAsString();
			if(!NumberUtils.isCreatable(layerNumber))
				continue;
			int layerIndex = NumberUtils.createInteger(layerNumber);

			if(layerIndex < 0 && -layerIndex > this.padding) {
				this.padding = -layerIndex;
			}

			SectorLayer layer = new SectorLayer();
			layer.loadFromJSON(layerJson);

			this.layers.put(layerIndex, layer);
		}
	}
}
