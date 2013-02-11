package havocx42;

import java.util.ArrayList;
import java.util.HashMap;

import pfaeff.IDChanger;

import com.mojang.nbt.*;

public class PlayerInventoryConverter {

	public static Tag convertPlayerInventory(IDChanger UI, Tag root,HashMap<BlockUID, BlockUID> translations) {
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
						BlockUID blockUID = new BlockUID(Integer.valueOf(idShortTag.data),
								Integer.valueOf(damageShortTag.data));
						if (translations.containsKey(blockUID)) {
							BlockUID toval = translations.get(blockUID);
							if (toval != null) {
								UI.changedPlayer++;
								idShortTag.data=toval.blockID.shortValue();
								if (toval.dataValue != null) {
									damageShortTag.data=toval.dataValue.shortValue();
								}
							} else {
								ErrorHandler.logError("null target for" + toval.blockID);
							}
						}
					}
				}
			}
		}
		return root;
	}
}
