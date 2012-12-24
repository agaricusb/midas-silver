package havocx42;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.mojang.nbt.*;

import pfaeff.IDChanger;

public class OldRegionFile extends RegionFileExtended {

	public OldRegionFile(File path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void convertItems(IDChanger UI, Tag root,
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
					if (id instanceof ShortTag&&damageValue instanceof ShortTag) {
						ShortTag idShort = (ShortTag) id;
						ShortTag damageShort = (ShortTag) damageValue;
						BlockUID blockUID = new BlockUID(Integer.valueOf(idShort.data),Integer.valueOf(damageShort.data));
						if (translations.containsKey(blockUID)) {
							BlockUID toBlockUID = translations.get(blockUID);
							if (toBlockUID != null) {
								UI.changedChest++;
								indexToBlockIDs.put(Integer.valueOf(i), toBlockUID);
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
					((ShortTag) ids.get(i)).data = indexToBlockIDs.get(i).blockID.shortValue();
					if(indexToBlockIDs.get(i).dataValue!=null)((ShortTag) damageValues.get(i)).data = indexToBlockIDs.get(i).dataValue.shortValue();
				}
			}
		}
	}

	@Override
	public void convertRegion(IDChanger UI,Tag root,
			final HashMap<BlockUID, BlockUID> translations) {
		Tag blockTag = root.findChildByName("Blocks", true);
		Tag dataTag = root.findChildByName("Data", true);
		if (blockTag instanceof ByteArrayTag&&dataTag instanceof ByteArrayTag) {
			ByteArrayTag blockIDs = (ByteArrayTag) blockTag;
			ByteArrayTag dataValues = (ByteArrayTag) dataTag;
			HashMap<Integer, BlockUID> indexToBlockIDs;
			indexToBlockIDs = new HashMap<Integer, BlockUID>();
			for (int i = 0; i < blockIDs.data.length; i++){
				Integer bID = Integer.valueOf(0x000000FF & (int) (blockIDs.data[i]));
				int j = (i % 2 == 0) ? i / 2 : (i - 1) / 2;
				Integer dataValue = (i % 2 == 0) ? 0x0000000F & (int) dataValues.data[j]: ((0x000000F0 & (int) dataValues.data[j]) >> 4);
				BlockUID blockUID = new BlockUID(bID,dataValue);
				if (translations.containsKey(blockUID)) {
					// Only allow blocks to be replaced with
					// blocks
					UI.changedPlaced++;
					indexToBlockIDs.put(Integer.valueOf(i),
								translations.get(blockUID));
				}
			}

			// write changes to nbt tree
			Set<Map.Entry<Integer, BlockUID>> set = indexToBlockIDs.entrySet();
			for (Entry<Integer, BlockUID> entry : set) {
				blockIDs.data[entry.getKey()] = entry.getValue().blockID.byteValue();
				if(entry.getValue().dataValue!=null){
					int j = (entry.getKey() % 2 == 0) ? entry.getKey() / 2 : (entry.getKey() - 1) / 2;
					int dataByte = (int) dataValues.data[j];
					
					int newDataByte = (entry.getKey() % 2 == 0) ? ((0x0000000F & entry.getValue().dataValue)) | (dataByte & 0x000000F0) : ((0x0000000F & entry.getValue().dataValue) << 4) | (dataByte & 0x0000000F);
					dataValues.data[j] = (byte) newDataByte;	
				}
			}
			for (Entry<Integer, BlockUID> entry : set) {
				if ((byte) blockIDs.data[entry.getKey()] != (byte) entry
						.getValue().blockID.byteValue()) {
					ErrorHandler.logError(entry.getKey() + " not converted to "
							+ entry.getValue());
				}
			}
			// System.out.print("test");
		}
	}

}
