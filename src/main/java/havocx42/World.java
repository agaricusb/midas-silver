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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import agaricus.midasplugins.ChargingBenchGregTechPlugin;
import agaricus.midasplugins.DumpTileEntitiesPlugin;
import agaricus.midasplugins.ProjectBenchPlugin;
import com.mojang.nbt.*;

import havocx42.buildcraftpipesplugin.BuildCraftPipesPlugin;
import joptsimple.OptionSet;
import pfaeff.IDChanger;
import plugins.convertblocksplugin.ConvertBlocks;
import plugins.convertitemsplugin.ConvertItems;
import plugins.convertplayerinventoriesplugin.ConvertPlayerInventories;

public class World {
    private File                            baseFolder;
    private ArrayList<RegionFileExtended>    regionFiles;
    private ArrayList<PlayerFile>            playerFiles;
    private Logger                            logger    = Logger.getLogger(this.getClass().getName());
    private boolean countBlockStats;

    public World(File path) throws IOException {
        baseFolder = path;
        regionFiles = getRegionFiles();
        playerFiles = getPlayerFiles();
    }

    public void convert(HashMap<BlockUID, BlockUID> translations, OptionSet options) {
        int count_file = 0;
        long beginTime = System.currentTimeMillis();
        this.countBlockStats = options.has("count-block-stats");

        // player inventories
        logger.log(Level.INFO, "Player inventories: "+ playerFiles.size());

        // load integrated plugins
        ArrayList<ConverterPlugin> regionPlugins = new ArrayList<ConverterPlugin>();
        if (!options.has("no-convert-blocks")) regionPlugins.add(new ConvertBlocks(
                ((Integer)options.valueOf("warn-unconverted-block-id-after")),
                options.has("count-block-stats")));
        if (!options.has("no-convert-items")) regionPlugins.add(new ConvertItems());
        if (!options.has("no-convert-buildcraft-pipes")) regionPlugins.add(new BuildCraftPipesPlugin());

        if (options.has("convert-project-table")) regionPlugins.add(new ProjectBenchPlugin());
        if (options.has("dump-tile-entities")) regionPlugins.add(new DumpTileEntitiesPlugin());
        if (options.has("convert-charging-bench-gregtech")) regionPlugins.add(new ChargingBenchGregTechPlugin());

        ArrayList<ConverterPlugin> playerPlugins = new ArrayList<ConverterPlugin>();
        if (!options.has("no-convert-player-inventories")) playerPlugins.add(new ConvertPlayerInventories());


        logger.log(Level.INFO, "Enabled "+regionPlugins.size()+" region plugins:");
        for (ConverterPlugin plugin : regionPlugins) {
            logger.log(Level.INFO, "- " + plugin.getPluginName());
        }

        logger.log(Level.INFO, "Enabled "+playerPlugins.size()+" player plugins:");
        for (ConverterPlugin plugin : playerPlugins) {
            logger.log(Level.INFO, "- " + plugin.getPluginName());
        }

        for (PlayerFile playerFile : playerFiles) {
            logger.log(Level.INFO, "Player inventory "+count_file+"/"+playerFiles.size()+": Current File: " + playerFile.getName());
            ++count_file;
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(playerFile))));

                CompoundTag root = NbtIo.read(dis);
                for (ConverterPlugin plugin : playerPlugins) {
                    plugin.convert(root, translations);
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
        logger.log(Level.INFO, "Region files: " + regionFiles.size());

        for (RegionFileExtended r : regionFiles) {
            logger.log(Level.INFO, "Region "+count_file+"/"+regionFiles.size()+": Current File: " + r.fileName.getName());

            try {
                r.convert(translations, regionPlugins);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to convert placed blocks", e);
                return;
            }
        }
        long duration = System.currentTimeMillis() - beginTime;
        logger.log(Level.INFO, "Done in " + duration + "ms" + System.getProperty("line.separator") + IDChanger.changedPlaced
                + " placed blocks changed." + System.getProperty("line.separator") + IDChanger.changedPlayer
                + " blocks in player inventories changed." + System.getProperty("line.separator") + IDChanger.changedChest
                + " blocks in entity inventories changed.");

        if (this.countBlockStats) {
            for (Map.Entry<BlockUID, Integer> entry : IDChanger.convertedBlockCount.entrySet()) {
                System.out.println("Count " + entry.getValue() + " " + entry.getKey());
            }
        }
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

        // Find all region files
        File[] files = regionDir.listFiles(mcaFiles);
        ArrayList<RegionFileExtended> result = new ArrayList<RegionFileExtended>();
        if(files == null ){
            throw new RuntimeException("No region files found");
        }

        for (int i = 0; i < files.length; i++) {
            logger.log(Level.INFO, "reading region file "+files[i].getName());
            result.add(new RegionFileExtended(files[i]));
        }
        logger.log(Level.INFO, "Found "+result.size()+" region files in "+regionDir.getName());
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
        logger.log(Level.INFO, "Found "+result.size()+" player files");

        return result;
    }
}
