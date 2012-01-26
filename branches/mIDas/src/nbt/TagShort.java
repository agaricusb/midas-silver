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

import java.io.IOException;


public class TagShort extends Tag {
	
	public short payload;

	@Override
	public void readValue(TagInputStream input, boolean named) throws IOException {
		super.readValue(input, named);
		payload = input.readShort();
	}

	@Override
	public void writeValue(TagOutputStream output, boolean named) throws IOException {
		super.writeValue(output, named);
		output.writeShort(payload);
	}

	@Override
	public int getType() {
		return Tag.TAG_Short;
	}

}
