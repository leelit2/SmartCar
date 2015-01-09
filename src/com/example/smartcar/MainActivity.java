package com.example.smartcar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	// 蓝牙管理类
	private BluetoothAdapter btAdapter;
	private BluetoothDevice btDevice;
	private BluetoothSocket btSocket;

	// 控件
	private TextView textView;
	private ProgressBar progressBar;
	private ListView listView;

	private List<String> showList = new ArrayList<String>();
	private ArrayAdapter<String> btInfoAdapter;

	// 扫描到的蓝牙设备，用于创建Socket
	private List<BluetoothDevice> remoteBtList = new ArrayList<BluetoothDevice>();

	// 蓝牙扫描相关广播
	private BroadcastReceiver btFoundReceiver;
	private BroadcastReceiver scanFinishedReceiver;

	// 蓝牙连接相关广播
	private BroadcastReceiver btConnectedReceiver;
	private BroadcastReceiver btDisConnectReceiver;
	private boolean isConnected;
	private String connectedDevice;

	// 创建Rfcomm通道的UUDI码
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// 输出流
	public static OutputStream outputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//实例化本地蓝牙对象
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		init();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 点击ListView的Item
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				btAdapter.cancelDiscovery();
				progressBar.setVisibility(View.INVISIBLE);
				// 得到点击的蓝牙设备对象
				btDevice = btAdapter.getRemoteDevice(remoteBtList.get(arg2)
						.getAddress());
				connectedDevice = remoteBtList.get(arg2).getName();
				// 用一个AlertDialog来提示连接
				new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher).setTitle("连接此设备？")
						.setPositiveButton("确定", new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								// 开始连接
								new TryToConnect().start();
							}
						}).show();
			}
		});
	}

	private void init() {
		textView = (TextView) findViewById(R.id.textview);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);

		// ListView相关初始化
		listView = (ListView) findViewById(R.id.listview);
		btInfoAdapter = new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_list_item_1, showList);
		listView.setAdapter(btInfoAdapter);

		// 注册发现蓝牙以及扫描结束的广播
		btFoundReceiver = new BtFoundReceiver();
		scanFinishedReceiver = new BtScanFinishedReceiver();
		IntentFilter foundFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		IntentFilter finishFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(btFoundReceiver, foundFilter);
		registerReceiver(scanFinishedReceiver, finishFilter);

		// 注册蓝牙连接及断开广播
		btConnectedReceiver = new BtConnectedReceiver();
		btDisConnectReceiver = new BtDisconnectReceiver();
		IntentFilter connectedFilter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_CONNECTED);
		IntentFilter disconnectedFilter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED);
		registerReceiver(btConnectedReceiver, connectedFilter);
		registerReceiver(btDisConnectReceiver, disconnectedFilter);
	}

	private class BtFoundReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			textView.setText("扫描到的蓝牙设备有：");
			// 获得扫描到的蓝牙设备对象
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// 将扫描到的蓝牙设备对象存放到remoteList中
			if (!remoteBtList.contains(device)) {
				remoteBtList.add(device);
				// 将该蓝牙设备相关信息存放到showList中
				String btInfo = device.getAddress() + "  " + device.getName();
				showList.add(btInfo);
				btInfoAdapter.notifyDataSetChanged();
			}
		}
	}

	private class BtScanFinishedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			progressBar.setVisibility(View.INVISIBLE);
			if (remoteBtList.size() == 0) {
				Toast.makeText(MainActivity.this, "没有扫描到有蓝牙设备，请重试...",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, Menu.NONE, "打开蓝牙");
		menu.add(Menu.NONE, 1, Menu.NONE, "关闭蓝牙");
		menu.add(Menu.NONE, 2, Menu.NONE, "扫描周围蓝牙设备");
		menu.add(Menu.NONE, 3, Menu.NONE, "进入控制台");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case 0:
			turnOnBluetooth();
			break;

		case 1:
			turnOffBluetooth();
			break;

		case 2:
			scanBluetooth();
			break;

		case 3:
			turnToControlActivity();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void turnOnBluetooth() {
		if (!btAdapter.isEnabled()) {
			btAdapter.enable();
			Toast.makeText(MainActivity.this, "蓝牙开启中...", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "蓝牙已开启", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void turnOffBluetooth() {
		if (btAdapter.isEnabled()) {
			btAdapter.disable();
		}
	}

	private void scanBluetooth() {
		if (btAdapter.isEnabled()) {
			// 扫描周围的蓝牙设备
			btAdapter.startDiscovery();
			progressBar.setVisibility(View.VISIBLE);
			Toast.makeText(MainActivity.this, "开始扫描", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "请打开蓝牙", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void turnToControlActivity() {
		if (isConnected) {
			// 如果连接成功，跳转到控制Activity
			Intent intent = new Intent(MainActivity.this, ControlActivity.class);
			startActivity(intent);
		} else {
			Toast.makeText(MainActivity.this, "请先连接蓝牙", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class TryToConnect extends Thread {
		public void run() {
			try {
				// 实例化BluetoothSocket对象
				btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
				btSocket.connect();
				outputStream = btSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class BtConnectedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			isConnected = true;
			textView.setText("蓝牙连接至" + connectedDevice + "，请进入控制台");
		}

	}

	private class BtDisconnectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			isConnected = false;
			textView.setText("蓝牙断开连接");
			// 断开后重新连接
			new TryToConnect().start();
			Toast.makeText(MainActivity.this, "蓝牙断开连接，重新连接中...",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 取消注册广播
		unregisterReceiver(btFoundReceiver);
		unregisterReceiver(scanFinishedReceiver);
		unregisterReceiver(btConnectedReceiver);
		unregisterReceiver(btDisConnectReceiver);
		try {
			if (btSocket != null)
				btSocket.close();
			if (outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
