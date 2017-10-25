/*
 * Copyright 2009 Cedric Priscal
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
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.umeng.analytics.MobclickAgent;
import com.yobotics.control.demo.R;

import java.io.IOException;

public class ConsoleActivity extends SerialPortActivity {

	EditText mReception;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serial_console);

//		setTitle("Loopback test");
		mReception = (EditText) findViewById(R.id.EditTextReception);

		EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		Emission.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int i;
				CharSequence t = v.getText();
				char[] text = new char[t.length()];
				for (i=0; i<t.length(); i++) {
					text[i] = t.charAt(i);
				}
				try {
					mOutputStream.write(new String(text).getBytes());
					mOutputStream.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
//				for (int i = 0; i < size; i++) {
//					mReception.append(String.valueOf(buffer[i]));
//				}
				
				byte[] buffer1=new byte[size];
				//mReception.append(new String(buffer, 0, size));
		//	  	
				
				for (int i = 0; i < size; i++) {
					buffer1[i]=buffer[i];
				}
				//mReception.append(Arrays.toString(buffer1));
				mReception.append(bytes2HexString(buffer1));
			}
		});
		
		 	 
		
		 
	}
	
	
	public static String bytes2HexString(byte[] b) {
		  String ret = "";
		  for (int i = 0; i < b.length; i++) {
		   String hex = Integer.toHexString(b[ i ] & 0xFF);
		   if (hex.length() == 1) {
		    hex = '0' + hex;
		   }
		   ret += hex.toUpperCase();
		  }
		  return ret;
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
		 
		protected void onDataReceived1(final byte[] buffer, final int size) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (mReception != null) {
						byte[] buffer1=new byte[size];
						//mReception.append(new String(buffer, 0, size));
				//	  	mReception.append(Arrays.toString(buffer)+"size:"+size);
						//mReception.append(toHexString(new String(buffer))+"");
						for (int i = 0; i < size; i++) {
							buffer1[i]=buffer[i];
						}
						 mReception.append(Bytes2HexString(buffer1)+"");
					}
					
				}
			});
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
	}