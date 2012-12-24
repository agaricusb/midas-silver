package havocx42;

import java.util.ArrayList;
import java.util.HashMap;

import pfaeff.IDChanger;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;

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
						short id = itemCompoundTag.getShort("id");
						short damage = itemCompoundTag.getShort("Damage");
						BlockUID blockUID = new BlockUID(Integer.valueOf(id),
								Integer.valueOf(damage));
						if (translations.containsKey(blockUID)) {
							BlockUID toval = translations.get(blockUID);
							if (toval != null) {
								UI.changedPlayer++;
								itemCompoundTag.putShort("id",
										toval.blockID.shortValue());
								if (toval.dataValue != null) {
									itemCompoundTag.putShort("Damage",
											toval.dataValue.shortValue());
								}
							} else {
								ErrorHandler.logError("null target for" + id);
							}
						}
					}
				}
			}
		}
		return root;
	}

}
