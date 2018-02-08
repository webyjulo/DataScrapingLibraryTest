package datascraping.julo.com.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.flurry.android.FlurryAgent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vdarmadi on 1/18/17.
 */

public class DataScrapeLogger {

    /**
     * converts printstacktrace to string that can be logged anywhere.
     *
     * @param e Exception
     * @return result of stacktrace conversion
     */
    public static String stackTraceToString(Exception e) {
        try {
            if (e != null) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return sw.toString();
            } else {
                return "";
            }
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Send log to Flurry with given message.
     *
     * @param frag_key this is the category tag shown on Flurry event log
     * @param msg custom message
     */
    public static void logFlurryMsg(String frag_key, String msg){
        Map<String, String> map = new LinkedHashMap<>();
        map.put("msg", msg);
        FlurryAgent.logEvent(frag_key, map, false);
        Answers.getInstance().logCustom(new CustomEvent(frag_key).putCustomAttribute("msg", msg));
    }
}
