package com.tehblasian.twitterbot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.tehblasian.twitterbot.R.id.increment1;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static int numTweets;
    private static int interval;
    private static int sleep;

    private static final int MIN_INTERVAL = 15;
    private static final int MIN_SLEEP = 15;

    //shared preferences to save important values
    SharedPreferences sharedPreferences;

    //array list to store replies
    private ArrayList<String> replyList = new ArrayList<String>();
    //array list to store searches
    private ArrayList<String> searchList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get preferences file
        sharedPreferences = getSharedPreferences("userPreferences", Context.MODE_PRIVATE);

        this.numTweets = sharedPreferences.getInt("numTweets", 5);
        this.sleep = sharedPreferences.getInt("sleepTime", 30);
        this.interval = sharedPreferences.getInt("intervalTime", 15);

        //get lists
        Gson gson = new Gson();
        String jsonSearches = sharedPreferences.getString("searchList", "");
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        this.searchList = gson.fromJson(jsonSearches, type) ;
        String jsonReplies = sharedPreferences.getString("repliesList", "");
        this.replyList = gson.fromJson(jsonReplies, type);

        //if lists are not yet initialized, do it
        if(this.replyList == null)
            this.replyList = new ArrayList<String>();
        if(this.searchList == null)
            this.searchList = new ArrayList<String>();

        //get value text views and keep them updated
        TextView tweetNumber = (TextView)findViewById(R.id.tweets_sleep);
        TextView intervalTime = (TextView)findViewById(R.id.interval_time);
        TextView sleepTime = (TextView)findViewById(R.id.sleep_time);

        tweetNumber.setText(""+numTweets);
        intervalTime.setText(""+interval);
        sleepTime.setText(""+sleep);

        //set action for add reply
        Button addReply = (Button)findViewById(R.id.add_reply);
        addReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText replyQuery = (EditText)findViewById(R.id.reply);
                String reply = replyQuery.getText().toString();
                replyList.add(reply);
                replyQuery.setText("");
                Toast.makeText(SettingsActivity.this, "Reply Added!", Toast.LENGTH_SHORT).show();
            }
        });

        //set action for add search
        Button addSearch = (Button)findViewById(R.id.add_search);
        addSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText searchQuery = (EditText)findViewById(R.id.search_query);
                String reply = searchQuery.getText().toString();
                searchList.add(reply);
                searchQuery.setText("");
                Toast.makeText(SettingsActivity.this, "Search Added!", Toast.LENGTH_SHORT).show();
            }
        });

        //get increment buttons
        Button increment1 = (Button)findViewById(R.id.increment1);
        Button increment2 = (Button)findViewById(R.id.increment2);
        Button increment3 = (Button)findViewById(R.id.increment3);
        //get decrement buttons
        Button decrement1 = (Button)findViewById(R.id.decrement1);
        Button decrement2 = (Button)findViewById(R.id.decrement2);
        Button decrement3 = (Button)findViewById(R.id.decrement3);

        //get view searches button
        Button viewSearches = (Button)findViewById(R.id.view_searches);
        viewSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create intent to open the settings activity
                Intent searchesIntent = new Intent(SettingsActivity.this, SearchesActivity.class);
                //add list of words to intent
                searchesIntent.putStringArrayListExtra("searchList", searchList);
                //start the new activity
                startActivityForResult(searchesIntent, 2);
            }
        });

        //get view replies button
        Button viewReplies = (Button)findViewById(R.id.view_replies);
        viewReplies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create intent to open the settings activity
                Intent repliesIntent = new Intent(SettingsActivity.this, RepliesActivity.class);
                //add list of words to intent
                repliesIntent.putStringArrayListExtra("replyList", replyList);
                //start the new activity
                startActivityForResult(repliesIntent, 1);
            }
        });

        //get save changes button //TODO save settings
        Button save = (Button)findViewById(R.id.save_changes);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //edit preferences file
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //add important values to editor
                editor.putInt("numTweets", numTweets);
                editor.putInt("intervalTime", interval);
                editor.putInt("sleepTime", sleep);
                //add lists to prefs using gson
                Gson gson = new Gson();
                String jsonSearches = gson.toJson(searchList);
                String jsonReplies = gson.toJson(replyList);
                editor.putString("searchList", jsonSearches);
                editor.putString("repliesList", jsonReplies);
                //commit changes
                editor.commit();
                Toast.makeText(SettingsActivity.this, "Changes Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        //set listeners
        increment1.setOnClickListener(this);
        increment2.setOnClickListener(this);
        increment3.setOnClickListener(this);
        decrement1.setOnClickListener(this);
        decrement2.setOnClickListener(this);
        decrement3.setOnClickListener(this);
    }

    public void onClick(View v) {
        int i;
        TextView t;
       switch (v.getId()){
           case increment1:
               t = (TextView)findViewById(R.id.tweets_sleep);
               display(++numTweets, t);
               break;
           case R.id.increment2:
               t = (TextView)findViewById(R.id.interval_time);
               interval = interval + 15;
               display(interval, t);
               break;
           case R.id.increment3:
               t = (TextView)findViewById(R.id.sleep_time);
               sleep = sleep + 15;
               display(sleep, t);
               break;
           case R.id.decrement1:
               t = (TextView)findViewById(R.id.tweets_sleep);
               if(numTweets > 1) display(--numTweets, t);
               break;
           case R.id.decrement2:
               t = (TextView)findViewById(R.id.interval_time);
               if(interval > MIN_INTERVAL){
                   interval = interval - 15;
                   display(interval, t);
               }
               break;
           case R.id.decrement3:
               t = (TextView)findViewById(R.id.sleep_time);
               if(sleep > MIN_SLEEP){
                   sleep = sleep - 15;
                   display(sleep, t);
               }
               break;
       }
    }

    public void display(int number, TextView target){
        target.setText("" + number);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK)
            this.replyList = data.getStringArrayListExtra("replyList");
        if(requestCode == 2 && resultCode == RESULT_OK)
            this.searchList = data.getStringArrayListExtra("searchList");
    }
}
