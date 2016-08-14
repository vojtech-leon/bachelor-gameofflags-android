package vojtele1.gameofflags.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * trida obsahujici praci s datem
 * Created by Leon on 10.8.2016.
 */
public class FormatDate {

    /**
     * upraveny string podle SimpleDateFormat("yyyy-MM-dd HH:mm:ss") a v UTC
     * @param string
     * @return Date
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    public static Date stringToDate(String string) throws ParseException {
        SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nastavi prijaty cas na UTC
        sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdfPrijaty.parse(string);
    }

    /**
     * upraveny date podle SimpleDateFormat("dd. MM. yyyy HH:mm:ss") pro bezne pouziti
     * @param date
     * @return string
     */
    @SuppressLint("SimpleDateFormat")
    public static String dateToString(Date date) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(date);
    }

    /**
     * upraveny object podle SimpleDateFormat("dd. MM. yyyy HH:mm:ss")
     * @param object
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    public static String objectToString(Object object) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(object);
    }

    /**
     * upraveny date podle SimpleDateFormat("yyyy-MM-dd HH:mm:ss") pro serverove ucely
     * @param date
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    public static String dateToStringServer(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nastavi cas na UTC
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
