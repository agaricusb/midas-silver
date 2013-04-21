package havocx42;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import com.mojang.nbt.*;

import pfaeff.IDChanger;

public class World {
    private File                            baseFolder;
    private ArrayList<RegionFileExtended>    regionFiles;
    private ArrayList<PlayerFile>            playerFiles;
    private Logger                            logger    = Logger.getLogger(this.getClass().getName());

    public World(File path) throws IOException {
        baseFolder = path;
        regionFiles = getRegionFiles();
        playerFiles = getPlayerFiles();
    }

    public void convert(IDChanger UI, HashMap<BlockUID, BlockUID> translations) {
        Status status = UI.status;
        status.changedChest = 0;
        status.changedPlaced = 0;
        status.changedPlayer = 0;
        int count_file = 0;
        long beginTime = System.currentTimeMillis();

        // player inventories
        status.pb_file.setMaximum(playerFiles.size() - 1);
        // load plugins
        PluginLoader pl;
        try {
            pl = new PluginLoader();
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Unable to load plugins: " + e.getMessage(), e);
            return;
        }
        pl.loadPlugins();
        ArrayList<ConverterPlugin> regionPlugins = pl.getPluginsOfType(PluginType.REGION);
        ArrayList<ConverterPlugin> playerPlugins = pl.getPluginsOfType(PluginType.PLAYER);

        for (PlayerFile playerFile : playerFiles) {
            status.pb_file.setValue(count_file++);
            status.lb_file.setText("Current File: " + playerFile.getName());
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(playerFile))));

                CompoundTag root = NbtIo.read(dis);
                for (ConverterPlugin plugin : playerPlugins) {
                    plugin.convert(status, root, translations);
                }
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(playerFile)));
                NbtIo.writeCompressed(root, dos);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to convert player inventories", e);
                return;
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Unable to close input stream", e);
                    }
                }
            }

        }
        // PROGESSBAR FILE
        count_file = 0;
        if (regionFiles == null) {
            // No valid region files found
            return;
        }
        status.pb_file.setValue(0);
        status.pb_file.setMaximum(regionFiles.size() - 1);

        for (RegionFileExtended r : regionFiles) {
            status.lb_file.setText("Current File: " + r.fileName.getName());
            status.pb_file.setMaximum(regionFiles.size() - 1);
            status.pb_file.setValue(count_file++);
            

            try {
                r.convert(status, translations, regionPlugins);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to convert placed blocks", e);
                return;
            }
        }
        long duration = System.currentTimeMillis() - beginTime;
        JOptionPane.showMessageDialog(UI, "Done in " + duration + "ms" + System.getProperty("line.separator") + status.changedPlaced
                + " placed blocks changed." + System.getProperty("line.separator") + status.changedPlayer
                + " blocks in player inventories changed." + System.getProperty("line.separator") + status.changedChest
                + " blocks in entity inventories changed.", "Information", JOptionPane.INFORMATION_MESSAGE);
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
        File[] files = regionDir.listFiles(mcaFiles);
        ArrayList<RegionFileExtended> result = new ArrayList<RegionFileExtended>();
        if(files == null ){
            throw new RuntimeException("No region files found");
        }

        for (int i = 0; i < files.length; i++) {
            result.add(new RegionFileExtended(files[i]));
        }
        return result;
    }

    private ArrayList<PlayerFile> getPlayerFiles() throws IOException {
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
                    result.add(new PlayerFile(files[i].getAbsolutePath(), files[i].getName()));
                }
            }
        }
        if (levelDat.exists()) {
            result.add(new PlayerFile(levelDat.getAbsolutePath(), "level.dat"));
        }

        return result;
    }
}
