package havocx42.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PopupFormatter extends Formatter {

	public PopupFormatter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		sb.append(record.getMessage());
		sb.append("\nSee log for more details");
		return sb.toString();
	}

}
