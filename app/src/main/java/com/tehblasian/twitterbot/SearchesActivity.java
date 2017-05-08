package com.tehblasian.twitterbot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchesActivity extends AppCompatActivity {

    private ArrayList<String> searchList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searches);

        //receive reply list from settings activity
        Intent i = getIntent();
        this.searchList = i.getStringArrayListExtra("searchList");
        //create array adapter
        final ReplyAdapter searchAdapter = new ReplyAdapter(this, searchList);
        //retrieve list view
        ListView listView = (ListView) findViewById(R.id.search_list);
        //set adapter to list view
        listView.setAdapter(searchAdapter);

        //get delete all button
        Button deleteAll = (Button)findViewById(R.id.delete_all_searches);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchList.size() > 0) {
                    searchList.clear();
                    searchAdapter.notifyDataSetChanged();
                    Toast.makeText(SearchesActivity.this, "All Searches Deleted!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(SearchesActivity.this, "No Searches To Delete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putStringArrayListExtra("searchList", searchList);
        setResult(RESULT_OK, i);
        finish();
    }
}

