/*
 * Copyright 2011 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import android.os.Bundle;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;

import java.io.IOException;

public class SendingActivity extends SerialPortActivity {

	SendingThread mSendingThread;
	byte[] mBuffer;
	// byte i;
	// byte message12[];
	protected Handler handler = new Handler();;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent() == null) {
			return;
		}
		mBuffer = new byte[4];
		String s = getIntent().getExtras().getString("info");
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

	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		// ignore incoming data
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
			// }
			handler.post(runnableUi);

		}
	}

	// 构建Runnable对象，在runnable中更新界面
	Runnable runnableUi = new Runnable() {
		@Override
		public void run() {
			// 更新界面
			SendingActivity.this.finish();
		}

	};

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

	/**
	 * /** 0x00:初始化，原地踏步； 0x01:前进步态； 0x02:左移步态； 0x03:自转步态； 0x04:前进、左移、自转融合步态；
	 * 0x05:姿态调整展示模式； 0x06:抗侧向冲击实验模式：站立； 0x07:抗侧向冲击实验模式：前进； 0x08:中高速跑模式；
	 * 0x09:四足同时跳跃模式； 0x0a:对角原地跳模式。
	 */

}
