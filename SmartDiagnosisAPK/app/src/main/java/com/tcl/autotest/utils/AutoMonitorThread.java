package com.tcl.autotest.utils;

import com.tcl.autocase.BluetoothTest;
import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.manuRunActivity;
import com.tcl.autotest.tool.Tool;

public class AutoMonitorThread extends Thread {
	
	private static final String TAG = "AutoMonitorThread";
	
//	String tagText;
//	String status;
	
	public AutoMonitorThread(){

	}
	
	/*public AutoMonitorThread(String tagText,String status){
		this.tagText = tagText;
		this.status = status;
	}*/
	
	
	public void run(){
		Tool.toolLog(TAG + " start thread!!!");
		while (true) {
            String result = SocketUtils.receData();
             String testState = Test.state;

             String tagText = Test.gettag;
            
            Tool.toolLog(TAG + " result ---- " + result + " testState---- " + testState
            		+ " tagText ---- " + tagText);
            
            //准备开始测试
            if(result != null && result.contains("Begin") && AllMainActivity.socketAllStart){
    			Tool.toolLog(TAG + " Begin all =======================");
            	new SocketUtils().sendUIStr("StartAllItemsActivity_autotest");
            }
            
            if(result != null && result.contains("Begin") && autoRunActivity.socketAutoStart){
            	Tool.toolLog(TAG + " Begin auto =======================");
            	new SocketUtils().sendUIStr("StartAutoActivity_autotest");
            }
            
            if(result != null && result.contains("Begin") && manuRunActivity.socketManuStart){
            	Tool.toolLog(TAG + " Begin manu =======================");
            	new SocketUtils().sendUIStr("StartManuActivity_manutest");
            }
            
            //测试中
            if (result != null && (result.contains("autotest")
            		|| result.contains("manuTest")) ) 
            {
                Tool.toolLog(TAG + " RECEIVE :" + tagText);

        		if(testState != null)
        		{
	        		if(testState.equals("Pass")){
	                	new SocketUtils().sendUIStr(tagText + "_onFinish_SuccessTest");
	                }
	                if(testState.equals("Fail")){
	                	new SocketUtils().sendUIStr(tagText + "_onFinish_Exception");
	                }
        		}
        		else{
        			new SocketUtils().sendUIStr("no key ==============");
        		}
            }
            
            //测试结束
            if(result != null && result.contains("End") && AllMainActivity.socketAllStart){
            	AllMainActivity.socketAllStart = false;
            	new SocketUtils().sendUIStr("AllMainActivity finish all the test");
            }
            
            if(result != null && result.contains("End") && autoRunActivity.socketAutoStart){
            	autoRunActivity.socketAutoStart = false;
            	new SocketUtils().sendUIStr("autoRunActivity finish all the test");
            }
            
            if(result != null && result.contains("End") && manuRunActivity.socketManuStart){
            	manuRunActivity.socketManuStart = false;
            	new SocketUtils().sendUIStr("manuRunActivity finish all the test");
            }
            
		}
	}


}
