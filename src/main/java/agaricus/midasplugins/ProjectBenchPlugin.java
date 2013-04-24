package agaricus.midasplugins;

import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;
import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectBenchPlugin implements ConverterPlugin {
    @Override
    public String getPluginName() {
        return "RedPower2 Project Table -> bau5 Project Bench tile entity converter plugin";
    }

    @Override
    public PluginType getPluginType() {
        return PluginType.REGION;
    }

    @Override
    public void convert(Tag root, HashMap<BlockUID, BlockUID> translations) {
        System.out.println("PB TAG:"+root);
        root.print(System.out);

        Tag tileEntitiesTag = root.findChildByName("TileEntities", true);
        if (!(tileEntitiesTag instanceof ListTag)) {
            System.out.println("TileEntities not list: " + tileEntitiesTag);
            return;
        }
        ListTag tileEntitiesListTag = (ListTag) tileEntitiesTag;
        System.out.println("PB TE found:"+tileEntitiesListTag.size());

        for (int i = 0; i < tileEntitiesListTag.size(); ++i) {
            Tag tileEntity = tileEntitiesListTag.get(i);

            Tag idTag = tileEntity.findChildByName("id", false);

            System.out.println(" - idTag="+idTag+" name="+(idTag == null ? "null" : idTag.getName()));
        }
    }
}
