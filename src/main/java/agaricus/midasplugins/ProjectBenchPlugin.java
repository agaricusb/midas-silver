package agaricus.midasplugins;

import com.mojang.nbt.Tag;
import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;

import java.util.HashMap;

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
    public void convert(Tag tag, HashMap<BlockUID, BlockUID> translations) {
        System.out.println("PB TAG:"+tag);
        tag.print(System.out);
    }
}
