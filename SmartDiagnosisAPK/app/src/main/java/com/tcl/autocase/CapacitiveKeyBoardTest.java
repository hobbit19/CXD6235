package com.tcl.autocase;

import android.os.Environment;
import android.text.format.Time;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opencsv.CSVWriter;
import com.tcl.autotest.AllMainActivity;
import com.tcl.autotest.ExecuteTest;
import com.tcl.autotest.R;
import com.tcl.autotest.autoRunActivity;
import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.FinishThread;
import com.tcl.autotest.utils.Msg;
import com.tcl.autotest.utils.Test;
import com.tcl.autotest.utils.DownTimeCallBack;
import com.tcl.autotest.utils.TestCountDownTimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by user on 18-4-10.
 */

public class CapacitiveKeyBoardTest extends Test implements DownTimeCallBack {

    private final static String TAG = "CapacitiveKeyBoardTest";
    private TestCountDownTimer testCountDownTimer = null;
    String resultStri;
    private static final String CKB_RAW = "/sys/devices/touch_keypad/full_raw_rt78";
    private static final String ID = "/sys/devices/touch_keypad/product_id";
    private static final String CKB_OPEN = "/sys/devices/touch_keypad/open_rt78";

    private static final int rawdata_num = 36;
    private static final int opendata_num = 36;

    private static ArrayList<String> upperLimit = new ArrayList<String>();
    private static ArrayList<String> lowerLimit = new ArrayList<String>();
    private static ArrayList<String> openLimit = new ArrayList<String>();
    private ArrayList<String> open_errorLimit = new ArrayList<String>();
    private ArrayList<String> errorLimit = new ArrayList<String>();
    private ArrayList<String> rawdataList = new ArrayList<String>();

    public CapacitiveKeyBoardTest(ID id, String name) {
        super(id, name);
    }

    @Override
    public void onFinish() {
        onFail();
    }

    @Override
    public void onTick() {

    }

    @Override
    public void setUp() {
        Test.gettag = TAG;
        Test.state = null;
        Tool.toolLog(TAG + "_start_test");
        testCountDownTimer = new TestCountDownTimer(10*SECOND, SECOND, this);
        testCountDownTimer.start();
    }

    @Override
    public void initView() {
        mLayout = (LinearLayout) View.inflate(mContext, R.layout.base_screen,null);
        text_top_zone = (TextView) mLayout.findViewById(R.id.text_top_zone);
        text_cen_zone = (TextView) mLayout.findViewById(R.id.text_cen_zone);
        text_top_zone.setText(mName);
        mContext.setContentView(mLayout);

        initRawdataLimit();
        initOpenDataLimit();
        String product_id = ReadFileString(ID);
        String rawdata_data = ReadFileString(CKB_RAW);
        String open_data = ReadFileString(CKB_OPEN);
        if (rawdata_data != null && open_data != null && !rawdata_data.contains("ERROR") && rawdata_data.length() > 0) {

            String[] row_data = rawdata_data.split("\n");
            if(row_data.length >= rawdata_num+2) {
                for (int i = 2; i < rawdata_num + 2; i++) {
                  rawdataList.add(row_data[i].trim());
                  }
                }

            startTest(rawdata_data, open_data);
         }
    }
    public String ReadFileString(String path) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File((path)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fileInputStream != null){
            byte[] range = new byte[512];
            int ret = -1;
            try {
                ret = fileInputStream.read(range);
                    if (ret <= 0) {
                        return null;
                    }
             } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            byte[] readBytes = new byte[ret];
            System.arraycopy(range, 0, readBytes, 0, ret);
            if (readBytes != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resultStri = new String(readBytes, Charset.forName("US-ASCII"));
                resultStri = resultStri.trim();
            } else {
                resultStri = "ERROR";

            }
            return resultStri;
        }
        return null;

        }




    @Override
    public void finish() {
        if(testCountDownTimer!=null){
            testCountDownTimer.cancel();
        }
    }

    @Override
    public String getmContextTag() {
        return TAG;
    }

    @Override
    public void setmContextTag() {

    }

    @Override
    public void destroy() {
        finish();
    }
    //	@Override
    public void onSucess() {
        // TODO Auto-generated method stub
//		Tool.toolLog(TAG + " onFinish");
        Test.state = "Pass";
        Tool.toolLog(TAG + " Test.state " + Test.state);
        FinishThread finishThread = new FinishThread(0x01, ExecuteTest.temppositon);
        finishThread.start();
        try {
            finishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
        Msg.exitWithSuccessTest(mContext, TAG, 10,true,"Pass");
        //Write data into file
        if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
            Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "PASS");
        }else{
            Msg.WriteModelResult(mContext, autoRunActivity.auto_file_text, "PASS");
        }
        //End
    }

    public void onFail(){
//		Tool.toolLog(TAG + " onFail");
        //Begin Add By Jianke.Zhang 2015/01/14
        //Write data into file
        Test.state = "Fail";
        mContext.setResult(RESULT.FAILED.ordinal());
        Tool.toolLog(TAG + " index 8882 -> " + ExecuteTest.temppositon);
        int double_test;
        if(AllMainActivity.mainAllTest){
            double_test = AllMainActivity.double_check_for_auto[ExecuteTest.temppositon]++;
            //AllMainActivity.mainAllTest = false;
        }else {
            double_test = autoRunActivity.double_check[ExecuteTest.temppositon]++;
        }
        Tool.toolLog(TAG + " double_test 9992-> " + double_test);

        FinishThread tFinishThread = new FinishThread(0x02,ExecuteTest.temppositon);
        tFinishThread.start();
        try {
            tFinishThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*Jianke
		 * ���Ҳ����Ҫͬ��*/
        if(double_test==1){
            Msg.exitWithException(mContext,TAG,50,true,"Pass");
            if(AllMainActivity.mainAllTest && !AllMainActivity.autofileFlag){
                Msg.WriteModelResult(mContext,AllMainActivity.all_items_file_text, "FAIL");
            }else {
                Msg.WriteModelResult(mContext,autoRunActivity.auto_file_text, "FAIL");
            }
        }else{
            Msg.exitWithException(mContext,TAG,50,true,"Fail");
        }
        //End
    }

    boolean rawdata_dataOk = true;
    boolean opendata_dataOk = true;
    private void startTest(String data, String open_data) {
         try {
            String[] row_data = data.split("\n");


             for (int i = 0; i < rawdataList.size(); i++) {
                 int upper = Integer.valueOf(upperLimit.get(i));
                 int lower = Integer.valueOf(lowerLimit.get(i));
                 int rawdata = Integer.valueOf(rawdataList.get(i));
                 String s = i + ", data: " + rawdata + ", limit: (" + lower + "," + upper + ")";
                 if (rawdata < lower || rawdata > upper) {
                     errorLimit.add(s + " nok!");
                     rawdata_dataOk = false;
                     }
             }







            String[] open_data_array = null;
            if (!open_data.contains("ERROR") && open_data != null && open_data.length() > 0) {
                open_data_array = open_data.split("\n");
                if(open_data_array.length >= rawdata_num+2) {
                    for (int i = 2; i < rawdata_num+2; i++) {
                        int opendata = Integer.valueOf(open_data_array[i].trim());
                        int limit = Integer.valueOf(openLimit.get(i-2));
                        String s = (i-2) + ", data: " + opendata + ", limit: " + limit;
                        if(opendata < limit) {
                            open_errorLimit.add(s);
                            opendata_dataOk = false;
                         }
                     }
                 }
             }else{
                opendata_dataOk = false;
             }
            String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Time time = new Time();
            time.setToNow();
            String csvFileName = null;
            csvFileName = new String("ckb_rawdata_");
            csvFileName = csvFileName.concat(time.format("%Y%m%d_%H%M%S"));
            if (errorLimit.size() > 0) {
                csvFileName = csvFileName.concat("_NG");
            }
            csvFileName = csvFileName.concat(".csv");
            String csvFilePath = sdCardPath.concat("/");
            csvFilePath = csvFilePath.concat(csvFileName);

            CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeNext(new String[] { "===========raw data===========\n" });
            for (int i = 0; i < row_data.length; i++) {
                writer.writeNext(new String[] { row_data[i] });
             }
            int errorSize = errorLimit.size();
            String[] errorData = new String[errorSize];
            if (errorSize != 0) {
               writer.writeNext(new String[] { "\nNG POINT:" });
               for (int k = 0; k < errorSize; k++) {
                    errorData[k] = errorLimit.get(k) + "\n";
               }
               writer.writeNext(errorData);
            }
            writer.writeNext(new String[] { "\n\n===========open test===========" });
            if(open_data_array != null) {
               for (int i = 0; i < open_data_array.length; i++) {
                    writer.writeNext(new String[] { open_data_array[i] });
                }
             }
            int open_errorSize = open_errorLimit.size();
            String[] open_errorData = new String[open_errorSize];
            if (open_errorSize != 0) {
                writer.writeNext(new String[] { "\nNG POINT:" });
                for (int k = 0; k < open_errorSize; k++) {
                    open_errorData[k] = open_errorLimit.get(k) + "\n";
                 }
                writer.writeNext(open_errorData);
                }
            writer.close();
            } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            rawdata_dataOk = false;
            opendata_dataOk = false;
            } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            rawdata_dataOk = false;
            opendata_dataOk = false;
            }

        if(rawdata_dataOk && opendata_dataOk){
            onSucess();
        }else {
            onFail();
        }
    }




    private void initOpenDataLimit() {
        try {
            InputStream instream = mContext.getResources().getAssets().open("ckb_open_limit_athena.ini");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                int index = 0;
                while ((line = buffreader.readLine()) != null) {
                    openLimit.add(index, line);
                    index++;
                    }
            instream.close();
                }
        } catch (IOException e) {
            }

    }

    private boolean initRawdataLimit() {
        ArrayList<String> dataList = new ArrayList<String>();
        try {
            InputStream instream = mContext.getResources().getAssets().open("ckb_rawdata_limit_athena.ini");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                int index = 0;
                while ((line = buffreader.readLine()) != null) {
                    if (line.equals("") || line.startsWith("//")) {
                        continue;
                     }
                    dataList.add(index, line);
                    index++;
                    }
                instream.close();
                }
            } catch (IOException e) {
            }
        for (int i = 0; i < dataList.size() / 2; i++) {
            String[] item = dataList.get(i).split(",");
            for (int p = 0; p < item.length; p++) {
                upperLimit.add(item[p].trim());
             }
         }
        if( upperLimit.size() <rawdata_num ) {
            return false;
         }
        int k = 0;
        for (int j = dataList.size() / 2; j < dataList.size(); j++) {
            String[] item = dataList.get(j).split(",");
            for (int p = 0; p < item.length; p++) {
                lowerLimit.add(item[p].trim());
                }
            k++;
         }
        if( lowerLimit.size() <rawdata_num ) {
            return false;
        }

        return true;
    }

}
