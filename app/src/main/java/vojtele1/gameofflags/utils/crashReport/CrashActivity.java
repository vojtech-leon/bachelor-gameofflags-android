package vojtele1.gameofflags.utils.crashReport;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;

import vojtele1.gameofflags.R;
import vojtele1.gameofflags.utils.C;

import static android.support.v4.content.FileProvider.getUriForFile;


/**
 * Aktivita umoznujici poslani crash reportu na email
 * @author Bc. Radek Brůha <bruhara1@uhk.cz>
 */
@SuppressWarnings({ "ConstantConditions", "unchecked" })
public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        findViewById(R.id.buttonCrash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = getUriForFile(CrashActivity.this, "vojtele1.gameofflags", new File(CrashActivity.this.getFilesDir(), "crash.log"));

                Intent intent = new Intent(Intent.ACTION_SEND)
                        .setType("message/rfc822")
                        .putExtra(Intent.EXTRA_EMAIL, C.EXCEPTION_EMAIL)
                        .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.activity_crash_email_subject))
                        .putExtra(Intent.EXTRA_TEXT, getString(R.string.activity_crash_email_text))
                        .putExtra(Intent.EXTRA_STREAM, uri);

                for (ResolveInfo resolveInfo : CrashActivity.this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
                    CrashActivity.this.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivity(intent);
                Process.killProcess(Process.myPid());
                System.exit(0);
            }
        });
    }
}