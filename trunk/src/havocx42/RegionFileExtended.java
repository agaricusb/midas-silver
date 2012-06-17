package havocx42;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import pfaeff.IDChanger;

import nbt.Tag;
import nbt.TagInputStream;
import nbt.TagList;
import nbt.TagOutputStream;
import nbt.TagShort;

import region.RegionFile;

public abstract class RegionFileExtended extends RegionFile {

	public RegionFileExtended(File path) {
		super(path);
		// TODO Auto-generated constructor stub
	}
	
	protected abstract void convertRegion(IDChanger UI,Tag root,
			final HashMap<Integer, Integer> translations);
	
	protected abstract void convertItems(IDChanger UI,Tag root, HashMap<Integer, Integer> translations);

	public void convert(IDChanger UI,HashMap<Integer, Integer> translations) throws IOException {

		// Progress


		// System.out.println("Processing file " + rf.getFile());
		ArrayList<Point> chunks = new ArrayList<Point>();

		// Get available chunks
		for (int x = 0; x < 32; x++) {
			for (int z = 0; z < 32; z++) {
				if (hasChunk(x, z)) {
					chunks.add(new Point(x, z));
				}
			}
		}

		// PROGESSBAR CHUNK
		UI.pb_chunk.setMaximum(chunks.size() - 1);
		int count_chunk = 0;

		for (Point p : chunks) {
			// Progress
			UI.pb_chunk.setValue(count_chunk++);
			UI.lb_chunk.setText("Current Chunk: (" + p.x + "; " + p.y+ ")");
			// Read chunks

			DataInputStream input = getChunkDataInputStream(p.x, p.y);
			TagInputStream TIS = new TagInputStream(input);
			Tag root = TIS.readTag(true);
			input.close();
			TIS.close();
			// Find blocks
			convertRegion(UI,root, translations);
			// find blocks and items in chest etc. inventory
			convertItems(UI,root, translations);

			// Write chunks
			DataOutputStream output = getChunkDataOutputStream(p.x, p.y);
			TagOutputStream tos = new TagOutputStream(output);
			tos.writeTag(root, true);
			output.close();
			tos.close();
		}
	}

}