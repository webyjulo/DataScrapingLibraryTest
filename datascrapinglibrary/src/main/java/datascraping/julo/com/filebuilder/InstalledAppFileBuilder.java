package datascraping.julo.com.filebuilder;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class InstalledAppFileBuilder extends FileBuilder {

    public static void buildInstalledApp(File folder, List<ApplicationInfo> packages, PackageManager pm, String fileName) {
        if (folder != null && packages != null && pm != null) {
            // loop through the list of installed packages and see if the selected
            // app is in the list
            try {
                FileWriter fw;
                if (!folder.exists()) {
                    folder.mkdir();
                }
                final String filename = folder.toString() + "/" + fileName;
                fw = new FileWriter(filename, true);
                fw.append("Application ID");
                fw.append(DELIMITER);

                fw.append("Application Name");
                fw.append(DELIMITER);

                fw.append("Application Package Name");
                fw.append(DELIMITER);

                fw.append("Icon");
                fw.append(DELIMITER);

                fw.append("Received");
                fw.append(DELIMITER);

                fw.append("Send");
                fw.append(DELIMITER);

                fw.append("Total");
                fw.append(DELIMITER);

                fw.append("Install Time Millis");
                fw.append(DELIMITER);

                fw.append("Last Updated Millis");

                fw.append('\n');

                for (ApplicationInfo packageInfo : packages) {
                    // get the UID for the selected app
                    int UID = packageInfo.uid;
                    String package_name = packageInfo.packageName;
                    ApplicationInfo app = null;
                    try {
                        app = pm.getApplicationInfo(package_name, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Crashlytics.logException(e);
                    }
                    long installTimeMillis = pm.getPackageInfo(package_name, 0).firstInstallTime;
                    String appFile = packageInfo.sourceDir;
                    long lastUpdatedMillis = new File(appFile).lastModified();
                    String name = (String) pm.getApplicationLabel(app);
                    Drawable icon = pm.getApplicationIcon(app);
                    // internet usage for particular app(sent and received)
                    double received = (double) TrafficStats.getUidRxBytes(UID) / (1024 * 1024);
                    double send = (double) TrafficStats.getUidTxBytes(UID) / (1024 * 1024);
                    double total = received + send;

                    try {
                        appendEscapedString(fw, "" + UID);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, name);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, package_name);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + icon);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + received);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + send);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + total);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + installTimeMillis);
                        fw.append(DELIMITER);

                        appendEscapedString(fw, "" + lastUpdatedMillis);

                        fw.append('\n');

                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "buildInstalledApp: folder or packages or pm is null");
        }
    }
}
