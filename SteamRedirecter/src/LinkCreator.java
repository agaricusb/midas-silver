import java.io.*;
import java.util.*;
import java.util.concurrent.Future;

public class LinkCreator {
	public static void create(File source, File target) throws InterruptedException, IOException {
		
			if (!(source.exists() && target.exists() && source.isDirectory() && target
					.isDirectory())) {
				throw new IOException("Source or target is not a directory");
			}

			StringBuilder builder = new StringBuilder();
			Set<File> targetFiles = new HashSet<File>(Arrays.asList(target
					.listFiles()));
			Set<File> sourceFiles = new HashSet<File>(Arrays.asList(source
					.listFiles()));

			targetFiles.removeAll(sourceFiles);

			String[] cmd = new String[3];
			cmd[0] = "cmd.exe";
			cmd[1] = "/C";

			Runtime rt = Runtime.getRuntime();
			for (File f : targetFiles) {
				
				String dir = f.isDirectory()?"/D ":"";
				
				cmd[2]="mklink "+dir+"\""+source+"\\"+f.getName()+"\" \""+f.getAbsolutePath()+"\"";
				
				builder.append("Execing " + cmd[0] + " " + cmd[1] + " "
						+ cmd[2]);
				Process proc = rt.exec(cmd);
				// any error message?
				StreamGobbler errorGobbler = new StreamGobbler(
						proc.getErrorStream(), "ERROR: ");

				// any output?
				StreamGobbler outputGobbler = new StreamGobbler(
						proc.getInputStream(), "OUTPUT: ");

				// kick them off
				errorGobbler.start();
				outputGobbler.start();

				// any error???
				int exitVal = proc.waitFor();
				System.out.println("ExitValue: " + exitVal);
			}
	}
}