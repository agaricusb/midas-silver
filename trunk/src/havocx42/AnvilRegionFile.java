package havocx42;

import java.io.File;
import java.util.HashMap;

import pfaeff.IDChanger;

import nbt.Tag;

import region.RegionFile;

public class AnvilRegionFile extends RegionFileExtended  {

	public AnvilRegionFile(File path) {
		super(path);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void convertRegion(IDChanger UI,Tag root, HashMap<Integer, Integer> translations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void convertItems(IDChanger UI,Tag root, HashMap<Integer, Integer> translations) {
		// TODO Auto-generated method stub
		
	}

}
