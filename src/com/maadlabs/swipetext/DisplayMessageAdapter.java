package com.maadlabs.swipetext;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class DisplayMessageAdapter extends ArrayAdapter<Message> {

Context context;
int sentResource, rcvdResource;
ArrayList<Message> messages = null;

public DisplayMessageAdapter(Context context, int sentResource, int rcvdResource, ArrayList<Message> messages) {
    super(context, sentResource, rcvdResource, messages);
    this.context = context;
    this.sentResource = sentResource;
    this.rcvdResource = rcvdResource;
    this.messages = messages;
}



@SuppressLint("NewApi")
@Override
public View getView(int position, View convertView, ViewGroup parent) {

	View row = convertView;
    Message message = messages.get(position);
    Holder holder;
    
    

    	if(row == null)
        {
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		holder = new Holder(row);
            row = inflater.inflate(R.layout.message_layout, parent, false);
            holder.sent  = (TextView) row.findViewById(R.id.sent_message_content);
            holder.rcvd = (TextView) row.findViewById(R.id.rcvd_message_content);
            row.setTag(holder);
        }
	    else
	    {
	    	Log.i("holder","holder");
	    	holder = (Holder) row.getTag();
	    }
    	if(message.sent != null)
        {
    		RelativeLayout.LayoutParams params = (LayoutParams) holder.sent.getLayoutParams();
    		params.setMargins(30, params.topMargin, params.rightMargin, params.bottomMargin);
    		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    		
    		holder.sent.setVisibility(View.VISIBLE);
    		holder.sent.setLayoutParams(params);
	        holder.sent.setBackgroundResource(R.drawable.sent);
	        holder.sent.setText(message.sent);
	        holder.rcvd.setVisibility(View.GONE);
	      
        }
    	
    	else
    	{
    		RelativeLayout.LayoutParams params = (LayoutParams) holder.rcvd.getLayoutParams();
    		params.setMargins(params.leftMargin, params.topMargin, 30, params.bottomMargin);
    		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    		
    		holder.rcvd.setVisibility(View.VISIBLE);
    		holder.rcvd.setLayoutParams(params);
    		holder.rcvd.setBackgroundResource(R.drawable.rcvd);
	        holder.rcvd.setText(message.received);
	        holder.sent.setVisibility(View.GONE);
    	}
    		
    

    return row;
    }


public class Holder {

	   private View row;
	   private TextView sent;
	   private TextView rcvd;

	   public Holder(View row) {
	      this.row = row;
	   }

	  
	   }
	
}