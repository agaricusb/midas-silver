package plugins.convertblocksplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;

import havocx42.BlockUID;
import havocx42.ConverterPlugin;
import havocx42.PluginType;
import havocx42.Section;
import havocx42.Status;
import pfaeff.IDChanger;

public class ConvertBlocks implements ConverterPlugin {

	private int warnUnconvertedAfter;
	private boolean countBlockStats;

	public ConvertBlocks(Integer warnUnconvertedAfter, boolean countBlockStats) {
		if (warnUnconvertedAfter == null) {
			this.warnUnconvertedAfter = -1;
		} else {
			this.warnUnconvertedAfter = warnUnconvertedAfter; 	
		}
		this.countBlockStats = countBlockStats;
	}

	@Override
	public String getPluginName() {
		return "Convert Blocks";
	}

	@Override
	public void convert(Tag root, final HashMap<BlockUID, BlockUID> translations) {
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
								IDChanger.changedPlaced++;

							if (translations.get(blockUID).dataValue == null || translations.get(blockUID).dataValue < 16) {

								indexToBlockIDs.put(Integer.valueOf(i), translations.get(blockUID));
							}

							if (countBlockStats) {
								Integer count = IDChanger.convertedBlockCount.get(blockUID);
								if (count == null) {
									IDChanger.convertedBlockCount.put(blockUID, 1);
								} else {
									IDChanger.convertedBlockCount.put(blockUID, count + 1);
								}
							}
						} else {
							if (warnUnconvertedAfter != -1 && blockUID.blockID > warnUnconvertedAfter) {
								System.out.println("untranslated block:" + blockUID);
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
							//ErrorHandler.logError(entry.getKey() + " not converted to " + entry.getValue());
						}
					}

				}
			}
		}
	}

	@Override
	public PluginType getPluginType() {
		return PluginType.REGION;
	}

}
