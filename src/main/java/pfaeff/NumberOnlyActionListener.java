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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;

public class NumberOnlyActionListener implements ActionListener {
	
	private ArrayList<String> exceptions;
	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	
	public NumberOnlyActionListener(ArrayList<String> exceptions, int minValue, int maxValue) {
		this.exceptions = exceptions;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox cb = (JComboBox)e.getSource();
			String item = (String)cb.getSelectedItem();
			
			if (exceptions.contains(item)) {
				return;
			}
			
			try {
				int val = Integer.parseInt(item);
				if ((val > maxValue) || (val < minValue)) {
					cb.setSelectedIndex(0);
				}
			} catch(NumberFormatException ex) {
				//cb.setSelectedIndex(0);
			}
		}
	}

}
