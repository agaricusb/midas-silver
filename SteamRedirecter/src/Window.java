import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class Window {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private File source;
	private File target;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 324);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JButton btnSelectSource = new JButton("Select Source");
		btnSelectSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					source = fc.getSelectedFile();
					textField_1.setText(source.getAbsolutePath());
				}
			}
		});
		btnSelectSource.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(btnSelectSource);

		textField_1 = new JTextField();
		textField_1.setEditable(false);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);

		JButton btnSelectTarget = new JButton("Select Target");
		btnSelectTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					target = fc.getSelectedFile();
					textField.setText(target.getAbsolutePath());
				}
			}
		});
		btnSelectTarget.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(btnSelectTarget);

		textField = new JTextField();
		textField.setEditable(false);
		frame.getContentPane().add(textField);
		textField.setColumns(10);

		JButton btnRun = new JButton("Run");
		
		btnRun.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.getContentPane().add(btnRun);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					LinkCreator.create(source, target);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

}
