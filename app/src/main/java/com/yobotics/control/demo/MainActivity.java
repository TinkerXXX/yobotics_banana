package com.yobotics.control.demo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import android_serialport_api.SerialPort;
import android_serialport_api.sample.SerialMainMenu;

import com.framework.amper.AndroidPCServiceForward;
import com.framework.amper.AndroidServiceWithAndroid;
import com.framework.amper.ReConnectService;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.voicedemo.IatDemo;
import com.umeng.analytics.MobclickAgent;
import com.yobotics.control.adapter.ChatMessageAdapter;
import com.yobotics.control.bean.ChatMessage;
import com.yobotics.control.bean.ChatMessage.Type;
import com.yobotics.control.db.SqliteDBConnect;
import com.yobotics.control.util.ActivityManager;
import com.yobotics.control.util.MyUtil;
import com.yobotics.control.util.SuUtil;

public class MainActivity extends Activity implements OnClickListener {
    protected static final String TAG = MainActivity.class.getSimpleName();
    private Toast mToast;
    private TextView jssjTextView;
    private CallbackReceiver callbackReceiver;
    private CallbackReceiver2 callbackReceiver2;
    private AlarmNote qgphbackReceiver;
    private int selectedNum = 0;
    public Socket socket = null;
    private WifiManager wifiManager;
    private ActivityManager manager = ActivityManager.getActivityManager(this);
    private MyReceiver receiver;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private String[] mCloudVoicersEntries;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    // 云端/本地单选按钮
    private RadioGroup mRadioGroup;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 语记安装助手类
    ApkInstaller mInstaller;

    private SharedPreferences mSharedPreferences;

    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    /**
     * 展示消息的listview
     */
    private ListView mChatView;
    /**
     * 存储聊天消息
     */
    private List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
    /**
     * 适配器
     */
    private ChatMessageAdapter mAdapter;


    public int voiceC = 0;
    private SQLiteDatabase sdb;
    private Calendar c;
    private PendingIntent pi;
    private AlarmManager alm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //加载布局
        setContentView(R.layout.activity_main1);

        //消息接受者
        receiver = new MyReceiver();
        IntentFilter filterap = new IntentFilter();
        filterap.addAction("AP");
        filterap.addAction("NAP");
        registerReceiver(receiver, filterap);

        //初始化热点模块
        if (thefirsttime() || APmode()) {
            //WIFI热点自动开启
            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            setWifiApEnabled(true);
        }


        //socket30000发送string类型广播
        callbackReceiver = new CallbackReceiver();//
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyApplication.CALLBACK_RECEIVER_ACTION);
        filter.addAction("AP");
        filter.addAction("NAP");
        registerReceiver(callbackReceiver, filter);


        //播放音乐广播
        callbackReceiver2 = new CallbackReceiver2();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(MyApplication.CALLBACK_RECEIVER_ACTION2);
        registerReceiver(callbackReceiver2, filter2);

//情感陪护广播
        qgphbackReceiver = new AlarmNote();
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
        registerReceiver(qgphbackReceiver, filter1);

        startServiceX();
        initLayout();

////////////////////科大讯飞demo////////////////////////////////////////////////////////
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);

        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);

        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);

        mInstaller = new ApkInstaller(MainActivity.this);

        mAdapter = new ChatMessageAdapter(this, mDatas);
        mChatView.setAdapter(mAdapter);


        SqliteDBConnect sd = new SqliteDBConnect(MainActivity.this);
        sdb = sd.getReadableDatabase();
        if (!SpeechUtility.getUtility().checkServiceInstalled()) {//不存在本地服务
            mEngineType = SpeechConstant.TYPE_CLOUD;
        }

        Intent x = new Intent(MainActivity.this, AndroidPCServiceForward.class);
        startService(x);


    }

    public boolean APmode() {
        try {
            SharedPreferences preferencesformode = getSharedPreferences("userInfo", MODE_PRIVATE);
            return preferencesformode.getBoolean("AP", true);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
            return false;
        }
    }

    //判断是否热点模式
    public void setAPmode(boolean startAPMode) {
        try {
            SharedPreferences preferencesformode = getSharedPreferences("userInfo", MODE_PRIVATE);
            Editor editorformode = preferencesformode.edit();
            if (startAPMode) {
                editorformode.putBoolean("AP", true);
                editorformode.apply();
            } else {
                editorformode.putBoolean("AP", false);
                editorformode.apply();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    public void setnotfirsttime() {
        try {
            SharedPreferences setting = getSharedPreferences("APPINFO", MODE_PRIVATE);
            Editor editor1 = setting.edit();
            editor1.putBoolean("FIRST", false);
            editor1.apply();
        } catch (Exception e) {
            Log.e("thefirsttime", e.toString());
        }
    }

    //判断是否第一次启动
    public boolean thefirsttime() {
        try {
            SharedPreferences setting = getSharedPreferences("APPINFO", MODE_PRIVATE);
            Editor editor1 = setting.edit();
            editor1.apply();
            //第一次
            return setting.getBoolean("FIRST", true);
        } catch (Exception e) {
            Log.e("thefirsttime", e.toString());
            return false;
        }
    }
//	public boolean setWifiApEnabled(boolean enabled){
//		try{
//		WifiConfiguration apConfig = new WifiConfiguration();
////        SharedPreferences setting = getSharedPreferences("APPINFO", MODE_PRIVATE);
//        SharedPreferences preferences = getSharedPreferences("userInfo",
//                MODE_PRIVATE);
////        Editor editor1 = setting.edit();
//        Editor editor = preferences.edit();
////        Boolean user_first = setting.getBoolean("FIRST",true);
////        if(user_first){//第一次
//        if(thefirsttime()){
////        	int NO = (int)(Math.random()*1000);
//////        	editor1.putBoolean("FIRST", false);
//////        	editor1.commit();
////        	//user_first.
////        	String s=String.valueOf(NO);
////        	apConfig.SSID = "newDog"+s;
////    		apConfig.preSharedKey="12345678";
////    		editor.putString("APNAME", "newDog"+s);
////    		editor.putString("APKEY", "12345678");
//    		apConfig.SSID = "YBT-BD2-B5H-001";
//    		apConfig.preSharedKey="12345678";
//    		editor.putString("APNAME", "YBT-BD2-B5H-001");
//    		editor.putString("APKEY", "12345678");
//    		editor.commit();//提交修改
//    		setnotfirsttime();
//         }else{
//
//	        String APNAME = preferences.getString("APNAME", "");
//	        String APKEY = preferences.getString("APKEY", "");
//        	apConfig.SSID = APNAME;
//     		apConfig.preSharedKey=APKEY;
//        }
//		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//		Method method = wifiManager.getClass().getMethod("setWifiApEnabled",WifiConfiguration.class,Boolean.TYPE);
//		return (Boolean)method.invoke(wifiManager, apConfig, enabled);
//
//	}catch (Exception e){
//		return false;
//	}
//	}


    public boolean setWifiApEnabled(boolean enabled) {
        try {
            WifiConfiguration apConfig = new WifiConfiguration();
//    SharedPreferences setting = getSharedPreferences("APPINFO", MODE_PRIVATE);
            SharedPreferences preferences = getSharedPreferences("userInfo",
                    MODE_PRIVATE);
//    Editor editor1 = setting.edit();
            Editor editor = preferences.edit();
//    Boolean user_first = setting.getBoolean("FIRST",true);
//    if(user_first){//第一次
            if (thefirsttime()) {
//    	int NO = (int)(Math.random()*1000);
//////    	editor1.putBoolean("FIRST", false);
//////    	editor1.commit();
////    	//user_first.
//    	String s=String.valueOf(NO);
//    	apConfig.SSID = "newDog"+s;
//		apConfig.preSharedKey="12345678";
//		editor.putString("APNAME", "newDog"+s);
//		editor.putString("APKEY", "12345678");
                apConfig.SSID = "newdog00000";
                apConfig.preSharedKey = "12345678";
                editor.putString("APNAME", "newdog00000");
                editor.putString("APKEY", "12345678");
                editor.apply();//提交修改
                setnotfirsttime();
            } else {

                String APNAME = preferences.getString("APNAME", "");
                String APKEY = preferences.getString("APKEY", "");
                apConfig.SSID = APNAME;
                apConfig.preSharedKey = APKEY;
            }
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 初始化Layout。
     */
    private void initLayout() {
        //设置监听
        findViewById(R.id.kfxf).setOnClickListener(this);
        findViewById(R.id.serialButton).setOnClickListener(this);
        findViewById(R.id.speek).setOnClickListener(this);
        findViewById(R.id.tts_play).setOnClickListener(this);
        findViewById(R.id.tts_cancel).setOnClickListener(this);
        findViewById(R.id.tts_pause).setOnClickListener(this);
        findViewById(R.id.tts_resume).setOnClickListener(this);
        findViewById(R.id.image_tts_set).setOnClickListener(this);
        findViewById(R.id.tts_btn_person_select).setOnClickListener(this);

        //获取控件
        jssjTextView = findViewById(R.id.jssjTextView);
        mChatView = findViewById(R.id.id_chat_listView);
        mRadioGroup = findViewById(R.id.tts_rediogroup);

        //初始化数据
        mDatas.add(new ChatMessage(Type.INPUT, "您好，好久不见"));

        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {//单选框
                    case R.id.tts_radioCloud:
                        mEngineType = SpeechConstant.TYPE_CLOUD;
                        break;
                    case R.id.tts_radioLocal:
                        mEngineType = SpeechConstant.TYPE_LOCAL;
                        /*
                          选择本地合成 判断是否安装语记,未安装则跳转到提示安装页面
                         */
                        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                            mInstaller.install();////////////////////////////////////
                        }
                        break;
                    default:
                        break;
                }

            }
        });
    }


    void startServiceX() {

        if (!MyUtil.CheckServiceExists(MainActivity.this, "com.framework.amper.AndroidServiceWithAndroid"))// Start
        // ServiceX开启socket服务，安卓连安卓///////////////////////////////////////////////////////////////////////////////////////
        {
            Intent x = new Intent(MainActivity.this, AndroidServiceWithAndroid.class);
            startService(x);
        }


//		if (!MyUtil.CheckServiceExists(MainActivity.this,"com.framework.amper.AndroidService"))// Start
//		// ServiceX
//		{
//			Intent x = new Intent(MainActivity.this,AndroidService.class);
//			startService(x);
//		}

//		if (!MyUtil.CheckServiceExists(MainActivity.this,"com.framework.amper.AndroidPCServiceForward"))// Start
//			// ServiceX
//			{
//				Intent x = new Intent(MainActivity.this,AndroidPCServiceForward.class);
//				startService(x);
//			}
        if (!MyUtil.CheckServiceExists(MainActivity.this, "com.framework.amper.ReConnectService"))// Start
        // ServiceX网络断线检测与重连接
        {
            Intent x = new Intent(MainActivity.this, ReConnectService.class);
            startService(x);
        }

    }

    void stopServiceX() {
        if (MyUtil.CheckServiceExists(MainActivity.this, "com.framework.amper.AndroidServiceWithAndroid"))// Start
        {
            Intent x = new Intent(MainActivity.this, AndroidServiceWithAndroid.class);
            stopService(x);
        }
//		if (MyUtil.CheckServiceExists(MainActivity.this,"com.framework.amper.AndroidService"))// Start
//		{
//			Intent x = new Intent(MainActivity.this,AndroidService.class);
//			stopService(x);
//		}

        if (MyUtil.CheckServiceExists(MainActivity.this, "com.framework.amper.AndroidPCServiceForward"))// Start
        {
            Intent x = new Intent(MainActivity.this, AndroidPCServiceForward.class);
            stopService(x);
        }
        if (MyUtil.CheckServiceExists(MainActivity.this, "com.framework.amper.ReConnectService"))// Start
        {
            Intent x = new Intent(MainActivity.this, ReConnectService.class);
            stopService(x);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.kfxf:
                startActivity(new Intent(MainActivity.this, IatDemo.class));
                break;
            case R.id.serialButton:
                startActivity(new Intent(MainActivity.this, SerialMainMenu.class));
                break;
            case R.id.image_tts_set:
                if (SpeechConstant.TYPE_CLOUD.equals(mEngineType)) {
                    Intent intent = new Intent(MainActivity.this, TtsSettings.class);
                    startActivity(intent);
                } else {
                    // 本地设置跳转到语记中
                    if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                        mInstaller.install();
                    } else {
                        SpeechUtility.getUtility().openEngineSettings(null);
                    }
                }
                break;
            // 开始合成
            // 收到onCompleted 回调时，合成结束、生成合成音频
            // 合成的音频格式：只支持pcm格式
            case R.id.tts_play:
                String text = "山东优宝特智能机器人有限公司作为中国最大的智能语音技术提供商，在智能语音技术领域有着长期的研究积累，并在中文语音合成、语音识别、口语"
                        + "评测等多项技术上拥有国际领先的成果。山东优宝特智能机器人有限公司是我国唯一以语音技术为产业化方向的“国家863计划成果产业化基地”…";
                // 设置参数
                setParam();
                int code = mTts.startSpeaking(text, mTtsListener);
                // /**
                // * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
                // * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
                // */
                // String path =
                // Environment.getExternalStorageDirectory()+"/tts.pcm";
                // int code = mTts.synthesizeToUri(text, path, mTtsListener);

                if (code != ErrorCode.SUCCESS) {
                    if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                        // 未安装则跳转到提示安装页面
                        mInstaller.install();
                    } else {
                        showTip("语音合成失败,错误码: " + code);
                    }
                }
                break;
            // 取消合成
            case R.id.tts_cancel:
                mTts.stopSpeaking();
                break;
            // 暂停播放
            case R.id.tts_pause:
                mTts.pauseSpeaking();
                break;
            // 继续播放
            case R.id.tts_resume:
                mTts.resumeSpeaking();
                break;
            // 选择发音人
            case R.id.tts_btn_person_select:
                showPresonSelectDialog();
                break;
            case R.id.speek:
                speek();
                voiceC = 1;
                break;
            default:
                break;
        }
    }

    public void startSpeaking(String text) {
        // 设置参数
        setParam();
        int code = mTts.startSpeaking(text, mTtsListener);
        // /**
        // * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
        // * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
        // */
        // String path =
        // Environment.getExternalStorageDirectory()+"/tts.pcm";
        // int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                // 未安装则跳转到提示安装页面
                mInstaller.install();
            } else {
                showTip("语音合成失败,错误码: " + code);
            }
        }
    }

    public void speek() {
        mIatResults.clear();
        // 设置参数
        setParam1();
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
        }
    }


    class CallbackReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(MyApplication.CALLBACK_RECEIVER_ACTION2)) {
                byte[] b = intent.getByteArrayExtra("retArr");
                if (b[1] == 0 && b[2] == 0) {
                    play(R.raw.apple);
                }
                if (b[1] == 0x01 && b[2] == 0x01) {
                    play(R.raw.baba);
                }
                if (b[1] == 0x02 && b[2] == 0x02) {
                    play(R.raw.wobushangnidedang);
                }
                if (b[1] == 0x03 && b[2] == 0x03) {
                    play(R.raw.wodehaomama);
                }
                if (b[1] == 0x04 && b[2] == 0x04) {
                    play(R.raw.aiwonijiubbw);
                }

            }

        }
    }


    //内部类实现广播接收者
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MyReceiver", " ------------------- ");
            if (intent.getAction().equals("AP"))
                setAPmode(true);
            if (intent.getAction().equals("NAP"))
                setAPmode(false);
        }


    }

    // string 广播
    class CallbackReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(MyApplication.CALLBACK_RECEIVER_ACTION)) {
                String msg = intent.getStringExtra("custom");
                jssjTextView.append(msg + "\n");
                if (msg == null || "".equals(msg)) {
                    return;
                }
                if (msg.equals("com.outfit7.talkingtom2free")) {//////////////////////
                    openApp("com.outfit7.talkingtom2free");
                    return;
                }
                if (msg.equals("com.outfit7.talkingtom2freeStop")) {
                    SuUtil.kill("com.outfit7.talkingtom2free");
                    return;
                }
                if (msg.equals("com.ichano.athome.avs")) {
                    openApp("com.ichano.athome.avs");
                    return;
                }
                if (msg.equals("com.ichano.athome.avsstop")) {
                    SuUtil.kill("com.ichano.athome.avs");
                    return;
                }

                //////////////////////////////////////////////////
                if (msg.startsWith("VioceActivity##")) {
                    String[] a = msg.split("##");
                    if (a.length > 0 && a[1] != null) {

                        play(R.raw.ap);

                        startSpeaking(a[1]);
                    }

//					if(msg.equals("activity_cloud_nannan")){
//						voicer=	"nannan";
//					}
//					if(msg.equals("ctivity_cloud_xiaoxian")){
//						voicer=	"xiaoxin";
//						mSharedPreferences.edit().putString("xiaoxin", voicer);
//					}

                    return;
                }

                if (msg.startsWith("AlarmNote##")) {
                    String[] a = msg.split("##");
                    if (a.length > 0 && a[1] != null) {

                        JSONTokener jsonParser = new JSONTokener(a[1]);
                        // 此时还未读取任何json文本，直接读取就是一个JSONObject对象。
                        // 如果此时的读取位置在"name" : 了，那么nextValue就是"yuanzhifei89"（String）
                        JSONObject person;
                        try {
                            person = (JSONObject) jsonParser.nextValue();
                            String name = person.getString("name");
                            String content = person.getString("content");
                            String time = person.getString("time");
                            String noteId = person.getString("noteId");
                            baocunNote(name, content, noteId, time);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        // 接下来的就是JSON对象的操作了


                        return;
                    }

                    return;
                }

                if (msg.equals("conversation_sjjl")) {//人机交流


//					startSpeaking("没有网络，你不能和我进行交流哦");
//					return;
                    manager.exit();
                    Intent intent1 = new Intent();
                    intent1.putExtra("com.main.typeofspeek", "talk");
                    intent1.setClass(MainActivity.this, ConversationActivity.class);
                    startActivity(intent1);
                    return;
                }
                if (msg.equals("conversation_voicecontrol")) {//语音控制


//					startSpeaking("没有网络，你不能和我进行交流哦");
//					return;
                    manager.exit();
                    voiceC = 1;
                    speek();
                    //Intent intent1 = new Intent();
                    //intent1.putExtra("com.main.typeofspeek", "commomd");
                    //intent1.setClass(MainActivity.this, ConversationActivity.class);
                    //startActivity(intent1);
                    return;
                }
                if (msg.equals("conversation_sjjl_close")) {//关闭人机交流
//					startSpeaking("没有网络，你不能和我进行交流哦");
//					return;
                    manager.exit();
                    return;
                }
                if (msg.equals("conversation_voicecontrol_close")) {//关闭人机交流
//					startSpeaking("没有网络，你不能和我进行交流哦");
//					return;
                    voiceC = 0;
                    manager.exit();
                    return;
                }


                if (msg.startsWith("simulate_")) {//仿生
                    palyRaw(msg);

                } else {

                    startSpeaking("执行的啥命令，人家都不知所措");
                }
            }
        }

    }


    /**
     * 发音人选择。
     */
    private void showPresonSelectDialog() {
        switch (mRadioGroup.getCheckedRadioButtonId()) {
            // 选择在线合成
            case R.id.tts_radioCloud:
                new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                        .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                                selectedNum, // 默认的选项
                                new DialogInterface.OnClickListener() { // 点击单选框后的处理
                                    public void onClick(DialogInterface dialog,
                                                        int which) { // 点击了哪一项
//									voicer = mCloudVoicersValue[which];
//									if ("catherine".equals(voicer)
//											|| "henry".equals(voicer)
//											|| "vimary".equals(voicer)) {
//
//									} else {
//
//									}
                                        selectedNum = which;
                                        dialog.dismiss();
                                    }
                                }).show();
                break;

            // 选择本地合成
            case R.id.tts_radioLocal:
                if (!SpeechUtility.getUtility().checkServiceInstalled()) {
                    mInstaller.install();
                } else {
                    SpeechUtility.getUtility().openEngineSettings(
                            SpeechConstant.ENG_TTS);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);///////////////////
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                // setParam();
                // mTts.startSpeaking("科大讯飞，让世界聆听我们的声音", mTtsListener);
                startSpeaking("欢迎使用优宝特机器人,网络配置成功，本机网络地址为" + getLocAddress());
            }
        }
    };


    // 打开第三方应用
    private void openApp(String packageName) {
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //AbToastUtil.showToast(ForwardMainActivity.this, "");
            showTip("本机未安装此程序");
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(
                resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null) {
            String packageName1 = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageName1, className);
            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    MediaPlayer mPlayer = null;

    private void play(int rawsrc) {
        if (mPlayer != null) {
            mPlayer.stop();
        }
        // 播放声音
        mPlayer = MediaPlayer.create(getApplicationContext(), rawsrc);
        try {
            if (mPlayer != null) {
                mPlayer.stop();
            }
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    int ret = 0; // 函数调用返回值
    /**
     * 听写监听器。科大讯飞语音识别demo
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");

        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    /////////////////////语音控制部分，result是语音识别结果
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        ByteArrayOutputStream outStream;
        outStream = new ByteArrayOutputStream();
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        showTip(resultBuffer.toString() + "lllllllllllllllll");


        if (resultBuffer.toString().contains("恐龙") || resultBuffer.toString().contains("发怒")) {
            play(R.raw.konglong);
            return;
        }

        if (resultBuffer.toString().contains("鸡叫") || resultBuffer.toString().contains("公鸡打鸣")) {
            play(R.raw.jijiao);
            return;
        }
        if (resultBuffer.toString().contains("狗叫") || resultBuffer.toString().contains("狗遇到陌生人")) {
            play(R.raw.goujiao);
            return;
        }

        if (resultBuffer.toString().contains("驴叫") || resultBuffer.toString().contains("学驴叫")) {
            play(R.raw.lvjiao);
            return;
        }
        if (resultBuffer.toString().contains("刮风") || resultBuffer.toString().contains("下雨")) {
            play(R.raw.guafaxiayu);
            return;
        }

        if (resultBuffer.toString().contains("放屁")) {
            play(R.raw.fangpi);
            return;
        }
        if (resultBuffer.toString().contains("前进") || resultBuffer.toString().contains("往前走")
                || resultBuffer.toString().contains("前景")
                || resultBuffer.toString().contains("田径")) {
            Intent intent = new Intent();
            intent.setAction("forward");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("后退") || resultBuffer.toString().contains("往后")
                || resultBuffer.toString().contains("后腿")
                || resultBuffer.toString().contains("后")) {
            Intent intent = new Intent();
            intent.setAction("back");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("左转弯") || resultBuffer.toString().contains("左拐")) {
            Intent intent = new Intent();
            intent.setAction("turnleft");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("右转弯") || resultBuffer.toString().contains("右拐")) {
            Intent intent = new Intent();
            intent.setAction("turnright");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("坐") || resultBuffer.toString().contains("坐下")
                || resultBuffer.toString().contains("作者") || resultBuffer.toString().contains("我想")
                || resultBuffer.toString().contains("不")) {
            Intent intent = new Intent();
            intent.setAction("sitdown");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("握手")) {
            Intent intent = new Intent();
            intent.setAction("shakehand");
            sendBroadcast(intent);
            speek();
            return;
        }
        if (resultBuffer.toString().contains("停下") || resultBuffer.toString().contains("停止")
                || resultBuffer.toString().contains("停") || resultBuffer.toString().contains("站起来")) {
            Intent intent = new Intent();
            intent.setAction("stop");
            sendBroadcast(intent);
            speek();
            return;
        }
        sendMessage(resultBuffer.toString());
        // mResultText.setSelection(mResultText.length());

    }

    //把语音识别结果发图灵
    public void sendMessage(final String msg) {
        if (TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "您还没有填写信息呢...", Toast.LENGTH_SHORT).show();
            speek();
            return;
        }
        ChatMessage to = new ChatMessage(Type.OUTPUT, msg);
        to.setDate(new Date());
        mDatas.add(to);
        mAdapter.notifyDataSetChanged();
        mChatView.setSelection(mDatas.size() - 1);
        new Thread() {
            public void run() {
                ChatMessage from = null;
                try {
                    from = HttpUtils.sendMsg(msg);//发送
                } catch (Exception e) {
                    from = new ChatMessage(Type.INPUT, "您说的什么我没有听懂呢...");
                }
                Message message = Message.obtain();
                message.obj = from;
                mHandler.sendMessage(message);
            }

        }.start();

    }

    private void palyRaw(String msg) {

        if ("simulate_fanue".equals(msg))//恐龙发怒
        {
            play(R.raw.konglong);
            //startSpeaking("电量低，请充电");
            return;
        }
        if ("simulate_gongjidaming".equals(msg))// 公鸡打鸣
        {
            play(R.raw.jijiao);
            return;
        }
        if ("simulate_charge".equals(msg))// gou
        {
            play(R.raw.charge);
            return;
        }

        if ("simulate_goujiao".equals(msg))// gou
        {
            play(R.raw.goujiao);
            return;
        }

        if ("simulate_lvjiao".equals(msg))//驴叫
        {
            play(R.raw.lvjiao);
            return;
        }

        if ("simulate_guangfexiayu".equals(msg))// 公风下雨
        {
            play(R.raw.guafaxiayu);
            return;
        }

        if ("simulate_fangpi".equals(msg))////放屁
        {
            play(R.raw.fangpi);
            return;
        }


        if ("simulate_tingtingge".equals(msg))//听歌
        {
            play(R.raw.apple);
            return;
        }

        if ("simulate_jianggushi".equals(msg))//将故事
        {
            play(R.raw.shouzhudaitu);
            return;
        }
    }

    /**
     * 科大讯飞参数设置
     *
     * @return
     */
    public void setParam1() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS,
                mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,
                mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        // mIat.setParameter(SpeechConstant.ASR_PTT,
        // mSharedPreferences.getString("iat_punc_preference", "1"));
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * 初始化合成引擎监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写UI监听器，初始化
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            if (voiceC == 1) {
                speek();
            } else {
                mIatDialog.dismiss();
            }
            //showTip(error.getPlainDescription(true));
        }

    };
//	/**
//	 * 此处是注销的回调处理 参考集成文档的1.7.10
//	 * http://dev.umeng.com/push/android/integration#1_7_10
//	 */
//	public IUmengUnregisterCallback mUnregisterCallback = new IUmengUnregisterCallback() {
//
//		@Override
//		public void onUnregistered(String registrationId) {
//			// TODO Auto-generated method stub
//			handler.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					updateStatus();
//				}
//			}, 2000);
//		}
//	};

    public void showTip(String str) {
        if (mToast == null)
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mToast.setText(str);
        mToast.show();
    }


    @Override
    protected void onDestroy() {
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        unregisterReceiver(callbackReceiver);
        unregisterReceiver(qgphbackReceiver);
        mIat.cancel();
        mIat.destroy();
        stopServiceX();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // 移动数据统计分析
        // FlowerCollector.onResume(MainActivity.this);
        // FlowerCollector.onPageStart(TAG);
        super.onResume();
        MobclickAgent.onResume(this);


    }

    @Override
    public void onPause() {
        // 移动数据统计分析
        // FlowerCollector.onPageEnd(TAG);
        // FlowerCollector.onPause(MainActivity.this);
        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
        unregisterReceiver(callbackReceiver);
        unregisterReceiver(callbackReceiver2);
        unregisterReceiver(qgphbackReceiver);
        mIat.cancel();
        mIat.destroy();
        stopServiceX();
        super.onPause();
        MobclickAgent.onPause(this);

    }

    //定时
    public void baocunNote(String name, String content, String noteId, String time) {
        if (!sdb.isOpen()) {
            SqliteDBConnect sd = new SqliteDBConnect(MainActivity.this);
            sdb = sd.getReadableDatabase();
        }

        if (noteId != null && !"".equals(noteId)) {
            saveNote(sdb, name, content, noteId, time);
        } else {
            addNote(sdb, name, content, time);
        }
        Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT)
                .show();
        String[] t = time.split(" ");
        String[] t1 = t[0].split("-");
        String[] t2 = t[1].split(":");
        Calendar c2 = Calendar.getInstance();
        c2.set(Integer.parseInt(t1[0]), Integer.parseInt(t1[1]) - 1,
                Integer.parseInt(t1[2]), Integer.parseInt(t2[0]),
                Integer.parseInt(t2[1]));
        c = Calendar.getInstance();
        if (c.getTimeInMillis() + 1000 * 10 <= c2.getTimeInMillis()) {
            String messageContent;
            if (content.length() > 20) {
                messageContent = content.substring(0, 18) + "…";
            } else {
                messageContent = content;
            }
            Intent intent = new Intent();
            intent.setAction(MyApplication.CALLBACK_QINGGANPEIHU_ACTION);
            intent.putExtra("messageTitle", name);
            intent.putExtra("messageContent", messageContent);
            pi = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alm = (AlarmManager) getSystemService(ALARM_SERVICE);
            alm.set(AlarmManager.RTC_WAKEUP, c2.getTimeInMillis(), pi);


        }

    }

    //insert 写入手机内置数据库
    public void addNote(SQLiteDatabase sdb, String name, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put("noteName", name);
        cv.put("noteContent", content);
        cv.put("noteTime", time);
        sdb.insert("note", null, cv);
        sdb.close();
    }

    //save
    public void saveNote(SQLiteDatabase sdb, String name, String content, String noteId, String time) {
        ContentValues cv = new ContentValues();
        cv.put("noteName", name);
        cv.put("noteContent", content);
        cv.put("noteTime", time);
        sdb.update("note", cv, "noteId=?", new String[]{noteId});
        sdb.close();
    }


    class AlarmNote extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String messageTitle = intent.getStringExtra("messageTitle");
            String messageContent = intent.getStringExtra("messageContent");
            Toast.makeText(context, "messageTitle:" + messageTitle + "messageContent:" + messageContent,
                    Toast.LENGTH_SHORT).show();
            play(R.raw.ap);

            startSpeaking(messageContent);

        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            ChatMessage from = (ChatMessage) msg.obj;
            mDatas.add(from);
            mAdapter.notifyDataSetChanged();
            mChatView.setSelection(mDatas.size() - 1);

            if (from.getType().equals(Type.INPUT)) {
                // int ret =
                // speechSynthesizer.speak(from.getMsg());//inputTextView.getText().toString()
                startSpeaking(from.getMsg());
            }

        }

    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
            if (voiceC == 1)
                speek();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE,
                    SpeechConstant.TYPE_CLOUD);
            if (selectedNum == 0)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");
            if (selectedNum == 1)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "vils");
            if (selectedNum == 2)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "yefang");
            if (selectedNum == 3)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaowanzi");
            if (selectedNum == 4)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisbabyxu");
            if (selectedNum == 5)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisjiuxu");
            if (selectedNum == 6)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisjying");
            if (selectedNum == 7)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "nannan");
            if (selectedNum == 8)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisbabyxu");
            if (selectedNum == 9)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisjinger");
            if (selectedNum == 10)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "yefang");
            if (selectedNum == 11)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "disduck");
            if (selectedNum == 12)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisxmeng");
            if (selectedNum == 13)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aismengchun");
            if (selectedNum == 14)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "ziqi");
            if (selectedNum == 15)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisduoxu");
            if (selectedNum == 16)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoxin");
            if (selectedNum == 17)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaowanzi");
            if (selectedNum == 18)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aiscatherine");
            if (selectedNum == 19)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "henry");
            if (selectedNum == 20)
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "allabent");

            // 设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            // 设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            // 设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }


    //获取本地ip地址
    public String getLocAddress() {

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

    public class MyThread60012 extends Thread {
        public byte[] inputString;

        public MyThread60012(byte[] str) {
            inputString = str;
        }

        @Override
        public void run() {
            // 定义消息
            try {

                // 连接服务器 并设置连接超时为5秒
                socket = new Socket();
                socket.connect(
                        new InetSocketAddress(mSharedPreferences.getString(
                                "network_ip", ""), 30012), 5000);
                // 获取输入输出流
                OutputStream ou = socket.getOutputStream();
                InputStream in = socket.getInputStream();///////////////////////////////////////

                BufferedReader br = new BufferedReader(new InputStreamReader(in));

					/*System.out.println("---------------- Socket输出："+br.toString());
                    File file = new File("test.txt");
					 BufferedReader read=null;
					 BufferedWriter writer=null;
					 try {
					   writer=new BufferedWriter(new  FileWriter("test.txt"));
					 } catch (IOException e1) {
					  e1.printStackTrace();
					 }
					 try {
					   read=br;
					   String tempString = null;
					   while((tempString=read.readLine())!=null){
					    writer.append(tempString);
					    writer.newLine();//换行
					    writer.flush();//需要及时清掉流的缓冲区，万一文件过大就有可能无法写入了
					  }read.close();
					   writer.close();
					   System.out.println("文件写入完成...");
					 } catch (IOException e) {
					  e.printStackTrace();
					 }*/

                // 向服务器发送信息
                ou.write(inputString);
                ou.flush();

                //int mes = in.read();/////////////////////////////////////////////
                //Toast toast = Toast.makeText(BaseActivity.this,"aaa",Toast.LENGTH_SHORT);
                //toast.show();
                // 关闭各种输入输出流
                // bff.close();
                String replyInfo = null;
                while (!((replyInfo = br.readLine()) == null)) {
                    System.out.println("接收服务器的数据信息：" + replyInfo);
                }
                //4.关闭资源
                br.close();
                in.close();
                ou.close();/////////////////////////////////////////////
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    socket = new Socket();
                    socket.connect(
                            new InetSocketAddress(mSharedPreferences.getString(
                                    "network_ip", ""), 30012), 5000);
                    // 获取输入输出流
                    OutputStream ou1 = socket.getOutputStream();
                    //
                    ou1.write(inputString);
                    ou1.flush();
                    ou1.close();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            } finally {
                if (null != socket && socket.isConnected()) {
                    try {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            }
        }
    }
}


