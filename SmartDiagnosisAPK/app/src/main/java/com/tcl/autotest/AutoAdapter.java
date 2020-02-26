package com.tcl.autotest;

import java.util.ArrayList;
import java.util.HashMap;

import com.tcl.autotest.tool.Tool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AutoAdapter extends BaseAdapter {

	private static final String TAG = "AutoAdapter";
	private ArrayList<String> autolist;
	private Context context;
	private LayoutInflater inflater = null;
	static AutoViewHolder holder = null;
	public static boolean getview_flag = false;
	public static boolean refresh_flag = false;
	public static ArrayList<Integer> totalListTemp = new ArrayList<Integer>();
	
	
	//0 ��ʾ �Զ��б�ֵ
	static String strAutolMax = AllMainActivity.getMaxList(0);
		
	public AutoAdapter(ArrayList<String> list, Context context) {
		this.context = context;
		this.autolist = list;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return autolist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return autolist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//Tool.toolLog(TAG + " getView position " + position);
//		Tool.toolLog(TAG + " ====== " + autoRunActivity.index_resume_global);
		
		if(position==0){
			totalListTemp = autoRunActivity.totalList;
		}
		//(position+1)==autoRunActivity.index_resume_global || autoRunActivity.index_resume_global==0
		if(true){
			holder = new AutoViewHolder();
			convertView = inflater.inflate(R.layout.auto_item, null);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.item_titletv);
			holder.tvResult =  (TextView) convertView.findViewById(R.id.item_resulttv);
			// Ϊview���ñ�ǩ 
			convertView.setTag(holder);

			// ����list��TextView����ʾ
			holder.tvTitle.setText(autolist.get(position));

			//Show result separated
			int index = 0;
//			Tool.toolLog(TAG + " autoRunActivity.totalList " + autoRunActivity.totalList);
//			Tool.toolLog(TAG + " autoRunActivity.boolList " + autoRunActivity.boolList);
			
			//Tool.toolLog(TAG + " totalListTemp 11111 " + totalListTemp);
			Tool.toolLog("size2:"+autoRunActivity.boolList.size()+" temp:"+totalListTemp.size());
			for(Integer tList : totalListTemp/*autoRunActivity.totalList*/){
				if(index == autoRunActivity.boolList.size()){
					break;
				}
				refresh_flag = autoRunActivity.boolList.get(index);
				index++;
				if((tList == position) && refresh_flag && (position+1)==index){
					holder.tvResult.setText("PASS");
					holder.tvResult.setBackgroundColor(Color.GREEN);
				}else if((tList == position) && !refresh_flag && (position+1)==index){
					holder.tvResult.setText("FAIL");
					holder.tvResult.setBackgroundColor(Color.RED);
				}
			}
			
		}
//		holder.tvResult.setText("No test");
		//you must set a value if test list count larger than screen
		/* Jianke.Zhang 2015/10/09
		 * �Է����滹û��ˢ������֮��ͽ�����һ��case������crash
		 * auto
		 * */
		int maxautolistValue = Integer.parseInt(strAutolMax);
//		Tool.toolLog(TAG + " maxautolistValue " + maxautolistValue);
		if(maxautolistValue == (position + 1)){
//			Tool.toolLog(TAG + " auto list position " + position);
			getview_flag = true;
			autoRunActivity.refresh_bool = false;
			autoRunActivity.auto_refresh_fail_idle = true;
		}
		else if(autolist.size() == (position + 1)){
//			Tool.toolLog(TAG + " position " + position);
			getview_flag = true;
			autoRunActivity.refresh_bool = false;
			autoRunActivity.auto_refresh_fail_idle = true;
		}
		
		return convertView;
	}

	public static AutoViewHolder getViewHolder(/*int positon*/){
		
		return holder;
	}
}
