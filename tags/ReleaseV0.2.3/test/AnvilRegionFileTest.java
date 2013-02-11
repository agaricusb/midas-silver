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
		
		byte[] blockBytes = {(byte)2,(byte)4,(byte)8,(byte)10};
		ByteArrayTag blocksTag = new ByteArrayTag("Blocks", blockBytes);
		byte[] dataBytes= {(byte)1,(byte)10};
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
		translations.put(new BlockUID(10, null),new BlockUID(9, 124));
		translations.put(new BlockUID(8, 10),new BlockUID(7, 2));
		AnvilRegionFile.staticConvertRegion(null, levelTag, translations);
		assertEquals((byte)2,blocksTag.data[0]);
		assertEquals((byte)5,blocksTag.data[1]);
		assertEquals((byte)33,dataTag.data[0]);
		assertEquals((byte)7,blocksTag.data[2]);
		assertEquals((byte)10,blocksTag.data[3]);
		assertEquals((byte)2,dataTag.data[1]);
	}

	@Test
	public void testConvertItems() {
		ShortTag idShortTag1  = new ShortTag("id",(short)45);
		ShortTag damageShortTag1 = new ShortTag("Damage",(short)23);
		
		ShortTag idShortTag2  = new ShortTag("id",(short)45);
		ShortTag damageShortTag2 = new ShortTag("Damage",(short)33);
		
		ShortTag idShortTag3  = new ShortTag("id",(short)768);
		ShortTag damageShortTag3 = new ShortTag("Damage",(short)33);
		
		CompoundTag itemCompoundTag1 = new CompoundTag();
		CompoundTag itemCompoundTag2 = new CompoundTag();
		CompoundTag itemCompoundTag3 = new CompoundTag();
		
		itemCompoundTag1.put("id", idShortTag1);
		itemCompoundTag1.put("Damage",damageShortTag1);
		
		itemCompoundTag2.put("id", idShortTag2);
		itemCompoundTag2.put("Damage",damageShortTag2);
		
		itemCompoundTag3.put("id", idShortTag3);
		itemCompoundTag3.put("Damage",damageShortTag3);
		
		ListTag<CompoundTag> ItemsTag = new ListTag<CompoundTag>("Items");
		ItemsTag.add(itemCompoundTag1);
		ItemsTag.add(itemCompoundTag2);
		ItemsTag.add(itemCompoundTag3);
		
		ListTag root = new ListTag("");
		root.add(ItemsTag);
		
		HashMap<BlockUID, BlockUID> translations = new HashMap<BlockUID, BlockUID>();
		translations.put(new BlockUID(45, null),new BlockUID(5, 2));
		
		AnvilRegionFile.staticConvertItems(null, root, translations);
		
		assertEquals((short)5,idShortTag1.data);
		assertEquals((short)2,damageShortTag1.data);
		
		assertEquals((short)5,idShortTag2.data);
		assertEquals((short)2,damageShortTag2.data);
		
		assertEquals((short)768,idShortTag3.data);
		assertEquals((short)33,damageShortTag3.data);
	}

}
