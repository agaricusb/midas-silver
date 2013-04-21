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
import havocx42.EventQueueProxy;
import havocx42.FileListCellRenderer;
import havocx42.Status;
import havocx42.TranslationRecord;
import havocx42.TranslationRecordFactory;
import havocx42.World;
import havocx42.logging.PopupHandler;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import region.RegionFile;

public class IDChanger {
    /**
     * 
     */
    private static final long    serialVersionUID    = 9149749206914440913L;
    private ArrayList<File>        saveGames            = new ArrayList<File>();
    private ArrayList<String>    idNames                = new ArrayList<String>();
    public Status                status                = new Status();

    // Gui Elements
    private JComboBox            cb_selectSaveGame;
    private JComboBox            cb_selectSourceID;
    private JComboBox            cb_selectTargetID;

    DefaultListModel            model                = new DefaultListModel();
    public JList                li_ID;

    private static Logger        logger                = Logger.getLogger(IDChanger.class.getName());

    public IDChanger() throws IOException {
        initIDNames();
    }

    private static void initRootLogger() throws SecurityException, IOException {

        FileHandler fileHandler;
        fileHandler = new FileHandler("midasLog.%u.%g.txt", 1024 * 1024, 3, true);
        fileHandler.setLevel(Level.CONFIG);
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            handler.setLevel(Level.INFO);
        }
        rootLogger.setLevel(Level.CONFIG);
        rootLogger.addHandler(fileHandler);
    }

    private void initIDNames() {
        try {
            InputStream inputStream = IDChanger.class.getResourceAsStream("/IDNames.txt");
            if (inputStream != null) {
                idNames = readFile(inputStream);
            } else {
                logger.info("IDNames.txt does not exist");
            }
        } catch (IOException e1) {
            logger.log(Level.WARNING, "Unable to load IDNames.txt", e1);
        }
    }

    private boolean isValidSaveGame(File f) {
        ArrayList<RegionFile> rf;
        try {
            rf = NBTFileIO.getRegionFiles(f);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to load save file", e);
            return false;
        }
        return ((rf != null) && (rf.size() > 0));

    }

    private ArrayList<String> readFile(InputStream f) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(f));
        String line = br.readLine();
        while (line != null) {
            /*
             * if (!line.equals("")) { line = " " + line; } line = index + line;
             */
            try {
                if (line.matches("[0-9]+(:?[0-9]+)? [0-9a-zA-Z.]+"))
                    result.add(line);

            } catch (NumberFormatException e) {
                logger.config("That's not how you format IDNames \"" + line + System.getProperty("line.separator")
                        + "example:" + System.getProperty("line.separator") + "1 stone");
                logger.config("User tried to input incorrectly formatted IDNames, no big deal");
            }
            line = br.readLine();
        }
        br.close();
        return result;
    }

    void readPatchFile(File f) {
        if (f.exists()) {
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
                            addTranslation(tr);
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
                logger.log(Level.WARNING, "Unable to open patch file", filewriting);
            }
        }
    }

    public void convert(File saveGame) {
        // new version use a hashmap to record what blocks to transmute
        // to what.
        final HashMap<BlockUID, BlockUID> translations = new HashMap<BlockUID, BlockUID>();

        for (int i = 0; i < model.size(); i++) {
            TranslationRecord tr = (TranslationRecord) model.get(i);
            logger.config(tr.toString());
            if (tr.source != null && tr.target != null) {
                translations.put(tr.source, tr.target);
            }

        }

        final IDChanger UI = this;
        // change block ids

        final World world;
        try {
            world = new World(saveGame);

            logger.log(Level.INFO, "Converting...");
            world.convert(UI, translations);

        } catch (IOException e1) {
            logger.log(Level.WARNING, "Unable to open world, are you sure you have selected a save?");
        }
    }

    private void addTranslation(TranslationRecord tr) {
        int index = 0;
        if (model.getSize() > 0) {
            index = model.getSize();
        }
        model.add(index, tr);
        return;
    }

    public static void main(String[] args) throws IOException {
        initRootLogger();

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

        IDChanger self = new IDChanger();

        self.readPatchFile((File) options.valueOf("patch-file"));
        self.convert((File) options.valueOf("input-save-game"));
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}
