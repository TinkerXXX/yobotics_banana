
package com.framework.amper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.yobotics.control.demo.MainActivity;
import com.yobotics.control.util.MyUtil;

/**
 * 开机广播 作用开机启动
 * @author yongbo002
 *
 */
public class BootReceiver extends BroadcastReceiver {
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext=context; 
		Intent i=new Intent();
		i.setClass(mContext, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
		mContext.startActivity(i);
		if (!MyUtil.CheckServiceExists(context, "com.framework.amper.ReConnectService")){
			Handler mHandler=new Handler();
			mHandler.postDelayed(new Runnable(){
				public void run()
				{
					Intent FafaServiceIntent = new Intent(Intent.ACTION_MAIN);
					FafaServiceIntent.setClass(mContext, ReConnectService.class);
					mContext.startService(FafaServiceIntent);
					
				 
				}
			}, 37000);
		}
	}
}
