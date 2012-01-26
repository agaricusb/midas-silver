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
import java.util.ArrayList;


public class TagList extends Tag {

	@Override
	public void readValue(TagInputStream input, boolean named) throws IOException {
		super.readValue(input, named);
		byte tagID = input.readByte();
		int length = input.readInt();
		children = new ArrayList<Tag>();
		for (int i = 0; i < length; i++) {
			Tag tag = Tag.createTagFromID(tagID);
			if (tag != null) {
				tag.readValue(input, false);
				children.add(tag);
			}
		}
	}

	@Override
	public void writeValue(TagOutputStream output, boolean named) throws IOException {
		super.writeValue(output, named);	
		if ((children != null) && (children.size() > 0)) {
			output.writeByte(children.get(0).getType());
			output.writeInt(children.size());
			for (Tag t : children) {
				t.writeValue(output, false);
			}
		} else {
			output.writeByte(Tag.TAG_Byte);
			output.writeInt(0);
		}
	}

	@Override
	public int getType() {
		return Tag.TAG_List;
	}

}
