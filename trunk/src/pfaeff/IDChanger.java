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
import havocx42.ConverterPlugin;
import havocx42.EventQueueProxy;
import havocx42.FileListCellRenderer;
import havocx42.PluginLoader;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;

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

import region.RegionFile;

/*
 * TODO: Clean up, isolate visualization from logic and data
 */
public class IDChanger extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 9149749206914440913L;
	private ArrayList<File>		saveGames			= new ArrayList<File>();
	private ArrayList<String>	idNames				= new ArrayList<String>();
	public Status				status				= new Status();

	// Gui Elements
	private JComboBox			cb_selectSaveGame;
	private JComboBox			cb_selectSourceID;
	private JComboBox			cb_selectTargetID;

	DefaultListModel			model				= new DefaultListModel();
	public JList				li_ID;

	private static Logger		logger				= Logger.getLogger(IDChanger.class.getName());

	public IDChanger(String title) throws SecurityException, IOException {
		super(title);

		// Init Data

		initSaveGames();
		initIDNames();
		initLogger();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add GUI elements
		getContentPane().add(createOpenFilesPanel(), BorderLayout.PAGE_START);
		getContentPane().add(createChooseIDsPanel(), BorderLayout.LINE_START);
		getContentPane().add(createProgressPanel(), BorderLayout.LINE_END);
		pack();

		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void initLogger() {
		Logger rootLogger = Logger.getLogger("");
		PopupHandler popupHandler = new PopupHandler(this);
		rootLogger.addHandler(popupHandler);
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

	private void initSaveGames() {
		File mcSavePath = getMCSavePath();
		if (mcSavePath.exists()) {
			saveGames = FileTools.getSubDirectories(mcSavePath);
		}
	}

	private void initIDNames() {
		try {
			String path = IDChanger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File f = new File((new File(path)).getParent(), "IDNames.txt");
			if (f.exists()) {
				idNames = readFile(f);
			} else {
				logger.info("IDNames.txt does not exist");
			}
		} catch (IOException | URISyntaxException e1) {
			logger.log(Level.WARNING, "Unable to load IDNames.txt", e1);
		}
	}

	private JPanel createProgressPanel() {
		JPanel pnl_progress = new JPanel();
		pnl_progress.setLayout(new BoxLayout(pnl_progress, BoxLayout.PAGE_AXIS));
		pnl_progress.setBorder(BorderFactory.createTitledBorder("Progress"));
		pnl_progress.add(createStartPanel());
		pnl_progress.add(Box.createVerticalStrut(10));
		status.lb_file = new JLabel("Current File:");
		pnl_progress.add(status.lb_file);
		pnl_progress.add(createFileProgressBar());
		status.lb_chunk = new JLabel("Current Chunk:");
		pnl_progress.add(status.lb_chunk);
		pnl_progress.add(createChunkProgressBar());

		pnl_progress.setPreferredSize(new Dimension(400, pnl_progress.getHeight()));

		return pnl_progress;
	}

	private JPanel createStartPanel() {
		JPanel pnl_start = new JPanel(new FlowLayout());
		pnl_start.add(createStartButton());
		// pnl_start.add(createBackupCheckBox());
		return pnl_start;
	}

	private JProgressBar createFileProgressBar() {
		status.pb_file = new JProgressBar(0, 100);
		status.pb_file.setValue(0);
		status.pb_file.setStringPainted(true);
		return status.pb_file;
	}

	private JProgressBar createChunkProgressBar() {
		status.pb_chunk = new JProgressBar(0, 100);
		status.pb_chunk.setValue(0);
		status.pb_chunk.setStringPainted(true);
		return status.pb_chunk;
	}

	// new version changed From to Translations
	private JPanel createChooseIDsPanel() {
		JPanel pnl_chooseIDs = new JPanel();
		pnl_chooseIDs.setBorder(BorderFactory.createTitledBorder("Change IDs"));
		pnl_chooseIDs.setLayout(new BoxLayout(pnl_chooseIDs, BoxLayout.PAGE_AXIS));
		pnl_chooseIDs.add(new JLabel("Translations: "));
		pnl_chooseIDs.add(initIDPane());
		pnl_chooseIDs.add(initSourceIDPanel());
		pnl_chooseIDs.add(new JLabel("To:"));
		pnl_chooseIDs.add(createSelectTargetIDComboBox());

		return pnl_chooseIDs;
	}

	private JPanel initSourceIDPanel() {
		JPanel pnl_sourceID = new JPanel(new FlowLayout());
		pnl_sourceID.add(createSelectSourceIDComboBox());
		pnl_sourceID.add(createAddIDButton());
		pnl_sourceID.add(createRemoveIDButton());
		return pnl_sourceID;
	}

	private JButton createStartButton() {
		JButton btn_start = new JButton("Start");
		btn_start.addActionListener(this);
		btn_start.setActionCommand("start");
		return btn_start;
	}

	// new version updated buttons
	private JButton createRemoveIDButton() {
		JButton btn_removeID = new JButton("Remove Translation");
		btn_removeID.addActionListener(this);
		btn_removeID.setActionCommand("removeID");
		return btn_removeID;
	}

	private JButton createAddIDButton() {
		JButton btn_addID = new JButton("Add Translation");
		btn_addID.addActionListener(this);
		btn_addID.setActionCommand("addID");
		return btn_addID;
	}

	// new version
	private JPanel createOpenFilesPanel() {
		JPanel pnl_openFiles = new JPanel(new FlowLayout());
		pnl_openFiles.setBorder(BorderFactory.createTitledBorder("Select files"));
		pnl_openFiles.add(new JLabel("Load patch file:"));
		pnl_openFiles.add(createOpenPatchFileButton());
		pnl_openFiles.add(new JLabel("Available savegames:"));
		pnl_openFiles.add(createSelectSaveGameComboBox());
		pnl_openFiles.add(createOpenFileButton());

		return pnl_openFiles;
	}

	// new version
	private JButton createOpenPatchFileButton() {
		JButton btn_openFile = new JButton("Load");
		btn_openFile.addActionListener(this);
		btn_openFile.setActionCommand("openPatch");
		return btn_openFile;
	}

	private JScrollPane initIDPane() {
		JScrollPane pn_ID = new JScrollPane(initIDTextArea());
		pn_ID.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return pn_ID;
	}

	private JList initIDTextArea() {
		li_ID = new JList(model);
		return li_ID;
	}

	private JComboBox createSelectSourceIDComboBox() {
		if (idNames.size() > 0) {
			cb_selectSourceID = new JComboBox(idNames.toArray());
		} else {
			cb_selectSourceID = new JComboBox();
		}

		CBRenderer renderer = new CBRenderer();
		renderer.maxIndex = idNames.size() - 1;

		cb_selectSourceID.setEditable(true);
		cb_selectSourceID.setRenderer(renderer);

		cb_selectSourceID.addActionListener(new NumberOnlyActionListener(idNames, 0, 32000 - 1));
		return cb_selectSourceID;
	}

	private JComboBox createSelectTargetIDComboBox() {
		if (idNames.size() > 0) {
			cb_selectTargetID = new JComboBox(idNames.toArray());
		} else {
			cb_selectTargetID = new JComboBox();
		}

		CBRenderer renderer = new CBRenderer();
		renderer.maxIndex = idNames.size() - 1;

		cb_selectTargetID.setEditable(true);
		cb_selectTargetID.setRenderer(renderer);

		cb_selectTargetID.addActionListener(new NumberOnlyActionListener(idNames, 0, 32000 - 1));
		return cb_selectTargetID;
	}

	private JComboBox createSelectSaveGameComboBox() {
		String[] names;
		if (saveGames.size() > 0) {
			names = new String[saveGames.size()];
			for (int i = 0; i < saveGames.size(); i++) {
				names[i] = saveGames.get(i).getName();
			}
		} else {
			names = new String[1];
			names[0] = "";
		}

		// Set initial number of saves for rendering
		FileListCellRenderer renderer = new FileListCellRenderer();
		renderer.maxIndex = names.length - 1;

		cb_selectSaveGame = new JComboBox(saveGames.toArray());
		cb_selectSaveGame.setRenderer(renderer);
		cb_selectSaveGame.addActionListener(this);
		return cb_selectSaveGame;
	}

	private JButton createOpenFileButton() {
		JButton btn_openFile = new JButton("Add savegame");
		btn_openFile.addActionListener(this);
		btn_openFile.setActionCommand("openFolder");
		return btn_openFile;
	}

	private File getMCSavePath() {
		return new File(getAppDir("minecraft"), "saves");
	}

	public static File getAppDir(String s) {
		String osName = System.getProperty("os.name");
		OperatingSystem operatingSystem = OperatingSystem.resolve(osName);
		OperatingSystemFamily family = OperatingSystemFamily.WINDOWS;
		if (operatingSystem != null) {
			family = operatingSystem.getFamily();
		}
		switch (family) {
		case WINDOWS:
			return new File(System.getenv("appdata"), "." + s);
		case LINUX:
			return new File(System.getProperty("user.home", "."), s);
		case MAC: {
			return new File(System.getProperty("user.home", "."), "Library/Application Support/" + s);
		}
		default:
			return new File(System.getProperty("user.home", "."), s);
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

	private ArrayList<String> readFile(File f) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while (line != null) {
			/*
			 * if (!line.equals("")) { line = " " + line; } line = index + line;
			 */
			try {
				if (line.matches("[0-9]+(:?[0-9]+)? [0-9a-zA-Z.]+"))
					result.add(line);

			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "That's not how you format IDNames \"" + line + System.getProperty("line.separator")
						+ "example:" + System.getProperty("line.separator") + "1 stone", "Error", JOptionPane.ERROR_MESSAGE);
				logger.config("User tried to input incorrectly formatted IDNames, no big deal");
			}
			line = br.readLine();
		}
		br.close();
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// something changed - reset progress bars
		status.pb_file.setValue(0);
		status.pb_chunk.setValue(0);

		// Open Save Folder
		if ("openFolder".equals(e.getActionCommand())) {
			final JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setSelectedFile(new File(getMCSavePath() + "/New World"));
			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				if (isValidSaveGame(f)) {
					if (!saveGames.contains(f)) {
						saveGames.add(f);

						cb_selectSaveGame.addItem(f);
						cb_selectSaveGame.setSelectedIndex(cb_selectSaveGame.getItemCount() - 1);

						// workaround for a bug that occurs when there was no
						// item in the beginning
						if (cb_selectSaveGame.getItemCount() > saveGames.size()) {
							cb_selectSaveGame.removeItemAt(0);
						}

						// System.out.println(saveGames.size());
						// System.out.println(cb_selectSaveGame.getItemCount());

					} else {
						cb_selectSaveGame.setSelectedIndex(saveGames.indexOf(f));
						JOptionPane.showMessageDialog(this, "The selected savegame is already in the list!", "Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "Invalid savegame!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		// new version open a patch file
		if ("openPatch".equals(e.getActionCommand())) {
			final JFileChooser fc = new JFileChooser();
			String path;
			try {
				path = IDChanger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				fc.setSelectedFile(new File((new File(path)).getParent(), "Patch.txt"));
			} catch (URISyntaxException e1) {
				logger.log(Level.WARNING, "Unable to load Patch file", e1);
			}

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
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
		}

		// Add ID
		// new version adds user stupidity resistance II
		if ("addID".equals(e.getActionCommand())) {
			try {
				String currentSource = (String) cb_selectSourceID.getSelectedItem();
				String currentTarget = (String) cb_selectTargetID.getSelectedItem();

				TranslationRecord tr = TranslationRecordFactory.createTranslationRecord(currentSource, currentTarget);
				if (tr != null) {
					addTranslation(tr);
				} else {
					JOptionPane.showMessageDialog(this, "That's not how you format translations" + System.getProperty("line.separator")
							+ "example:" + System.getProperty("line.separator") + "1 stone" + System.getProperty("line.separator")
							+ "3 dirt", "Error", JOptionPane.ERROR_MESSAGE);
				}
				// new version uses adds -> targetid to string

				// old version- didn't include target
				// model.add(index,
				// (String)cb_selectSourceID.getSelectedItem()+);
			} catch (NumberFormatException badinput) {
				JOptionPane.showMessageDialog(this, "That's not how you format translations" + System.getProperty("line.separator")
						+ "example:" + System.getProperty("line.separator") + "1 stone -> 3 dirt", "Error", JOptionPane.ERROR_MESSAGE);
				// ErrorHandler.logError(badinput);
			}
		}

		// Remove ID
		if ("removeID".equals(e.getActionCommand())) {
			for (int i = li_ID.getSelectedIndices().length - 1; i >= 0; i--) {
				model.remove(li_ID.getSelectedIndices()[i]);
			}
			// model.remove(model.indexOf((String)li_ID.getSelectedValue()));
		}
		// Start
		if ("start".equals(e.getActionCommand())) {
			// Savegame
			int saveIndex = cb_selectSaveGame.getSelectedIndex();
			if ((saveGames == null) || (saveGames.size() == 0)) {
				JOptionPane.showMessageDialog(this, "No save game has been chosen!", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (saveIndex < 0) {
				return;
			}

			if (model.size() == 0) {
				JOptionPane.showMessageDialog(this, "No IDs have been chosen!", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
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
				world = new World(saveGames.get(saveIndex));

				SwingWorker worker = new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						world.convert(UI, translations);
						return null;
					}
				};

				// worker.addPropertyChangeListener(this);
				worker.execute();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Unable to open world, are you sure you have selected a save?");
			}
		}
		return;
	}

	private void addTranslation(TranslationRecord tr) {
		for (int i = 0; i < model.size(); i++) {
			if (((TranslationRecord) model.get(i)).source.equals(tr.source)) {
				JOptionPane.showMessageDialog(this, "Source ID " + tr.source + " is already being translated!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		int index = 0;
		if (model.getSize() > 0) {
			index = model.getSize();
		}
		model.add(index, tr);
		return;
	}

	public static void main(String[] args) {
		try {
			initRootLogger();
		} catch (SecurityException | IOException e) {
			logger.log(Level.WARNING, "Unable to create log File", e);
			return;
		}
		EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		queue.push(new EventQueueProxy());

		try {
			// Use system specific look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unable to set look and feel", e);
		}
		try {
			IDChanger frame = new IDChanger("mIDas *GOLD* V0.2.4");

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Runtime Exception", e);
		}

		logger.config("System Look and Feel:" + UIManager.getSystemLookAndFeelClassName().toString());

	}
}
