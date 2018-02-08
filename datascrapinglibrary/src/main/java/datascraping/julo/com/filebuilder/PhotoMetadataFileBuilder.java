package datascraping.julo.com.filebuilder;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class PhotoMetadataFileBuilder extends FileBuilder {

    public static void buildPhotoMetadataDetails(File folder, File[] listOfFiles, String fileName) {
        if (folder != null && listOfFiles != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            List<String> cols = new ArrayList<String>();
            cols.add("Name");
            cols.add("File Bytes");
            cols.add("Type");
            cols.add("Timestamp");
            try {
                fw = new FileWriter(filename);
                for (String s : cols) {
                    fw.append(s);
                    if (cols.indexOf(s) < cols.size() - 1) // if not last column
                        fw.append(DELIMITER);
                }
                fw.append('\n');

                for (File f : listOfFiles) {
                    if (f.isFile()) {
                        appendEscapedString(fw, f.getName());
                        fw.append(DELIMITER);

                        appendEscapedString(fw, Long.toString(f.length()));
                        fw.append(DELIMITER);

                        appendEscapedString(fw, extractExtention(f.getName()));
                        fw.append(DELIMITER);

                        appendEscapedString(fw, Long.toString(f.lastModified()));

                        fw.append('\n');
                    }
                }
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "buildPhotoMetadataDetails: folder or listOfFiles is null");
        }
    }

    private static String extractExtention(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }
}
