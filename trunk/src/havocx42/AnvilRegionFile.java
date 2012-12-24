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
	public void convertItems(IDChanger UI, Tag root,
			HashMap<BlockUID, BlockUID> translations) {
		staticConvertItems(UI, root, translations);
	}

	public void staticConvertItems(IDChanger UI, Tag root,
			HashMap<BlockUID, BlockUID> translations) {
		HashMap<Integer, BlockUID> indexToBlockIDs;
		ArrayList<Tag> items = new ArrayList<Tag>();
		root.findAllChildrenByName(items, "Items", true);
		for (Tag t2 : items) {
			if (t2 instanceof ListTag) {
				ArrayList<Tag> ids = new ArrayList<Tag>();
				t2.findAllChildrenByName(ids, "id", true);
				ArrayList<Tag> damageValues = new ArrayList<Tag>();
				t2.findAllChildrenByName(damageValues, "Damage", true);
				indexToBlockIDs = new HashMap<Integer, BlockUID>();
				for (int i = 0; i < ids.size(); i++) {
					Tag id = ids.get(i);
					Tag damageValue = damageValues.get(i);
					if (id instanceof ShortTag
							&& damageValue instanceof ShortTag) {
						ShortTag idShort = (ShortTag) id;
						ShortTag damageShort = (ShortTag) damageValue;
						BlockUID blockUID = new BlockUID(
								Integer.valueOf(idShort.data),
								Integer.valueOf(damageShort.data));
						if (translations.containsKey(blockUID)) {
							BlockUID toBlockUID = translations.get(blockUID);
							if (toBlockUID != null) {
								UI.changedChest++;
								indexToBlockIDs.put(Integer.valueOf(i),
										toBlockUID);
							} else {
								System.err.println("null target");
								ErrorHandler
										.logError("null Target while converting items");
							}
						}
					}
				}
				// update nbt tree
				Set<Integer> set = indexToBlockIDs.keySet();
				for (Integer i : set) {
					((ShortTag) ids.get(i)).data = indexToBlockIDs.get(i).blockID
							.shortValue();
					if (indexToBlockIDs.get(i).dataValue != null)
						((ShortTag) damageValues.get(i)).data = indexToBlockIDs
								.get(i).dataValue.shortValue();
				}
			}
		}
	}

	@Override
	public void convertRegion(IDChanger UI, Tag root,
			final HashMap<BlockUID, BlockUID> translations) {
		staticConvertRegion(UI, root, translations);
	}

	public static void staticConvertRegion(IDChanger UI, Tag root,
			final HashMap<BlockUID, BlockUID> translations) {
		ArrayList<Tag> result = new ArrayList<Tag>();
		root.findAllChildrenByName(result, "Sections", true);
		CompoundTag sectionTag;
		for (Tag list : result) {
			if (list instanceof ListTag) {
				for (int sectionIndex = 0; sectionIndex < ((ListTag<?>) list)
						.size(); sectionIndex++) {
					sectionTag = ((ListTag<CompoundTag>) list)
							.get(sectionIndex);
					Section section = new Section(sectionTag);
					HashMap<Integer, BlockUID> indexToBlockIDs;
					indexToBlockIDs = new HashMap<Integer, BlockUID>();
					for (int i = 0; i < section.length(); i++) {
						BlockUID blockUID = section.getBlockUID(i);
						if (translations.containsKey(blockUID)) {
							if (UI != null) {
								UI.changedPlaced++;
							}
							indexToBlockIDs.put(Integer.valueOf(i),
									translations.get(blockUID));
						}
					}

					// write changes to nbt tree
					Set<Map.Entry<Integer, BlockUID>> set = indexToBlockIDs
							.entrySet();
					for (Entry<Integer, BlockUID> entry : set) {
						section.setBlockUID(entry.getKey(), entry.getValue());
					}
					for (Entry<Integer, BlockUID> entry : set) {
						if (!section.getBlockUID(entry.getKey()).equals(
								entry.getValue())) {
							ErrorHandler.logError(entry.getKey()
									+ " not converted to " + entry.getValue());
						}
					}

				}
			}
		}
	}
}