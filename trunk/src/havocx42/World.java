package havocx42;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;

import pfaeff.IDChanger;

public class World {
	private File baseFolder;
	private ArrayList<RegionFileExtended> regionFiles;
	public World(File path) {
		baseFolder = path;
		try {
			regionFiles = getRegionFiles();
			getDatFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void convert(IDChanger UI, HashMap<Integer, Integer> translations) {
		UI.changedChest = 0;
		UI.changedPlaced = 0;
		UI.changedPlayer = 0;

		long beginTime = System.currentTimeMillis();

		// player inventories
		//convertPlayerInventories(UI, translations);
		// PROGESSBAR FILE
		int count_file = 0;
		if (regionFiles == null) {
			// No valid region files found
			return;
		}
		UI.pb_file.setMaximum(regionFiles.size() - 1);

		for (RegionFileExtended r : regionFiles) {
			UI.pb_file.setValue(count_file++);
			UI.lb_file.setText("Current File: " + r.fileName);
			try {
				r.convert(UI, translations);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long duration = System.currentTimeMillis() - beginTime;
		JOptionPane.showMessageDialog(
				UI,
				"Done in " + duration + "ms"
						+ System.getProperty("line.separator") + UI.changedPlaced
						+ " placed blocks changed."
						+ System.getProperty("line.separator") + UI.changedPlayer
						+ " blocks in player inventories changed."
						+ System.getProperty("line.separator") + UI.changedChest
						+ " blocks in entity inventories changed.",
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

}
