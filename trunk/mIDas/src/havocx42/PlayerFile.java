package havocx42;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class PlayerFile extends RandomAccessFile {
	
	private String name;

	public PlayerFile(File file, String name, String mode) throws FileNotFoundException {
		super(file, mode);
		this.name = name;
	}
	
	public String getName(){
		return name;
	}

}
