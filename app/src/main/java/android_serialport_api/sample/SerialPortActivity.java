package android_serialport_api.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.yobotics.control.demo.MyApplication;
import com.yobotics.control.demo.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {
	protected MyApplication mApplication;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	public ReadThread mReadThread;
	
	SendingThread mSendingThread;
	byte[] mBuffer;
    /**
     * 读取数据
     * @author yongbo002
     *
     */
	public class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) 
						return;
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
	
	
	private class SendingThread extends Thread {
		@Override
		public void run() {
			// while (!isInterrupted()) {
			try {
				if (mOutputStream != null) {

					// byte message[] = { (byte) 0x00 };
					mOutputStream.write(mBuffer);
				} else {
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			// } 	handler.post(runnableUi);

		}
	}
	
	
	public void sendserialDate(String s){
		mBuffer = new byte[4];
	 
		if (s == null || "".equals(s)) {
			return;
		}
		if (s.equals("0")) {// 初始化，原地踏步；
			// byte message[]={(byte)0x8f, (byte)0x00,(byte)0x00 ,(byte)0xff};
			// byte message[] = {(byte) 0x00,(byte)0x01,(byte)0x01 ,(byte)0xff};
			// message12=message;
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x00;
			mBuffer[2] = (byte) 0x00;
			mBuffer[3] = (byte) 0xff;

		} else if (s.equals("1")) {// 前进步态；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x01;
			mBuffer[2] = (byte) 0x01;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("2")) {// 左移步态
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x02;
			mBuffer[2] = (byte) 0x02;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("3")) {// 自转步态；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x03;
			mBuffer[2] = (byte) 0x03;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("4")) {// 前进、左移、自转融合步态；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x04;
			mBuffer[2] = (byte) 0x04;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("5")) {// 姿态调整展示模式；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x05;
			mBuffer[2] = (byte) 0x05;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("6")) {// 抗侧向冲击实验模式：站立；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x06;
			mBuffer[2] = (byte) 0x06;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("7")) {// 抗侧向冲击实验模式：前进；
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x07;
			mBuffer[2] = (byte) 0x07;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("8")) {// 中高速跑模式
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x08;
			mBuffer[2] = (byte) 0x08;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("9")) {// 四足同时跳跃模式
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x09;
			mBuffer[2] = (byte) 0x09;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("10")) {// 对角原地跳模式。
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x0a;
			mBuffer[2] = (byte) 0x0a;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("11")) {// zanting
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x0b;
			mBuffer[2] = (byte) 0x0b;
			mBuffer[3] = (byte) 0xff;
		} else if (s.equals("12")) {// qianshen
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x26;
			mBuffer[2] = (byte) 0x26;
			mBuffer[3] = (byte) 0xff;
		}else if(s.equals("13")) {// shouhui
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x27;
			mBuffer[2] = (byte) 0x27;
			mBuffer[3] = (byte) 0xff;
		}else if(s.equals("14")) {// 蹲下
			mBuffer[0] = (byte) 0x8f;
			mBuffer[1] = (byte) 0x28;
			mBuffer[2] = (byte) 0x28;
			mBuffer[3] = (byte) 0xff;
		}else{
			return;
		}

		/**
		 * 0x00:初始化，原地踏步； 0x01:前进步态； 0x02:左移步态； 0x03:自转步态； 0x04:前进、左移、自转融合步态；
		 * 0x05:姿态调整展示模式； 0x06:抗侧向冲击实验模式：站立； 0x07:抗侧向冲击实验模式：前进； 0x08:中高速跑模式；
		 * 0x09:四足同时跳跃模式； 0x0a:对角原地跳模式。
		 */

		// mBuffer = new byte[1];
		// Arrays.fill(mBuffer, (byte) 0x00);
		if (mSerialPort != null) {
			mSendingThread = new SendingThread();
			mSendingThread.start();
		}
	}

	private void DisplayError(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SerialPortActivity.this.finish();
			}
		});
		b.show();
	}
	
	
	
	private void DisplayError1(int resourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Error");
		b.setMessage(resourceId);
		b.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				 
			}
		});
		b.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("SerialPortActivity", "onCreate");
		mApplication = (MyApplication) getApplication();
		try {
			mSerialPort = mApplication.getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();//写入
			mInputStream = mSerialPort.getInputStream();//读取 

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			DisplayError(R.string.error_security);
		} catch (IOException e) {
			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			DisplayError1(R.string.error_configuration);
		}
	}

	protected abstract void onDataReceived(final byte[] buffer, final int size);

	@Override
	protected void onDestroy() {
		if (mReadThread != null)
			mReadThread.interrupt();
		mApplication.closeSerialPort();
		mSerialPort = null;
		super.onDestroy();
	}
	
	 
		@Override
		public void onResume() {
			super.onResume();
			MobclickAgent.onResume(this);
		}
		@Override
		public void onPause() {
			super.onPause();
			MobclickAgent.onPause(this);
		}
}
