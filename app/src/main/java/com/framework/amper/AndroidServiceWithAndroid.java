package com.framework.amper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.yobotics.control.demo.MyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AndroidServiceWithAndroid extends Service {
	Context context;
	@Override
	public void onCreate() {
		context = this;
		new Thread(){
			@Override
			public void run(){
				ServerSocket serivce;
				try {
					serivce = new ServerSocket(30001);
					while (true) {
						//等待客户端连接
						Socket socket = serivce.accept();
						new Thread(new AndroidRunable(socket)).start();
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
	
	
	public class AndroidRunable implements Runnable {

		Socket socket = null;

		public AndroidRunable(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			// 向android客户端输出hello worild
			String line = null;
			String line1 = "";
			InputStream input;
			OutputStream output;
			String str = "ok";
			try {
				//向客户端发送信息
				output = socket.getOutputStream();
				input = socket.getInputStream();
				BufferedReader bff = new BufferedReader(new InputStreamReader(input));
				output.write(str.getBytes("UTF-8"));
				output.flush();
				//半关闭socket  
				//socket.shutdownOutput();
				//获取客户端的信息
				while ((line = bff.readLine()) != null) {
					System.out.print(line);
					line1+=line;
				}
				//关闭输入输出流
				output.close();
				bff.close();
				input.close();
				  
				
				//usb 监听。。。。。。。
				if(line1.startsWith("CESHI")){
					
					
					
				}else if(line1.startsWith("scan")){
					
					
				}else{
					Intent intent = new Intent();
					intent.setAction(MyApplication.CALLBACK_RECEIVER_ACTION);
					intent.putExtra("custom",line1);
					context.sendBroadcast(intent);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if (socket != null) {
					if (!socket.isClosed()) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}


		}
	}

}
