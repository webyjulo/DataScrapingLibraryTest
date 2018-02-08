package datascraping.julo.com.filebuilder;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class PhoneBookFileBuilder extends FileBuilder {

    public static void buildPhoneBook(File folder, Cursor cur, ContentResolver cr, String fileName) {
        if (folder != null && cur != null && cr != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            try {
                fw = new FileWriter(filename);
                fw.append("id");
                fw.append(DELIMITER);

                fw.append("Name");
                fw.append(DELIMITER);

                fw.append("Phone Number");
                fw.append(DELIMITER);

                fw.append("Type");

                fw.append('\n');

                if (cur != null && cur.getCount() > 0) {
                    while (cur.moveToNext()) {
                        if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) == 1) {
                            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            try {
                                int i = 0;
                                int pCount = pCur.getCount();
                                String[] phoneNum = new String[pCount];
                                String[] phoneType = new String[pCount];
                                while (pCur != null && pCur.moveToNext()) {
                                    phoneNum[i] = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    phoneType[i] = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                    appendEscapedString(fw, id);
                                    fw.append(DELIMITER);
                                    appendEscapedString(fw, name);
                                    fw.append(DELIMITER);
                                    appendEscapedString(fw, phoneNum[i]);
                                    fw.append(DELIMITER);
                                    appendEscapedString(fw, phoneType[i]);
                                    fw.append('\n');
                                    i++;
                                }
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                            } finally {
                                if (null != pCur) {
                                    pCur.close();
                                }
                            }
                        }

                    }
                }
                cur.close();
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "buildPhoneBook: folder or cursor or ContentResolver is null");
        }
    }
}
