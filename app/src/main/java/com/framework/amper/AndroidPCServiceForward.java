package com.framework.amper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.speech.setting.TtsSettings;
import com.yobotics.control.demo.Constant;
import com.yobotics.control.demo.MyApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.LinkedList;

import android_serialport_api.SerialPort;

/**
 * 一个Socket通信的完成，需要客户端和服务器端的配合，这个配合就好比，两个人在电话中聊天，当甲拨出号码，乙在另一头接起电话时，
 * 他们直接就已经搭建了一个沟通的桥梁，对于客户端和服务器端是一样的。客户端通过Socket向指定ip地址的某端口发出数据通信请求，
 * 而服务器端此时也正在监听该端口的情况，也就是说，对于指定的ip地址和端口号我们完全可以认为是甲乙电话通讯时的电话号码。
 * Socket通信分为两种一个是UDP，一个是TCP，UDP的好处是：速度快，但是缺点是此协议只管发送数据，并不管对方是否接收到数据，
 * 而TCP每次发出数据后都要等待对方传回数据做以肯定，然后再发送下一部分数据。但是速度不如UDP快。
 *
 * @author yongbo002
 */
public class AndroidPCServiceForward extends Service {
    Context context;

    private static Socket socket;
    final LinkedList<Socket> list = new LinkedList<Socket>();
    public Socket socketclient;
    private WifiManager wifiManager;
    WifiConfiguration apConfig = new WifiConfiguration();
    protected MyApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private SharedPreferences mSharedPreferences;
    byte[] retArrtmpe = null;
    // 花样动作
    byte[] retArrtmpe_hyzz = null;
    // 基本动作指令
    byte[] retArrtmpe_jbzd = null;
    int templenght = 0;// 标致
    int SoundCommond = 0;//0为无，1为前进，2为后退
    private MyReceiver receiver;

    @Override
    public void onCreate() {
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("forward");
        filter.addAction("stop");
        filter.addAction("back");
        filter.addAction("turnleft");
        filter.addAction("turnright");
        filter.addAction("sitdown");
        filter.addAction("shakehand");
        registerReceiver(receiver, filter);

        context = this;
        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME,
                MODE_PRIVATE);
        mApplication = (MyApplication) getApplication();


//		ClientThread clientthread = new ClientThread();
//		clientthread.start();

        new Thread() {
            @Override
            public void run() {
                ServerSocket serivce;
                try {
                    serivce = new ServerSocket(30012);
                    serivce.setReceiveBufferSize(255);
                    mSerialPort = mApplication.getSerialPort();
                    mOutputStream = mSerialPort.getOutputStream();// 写入
                    mInputStream = mSerialPort.getInputStream();// 读取
                    while (true) {
                        // 等待客户端连接
                        socket = serivce.accept();
                        list.add(socket);
                        InetAddress clientIP = socket.getInetAddress();
                        int clientPORT = socket.getPort();
                        new Thread(new SocketThread(socket)).start();// 多线程阻塞
                        new Thread(new SocketWriterThread(socket)).start();// 多线程阻塞
                        //	new ReadThread().start();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    //服务里面的一个方法
    public void callServiceMethod() {
        Toast.makeText(getApplicationContext(), "广播调用服务啦", Toast.LENGTH_SHORT).show();
    }

    //内部类实现广播接收者
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            mOutputStream = mSerialPort.getOutputStream();// 写入
            if (mOutputStream != null) {
                try {
                    if (intent.getAction().equals("forward"))
                        mOutputStream.write(Constant.FORWARD);
                    if (intent.getAction().equals("back"))
                        mOutputStream.write(Constant.BACK);
                    if (intent.getAction().equals("turnright"))
                        mOutputStream.write(Constant.TURNRIGHT);
                    if (intent.getAction().equals("turnleft"))
                        mOutputStream.write(Constant.TURNLEFT);
                    if (intent.getAction().equals("stop"))
                        mOutputStream.write(Constant.STOP);
                    if (intent.getAction().equals("sitdown"))
                        mOutputStream.write(Constant.SITDOWN);
                    if (intent.getAction().equals("shakehand"))
                        mOutputStream.write(Constant.SHAKEHAND);

                    mOutputStream.flush();
                    outStream.flush();
                    Thread.sleep(3100);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intentback = new Intent();
                intentback.setAction("OK");
                sendBroadcast(intentback);
            }

			
            /*if (intent.getAction().equals("forward")) {
                //SoundCommond=1;//前进
            	callServiceMethod();
				try{
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					mOutputStream = mSerialPort.getOutputStream();// 写入
					if (mOutputStream != null) {
						mOutputStream.write(Constant.FORWARD);
						mOutputStream.flush();
						outStream.flush();
						Thread.sleep(3100);
					
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				Intent intentback=new Intent();
				intentback.setAction("OK");
		        sendBroadcast(intentback);
			}
            if (intent.getAction().equals("stop")) {
            	callServiceMethod();
				//SoundCommond=1;//前进
				try{
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					mOutputStream = mSerialPort.getOutputStream();// 写入
					if (mOutputStream != null) {
						mOutputStream.write(Constant.STOP);
						mOutputStream.flush();
						outStream.flush();
						Thread.sleep(3100);
						
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				Intent intentback=new Intent();
				intentback.setAction("OK");
		        sendBroadcast(intentback);
			}*/
        }
    }

    public void sendSTC(byte[] s, Socket c) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(
                    c.getOutputStream());
            outputStream.write(s);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSTC(byte[] s) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(
                    socket.getOutputStream());
            outputStream.write(s);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理socket连接s
     */
    class SocketThread implements Runnable {
        private Socket socket;
        private String temp = "";

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @SuppressWarnings("unused")
        public void run() {
            //Intent intent2;
            //Bundle bundle = (Bundle)intent2.getExtras();
            //byte[] nametemp = new byte[255];
            //byte[] keytemp = new byte[255];

            try {
                while (true) {
                    int i_thy = 0;
                    BufferedInputStream inStream = new BufferedInputStream(
                            socket.getInputStream());
                    String sound = "";
                    byte[] a = {(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0xff};
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();///////////////////////
                    int c = -1;// 读取bis流中的下一个字节
                    int no = 0;
                    byte[] retArr = new byte[255];
                    //Toast.makeText(getApplicationContext(), "str", 0).show();
                    //ByteBuffer buffer = ByteBuffer.allocate(255);
                    //sendSTC(a);

//				while ((c = inStream.read()) != -1) {////////////////////////////////////////
                    while (true) {
//					outStream.write(c);
//					byte[] retArr = new byte[255];
//					retArr=	outStream.toByteArray();
                        c = inStream.read(retArr, no, 1);
                        no++;
                        if (retArr[0] == 35 || retArr[0] == 36 || retArr[0] == 115)
                            i_thy++;
                        if (retArr[no - 1] == -1 || retArr[no - 1] == 33) {
                            i_thy--;
                            break;
                        }
                    }
                    if (retArr[0] != 35 && retArr[0] != 36) {
                        byte[] temp = new byte[no];
                        for (int i = 0; i < no; i++)
                            temp[i] = retArr[i];
                        for (Socket s : list) {
                            sendSTC(temp, s);
                        }
                    }
                    if (retArr[0] == 35 && retArr[i_thy] == -1) {
                        int number = 0;
                        while (retArr[number] != -18) {
                            number++;
                        }
                        byte[] nametemp = new byte[number - 1];
                        byte[] keytemp = new byte[i_thy - number - 1];
                        for (int i = 1; i <= number - 1; i++)
                            nametemp[i - 1] = retArr[i];
                        for (int i = number + 1; i <= i_thy - 1; i++)
                            keytemp[i - number - 1] = retArr[i];
                        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        closeWifiHotspot();
                        wifiManager.setWifiEnabled(false);
                        setWifiApEnabled(true, nametemp, keytemp);
                        Intent intent = new Intent();
                        intent.setAction("AP");
                        sendBroadcast(intent);
//						int NO=0;
//						int temp=0;
//						for(int i = 1; i<=i_thy;i++){
//							if(retArr[i]!=-1 && NO == 0){
//							nametemp[i-1] = retArr[i];
//							}else if(retArr[i] == -1){
//								temp = i;NO=1;nametemp[i-1]='\0';
//							}else if(retArr[i]!=-18){
//								keytemp[i-temp-1] =retArr[i]; 
//							}else{
//								keytemp[i-temp-1]='\0';
//							}
                    }


                    /////////////////////////////////WIFI
                    if (retArr[0] == 36 && retArr[i_thy] == -1) {
                        int number = 0;
                        while (retArr[number] != -18) {
                            number++;
                        }
                        byte[] WIFInametemp = new byte[number - 1];
                        byte[] WIFIkeytemp = new byte[i_thy - number - 1];
                        for (int i = 1; i <= number - 1; i++)
                            WIFInametemp[i - 1] = retArr[i];
                        for (int i = number + 1; i <= i_thy - 1; i++)
                            WIFIkeytemp[i - number - 1] = retArr[i];
                        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        ConnectivityManager connectivityManager
                                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        closeWifiHotspot();
                        wifiManager.setWifiEnabled(false);
                        setmyWifiEnabled(true, WIFInametemp, WIFIkeytemp);
                        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        while (!wifiNetworkInfo.isConnected()) {
                            wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        }
//						Intent intent=new Intent();
//				        intent.setAction("NAP");
//				        sendBroadcast(intent);             

//						if(retArr[i_thy-1] == 1){
//							ClientThread clientthread = new ClientThread();
//							clientthread.start();
//						}

//						int NO=0;
//						int temp=0;
//						for(int i = 1; i<=i_thy;i++){
//							if(retArr[i]!=-1 && NO == 0){
//							nametemp[i-1] = retArr[i];
//							}else if(retArr[i] == -1){
//								temp = i;NO=1;nametemp[i-1]='\0';
//							}else if(retArr[i]!=-18){
//								keytemp[i-temp-1] =retArr[i]; 
//							}else{
//								keytemp[i-temp-1]='\0';
//							}
                    }

                    if ((no) == 6 && retArr[0] == -113
                            && retArr[(no) - 1] == -1 && retArr[(no) - 2] == 54 && retArr[(no) - 3] == 53) {
                        Intent intent = new Intent();
                        switch (retArr[(no) - 4]) {
                            case 1:
                                intent.setAction("1");
                                break;
                            case 2:
                                intent.setAction("2");
                                break;
                            case 3:
                                intent.setAction("3");
                                break;
                            case 4:
                                intent.setAction("4");
                                break;
                            case 5:
                                intent.setAction("5");
                                break;
                            case 6:
                                intent.setAction("6");
                                break;
                            case 7:
                                intent.setAction("7");
                                break;
                            case 8:
                                intent.setAction("8");
                                break;
                            case 9:
                                intent.setAction("9");
                                break;
                            case 10:
                                intent.setAction("10");
                                break;
                            case 11:
                                intent.setAction("11");
                                break;
                            case 12:
                                intent.setAction("12");
                                break;
                            case 13:
                                intent.setAction("13");
                                break;
                            case 14:
                                intent.setAction("14");
                                break;
                            case 15:
                                intent.setAction("15");
                                break;
                            case 16:
                                intent.setAction("16");
                                break;
                            case 17:
                                intent.setAction("17");
                                break;
                            case 18:
                                intent.setAction("18");
                                break;
                            case 19:
                                intent.setAction("19");
                                break;
                            case 20:
                                intent.setAction("20");
                                break;
                            case 21:
                                intent.setAction("21");
                                break;
                            case 22:
                                intent.setAction("22");
                                break;
                            case 23:
                                intent.setAction("23");
                                break;
                            case 24:
                                intent.setAction("24");
                                break;
                            case 25:
                                intent.setAction("25");
                                break;
                            case 26:
                                intent.setAction("26");
                                break;
                            case 27:
                                intent.setAction("27");
                                break;
                            case 28:
                                intent.setAction("28");
                                break;
                            case 29:
                                intent.setAction("29");
                                break;
                            case 30:
                                intent.setAction("30");
                                break;
                            case 31:
                                intent.setAction("31");
                                break;
                            case 32:
                                intent.setAction("32");
                                break;
                            case 33:
                                intent.setAction("33");
                                break;
                            case 34:
                                intent.setAction("34");
                                break;
                            case 35:
                                intent.setAction("35");
                                break;
                        }
                        context.sendBroadcast(intent);
                    }
                    //接收音乐
                    if ((no) == 4 && retArr[0] == -97
                            && retArr[(no) - 1] == -1) {
                        Intent intent = new Intent();
                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION2);
                        intent.putExtra("retArr", retArr);
                        context.sendBroadcast(intent);
                    }


                    if (retArr[0] == 115 && retArr[1] == 105) {
                        int i = 0;
                        while (retArr[i] != 33) {
                            i++;
                        }
                        byte[] temp = new byte[i];
                        for (int n = 0; n < i; n++)
                            temp[n] = retArr[n];
                        sound = new String(temp);
                        Intent intent = new Intent();
                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
                        intent.putExtra("custom", sound);
                        context.sendBroadcast(intent);
                    }


                    //开启音控
                    if ((no) == 4 && retArr[0] == -97
                            && retArr[(no) - 1] == -1 && retArr[(no) - 2] == 5) {
                        /*if (dialog != null && dialog.isShowing())
							dialog.dismiss();
						 
						mIatResults.clear();
						// 设置参数
						setParam();
						boolean isShowDialog = mSharedPreferences.getBoolean(
								getString(R.string.pref_key_iat_show), true);
						if (isShowDialog) {
							// 显示听写对话框
							mIatDialog.setListener(mRecognizerDialogListener);
							mIatDialog.show();
							showTip(getString(R.string.text_begin));
						} else {
							// 不显示听写对话框
							ret = mIat.startListening(mRecognizerListener);
							if (ret != ErrorCode.SUCCESS) {
								showTip("听写失败,错误码：" + ret);
							} else {
								showTip(getString(R.string.text_begin));
							}
						}*/
                    }
                    //读取信息(byte)0x8f,(byte)0x00,(byte)0x01,(byte)0x66,(byte)0x67,(byte)0xff
                    if (((no) == 6) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {
                        if (retArr[3] == 102) {
                            //String res = new String(a);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            bw.write("aaa");
                            bw.flush();

                        }
                    }
                    // 基本动作
                    if (((no) == 6) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {

                        if (retArr[1] == 49) {
                            retArrtmpe_jbzd = null;
                        } else {
                            retArrtmpe_jbzd = retArr;
                        }
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
////						if(retArr[(no) - 2] == 14){
////							for(Socket s:list){
////							sendSTC(Constant.GO,s);
////						}
////						}
//						byte[] temp = new byte[6];
//						for(int i = 0; i< no; i++)
//							temp[i] = retArr[i];
////						for(int i = 0 ; i < list.size() ; i++) {
////							  socket=list.get(i);
////							  sendSTC(temp);
////							}
//						for(Socket s:list){
//							sendSTC(temp,s);
//						}

                    }
                    if (((no) == 9) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {
                        if (retArr[1] == 21 || retArr[1] == 22) {
                            retArrtmpe_jbzd = null;
                        } else {
                            retArrtmpe_jbzd = retArr;
                        }
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }
                    if ((no) == 10 && retArr[0] == -113 && retArr[1] == 16 && retArr[(no) - 1] == -1) {
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }

                    if ((no) == 61 && retArr[0] == -113 && retArr[1] == 17 && retArr[(no) - 1] == -1) {
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }
                    if ((no) == 5 && retArr[0] == -113
                            && retArr[1] == -35
                            && retArr[(no) - 1] == -1) {
                        retArrtmpe_jbzd = null;
                        // 撒尿
                        // {(byte)0x8f,(byte)0xdd,(byte)0x01,(byte)0x01,(byte)0xff};
                        if (retArr[2] == 1 && retArr[3] == 1) {
                            retArrtmpe = retArr;
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_SANIAO[0]);
                                Thread.sleep(300);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[1]);
                                Thread.sleep(300);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[2]);
                                Thread.sleep(2000);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[3]);
                                Thread.sleep(300);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[4]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[5]);
                                Thread.sleep(1100);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[6]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[7]);
                                Thread.sleep(1100);
                                //mOutputStream.write(Constant.SANIAO[0]);
                                //mOutputStream.write(Constant.SANIAO[1]);
                                //mOutputStream.write(Constant.SANIAO[2]);
                                //mOutputStream.flush();
                                //outStream.flush();
                            }
                        }
						
						/*
						if (retArr[2] == 4 && retArr[3] == 4) {
							retArrtmpe = retArr;
							templenght=0;
							OutputStream mOutputStream = mSerialPort
									.getOutputStream();// 写入
							if (mOutputStream != null) {
								mOutputStream.write(Constant.HUAYANG_90[0]);
								Thread.sleep(1100);
								mOutputStream.write(Constant.HUAYANG_90[1]);
								Thread.sleep(420);
								mOutputStream.write(Constant.HUAYANG_90[2]);							
								Thread.sleep(300);
								mOutputStream.write(Constant.HUAYANG_90[3]);
								Thread.sleep(250);
								mOutputStream.write(Constant.HUAYANG_90[4]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_90[5]);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI[0]);
								//Thread.sleep(3000);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI[1]);
								//Thread.sleep(3000);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI[2]);
								//Thread.sleep(3000);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI[3]);
								///mOutputStream.flush();
								//outStream.flush();
							}

						}
						if (retArr[2] == 5 && retArr[3] == 5) {
							retArrtmpe = retArr;
							templenght=0;
							OutputStream mOutputStream = mSerialPort
									.getOutputStream();// 写入
							if (mOutputStream != null) {
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[0]);
								Thread.sleep(1100);
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[1]);
								Thread.sleep(600);
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[2]);
								//Thread.sleep(300);
								//mOutputStream.write(Constant.HUAYANG_YANGZHUANZHANLI[3]);
								//Thread.sleep(2500);
								//mOutputStream.write(Constant.HUAYANG_YANGZHUANZHANLI[4]);
								Thread.sleep(300);
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[3]);
								Thread.sleep(250);
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[4]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_CEZHUANZHANLI[5]);
							}
						}
						if (retArr[2] == 7 && retArr[3] == 7) {
							retArrtmpe = retArr;
							templenght=0;
							OutputStream mOutputStream = mSerialPort
									.getOutputStream();// 写入
							if (mOutputStream != null) {
								mOutputStream.write(Constant.HUAYANG_90_1[0]);
								Thread.sleep(1100);
								mOutputStream.write(Constant.HUAYANG_90_1[1]);
								Thread.sleep(600);
								mOutputStream.write(Constant.HUAYANG_90_1[2]);
								Thread.sleep(300);
								mOutputStream.write(Constant.HUAYANG_90_1[3]);
								Thread.sleep(250);
								mOutputStream.write(Constant.HUAYANG_90_1[4]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_90_1[5]);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI2[0]);
								//Thread.sleep(3000);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI2[1]);
								//Thread.sleep(500);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI2[2]);
								//Thread.sleep(1000);
								//mOutputStream.write(Constant.HUAYANG_ZHANLI2[3]);
								//mOutputStream.flush();
								//outStream.flush();
							}

						}
						if (retArr[2] == 6 && retArr[3] == 6) {
							retArrtmpe = retArr;
							templenght=0;
							OutputStream mOutputStream = mSerialPort
									.getOutputStream();// 写入
							if (mOutputStream != null) {
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[0]);
								Thread.sleep(1100);
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[1]);
								Thread.sleep(600);
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[2]);
								//Thread.sleep(300);
								//mOutputStream.write(Constant.HUAYANG_YANGZHUANZHANLI[3]);
								//Thread.sleep(2500);
								//mOutputStream.write(Constant.HUAYANG_YANGZHUANZHANLI[4]);
								Thread.sleep(300);
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[3]);
								Thread.sleep(250);
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[4]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_YANGDAOZHUANZHANLI[5]);
							}
						}*/
//						if (retArr[2] == 7 && retArr[3] == 7) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.tiao0);
//								Thread.sleep(2000);
//								mOutputStream.write(Constant.tiao1);
//								Thread.sleep(300);
//								mOutputStream.write(Constant.tiao2);
//								Thread.sleep(200);
//								mOutputStream.write(Constant.tiao1_1);
//							}
//						}
//						if (retArr[2] == 11 && retArr[3] == 11) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b1);
//								}
//						}
//						if (retArr[2] == 12 && retArr[3] == 12) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b2);
//								}
//						}
//						if (retArr[2] == 13 && retArr[3] == 13) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b3);
//								}
//						}
//						if (retArr[2] == 14 && retArr[3] == 14) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b4);
//								}
//						}
//						if (retArr[2] == 15 && retArr[3] == 15) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b5);
//								}
//						}
//						if (retArr[2] == 16 && retArr[3] == 16) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b6);
//								}
//						}
//						if (retArr[2] == 17 && retArr[3] == 17) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b7);
//								}
//						}
//						if (retArr[2] == 18 && retArr[3] == 18) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b8);
//								}
//						}
//						if (retArr[2] == 19 && retArr[3] == 19) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b9);
//								}
//						}
//						if (retArr[2] == 20 && retArr[3] == 20) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b10);
//								}
//						}
//						if (retArr[2] == 21 && retArr[3] == 21) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b11);
//								}
//						}
//						if (retArr[2] == 22 && retArr[3] == 22) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b12);
//								}
//						}
//						if (retArr[2] == 23 && retArr[3] == 23) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b13);
//								}
//						}
//						if (retArr[2] == 24 && retArr[3] == 24) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b14);
//								}
//						}
//						if (retArr[2] == 25 && retArr[3] == 25) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b15);
//								}
//						}
//						if (retArr[2] == 26 && retArr[3] == 26) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b16);
//								}
//						}
//						if (retArr[2] == 27 && retArr[3] == 27) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b17);
//								}
//						}
//						if (retArr[2] == 28 && retArr[3] == 28) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b18);
//								}
//						}
						/*if (retArr[2] == 29 && retArr[3] == 29) {
							retArrtmpe = retArr;
							templenght=0;
							OutputStream mOutputStream = mSerialPort
									.getOutputStream();// 写入
							if (mOutputStream != null) {
								mOutputStream.write(Constant.b19);
								}
						}*/

//						if (retArr[2] == 30 && retArr[3] == 30) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b1);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b2);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b3);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b4);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b5);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b6);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b7);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b8);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b9);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b11);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b12);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b13);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b14);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b15);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b16);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b17);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.b18);
//								Thread.sleep(500);
//								}
//						}
//						if (retArr[2] == 31 && retArr[3] == 31) {
//							retArrtmpe = retArr;
//							templenght=0;
//							OutputStream mOutputStream = mSerialPort
//									.getOutputStream();// 写入
//							if (mOutputStream != null) {
//								mOutputStream.write(Constant.b1_1);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_2);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_3);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_4);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_5);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_6);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_7);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_8);
//								Thread.sleep(1000);
//								
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_9);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_10);
//								Thread.sleep(1000);
//								
//								mOutputStream.write(Constant.b1_16);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_17);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_18);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_19);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_20);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_21);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_22);
//								Thread.sleep(1000);
//								mOutputStream.write(Constant.b1_23);
//								Thread.sleep(1000);
//								}
//						}


                        if (retArr[2] == 2 && retArr[3] == 2) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[0]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[1]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[2]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[3]);
                                Thread.sleep(370);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[4]);
                                Thread.sleep(370);
                            }
                        }
                        if (retArr[2] == 3 && retArr[3] == 3) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[0]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[1]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[2]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[3]);
                                Thread.sleep(370);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[4]);
                                Thread.sleep(370);
                            }
                        }
                        if (retArr[2] == 4 && retArr[3] == 4) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.tiao0);
                                Thread.sleep(2000);
                                mOutputStream.write(Constant.tiao1);
                                Thread.sleep(460);
                                //	mOutputStream.write(Constant.tiao5);
                                mOutputStream.write(Constant.tiao2);
                                Thread.sleep(200);
                                mOutputStream.write(Constant.tiao1_1);
//									Thread.sleep(100);
//									mOutputStream.write(Constant.tiao3);
//									Thread.sleep(100);
//									mOutputStream.write(Constant.tiao4);
                            }
                        }

                        /////////////////////////////////////////////////////////////////////////////////////////////////
                        if (retArr[2] == 20 && retArr[3] == 20) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.UNLOAD1);
//									Thread.sleep(500);
//									mOutputStream.write(Constant.UNLOAD2);
                            }
                        }
                        if (retArr[2] == 51 && retArr[3] == 51) {
                            Intent intent = new Intent();
                            intent.setAction("AP");
                            sendBroadcast(intent);
                        }
                        if (retArr[2] == 52 && retArr[3] == 52) {

                            Intent intent = new Intent();
                            intent.setAction("NAP");
                            sendBroadcast(intent);
                        }
                        if (retArr[2] == 21 && retArr[3] == 21) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.tiao0);
                                Thread.sleep(2000);
                                mOutputStream.write(Constant.tiao1);
                                Thread.sleep(350);
                                //	mOutputStream.write(Constant.tiao5);
                                mOutputStream.write(Constant.tiao2);
                                Thread.sleep(100);
                                mOutputStream.write(Constant.tiao1_1);
                                Thread.sleep(1000);
                                mOutputStream.write(Constant.action5);
                                Thread.sleep(2000);
                                mOutputStream.write(Constant.action6);
//									mOutputStream.write(Constant.tiao3);
//									Thread.sleep(100);
//									mOutputStream.write(Constant.tiao4);
                            }
                        }
                        /////////////////////////////////////////////////////////////////////////////////////////////////////
                        //打滚
                        if (retArr[2] == 22 && retArr[3] == 22) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.dg0);
                                Thread.sleep(1000);
                                mOutputStream.write(Constant.dg1);
                                Thread.sleep(550);
                                mOutputStream.write(Constant.dg2);
                                Thread.sleep(300);
                                mOutputStream.write(Constant.dg3);
                            }
                        }
                        if (retArr[2] == 5 && retArr[3] == 5) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.houtuitiao1);
                                Thread.sleep(500);
//									mOutputStream.write(Constant.houtuitiao2);
//									Thread.sleep(500);
//									mOutputStream.write(Constant.houtuitiao3);
//									Thread.sleep(500);
//									mOutputStream.write(Constant.houtuitiao4);
//									Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao5);
//									Thread.sleep(2000);
//									mOutputStream.write(Constant.houtuitiao6);
//									Thread.sleep(500);
//									mOutputStream.write(Constant.houtuitiao7);
//									Thread.sleep(500);
//									mOutputStream.write(Constant.houtuitiao8);
//									mOutputStream.write(Constant.tiao2);
//									Thread.sleep(200);
//									mOutputStream.write(Constant.tiao1_1);
//									Thread.sleep(100);
//									mOutputStream.write(Constant.tiao3);
                                //Thread.sleep(500);
                                //mOutputStream.write(Constant.houtuitiao4);
                            }
                        }

                        if (SoundCommond == 1) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.FORWARD);
                            }
                        }
                        if (retArr[2] == 10 && retArr[3] == 10) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
								/*mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[0]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[1]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[2]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[3]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[4]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[5]);
								Thread.sleep(1000);
								mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[6]);*/
                            }
                        }
                    }
                    if ((no) == 7 && retArr[0] == -113
                            && retArr[1] == 16
                            && retArr[(no) - 1] == -1) {
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort.getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }


                    // 头部
                    if ((no) > 12) {
                        retArrtmpe_jbzd = null;
                        if ((no) == 13 && retArr[0] == -113
                                && retArr[1] == 21 && retArr[2] == 8
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }
                        }
                        if ((no) == 13 && retArr[0] == -113
                                && retArr[1] == 22 && retArr[2] == 8
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();

                            }
                        }
                    }

                    // [-113, 37, 21, 1, 1, 0, 68, 63, 65, 0, 0, 0, 0, 0, 0, 0,
                    // 0, 0, 0, 0, 0, 0, 0, 0, 0, -1]
                    if ((no) > 26) {
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if ((no) == 27 && retArr[0] == -113
                                && retArr[1] == 37 && retArr[2] == 22
                                && retArr[(no) - 1] == -1) {
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }
                        }
                    }

                    if ((no) == 61 && retArr[0] == -113
                            && retArr[1] == 48 && retArr[2] == 56
                            && retArr[(no) - 1] == -1) {//舵机零位调整指令
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort.getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }
                    // 判断第一位为0x80 第二位为ox20 最后一位为0xff
                    if ((no) > 232) {
                        retArrtmpe_jbzd = null;
                        if ((no) > 232 && retArr[0] == -113
                                && (retArr[1] == 32)
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }

                        }

                    }

//				}
                    //retArr.c
                    //int a = retArr.length;
                    int a_thy = i_thy;
                    outStream.close();/////////////
                    //socket.close();///////////////////
//				inStream.close();
                }////////////////
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("hahahaha", e.toString());
            }
//			finally {
//				if (socket != null) {
//					if (!socket.isClosed()) {
//						try {
//							socket.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//
//			}
        }

    }

    public void closeWifiHotspot() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setmyWifiEnabled(boolean enabled, byte[] a, byte[] b) {
        ConnectivityManager connectionManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        String name = new String(a);
        String key = new String(b);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        apConfig = createWifiInfo(name, key, 2);
//			while(!networkInfo.isConnected()){

        try {
            Thread.sleep(6100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int wcgID = wifiManager.addNetwork(apConfig);
        wifiManager.enableNetwork(wcgID, true);
//			}
    }

    public boolean setWifiApEnabled(boolean enabled, byte[] a, byte[] b) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
            Editor editor = sharedPreferences.edit();//获取编辑器

            String name = new String(a);
            String key = new String(b);
            editor.putString("APNAME", name);
            editor.putString("APKEY", key);
            editor.commit();//提交修改
            apConfig.SSID = name;
            apConfig.preSharedKey = key;
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {
            return false;
        }
    }


    public class ClientOutputThread extends Thread {


        private DataOutputStream out;
        private boolean isStart = true;
        private String msg;

        public ClientOutputThread(Socket socket) {
            //this.socket = socket;
            try {
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                while (isStart) {
                    if (msg != null) {
                        out.writeBytes(msg);
                        out.flush();
                        synchronized (this) {
                            wait();
                        }
                    }
                }
                out.close();
                if (socketclient != null)
                    socketclient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public Socket getSocket() {
            return socketclient;
        }


        public void setSocket(Socket socket) {
            //this.socket = socket;
        }


        public DataOutputStream getOut() {
            return out;
        }


        public void setOut(DataOutputStream out) {
            this.out = out;
        }


        public boolean isStart() {
            return isStart;
        }


        public void setStart(boolean isStart) {
            this.isStart = isStart;
        }


        public String getMsg() {
            return msg;
        }


        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public class ClientInputThread extends Thread {
        private DataInputStream in;
        private boolean isStart = true;
        private String msg = "";
        //   	private MessageListener messageListener;
        Toast toast;

        public ClientInputThread(Socket socket) {

            try {
                in = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            try {
                while (true) {
                    BufferedInputStream inStream = new BufferedInputStream(
                            socketclient.getInputStream());
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    String sound = "";
                    int c = -1;// 读取bis流中的下一个字节
                    int no = 0;
                    byte[] retArr = new byte[255];

                    while (true) {
//					outStream.write(c);
//					byte[] retArr = new byte[255];
//					retArr=	outStream.toByteArray();
                        c = inStream.read(retArr, no, 1);
                        no++;
                        if (retArr[no - 1] == -1) {
                            break;
                        }
                    }
                    //接收音乐
                    if ((no) == 4 && retArr[0] == -97
                            && retArr[(no) - 1] == -1) {
                        Intent intent = new Intent();
                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION2);
                        intent.putExtra("retArr", retArr);
                        context.sendBroadcast(intent);

                    }


                    if (retArr[0] == 115 && retArr[1] == 105) {
                        int i = 0;
                        while (retArr[i] != 33) {
                            i++;
                        }
                        byte[] temp = new byte[i];
                        for (int n = 0; n < i; n++)
                            temp[n] = retArr[n];
                        sound = new String(temp);
                        Intent intent = new Intent();
                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
                        intent.putExtra("custom", sound);
                        context.sendBroadcast(intent);
                    }


                    //开启音控
                    if ((no) == 4 && retArr[0] == -97
                            && retArr[(no) - 1] == -1 && retArr[(no) - 2] == 5) {
					/*if (dialog != null && dialog.isShowing())
						dialog.dismiss();
					 
					mIatResults.clear();
					// 设置参数
					setParam();
					boolean isShowDialog = mSharedPreferences.getBoolean(
							getString(R.string.pref_key_iat_show), true);
					if (isShowDialog) {
						// 显示听写对话框
						mIatDialog.setListener(mRecognizerDialogListener);
						mIatDialog.show();
						showTip(getString(R.string.text_begin));
					} else {
						// 不显示听写对话框
						ret = mIat.startListening(mRecognizerListener);
						if (ret != ErrorCode.SUCCESS) {
							showTip("听写失败,错误码：" + ret);
						} else {
							showTip(getString(R.string.text_begin));
						}
					}*/
                    }
                    //读取信息(byte)0x8f,(byte)0x00,(byte)0x01,(byte)0x66,(byte)0x67,(byte)0xff
                    if (((no) == 6) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {
                        if (retArr[3] == 102) {
                            //String res = new String(a);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            bw.write("aaa");
                            bw.flush();

                        }
                    }
                    // 基本动作
                    if (((no) == 6) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {

                        if (retArr[1] == 49) {
                            retArrtmpe_jbzd = null;
                        } else {
                            retArrtmpe_jbzd = retArr;
                        }
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                        //byte[] temp = new byte[6];
//					byte[] temp = {(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x05};
////					for(int i = 0; i< no; i++)
////						temp[i] = retArr[i];
//					sendSTC(temp);

                    }
                    if (((no) == 9) && retArr[0] == -113
                            && retArr[(no) - 1] == -1) {
                        if (retArr[1] == 21 || retArr[1] == 22) {
                            retArrtmpe_jbzd = null;
                        } else {
                            retArrtmpe_jbzd = retArr;
                        }
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }

                    }
                    if ((no) == 10 && retArr[0] == -113 && retArr[1] == 16 && retArr[(no) - 1] == -1) {
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }

                    if ((no) == 61 && retArr[0] == -113 && retArr[1] == 17 && retArr[(no) - 1] == -1) {
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }
                    if ((no) == 5 && retArr[0] == -113
                            && retArr[1] == -35
                            && retArr[(no) - 1] == -1) {
                        retArrtmpe_jbzd = null;
                        // 撒尿
                        // {(byte)0x8f,(byte)0xdd,(byte)0x01,(byte)0x01,(byte)0xff};
                        if (retArr[2] == 1 && retArr[3] == 1) {
                            retArrtmpe = retArr;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_SANIAO[0]);
                                Thread.sleep(3100);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[1]);
                                Thread.sleep(500);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[2]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[3]);
                                Thread.sleep(300);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[4]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_SANIAO[5]);
                                Thread.sleep(1100);
                                //mOutputStream.write(Constant.SANIAO[0]);
                                //mOutputStream.write(Constant.SANIAO[1]);
                                //mOutputStream.write(Constant.SANIAO[2]);
                                //mOutputStream.flush();
                                //outStream.flush();
                            }

                        }


                        if (retArr[2] == 2 && retArr[3] == 2) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[0]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[1]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[2]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[3]);
                                Thread.sleep(370);
                                mOutputStream.write(Constant.HUAYANG_HUANGSHENTI[4]);
                                Thread.sleep(370);
                            }
                        }
                        if (retArr[2] == 3 && retArr[3] == 3) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[0]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[1]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[2]);
                                Thread.sleep(400);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[3]);
                                Thread.sleep(370);
                                mOutputStream.write(Constant.HUAYANG_NIUSHENTI[4]);
                                Thread.sleep(370);
                            }
                        }
                        if (retArr[2] == 4 && retArr[3] == 4) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.tiao0);
                                Thread.sleep(2000);
                                mOutputStream.write(Constant.tiao1);
                                Thread.sleep(460);
                                //	mOutputStream.write(Constant.tiao5);
                                mOutputStream.write(Constant.tiao2);
                                Thread.sleep(200);
                                mOutputStream.write(Constant.tiao1_1);
//								Thread.sleep(100);
//								mOutputStream.write(Constant.tiao3);
//								Thread.sleep(100);
//								mOutputStream.write(Constant.tiao4);
                            }
                        }

                        if (retArr[2] == 5 && retArr[3] == 5) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.houtuitiao1);
                                Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao2);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao3);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao4);
//								Thread.sleep(500);
//							mOutputStream.write(Constant.houtuitiao5);
//								Thread.sleep(2000);
//								mOutputStream.write(Constant.houtuitiao6);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao7);
//								Thread.sleep(500);
//								mOutputStream.write(Constant.houtuitiao8);
//								mOutputStream.write(Constant.tiao2);
//								Thread.sleep(200);
//								mOutputStream.write(Constant.tiao1_1);
//								Thread.sleep(100);
//								mOutputStream.write(Constant.tiao3);
                                //Thread.sleep(500);
                                //mOutputStream.write(Constant.houtuitiao4);
                            }
                        }


                        if (SoundCommond == 1) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(Constant.FORWARD);
                            }
                        }
                        if (retArr[2] == 10 && retArr[3] == 10) {
                            retArrtmpe = retArr;
                            templenght = 0;
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
							/*mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[0]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[1]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[2]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[3]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[4]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[5]);
							Thread.sleep(1000);
							mOutputStream.write(Constant.HUAYANG_HOUTUIZHANLI[6]);*/
                            }
                        }
                    }
                    if ((no) == 7 && retArr[0] == -113
                            && retArr[1] == 16
                            && retArr[(no) - 1] == -1) {
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort.getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }


                    // 头部
                    if ((no) > 12) {
                        retArrtmpe_jbzd = null;
                        if ((no) == 13 && retArr[0] == -113
                                && retArr[1] == 21 && retArr[2] == 8
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }
                        }
                        if ((no) == 13 && retArr[0] == -113
                                && retArr[1] == 22 && retArr[2] == 8
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();

                            }
                        }
                    }

                    // [-113, 37, 21, 1, 1, 0, 68, 63, 65, 0, 0, 0, 0, 0, 0, 0,
                    // 0, 0, 0, 0, 0, 0, 0, 0, 0, -1]
                    if ((no) > 26) {
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort
                                .getOutputStream();// 写入
                        if ((no) == 27 && retArr[0] == -113
                                && retArr[1] == 37 && retArr[2] == 22
                                && retArr[(no) - 1] == -1) {
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }
                        }
                    }

                    if ((no) == 61 && retArr[0] == -113
                            && retArr[1] == 48 && retArr[2] == 56
                            && retArr[(no) - 1] == -1) {//舵机零位调整指令
                        retArrtmpe_jbzd = null;
                        mApplication.closeSerialPort();
                        mSerialPort = mApplication.getSerialPort();
                        OutputStream mOutputStream = mSerialPort.getOutputStream();// 写入
                        if (mOutputStream != null) {
                            mOutputStream.write(retArr);
                            mOutputStream.flush();
                            outStream.flush();
                        }
                    }
                    // 判断第一位为0x80 第二位为ox20 最后一位为0xff
                    if ((no) > 232) {
                        retArrtmpe_jbzd = null;
                        if ((no) > 232 && retArr[0] == -113
                                && (retArr[1] == 32)
                                && retArr[(no) - 1] == -1) {
                            mApplication.closeSerialPort();
                            mSerialPort = mApplication.getSerialPort();
                            OutputStream mOutputStream = mSerialPort
                                    .getOutputStream();// 写入
                            if (mOutputStream != null) {
                                mOutputStream.write(retArr);
                                mOutputStream.flush();
                                outStream.flush();
                            }

                        }

                    }
                }
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("hahahaha", e.toString());
            }


        }

        public Socket getSocket() {
            return socketclient;
        }


        public void setSocket(Socket socket) {
//    		this.socket = socket;
        }


        public DataInputStream getIn() {
            return in;
        }


        public void setIn(DataInputStream in) {
            this.in = in;
        }


        public boolean isStart() {
            return isStart;
        }


        public void setStart(boolean isStart) {
            this.isStart = isStart;
        }
    }

    public void sendcom(byte[] s) {
        //Socket socket = null;
        try {
            //this.socket = socket;
            //Socket socket = new
            OutputStream ou = socketclient.getOutputStream();
            // 向服务器发送信息
            //byte[] s = mesg.getBytes();
            //byte[] s={(byte)0x8f,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0xff};
            ou.write(s);
            ou.flush();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e("haha", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("haha1", e.toString());
        } catch (Exception e) {
            Log.e("haha2", e.toString());
        }
    }

    public class ClientThread extends Thread {

        private ClientInputThread in;
        private ClientOutputThread out;

        public ClientThread() {

        }

        @Override
        public void run() {
            try {
                socketclient = new Socket();
                socketclient.connect(new InetSocketAddress("192.168.43.1", 30012), 5000);
//				socket.connect(new InetSocketAddress("192.168.10.119", 30012), 3000);
                in = new ClientInputThread(socketclient);
//				out = new ClientOutputThread(socket);
            } catch (Exception e) {
                Log.e("ll", e.toString());
            }
//			in.setStart(true);
//			out.setStart(true);
            in.start();
//			out.start();
        }

        public ClientInputThread getIn() {
            return in;
        }

        public ClientOutputThread getOut() {
            return out;
        }
    }


    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    mSerialPort = mApplication.getSerialPort();
                    mInputStream = mSerialPort.getInputStream();// 读取
                    byte[] buffer = new byte[1024];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }


    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    protected void onDataReceived(final byte[] buffer, final int size) {

        byte[] buffer1 = new byte[size];
        //mReception.append(new String(buffer, 0, size));
        //	  	mReception.append(Arrays.toString(buffer)+"size:"+size);
        //mReception.append(toHexString(new String(buffer))+"");
        for (int i = 0; i < size; i++) {
            buffer1[i] = buffer[i];
        }
        //Intent intent = new Intent();
        //intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
        //intent.putExtra("custom",(Bytes2HexString(buffer1)+""));
        //context.sendBroadcast(intent);

    }

    /**
     * 读取串口数据
     */
    class SocketWriterThread implements Runnable {
        private Socket socket;
        private String temp = "";

        public SocketWriterThread(Socket socket) {
            this.socket = socket;
        }

        @SuppressWarnings("unused")
        public void run() {
            try {
                while (true) {
                    // Thread.sleep(2000);
                    // Writer writer = new
                    // OutputStreamWriter(socket.getOutputStream());
                    // Thread.sleep(1000);
                    // writer.write(getVersionCode());
                    //
                    // writer.flush();
                    int ithy = 0;
                    mSerialPort = mApplication.getSerialPort();
                    mInputStream = mSerialPort.getInputStream();// 读取串口数据
                    BufferedInputStream inStream = new BufferedInputStream(
                            mInputStream);
                    ByteArrayOutputStream outStream;
                    outStream = new ByteArrayOutputStream();
                    int sum = 0;
                    int flag = 0;
                    // OutputStream output=socket.getOutputStream();
                    int c = -1;// 读取bis流中的下一个字节

                    while ((c = inStream.read()) != -1) {
                        //ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        outStream.write(c);
                        ithy++;
                        // c=inStream.read();
                        byte[] retArr = outStream.toByteArray();
                        if (retArr[retArr.length - 1] == -114) {
                            flag = 1;
                        }
                        if (flag == 0)
                            outStream.reset();

//						if (retArr.length >= 60) {

//							if(retArr[retArr.length - 1] == -1){
//								mApplication.closeSerialPort();
//								mSerialPort = mApplication.getSerialPort();
//							}
//					}
                        // 判断第一位为0x80 第二位为ox20 最后一位为0xff
//						if (retArr.length >= 3) {

//							if (retArrtmpe_jbzd != null
//									&& retArrtmpe_jbzd.length > 0) {

//								if (retArr[retArr.length - 3] == retArrtmpe_jbzd[1]
//										&& (retArr[retArr.length - 2] == retArrtmpe_jbzd[2])) {
//									if (retArr.length >= 3
//											&& retArr[retArr.length - 1] == -1) {
//										retArrtmpe_jbzd = null;
                        // Intent intent1 = new Intent();
                        // intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
                        // intent1.putExtra("messageTitle",
                        // "基本指令状态");
                        // intent1.putExtra("messageContent","发送基本指令成功");
                        // context.sendBroadcast(intent1);
//										outStream.flush();
//									}
//								} else {

//									if (mOutputStream != null) {
//										mOutputStream.write(retArrtmpe_jbzd);
//										mOutputStream.flush();
                        // Intent intent1 = new Intent();
                        // intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
                        // intent1.putExtra("messageTitle",
                        // "基本指令状态");
                        // intent1.putExtra("messageContent","发送基本指令失败");
                        // context.sendBroadcast(intent1);

//									}
//								}
//							}

                        // byte[]
                        // retArr1={(byte)0xa5,(byte)0x50,(byte)0x50,(byte)0xff};
                        /**
                         * 判断没电情况: 返回没电代码 A5 50 50 FF 处理流程：1 开发版语音播报 2)发给 手机
                         * android 血条控制
                         */
                        if (retArr.length >= 3) {
                            if (retArr[0] == -114 && retArr[1] == 81) {
                                if (retArr.length >= retArr[2] + 5) {
                                    //if(retArr[retArr.length - 1] == -1 && )
                                    for (int n = retArr[2] + 2; n >= 1; n--) {
                                        sum = sum + retArr[n];
                                    }
                                    if (retArr[retArr.length - 2] == sum && retArr[retArr.length - 1] == -1) {
                                        sum = 0;
                                        Intent intent = new Intent();
                                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
                                        intent.putExtra("custom", "simulate_goujiao");
                                        //MediaPlayer mPlayer=null;
                                        //mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.goujiao);
                                        context.sendBroadcast(intent);
                                        outStream.flush();
                                        outStream.reset();
                                        //Thread.sleep(9000);
                                    }
                                    flag = 0;
                                }

                            }

                            if (retArr[0] == -114 && retArr[1] == 80) {
                                if (retArr.length >= retArr[2] + 5) {

                                    //if(retArr[retArr.length - 1] == -1 && )
                                    for (int n = retArr[2] + 2; n >= 1; n--) {
                                        sum = sum + retArr[n];
                                    }
                                    if (retArr[retArr.length - 2] == sum && retArr[retArr.length - 1] == -1) {
                                        sum = 0;
                                        Intent intent = new Intent();
                                        intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
                                        intent.putExtra("custom", "simulate_charge");
                                        //MediaPlayer mPlayer=null;
                                        //mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.goujiao);
                                        context.sendBroadcast(intent);
                                        outStream.flush();
                                        outStream.reset();
                                    }
                                    flag = 0;
                                }

                            }
                        }
                        if (retArr.length >= 3
                                && (retArr[retArr.length - 3] == 80)
                                && (retArr[retArr.length - 2] == 80)
                                && retArr[retArr.length - 1] == -1) {
                            outStream.flush();
                            Long a = mSharedPreferences.getLong("mtjxtime",
                                    0);
                            Long b = System.currentTimeMillis();
                            if (a == 0 || (b - a > 1000 * 5 * 2)) {
                                mSharedPreferences.edit()
                                        .putLong("mtjxtime", b).commit();
                                java.util.Random random = new java.util.Random();// 定义随机类
                                int result = random.nextInt(nopawer.length);
                                Intent intent1 = new Intent();
                                intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
                                intent1.putExtra("messageTitle", "没电了，给人家");
                                intent1.putExtra("messageContent",
                                        nopawer[result]);
                                context.sendBroadcast(intent1);
                                outStream.flush();

                            }

                        }

                        /**
                         * 检查有障碍 狗叫 返回没电代码 A5 51 51 FF 处理流程： 检查有障碍 狗叫
                         */
							/*if (retArr.length >= 4
									&& (retArr[retArr.length - 4] == 81)
									&& (retArr[retArr.length - 3] == 0)
									&& (retArr[retArr.length - 2] == 81)
									&& retArr[retArr.length - 1] == -1) 
							{
								Intent intent = new Intent();
								intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
								intent.putExtra("custom", "simulate_goujiao");
							//	//MediaPlayer mPlayer=null;
							//	//mPlayer = MediaPlayer.create(getApplicationContext(),R.raw.goujiao);
								context.sendBroadcast(intent);
								outStream.flush();
								outStream.reset();
							}*/

//							if (retArr.length >= 4 && retArr[0] == 0xA5
//									&& (retArr[1] == 0x32)
//									&& retArr[retArr.length - 1] == 0xFF) { // 花样动作数组格式定义
                        // 写入动作失败回执
                        // A5
                        // 32
                        // Length
                        // Error
                        // sum
                        // FF

                        // Intent intent1 = new Intent();
                        // intent1.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
                        // intent1.putExtra("messageTitle","花样动作数组格式定义  写入动作失败");
                        // intent1.putExtra("messageContent",
                        // "花样动作数组格式定义  写入动作失败");
                        // context.sendBroadcast(intent1);
//							}

//							if (retArr.length >= 4
//									&& retArr[retArr.length - 4] == 49
//									&& retArr[retArr.length - 1] == -1) { // 花样动作数组格式定义
//																			// 写入动作成功回执
//																			// A5
//																			// 31
//																			// Length
//																			// Error
//																			// sum
//																			// FF
//								outStream.flush();
//								if (retArrtmpe.length == 5
//										&& retArrtmpe[0] == -113
//										&& retArrtmpe[1] == -35
//										&& retArrtmpe[retArrtmpe.length - 1] == -1) {
//									// 撒尿
//									// {(byte)0x8f,(byte)0xdd,(byte)0x01,(byte)0x01,(byte)0xff};
//									if (retArrtmpe[2] == 1
//											&& retArrtmpe[3] == 1) {
//										if (Constant.SANIAO.length >= templenght) {
//											templenght++;
//											OutputStream mOutputStream = mSerialPort
//													.getOutputStream();// 写入
//											if (mOutputStream != null) {
//												mOutputStream
//														.write(Constant.SANIAO[templenght]);
//												mOutputStream.flush();
//												outStream.flush();
//											}
//										}
//									}
//									
//									if (retArrtmpe[2] == 4
//											&& retArrtmpe[3] == 4) {
//										if (Constant.HUAYANG_ZHANLI.length >= templenght) {
//											Thread.sleep(3000);
//											templenght++;
//											mOutputStream.write(Constant.HUAYANG_ZHANLI[templenght]);
//											mOutputStream.flush();
// 											outStream.flush();
//											OutputStream mOutputStream = mSerialPort
//													.getOutputStream();// 写入
//											if (mOutputStream != null) {
//												mOutputStream
//														.write(Constant.HUAYANG_ZHANLI[templenght]);
//												mOutputStream.flush();
//												outStream.flush();
//											}
//										}
//									}

//								}
//							}

                    }
                    outStream = new ByteArrayOutputStream();
                    int aa = ithy;
                    //for(retArr.length)
                }
//				}

            } catch (Exception e) {

                try {
                    mSerialPort = mApplication.getSerialPort();
                    mInputStream = mSerialPort.getInputStream();// 读取
                } catch (InvalidParameterException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            } /*finally {
				if (socket != null) {
					if (!socket.isClosed()) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}*/
        }

    }

    //	private BroadcastReceiver wifireceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                // wifi已成功扫描到可用wifi。
//            	Toast.makeText(getApplicationContext(), "广播调用服务啦", 0).show();
//            	
//            }
//        }};
    public WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

//	        WifiConfiguration tempConfig = this.IsExsits(SSID);  
//	        if (tempConfig != null) {  
//	        	wifiManager.removeNetwork(tempConfig.networkId);  
//	        }  

        // 分为三种情况：1没有密码2用wep加密3用wpa加密
//	        if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS  
//	            config.wepKeys[0] = "";  
//	            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
//	            config.wepTxKeyIndex = 0;  
//	              
//	        } else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP   
//	            config.hiddenSSID = true;  
//	            config.wepKeys[0] = "\"" + password + "\"";  
//	            config.allowedAuthAlgorithms  
//	                    .set(WifiConfiguration.AuthAlgorithm.SHARED);  
//	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
//	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
//	            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
//	            config.allowedGroupCiphers  
//	                    .set(WifiConfiguration.GroupCipher.WEP104);  
//	            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
//	            config.wepTxKeyIndex = 0;  
//	        } else if (type == TYPE_WPA) {   // WIFICIPHER_WPA  
        config.preSharedKey = "\"" + password + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
//	        }   

        return config;
    }

    // 没电
    String[] nopawer = {"人家都没电了，还让人家工作,这是在虐待我嘛！", "没电了，不响了，你帮我充电啦！",
            "怀疑自己是不是有强迫症，明明身体没电了,还不给人家充电",
            "看来是没电了。就象是没了电力的玩具一样，有一招没一招地动着。想想以前，我可是带着电的", "昨晚又没给人家充电,今天看来是动力不足"};
    // 障碍
    String[] barrier = {"前方有不明障碍物,请注意躲避！", "道理不通，请注意规避！", "前方有摄像头限速0米，请注意哦",
            "前方为事故多发路段，请注意哦"};

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        //String actionNO = intent.getStringExtra("soundcontrol");
        //if(actionNO.equals("forward"))
        //{
			/*try{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] retArr = outStream.toByteArray();
			byte[] forwardcom = {(byte)0x8f,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0xff};
			retArrtmpe = retArr;
			OutputStream mOutputStream = mSerialPort
					.getOutputStream();// 写入
			if (mOutputStream != null) {
				mOutputStream.write(forwardcom);
				mOutputStream.flush();
				outStream.flush();
			}}catch (Exception e) {
				e.printStackTrace();
			} */
        //}
        super.onStart(intent, startId);
    }

    //@Override
	/*public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//Bundle bundle = (Bundle)intent.getExtras();
		//intent = getIntent();
		String actionNO = intent.getStringExtra("soundcontrol");
		//actionNO = intent.getStringExtra("soundcontrol");
		if(actionNO.equals("forward"))
		{
			try{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] retArr = outStream.toByteArray();
			byte[] forwardcom = {(byte)0x8f,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0xff};
			retArrtmpe = retArr;
			OutputStream mOutputStream = mSerialPort
					.getOutputStream();// 写入
			if (mOutputStream != null) {
				mOutputStream.write(forwardcom);
				mOutputStream.flush();
				outStream.flush();
			}}catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return super.onStartCommand(intent, flags, startId);
	}*/

}
/*class Reminder{  
    Timer timer;  
    public Reminder(int seconds){  
        timer = new Timer();  
        timer.schedule(new RemindTask(), seconds*1000);  
    }  
    class RemindTask extends TimerTask{  
        public void run(){  
            System.out.println("Time''s up!");  
            timer.cancel(); //Terminate the timer thread  
        }  
    } 
}*/ 
