package com.shelby.twittertrends;

/**
 * Created by Shelby on 12/29/17.
 */

public class JsonData
{
    String name;
    String url;
    String query;
    String tweet_volume;



    public JsonData(String name, String url, String query, String tweet_volume)
    {
        this.name = name;
        this.url = url;
        this.query = query;
        this.tweet_volume = tweet_volume;
    }

   @Override
   public String toString(){
       return name;
   }




}
