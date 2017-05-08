package com.tehblasian.twitterbot;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jeremiah-David Wreh on 26/03/2017.
 */

public class ReplyAdapter extends ArrayAdapter<String> {
    private ArrayList<String> replyList;

    public ReplyAdapter(Activity context, ArrayList<String> replyList){
        super(context, 0, replyList);
        this.replyList = replyList;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //check if existing view is being reused, else inflate it
        View replyListView = convertView;
        if(replyListView == null){
            replyListView = LayoutInflater.from(getContext()).inflate(R.layout.reply_list_layout, parent, false);
        }

        //get the reply string located at this position in the list
        String reply = getItem(position);
        //get the textview in the layout that will hold the reply string
        TextView replyView = (TextView)replyListView.findViewById(R.id.reply_string);
        //set the text to the correct reply
        replyView.setText(reply);

        //get the delete button
        Button delete = (Button)replyListView.findViewById(R.id.delete_reply);
        //add x
        delete.setText("X");
        //add onclick
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyList.remove(position);
                notifyDataSetChanged();
            }
        });

        return replyListView;
    }
}
