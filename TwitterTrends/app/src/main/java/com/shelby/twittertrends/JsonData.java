package com.shelby.twittertrends;

import android.util.Log;

import java.io.Serializable;
import java.text.DecimalFormat;

import static com.shelby.twittertrends.numberParser.format;

/**
 * Created by Shelby on 12/29/17.
 */

public class JsonData implements Serializable {
    private String name;
    private String url;
    private String query;
    private String tweet_volume;
    private String displayName;

    public JsonData(String name, String url, String query, String tweet_volume, String displayName)
    {
        this.name = name;
        this.url = url;
        this.query = query;
        this.tweet_volume = tweet_volume;
        this.displayName = displayName;
    }

   @Override
   public String toString()
   {
       if(this.tweet_volume == null || tweet_volume.equals("null") || tweet_volume.isEmpty())
       {
           return (name);
       }
       else {

           if(tweet_volume.contains("k"))
           {
               return (name +  tweet_volume);
           }
           if(tweet_volume.contains("m"))
           {
               return (name + tweet_volume);
           }

           Log.d("The value is", tweet_volume);
           tweet_volume = format(Double.parseDouble(tweet_volume));

           return (name + tweet_volume);
       }
   }

    public String getName() {
        return name;
    }


    public String getType() {

        if(this.tweet_volume == null || tweet_volume.equals("null") || tweet_volume.isEmpty())
        {
            return "";
        }
        else {

            if(tweet_volume.contains("k"))
            {
                return (tweet_volume);
            }
            if(tweet_volume.contains("m"))
            {
                return (tweet_volume);
            }

            tweet_volume = format(Double.parseDouble(tweet_volume));

            return (tweet_volume);
        }

    }

    public String getQuery()
    {
        return query;
    }
    public String getUrl()
    {
        return url;
    }
    public String getTweet_volume()
    {
        return tweet_volume;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public void setQuery(String query)
    {
        this.query=query;
    }
    public void setTweet_volume(String tweet_volume)
    {
        this.tweet_volume=tweet_volume;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
