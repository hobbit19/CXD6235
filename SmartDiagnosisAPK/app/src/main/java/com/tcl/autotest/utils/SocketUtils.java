package com.tcl.autotest.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.tcl.autotest.tool.Tool;

import android.util.Log;

public class SocketUtils {

	private static final String TAG = "MyAccessibility";
	private static final String AUTO_TAG = "AUTOMATION";
	private static final int PORT = 9999;
	private static final int PORT2 = 8888;
	
	public static ServerSocket mServerSocket = null;
	public static ServerSocket serverSocket = null;
	public static Socket mSocket;
	public static Socket socket ;
	private static boolean isRun = true;
	private static BufferedOutputStream out = null;

	private static InputStream ips;
	private static InputStreamReader ipsr;
	private static BufferedReader br;

	synchronized public void sendUIStr(String datastr) {
		// synchronized (this) {
		try {
			// String ip = InetAddress.getLocalHost().getHostAddress();
			// System.out.println("send ip地址是: " + ip);
			// Log.e(AUTO_TAG, "send ip addr is : " + ip);

			if (serverSocket != null) {
				//serverSocket.close();
				//serverSocket = null;
			}
			if (serverSocket == null) {
				serverSocket = new ServerSocket(PORT);
				serverSocket.setReuseAddress(true);
				if (!serverSocket.isBound()) {
					serverSocket.bind(new InetSocketAddress(PORT));
				}
			}

			// serverSocket = new ServerSocket(PORT);
			//Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket" + "建立 send Socket");
			Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket send is connected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket" + e.getMessage());
			Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket exception :" + e.getMessage());
		}

		System.out.println("send AeccibilityServiceSocket :" + "开始监听");
		Tool.toolLog(AUTO_TAG +  "send AeccibilityServiceSocket start to listen");
		// System.out.println("socket22 mText111 :" + mText);
		// while (true) {
		try {
			if (!isRun) {
				// break;
			}
			mSocket = serverSocket.accept();
			System.out.println("send AeccibilityServiceSocket :" + "检测到有连接");
			System.out.println("send AeccibilityServiceSocket :  已经连接");
			Tool.toolLog(AUTO_TAG + " Exist AeccibilityServiceSocket send connection!");

			// socket.setKeepAlive(true);
			mSocket.setTcpNoDelay(true);
			// socket.setSoTimeout(5000);
			mSocket.setSendBufferSize(4096);
			out = new BufferedOutputStream(mSocket.getOutputStream());

			Tool.toolLog(AUTO_TAG + " After get outputStream");
			String str = "";

			// str = packageName + "-" + str;
			// Log.e(TAG, "activities-->"+str);
//			if (count == 0) {
//				MyAccessibility.getApp();
//				for (String mStr : MyAccessibility.appInfoList) {
//					str += mStr + "*";
//				}
//				out.write(str.getBytes("utf-8"));
//				out.flush();
//				str = "";
//				for (String mStr : MyAccessibility.mlist) {
//					str += mStr + "*";
//				}
//				out.write(str.getBytes("utf-8"));
//			} else {
				/*for (String mStr : MyAccessibility.mlist) {
					str += mStr + "$";
				}

				str = str + "\r\n";*/
				str = datastr;
				Tool.toolLog("Message :" + str);
				out.write(str.getBytes("utf-8"));
				
				//out.write(("UI").getBytes("utf-8"));
			//}

			Tool.toolLog(AUTO_TAG + " After write to output stream");
			out.flush();
			// out.close();
			System.out.println("Message Sended!");
			Tool.toolLog(AUTO_TAG + " send conent to client");
			//count++;
		} catch (Exception e) {
			System.out.println("set up socket exception22 :" + e.getMessage());
			Tool.toolLog(AUTO_TAG + " Exception UI :" + e.toString());

		} finally {
//			try {
//
//				if (out != null) {
//					out.close();
//					out = null;
//				}
//
//				if (mSocket != null) {
//					mSocket.close();
//					isRun = false;
//					mSocket = null;
//				}
//
//				if (serverSocket != null) {
//					serverSocket.close();
//					serverSocket = null;
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			// }
		}
		// }
	}

	synchronized public void sendPackageInfo() {/*
		// synchronized (this) {
		try {
			// String ip = InetAddress.getLocalHost().getHostAddress();
			// System.out.println("send ip地址是: " + ip);
			// Log.e(AUTO_TAG, "send ip addr is : " + ip);

			if (serverSocket != null) {
				//serverSocket.close();
				//serverSocket = null;
			}
			if (serverSocket == null) {
				serverSocket = new ServerSocket(PORT);
				serverSocket.setReuseAddress(true);
				if (!serverSocket.isBound()) {
					serverSocket.bind(new InetSocketAddress(PORT));
				}
			}

			// serverSocket = new ServerSocket(PORT);
			System.out.println("AeccibilityServiceSocket" + "建立 send Socket");
			Log.e(AUTO_TAG, "AeccibilityServiceSocket send is connected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("AeccibilityServiceSocket" + e.getMessage());
			Log.d(AUTO_TAG, "AeccibilityServiceSocket exception :" + e.getMessage());
		}

		System.out.println("send AeccibilityServiceSocket :" + "开始监听");
		Log.e(AUTO_TAG, "send AeccibilityServiceSocket start to listen");
		// System.out.println("socket22 mText111 :" + mText);
		// while (true) {
		try {
			socket = serverSocket.accept();
			System.out.println("send AeccibilityServiceSocket :" + "检测到有连接");
			System.out.println("send AeccibilityServiceSocket :  已经连接");
			Log.e(AUTO_TAG, " Exist AeccibilityServiceSocket send connection!");

			// socket.setKeepAlive(true);
			socket.setTcpNoDelay(true);
			// socket.setSoTimeout(5000);
			socket.setSendBufferSize(4096);
			out = new BufferedOutputStream(socket.getOutputStream());

			Log.e(AUTO_TAG, "After get outputStream");
			String str = "";

			MyAccessibility.getApp();
			for (String mStr : MyAccessibility.appInfoList) {
				str += mStr + "*";
			}
			out.write(str.getBytes("utf-8"));
			
			//out.write(("package").getBytes("utf-8"));

			Log.e(AUTO_TAG, "After write to output stream");
			out.flush();
			// out.close();
			System.out.println("Message Sended!");
			Log.e(AUTO_TAG, "send conent to client");
			// count++;
		} catch (Exception e) {
			System.out.println("set up socket exception22 :" + e.getMessage());
			Log.e(AUTO_TAG, "Exception Package:" + e.toString());

		} finally {
//			try {

//				if (out != null) {
//					out.close();
//					out = null;
//				}
//
//				if (socket != null) {
//					socket.close();
//					isRun = false;
//					socket = null;
//				}
//
//				if (serverSocket != null) {
//					serverSocket.close();
//					serverSocket = null;
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			// }
		}
		// }
	*/}
	
	synchronized public void sendDismentions() {/*
		// synchronized (this) {
		try {
			// String ip = InetAddress.getLocalHost().getHostAddress();
			// System.out.println("send ip地址是: " + ip);
			// Log.e(AUTO_TAG, "send ip addr is : " + ip);

			if (serverSocket != null) {
				//serverSocket.close();
				//serverSocket = null;
			}
			if (serverSocket == null) {
				serverSocket = new ServerSocket(PORT);
				serverSocket.setReuseAddress(true);
				if (!serverSocket.isBound()) {
					serverSocket.bind(new InetSocketAddress(PORT));
				}
			}

			// serverSocket = new ServerSocket(PORT);
			System.out.println("AeccibilityServiceSocket" + "建立 send Socket");
			Log.e(AUTO_TAG, "AeccibilityServiceSocket send is connected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("AeccibilityServiceSocket" + e.getMessage());
			Log.d(AUTO_TAG, "AeccibilityServiceSocket exception :" + e.getMessage());
		}

		System.out.println("send AeccibilityServiceSocket :" + "开始监听");
		Log.e(AUTO_TAG, "send AeccibilityServiceSocket start to listen");
		// System.out.println("socket22 mText111 :" + mText);
		// while (true) {
		try {
			socket = serverSocket.accept();
			System.out.println("send AeccibilityServiceSocket :" + "检测到有连接");
			System.out.println("send AeccibilityServiceSocket :  已经连接");
			Log.e(AUTO_TAG, " Exist AeccibilityServiceSocket send connection!");

			// socket.setKeepAlive(true);
			socket.setTcpNoDelay(true);
			// socket.setSoTimeout(5000);
			socket.setSendBufferSize(4096);
			out = new BufferedOutputStream(socket.getOutputStream());

			Log.e(AUTO_TAG, "After get outputStream");
			String str = "";

			//MyAccessibility.getApp();
			//for (String mStr : MyAccessibility.appInfoList) {
		//		str += mStr + "*";
		//	}
			str =MyAccessibility.width+","+MyAccessibility.height; 
			out.write(str.getBytes("utf-8"));
			
			//out.write(("package").getBytes("utf-8"));

			Log.e(AUTO_TAG, "After write to output stream");
			out.flush();
			// out.close();
			System.out.println("Message Sended!");
			Log.e(AUTO_TAG, "send conent to client");
			// count++;
		} catch (Exception e) {
			System.out.println("set up socket exception22 :" + e.getMessage());
			Log.e(AUTO_TAG, "Exception Package:" + e.toString());

		} finally {

		}
	*/}
	
	@SuppressWarnings("finally")
	static public String receData() {
		String s = "";
		String line = "";
		try {
			// String ip = InetAddress.getLocalHost().getHostAddress();
			// System.out.println("receive ip地址是: " + ip);
			// Log.e(AUTO_TAG, "receive ip addr is : " + ip);

			if (mServerSocket != null) {
				//mServerSocket.close();
				//mServerSocket = null;
			}
			if (mServerSocket == null) {
				mServerSocket = new ServerSocket(PORT2);
				mServerSocket.setReuseAddress(true);
				if (!mServerSocket.isBound()) {
					mServerSocket.bind(new InetSocketAddress(PORT2));
				}
			}
			// mServerSocket = new ServerSocket(PORT2);
			System.out.println("AeccibilityServiceSocket" + "建立receive Socket");
			Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket receive is connected");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("AeccibilityServiceSocket" + e.getMessage());
			Tool.toolLog(AUTO_TAG + " AeccibilityServiceSocket exception :" + e.getMessage());
		}
		// while (true) {
		
		try {
			mSocket = mServerSocket.accept();
			System.out.println("AeccibilityServiceSocket :" + "检测到有连接");
			System.out.println("AeccibilityServiceSocket :  已经连接");
			Tool.toolLog(AUTO_TAG + " Exist AeccibilityServiceSocket connection receive!");

			ips = mSocket.getInputStream();
			ipsr = new InputStreamReader(ips);
			br = new BufferedReader(ipsr);
			
			while ((s = br.readLine()) != null) {
				line += s;
			}
			System.out.println("receive data is :" + line);
			Tool.toolLog(AUTO_TAG + " receive data is :" + line);
//			if (line.contains("send")) {
////				if (br != null) {
////					br.close();
////				}
////
////				if (ipsr != null) {
////					ipsr.close();
////				}
////
////				if (ips != null) {
////					ips.close();
////				}
////				if (mSocket != null) {
////					mSocket.close();
////				}
//				// Log.e(AUTO_TAG, "contains send222222222222222");
//				return line;
//			}
		} catch (Exception ex) {
			System.out.println("ex : " + ex.toString());
			return null;
		} finally {
			try {
				if (br != null) {
					br.close();
				}

				if (ipsr != null) {
					ipsr.close();
				}

				if (ips != null) {
					ips.close();
				}
				if (mSocket != null) {
					mSocket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return line;
		}
		// }
	}
}
