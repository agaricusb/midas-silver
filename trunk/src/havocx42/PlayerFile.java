package havocx42;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.mojang.nbt.*;

import pfaeff.IDChanger;

public class PlayerFile extends File {
	
	private String name;

	public PlayerFile(String location,String name) throws FileNotFoundException {
		super(location);
		this.name = name;
	}
	
	public String getName(){
		return name;
	}



}
