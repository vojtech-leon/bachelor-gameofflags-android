
package vojtele1.gameofflags.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import vojtele1.gameofflags.R;

/**
 * Created by NB on 4.7.2016.
 *
 */
public class RetryingSender {
    Activity activity;
    protected boolean knowResponse;
    protected boolean knowAnswer;
    protected int counterError;
    RequestQueue requestQueue;
    AlertDialog alertDialog;
    /**
     * Pro zjisteni, jestli progress dialog v showLoadingProgress() bezi
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

    private void showInfoDialog(String text, final boolean finishActivity, final boolean popup) {
        if (popup) {
            CustomDialog.showDialog(activity, text, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Handler mainHandler = WebviewOnClick.popUpView.getHandler();
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            WebviewOnClick.popUp.dismiss();
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            });
        }
        CustomDialog.showDialog(activity, text, finishActivity);
    }
    public void startSender(final boolean finish, final boolean popup) {
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
                                    if (counterError >= 20) {
                                        showInfoDialog("Problém s připojením, zkuste to prosím znovu.", finish, popup);
                                        hideLoadingProgress();
                                    } else if (!knowAnswer) {
                                        requestQueue.add(send());
                                        startSender(finish);
                                    } else {
                                        hideLoadingProgress();
                                        System.out.println("Loading dokoncen.");
                                    }
                                } else {
                                    startSender(finish);
                                }
                            }
                        }, 100);

                    }
                });
            }
        }.start();

    }
    public void startSender() {
        startSender(false);
    }

    public void startSender(final boolean finish) {
        startSender(finish,false);
    }
    protected CustomRequest send() {
        return null;
    }
}
