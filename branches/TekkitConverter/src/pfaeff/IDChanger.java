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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import nbt.Tag;
import nbt.TagByteArray;
import nbt.TagInputStream;
import nbt.TagOutputStream;
import region.RegionFile;


public class IDChanger extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4828256928267701067L;
	private ArrayList<File> saveGames = new ArrayList<File>();
	// Gui Elements
	private JComboBox cb_selectSaveGame;


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
								"Thats not how you format IDNames \"" + line
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
				// Backup savegame
				// if (c_backup.isSelected()) {
				// backUpSaveGame(saveGames.get(saveIndex));
				// }


				final HashMap<Integer, Integer> translations = new HashMap<Integer, Integer>();

				translations.put(new Integer(206),new Integer(211));
				translations.put(new Integer(207),new Integer(212));
				translations.put(new Integer(208),new Integer(213));
				

				// change block ids
				SwingWorker<Void,Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						changeIDs(regionFiles, translations);
						return null;
					}
				};
				// worker.addPropertyChangeListener(this);
				worker.execute();
				f=new File(saveGames.get(saveIndex),"convertedto1_2");
				if(!f.exists()){
					f.createNewFile();
				}
			} catch (IOException e1) {
				ErrorHandler.logError(e1);
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
			HashMap<Integer, Integer> translations) throws IOException {
		try {

			long beginTime = System.currentTimeMillis();
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
			JOptionPane.showMessageDialog(this, "Done in " + duration + "ms",
					"Information", JOptionPane.INFORMATION_MESSAGE);
		} catch (NullPointerException npe) {
			ErrorHandler.logError(npe);
			JOptionPane
			.showMessageDialog(
					this,
					"A serious error has occured, an errorlog should have been created. Please report this on the MC forum thread.",
					"Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}


	public void convertRegion(Tag root,final HashMap<Integer, Integer> translations) {
		Tag t = root.findChildByName("Blocks", true);
		if (t instanceof TagByteArray) {
			TagByteArray blocks = (TagByteArray) t;
			HashMap<Integer, Integer> indexToBlockIDs;
			indexToBlockIDs = new HashMap<Integer, Integer>();
			for (int i = 0; i < blocks.payload.length; i++) {	
				Integer bID = Integer.valueOf(
						0x000000FF & (int) (blocks.payload[i]));
				if (translations.containsKey(bID)) {
					// Only allow blocks to be replaced with
					// blocks
					if (translations.get(bID) <= 255) {
						indexToBlockIDs.put(Integer.valueOf(i),
								translations.get(bID));
					}
				}
			}
			
			// write changes to nbt tree
			Set<Map.Entry<Integer,Integer>> set = indexToBlockIDs.entrySet();
			for (Entry<Integer,Integer> entry : set) {
				blocks.payload[entry.getKey()] = entry.getValue().byteValue();
			}
			for (Entry<Integer,Integer> entry : set) {
				if((byte)blocks.payload[entry.getKey()] != (byte)entry.getValue().byteValue()){
					ErrorHandler.logError(entry.getKey()+" not converted to "+entry.getValue());
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			// Use system specific look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		@SuppressWarnings("unused")
		IDChanger frame = new IDChanger("Tekkit 1.1.4 to 1.2 world converter - by havocx42");
	}
}
