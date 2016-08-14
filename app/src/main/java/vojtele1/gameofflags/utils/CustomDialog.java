package vojtele1.gameofflags.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import vojtele1.gameofflags.R;

/**
 * Veskere varianty informujicich/upozornujicich dialogu
 * Created by Leon on 18.07.2016.
 */
public class CustomDialog {
    public static Dialog dialog;

    public static void showAlertDialog(Context context, String textDialog) {
        showAlertDialog(context,textDialog,null);
    }

    public static void showAlertDialog(final Activity activity, String textDialog, final boolean finish) {
        showAlertDialog(activity, textDialog, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (finish)
                    activity.finish();
            }
        });
    }

    public static void showAlertDialog(Context context, String textDialog, DialogInterface.OnDismissListener onDismissListener) {
        showDialog(context, textDialog, onDismissListener, true);
    }

    public static void showInfoDialog(Context context, String textDialog, DialogInterface.OnDismissListener onDismissListener) {
        showDialog(context, textDialog, onDismissListener, false);
    }

    /**
     * zobrazi dialog daneho typu
     * @param context context
     * @param textDialog zobrazovany text
     * @param onDismissListener reakce po zmizeni dialogu
     * @param type - true znamena alert, false je info dialog
     */
    private static void showDialog(Context context, String textDialog, DialogInterface.OnDismissListener onDismissListener, Boolean type) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new Dialog(context);
        if (type) {
            dialog.setContentView(R.layout.dialog_alert_ok);
        } else {
            dialog.setContentView(R.layout.dialog_info_ok);
        }

        TextView text = (TextView) dialog.findViewById(R.id.txt_dia_in_ok);
        text.setText(textDialog);

        dialog.show();

        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        Button button = (Button) dialog.findViewById(R.id.btn_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(onDismissListener);
    }

    public static void showAlertDialogYesNo(Context context, String textDialog, View.OnClickListener yesListener, View.OnClickListener noListener) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_alert_yes_no);

        TextView text = (TextView) dialog.findViewById(R.id.txt_dia_in_yes_no);
        text.setText(textDialog);

        dialog.show();

        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        Button buttonYes = (Button) dialog.findViewById(R.id.btn_yes);
        Button buttonNo = (Button) dialog.findViewById(R.id.btn_no);
        buttonYes.setOnClickListener(yesListener);
        buttonNo.setOnClickListener(noListener);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialog.dismiss();
            }
        });
    }

    public static void showInfoDialogEditText(Context context, String title, View.OnClickListener okListener) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_info_with_edit_text);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView text = (TextView) dialog.findViewById(R.id.txt_dia_in_edit_text);
        text.setText(title);

        dialog.show();

        dialog.getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        Button button = (Button) dialog.findViewById(R.id.btn_ok);
        button.setOnClickListener(okListener);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // zablokovani dismissu
                //dialog.dismiss();
            }
        });
    }

    public static void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
