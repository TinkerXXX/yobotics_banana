package com.yobotics.control.demo;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iflytek.cloud.SpeechUtility;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MyApplication extends Application {
	private static MyApplication instance;
	/*
	 * 推送
	 */
	//private PushAgent mPushAgent;
	public static final String CALLBACK_RECEIVER_ACTION = "callback_receiver_action";
	
	public static final String CALLBACK_RECEIVER_ACTION2 = "callback_receiver_action_2";
	
	/*
	 * 情感陪护
	 */
	public static final String CALLBACK_QINGGANPEIHU_ACTION = "callback_naoz_action";
	// public static final String aliasType="xll";
	// 注册回调
//	public static IUmengRegisterCallback mRegisterCallback;
//	// 注销回调
//	public static IUmengUnregisterCallback mUnregisterCallback;

	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;

	public SerialPort getSerialPort() throws SecurityException, IOException,
            InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this); // getSharedPreferences("android_serialport_api.sample_preferences",
														// MODE_PRIVATE);
			String path = sp.getString("DEVICE", "/dev/ttyS3");
			int baudrate = Integer.decode(sp.getString("BAUDRATE", "9600"));

			Log.d("DEVICEDEVICE", "DEVICE:" + path + " DEVICE:"
					+ baudrate);
			/* Check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				//throw new InvalidParameterException();
				
				Log.d("DEVICEDEVICE", "DEVICE:" + path + " DEVICE:"+ baudrate);
				
			}else{

			/* Open the serial port */
				mSerialPort = new SerialPort(new File(path), baudrate, 0);
			}
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	public MyApplication() {
		instance = this;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {

		// 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用半角“,”分隔。
		// 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

		SpeechUtility.createUtility(MyApplication.this, "appid="+ getString(R.string.app_id));

		// 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
		// Setting.setShowLog(false);
		super.onCreate();
		pushMessage();
	}

	/**
	 * 清空上次登录参数
	 */
	public void clearLoginParams() {
		// Editor editor = mSharedPreferences.edit();
		// editor.clear();
		// editor.commit();
		mPushAgentDelliasTask();
	}

	/**
	 * 描述：注册推送服务
	 */
	public void mPushAgentDelliasTask() {
		// String jsbh = mPreferenceDao.readString("jsbh", "");
		// if (jsbh != null && !"".equals(jsbh)) {
		// new delTagTask(jsbh).execute();
		// }
		// String rybh = mPreferenceDao.readString("rybh", "");
		// if (rybh != null && !"".equals(rybh)) {
		// new DelAliasTask(rybh, "ydxy").execute();
		// }
	}
 
 
	public void pushMessage() {
//		mPushAgent = PushAgent.getInstance(this);
//		// 在测试时可以设置调试模式
//		// 注意 正式发布应用时，请务必将本开关关闭，避免影响用户正常使用APP。
//		mPushAgent.setDebugMode(true);
//		// 通知栏如何展示多条通知
//		// 默认情况下，使用SDK提供的通知类型，当设备收到多条通知时，通知栏只展示最新的一条通知，可以使用下面的方法来展示多条通知。
//		// 注意：请慎用此开关，以免用户看到多条通知时，会带来不好的用户体验。
//		mPushAgent.setMergeNotificaiton(false);
//		/**
//		 * 该Handler是在IntentService中被调用，故 1.
//		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK 2.
//		 * IntentService里的onHandleIntent方法是并不处于主线程中，因此，如果需调用到主线程，需如下所示;
//		 * 或者可以直接启动Service
//		 * 
//		 * 
//		 * 自定义通知栏样式 在消息推送的SDK里，有一个类UmengMessageHandler， 该类负责处理消息，包括通知消息和自定义消息。
//		 * 其中，有一个成员函数：getNotification，
//		 * 若SDK默认的消息展示样式不符合开发者的需求，可通过覆盖该方法来自定义通知栏展示样式
//		 * 
//		 * UmengMessageHandler 是负责消息的处理的
//		 * */
//		UmengMessageHandler messageHandler = new UmengMessageHandler() {
//			// dealWithCustomMessage() 方法负责处理自定义消息，需由用户处理
//			@Override
//			public void dealWithCustomMessage(final Context context,
//					final UMessage msg) {
//				new Handler(getMainLooper()).post(new Runnable() {
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						UTrack.getInstance(getApplicationContext()).trackMsgClick(msg, false);
////						Toast.makeText(context, "自定义数据类型" + msg.custom,
////								Toast.LENGTH_LONG).show();
//						Intent intent = new Intent();
//						intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
//						intent.putExtra("custom", msg.custom);
//						sendBroadcast(intent);
//
//					}
//				});
//			}
//
//			@Override
//			public Notification getNotification(Context context, UMessage msg) {
//				switch (msg.builder_id) {
//				case 1:
//
////					Toast.makeText(context,
////							"自定义dealWithCustomMessage" + msg.custom + msg.text,
////							Toast.LENGTH_LONG).show();
//					NotificationCompat.Builder builder = new NotificationCompat.Builder(
//							context);
//					RemoteViews myNotificationView = new RemoteViews(
//							context.getPackageName(),
//							R.layout.notification_view);
//					myNotificationView.setTextViewText(R.id.notification_title,
//							msg.title);
//					myNotificationView.setTextViewText(R.id.notification_text,
//							msg.text);
//					myNotificationView.setImageViewBitmap(
//							R.id.notification_large_icon,
//							getLargeIcon(context, msg));
//					myNotificationView.setImageViewResource(
//							R.id.notification_small_icon,
//							getSmallIconId(context, msg));
//					builder.setContent(myNotificationView);
//					builder.setAutoCancel(true);
//					Notification mNotification = builder.build();
//					// 由于Android
//					// v4包的bug，在2.3及以下系统，Builder创建出来的Notification，并没有设置RemoteView，故需要添加此代码
//					mNotification.contentView = myNotificationView;
//					return mNotification;
//				default:
//					// 默认为0，若填写的builder_id并不存在，也使用默认。
//					return super.getNotification(context, msg);
//				}
//			}
//		};
//		mPushAgent.setMessageHandler(messageHandler);
//
//		/**
//		 * UmengNotificationClickHandler，负责处理消息的点击事件。 该类主要有四个成员方法：
//		 * 该Handler是在BroadcastReceiver中被调用，故
//		 * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
//		 * */
//		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
//
//			// 打开应用
//			@Override
//			public void launchApp(Context arg0, UMessage arg1) {
//				// Toast.makeText(arg0, "自定义launchApp",
//				// Toast.LENGTH_LONG).show();
//				// TODO Auto-generated method stub
//				Toast.makeText(arg0, "自定义launchApp", Toast.LENGTH_LONG).show();
//
//				super.launchApp(arg0, arg1);
//			}
//
//			// 打开指定页面（Activity）
//			@Override
//			public void openActivity(Context arg0, UMessage arg1) {
//				// TODO Auto-generated method stub
//				// Intent intent = new Intent(CALLBACK_RECEIVER_ACTION);
//				// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				Toast.makeText(arg0, "自定义openActivity", Toast.LENGTH_LONG)
//						.show();
//				super.openActivity(arg0, arg1);
//			}
//
//			// 使用系统默认浏览器打开指定网页
//			@Override
//			public void openUrl(Context context, UMessage arg1) {
//				Toast.makeText(context, "自定义openUrl" + arg1.url,
//						Toast.LENGTH_LONG).show();
//				// TODO Auto-generated method stub
//				super.openUrl(context, arg1);
//
//			}
//
//			// 定义行为
//			@Override
//			public void dealWithCustomAction(Context context, UMessage msg) {
//				Toast.makeText(context, "自定义dealWithCustomAction",
//						Toast.LENGTH_LONG).show();
//
//			}
//
//		};
//		mPushAgent.setNotificationClickHandler(notificationClickHandler);
//		// 注册回调：IUmengRegisterCallback；当开启友盟推送时，可传入注册回调，即PushAgent.enable(IUmengRegisterCallback)
//		mRegisterCallback = new IUmengRegisterCallback() {
//
//			@Override
//			public void onRegistered(String registrationId) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(CALLBACK_RECEIVER_ACTION);
//				sendBroadcast(intent);
//			}
//
//		};
//		mPushAgent.setRegisterCallback(mRegisterCallback);
//		// 注销回调：IUmengUnregisterCallback；当关闭友盟推送时，可传入注销回调，即PushAgent.disable(IUmengUnregisterCallback)。
//		mUnregisterCallback = new IUmengUnregisterCallback() {
//
//			@Override
//			public void onUnregistered(String registrationId) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(CALLBACK_RECEIVER_ACTION);
//				sendBroadcast(intent);
//			}
//		};
//		mPushAgent.setUnregisterCallback(mUnregisterCallback);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

}
