/*
 * Copyright 2011 Kai Rohr 
 *    
 *
 *    This file is part of mIDas.
 *
 *    mIDas is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    mIDas is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with mIDas.  If not, see <http://www.gnu.org/licenses/>.
 */

package pfaeff;

import havocx42.BlockUID;
import havocx42.TranslationRecord;
import havocx42.TranslationRecordFactory;
import havocx42.World;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;
import java.util.Map;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import region.RegionFile;

public class IDChanger {

    public static Logger        logger                = Logger.getLogger(IDChanger.class.getName());

    public static int changedPlaced = 0;
    public static int changedChest = 0;
    public static int changedPlayer = 0;

    public static Map<BlockUID, Integer> convertedBlockCount = new HashMap<BlockUID, Integer>();

    private static boolean isValidSaveGame(File f) {
        logger.log(Level.INFO, "Checking save game: " + f.getName());
        ArrayList<RegionFile> rf;
        try {
            rf = NBTFileIO.getRegionFiles(f);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to load save file", e);
            return false;
        }
        return ((rf != null) && (rf.size() > 0));

    }

    private static HashMap<BlockUID, BlockUID> readPatchFile(File f) {
        // new version use a hashmap to record what blocks to transmute
        // to what.
        final HashMap<BlockUID, BlockUID> translations = new HashMap<BlockUID, BlockUID>();

        if (!f.exists()) {
            logger.log(Level.SEVERE, "No such patch file: " + f.getName());
            System.exit(1);
        }

        try {
            FileInputStream fstream = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            TranslationRecord tr;
            while ((strLine = br.readLine()) != null) {
                try {
                    tr = TranslationRecordFactory.createTranslationRecord(strLine);
                    if (tr != null) {
                        translations.put(tr.source, tr.target);
                    } else {
                        logger.info("Patch contains an invalid line, no big deal: " + strLine);
                    }

                } catch (NumberFormatException e2) {
                    // JOptionPane.showMessageDialog(this,
                    // "That's not how you format translations \""+strLine+System.getProperty("line.separator")+"example:"+System.getProperty("line.separator")+"1 stone -> 3 dirt",
                    // "Error", JOptionPane.ERROR_MESSAGE);
                    logger.info("Patch contains an invalid line, no big deal" + strLine);
                    continue;
                }

            }
            br.close();
            in.close();
            fstream.close();

        } catch (Exception filewriting) {
            logger.log(Level.SEVERE, "Unable to open patch file", filewriting);
            System.exit(1);
        }

        return translations;
    }


    public static void main(String[] args) throws IOException {
        OptionSet options = null;

        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "help"), "Show the help");

                acceptsAll(asList("p", "patch-file"), "Patch file to use")
                        .withRequiredArg()
                        .ofType(File.class);

                acceptsAll(asList("i", "input-save-game"), "Save game to read as input")
                        .withRequiredArg()
                        .ofType(File.class);

                acceptsAll(asList("no-convert-blocks"), "Disable block ID conversion");
                acceptsAll(asList("no-convert-items"), "Disable item ID conversion");
                acceptsAll(asList("no-convert-buildcraft-pipes"),"Disable BuildCraft pipe ID conversion");
                acceptsAll(asList("no-convert-player-inventories"), "Disable player inventory ID conversion");

                acceptsAll(asList("convert-project-table"), "Enable conversion of RedPower2 Project Table to bau5 Project Bench");
                acceptsAll(asList("dump-tile-entities"), "Enable dumping tile entity NBT data for debugging purposes");
                acceptsAll(asList("count-block-stats"), "Enable counting the types of blocks converted");
                acceptsAll(asList("convert-charging-bench-gregtech"), "Enable conversion of IC2 Charging Bench to GregTech Charge-O-Mat");
                acceptsAll(asList("warn-unconverted-block-id-after"), "Log block IDs without mappings, after vanilla maximum")
                        .withRequiredArg()
                        .ofType(Integer.class);
            }
        };

        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            return;
        }

        if (!options.has("patch-file") || !options.has("input-save-game")) {
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        HashMap<BlockUID, BlockUID> translations = readPatchFile((File) options.valueOf("patch-file"));

        logger.log(Level.INFO, "loaded "+translations.size()+" translations");


        File saveGame = (File) options.valueOf("input-save-game");

        if (!isValidSaveGame(saveGame)) {
            logger.log(Level.SEVERE, "Invalid save game: "+ saveGame.getName());
            return;
        }

        // change block ids

        final World world;
        try {
            world = new World(saveGame);

            logger.log(Level.INFO, "Converting...");
            world.convert(translations, options);

        } catch (IOException e1) {
            logger.log(Level.WARNING, "Unable to open world, are you sure you have selected a save?");
        }
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}
