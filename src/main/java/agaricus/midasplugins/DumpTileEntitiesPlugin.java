package agaricus.midasplugins;

import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;
import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;

import java.util.HashMap;

public class DumpTileEntitiesPlugin implements ConverterPlugin {

    @Override
    public String getPluginName() {
        return "Dump tile entities";
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.REGION;
    }

    @Override
    public void convert(Tag root, HashMap<BlockUID, BlockUID> translations) {
        Tag tileEntitiesTag = root.findChildByName("TileEntities", true);
        if (!(tileEntitiesTag instanceof ListTag)) {
            System.out.println("TileEntities not list: " + tileEntitiesTag);
            return;
        }
        ListTag tileEntitiesListTag = (ListTag) tileEntitiesTag;

        for (int i = 0; i < tileEntitiesListTag.size(); ++i) {
            Tag tileEntity = tileEntitiesListTag.get(i);

            System.out.println("Tile entity "+i+":");
            tileEntity.print(System.out);
        }
    }
}
