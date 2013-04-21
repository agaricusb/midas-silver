package havocx42.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PopupHandler extends Handler {
    JFrame    jFrame;

    public PopupHandler(JFrame jFrame) {
        this.jFrame = jFrame;
        setFormatter(new PopupFormatter());
        this.setLevel(Level.WARNING);
    }

    @Override
    public void close() throws SecurityException {
        // TODO Auto-generated method stub

    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            JOptionPane.showMessageDialog(jFrame, getFormatter().format(record), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

}
