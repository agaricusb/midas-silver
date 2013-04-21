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

import havocx42.PlayerFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import region.RegionFile;

public class NBTFileIO {
    /**
     * Returns a list of region files for a specific world
     * 
     * @param baseFolder
     *            Folder that contains the level files and the region directory
     * @return A list of region files
     * @throws IOException
     */
    public static ArrayList<RegionFile> getRegionFiles(File baseFolder)
            throws IOException {
        // Switch to the "region" folder
        File regionDir = new File(baseFolder, "region");
        if(!regionDir.exists()){
            regionDir= new File(baseFolder,"DIM1/region");
        }
        if(!regionDir.exists()){
            regionDir= new File(baseFolder,"DIM-1/region");
        }

        // Create a filter to only include mcr-files
        FileFilter mcrFiles = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().toLowerCase().endsWith("mcr")||pathname.getName().toLowerCase().endsWith("mca")) {
                    return true;
                }
                return false;
            }
        };
        // Find all region files
        File[] files = regionDir.listFiles(mcrFiles);
        if (files == null) {
            return null;
        }
        ArrayList<RegionFile> result = new ArrayList<RegionFile>();
        for (int i = 0; i < files.length; i++) {
            result.add(new RegionFile(files[i]));
        }

        return result;
    }

    /*public static ArrayList<PlayerFile> getDatFiles(File baseFolder)
            throws IOException {
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
                    result.add(new PlayerFile(files[i],files[i].getName(),"rw"));
                }
            }
        }
        if(levelDat.exists()){
            result.add (new PlayerFile(levelDat,"level.dat","rw"));
        }

        return result;
    }*/
}
