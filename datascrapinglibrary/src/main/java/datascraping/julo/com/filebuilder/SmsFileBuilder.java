package datascraping.julo.com.filebuilder;

import android.database.Cursor;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class SmsFileBuilder extends FileBuilder {

    public static void buildSmsDetails(File folder, Cursor cursor, String fileName) {
        if (folder != null && cursor != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            try {
                fw = new FileWriter(filename);
                List<String> cols = new ArrayList<String>();
                String[] colArray = cursor.getColumnNames();
                if (colArray != null) {
                    List<String> colList = Arrays.asList(colArray); // need List type to check if last column
                    for (String s : colList) {
                        fw.append(s);
                        if (colList.indexOf(s) < colList.size() - 1) // if not last column
                            fw.append(DELIMITER);
                        cols.add(s);
                    }
                }
                fw.append('\n');

                if (cursor.moveToFirst()) {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        for (String col : cols) {
                            String value = getValue(cursor, cursor.getColumnIndex(col));
                            if (col.equals("type")) {
                                value = formatType(value);
                            }
                            appendEscapedString(fw, value);
                            if(cols.indexOf(col) < cols.size() - 1) // if not last column
                                fw.append(DELIMITER);
                        }
                        fw.append('\n');
                        cursor.moveToNext();
                    }
                }
                cursor.close();
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "buildSmsDetails: folder or cursor is null");
        }
    }

    private static String formatType(String value) {
        switch (Integer.parseInt(value)) {
            case 1:
                value = "INBOX";
                break;

            case 2:
                value = "SENT";
                break;

            case 3:
                value = "DRAFT";
                break;
        }
        return value;
    }
}
