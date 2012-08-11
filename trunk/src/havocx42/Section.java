package havocx42;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.CompoundTag;

public class Section {
	public CompoundTag sectionTag;
	public ByteArrayTag addTag;
	public ByteArrayTag blocksTag;
	
	public Section(CompoundTag sectionTag) {
		super();
		this.sectionTag=sectionTag;
		blocksTag = (ByteArrayTag) sectionTag.findChildByName("Blocks", false);
		addTag = (ByteArrayTag) sectionTag.findChildByName("Add",false);
	}

	public int length() {
		return blocksTag.data.length;
	}

	public Integer get(int i) {
		Integer bID = Integer.valueOf(0x000000FF & (int) (blocksTag.data[i]));
		if (addTag != null) {
			int j = (i % 2 == 0) ? i / 2 : (i - 1) / 2;
			Integer aID = (i % 2 == 0) ? 0x0000000F & (int) addTag.data[j]
					: ((0x000000F0 & (int) addTag.data[j]) >> 4);
			bID += (aID << 8);
		}
		return bID;
	}

	public void set(int i, int value) {
		int bID = value & 0x000000FF;
		boolean needed=value>255;
		
		if(addTag==null&&needed){
			byte[] bytes = new byte[2048];
			addTag=new ByteArrayTag("Add",bytes);
			sectionTag.put("Add", addTag);
		}
		if(addTag!=null||needed){
			int j = (i % 2 == 0) ? i / 2 : (i - 1) / 2;
			int addByte = (int) addTag.data[j];
			int newAddByte = (i % 2 == 0) ? ((0x00000F00 & value) >> 8)| (addByte & 0x000000F0) : ((0x00000F00 & value) >> 4)| (addByte & 0x0000000F);
			addTag.data[j] = (byte) newAddByte;
		}
		blocksTag.data[i] = (byte) bID;
	}
}
