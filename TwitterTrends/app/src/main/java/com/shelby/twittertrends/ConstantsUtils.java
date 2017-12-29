package com.shelby.twittertrends;

/**
 * Created by Shelby on 12/29/17.
 */

public class ConstantsUtils {

        public static final String URL_ROOT_TWITTER_API = "https://api.twitter.com";
        public static final String URL_SEARCH = URL_ROOT_TWITTER_API + "/1.1/search/tweets.json?q=";
        public static final String URL_AUTHENTICATION = URL_ROOT_TWITTER_API + "/oauth2/token";

        public static final String URL_INDIA_TRENDING ="https://api.twitter.com/1.1/trends/place.json?id=23424977";
        public static final String URL_WORLD_TRENDING ="https://api.twitter.com/1.1/trends/place.json?id=1";


        public static final String CONSUMER_KEY = "oqqV4Yfzhu9EXGkXUs7CAZAJ6";
        public static final String CONSUMER_SECRET = "ZHvHhPACxzWbqpAxweH9JmdVFXuB8B9PwJ0WaH50rYMSWbt7s9";

}
