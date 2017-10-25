package com.yobotics.control.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
import com.yobotics.control.util.ActivityManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android_serialport_api.sample.SerialPortActivity;

public class ConversationActivity extends SerialPortActivity implements OnClickListener {
	protected static final String TAG = ConversationActivity.class.getSimpleName();
	private ActivityManager manager = ActivityManager.getActivityManager(this);
	private TextView tvStatus;
	private ProgressDialog dialog;
	 
	// private Button serialButton;
	private Toast mToast;
	private TextView jssjTextView;
	int typeNO = 0;

	// 语音合成对象
	private SpeechSynthesizer mTts;
	private String[] mCloudVoicersEntries;
	private String[] mCloudVoicersValue;
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
	private MyReceiver receiver;///////////////////////////////////////
	// 云端/本地单选按钮
	private RadioGroup mRadioGroup;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_conversation);

		initView();
		//http://bbs.51cto.com/thread-970933-1.html
		manager.putActivity(this);
		 
		initLayout();
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(ConversationActivity.this,mTtsInitListener);
		// 云端发音人名称列表
		mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
		mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME,MODE_PRIVATE);
		// mInstaller = new ApkInstaller(MainActivity.this);
		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(ConversationActivity.this,	mInitListener);
		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		mIatDialog = new RecognizerDialog(ConversationActivity.this, mInitListener);
		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,	Activity.MODE_PRIVATE);
		mInstaller = new ApkInstaller(ConversationActivity.this);
		mAdapter = new ChatMessageAdapter(this, mDatas);
		mChatView.setAdapter(mAdapter);
		Intent intent = getIntent();//获得Intent
		String type = intent.getStringExtra("com.main.typeofspeek");
		if(type.equals("commomd"))
		{
			typeNO = 1;
		}
		
		//语音控制回执广播接收
					receiver=new MyReceiver();///////////////////////////////////////////////////
			        IntentFilter filtercallback=new IntentFilter();
			        filtercallback.addAction("OK");
			        registerReceiver(receiver, filtercallback);
					
	}

	private void initView() {

		tvStatus = (TextView) findViewById(R.id.tvStatus);

		jssjTextView = (TextView) findViewById(R.id.jssjTextView);
		 
		 
		findViewById(R.id.kfxf1).setOnClickListener(this);
		 
		mChatView = (ListView) findViewById(R.id.id_chat_listView);
		mDatas.add(new ChatMessage(Type.INPUT, "您好，好久不见"));
	}

	public void sendMessage(final String msg) {

		if (TextUtils.isEmpty(msg)) {
			startSpeaking("你说的什么，我没听见啊");
			Toast.makeText(this, "您还没有填写信息呢...", Toast.LENGTH_SHORT).show();
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
					from = HttpUtils.sendMsg(msg);
				} catch (Exception e) {
					from = new ChatMessage(Type.INPUT, "sorry,你说的什么，我都没听懂...");
				}
				Message message = Message.obtain();
				message.obj = from;
				mHandler.sendMessage(message);
			};
		}.start();

	}

	/**
	 * 初始化Layout。
	 */
	private void initLayout() {

		findViewById(R.id.speek1).setOnClickListener(this);
		findViewById(R.id.tts_play).setOnClickListener(ConversationActivity.this);
		findViewById(R.id.tts_cancel).setOnClickListener(ConversationActivity.this);
		findViewById(R.id.tts_pause).setOnClickListener(ConversationActivity.this);
		findViewById(R.id.tts_resume).setOnClickListener(ConversationActivity.this);
		findViewById(R.id.image_tts_set).setOnClickListener(ConversationActivity.this);
		findViewById(R.id.tts_btn_person_select).setOnClickListener(ConversationActivity.this);

		mRadioGroup = ((RadioGroup) findViewById(R.id.tts_rediogroup));
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tts_radioCloud:
					mEngineType = SpeechConstant.TYPE_CLOUD;
					break;
				case R.id.tts_radioLocal:
					mEngineType = SpeechConstant.TYPE_LOCAL;
					/**
					 * 选择本地合成 判断是否安装语记,未安装则跳转到提示安装页面
					 */
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();
					}
					break;
				default:
					break;
				}

			}
		});
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.kfxf1:
			startActivity(new Intent(ConversationActivity.this, IatDemo.class));
			break;
		 
		case R.id.image_tts_set:
			if (SpeechConstant.TYPE_CLOUD.equals(mEngineType)) {
				Intent intent = new Intent(ConversationActivity.this, TtsSettings.class);
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
		case R.id.speek1:
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			mIatResults.clear();
			// 设置参数
			setParam1();
			boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
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
			break;
		default:
			break;
		}
	}
	
	//
	public void speek() {
		
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
		mIatResults.clear();
		// 设置参数
		setParam1();
		boolean isShowDialog = mSharedPreferences.getBoolean(getString(R.string.pref_key_iat_show), true);
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
		}else{
		//	showTip("111111 " + code);
		}
	}

	 

	 

	public void showLoading() {
		if (dialog == null) {
			dialog = new ProgressDialog(this);
			dialog.setMessage("Loading");
		}
		dialog.show();
	}

	 

	public Handler handler = new Handler();

	 

	private int selectedNum = 0;

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
//									MainActivity.voicer = mCloudVoicersValue[which];
//									if ("catherine".equals(MainActivity.voicer)
//											|| "henry".equals(MainActivity.voicer)
//											|| "vimary".equals(MainActivity.voicer)) {
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
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
				// setParam();
				// mTts.startSpeaking("科大讯飞，让世界聆听我们的声音", mTtsListener);
				//蹲下
				sendserialDate("14");
				startSpeaking("您好，好久不见,你吃了吗?");
			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ChatMessage from = (ChatMessage) msg.obj;
			mDatas.add(from);
			mAdapter.notifyDataSetChanged();
			mChatView.setSelection(mDatas.size() - 1);

			if (from.getType().equals(Type.INPUT) && typeNO == 0) {
				// int ret =
				// speechSynthesizer.speak(from.getMsg());//inputTextView.getText().toString()
				startSpeaking(from.getMsg());
			}

		};
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			
		//	sendserialDate("12");//伸手
			
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
				//sendserialDate("13");//收回
				showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
			
			if(typeNO == 0)
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
	 * @param param
	 * @return
	 */
	private void setParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, "nannan");
			// 设置合成语速
			mTts.setParameter(SpeechConstant.SPEED,mSharedPreferences.getString("speed_preference", "50"));
			// 设置合成音调
			mTts.setParameter(SpeechConstant.PITCH,mSharedPreferences.getString("pitch_preference", "50"));
			// 设置合成音量
			mTts.setParameter(SpeechConstant.VOLUME,mSharedPreferences.getString("volume_preference", "50"));
		} else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
			/**
			 * TODO 本地合成不设置语速、音调、音量，默认使用语记设置 开发者如需自定义参数，请参考在线合成参数设置
			 */
		}
		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE,mSharedPreferences.getString("stream_preference", "3"));
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}
 
 

	int ret = 0; // 函数调用返回值
	/**
	 * 听写监听器。
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
		 	showTip(error.getPlainDescription(true)+"cssssssssssssssss");
			//speek();
			
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

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

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
		
		if(resultBuffer.toString().contains("前进")||resultBuffer.toString().contains("往前走"))
		{
			Intent intent=new Intent();
	        intent.setAction("forward");
	        sendBroadcast(intent);return;
		}
		if(resultBuffer.toString().contains("停下")||resultBuffer.toString().contains("停止"))
		{
			Intent intent=new Intent();
	        intent.setAction("stop");
	        sendBroadcast(intent);return;
		}
		 
		//if( typeNO == 0){
			sendMessage(resultBuffer.toString());
		
		
		// mResultText.setSelection(mResultText.length());

	}
	
			 
	//服务里面的一个方法//////////////////////////////////////////////////////////////////
	    public void callServiceMethod(){
	        Toast.makeText(getApplicationContext(), "广播调用服务啦", 0).show();
	    }
		//内部类实现广播接收者
	    private class MyReceiver extends BroadcastReceiver {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            callServiceMethod();
	            if (intent.getAction().equals("OK")) {
					//SoundCommond=1;//前进
					speek();
				}
	        }
	    } 
	 
	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setParam1() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference","mandarin");
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
		mIat.setParameter(SpeechConstant.VAD_BOS,mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS,	mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		// mIat.setParameter(SpeechConstant.ASR_PTT,
		// mSharedPreferences.getString("iat_punc_preference", "1"));
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
	}

	/**
	 * 初始化监听器。
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
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			//showTip("22222222222222222222222222222222222222222222222");
			if(!isLast)
				{printResult(results);}
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			speek();
			//showTip(error.getPlainDescription(true));
		}
		
		

	};
	 

	public void showTip(String str) {
		if (mToast == null)
			mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mToast.setText(str);
		mToast.show();
	}
	
	
	
	@Override
	protected void onDestroy() {
		mTts.stopSpeaking();
		sendserialDate("11");//暂停
		// 退出时释放连接
		mTts.destroy();
		mIat.cancel();
		mIat.destroy();
		super.onDestroy();
		manager.removeActivity(this);
	}
	@Override
	public void onResume() {
		// 移动数据统计分析
		// FlowerCollector.onResume(MainActivity.this);
		// FlowerCollector.onPageStart(TAG);
		MobclickAgent.onResume(this);
		super.onResume();
	}
	@Override
	public void onPause() {
		// 移动数据统计分析
		// FlowerCollector.onPageEnd(TAG);
		// FlowerCollector.onPause(MainActivity.this);
		MobclickAgent.onPause(this);
		super.onPause();
	}

	 
	 
	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		// TODO Auto-generated method stub
		
	}
	
	 

}
