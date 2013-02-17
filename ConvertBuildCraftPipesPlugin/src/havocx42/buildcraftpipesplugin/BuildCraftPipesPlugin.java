package havocx42.buildcraftpipesplugin;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.nbt.IntTag;
import com.mojang.nbt.Tag;

import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import havocx42.Status;

public class BuildCraftPipesPlugin implements ConverterPlugin {

	@Override
	public void convert(Status status, Tag tag, HashMap<BlockUID, BlockUID> translations) {
		ArrayList<Tag> result = new ArrayList<Tag>();
		tag.findAllChildrenByName(result , "pipeId", true);
		for(Tag pipeidTag : result){
			if(pipeidTag instanceof IntTag){
				IntTag pipeidShortTag = (IntTag)pipeidTag;
				BlockUID block = new BlockUID((int) pipeidShortTag.data,null);
				if(translations.containsKey(block)){
					pipeidShortTag.data=translations.get(block).blockID.shortValue();
					status.changedPlaced=status.changedPlaced+1;
				}
			}
			
		}

	}

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return "Build Craft Pipes Plugin";
	}

	@Override
	public PluginType getPluginType() {
		// TODO Auto-generated method stub
		return PluginType.REGION;
	}

}
