package datascraping.julo.com.filebuilder;

import android.database.Cursor;
import android.provider.CallLog;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static datascraping.julo.com.util.DataScrapeLogger.logFlurryMsg;
import static datascraping.julo.com.util.DataScrapeLogger.stackTraceToString;

/**
 * Created by vdarmadi on 9/25/16.
 */
public class CallFileBuilder extends FileBuilder {

    public static void buildCallDetail(File folder, Cursor managedCursor, String fileName) {
        if (folder != null && managedCursor != null) {
            final String filename = folder.toString() + "/" + fileName;
            FileWriter fw;
            try {
                fw = new FileWriter(filename);
                fw.append("Name");
                fw.append(DELIMITER);

                fw.append("Phone Number");
                fw.append(DELIMITER);

                fw.append("Call Type");
                fw.append(DELIMITER);

                fw.append("Call Date");
                fw.append(DELIMITER);

                fw.append("Call duration in sec");
                fw.append(DELIMITER);

                fw.append("Number Label");
                fw.append(DELIMITER);

                fw.append("Number Type");
                fw.append(DELIMITER);

                fw.append("Presentation");
                fw.append(DELIMITER);

                fw.append("Readmess");
                fw.append(DELIMITER);

                fw.append("Date Time");
                fw.append(DELIMITER);

                fw.append("GeoCode Location");
                fw.append(DELIMITER);

                fw.append("Photo URI");
                fw.append(DELIMITER);

                fw.append("Lookup URI");
                fw.append(DELIMITER);

                fw.append("Country ISO");
                fw.append(DELIMITER);

                fw.append("Transcription");
                fw.append(DELIMITER);

                fw.append("Phone Account ID");
                fw.append(DELIMITER);

                fw.append("Photo ID");
                fw.append(DELIMITER);

                fw.append("Voicemail URI");
                fw.append(DELIMITER);

                fw.append("Features");
                fw.append(DELIMITER);

                fw.append("New");
                fw.append(DELIMITER);

                fw.append("Data Usage");
                fw.append(DELIMITER);

                fw.append("Phone Account Component Name");
                fw.append(DELIMITER);

                fw.append("Matched Number");
                fw.append(DELIMITER);

                fw.append("Normalized Number");
                fw.append(DELIMITER);

                fw.append("Formatted Number");

                fw.append('\n');

                //===================
                StringBuffer sb = new StringBuffer();
                int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
                int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                int cachedNumberLabel = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL);
                int cachedNumberType = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE);
                int numberPresentation = managedCursor.getColumnIndex(CallLog.Calls.NUMBER_PRESENTATION);
                int read = managedCursor.getColumnIndex(CallLog.Calls.IS_READ);
                int geocodeLocation = managedCursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION);
                int cachedFormattedNr = managedCursor.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER);
                int cachedPhotoUri = managedCursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI);
                int cachedLookupUri = managedCursor.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI);
                int countryIsoColIx = managedCursor.getColumnIndex(CallLog.Calls.COUNTRY_ISO);
                int transcriptionColIx = managedCursor.getColumnIndex(CallLog.Calls.TRANSCRIPTION);
                int phoneAccountIdColIx = managedCursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
                int cachedPhotoId = managedCursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_ID);
                int voicemailUriColIx = managedCursor.getColumnIndex(CallLog.Calls.VOICEMAIL_URI);
                int featuresColIx = managedCursor.getColumnIndex(CallLog.Calls.FEATURES);
                int newColIx = managedCursor.getColumnIndex(CallLog.Calls.NEW);
                int dataUsageColIx = managedCursor.getColumnIndex(CallLog.Calls.DATA_USAGE);
                int phoneAccountCompName = managedCursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME);
                int cachedMatchedNr = managedCursor.getColumnIndex(CallLog.Calls.CACHED_MATCHED_NUMBER);
                int cachedNormNr = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NORMALIZED_NUMBER);

                sb.append("Call Details :");
                while (managedCursor.moveToNext()) {
                    String NameCall = getValue(managedCursor, name);
                    String phNumber = getValue(managedCursor, number);
                    String callType = getValue(managedCursor, type);
                    String callDate = getValue(managedCursor, date);
                    String callDuration = getValue(managedCursor, duration);
                    String numberLabel = getValue(managedCursor, cachedNumberLabel);
                    String numberType = getValue(managedCursor, cachedNumberType);
                    String presentation = getValue(managedCursor, numberPresentation);
                    String isRead = getValue(managedCursor, read);
                    long  dateTimeMillis = System.currentTimeMillis();
                    String geoCodeLoc = getValue(managedCursor, geocodeLocation);
                    String photoUri = getValue(managedCursor, cachedPhotoUri);
                    String lookupUri = getValue(managedCursor, cachedLookupUri);
                    String countryIso = getValue(managedCursor, countryIsoColIx);
                    String transcription = getValue(managedCursor, transcriptionColIx);
                    String phoneAccountId = getValue(managedCursor, phoneAccountIdColIx);
                    String photoId = getValue(managedCursor, cachedPhotoId);
                    String voicemailUri = getValue(managedCursor, voicemailUriColIx);
                    String features = getValue(managedCursor, featuresColIx);
                    String newVar = getValue(managedCursor, newColIx);
                    String dataUsage = getValue(managedCursor, dataUsageColIx);
                    String phoneAccountComponentName = getValue(managedCursor, phoneAccountCompName);
                    String matchedNumber = getValue(managedCursor, cachedMatchedNr);
                    String normalizedNumber = getValue(managedCursor, cachedNormNr);
                    String formattedNr = getValue(managedCursor, cachedFormattedNr);
                    String dir = null;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }

                    appendEscapedString(fw, NameCall);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, phNumber);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, dir);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, callDate);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, callDuration);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, numberLabel);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, numberType);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, presentation);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, isRead);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, "" + dateTimeMillis);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, geoCodeLoc);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, photoUri);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, lookupUri);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, countryIso);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, transcription);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, phoneAccountId);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, photoId);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, voicemailUri);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, features);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, newVar);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, dataUsage);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, phoneAccountComponentName);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, matchedNumber);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, normalizedNumber);
                    fw.append(DELIMITER);

                    appendEscapedString(fw, formattedNr);

                    fw.append('\n');
                }
                managedCursor.close();
                fw.close();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        } else {
            logFlurryMsg("file_builder", "folder or cursor is null");
        }
    }
}
