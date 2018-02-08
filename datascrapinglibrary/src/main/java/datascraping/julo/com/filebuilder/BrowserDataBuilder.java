package datascraping.julo.com.filebuilder;

import android.database.Cursor;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class BrowserDataBuilder extends FileBuilder {

    public static void buildBrowserHistory(File folder, Cursor managedCursor, String fileName) {
        if (folder != null && managedCursor != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            try {
                fw = new FileWriter(filename);
                fw.append("Title");
                fw.append(DELIMITER);

                fw.append("Url");
                fw.append(DELIMITER);

                fw.append("Bookmark");
                fw.append(DELIMITER);

                fw.append("Visits");
                fw.append(DELIMITER);

                fw.append("Created");
                fw.append(DELIMITER);

                fw.append("Date");

                fw.append('\n');

                int title = managedCursor.getColumnIndex("title");
                int url = managedCursor.getColumnIndex("url");
                int bookmark = managedCursor.getColumnIndex("bookmark");
                int visits = managedCursor.getColumnIndex("visits");
                int created = managedCursor.getColumnIndex("created");
                int date = managedCursor.getColumnIndex("date");

                while (managedCursor.moveToNext()) {
                    String titleStr = getValue(managedCursor, title);
                    String urlStr = getValue(managedCursor, url);
                    String bookmarkStr = getValue(managedCursor, bookmark);
                    String visitsStr = getValue(managedCursor, visits);
                    String createdStr = getValue(managedCursor, created);
                    String dateStr = getValue(managedCursor, date);

                    appendEscapedString(fw, titleStr);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, urlStr);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, bookmarkStr);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, visitsStr);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, createdStr);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, dateStr);

                    fw.append('\n');
                }
                managedCursor.close();
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } // folder or cursor is null ?
        else {
            logFlurryMsg("file_builder", "folder or cursor is null");
        }
    }
}
