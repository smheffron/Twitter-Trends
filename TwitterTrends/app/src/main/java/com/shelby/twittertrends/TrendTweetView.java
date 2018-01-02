package com.shelby.twittertrends;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mopub.common.util.Json;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.params.Geocode;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.UserTimeline;
import com.twitter.sdk.android.tweetui.TweetView;

import org.w3c.dom.Text;

import javax.xml.datatype.Duration;


public class TrendTweetView extends ListActivity {

    private TextView trendID;
    private boolean isGuest;
    private long tweetId;
    private Context mContext;
    private PopupWindow mPopupWindow;
    private ConstraintLayout mRelativeLayout;
    private TwitterSession session;
    private JsonData jData;
    private String query;
    private double lat;
    private double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_view);

        Intent intent = getIntent();
        isGuest = intent.getBooleanExtra("isGuest", false);
        lon=intent.getDoubleExtra("lon",Double.MAX_VALUE);
        lat=intent.getDoubleExtra("lat",Double.MAX_VALUE);
        jData = (JsonData) intent.getSerializableExtra("trend");

        session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        trendID = (TextView)findViewById(R.id.trendID);
        mContext = getApplicationContext();

        query = jData.getQuery();
        trendID.setText(jData.getDisplayName());

        TwitterCore.getInstance().getApiClient().getSearchService().tweets(query, null, null, null, null, null, null, null, null, true);

        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query(query)
                .maxItemsPerRequest(100)
                .resultType(SearchTimeline.ResultType.MIXED)
                .build();

        final CustomTweetTimelineListAdapter adapter = new CustomTweetTimelineListAdapter(this, searchTimeline);
        setListAdapter(adapter);

    }


    class CustomTweetTimelineListAdapter extends TweetTimelineListAdapter {

        public CustomTweetTimelineListAdapter(Context context, Timeline<Tweet> timeline) {
            super(context, timeline);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            //disable subviews
            if(view instanceof ViewGroup){
                disableViewAndSubViews((ViewGroup) view);
            }

            //enable root view and attach custom listener
            view.setEnabled(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        tweetId = getItemId(position);

                        mRelativeLayout = (ConstraintLayout) findViewById(R.id.myConLay);

                        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);

                        // Inflate the custom layout/view
                        final View customView = inflater.inflate(R.layout.my_tweet_layout, null);

                        final LinearLayout tweetView = (LinearLayout) customView.findViewById(R.id.myLinLayout2);

                        final TextView likes = (TextView) customView.findViewById(R.id.likes);
                        final TextView retweets = (TextView) customView.findViewById(R.id.retweets);

                        TweetUtils.loadTweet(tweetId, new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                Tweet tweet = result.data;
                                likes.setText(String.valueOf(tweet.favoriteCount));
                                retweets.setText(String.valueOf(tweet.retweetCount));

                                TweetView tv = new TweetView(TrendTweetView.this, tweet, R.style.tw__TweetLightWithActionsStyle);

                                tv.setOnActionCallback(new Callback<Tweet>() {
                                    @Override
                                    public void success(Result<Tweet> result) {
                                        // Intentionally blank
                                    }

                                    @Override
                                    public void failure(TwitterException exception) {
                                        if (exception instanceof TwitterAuthException) {
                                            Toast.makeText(getApplicationContext(), "Must be logged in to like tweets", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                tweetView.addView(tv);
                            }

                            @Override
                            public void failure(TwitterException exception) {
                                // Toast.makeText(...).show();
                            }
                        });

                        mPopupWindow = new PopupWindow(
                                customView,
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                        );

                        if (Build.VERSION.SDK_INT >= 21) {
                            mPopupWindow.setElevation(5.0f);
                        }

                        mPopupWindow.setOutsideTouchable(true);
                        mPopupWindow.setFocusable(true);
                        // Removes default background.
                        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);
                }

            });
            return view;
        }

        private void disableViewAndSubViews(ViewGroup layout) {
            layout.setEnabled(false);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    disableViewAndSubViews((ViewGroup) child);
                } else {
                    child.setEnabled(false);
                    child.setClickable(false);
                    child.setLongClickable(false);
                }
            }
        }

    }

    public void handleBackButtonOnTweetView(View view) {
        Intent intent = new Intent(getApplicationContext() , TrendingView.class);

        intent.putExtra("fromTweetView", true);

        if(isGuest)
        {
            intent.putExtra("isGuest",true);
            intent.putExtra("lat",lat);
            intent.putExtra("lon",lon);
        }
        else
        {
            intent.putExtra("isGuest", false);
            intent.putExtra("lat",lat);
            intent.putExtra("lon",lon);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
