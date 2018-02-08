package datascraping.julo.com.filebuilder;

import android.database.Cursor;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by vdarmadi on 9/25/16.
 */
public abstract class FileBuilder {
    public static final String DELIMITER = ",";

    protected static String getValue(Cursor managedCursor, int columnIndex) {
        if (columnIndex >= 0) {
            return managedCursor.getString(columnIndex);
        }
        return null;
    }

    protected static String formatDate(String date) {
        if (date == null) return null;
        Date smsDayTime = new Date(Long.valueOf(date));
        return smsDayTime.toString();
    }

    protected static void appendEscapedString(FileWriter fw, String input) throws IOException {
        String escaped = StringEscapeUtils.escapeCsv(input);
        if (escaped != null && escaped.length() > 0 && escaped.charAt(0) == '"') {
            fw.append(escaped);
        } else {
            fw.append("\"");
            fw.append(escaped);
            fw.append("\"");
        }
    }
}
