package com.shelby.twittertrends;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class WelcomeScreen extends AppCompatActivity
{

    private TwitterLoginButton loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Twitter.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);


        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result)
            {
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;

                goToTrendingView();


            }

            @Override
            public void failure(TwitterException exception)
            {
                // Do something on failure
            }
        });


    }

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data)
{
    super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
    loginButton.onActivityResult(requestCode, resultCode, data);
}

private void goToTrendingView()
{
    Intent intent = new Intent(this, TrendingView.class);
    startActivity(intent);
}




}
