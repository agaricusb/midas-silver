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

package nbt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TagInputStream extends DataInputStream {

	public TagInputStream(InputStream in) {
		super(in);
	}
	
	/**
	 * Read an NBT Tag/Tree from an InputStream
	 * @param named true, if it is a named Tag
	 * @return An NBT Tag/Tree
	 * @throws IOException
	 */
	public Tag readTag(boolean named) throws IOException {
		Tag result = null;
		byte firstByte = readByte();
		result = Tag.createTagFromID(firstByte);
		if (result != null) {
			result.readValue(this, named);
		}
		return result;
	}

}
