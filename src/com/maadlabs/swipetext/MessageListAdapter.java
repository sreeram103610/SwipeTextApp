package com.maadlabs.swipetext;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<Messages> {

	Context context;
	int resource;
	ArrayList<Messages> messages = null;
	
	public MessageListAdapter(Context context, int resource, ArrayList<Messages> messages) {
		super(context, resource, messages);
		this.context = context;
		this.resource = resource;
		this.messages = messages;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
		MessagesHolder holder = null;
		
		if(row == null)
		{
			Log.i("row-null","row-null");
			LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(resource, parent, false);
			
			holder = new MessagesHolder();
			holder.id  = (TextView) row.findViewById(R.id.sendername);
			holder.message = (TextView) row.findViewById(R.id.messagecontent);
			holder.date = (TextView) row.findViewById(R.id.date);
			row.setTag(holder);
		}
		
		else
		{
			Log.i("holder-not-null","holder-not-null");
			holder = (MessagesHolder) row.getTag();
		}
		
		Messages message = messages.get(position);
		if(message.name!=null)
			holder.id.setText(message.name);
		else
			holder.id.setText(message.number);
		holder.message.setText(message.content);
		holder.date.setText(message.date);
		
		return row;
	}
	
	static class MessagesHolder
	{
		TextView id;
		TextView message;
		TextView date;
	}
	
}