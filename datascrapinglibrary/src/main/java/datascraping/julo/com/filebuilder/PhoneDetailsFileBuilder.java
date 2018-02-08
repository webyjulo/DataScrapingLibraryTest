package datascraping.julo.com.filebuilder;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class PhoneDetailsFileBuilder extends FileBuilder {

    public static void buildPhoneDetails(File folder, Map<String, String> m, String fileName) {
        if (folder != null && m != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            try {
                fw = new FileWriter(filename);
                Set<String> keys = m.keySet();
                if (keys != null) {
                    List<String> keyList = new ArrayList(keys); // need List type to check if last column
                    for (String s : keyList) {
                        fw.append(s);
                        if (keyList.indexOf(s) < keyList.size() - 1) // if not last column
                            fw.append(DELIMITER);
                    }
                    fw.append('\n');

                    for (String s : keyList) {
                        appendEscapedString(fw, m.get(s));
                        if (keyList.indexOf(s) < keyList.size() - 1) // if not last column
                            fw.append(DELIMITER);
                    }
                }

                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }  else {
            logFlurryMsg("file_builder", "buildPhoneDetails: folder or m is null");
        }
    }
}
