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

public class AllItemsAdapter extends BaseAdapter {

	private static final String TAG = "AllItemsAdapter";
	private ArrayList<String> allItemslList;
	private Context context;
	private LayoutInflater inflater = null;
	static AutoViewHolder holder = null;
	public static boolean getview_flag = false;
	public static boolean refresh_flag = false;
	public static ArrayList<Integer> totalListTemp = new ArrayList<Integer>();
	private boolean listRefresh = false;
	
	//2 ��ʾ ���� �б�ֵ
	static String strAlllMax = AllMainActivity.getMaxList(2);
		
	public AllItemsAdapter(ArrayList<String> list, Context context) {
		this.context = context;
		this.allItemslList = list;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return allItemslList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return allItemslList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(position==0){
			totalListTemp = AllMainActivity.totalList;
		}
		
		if(true){
			holder = new AutoViewHolder();
			convertView = inflater.inflate(R.layout.auto_item, null);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.item_titletv);
			holder.tvResult =  (TextView) convertView.findViewById(R.id.item_resulttv);
			// Ϊview���ñ�ǩ 
			convertView.setTag(holder);

			// ����list��TextView����ʾ
			holder.tvTitle.setText(allItemslList.get(position));
			
			//Show result separated
			int index = 0;
			//Tool.toolLog(TAG + " AllMainActivity.boolList " + AllMainActivity.boolList);
			
			for(Integer tList : totalListTemp){
				if(index == AllMainActivity.boolList.size()){
					break;
				}
				refresh_flag = AllMainActivity.boolList.get(index);
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
		//you must set a value if the list counts larger than screen
		/* Jianke.Zhang 2015/10/09
		 * �Է����滹û��ˢ������֮��ͽ�����һ��case������crash
		 * all items
		 * */
		int maxalllistValue = Integer.parseInt(strAlllMax);
//		Tool.toolLog(TAG + " maxalllistValue " + maxalllistValue);
		if(maxalllistValue == (position + 1)){
			Tool.toolLog(TAG + " all items position " + position);
			getview_flag = true;
			AllMainActivity.refresh_all_bool = false;
			AllMainActivity.refresh_fail_idle = true;
		}
		else 
		if(allItemslList.size() == (position + 1)){
			Tool.toolLog(TAG + " position " + position);
			getview_flag = true;
			AllMainActivity.refresh_all_bool = false;
			AllMainActivity.refresh_fail_idle = true;
		}
		
		return convertView;
	}

	public static AutoViewHolder getViewHolder(){
		
		return holder;
	}
}
