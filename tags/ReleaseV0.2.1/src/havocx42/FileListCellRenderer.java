package havocx42;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class FileListCellRenderer extends JLabel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1815749193896485573L;
	public int maxIndex = -1;
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,int index, boolean isSelected, boolean cellHasFocus) {
		if (value==null)setText("");
		else setText(((File)value).getName());
		setOpaque(true);
		if ((index % 2) == 1) {
			setBackground(new Color(0.9f, 0.9f, 0.9f));
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
