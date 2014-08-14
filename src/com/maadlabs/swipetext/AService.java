package com.maadlabs.swipetext;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class AService extends Service
{
  private SmsListener smsReceiver;
  final IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

  @Override
    public void onCreate()
    {
     smsFilter.setPriority(1000);
     this.smsReceiver = new SmsListener();
     this.registerReceiver(this.smsReceiver, smsFilter);
    }

@Override
public IBinder onBind(Intent arg0) {
	// TODO Auto-generated method stub
	return null;
}
}