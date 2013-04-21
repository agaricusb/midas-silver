package plugins.convertitemsplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.ShortTag;
import com.mojang.nbt.Tag;

import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import havocx42.Section;
import havocx42.Status;
import pfaeff.IDChanger;

public class ConvertItems implements ConverterPlugin {

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return "Convert Items";
	}

	@Override
	public void convert(Tag root, HashMap<BlockUID, BlockUID> translations) {
		HashMap<Integer, BlockUID> indexToBlockIDs;
		ArrayList<Tag> itemsTags = new ArrayList<Tag>();
		root.findAllChildrenByName(itemsTags, "Items", true);
		for (Tag itemsTag : itemsTags) {
			if (itemsTag instanceof ListTag) {
				ListTag itemsListTag = (ListTag) itemsTag;
				for (int itemsIndex = 0; itemsIndex < itemsListTag.size(); itemsIndex++) {
					if (itemsListTag.get(itemsIndex) instanceof CompoundTag) {
						CompoundTag itemCompoundTag = (CompoundTag) itemsListTag.get(itemsIndex);
						Tag idTag = itemCompoundTag.findChildByName("id", false);
						Tag damageTag = itemCompoundTag.findChildByName("Damage", false);
						if (idTag != null && damageTag != null && idTag instanceof ShortTag && damageTag instanceof ShortTag) {
							ShortTag idShortTag = (ShortTag) idTag;
							ShortTag damageShortTag = (ShortTag) damageTag;
							BlockUID currentBlock = new BlockUID(Integer.valueOf(idShortTag.data), Integer.valueOf(damageShortTag.data));
							if (translations.containsKey(currentBlock)) {
								BlockUID targetBlock = translations.get(currentBlock);
								IDChanger.changedChest++;
								idShortTag.data = targetBlock.blockID.shortValue();
								if (targetBlock.dataValue != null)
									damageShortTag.data = targetBlock.dataValue.shortValue();
							}
						} 
					}
				}
			}				
		}
	}

	@Override
	public PluginType getPluginType() {
		// TODO Auto-generated method stub
		return PluginType.REGION;
	}

}
