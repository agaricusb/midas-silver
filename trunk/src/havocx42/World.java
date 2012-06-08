package havocx42;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import pfaeff.IDChanger;

import nbt.Tag;
import nbt.TagInputStream;
import nbt.TagList;
import nbt.TagOutputStream;
import nbt.TagShort;

import region.RegionFile;

public class World {
	private File baseFolder;
	private ArrayList<RegionFileExtended> regionFiles;
	private ArrayList<PlayerFile> datFiles;

	public World(File path) {
		baseFolder = path;
		try {
			regionFiles = getRegionFiles();
			datFiles = getDatFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void convert(IDChanger UI, HashMap<Integer, Integer> translations)
			throws IOException {
		UI.changedChest = 0;
		UI.changedPlaced = 0;
		UI.changedPlayer = 0;

		long beginTime = System.currentTimeMillis();

		// player inventories
		convertPlayerInventories(UI, translations);
		// PROGESSBAR FILE
		int count_file = 0;
		if (regionFiles == null) {
			// No valid region files found
			return;
		}
		UI.pb_file.setMaximum(regionFiles.size() - 1);

		for (RegionFileExtended r : regionFiles) {
			UI.pb_file.setValue(count_file++);
			UI.lb_file.setText("Current File: " + r.getFile().getName());
			r.convert(UI, translations);
		}
		
		JOptionPane.showMessageDialog(
				UI,
				"Done in " + /* duration + */"ms"
						+ System.getProperty("line.separator")
						+ /* changedPlaced + */" placed blocks changed."
						+ System.getProperty("line.separator")
						+ /*
						 * changedPlayer +
						 */" blocks in player inventories changed."
						+ System.getProperty("line.separator")
						+ /*
						 * changedChest +
						 */" blocks in entity inventories changed.",
				"Information", JOptionPane.INFORMATION_MESSAGE);

	}

	private ArrayList<RegionFileExtended> getRegionFiles() throws IOException {
		// Switch to the "region" folder
		File regionDir = new File(baseFolder, "region");
		if (!regionDir.exists()) {
			regionDir = new File(baseFolder, "DIM1/region");
		}
		if (!regionDir.exists()) {
			regionDir = new File(baseFolder, "DIM-1/region");
		}

		FileFilter mcaFiles = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith("mca")) {
					return true;
				}
				return false;
			}
		};

		// Create a filter to only include mcr-files
		FileFilter mcrFiles = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith("mcr")) {
					return true;
				}
				return false;
			}
		};
		// Find all region files
		boolean anvil = true;
		File[] files = regionDir.listFiles(mcaFiles);
		if (files == null || files.length == 0) {
			anvil = false;
			files = regionDir.listFiles(mcrFiles);
			if (files == null) {
				return null;
			}
		}
		ArrayList<RegionFileExtended> result = new ArrayList<RegionFileExtended>();
		if (anvil) {
			for (int i = 0; i < files.length; i++) {
				result.add(new AnvilRegionFile(files[i]));
			}
		} else {
			for (int i = 0; i < files.length; i++) {
				result.add(new OldRegionFile(files[i]));
			}
		}
		return result;
	}

	private ArrayList<PlayerFile> getDatFiles() throws IOException {
		// Switch to the "region" folder
		File playersDir = new File(baseFolder, "players");
		File levelDat = new File(baseFolder, "level.dat");
		// Create a filter to only include dat-files
		FileFilter datFiles = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith("dat")) {
					return true;
				}
				return false;
			}
		};
		ArrayList<PlayerFile> result = new ArrayList<PlayerFile>();
		// Find all dat files
		if (playersDir.exists()) {
			File[] files = playersDir.listFiles(datFiles);
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					result.add(new PlayerFile(files[i], files[i].getName(),
							"rw"));
				}
			}
		}
		if (levelDat.exists()) {
			result.add(new PlayerFile(levelDat, "level.dat", "rw"));
		}

		return result;
	}

	private void convertPlayerInventories(IDChanger UI,
			HashMap<Integer, Integer> translations) {
		try {

			UI.pb_file.setMaximum(datFiles.size() - 1);
			int count_file = 0;

			for (PlayerFile df : datFiles) {

				UI.pb_file.setValue(count_file++);
				UI.lb_file.setText("Current File: " + df.getName());
				DataInputStream dfinput = getLevelDataInputStream(df);
				TagInputStream tis = new TagInputStream(dfinput);
				Tag dfroot;

				dfroot = tis.readTag(true);

				ArrayList<Tag> items = new ArrayList<Tag>();
				dfroot.findAllChildrenByName(items, "Inventory", true);
				HashMap<Integer, Integer> indexToBlockIDs;
				for (Tag t2 : items) {
					indexToBlockIDs = new HashMap<Integer, Integer>();
					if (t2 instanceof TagList) {
						ArrayList<Tag> ids = new ArrayList<Tag>();
						t2.findAllChildrenByName(ids, "id", true);
						for (int i = 0; i < ids.size(); i++) {
							Tag id = ids.get(i);
							if (id instanceof TagShort) {
								TagShort idShort = (TagShort) id;
								if (translations.containsKey(Integer
										.valueOf(idShort.payload))) {
									Integer toval = translations.get(Integer
											.valueOf(idShort.payload));
									if (toval != null) {
										UI.changedPlayer++;
										indexToBlockIDs.put(Integer.valueOf(i),
												toval);
									} else {
										ErrorHandler.logError("null target for"
												+ idShort.payload);
									}
								}
							}
						}
						// update nbt tree
						Set<Integer> set = indexToBlockIDs.keySet();
						for (Integer i : set) {
							((TagShort) ids.get(i)).payload = indexToBlockIDs
									.get(i).shortValue();
						}
					}
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dfoutput = new DataOutputStream(
						new GZIPOutputStream(bos));
				(new TagOutputStream(dfoutput)).writeTag(dfroot, true);
				dfoutput.close();
				df.seek(0);
				df.write(bos.toByteArray());
				df.close();
				dfinput.close();
				tis.close();
			}
		} catch (IOException e) {
			ErrorHandler.logError(e);
		}

	}

	private DataInputStream getLevelDataInputStream(RandomAccessFile f) {
		try {
			// might crash if file is HUGE
			byte[] data = new byte[(int) (f.length() - 1)];
			f.read(data);
			DataInputStream ret = new DataInputStream(new GZIPInputStream(
					new ByteArrayInputStream(data)));
			// debug("READ", x, z, " = found");
			return ret;

		} catch (IOException e) {
			// debugln("READ", x, z, "exception");
			ErrorHandler.logError(e);
			return null;
		}
	}

}
