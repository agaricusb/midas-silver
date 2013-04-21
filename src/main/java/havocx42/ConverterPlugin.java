package havocx42;

import java.util.HashMap;

import pfaeff.IDChanger;

import com.mojang.nbt.Tag;

public interface ConverterPlugin {

    /**
     * Method to return a human readable name for the Plugin
     * 
     * @return A human readable name for the plugin
     */
    public String getPluginName();

    /**
     * Method to return the plugin type. REGION for a plugin that converts
     * placed blocks or blocks in entity inventories and expects to receive the
     * root Tag of a Region file PLAYER for a plugin that converts the items in
     * a player inventory and expects to receive the root Tag of a player file
     * 
     * @return The plugin type
     */
    public PluginType getPluginType();

    /**
     * The method that does the conversion
     * 
     * @param status The status object, update this to change the UI
     * @param root The root tag of the region or Playerfile
     * @param translations The translations that should be performed.
     */
    public void convert(Status status, Tag root, HashMap<BlockUID, BlockUID> translations);

}
