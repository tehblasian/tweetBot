package com.tehblasian.twitterbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RepliesActivity extends AppCompatActivity {

    private ArrayList<String> replyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);

        //receive reply list from settings activity
        Intent i = getIntent();
        this.replyList = i.getStringArrayListExtra("replyList");
        //create array adapter
        final ReplyAdapter repliesAdapter = new ReplyAdapter(this, replyList);
        //retrieve list view
        ListView listView = (ListView) findViewById(R.id.reply_list);
        //set adapter to list view
        listView.setAdapter(repliesAdapter);

        //get delete all button
        Button deleteAll = (Button)findViewById(R.id.delete_all);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replyList.size() > 0) {
                    replyList.clear();
                    repliesAdapter.notifyDataSetChanged();
                    Toast.makeText(RepliesActivity.this, "All Replies Deleted!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(RepliesActivity.this, "No Replies To Delete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putStringArrayListExtra("replyList", replyList);
        setResult(RESULT_OK, i);
        finish();
    }
}
