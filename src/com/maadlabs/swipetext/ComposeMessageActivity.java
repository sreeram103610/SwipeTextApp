package com.maadlabs.swipetext;

import java.util.ArrayList;



import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeMessageActivity extends Activity {

	Button button;
	private AutoCompleteTextView edittext1;
	private EditText composeBox;
	private ArrayList<String> CONTACTS_NAME, CONTACTS_NUMBER, CONTACTS;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_message);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        showContacts();
        setSuggestions();
        setMessageView();
        
	}
	
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
	        registerReceiver(new BroadcastReceiver(){
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
	        }, new IntentFilter(SENT));

	        //---when the SMS has been delivered---
	        registerReceiver(new BroadcastReceiver(){
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
	        }, new IntentFilter(DELIVERED));  
	
		
	}
	
	
	public void setMessageView()
	{
		Button sendButton = (Button) findViewById(R.id.send_message);
		sendButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				composeBox = (EditText) findViewById(R.id.composeMessage);
				if(edittext1.getText().toString().length() == 0)
				{
					Toast.makeText(getApplicationContext(), "Recipient cannot be empty", Toast.LENGTH_SHORT).show();
				}
				else if(composeBox.getText().toString().length() == 0)
				{
					Toast.makeText(getApplicationContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
				}
				else
				{
				String number = new String();
				String name = new String();
				int tmp = (CONTACTS.indexOf(edittext1.getText().toString()));
				if(tmp!=-1)
				{
					number = CONTACTS_NUMBER.get(tmp);
					name = CONTACTS_NAME.get(tmp);
				}
				else
				{
					number = edittext1.getText().toString();
					name = "Unknown";
				}
				sendMessage(composeBox, button, number, getApplicationContext());	
				
				Intent intent = new Intent(ComposeMessageActivity.this, ViewMessageActivity.class);
				intent.putExtra("Value1", number);
				intent.putExtra("Value2", composeBox.getText().toString());
				intent.putExtra("Value3", name);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				
			}
			}
			
		});
	}
	
	public void setSuggestions()
	{
		edittext1 = (AutoCompleteTextView) findViewById(R.id.sendTo);
		
		
		ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,CONTACTS);
    	edittext1.setAdapter(adapter1);
    	
    	edittext1.addTextChangedListener(new TextWatcher() {         
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {                

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
       
    	adapter1.setNotifyOnChange(true);
    	
		edittext1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                    long id) {
            	
            	String item = parent.getItemAtPosition(pos).toString();
                edittext1.setText(item);
               
            }
		});
		
	
	}
	
	
	
    public void showContacts()
    {
    	String tmp;
    	CONTACTS = new ArrayList<String>();
    	CONTACTS_NUMBER = new ArrayList<String>();
    	CONTACTS_NAME = new ArrayList<String>();
    	edittext1 = (AutoCompleteTextView) findViewById(R.id.sendTo);
    	
    	Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    	String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
    	                ContactsContract.CommonDataKinds.Phone.NUMBER};

    	Cursor people = getContentResolver().query(uri, projection, null, null, null);
        if(people.getCount()>0)
        {
    	int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
    	int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
     
    	people.moveToFirst();
    	do {
    		String name   = people.getString(indexName);
    	    String number = people.getString(indexNumber);
    	    
    	    tmp = name+" "+number;
    	    CONTACTS.add(tmp);
    	    CONTACTS_NAME.add(name);
    	    CONTACTS_NUMBER.add(number);
    	} while (people.moveToNext());
    	
        }
       
    	 ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,CONTACTS);
     	 edittext1.setAdapter(adapter1);

    	
    }
    
    


}