package com.tcl.autotest.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tcl.autotest.tool.Tool;

import android.content.Context;
import android.util.Log;

public class CopyFileFromAssets {
	private static final String TAG = "CopyFileFromAssets";

	
	/**
	 * @param myContext
	 * @param ASSETS_NAME
	 * @return 判断是否有product.xml
	 * */
	
	public static boolean isCommon(Context myContext, String ASSETS_NAME){
		
		InputStream is = null;
		try {
			is = myContext.getResources().getAssets().open(ASSETS_NAME);
			is.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				if(is != null){
					is.close();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param myContext
	 * @param ASSETS_NAME
	 *            要复制的文件名
	 * @param savePath
	 *            要保存的路径
	 * @param saveName
	 *            复制后的文件名 testCopy(Context context)是一个测试例子。
	 */
	
	public static void copy(Context myContext, String ASSETS_NAME,
			String savePath, String saveName) {
		String filename = savePath + "/" + saveName;

		File dir = new File(savePath);
		// 如果目录不中存在，创建这个目录
		if (!dir.exists())
			dir.mkdir();
		try {
			if ((new File(filename)).exists()) {
				(new File(filename)).delete();
			}
			if (!(new File(filename)).exists()) {
				InputStream is;

				// String cpath = myContext.getFilesDir().getAbsolutePath();
				// String path = cpath + "/../assets/";// + ASSETS_NAME;
				// boolean isExist = fileIsExists(cpath);

				try {
					is = myContext.getResources().getAssets().open(ASSETS_NAME);
				} catch (Exception e) {
					is = null;
				}

				if (is == null) {
					is = myContext.getResources().getAssets()
							.open("common.xml");
				} else {
					is = myContext.getResources().getAssets().open(ASSETS_NAME);
				}
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[7168];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Tool.toolLog("AllMainActivity" + "settingSucessed");
	}

	private static byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();

		return imgdata;
	}

	public void testCopy(Context context) {
		String path = context.getFilesDir().getAbsolutePath();
		String name = "test.txt";
		CopyFileFromAssets.copy(context, name, path, name);

	}

	// 判断文件是否存在
	public static boolean fileIsExists(String strFile) {
		// try
		// {
		File f = new File(strFile);
		String[] ll = f.list();
		for (String l : ll) {

		}
		// }
		if (!f.exists()) {

			return false;
		}

		// }
		/*
		 * catch (Exception e) { return false; }
		 */

		return true;
	}

}
