package com.shelby.twittertrends;

import android.app.ListActivity;
import android.content.Context;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mopub.common.util.Json;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class TrendingView extends ListActivity {

    public static final String TAG = "TwitterUtils";
    String jsonResponse;
    ArrayList<JsonData> jArray;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending_view);

        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        String userName = session.getUserName();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        showWelcomeToast(userName);

        listView = (ListView)findViewById(android.R.id.list);


        jsonResponse = getTimelineForSearchTerm();
        try {
            jArray = parseJson(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<JsonData> arrayAdapter = new ArrayAdapter<JsonData>(getApplicationContext(),android.R.layout.simple_list_item_1, jArray);
        listView.setAdapter(arrayAdapter);


}



    private void showWelcomeToast(String userName) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        String text = "Hello " + userName + "!";
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    public static String appAuthentication() {

        HttpURLConnection httpConnection = null;
        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = null;

        try {
            URL url = new URL(ConstantsUtils.URL_AUTHENTICATION);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            String accessCredential = ConstantsUtils.CONSUMER_KEY + ":"
                    + ConstantsUtils.CONSUMER_SECRET;
            String authorization = "Basic "
                    + Base64.encodeToString(accessCredential.getBytes(),
                    Base64.NO_WRAP);
            String param = "grant_type=client_credentials";

            httpConnection.addRequestProperty("Authorization", authorization);
            httpConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            httpConnection.connect();

            outputStream = httpConnection.getOutputStream();
            outputStream.write(param.getBytes());
            outputStream.flush();
            outputStream.close();
            // int statusCode = httpConnection.getResponseCode();
            // String reason =httpConnection.getResponseMessage();

            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));
            String line;
            response = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }

            Log.d(TAG,
                    "POST response code: "
                            + String.valueOf(httpConnection.getResponseCode()));
            Log.d(TAG, "JSON response: " + response.toString());

        } catch (Exception e) {
            Log.e(TAG, "POST error: " + Log.getStackTraceString(e));

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return response.toString();
    }

    public static String getTimelineForSearchTerm() {
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(ConstantsUtils.URL_WORLD_TRENDING);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");

            String jsonString = appAuthentication();
            JSONObject jsonObjectDocument = new JSONObject(jsonString);
            String token = jsonObjectDocument.getString("token_type") + " "
                    + jsonObjectDocument.getString("access_token");
            httpConnection.setRequestProperty("Authorization", token);

            httpConnection.setRequestProperty("Authorization", token);
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpConnection.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }

            Log.d(TAG,
                    "GET response code: "
                            + String.valueOf(httpConnection
                            .getResponseCode()));
            Log.d(TAG, "JSON response: " + response.toString());

        } catch (Exception e) {
            Log.e(TAG, "GET error: " + Log.getStackTraceString(e));

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();

            }
        }
        return response.toString();
    }

    public ArrayList<JsonData> parseJson(String jsonResponse) throws JSONException
    {
        JSONArray trendsArray = null;
        ArrayList<JsonData> jData = new ArrayList<>();
        String name;
        String query;
        String url;
        String tweet_volume;
        try {
            JSONArray wrapper = new JSONArray(jsonResponse);
            JSONObject trendsObject = wrapper.getJSONObject(0);
            trendsArray = trendsObject.getJSONArray("trends");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < trendsArray.length(); i++) {
                JSONObject trend = trendsArray.getJSONObject(i);
                name = trend.getString("name");
                url = trend.getString("url");
                query = trend.getString("query");
                tweet_volume = trend.getString("tweet_volume");

                try{
                    Log.d(TAG,name);
                }
                catch (Exception e)
                {
                    Log.d(TAG,"That did not work");
                }

                JsonData j = new JsonData(name,url,query,tweet_volume);
                jData.add(j);

            }

        } catch (JSONException e) {
            // handle the error here ...
        }

        return jData;
    }


}
