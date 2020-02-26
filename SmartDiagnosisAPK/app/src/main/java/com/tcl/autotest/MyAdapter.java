package com.tcl.autotest;


import java.util.ArrayList;
import java.util.HashMap;

import com.tcl.autotest.tool.Tool;
import com.tcl.autotest.utils.ManuFinishThread;
import com.tcl.autotest.utils.XMLDocument;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
	private static final String TAG = "MyAdapter";
	// ������ݵ�list
	private ArrayList<String> list;
	// ��������CheckBox��ѡ��״��
	private static HashMap<Integer, Boolean> isSelected;
	// ������
	private Context context;
	// �������벼��
	private LayoutInflater inflater = null;

	static ViewHolder holder = null;
	
	static int mPosition;
	
	public static boolean refresh_flag = false;
	
	ArrayList<Integer> mList;
	public static ArrayList<Integer> totalManuListTemp = new ArrayList<Integer>();
	
	//1 ��ʾ �ֶ��б�ֵ
	static String strManulMax = AllMainActivity.getMaxList(1);
	
	// ������
	public MyAdapter(ArrayList<String> list, Context context) {
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
		isSelected = new HashMap<Integer, Boolean>();
		// ��ʼ������
		initDate();
	}

	// ��ʼ��isSelected������
	private void initDate() {
		for (int i = 0; i < list.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		Tool.toolLog("MyAdapter getView position " + position);
		//Tool.toolLog(TAG + " index_resume_manu_global " + manuRunActivity.index_resume_manu_global);
		
//		if (convertView == null) {
//			Tool.toolLog("aaaaaaaaaaaaaaaaaa");
			// ���ViewHolder����
			holder = new ViewHolder();
//		} else {
			// ȡ��holder
//			Tool.toolLog("bbbbbbbbbbbbbbbbbbbb");
//			holder = (ViewHolder) convertView.getTag();
//		}

		// ���벼�ֲ���ֵ��convertview
		convertView = inflater.inflate(R.layout.item, null);
		holder.tv = (TextView) convertView.findViewById(R.id.item_tv);
		holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
		holder.re_tv = (TextView) convertView.findViewById(R.id.item_text);
		// Ϊview���ñ�ǩ 
		convertView.setTag(holder);
					
		// ����list��TextView����ʾ
		holder.tv.setText(list.get(position));
		// ����isSelected������checkbox��ѡ��״��
		holder.cb.setChecked(getIsSelected().get(position));
		
		mList = manuRunActivity.getCheckedList();
		if(position == 0){
//			Tool.toolLog(TAG + " 66666666666666666666");
			totalManuListTemp = mList;
		}
//		Tool.toolLog(TAG + " refresh_flag --> " + refresh_flag);
//		Tool.toolLog(TAG + " totalManuListTemp --> " + totalManuListTemp);
//		Tool.toolLog(TAG + " manuRunActivity.abl " + manuRunActivity.abl);
//		ArrayList<Boolean> aList = manuRunActivity.getResultList();
//		Tool.toolLog("aList --> " + aList);
		int index = 0;
		
		if(totalManuListTemp != null){
			for(Integer mInt : totalManuListTemp/*mList*/){
//				Tool.toolLog("mInt--> " + mInt);
//				Tool.toolLog("position--> " + position);
				if(manuRunActivity.abl.size() > 0){
					refresh_flag = manuRunActivity.abl.get(index);
				}
				index++;
//				Tool.toolLog(TAG + " 1111 " + getIsSelected().get(position));
//				Tool.toolLog(TAG + " mInt " + mInt);
				if(mInt == position && refresh_flag && manuRunActivity.Over_test){
					holder.re_tv.setText("PASS");
//					convertView.setBackgroundColor(Color.RED);
					holder.re_tv.setBackgroundColor(Color.GREEN);
				}else if(mInt == position && !refresh_flag && manuRunActivity.Over_test){
					holder.re_tv.setText("FAIL");
					holder.re_tv.setBackgroundColor(Color.RED);
				}
			}
		}else{
			holder.re_tv.setText("No test");
		}
		
		//Maybe different 03/09
		//you must set a value in config file if the list counts larger than the screen
		/* Jianke.Zhang 2015/10/09
		 * �Է����滹û��ˢ������֮��ͽ�����һ��case������crash
		 * manual
		 * */
		int maxmanuallistValue = Integer.parseInt(strManulMax);
//		Tool.toolLog(TAG + " maxmanuallistValue --> " + maxmanuallistValue);
		if(maxmanuallistValue == (position + 1)){
			Tool.toolLog(TAG + " manu list position " + position);
			manuRunActivity.manrefresh_bool = false;
		}else if(list.size() == (position + 1)) {
			Tool.toolLog(TAG + " position list size " + position);
			manuRunActivity.manrefresh_bool = false;
		}
		
		return convertView;
	}

	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		MyAdapter.isSelected = isSelected;
	}

	public int getItemPosition() {
		
		return mPosition;
	}
	
	public static ViewHolder getViewHolder(/*int positon*/){
		
		return holder;
	}

	public static void setItemPosition(int i) {
		// TODO Auto-generated method stub
		mPosition = i;
	}
	
}