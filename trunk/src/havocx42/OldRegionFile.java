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
		staticConvertItems(UI, root, translations);
	}

	public void staticConvertItems(IDChanger UI, Tag root,
			HashMap<BlockUID, BlockUID> translations) {
		HashMap<Integer, BlockUID> indexToBlockIDs;
		ArrayList<Tag> itemsTags = new ArrayList<Tag>();
		root.findAllChildrenByName(itemsTags, "Items", true);
		for (Tag itemsTag : itemsTags) {
			if (itemsTag instanceof ListTag) {
				ListTag itemsListTag = (ListTag) itemsTag;
				for(int itemsIndex = 0; itemsIndex < itemsListTag.size(); itemsIndex++){
					if(itemsListTag.get(itemsIndex) instanceof CompoundTag){
						CompoundTag itemCompoundTag = (CompoundTag) itemsListTag.get(itemsIndex);
						Tag idTag = itemCompoundTag.findChildByName("id", false);
						Tag damageTag = itemCompoundTag.findChildByName("Damage", false);
						if(idTag!=null&&damageTag!=null&&idTag instanceof ShortTag&&damageTag instanceof ShortTag){
							ShortTag idShortTag = (ShortTag) idTag;
							ShortTag damageShortTag = (ShortTag) damageTag;
							BlockUID currentBlock = new BlockUID(Integer.valueOf(idShortTag.data),Integer.valueOf(damageShortTag.data));
							if(translations.containsKey(currentBlock)){
								BlockUID targetBlock = translations.get(currentBlock);
								if(UI!=null)UI.changedChest++;
								idShortTag.data=targetBlock.blockID.shortValue();
								if(targetBlock.dataValue!=null)damageShortTag.data=targetBlock.dataValue.shortValue();
							}
						}else{
							ErrorHandler.logError("Incorrect Tag type");
						}
					}
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
