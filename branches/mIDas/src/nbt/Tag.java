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

public abstract class Tag {
	/**
	 * Constant values to identify the tag type
	 */	
	public static final int TAG_End 		= 0;
	public static final int TAG_Byte 		= 1;
	public static final int TAG_Short 		= 2;
	public static final int TAG_Int 		= 3;
	public static final int TAG_Long		= 4;
	public static final int TAG_Float 		= 5;
	public static final int TAG_Double		= 6;
	public static final int TAG_Byte_Array	= 7;
	public static final int TAG_String		= 8;
	public static final int TAG_List		= 9;
	public static final int TAG_Compound	= 10;

	/**
	 * Tag Attributes
	 */
	protected String name = "";
	protected ArrayList<Tag> children;
	
	/**
	 * Creates a new Tag of type tagID
	 */
	public static Tag createTagFromID(byte tagID) {
		Tag result = null;		
		switch (tagID) {
		case Tag.TAG_End: {
			break;
		}
		case TAG_Byte: {
			result = new TagByte();
			break;
		}
		case TAG_Short: {
			result = new TagShort();
			break;
		}
		case TAG_Int: {
			result = new TagInt();
			break;			
		}
		case TAG_Long: {
			result = new TagLong();
			break;			
		}		
		case TAG_Float: {
			result = new TagFloat();
			break;			
		}	
		case TAG_Double: {
			result = new TagDouble();
			break;					
		}	
		case TAG_Byte_Array: {
			result = new TagByteArray();
			break;					
		}		
		case TAG_String: {
			result = new TagString();
			break;					
		}		
		case TAG_List: {
			result = new TagList();
			break;					
		}	
		case TAG_Compound: {
			result = new TagCompound();
			break;					
		}		
		}		
		return result;
	}
	
	/**
	 * Get the name of this tag
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Finds a child node by its name.
	 * @param name name that is to be searched for
	 * @param recursive true to perform a depth-first search
	 * @return
	 */
	public Tag findChildByName(String name, boolean recursive) {
		if (children != null) {
			for (Tag t : children) {
				if (t.name != null) {
					if (t.name.equals(name)) {
						return t;
					}
				}
				if (recursive) {
					Tag result = t.findChildByName(name, true);
					if (result != null) {
						return result;
					}
				}				
			}
		}
		return null;
	}
	
	/**
	 * Finds all children with a specific name
	 * 
	 * @param result stores the resulting list 
	 * @param name name that is to be searched for
	 * @param recursive
	 */
	public void findAllChildrenByName(ArrayList<Tag> result, String name, boolean recursive) {
		if (result == null) {
			result = new ArrayList<Tag>();
		}
		if (children != null) {
			for (Tag t : children) {
				if (t.name != null) {
					if (t.name.equals(name)) {
						result.add(t);
					}
				}
				if (recursive) {
					t.findAllChildrenByName(result, name, true);
				}				
			}
		}
	}	
	
	/**
	 * Get the type of this tag
	 * 
	 * @return
	 */
	public abstract int getType();
	
	/**
	 * Is called when a tag is being read
	 * 
	 * @param input
	 * @param named true if the tag is a named tag. false otherwise
	 * @throws IOException
	 */
	public void readValue(TagInputStream input, boolean named) throws IOException {
		if (named) {
			name = input.readUTF();
		} else {
			name = null;
		}		
	}
	
	/**
	 * Is called when a tag is being written
	 * 
	 * @param output
	 * @param named true if the tag is a named tag. false otherwise
	 * @throws IOException
	 */
	public void writeValue(TagOutputStream output, boolean named) throws IOException {		
		if ((named) && (name != null)) {
			output.writeUTF(name);
		} 
	}
	
	/**
	 * Print the structure of the NBT file
	 * @param depth current tree depth
	 */
	public void print(int depth) {
		for (int i=1; i < depth; i++) {
			System.out.print("\t");
		}		
		System.out.println(name);
		if (children != null) {
			for (Tag t : children) {
				t.print(depth + 1);
			}
		}
	}
}
