package datascraping.julo.com.datascrapinglibrary;

import android.Manifest;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import datascraping.julo.com.filebuilder.BrowserDataBuilder;
import datascraping.julo.com.filebuilder.CalendarFileBuilder;
import datascraping.julo.com.filebuilder.CallFileBuilder;
import datascraping.julo.com.filebuilder.GmailFileBuilder;
import datascraping.julo.com.filebuilder.InstalledAppFileBuilder;
import datascraping.julo.com.filebuilder.PhoneBookFileBuilder;
import datascraping.julo.com.filebuilder.PhoneDetailsFileBuilder;
import datascraping.julo.com.filebuilder.PhotoMetadataFileBuilder;
import datascraping.julo.com.filebuilder.SmsFileBuilder;
import datascraping.julo.com.util.AppConstant;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;

/**
 * Created by webster on 02/12/17.
 */

public class JuloDataScrapeRunner {

    private static JuloDataScrapeRunner instance = null;

    protected JuloDataScrapeRunner() {
        // Exists only to defeat instantiation.
    }
    public static JuloDataScrapeRunner getInstance() {
        if(instance == null) {
            instance = new JuloDataScrapeRunner();
        }
        return instance;
    }

    public static void runDataScraping(final Context activity) {
        if (activity != null) {
            logFlurryMsg("data_scrape_runner", "runDataScraping: start");

            File folder = new File(activity.getFilesDir() + activity.getResources().getString(R.string.data_scrap_folder));
            if (!folder.exists()) {
                folder.mkdir();
            }
            for (File file: folder.listFiles()) if (!file.isDirectory()) file.delete(); // delete all files inside "CSVFiles" folder

            // calls
            extractCallDetails(folder, activity);

            // sms
            extractSmsDetails(folder, activity);

            // app data
            extractInstalledApp(folder, activity);

            // phone details
            extractPhoneDetails(folder, activity);

            // Gmail contacts
            extractGmailContacts(folder, activity);

            // phone book
            extractPhoneBook(folder, activity);

            // calendar
            extractCalendarDetails(folder, activity);

            // photos
            extractPhotoMetadataDetails(folder, activity);

            // whatsapp media
            extractWhatsappMediaDetails(folder, activity);

            // whatsapp database
            extractWhatsappDatabaseDetails(folder, activity);

            // browser data
            extractBrowserData(folder, activity);

            List<String> filesTmp = new ArrayList<>();
            File[] flist = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".txt");
                }
            });
            for (File file : flist) {
                if (file.isFile()) {
                    filesTmp.add(file.getAbsolutePath());
                }
            }
            Object[] objects = filesTmp.toArray();
            String[] files = Arrays.copyOf(objects, objects.length,String[].class);
            zip(files,  folder.toString() + "/" + activity.getResources().getString(R.string.data_scrap_file));

            logFlurryMsg("data_scrape_runner", "runDataScraping: end");
        } else {
            logFlurryMsg("data_scrape_runner", "runDataScraping: activity is null");
        }
    }

    private static void extractPhotoMetadataDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractPhotoMetadataDetails: start");
        try {
            File photoFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + "Camera");
            File[] listOfFiles = photoFolder.listFiles();
            PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_photo_video_file));
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractPhotoMetadataDetails: end");
    }

    private static void extractBrowserData(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractBrowserData: start");

        String[] projection = new String[]{
                AppConstant.TITLE_COL, AppConstant.URL_COL, AppConstant.CREATED_COL, AppConstant.DATE_COL
                , AppConstant.VISITS_COL, AppConstant.BOOKMARKS_COL
        };

        // default browser
        try {
            Cursor managedCursor = activity.getContentResolver().query(Uri.parse(AppConstant.DEFAULT_BROWSER_URI), projection, null, null, null);
            BrowserDataBuilder.buildBrowserHistory(folder, managedCursor, activity.getResources().getString(R.string.data_scrap_default_browser_file));
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        // chrome
        try {
            Cursor managedCursor = activity.getContentResolver().query(Uri.parse(AppConstant.CHROME_BROWSER_URI), projection, null, null, null);
            BrowserDataBuilder.buildBrowserHistory(folder, managedCursor, activity.getResources().getString(R.string.data_scrap_chrome_browser_file));
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        logFlurryMsg("data_scrape_runner", "extractBrowserData: end");
    }



    private static void extractWhatsappDatabaseDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractWhatsappDatabaseDetails: start");
        try {
            // database
            File whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Databases/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_db_file));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractWhatsappDatabaseDetails: end");
    }

    private static void extractWhatsappMediaDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractWhatsappMediaDetails: start");

        try {
            // images
            File whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_photo_file));
            }

            // images sent
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images/Sent/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_photo_sent_file));
            }

            // videos
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Video/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_video_file));
            }

            // videos sent
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Video/Sent/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_video_sent_file));
            }

            // animated gifs
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Animated Gifs/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_agifs_file));
            }

            // animated gifs sent
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Animated Gifs/Sent/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_agifs_sent_file));
            }

            // WhatsApp Audio
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_audio_file));
            }

            // WhatsApp Audio sent
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/Sent/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_audio_sent_file));
            }

            // WhatsApp Documents
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Documents/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_audio_file));
            }

            // WhatsApp Documents sent
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Documents/Sent/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_audio_sent_file));
            }

            // WhatsApp Profile Photos
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Profile Photos/");
            if (whatsappFolder.exists()) {
                File[] listOfFiles = whatsappFolder.listFiles();
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_pphoto_file));
            }

            // WhatsApp Voice Notes
            whatsappFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Voice Notes/");
            if (whatsappFolder.exists()) {
                ArrayList<File> listOfFilesArrList = new ArrayList<File>();
                walk(whatsappFolder.getAbsolutePath(), listOfFilesArrList);
                File[] listOfFiles = listOfFilesArrList.toArray(new File[listOfFilesArrList.size()]);
                PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, activity.getResources().getString(R.string.data_scrap_whatsapp_voice_file));
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractWhatsappMediaDetails: end");
    }

    private static void walk( String path, ArrayList<File> listOfFiles) {
        if (path != null && listOfFiles != null) {
            try {
                File root = new File(path);
                File[] list = root.listFiles();

                if (list == null) return;

                for (File f : list) {
                    if (f.isDirectory()) {
                        walk(f.getAbsolutePath(), listOfFiles);
                    } else {
                        listOfFiles.add(f);
                    }
                }
            } catch (Exception e){
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("data_scrape_runner", "walk: path or listOfFiles is null");
        }
    }

    private static void extractInstalledApp(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractInstalledApp: start");
        try {
            final PackageManager pm = activity.getPackageManager();
            // get a list of installed apps.
            List<ApplicationInfo> packages = pm.getInstalledApplications(0);
            InstalledAppFileBuilder.buildInstalledApp(folder, packages, pm, activity.getResources().getString(R.string.data_scrap_app_file));
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractInstalledApp: end");
    }

    private static void extractSmsDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractSmsDetails: start");
        try {
            Uri uri = Uri.parse("content://sms");
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            SmsFileBuilder.buildSmsDetails(folder, cursor, activity.getResources().getString(R.string.data_scrap_sms_file));
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractSmsDetails: end");
    }

    private static void extractCalendarDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractCalendarDetails: start");
        try {
            Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            long now = System.currentTimeMillis();
            ContentUris.appendId(eventsUriBuilder, now - (1 * DateUtils.YEAR_IN_MILLIS));
            ContentUris.appendId(eventsUriBuilder, now + (1 * DateUtils.YEAR_IN_MILLIS));
            Uri eventsUri = eventsUriBuilder.build();
            Cursor cur1 = activity.getContentResolver().query(eventsUri, null, null, null, CalendarContract.Instances.DTSTART + " ASC");
            CalendarFileBuilder.buildCalendarDetails(folder, cur1, activity.getResources().getString(R.string.data_scrap_calendar_file));
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractCalendarDetails: end");
    }

    private static void extractCallDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractCallDetails: start");
        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Cursor managedCursor = activity.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            CallFileBuilder.buildCallDetail(folder, managedCursor, activity.getResources().getString(R.string.data_scrap_call_file));
        } catch(Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractCallDetails: end");
    }

    private static void extractGmailContacts(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractGmailContacts: start");
        try {
            GmailFileBuilder.buildGmailContacts(folder, activity.getContentResolver(), activity.getResources().getString(R.string.data_scrap_gmail_file));
        } catch(Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractGmailContacts: end");
    }

    private static void extractPhoneBook(File folder, Context activity){
        logFlurryMsg("data_scrape_runner", "extractPhoneBook: start");
        try {
            Cursor managedCursor = activity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            PhoneBookFileBuilder.buildPhoneBook(folder, managedCursor, activity.getContentResolver(), activity.getResources().getString(R.string.data_scrap_phone_book_file));
        } catch(Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractPhoneBook: end");
    }

    private static void extractPhoneDetails(File folder, Context activity) {
        logFlurryMsg("data_scrape_runner", "extractPhoneDetails: start");
        try {
            Map<String, String> m = new HashMap<String, String>();
            m.put("manufacturer", Build.MANUFACTURER);
            m.put("brand", Build.BRAND);
            m.put("product", Build.PRODUCT);
            m.put("model", Build.MODEL);
            m.put("Device", Build.DEVICE);
            m.put("Display", Build.DISPLAY);
            m.put("ID", Build.ID);
            m.put("Serial", Build.SERIAL);
            m.put("Type", Build.TYPE);
            m.put("User", Build.USER);
            m.put("version", System.getProperty("os.version"));
            m.put("OSAPILevel", android.os.Build.VERSION.RELEASE);
            m.put("Sdk", "" + android.os.Build.VERSION.SDK_INT);

            PhoneDetailsFileBuilder.buildPhoneDetails(folder, m, activity.getResources().getString(R.string.data_scrap_phone_detail_file));
        } catch(Exception e){
            Crashlytics.logException(e);
        }
        logFlurryMsg("data_scrape_runner", "extractPhoneDetails: end");
    }

    public static void zip(String[] files, String zipFile) {
        if (files != null && zipFile != null) {
            String[] _files = files;
            String _zipFile = zipFile;
            int BUFFER = 1024;

            try {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(_zipFile);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                byte data[] = new byte[BUFFER];
                for (int i = 0; i < _files.length; i++) {
                    FileInputStream fi = new FileInputStream(_files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("data_scrape_runner", "zip: files or zipFiles is null");
        }
    }
}

