package havocx42;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import pfaeff.IDChanger;

import nbt.Tag;
import nbt.TagByteArray;
import nbt.TagList;
import nbt.TagShort;

import region.RegionFile;

public class OldRegionFile extends RegionFileExtended {

	public OldRegionFile(File path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void convertItems(IDChanger UI,Tag root, HashMap<Integer, Integer> translations) {
		HashMap<Integer, Integer> indexToBlockIDs;
		ArrayList<Tag> items = new ArrayList<Tag>();
		root.findAllChildrenByName(items, "Items", true);
		for (Tag t2 : items) {
			if (t2 instanceof TagList) {
				ArrayList<Tag> ids = new ArrayList<Tag>();
				t2.findAllChildrenByName(ids, "id", true);
				indexToBlockIDs = new HashMap<Integer, Integer>();
				for (int i = 0; i < ids.size(); i++) {
					Tag id = ids.get(i);
					if (id instanceof TagShort) {
						TagShort idShort = (TagShort) id;
						if (translations.containsKey(Integer
								.valueOf(idShort.payload))) {
							Integer toval = translations.get(Integer
									.valueOf(idShort.payload));
							if (toval != null) {
								UI.changedChest++;
								indexToBlockIDs.put(Integer.valueOf(i), toval);
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
					((TagShort) ids.get(i)).payload = indexToBlockIDs.get(i)
							.shortValue();
				}
			}
		}
	}

	@Override
	protected void convertRegion(IDChanger UI,Tag root,
			final HashMap<Integer, Integer> translations) {
		Tag t = root.findChildByName("Blocks", true);
		if (t instanceof TagByteArray) {
			TagByteArray blocks = (TagByteArray) t;
			HashMap<Integer, Integer> indexToBlockIDs;
			indexToBlockIDs = new HashMap<Integer, Integer>();
			for (int i = 0; i < blocks.payload.length; i++) {
				Integer bID = Integer
						.valueOf(0x000000FF & (int) (blocks.payload[i]));
				if (translations.containsKey(bID)) {
					// Only allow blocks to be replaced with
					// blocks
					if (translations.get(bID) <= 255) {
						UI.changedPlaced++;
						indexToBlockIDs.put(Integer.valueOf(i),
								translations.get(bID));
					}
				}
			}

			// write changes to nbt tree
			Set<Map.Entry<Integer, Integer>> set = indexToBlockIDs.entrySet();
			for (Entry<Integer, Integer> entry : set) {
				blocks.payload[entry.getKey()] = entry.getValue().byteValue();
			}
			for (Entry<Integer, Integer> entry : set) {
				if ((byte) blocks.payload[entry.getKey()] != (byte) entry
						.getValue().byteValue()) {
					ErrorHandler.logError(entry.getKey() + " not converted to "
							+ entry.getValue());
				}
			}
			// System.out.print("test");
		}
	}

}
