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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class FileTools {
	/**
	 * Returns a list of sub directories of a certain directory
	 * 
	 * @param dir
	 * @return
	 */
	public static ArrayList<File> getSubDirectories(File dir) {
		ArrayList<File> result = new ArrayList<File>();
		// Create a filter to only include directories
		FileFilter directories = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}			
		};			
		File[] subdirs = dir.listFiles(directories);
		Collections.addAll(result, subdirs);
		return result;
	}
	
	/**
	 * Copies a directory and all files contained
	 * 
	 * @param sourceDir
	 * @param destDir
	 * @throws IOException
	 */
	public static void copyDirectory(File sourceDir, File destDir) throws IOException {
		if (sourceDir.isDirectory()) {
			if (!destDir.exists()) {
				destDir.mkdir();
			}
			String[] children = sourceDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceDir, children[i]), new File(destDir, children[i]));
			}
		} else {
			copyFile(sourceDir, destDir);
		}
	}
	
	/**
	 * Copies a file
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    InputStream in = new FileInputStream(sourceFile);
	    OutputStream out = new FileOutputStream(destFile);

	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
}
