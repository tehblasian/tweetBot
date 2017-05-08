package com.tehblasian.twitterbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;

	// Shared Preferences
	private static SharedPreferences sharedPreferences;

	private TextView status;

	private tweetBot bot = null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Shared Preferences
		sharedPreferences = getApplicationContext().getSharedPreferences("userPreferences", 0);

		//get status textview
		status = (TextView)findViewById(R.id.bot_status);

        //get start/pause button
		Button controlBot = (Button)findViewById(R.id.start_pause_bot);
		controlBot.setTag(0);
		controlBot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int status = (Integer)controlBot.getTag();
				if(status == 0) {
					bot = new tweetBot();
					if (bot.searchQueries == null || bot.replyList == null || bot.searchQueries.isEmpty() || bot.replyList.isEmpty())
						Toast.makeText(MainActivity.this, "Please add searches & replies first!", Toast.LENGTH_SHORT).show();
					else {
						try {
						/*	bot.searchAndReply();
							bot.updateBotStatus(); */
							bot.run();
							controlBot.setTag(1);
							controlBot.setText("PAUSE BOT");
						} catch (TwitterException e) {
							Log.e("Tweet Error: ", "Could not search and reply to tweet.");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				else if(status == 1){
					if(!bot.isStopped()) {
						bot.pause();
						controlBot.setTag(2);
						controlBot.setText("RESUME");
					}
					else Toast.makeText(MainActivity.this, "Can't pause! The bot is already stopped!", Toast.LENGTH_SHORT).show();
				}
				else if(status == 2){
					if(!bot.isStopped()) {
						bot.wakeUp();
						controlBot.setTag(1);
						controlBot.setText("PAUSE BOT");
					}
					else Toast.makeText(MainActivity.this, "Can't resume! The bot is already stopped!", Toast.LENGTH_SHORT).show();
				}
			}
		});
       //get stop button
		Button stopBot = (Button)findViewById(R.id.stop_bot);
		stopBot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(bot != null){
					bot.stop();
					controlBot.setTag(0);
					controlBot.setText("START BOT!");
				}
			}
		});

        //get settings button
        Button settings = (Button)findViewById(R.id.openSettings);
        //set onClick listener to settings button
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create intent to open the settings activity
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                //start the new activity
                startActivity(settingsIntent);
            }
        });
    }

	@Override
	protected void onResume() {
		super.onResume();
		if(bot != null)
			bot.getLists(); //to update lists every time user goes back to main screen, even while the bot is running
	}

	protected class tweetBot extends AppCompatActivity {
		//API AUTH
		private static final String TWITTER_CONSUMER_KEY = "Cim8m4o97Z5n80uw6mEDNzfVw";
		private static final String TWITTER_CONSUMER_SECRET = "k3nFvZfJT6ZcV4Z80FHhxhzBP8jcJZuiOBnML947ljEP9INHHe";

		// Preference Constants
		static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
		static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

		private runBot execute;
		private Twitter botTwitter;

		private int maxTweets;
		private int numTweets;
		private int totalTweets;
		private int sleepTime;
		private int intervalTime;
		private boolean sleeping;
		private boolean isStopped;

		private ArrayList<String> searchQueries;
		private ArrayList<String> replyList;

		public tweetBot(){
			ConfigurationBuilder config = new ConfigurationBuilder();
			config.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			config.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

			// Access Token
			String access_token = sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

			config.setOAuthAccessToken(access_token);
			config.setOAuthAccessTokenSecret(access_token_secret);

			this.botTwitter = new TwitterFactory(config.build()).getInstance();
			this.maxTweets = sharedPreferences.getInt("numTweets", 0);
			this.numTweets = 0;
			this.totalTweets = 0;
			this.sleepTime = sharedPreferences.getInt("sleepTime", 0);
			this.intervalTime = sharedPreferences.getInt("intervalTime", 0);
			this.sleeping = false;
			this.isStopped = false;
			this.getLists();
		}

		public tweetBot(int maxTweets, int sleepTime, int intervalTime){
			ConfigurationBuilder config = new ConfigurationBuilder();
			config.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			config.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

			// Access Token
			String access_token = sharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
			// Access Token Secret
			String access_token_secret = sharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

			config.setOAuthAccessToken(access_token);
			config.setOAuthAccessTokenSecret(access_token_secret);

			this.botTwitter = new TwitterFactory(config.build()).getInstance();
			this.maxTweets = maxTweets;
			this.numTweets = 0;
			this.totalTweets = 0;
			this.sleepTime = sleepTime;
			this.intervalTime = intervalTime;
			this.sleeping = false;
			this.isStopped = false;

			this.getLists();
		}

		public void getLists(){
			//get lists
			Gson gson = new Gson();
			String jsonSearches = sharedPreferences.getString("searchList", "");
			Type type = new TypeToken<ArrayList<String>>(){}.getType();
			this.searchQueries = gson.fromJson(jsonSearches, type) ;
			String jsonReplies = sharedPreferences.getString("repliesList", "");
			this.replyList = gson.fromJson(jsonReplies, type);
		}

		public void run() throws TwitterException, InterruptedException {
			Toast.makeText(MainActivity.this, "Bot Started!", Toast.LENGTH_SHORT).show();
			this.isStopped = false;
			execute = new runBot();
			execute.execute();
		}

		public void stop(){
			if(isSleeping() && !isStopped()) {
				this.isStopped = true;
				this.sleeping = false;
				this.numTweets = 0;
				execute.stop();
				bot = new tweetBot();
				status.setText(getBotStatus());
				Toast.makeText(MainActivity.this, "The bot has been stopped", Toast.LENGTH_SHORT).show();
			}
			else Toast.makeText(MainActivity.this, "The bot has already been stopped!", Toast.LENGTH_SHORT).show();
		}

		public void pause(){
			if(isActive()) {
				this.sleeping = true;
				execute.pause();
			}
		}

		public void wakeUp(){
			if(!isActive()) {
				this.sleeping = false;
				execute.wakeUp();
			}
		}

		public Status findTweet(String query) throws TwitterException{
			//create a new search
			Query mySearch = new Query(query);
			QueryResult result;
			//get the results from that search
			result = botTwitter.search(mySearch);

			//generate a number to get a random tweet from list
			int randomTweet = (int)(Math.random() * result.getTweets().size());
			//get tweet from list
			Status tweetResult = result.getTweets().get(randomTweet);

			Log.e("Tweet found: ", tweetResult.getText());

			return tweetResult;
		}

		public String getRandomQuery(){
			int randomSearch = 0;
			if(searchQueries.size() > 1)
				randomSearch = (int)(Math.random() * searchQueries.size());

			String query = searchQueries.get(randomSearch);
			Log.e("Search Query: ", query);
			return "\"" + query + "\"";
		}

		public String getRandomReply(){
			int randomReply = 0;
			if(replyList.size() > 1)
				randomReply = (int)(Math.random() * replyList.size());

			String reply = replyList.get(randomReply);
			Log.e("Reply: ", reply);
			return reply;
		}

		public void reply(Status tweet, String reply) throws TwitterException{
		    StatusUpdate tweetBack = new StatusUpdate("@" + tweet.getUser().getScreenName() + " " + reply);
			this.botTwitter.updateStatus(tweetBack.inReplyToStatusId(tweet.getId()));
			this.numTweets++;
			this.totalTweets++;
		}

		public void tweet(String tweet) throws TwitterException{
			Status status = this.botTwitter.updateStatus(tweet);
		}

		public void searchAndReply() throws TwitterException {
			//get search query from list
			String searchQuery = getRandomQuery();
			//find a tweet
			Status tweet = findTweet(searchQuery);
			//get reply from list
			String reply = getRandomReply();
			//reply to tweet and increment numTweets
			reply(tweet, reply);
		}

		public boolean isActive(){
			return !sleeping && !this.isStopped;
		}

		public boolean isSleeping(){
			return this.sleeping;
		}

		public boolean isStopped(){
			return this.isStopped;
		}

		public String getLocation(Status tweet){
			GeoLocation location = tweet.getGeoLocation();
			return location.toString();
		}

		public String removeHashtag(String tweet){
			//add padding to tweet to avoid out of bounds error
			tweet = " " + tweet + " ";
			//get portion of tweet upto hashtag
			String tweetToHash = tweet.substring(0, tweet.indexOf('#')-1);
			//get portion of tweet after hashtag
			String hashtag = tweet.substring(tweet.indexOf('#'), tweet.length());
			String tweetAfterHash = hashtag.substring(hashtag.indexOf(" "), hashtag.length());

			return tweetToHash + tweetAfterHash;
		}

		public String getBotStatus(){
			return this.toString();
		}

		public String toString(){
			return "STATUS:\n" +
					(isActive()? "Running!" : (isSleeping()? "I'm sleeping!" : "Stopped!")) +
					"\nNumber of Tweets Sent: " + this.totalTweets;
		}

		//the bot will run in the background
		private class runBot extends AsyncTask<Void, String, Void>{

			boolean pause = false;
			String watcher = "watching";
			//run bot in background thread
			@Override
			protected Void doInBackground(Void... params) {
				while(!isStopped) {
					sleeping = false;
					while (numTweets < maxTweets) {
						//search for a tweet and reply
						try {
							searchAndReply();
						} catch (TwitterException e) {
							e.printStackTrace();
						}
						//update status
						publishProgress(getBotStatus());
						Log.e("Status", this.toString());
						if(isCancelled())
							break;
						if(pause){
							synchronized (watcher){
								try {
									watcher.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								this.pause = false;
							}
						}

						//interval between tweets
						try {
							sleeping = true;
							Thread.sleep(intervalTime * 60 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//reset tweets counter
					numTweets = 0;
					//take a break
					sleeping = true;
					//update status
					publishProgress(getBotStatus());
					if(isCancelled())
						break;
					try {
						Log.e("State", (!isActive()? "Went to sleep":"Still active"));
						Thread.sleep(sleepTime * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}

			//to send bot status to UI
			@Override
			protected void onProgressUpdate(String... values) {
				super.onProgressUpdate(values);
				status.setText(values[0]);
			}

			public void stop(){
				this.cancel(true);
				Log.e("State", "Stopped");
			}

			public void pause(){
				this.pause = true;
				Log.e("State", "Paused");
			}

			public void wakeUp(){
				synchronized (watcher) {
					this.watcher.notify();
				}
				Log.e("State", "Resumed");
			}
		}
	}
}
