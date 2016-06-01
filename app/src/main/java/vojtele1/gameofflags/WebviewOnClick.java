package vojtele1.gameofflags;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import vojtele1.gameofflags.utils.C;

/**
 * Created by Leon on 31.05.2016.
 */
public class WebviewOnClick {
    Context context;
    Activity activity;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String getFlagInfoFull = adresa + "getflaginfofull";

    TextView fractionName, playerName, flagName, flagWhen;
    RequestQueue requestQueue;
    String cas,responseFrName, responsePName, responseFlName;

    PopupWindow popUp;
    View popUpView;

    public WebviewOnClick(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(context);
        popUp = new PopupWindow(context);

    }
    /** Show a toast from svg */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void showPopup(String id) {
        popUpView = LayoutInflater.from(context).inflate(R.layout.flag_info, null);
        fractionName = (TextView) popUpView.findViewById(R.id.flag_info_textView_fraction);
        playerName = (TextView) popUpView.findViewById(R.id.flag_info_textView_flag_owner);
        flagName = (TextView) popUpView.findViewById(R.id.flag_info_textView_flag_name);
        flagWhen = (TextView) popUpView.findViewById(R.id.flag_info_textView_flagWhen);

        popUp.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popUp.setContentView(popUpView);
        // Closes the popup window when touch outside.
        popUp.setOutsideTouchable(true);
        popUp.setFocusable(true);
        popUp.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFAB00")));
        popUp.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
        showFlag(id);
    }


    private void showFlag(String idFlag) {
        Map<String, String> params = new HashMap();
        params.put("ID_flag", idFlag);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, getFlagInfoFull, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        try {
                            JSONArray flagsJson = response.getJSONArray("flag");
                            JSONObject flagJson = flagsJson.getJSONObject(0);
                            JSONObject time = flagJson.getJSONObject("flagWhen");
                            String flagDate = time.getString("date");
                            responseFlName = flagJson.getString("flagName");
                            responsePName = flagJson.getString("playerName");
                            responseFrName = flagJson.getString("fractionName");
                            //zmena formatu casu
                            SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                            // nastavi prijaty cas na UTC
                            sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));
                            try {
                                Date date = sdfPrijaty.parse(flagDate);
                                // preformatuje prijaty cas do bezneho ciselneho tvaru
                                cas = sdfVysledny.format(date.getTime());

                                napis();
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
            }
        });
        requestQueue.add(jsObjRequest);


    }
    private void napis() {

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = popUpView.getHandler();

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                flagName.setText(responseFlName);
                playerName.setText(responsePName);
                fractionName.setText(responseFrName);
                flagWhen.setText(cas);
            }
        };
        mainHandler.post(myRunnable);


    }
}
