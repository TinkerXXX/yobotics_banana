package com.yobotics.control.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.DataOutputStream;
import java.io.IOException;

//http://forum.banana-pi.org.cn/thread-828-1-1.html
public class LEDActivity extends Activity {
	Process process;
	DataOutputStream dos;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_led_main);
  
		 
		
		try {
			process= Runtime.getRuntime().exec("/system/xbin/su");
			dos=new DataOutputStream(process.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			finish();
		}
		Switch swBLUE=(Switch)findViewById(R.id.butBuleLed);
		swBLUE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean ischecked) {
				// TODO Auto-generated method stub
				if(ischecked)
					BuleLED(true);
				else
					BuleLED(false);
			}
		});
		
		Switch swGreen=(Switch)findViewById(R.id.butGreenLed);
		swGreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean ischecked) {
				// TODO Auto-generated method stub
				if(ischecked)
					GreenLED(true);
				else
					GreenLED(false);
			}
		});
	}

	 public boolean GreenLED(boolean tf){
         try {
		    dos=new DataOutputStream(process.getOutputStream());
            if(tf)
            	dos.writeBytes("echo 1 > /sys/class/gpio_sw/PG10/data\n");
	        else
	        	dos.writeBytes("echo 0 > /sys/class/gpio_sw/PG10/data\n");
			            
		        dos.flush();
	            return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					 return false;
				}
			
	 }
	 public boolean BuleLED(boolean tf){
         try {
		    dos=new DataOutputStream(process.getOutputStream());
            if(tf)
            	dos.writeBytes("echo 1 > /sys/class/gpio_sw/PG11/data\n");
	        else
	        	dos.writeBytes("echo 0 > /sys/class/gpio_sw/PG11/data\n");
			            
		        dos.flush();
	            return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					 return false;
				}
			
	 }

}
