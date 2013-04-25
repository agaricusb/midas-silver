package agaricus.midasplugins;

import com.mojang.nbt.ListTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;
import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import pfaeff.IDChanger;

import java.util.HashMap;

public class ChargingBenchGregTechPlugin implements ConverterPlugin {

    @Override
    public String getPluginName() {
        return "IC2 Charging Bench -> GregTech Charge-O-Mat tile entity converter plugin";
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
            Tag idTag = tileEntity.findChildByName("id", false);
            if (idTag == null || !(idTag instanceof StringTag)) {
                System.out.println("- unrecognized ID tag: "+ idTag);
                continue;
            }
            StringTag idTagString = (StringTag) idTag;

            if (idTag.toString().startsWith("Charging Bench Mk")) {
                System.out.println("Found charging bench");

                idTagString.data = "Charge_O_Mat";

                Tag itemsTag = tileEntity.findChildByName("Items", false);
                if (itemsTag == null) {
                    System.out.println("- unrecognized Items tag: " + itemsTag);
                    continue;
                }

                itemsTag.setName("Inventory");

                System.out.println("Converted to Charge_O_Mat");

                IDChanger.changedPlaced++;
            }
        }
    }
}
