package com.shelby.twittertrends;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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

public class TrendingView extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "TwitterUtils";
    private String jsonResponse;
    private ArrayList<JsonData> jArray;
    private ListView listView;
    private double lat;
    private double lon;
    private TextView trendingText;
    private String userName;
    private boolean isGuest;
    private static CustomAdapter adapter;
    private boolean isFirst;
    private TwitterSession session;
    private String geoResp;
    private String woeid = null;




    private static final int PLACE_PICKER_REQUEST = 1;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GoogleApiClient mGoogleApiClient;
    PlacePicker.IntentBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_trending_view);

        Intent intent = getIntent();
        isGuest = intent.getBooleanExtra("isGuest", false);
        isFirst = intent.getBooleanExtra("fromTweetView", false);
        lat=intent.getDoubleExtra("lat",Double.MAX_VALUE);
        lon=intent.getDoubleExtra("lon",Double.MAX_VALUE);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();


        int PLACE_PICKER_REQUEST = 1;

        builder = new PlacePicker.IntentBuilder();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        trendingText = (TextView) findViewById(R.id.trendingText);
        listView = (ListView) findViewById(R.id.listView);

        try {
            userName = session.getUserName();
            if (!isFirst && userName != null) {
                showWelcomeToast(userName);
            }
        } catch (Exception e) {
        }

       update();


    }

    private void update()
    {

        if(lat!=Double.MAX_VALUE && lon!=Double.MAX_VALUE)
        {
            geoResp = getTimelineForSearchTermGeo(lat,lon);

            try {
                woeid = parseGeoJson(geoResp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            woeid = "1";
            trendingText.setText("Worldwide");
        }

        jsonResponse = getTimelineForSearchTerm(woeid);

        try {
            jArray = parseJson(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new CustomAdapter(jArray, getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), TrendTweetView.class);
                intent.putExtra("trend", jArray.get(position));
                intent.putExtra("isGuest", isGuest);
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                startActivity(intent);
            }
        });
    }



    private void showWelcomeToast(String userName) {
        Toast.makeText(getApplicationContext(),"Hello " + userName +"!" ,Toast.LENGTH_SHORT).show();
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

    public static String getTimelineForSearchTerm(String woeid) {
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(ConstantsUtils.URL_TRENDING + woeid);
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

    public ArrayList<JsonData> parseJson(String jsonResponse) throws JSONException {
        JSONArray trendsArray = null;
        ArrayList<JsonData> jData = new ArrayList<>();
        String name;
        String query;
        String url;
        String tweet_volume;
        String displayName;
        try {
            JSONArray wrapper = new JSONArray(jsonResponse);
            JSONObject trendsObject = wrapper.getJSONObject(0);
            trendsArray = trendsObject.getJSONArray("trends");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < trendsArray.length(); i++) {
                JSONObject trend = trendsArray.getJSONObject(i);
                name = ((i+1)+". " + trend.getString("name"));
                displayName = trend.getString("name");
                url = trend.getString("url");
                query = trend.getString("query");
                tweet_volume = trend.getString("tweet_volume");

                try {
                    Log.d(TAG, name);
                } catch (Exception e) {
                    Log.d(TAG, "That did not work");
                }

                JsonData j = new JsonData(name, url, query, tweet_volume, displayName);
                jData.add(j);

            }

        } catch (JSONException e) {
            // handle the error here ...
        }

        return jData;
    }


    public static String getTimelineForSearchTermGeo(double latitude, double longitude) {
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL("https://api.twitter.com/1.1/trends/closest.json?lat=" + latitude + "&long=" + longitude);
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

    public String parseGeoJson(String jsonResponse) throws JSONException {
        JSONArray locArray = new JSONArray(jsonResponse);
        String woeid = null;

        try {
            for (int i = 0; i < locArray.length(); i++) {
                JSONObject locObject = locArray.getJSONObject(i);
                woeid = locObject.getString("woeid");
                trendingText.setText(locObject.getString("name"));

                try {
                    Log.d(TAG, woeid);
                } catch (Exception e) {
                    Log.d(TAG, "That did not work");
                }

            }

        } catch (JSONException e) {
            Log.d(TAG, "it did not work at all");
        }

        return woeid;
    }

    public void handleBackButton(View view) {
        Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void handleLocationPicker(View view) {
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(this,data);
                lat = selectedPlace.getLatLng().latitude;
                lon =selectedPlace.getLatLng().longitude;

                update();

                mGoogleApiClient.stopAutoManage(this);
                mGoogleApiClient.disconnect();



            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),"Cannot connect to Google services",Toast.LENGTH_SHORT).show();
    }






}
