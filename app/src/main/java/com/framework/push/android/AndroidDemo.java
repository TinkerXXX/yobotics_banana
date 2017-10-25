package com.framework.push.android;
 


public class AndroidDemo {
	private String appkey = AppConten.appkey_android;
	private String appMasterSecret = AppConten.appMasterSecret_android;////服务器秘钥，
	private String timestamp = null;
	
	public AndroidDemo( ) {
		try {
			 
			timestamp = Integer.toString((int)(System.currentTimeMillis() / 1000));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	 
	 
 
	
	public static void main(String[] args) {
		// TODO set your appkey and master secret here   your appkey  the app master secret
		AndroidDemo demo = new AndroidDemo();
		try {
			demo.sendAndroidCustomizedcast("yobotics123","01011");
			/* TODO these methods are all available, just fill in some fields and do the test
			 * demo.sendAndroidBroadcast();
			 * demo.sendAndroidGroupcast();
			 * demo.sendAndroidCustomizedcast();
			 * demo.sendAndroidFilecast();
			 * 
			 * demo.sendIOSBroadcast();
			 * demo.sendIOSUnicast();
			 * demo.sendIOSGroupcast();
			 * demo.sendIOSCustomizedcast();
			 * demo.sendIOSFilecast();
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	 
	 

	//自定义播(customizedcast): 开发者通过自有的alias进行推送, 可以针对单个或者一批alias进行推送，也可以将alias存放到文件进行发送。
	public void sendAndroidCustomizedcast(String alias, String text) throws Exception {
			AndroidCustomizedcast customizedcast = new AndroidCustomizedcast();
			customizedcast.setAppMasterSecret(appMasterSecret);
			customizedcast.setPredefinedKeyValue("appkey", this.appkey);
			customizedcast.setPredefinedKeyValue("timestamp", this.timestamp);
			// TODO Set your alias here, and use comma to split them if there are multiple alias.
			// And if you have many alias, you can also upload a file containing these alias, then 
			// use file_id to send customized notification.
	 		customizedcast.setPredefinedKeyValue("alias", alias);
			// TODO Set your alias_type here
	 		customizedcast.setPredefinedKeyValue("alias_type", "yobotics");	
			customizedcast.setPredefinedKeyValue("ticker", alias);
			customizedcast.setPredefinedKeyValue("title",  alias);
			customizedcast.setPredefinedKeyValue("text",   text);
			customizedcast.setPredefinedKeyValue("after_open", "go_custom");
			customizedcast.setPredefinedKeyValue("display_type", "message");
			customizedcast.setPredefinedKeyValue("custom", text);
			// TODO Set 'production_mode' to 'false' if it's a test device. 
			// For how to register a test device, please see the developer doc.
			customizedcast.setPredefinedKeyValue("production_mode", "true");
			customizedcast.send();
		}
	

}
