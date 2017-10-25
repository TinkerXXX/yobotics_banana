package com.framework.push.android;

/**
 *  开发者通过自有的alias进行推送, 可以针对单个或者一批alias进行推送，也可以将alias存放到文件进行发送。
 * @author yongbo002
 *
 */
public class AndroidCustomizedcast extends AndroidNotification {
	public AndroidCustomizedcast() {
		try {
			this.setPredefinedKeyValue("type", "customizedcast");	
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	 
}
