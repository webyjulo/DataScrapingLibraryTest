package datascraping.julo.com.filebuilder;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

public class GmailFileBuilder extends FileBuilder {

    public static void buildGmailContacts(File folder, ContentResolver cr, String fileName) {

        if (folder != null && cr != null) {

            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;

            try {
                fw = new FileWriter(filename);
                fw.append("Contact Id");
                fw.append(DELIMITER);

                fw.append("Name");
                fw.append(DELIMITER);

                fw.append("Email Address");
                fw.append(DELIMITER);

                fw.append("Type");

                fw.append('\n');

                Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);
                try {
                    while (cursor != null && cursor.moveToNext()) {

                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                        String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        String emailType = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY));
                        if (name == null) {
                            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME));
                        }
                        appendEscapedString(fw, id);
                        fw.append(DELIMITER);
                        appendEscapedString(fw, name);
                        fw.append(DELIMITER);
                        appendEscapedString(fw, email);
                        fw.append(DELIMITER);
                        appendEscapedString(fw, emailType);
                        fw.append('\n');
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }


                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "buildPhoneBook: folder or cursor or ContentResolver is null");
        }
    }
}
