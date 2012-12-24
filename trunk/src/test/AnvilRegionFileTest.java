package test;

import static org.junit.Assert.*;

import java.util.HashMap;

import havocx42.AnvilRegionFile;
import havocx42.BlockUID;

import org.junit.Test;

import com.mojang.nbt.*;

public class AnvilRegionFileTest {

	@Test
	public void testConvertRegion() {
		
		byte[] blockBytes = {(byte)2,(byte)4};
		ByteArrayTag blocksTag = new ByteArrayTag("Blocks", blockBytes);
		byte[] dataBytes= {(byte)1};
		ByteArrayTag dataTag = new ByteArrayTag("Data", dataBytes);
		
		CompoundTag sectionTag = new CompoundTag();
		sectionTag.put("Blocks", blocksTag);
		sectionTag.put("Data", dataTag);
		
		ListTag<CompoundTag> sectionsTag = new ListTag<CompoundTag>();
		sectionsTag.add(sectionTag);
		
		CompoundTag levelTag = new CompoundTag();
		levelTag.put("Sections", sectionsTag);

		HashMap<BlockUID, BlockUID> translations = new HashMap<BlockUID, BlockUID>();
		translations.put(new BlockUID(4, null),new BlockUID(5, 2));
		AnvilRegionFile.staticConvertRegion(null, levelTag, translations);
		assertEquals((byte)5,blocksTag.data[1]);
		assertEquals((byte)2,blocksTag.data[0]);
		assertEquals((byte)33,dataTag.data[0]);
	}

	@Test
	public void testConvertItems() {
		fail("Not yet implemented");
	}

}
