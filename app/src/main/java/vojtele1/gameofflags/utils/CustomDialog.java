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

    public static void showDialog(Context context, String textDialog) {
        showDialog(context,textDialog,null);
    }

    public static void showDialog(final Activity activity, String textDialog, final boolean finish) {
        showDialog(activity, textDialog, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (finish)
                    activity.finish();
            }
        });
    }
    public static void showDialog(Context context, String textDialog, DialogInterface.OnDismissListener onDismissListener) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_ok);

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

    public static void showDialogYesNo(Context context, String textDialog, View.OnClickListener yesListener, View.OnClickListener noListener) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_yes_no);

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
    public static void dismissDialog() {
        dialog.dismiss();
    }
}
