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
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.yobotics.control.demo.R;

import java.io.IOException;
import java.util.Arrays;

public class Sending01010101Activity extends SerialPortActivity {

	SendingThread mSendingThread;
	byte[] mBuffer;
	TextView mReception;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_sending01010101);
		mBuffer = new byte[1];
		mReception = (TextView) findViewById(R.id.textView1);
		Arrays.fill(mBuffer, (byte) 0x00);
		if (mSerialPort != null) {
			mSendingThread = new SendingThread();
			mSendingThread.start();
		}
	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		// ignore incoming data
		runOnUiThread(new Runnable() {
			public void run() {
				  
				
				if (mReception != null) {
					for(int i=0;i<size;i++){
						mReception.append(String.valueOf(buffer[i]));
					}
					
//					for (byte element: buffer )
//					{
//						mReception.append(String.valueOf(element));
//					}
					System.out.println("打印返回数据"+mReception.getText().toString());
					
					//mReception.append(new String(buffer, 0, size));
				}
			}
		});
	}

	private class SendingThread extends Thread {
		@Override
		public void run() {
		 	 // while (!isInterrupted()) {
				try {
					if (mOutputStream != null) {			
					 	byte message[] = {(byte) 0x8f,(byte) 0x07,(byte) 0x07,(byte) 0xff};
						mOutputStream.write(message);
					} else {
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
		  //	}
	  	 }
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
