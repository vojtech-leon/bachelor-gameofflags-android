package vojtele1.gameofflags;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon on 25.10.2015.
 */
public class Act1Login extends Activity implements View.OnClickListener {

    private String token;

    private GitkitClient client;


    private String nickname;
    RequestQueue requestQueue;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";

    String loginPlayer = adresa + "loginplayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Step 1: Create a GitkitClient.
        // The configurations are set in the AndroidManifest.xml. You can also set or overwrite them
        // by calling the corresponding setters on the GitkitClient builder.
        //

        client = GitkitClient.newBuilder(this, new GitkitClient.SignInCallbacks() {
            // Implement the onSignIn method of GitkitClient.SignInCallbacks interface.
            // This method is called when the sign-in process succeeds. A Gitkit IdToken and the signed
            // in account information are passed to the callback.
            @Override
            public void onSignIn(IdToken idToken, GitkitUser user) {
                showProfilePage(idToken, user);

                // Now use the idToken to create a session for your user.
                // To do so, you should exchange the idToken for either a Session Token or Cookie
                // from your server.
                // Finally, save the Session Token or Cookie to maintain your user's session.
                String uspech = "nic";

                uspech = "Email: " + user.getEmail() + "\n"
                        + "LocalId: " + user.getLocalId() + "\n"
                        + "ProviderId: " + idToken.getProviderId() + "\n"
                        + "Auth: " + idToken.getKeyId() + "\n"
                        + "expired-kdy: " + idToken.getExpireAt() + "\n"
                        + "issueAt: " + idToken.getIssueAt();

                token = idToken.getTokenString();
                System.out.println(token);

                Toast.makeText(Act1Login.this, uspech, Toast.LENGTH_LONG).show();

                novyHrac();


            }

            // Implement the onSignInFailed method of GitkitClient.SignInCallbacks interface.
            // This method is called when the sign-in process fails.
            @Override
            public void onSignInFailed() {
                Toast.makeText(Act1Login.this, "Sign in failed", Toast.LENGTH_LONG).show();
            }
        }).build();

        showSignInPage();
    }




    // Step 3: Override the onActivityResult method.
    // When a result is returned to this activity, it is maybe intended for GitkitClient. Call
    // GitkitClient.handleActivityResult to check the result. If the result is for GitkitClient,
    // the method returns true to indicate the result has been consumed.
    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (!client.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent);
        }

    }



    // Step 4: Override the onNewIntent method.
    // When the app is invoked with an intent, it is possible that the intent is for GitkitClient.
    // Call GitkitClient.handleIntent to check it. If the intent is for GitkitClient, the method
    // returns true to indicate the intent has been consumed.

    @Override
    protected void onNewIntent(Intent intent) {
        if (!client.handleIntent(intent)) {
            super.onNewIntent(intent);
        }
    }



    private void showSignInPage() {
        setContentView(R.layout.activity_login_welcome);
        Button button = (Button) findViewById(R.id.sign_in);
        button.setOnClickListener(this);
    }


    private void showProfilePage(IdToken idToken, GitkitUser user) {
        setContentView(R.layout.activity_login_profile);
        showAccount(user);
        findViewById(R.id.sign_out).setOnClickListener(this);
    }


    // Step 5: Respond to user actions.
    // If the user clicks sign in, call GitkitClient.startSignIn() to trigger the sign in flow.

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.sign_in) {
            client.startSignIn();
        } else if (v.getId() == R.id.sign_out) {
            showSignInPage();
        }
    }



    private void showAccount(GitkitUser user) {
        ((TextView) findViewById(R.id.account_email)).setText(user.getEmail());

        if (user.getDisplayName() != null) {
            ((TextView) findViewById(R.id.account_name)).setText(user.getDisplayName());
        }

        if (user.getPhotoUrl() != null) {
            final ImageView pictureView = (ImageView) findViewById(R.id.account_picture);
            new AsyncTask<String, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(String... arg) {
                    try {
                        byte[] result = HttpUtils.get(arg[0]);
                        return BitmapFactory.decodeByteArray(result, 0, result.length);
                    } catch (IOException e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        pictureView.setImageBitmap(bitmap);
                    }
                }
            }.execute(user.getPhotoUrl());
        }
    }
    public void pokracujWebView(View view) {
        Intent intent = new Intent(this, Act2WebView.class);
        intent.putExtra("token", token);
        startActivity(intent);

    }


    private void novyHrac() {
        Map<String, String> params = new HashMap();
        params.put("token", token);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  loginPlayer, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray players = response.getJSONArray("player");
                            for (int i = 0; i < players.length(); i++) {
                                JSONObject player = players.getJSONObject(i);
                                nickname = player.getString("nickname");
                              //  Toast.makeText(Act1Login.this, nickname, Toast.LENGTH_LONG).show();
                                // TODO zmena jmena po "registraci", treba overeni, jestli se jmenuje user(default)
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());

            }
        });

        requestQueue.add(jsObjRequest);

    }
}
