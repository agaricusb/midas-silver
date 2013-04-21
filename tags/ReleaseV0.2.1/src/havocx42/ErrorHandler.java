package havocx42;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ErrorHandler {
    static File errorLog;

    /**
     * Logs an error to a file in the running directory
     * 
     * @param e An exception to have its stacktrace printed to the error file
     */
    public static boolean logError(Exception e) {
        e.printStackTrace();
        try {
            if (!initErrorFile()) {
                return false;
            }
            if (errorLog != null && errorLog.exists()) {
                PrintStream writer;
                writer = new PrintStream(errorLog);
                e.printStackTrace(writer);
                writer.close();
                
                return true;
            } else {
                return false;
            }
        } catch (IOException e1) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Logs an error to a file in the running directory
     * 
     * @param s A String to be recorded in the error file
     */
    public static boolean logError(String s) {
        System.out.println(s);
        try {
            if (!initErrorFile()) {
                return false;
            }
            if (errorLog != null && errorLog.exists()) {
                PrintStream writer;
                writer = new PrintStream(new FileOutputStream(errorLog,true));
                writer.append(s+System.getProperty("line.separator"));
                writer.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean initErrorFile() throws IOException{
        if (errorLog == null) {
            Calendar date = Calendar.getInstance();
            SimpleDateFormat dateformatter = new SimpleDateFormat(
                    "yyyyMMddhhmmss");
            String name = "errorlog" + dateformatter.format(date.getTime())
                    + ".txt";
            File tempLog = new File(name);
            if (!tempLog.createNewFile()){
                return false;
            }
            errorLog = tempLog;
        }
        return true;    
    }

}
