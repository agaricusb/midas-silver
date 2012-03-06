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

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CBRenderer extends JLabel implements ListCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -646755438286846622L;
	public int maxIndex = -1;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,	int index, boolean isSelected, boolean cellHasFocus) {
		setText(((File)value).getName());
		setOpaque(true);
		if ((index % 2) == 1) {
			setBackground(new Color(0.6f, 1.0f, 0.6f));
		} else {
			setBackground(Color.white);
		}	
		
		if (isSelected) {
			setBackground(new Color(0.6f, 0.6f, 1.0f));
		}
		
		if (index > maxIndex) {
			setForeground(Color.red);
		} else {
			setForeground(Color.black);
		}
		return this;
	}

}
