package vojtele1.gameofflags.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * Created by NB on 4.7.2016.
 */
public class BaseActivity extends AppCompatActivity {
    AlertDialog alertDialog;

    protected void showInfoDialog(String text) {
        alertDialog =  new AlertDialog.Builder(this)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    protected void showInfoDialog(String text, DialogInterface.OnClickListener onClickListener) {
        alertDialog =  new AlertDialog.Builder(this)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", onClickListener)
                .show();
    }
    protected void showInfoDialog(String text, final boolean finishActivity) {
        alertDialog =  new AlertDialog.Builder(this)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finishActivity) finish();
                    }
                })
                .show();
    }
    protected void showInfoDialog(String title, String text) {
        alertDialog =  new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    protected void showInfoDialog(String title, String text, final boolean finishActivity) {
        alertDialog =  new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finishActivity) finish();
                    }
                })
                .show();
    }

    /**
     * upraveny string podle SimpleDateFormat("yyyy-MM-dd HH:mm:ss") a v UTC
     * @param string
     * @return Date
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    protected Date stringToDate(String string) throws ParseException {
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
    protected String dateToString(Date date) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(date);
    }

    /**
     * upraveny object podle SimpleDateFormat("dd. MM. yyyy HH:mm:ss")
     * @param object
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    protected String objectToString(Object object) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(object);
    }

    /**
     * upraveny date podle SimpleDateFormat("yyyy-MM-dd HH:mm:ss") pro serverove ucely
     * @param date
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    protected String dateToStringServer(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nastavi cas na UTC
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
