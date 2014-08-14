package com.maadlabs.swipetext;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat" })
public class ViewMessageActivity extends Activity {

	
	Message message;
	ArrayList<Message> allMessages;
	ListView listview;
	EditText composeBox;
	Button sendButton;
	String intentPhoneNumber, intentMessage, intentName;
	DisplayMessageAdapter adapter;
	private BroadcastReceiver sendBroadcastReceiver;
	private BroadcastReceiver deliveryBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		Bundle extras = getIntent().getExtras();
		
		
		intentPhoneNumber = extras.getString("Value1");
		intentMessage = extras.getString("Value2");
		intentName = extras.getString("Value3");
		
		ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(intentPhoneNumber);
		if(intentName != null)
			actionBar.setTitle(intentName); 
		else
			actionBar.setTitle("Unknown");
				
		if(intentPhoneNumber.length()>10)
		{
			intentPhoneNumber = intentPhoneNumber.substring(intentPhoneNumber.length()-10, intentPhoneNumber.length());
		}
		displayMessage();
		
		
		composeBox = (EditText) findViewById(R.id.compose);
		sendButton = (Button) findViewById(R.id.send_message);
		
		setListener(composeBox,sendButton, intentPhoneNumber);
		if(!intentMessage.equals("!@#$%null!@#$%"))
			commonFunctionsOnIntent(intentMessage);
		
	}
	

	
	public void displayMessage()
	{
		
		allMessages = new ArrayList<Message>();
		
		Log.i("display_message","display_message");
		Uri mTable = Uri.parse("content://sms");

		Cursor cursor = this.getContentResolver().query(mTable, new String[] {"_id",  "address",  "date", "body",  "type", "read" }, "address LIKE '%"+intentPhoneNumber+"'", null, "date" + " COLLATE LOCALIZED DESC");
		
		if(cursor.getCount()>0)
		{
		Log.i("number",intentPhoneNumber);
		cursor.moveToLast();
		do
		{
			
			if(cursor.getInt(cursor.getColumnIndex("type")) == 1)
			{
				message = new Message(null,cursor.getString(cursor.getColumnIndex("body")));
				Log.i("message/1", cursor.getString(cursor.getColumnIndex("body")));
			}
			else if(cursor.getInt(cursor.getColumnIndex("type")) == 2)
			{
				
				message = new Message(cursor.getString(cursor.getColumnIndex("body")),null);
			}
			allMessages.add(message);
		}while(cursor.moveToPrevious());
		Log.i("count",Integer.toString(allMessages.size()));
		
		}
		populateView(allMessages);
		cursor.close();
	}
	
	public void updateView(Message newMessage, ArrayList<Message> allMessages, DisplayMessageAdapter adapter)
	{
		
		allMessages.add(newMessage);
		adapter.notifyDataSetChanged();
		
	}
	
	public void populateView(ArrayList<Message> allMessages)
	{
		adapter = new DisplayMessageAdapter(getApplicationContext() , R.layout.sent_message_layout, R.layout.received_message_layout, allMessages);
		listview = (ListView) findViewById(R.id.messageview);
		listview.setAdapter(adapter);
		listview.setSelection(listview.getAdapter().getCount()-1);
		ListView l = listview;
		l.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		Log.i("allMessagesSize",Integer.toString(allMessages.size()));
	}
	
	/*protected void onStop()
	{
	    unregisterReceiver(sendBroadcastReceiver);
	    unregisterReceiver(deliveryBroadcastReceiver);
	    super.onStop();
	}*/
	
	public void sendMessage(final EditText composeBox, Button sendButton, final String number, final Context context)
	{
		String message = new String();
		message = composeBox.getText().toString();
		SmsManager sms = SmsManager.getDefault();
		
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
	            new Intent(SENT), 0);

	        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
	            new Intent(DELIVERED), 0);

	    	sms.sendTextMessage(number, null, message, sentPI, deliveredPI); 
	        //---when the SMS has been sent---
	        sendBroadcastReceiver = new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(context, "SMS sent", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(context, "Generic failure", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(context, "No service", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Toast.makeText(context, "Null PDU", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(context, "Radio off", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                }
	            }
	        };

	        //---when the SMS has been delivered---
	        deliveryBroadcastReceiver = new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS delivered", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case Activity.RESULT_CANCELED:
	                        Toast.makeText(getBaseContext(), "SMS not delivered", 
	                                Toast.LENGTH_SHORT).show();
	                        break;                        
	                }
	            }
	        };
	
	        registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
	        registerReceiver(sendBroadcastReceiver , new IntentFilter(SENT));
		
	}
	
	public void setListener(final EditText composeBox, final Button sendButton, final String number)
	{
		

		sendButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(composeBox.getText().toString().length()<=0)
					{
						Toast.makeText(getApplicationContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
					}
					else
					{
						sendMessage(composeBox, sendButton, number, getApplicationContext());
						commonFunctionsOnIntent(intentMessage);
						
					}
				
		}});
	}
	
	public void commonFunctionsOnIntent(String message)
	{
		String tempMessage = new String();
		if(!message.equals("!@#$%null!@#$%"))
		{
			tempMessage = message;
			Log.i("if-part", "if-part");
		}
		else
		{
			tempMessage = composeBox.getText().toString();
			Log.i("else-part", "else-part");
		}
		Message newMessage = new Message(tempMessage,null);
		Log.i("message", tempMessage);
		
		updateView(newMessage, allMessages, adapter);
		composeBox.setText(null);
		listview.setSelection(listview.getAdapter().getCount()-1);
	}
}