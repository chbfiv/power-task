package com.mtelab.tasktags.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OAuthReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(OAuthHelper.INTENT_ON_AUTH))
		{
			String authToken = intent.getStringExtra(OAuthHelper.INTENT_EXTRA_AUTH_TOKEN);
			onAuthToken(context, authToken);
		}
	}
	
	public void onAuthToken(Context context, String authToken) {

	}
}
