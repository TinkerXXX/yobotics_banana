package com.yobotics.control.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

public class MyUtil {
	/**
	 * 描述：判断网络是否有效.
	 *
	 * @param context the context
	 * @return true, if is network available
	 */
	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	/**
	 * 服务是否存在
	 * @param context
	 * @param ServiceClass
	 * @return
	 */
	static public boolean CheckServiceExists(Context context, String ServiceClass)
	{
		boolean mReturn=false;
		
		ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> mServiceList = am.getRunningServices(80);
		for (ActivityManager.RunningServiceInfo mInfo : mServiceList)
		{
			String aa=mInfo.service.getClassName();
			if (aa.compareTo(ServiceClass)==0)
			{
				if (mInfo.started)
					mReturn=true;
				break;
			}
		}
		return mReturn;
	}
	/**
	 * 检查电话号码是否合法
	 * @param number
	 * @return
	 */
	public static boolean checkValidePhoneNumber(String number)
	{
		String[] p={"00000","11111","22222","33333","44444","55555","66666","77777","88888","99999"};
		for (int i=0;i<10;i++)
			if (number.contains(p[i]))
				return false;
		return true;
	}
	
	static public void Sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
	}
	
	static public int getIntValue(String s, int def) {
		int v = def;
		try {
			v = Integer.parseInt(s.substring(s.indexOf("=") + 1));
		} catch (Exception e) {
		}
		return v;
	};

	static public String getStringValue(String s) {
		try {
			return s.substring(s.indexOf("=") + 1);
		} catch (Exception e) {
		}
		return "";
	};
	/**
	 * 将整形转换成一个Ip地址
	 * @param longIp
	 * @return
	 */
	static public String longToIPForServer(long longIp){
        StringBuffer sb = new StringBuffer("");
        
        sb.append(String.valueOf((longIp & 0x000000FF)));
        sb.append(".");
        
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        
        sb.append(String.valueOf((longIp >>> 24)));
        return sb.toString();
    }
	/**
	 * 将一个Ip地址转换成整形
	 * @param strIp
	 * @return
	 */
	static public long ipToLong(String strIp){
        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);

        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1+1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2+1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3+1));
        return (ip[3] << 24) + (ip[2] << 16) + (ip[1] << 8) + ip[0];
    }
}
