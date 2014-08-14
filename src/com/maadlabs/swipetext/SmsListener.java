package com.maadlabs.swipetext;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;

public class SmsListener extends BroadcastReceiver{

    
	@Override
    public void onReceive(Context context, Intent intent) 
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String messageReceived = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
           Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                messageReceived += msgs[i].getMessageBody().toString();
                messageReceived += "\n";        
            }
            String name = new String();
            //---display the new SMS message---
        
            String tmp = msgs[0].getOriginatingAddress();
            if(tmp.length()>10)
				tmp = tmp.substring(tmp.length()-10, tmp.length());
            Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(tmp));
			Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {PhoneLookup.DISPLAY_NAME}, null, null, null);
			// phonesCursor returns the name for the number
			if((phonesCursor.getCount()>0) && phonesCursor.moveToFirst()) {
			    name = phonesCursor.getString(0); // this is the contact name
			    if(name.length()>20)
			    {
			    	name = name.substring(0, 20)+"..";
			    }
			}
            createNotification(context, name, messageReceived);
        }                       
        abortBroadcast();
    }
    
    @SuppressLint("NewApi")
  		public void createNotification(Context context, String msgFrom,String msgBody) {
  	    		    // Prepare intent which is triggered if the
  	    		    // notification is selected
  	    		    Intent intent = new Intent(context, MainActivity.class);
  	    		    intent.putExtra("from", "notification");
  	    		    PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
  	    		   
  	    		    // Build notification
  	    		    // Actions are just fake
  	    		   
  	    		    Notification noti = new Notification.Builder(context)
  	    		        .setContentTitle("New message" + msgFrom)
  	    		        .setContentText(msgBody)
  	    		        .setSmallIcon(R.drawable.ic_launcher)
  	    		        .setContentIntent(pIntent)
  	    		        .build();
  	    		    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  	    		    // hide the notification after its selected
  	    		    noti.flags |= Notification.FLAG_AUTO_CANCEL;

  	    		    notificationManager.notify(0, noti);

  	    		  }
}