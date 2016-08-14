package vojtele1.gameofflags;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitUser;
import com.google.identitytoolkit.IdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vojtele1.gameofflags.utils.BaseActivity;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.CustomDialog;
import vojtele1.gameofflags.utils.CustomRequest;
import vojtele1.gameofflags.utils.RetryingSender;
import vojtele1.gameofflags.utils.crashReport.ExceptionHandler;

/**
 * activita obsahujici prihlaseni do hry
 * Created by Leon on 25.10.2015.
 */
public class Act1Login extends BaseActivity {

    private String token;

    private GitkitClient client;

    private String newNickname;

    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_welcome);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of token from SharedPreferences. Set to "" as a default.
        token = sharedPreferences.getString(C.TOKEN, "");

        if (!token.equals("")) {
            loginPlayer();
        }

        client = GitkitClient.newBuilder(this, new GitkitClient.SignInCallbacks() {
            @Override
            public void onSignIn(IdToken idToken, GitkitUser user) {
                token = idToken.getTokenString();
                Log.d(C.LOG_ACT1LOGIN, token);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(C.TOKEN, token);
                editor.apply();

                loginPlayer();
            }

            @Override
            public void onSignInFailed() {
                Toast.makeText(Act1Login.this, R.string.act1_login_failed, Toast.LENGTH_LONG).show();
            }
        }).build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (!client.handleActivityResult(requestCode, resultCode, intent)) {
            super.onActivityResult(requestCode, resultCode, intent);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (!client.handleIntent(intent)) {
            super.onNewIntent(intent);
        }
    }

    public void logIn(View view) {
            client.startSignIn();
    }

    private void loginPlayer() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                Map<String, String> params = new HashMap<>();
                params.put("token", token);

               return new CustomRequest(Request.Method.POST, C.LOGIN_PLAYER, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(C.LOG_ACT1LOGIN, response.toString());
                                knowResponse = true;

                                try {
                                    JSONArray players = response.getJSONArray("player");
                                    JSONObject player = players.getJSONObject(0);
                                    String fraction_name;
                                    int fraction = player.optInt("player_fraction");
                                    if (fraction == 1) {
                                        fraction_name = "Red";
                                    } else if (fraction == 2) {
                                        fraction_name = "Blue";
                                    } else {
                                        fraction_name = "";
                                    }
                                    // pokud jmeno neni nastaveno v db, vrati se null,
                                    // proto se porovnava s null (jmeno "null" muze byt)
                                    if (player.get("nickname").equals(null)) {
                                        nameChange(fraction_name);
                                    } else {
                                        continueToWebview();
                                    }
                                    knowAnswer = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(C.LOG_ACT1LOGIN,  "" + error.getMessage());
                        knowResponse = true;
                        counterError++;

                    }
                });
            }
        };
        r.startSender();
    }

    private void continueToWebview() {
        Intent intent = new Intent(this, Act2WebView.class);
        startActivity(intent);
    }

    private void nameChange(final String fraction_name) {
        CustomDialog.showInfoDialogEditText(Act1Login.this, getString(R.string.act1_set_your_nickname), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) CustomDialog.dialog.findViewById(R.id.etxt_in_dia);
                newNickname = editText.getText().toString();
                if (newNickname.length() >= 4) {
                    changeNameRequest(newNickname, fraction_name);
                } else {
                    nameChange(fraction_name);
                    Toast.makeText(Act1Login.this, R.string.act1_nickname_must_have_atleast_4, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void changeNameRequest(final String nickname, final String fraction_name) {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                params.put("nickname", nickname);

                return new CustomRequest(Request.Method.POST,  C.CHANGE_PLAYER_NAME, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(C.LOG_ACT1LOGIN, response.toString());
                            knowResponse = true;
                            try {
                                JSONArray players = response.getJSONArray("player");
                                JSONObject player = players.getJSONObject(0);
                                boolean nameAvailability = player.getBoolean("nameAvailability");
                                if (nameAvailability) {
                                    String message;
                                    if (fraction_name.equals("")) {
                                        message = getString(R.string.act1_you_were_renamed_new_nickname_is) + nickname + "!";
                                    } else {
                                        message =  getString(R.string.act1_welcome_in_game) + nickname + getString(R.string.act1_your_fraction_is)
                                                + fraction_name + "!";
                                    }
                                    CustomDialog.showInfoDialog(Act1Login.this, message, new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            continueToWebview();
                                        }
                                    });
                                } else {
                                    Toast.makeText(Act1Login.this, R.string.act1_this_nickname_exists, Toast.LENGTH_LONG).show();
                                    nameChange(fraction_name);
                                }
                                knowAnswer = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(C.LOG_ACT1LOGIN, "" + error.getMessage());
                            knowResponse = true;
                            counterError++;
                        }
                    });
            }
        };
        r.startSender();
    }

    @Override
    public void onBackPressed() {
        // zakomentovani zabrani reakci na stisk hw back
        //super.onBackPressed();
    }
}
