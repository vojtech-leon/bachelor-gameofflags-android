
package vojtele1.gameofflags.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import vojtele1.gameofflags.R;

/**
 * Created by NB on 4.7.2016.
 */
public class RetryingSender {
    Activity activity;
    protected boolean knowResponse;
    protected boolean knowAnswer;
    protected int counterError;
    RequestQueue requestQueue;
    AlertDialog alertDialog;
    /**
     * Pro zjisteni, jestli progress dialog v showProgressDialogLoading() bezi
     * a umoznuje jeho ukonceni.
     */
    protected ProgressDialog progressDialog;

    public RetryingSender(Activity activity) {
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(activity.getApplicationContext());
    }

    protected void showLoadingProgress() {
        progressDialog = new ProgressDialog(activity);

        progressDialog.show();

        progressDialog.setCancelable(false);

        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        progressDialog.setContentView(R.layout.progress_dialog_loading);
    }
    protected void hideLoadingProgress() {
        progressDialog.dismiss();
    }

    protected void showInfoDialog(String text, final boolean finishActivity) {
        alertDialog =  new AlertDialog.Builder(activity)
                .setMessage(text)
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (finishActivity) activity.finish();
                    }
                })
                .show();
    }
    public void start2(final boolean finish) {
        new Thread() {
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (progressDialog == null || !progressDialog.isShowing()) {
                            counterError = 0;
                            showLoadingProgress();
                            requestQueue.add(send());
                        }
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (knowResponse) {
                                    System.out.println("Pocet chyb: " + counterError);
                                    if (counterError >= 200) {
                                        showInfoDialog("Problém s připojením k databázi, zkuste to prosím později.", finish);
                                        hideLoadingProgress();
                                    } else if (!knowAnswer) {
                                        requestQueue.add(send());
                                        start2(finish);
                                    } else {
                                        hideLoadingProgress();
                                        System.out.println("Loading dokoncen.");
                                    }
                                } else {
                                    start2(finish);
                                }
                            }
                        }, 100);

                    }
                });
            }
        }.start();

    }
    public void start() {
        start2(false);
    }
    protected CustomRequest send() {
        knowResponse = false;
        knowAnswer = false;
        return null;
    }
}
