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

    @SuppressLint("SimpleDateFormat")
    protected Date stringToDate(String string) throws ParseException {
        SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // nastavi prijaty cas na UTC
        sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdfPrijaty.parse(string);
    }

    @SuppressLint("SimpleDateFormat")
    protected String dateToString(Date date) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(date);
    }
    @SuppressLint("SimpleDateFormat")
    protected String dateToString(Object object) {
        SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
        return sdfVysledny.format(object);
    }
}
