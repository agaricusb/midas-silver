package havocx42;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.mojang.nbt.*;

import pfaeff.IDChanger;

public class AnvilRegionFile extends RegionFileExtended {

	public AnvilRegionFile(File path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void convertItems(IDChanger UI, Tag root, HashMap<BlockUID, BlockUID> translations) {
		staticConvertItems(UI, root, translations);
	}

	public void staticConvertItems(IDChanger UI, Tag root, HashMap<BlockUID, BlockUID> translations) {
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
						if (idTag != null && damageTag != null && idTag instanceof ShortTag
								&& damageTag instanceof ShortTag) {
							ShortTag idShortTag = (ShortTag) idTag;
							ShortTag damageShortTag = (ShortTag) damageTag;
							BlockUID currentBlock = new BlockUID(Integer.valueOf(idShortTag.data),
									Integer.valueOf(damageShortTag.data));
							if (translations.containsKey(currentBlock)) {
								BlockUID targetBlock = translations.get(currentBlock);
								if (UI != null) UI.changedChest++;
								idShortTag.data = targetBlock.blockID.shortValue();
								if (targetBlock.dataValue != null)
									damageShortTag.data = targetBlock.dataValue.shortValue();
							}
						} else {
							ErrorHandler.logError("Incorrect Tag type");
						}
					}
				}
				/*
				 * ArrayList<Tag> ids = new ArrayList<Tag>();
				 * item.findAllChildrenByName(ids, "id", true); ArrayList<Tag>
				 * damageValues = new ArrayList<Tag>();
				 * item.findAllChildrenByName(damageValues, "Damage", true);
				 * indexToBlockIDs = new HashMap<Integer, BlockUID>(); for (int
				 * i = 0; i < ids.size(); i++) { Tag id = ids.get(i); Tag
				 * damageValue = damageValues.get(i); if (id instanceof ShortTag
				 * && damageValue instanceof ShortTag) { ShortTag idShort =
				 * (ShortTag) id; ShortTag damageShort = (ShortTag) damageValue;
				 * BlockUID blockUID = new BlockUID(
				 * Integer.valueOf(idShort.data),
				 * Integer.valueOf(damageShort.data)); if
				 * (translations.containsKey(blockUID)) { BlockUID toBlockUID =
				 * translations.get(blockUID); if (toBlockUID != null) {
				 * if(UI!=null)UI.changedChest++;
				 * indexToBlockIDs.put(Integer.valueOf(i), toBlockUID); } else {
				 * System.err.println("null target"); ErrorHandler
				 * .logError("null Target while converting items"); } } } } //
				 * update nbt tree Set<Integer> set = indexToBlockIDs.keySet();
				 * for (Integer i : set) { ((ShortTag) ids.get(i)).data =
				 * indexToBlockIDs.get(i).blockID .shortValue(); if
				 * (indexToBlockIDs.get(i).dataValue != null) ((ShortTag)
				 * damageValues.get(i)).data = indexToBlockIDs
				 * .get(i).dataValue.shortValue();
				 */
			}
		}
	}

	@Override
	public void convertRegion(IDChanger UI, Tag root, final HashMap<BlockUID, BlockUID> translations) {
		staticConvertRegion(UI, root, translations);
	}

	public static void staticConvertRegion(IDChanger UI, Tag root,
			final HashMap<BlockUID, BlockUID> translations) {
		ArrayList<Tag> result = new ArrayList<Tag>();
		root.findAllChildrenByName(result, "Sections", true);
		CompoundTag sectionTag;
		for (Tag list : result) {
			if (list instanceof ListTag) {
				for (int sectionIndex = 0; sectionIndex < ((ListTag<?>) list).size(); sectionIndex++) {
					sectionTag = ((ListTag<CompoundTag>) list).get(sectionIndex);
					Section section = new Section(sectionTag);
					HashMap<Integer, BlockUID> indexToBlockIDs;
					indexToBlockIDs = new HashMap<Integer, BlockUID>();
					for (int i = 0; i < section.length(); i++) {
						BlockUID blockUID = section.getBlockUID(i);
						if (translations.containsKey(blockUID)) {
							if (UI != null) {
								UI.changedPlaced++;
							}
							if (translations.get(blockUID).dataValue < 16) {

								indexToBlockIDs.put(Integer.valueOf(i), translations.get(blockUID));
							}
						}
					}

					// write changes to nbt tree
					Set<Map.Entry<Integer, BlockUID>> set = indexToBlockIDs.entrySet();
					for (Entry<Integer, BlockUID> entry : set) {
						section.setBlockUID(entry.getKey(), entry.getValue());
					}
					for (Entry<Integer, BlockUID> entry : set) {
						if (!section.getBlockUID(entry.getKey()).equals(entry.getValue())) {
							ErrorHandler.logError(entry.getKey() + " not converted to "
									+ entry.getValue());
						}
					}

				}
			}
		}
	}
}