package vojtele1.gameofflags;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Leon on 21.10.2015.
 */
public class Act4Settings extends AppCompatActivity {

    String userId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // vytahne id z activity loginu
        userId = getIntent().getStringExtra("userId");
    }
    public void logout(View view) {
        Intent intent = new Intent(this, Act1Login.class);
        startActivity(intent);
    }
}
