package vojtele1.gameofflags.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import vojtele1.gameofflags.R;

/**
 * Created by Leon on 31.05.2016.
 */
public class WebviewOnClick {
    Activity activity;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String getFlagInfoUser = adresa + "getflaginfouser";

    TextView tvFractionName, tvPlayerName, tvFlagName, tvFlagWhen, tvWhatToDo;
    RequestQueue requestQueue;
    String cas,responseFrName, responsePName, responseFlName, whatToDo;

     public static PopupWindow popUp;
    public static View popUpView;

    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;

    private String token;

    public WebviewOnClick(Activity activity) {
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(activity);
        popUp = new PopupWindow(activity);

        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = activity.getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of token from SharedPreferences. Set to "" as a default.
        token = sharedPreferences.getString(C.TOKEN, "");

    }
    @JavascriptInterface
    public void showPopup(String id) {
        popUpView = LayoutInflater.from(activity).inflate(R.layout.flag_info, null);
        tvFractionName = (TextView) popUpView.findViewById(R.id.flag_info_textView_fraction);
        tvPlayerName = (TextView) popUpView.findViewById(R.id.flag_info_textView_flag_owner);
        tvFlagName = (TextView) popUpView.findViewById(R.id.flag_info_textView_flag_name);
        tvFlagWhen = (TextView) popUpView.findViewById(R.id.flag_info_textView_flagWhen);
        tvWhatToDo = (TextView) popUpView.findViewById(R.id.flag_info_whatToDo);

        popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setContentView(popUpView);
        // Closes the popup window when touch outside.
        popUp.setOutsideTouchable(true);
        popUp.setFocusable(true);
        //popUp.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFAB00

        popUp.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.background_popup_orange_with_dark_orange_border));

        popUp.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
        showFlag(id);
    }
    private void showFlag(final String idFlag) {
        RetryingSender r = new RetryingSender(activity) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                params.put("ID_flag", idFlag);

                return new CustomRequest(Request.Method.POST, getFlagInfoUser, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            knowResponse = true;
                            System.out.println(response.toString());
                            try {
                                JSONArray flagsJson = response.getJSONArray("flag");
                                JSONObject flagJson = flagsJson.getJSONObject(0);
                                JSONObject time = flagJson.getJSONObject("flagWhen");
                                String flagDate = time.getString("date");
                                responseFlName = flagJson.getString("flagName");
                                responsePName = flagJson.getString("playerName");
                                responseFrName = flagJson.getString("fractionName");
                                String flagMe = flagJson.getString("flagMe");
                                String fractionMe = flagJson.getString("fractionMe");
                                //zmena formatu casu
                                SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                                // nastavi prijaty cas na UTC
                                sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));

                                try {
                                    Date date = sdfPrijaty.parse(flagDate);
                                    // preformatuje prijaty cas do bezneho ciselneho tvaru
                                    cas = sdfVysledny.format(date.getTime());


                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                    long dateFlagChange = date.getTime();
                                    // ziskani aktualniho casu
                                    Long dateNow = new Date().getTime();
                                    if (flagMe.equals("true") && !fractionMe.equals("true")) {
                                        whatToDo = "zabrána tebou za druhou frakci.";
                                    } else if (flagMe.equals("true")) {
                                        whatToDo = "zabrána tebou.";
                                    } else if (fractionMe.equals("true")) {
                                        whatToDo = "patří tvé frakci.";
                                    } else if (dateNow < dateFlagChange + C.FLAG_IMMUNE_TIME) {
                                        whatToDo = "čerstvě zabrána, počkej do " + sdf.format(date.getTime()) + ".";
                                    } else {
                                        whatToDo = "zaber ji!";
                                    }


                                    setText();
                                    knowAnswer = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.append(error.getMessage());
                    knowResponse = true;
                    counterError++;
                }
            });
            }
        };
        r.startSender();
    }

    private void setText() {
        Handler mainHandler = popUpView.getHandler();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                tvFlagName.setText(responseFlName);
                tvPlayerName.setText(responsePName);
                tvFractionName.setText(responseFrName);
                tvFlagWhen.setText(cas);
                tvWhatToDo.setText(whatToDo);
            }
        };
        mainHandler.post(myRunnable);
    }
}
