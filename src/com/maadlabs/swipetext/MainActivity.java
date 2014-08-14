package com.maadlabs.swipetext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

@SuppressLint({ "SimpleDateFormat", "HandlerLeak" })
public class MainActivity extends Activity {

	
	public ArrayList<Messages> msgs;
	private ProgressDialog progressBar;
	public ListView listview;
	public int flag;
	private int maxTS;
	public MessageListAdapter adapter;
	public Messages messageFromContext;
	Handler handler;
	
	SmsListener smslistener = new SmsListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flag = 1;
		adapter = new MessageListAdapter(getApplicationContext() , R.layout.listitem_layout, msgs);
		setContentView(R.layout.activity_main);
		msgs = new ArrayList<Messages>();
		fetchMessageTask();
		displayMessage();
		registerForContextMenu(listview);
		this.registerReceiver(smslistener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, new MyContentObserver(handler));
		adapter.notifyDataSetChanged();
	    };
	    
	  @Override
	  public void onStop()
	  {
		  super.onStop();
		  this.unregisterReceiver(smslistener);
	  }
	
	
	
	    
	  
	 /*   private void receivedMessage(String message)
	    {
	        msgs.add(message);
	        adapter.notifyDataSetChanged();
	    }  */

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
	  if (v.getId()==R.id.messageslist) {
	    menu.setHeaderTitle("Options");
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    messageFromContext = (Messages) listview.getItemAtPosition(info.position);
	    String[] menuItems = {"View Message", "Delete Thread"};
	    for (int i = 0; i<menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String[] menuItems = {"View Message", "Delete Thread"};
	  String menuItemName = menuItems[menuItemIndex];
	  if(menuItemName.equals("View Message"))
	  {
		  openViewMessagesActivity(messageFromContext);
	  }
	  else
	  {
		  
		  msgs.remove(info.position);
		  adapter.notifyDataSetChanged();
	  }

	  

	  return true;
	}
	
	public void onResume()
	{
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	public void getMessages(ArrayList<Messages> messages)
	{
		
		Context context = getApplicationContext();
		msgs = messages;
		adapter = new MessageListAdapter(context , R.layout.listitem_layout, msgs);
		listview = (ListView) findViewById(R.id.messageslist);
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	
		
	}

	public class MessageRetrievalTask {
		
		int c;
		ContentResolver cr;
		ArrayList<Messages> messages;
	    ArrayList<Messages> data;
		MessageListAdapter myExistingAdapter;
		public String lastDate;
	
		public void onPreExecute()
		{
			c = 0;
			messages = new ArrayList<Messages>();
		}
		public ArrayList<Messages> doInBackground()
		{
					
			ArrayList<String> numbers = null;
			
			lastDate = new String();
			numbers = new ArrayList<String>();
			
			Messages message = null;
			Uri mTable = Uri.parse("content://sms");
			cr = getContentResolver();
			Cursor cursor = this.cr.query(mTable, new String[] {"_id",  "address",  "date", "body",  "type", "read" }, null, null, "date" + " COLLATE LOCALIZED DESC");
			
			// cursor has all the messages
			if(cursor.getCount()>0)
			{
				cursor.moveToFirst();
				do
				{
					String tmp = new String();
					String name = null;
					String number = new String();

					tmp = cursor.getString(cursor.getColumnIndex("address"));
					Log.i("Number", tmp);
					if(tmp.length()>10)
						tmp = tmp.substring(tmp.length()-10, tmp.length());
						
						if(numbers.contains(tmp) == false)
						{
							
						    numbers.add(tmp);
						
							Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(tmp));
							Cursor phonesCursor = getApplicationContext().getContentResolver().query(phoneUri, new String[] {PhoneLookup.DISPLAY_NAME}, null, null, null);
							// phonesCursor returns the name for the number
							if((phonesCursor.getCount()>0) && phonesCursor.moveToFirst()) {
							    name = phonesCursor.getString(0); // this is the contact name
							    if(name.length()>20)
							    {
							    	name = name.substring(0, 20)+"..";
							    }
							   
						}
							int currTS = cursor.getColumnIndex("date");
							if(maxTS<cursor.getColumnIndex("date"))
							{
								maxTS = currTS;
							}
							Date dateFromSms = new Date(cursor.getLong(cursor.getColumnIndex("date")));
							String formattedDate = new SimpleDateFormat("MMM dd").format(dateFromSms);
							message = new Messages(tmp, formattedDate, cursor.getString(cursor.getColumnIndex("body")));
							message.name = name;
							messages.add(message);
						}
						
				
				}while(cursor.moveToNext());
				
			}
			
			cursor.close();
			
			
			return messages;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.compose:
	            openCompose();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	public void openCompose()
	{
		Intent intent = new Intent(this, ComposeMessageActivity.class);
		startActivity(intent);
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
	}
	
	public void fetchMessageTask()
	{
		MessageRetrievalTask bgt = new MessageRetrievalTask();
		bgt.onPreExecute();
		getMessages(bgt.doInBackground());
		
	}
	
	@Override
	 public void onPause(){
	        super.onPause();
	        this.registerReceiver(smslistener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
	    }
	
	 
	public void displayMessage()
	{
		listview = (ListView) findViewById(R.id.messageslist);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			Messages message = (Messages) arg0.getAdapter().getItem(arg2);
			openViewMessagesActivity(message);
			
		}

		});

		
		
	}
	
	
	
	public void openViewMessagesActivity(Messages message)
	{
		Intent i = new Intent(getApplicationContext(), ViewMessageActivity.class);
		i.putExtra("Value1", message.number);
		i.putExtra("Value2", "!@#$%null!@#$%");
		i.putExtra("Value3", message.name);
		Log.i("number", message.number);
		
		startActivity(i); 
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		
	}
	
	public class MyContentObserver extends ContentObserver
	{

	    public MyContentObserver(Handler handler)
	    {
	        super(handler);
	    }

	    @Override
	    public void onChange(boolean selfChange)
	    {
	        runOnUiThread(new Runnable() {
	            public void run() {
	                fetchMessageTask();
	            }
	        });
	        super.onChange(selfChange);
	    }
	}

	
}
