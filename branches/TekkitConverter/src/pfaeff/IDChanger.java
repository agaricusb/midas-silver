/*
 * Copyright 2011 Kai Röhr 
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

import havocx42.ErrorHandler;
import havocx42.PlayerFile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.text.ChangedCharSetException;

import nbt.Tag;
import nbt.TagByteArray;
import nbt.TagInputStream;
import nbt.TagList;
import nbt.TagOutputStream;
import nbt.TagShort;
import region.RegionFile;

/*
 * TODO: Clean up, isolate visualization from logic and data
 */
public class IDChanger extends JFrame implements ActionListener {
	private ArrayList<File> saveGames = new ArrayList<File>();
	private ArrayList<String> idNames = new ArrayList<String>();
	private static final int VERSION_GZIP = 1;
	private static final int VERSION_DEFLATE = 2;
	public int changedPlaced = 0;
	public int changedChest = 0;
	public int changedPlayer = 0;

	// Gui Elements
	private JComboBox cb_selectSaveGame;
	private JComboBox cb_selectSourceID;
	private JComboBox cb_selectTargetID;

	private JCheckBox c_backup;

	DefaultListModel model = new DefaultListModel();
	private JList li_ID;

	private JLabel lb_file;
	private JLabel lb_chunk;
	private JProgressBar pb_file;
	private JProgressBar pb_chunk;

	public IDChanger(String title) {
		super(title);

		// Init Data
		initSaveGames();
		initIDNames();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add GUI elements
		getContentPane().add(createOpenFilesPanel(), BorderLayout.PAGE_START);
		getContentPane().add(createProgressPanel(), BorderLayout.LINE_START);
		pack();

		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void initSaveGames() {
		File mcSavePath = getMCSavePath();
		if (mcSavePath.exists()) {
			saveGames = FileTools.getSubDirectories(mcSavePath);
		}
	}

	private void initIDNames() {
		try {
			String path = IDChanger.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath();
			File f = new File((new File(path)).getParent(), "IDNames.txt");
			if (f.exists()) {
				try {
					readFile(f);
				} catch (IOException e) {
					ErrorHandler.logError(e);
				}
			}
		} catch (URISyntaxException e1) {
			ErrorHandler.logError(e1);
		}
	}

	private JPanel createProgressPanel() {
		JPanel pnl_progress = new JPanel();
		pnl_progress
				.setLayout(new BoxLayout(pnl_progress, BoxLayout.PAGE_AXIS));
		pnl_progress.setBorder(BorderFactory.createTitledBorder("Progress"));
		pnl_progress.add(createStartPanel());
		pnl_progress.add(Box.createVerticalStrut(10));
		lb_file = new JLabel("Current File:");
		pnl_progress.add(lb_file);
		pnl_progress.add(createFileProgressBar());
		lb_chunk = new JLabel("Current Chunk:");
		pnl_progress.add(lb_chunk);
		pnl_progress.add(createChunkProgressBar());

		pnl_progress.setPreferredSize(new Dimension(400, 150));

		return pnl_progress;
	}

	private JPanel createStartPanel() {
		JPanel pnl_start = new JPanel(new FlowLayout());
		pnl_start.add(createStartButton());
		// pnl_start.add(createBackupCheckBox());
		return pnl_start;
	}

	private JProgressBar createFileProgressBar() {
		pb_file = new JProgressBar(0, 100);
		pb_file.setValue(0);
		pb_file.setStringPainted(true);
		return pb_file;
	}

	private JProgressBar createChunkProgressBar() {
		pb_chunk = new JProgressBar(0, 100);
		pb_chunk.setValue(0);
		pb_chunk.setStringPainted(true);
		return pb_chunk;
	}

	private JButton createStartButton() {
		JButton btn_start = new JButton("Start");
		btn_start.addActionListener(this);
		btn_start.setActionCommand("start");
		return btn_start;
	}

	// new version
	private JPanel createOpenFilesPanel() {
		JPanel pnl_openFiles = new JPanel();
		Box b= new Box(BoxLayout.LINE_AXIS);
		pnl_openFiles.setLayout(new BoxLayout(pnl_openFiles,BoxLayout.PAGE_AXIS));
		JLabel l = new JLabel("Only Select a Tekkit 1.1.4 World!!!");
		l.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnl_openFiles.add(l);
		pnl_openFiles.setBorder(BorderFactory
				.createTitledBorder("Select Savegame"));
		b.add(new JLabel("Available savegames:"));
		b.add(createSelectSaveGameComboBox());
		b.add(createOpenFileButton());
		pnl_openFiles.add(b);
		

		return pnl_openFiles;
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
		CBRenderer renderer = new CBRenderer();
		renderer.maxIndex = names.length - 1;

		cb_selectSaveGame = new JComboBox(names);
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
			return new File(System.getProperty("user.home", "."),
					"Library/Application Support/" + s);
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
			ErrorHandler.logError(e);
			return false;
		}
		return ((rf != null) && (rf.size() > 0));

	}

	private ArrayList<String> readFile(File f) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		int index = 0;
		while (line != null) {
			/*
			 * if (!line.equals("")) { line = " " + line; } line = index + line;
			 */
			try {
				if (line.contains(" ")) {
					Integer.parseInt(line.substring(0, line.indexOf(" ")));
					result.add(line);
				}
			} catch (NumberFormatException e) {
				JOptionPane
						.showMessageDialog(
								this,
								"That's not how you format IDNames \"" + line
										+ System.getProperty("line.separator")
										+ "example:"
										+ System.getProperty("line.separator")
										+ "1 stone", "Error",
								JOptionPane.ERROR_MESSAGE);
				ErrorHandler
						.logError("User tried to input IDNames, no big deal");
			}
			line = br.readLine();
			index++;
		}
		br.close();
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// something changed - reset progress bars
		pb_file.setValue(0);
		pb_chunk.setValue(0);

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

						cb_selectSaveGame.addItem(f.getName());
						cb_selectSaveGame.setSelectedIndex(cb_selectSaveGame
								.getItemCount() - 1);

						// workaround for a bug that occurs when there was no
						// item in the beginning
						if (cb_selectSaveGame.getItemCount() > saveGames.size()) {
							cb_selectSaveGame.removeItemAt(0);
						}

						// System.out.println(saveGames.size());
						// System.out.println(cb_selectSaveGame.getItemCount());

					} else {
						cb_selectSaveGame
								.setSelectedIndex(saveGames.indexOf(f));
						JOptionPane
								.showMessageDialog(
										this,
										"The selected savegame is already in the list!",
										"Information",
										JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "Invalid savegame!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		// Start
		if ("start".equals(e.getActionCommand())) {
			// Savegame
			int saveIndex = cb_selectSaveGame.getSelectedIndex();
			// System.out.println("Selected Savegame " +
			// saveGames.get(saveIndex));
			BufferedWriter log = null;
			FileWriter fstream = null;
			try {
				if ((saveGames == null) || (saveGames.size() == 0)) {
					return;
				}

				if (saveIndex < 0) {
					return;
				}

				File f;
				f=new File(saveGames.get(saveIndex),"convertedto1_2");
				if(f.exists()){
					JOptionPane.showMessageDialog(this, "World already converted. To force another conversion delete convertedto1_2 file in save directory",
							"Information", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				final ArrayList<RegionFile> regionFiles = NBTFileIO
						.getRegionFiles(saveGames.get(saveIndex));
				final ArrayList<PlayerFile> datFiles = NBTFileIO
						.getDatFiles(saveGames.get(saveIndex));
				// Backup savegame
				// if (c_backup.isSelected()) {
				// backUpSaveGame(saveGames.get(saveIndex));
				// }

				// new version use a hashmap to record what blocks to transmute
				// to what.
				final HashMap<Integer, Integer> translations = new HashMap<Integer, Integer>();
				// Create file
				
				translations.put(new Integer(177),new Integer(135));
				translations.put(new Integer(189),new Integer(187));
				translations.put(new Integer(190),new Integer(123));
				translations.put(new Integer(206),new Integer(211));
				translations.put(new Integer(207),new Integer(212));
				translations.put(new Integer(208),new Integer(213));
				translations.put(new Integer(208),new Integer(213));
				translations.put(new Integer(196),new Integer(255));
				translations.put(new Integer(218),new Integer(226));
				translations.put(new Integer(187),new Integer(189));
				translations.put(new Integer(177),new Integer(135));
				
				
				//thanks to HalibutBarn for these
				translations.put(new Integer(7256),new Integer(7310)); // Crowbar
                translations.put(new Integer(7258),new Integer(7256)); // Signal tuner
                translations.put(new Integer(7259),new Integer(7264)); // Creosote oil
                translations.put(new Integer(7260),new Integer(7309)); // Signal lamp
                translations.put(new Integer(7261),new Integer(7262)); // Controller circuit
                translations.put(new Integer(7262),new Integer(7297)); // Receiver circuit
                translations.put(new Integer(7263),new Integer(7288)); // Coal coke
                translations.put(new Integer(7264),new Integer(7291)); // Wooden tie
                translations.put(new Integer(7265),new Integer(7304)); // Stone tie
                translations.put(new Integer(7266),new Integer(7295)); // Wooden railbed
                translations.put(new Integer(7267),new Integer(7277)); // Stone railbed
                translations.put(new Integer(7268),new Integer(7274)); // Junction rail
                translations.put(new Integer(7269),new Integer(7282)); // Switch rail
                translations.put(new Integer(7270),new Integer(7290)); // Boarding rail
                translations.put(new Integer(7271),new Integer(7278)); // Holding rail
                translations.put(new Integer(7272),new Integer(7268)); // One-way rail
                translations.put(new Integer(7273),new Integer(7266)); // Control rail
                translations.put(new Integer(7274),new Integer(7300)); // Launcher rail
                translations.put(new Integer(7275),new Integer(7301)); // Priming rail
                translations.put(new Integer(7276),new Integer(7281)); // Wooden rail
                translations.put(new Integer(7277),new Integer(7284)); // Wooden booster rail
                translations.put(new Integer(7278),new Integer(7269)); // Wooden junction rail
                translations.put(new Integer(7279),new Integer(7298)); // Wooden switch rail
                translations.put(new Integer(7280),new Integer(7271)); // High-speed rail
                translations.put(new Integer(7281),new Integer(7292)); // High-speed switch rail
                translations.put(new Integer(7282),new Integer(7261)); // High-speed booster rail
                translations.put(new Integer(7283),new Integer(7287)); // High-speed transition rail
                translations.put(new Integer(7284),new Integer(7275)); // Metal posts
                translations.put(new Integer(7285),new Integer(7293)); // Wood posts
                translations.put(new Integer(7286),new Integer(7276)); // Stone post
                translations.put(new Integer(7287),new Integer(7285)); // Concrete block
                translations.put(new Integer(7288),new Integer(7273)); // Block signal
                translations.put(new Integer(7289),new Integer(7272)); // Dual-head block signal
                translations.put(new Integer(7290),new Integer(7260)); // Switch motor
                translations.put(new Integer(7291),new Integer(7294)); // Signal receiver box
                translations.put(new Integer(7292),new Integer(7299)); // Signal controller box
                translations.put(new Integer(7293),new Integer(7279)); // Minecart loader
                translations.put(new Integer(7294),new Integer(7270)); // Advanced Loader
                translations.put(new Integer(7295),new Integer(7259)); // Minecart unloader
                translations.put(new Integer(7296),new Integer(7302)); // Advanced unloader
                translations.put(new Integer(7297),new Integer(7280)); // Minecart liquid loader
                translations.put(new Integer(7298),new Integer(7286)); // Minecart liquid unloader
                translations.put(new Integer(7299),new Integer(7263)); // Minecart dispenser
                translations.put(new Integer(7300),new Integer(7283)); // Coke oven brick
                translations.put(new Integer(7301),new Integer(7265)); // TNT cart
                translations.put(new Integer(7302),new Integer(7308)); // Tank cart
                translations.put(new Integer(7303),new Integer(7307)); // Tunnel bore
                translations.put(new Integer(7304),new Integer(7311)); // Diamond bore head
                translations.put(new Integer(7305),new Integer(7303)); // Iron bore head


				
				
				
			

				// change block ids
				SwingWorker worker = new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						changeIDs(regionFiles, datFiles, translations);
						return null;
					}
				};
				// worker.addPropertyChangeListener(this);
				worker.execute();
			} catch (IOException e1) {
				ErrorHandler.logError(e1);
			} finally {
				if (log != null) {
					try {
						log.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (fstream != null) {
					try {
						fstream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}



	/**
	 * Changes IDs in the given region files
	 * 
	 * @param regionFiles
	 * @param translations
	 *            hashmap containing the source block id as a key and the target
	 *            block id as a value for that key
	 * @throws IOException
	 */
	public void changeIDs(ArrayList<RegionFile> regionFiles,
			ArrayList<PlayerFile> datFiles,
			HashMap<Integer, Integer> translations) throws IOException {
		changedChest = 0;
		changedPlaced = 0;
		changedPlayer = 0;
		try {

			long beginTime = System.currentTimeMillis();

			// player inventories
			convertPlayerInventories(datFiles, translations);
			// PROGESSBAR FILE
			int count_file = 0;
			if (regionFiles == null) {
				// No valid region files found
				return;
			}
			pb_file.setMaximum(regionFiles.size() - 1);

			for (RegionFile rf : regionFiles) {
				// Progress
				pb_file.setValue(count_file++);
				lb_file.setText("Current File: " + rf.getFile().getName());

				// System.out.println("Processing file " + rf.getFile());
				ArrayList<Point> chunks = new ArrayList<Point>();

				// Get available chunks
				for (int x = 0; x < 32; x++) {
					for (int z = 0; z < 32; z++) {
						if (rf.hasChunk(x, z)) {
							chunks.add(new Point(x, z));
						}
					}
				}

				// PROGESSBAR CHUNK
				pb_chunk.setMaximum(chunks.size() - 1);
				int count_chunk = 0;

				for (Point p : chunks) {
					// Progress
					pb_chunk.setValue(count_chunk++);
					lb_chunk.setText("Current Chunk: (" + p.x + "; " + p.y
							+ ")");
					// Read chunks

					DataInputStream input = rf
							.getChunkDataInputStream(p.x, p.y);
					TagInputStream TIS = new TagInputStream(input);
					Tag root = TIS.readTag(true);
					input.close();
					TIS.close();
					// Find blocks
					convertRegion(root, translations);
					// find blocks and items in chest etc. inventory
					convertItems(root, translations);

					// Write chunks
					DataOutputStream output = rf.getChunkDataOutputStream(p.x,
							p.y);
					TagOutputStream tos = new TagOutputStream(output);
					tos.writeTag(root, true);
					output.close();
					tos.close();

				}
				rf.close();
			}

			long duration = System.currentTimeMillis() - beginTime;
			JOptionPane.showMessageDialog(
					this,
					"Done in " + duration + "ms"
							+ System.getProperty("line.separator")
							+ changedPlaced + " placed blocks changed."
							+ System.getProperty("line.separator")
							+ changedPlayer
							+ " blocks in player inventories changed."
							+ System.getProperty("line.separator")
							+ changedChest
							+ " blocks in entity inventories changed.",
					"Information", JOptionPane.INFORMATION_MESSAGE);
		} catch (NullPointerException npe) {
			ErrorHandler.logError(npe);
			JOptionPane
					.showMessageDialog(
							this,
							"A serious error has occured, an errorlog should have been created. Please report this on the MC forum thread.",
							"Information", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void convertPlayerInventories(ArrayList<PlayerFile> datFiles,
			HashMap<Integer, Integer> translations) {
		try {

			pb_file.setMaximum(datFiles.size() - 1);
			int count_file = 0;

			for (PlayerFile df : datFiles) {

				pb_file.setValue(count_file++);
				lb_file.setText("Current File: " + df.getName());
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
										changedPlayer++;
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

	public void convertRegion(Tag root,
			final HashMap<Integer, Integer> translations) {
		Tag t = root.findChildByName("Blocks", true);
		if (t instanceof TagByteArray) {
			TagByteArray blocks = (TagByteArray) t;
			HashMap<Integer, Integer> indexToBlockIDs;
			indexToBlockIDs = new HashMap<Integer, Integer>();
			for (int i = 0; i < blocks.payload.length; i++) {
				Integer bID = Integer
						.valueOf(0x000000FF & (int) (blocks.payload[i]));
				if (translations.containsKey(bID)) {
					// Only allow blocks to be replaced with
					// blocks
					if (translations.get(bID) <= 255) {
						changedPlaced++;
						indexToBlockIDs.put(Integer.valueOf(i),
								translations.get(bID));
					}
				}
			}

			// write changes to nbt tree
			Set<Map.Entry<Integer, Integer>> set = indexToBlockIDs.entrySet();
			for (Entry<Integer, Integer> entry : set) {
				blocks.payload[entry.getKey()] = entry.getValue().byteValue();
			}
			for (Entry<Integer, Integer> entry : set) {
				if ((byte) blocks.payload[entry.getKey()] != (byte) entry
						.getValue().byteValue()) {
					ErrorHandler.logError(entry.getKey() + " not converted to "
							+ entry.getValue());
				}
			}
			// System.out.print("test");
		}
	}

	public void convertItems(Tag root, HashMap<Integer, Integer> translations) {
		HashMap<Integer, Integer> indexToBlockIDs;
		ArrayList<Tag> items = new ArrayList<Tag>();
		root.findAllChildrenByName(items, "Items", true);
		for (Tag t2 : items) {
			if (t2 instanceof TagList) {
				ArrayList<Tag> ids = new ArrayList<Tag>();
				t2.findAllChildrenByName(ids, "id", true);
				indexToBlockIDs = new HashMap<Integer, Integer>();
				for (int i = 0; i < ids.size(); i++) {
					Tag id = ids.get(i);
					if (id instanceof TagShort) {
						TagShort idShort = (TagShort) id;
						if (translations.containsKey(Integer
								.valueOf(idShort.payload))) {
							Integer toval = translations.get(Integer
									.valueOf(idShort.payload));
							if (toval != null) {
								changedChest++;
								indexToBlockIDs.put(Integer.valueOf(i), toval);
							} else {
								System.err.println("null target");
								ErrorHandler
										.logError("null Target while converting items");
							}
						}
					}
				}
				// update nbt tree
				Set<Integer> set = indexToBlockIDs.keySet();
				for (Integer i : set) {
					((TagShort) ids.get(i)).payload = indexToBlockIDs.get(i)
							.shortValue();
				}
			}
		}
	}

	public DataInputStream getLevelDataInputStream(RandomAccessFile f) {
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

	public static void main(String[] args) {
		try {
			// Use system specific look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		IDChanger frame = new IDChanger("Tekkit 1.1.4 to 2.0/2.1 world converter - by havocx42");
	}
}
