package com.framework.amper;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.yobotics.control.demo.MyApplication;
import com.yobotics.control.util.MyUtil;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 * 重连接服务.
 */
public class ReConnectService extends Service {
	private Context context;
	@Override
	public void onCreate() {
		context = this;
		Log.i("TAG", "[服务]重连服务：启动");
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(reConnectionBroadcastReceiver, mFilter);
		super.onCreate();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onDestroy() {
	    Log.i("TAG", "[服务]重连服务：关闭");
		unregisterReceiver(reConnectionBroadcastReceiver);
		super.onDestroy();
	}
	BroadcastReceiver reConnectionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d("TAG", "----监听到网络状态改变----");
				boolean isAvailable = MyUtil.isNetworkAvailable(context);
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				
				if (isAvailable) {
					//Toast.makeText(context, "连接正常!", Toast.LENGTH_LONG).show();
					Intent intent1 = new Intent();
					intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
					intent1.putExtra("messageTitle","连接正常本机网络地址为"+getLocAddress());
					intent1.putExtra("messageContent", "连接正常本机网络地址为"+getLocAddress());
					context.sendBroadcast(intent1);
				} else {
					Intent intent1 = new Intent();
					intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
					intent1.putExtra("messageTitle","网络好像出问题了,请检查网络状态哦");
					intent1.putExtra("messageContent", "网络好像出问题了,请检查网络状态");
					context.sendBroadcast(intent1);
				//	Toast.makeText(context, "网络好像出问题了,请检查网络状态哦!", Toast.LENGTH_LONG).show();
				}
			}

		}

	};
	
	
	  //获取本地ip地址
	  public String getLocAddress(){
	     
	    String ipaddress = "";
	     
	    try {
	      Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	      // 遍历所用的网络接口
	      while (en.hasMoreElements()) {
	        NetworkInterface networks = en.nextElement();
	        // 得到每一个网络接口绑定的所有ip
	        Enumeration<InetAddress> address = networks.getInetAddresses();
	        // 遍历每一个接口绑定的所有ip
	        while (address.hasMoreElements()) {
	          InetAddress ip = address.nextElement();
	          if (!ip.isLoopbackAddress()
	              && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
	            ipaddress = ip.getHostAddress();
	          }
	        }
	      }
	    } catch (SocketException e) {
	      Log.e("", "获取本地ip地址失败");
	      e.printStackTrace();
	    }
	     
	    System.out.println("本机IP:" + ipaddress);
	     
	    return ipaddress;
	 
	  }
	
}
