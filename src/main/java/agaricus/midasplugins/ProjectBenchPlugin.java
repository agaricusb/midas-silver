package agaricus.midasplugins;

import com.mojang.nbt.ListTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;
import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import pfaeff.IDChanger;

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

            if (idTag.toString().equals("RPAdvBench")) {
                System.out.println("Found RPAdvBench");

                /* example RedPower Project Table NBT:
                TAG_List("Items"): 14 entries of type TAG_Compound
                {
                   TAG_Compound: 4 entries
                   {
                      TAG_Short("id"): 30188
                      TAG_Short("Damage"): 0
                      TAG_Byte("Count"): 64
                      TAG_Byte("Slot"): 26
                   }
                   ...
                }
                TAG_String("id"): RPAdvBench
                TAG_Byte("rot"): 0
                TAG_Long("sched"): -1
                TAG_Int("z"): 386
                TAG_Int("y"): 249
                TAG_Byte("ps"): 0
                TAG_Int("x"): 1140
                */
                idTagString.data = "bau5pbTileEntity";

                Tag itemsTag = tileEntity.findChildByName("Items", false);
                if (itemsTag == null) {
                    System.out.println("- unrecognized Items tag: " + itemsTag);
                    continue;
                }

                itemsTag.setName("Inventory");

                /* example bau5 Project Bench:
                TAG_String("id"): bau5pbTileEntity
                TAG_List("Inventory"): 8 entries of type TAG_Compound
                {
                   TAG_Compound: 4 entries
                   {
                      TAG_Short("id"): 4
                      TAG_Short("Damage"): 0
                      TAG_Byte("Count"): 8
                      TAG_Byte("Slot"): 0
                   }
                TAG_Int("z"): 381
                TAG_Int("y"): 250
                TAG_Int("x"): 1146
                 */

                System.out.println("Converted to bau5pbTileEntity");

                IDChanger.changedPlaced++;
            }
        }
    }
}
