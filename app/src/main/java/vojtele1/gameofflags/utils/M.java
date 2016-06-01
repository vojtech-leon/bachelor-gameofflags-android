package vojtele1.gameofflags.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import vojtele1.gameofflags.R;

/**
 * vsechny metody, ktere si nezaslouzi vlastni tridu
 * Created by Leon on 01.06.2016.
 */
public class M {
    /**
     * Pro zjisteni, jestli progress dialog v showProgressDialogLoading() bezi
     * a umoznuje jeho ukonceni.
     */
    public static ProgressDialog progressDialog;



    /**
     * Zjisti, zdali je Poloha zapnuta
     * @param context
     * @return true/false
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static ProgressDialog showProgressDialogLoading(Context context) {

        progressDialog = new ProgressDialog(context);

        progressDialog.show();

        progressDialog.setCancelable(false);

        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        progressDialog.setContentView(R.layout.progress_dialog_loading);

        return progressDialog;
    }
}
