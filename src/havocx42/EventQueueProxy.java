package havocx42;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class EventQueueProxy extends EventQueue {

    private Logger    logger    = Logger.getLogger(this.getClass().getName());

    @Override
    protected void dispatchEvent(AWTEvent newEvent) {
        try {
            super.dispatchEvent(newEvent);
        } catch (Throwable t) {
            t.printStackTrace();
            String message = t.getMessage();

            if (message == null || message.length() == 0) {
                message = "Fatal: " + t.getClass();
            }

            logger.log(Level.SEVERE,"Fatal error: "+t.getMessage(),t);
        }
    }

}
