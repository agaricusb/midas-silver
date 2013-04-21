package plugins.convertplayerinventoriesplugin;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.ShortTag;
import com.mojang.nbt.Tag;

import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import havocx42.Status;
import pfaeff.IDChanger;

public class ConvertPlayerInventories implements ConverterPlugin {

	@Override
	public String getPluginName() {
		return "Convert Player Inventories";
	}

	@Override
	public PluginType getPluginType() {
		return PluginType.PLAYER;
	}

	@Override
	public void convert(Tag root, HashMap<BlockUID, BlockUID> translations) {
		ArrayList<Tag> inventoriesTag = new ArrayList<Tag>();
		root.findAllChildrenByName(inventoriesTag, "Inventory", true);
		for (Tag inventoryTag : inventoriesTag) {
			if (inventoryTag instanceof ListTag) {
				ListTag inventoryListTag = (ListTag) inventoryTag;
				for (int i = 0; i < inventoryListTag.size(); i++) {
					Tag itemTag = inventoryListTag.get(i);
					if (itemTag instanceof CompoundTag) {
						CompoundTag itemCompoundTag = (CompoundTag) itemTag;
						ShortTag idShortTag = (ShortTag) itemCompoundTag.findChildByName("id", false);
						ShortTag damageShortTag = (ShortTag) itemCompoundTag.findChildByName("Damage", false);
						BlockUID blockUID = new BlockUID(Integer.valueOf(idShortTag.data), Integer.valueOf(damageShortTag.data));
						if (translations.containsKey(blockUID)) {
							BlockUID toval = translations.get(blockUID);
							if (toval != null) {
								IDChanger.changedPlayer++;
								idShortTag.data = toval.blockID.shortValue();
								if (toval.dataValue != null) {
									damageShortTag.data = toval.dataValue.shortValue();
								}
							} else {
								//ErrorHandler.logError("null target for" + toval.blockID);
							}
						}
					}
				}
			}
		}
		return;
	}

}
