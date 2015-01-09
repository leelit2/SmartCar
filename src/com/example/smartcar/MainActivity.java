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

	// ����������
	private BluetoothAdapter btAdapter;
	private BluetoothDevice btDevice;
	private BluetoothSocket btSocket;

	// �ؼ�
	private TextView textView;
	private ProgressBar progressBar;
	private ListView listView;

	private List<String> showList = new ArrayList<String>();
	private ArrayAdapter<String> btInfoAdapter;

	// ɨ�赽�������豸�����ڴ���Socket
	private List<BluetoothDevice> remoteBtList = new ArrayList<BluetoothDevice>();

	// ����ɨ����ع㲥
	private BroadcastReceiver btFoundReceiver;
	private BroadcastReceiver scanFinishedReceiver;

	// ����������ع㲥
	private BroadcastReceiver btConnectedReceiver;
	private BroadcastReceiver btDisConnectReceiver;
	private boolean isConnected;
	private String connectedDevice;

	// ����Rfcommͨ����UUDI��
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// �����
	public static OutputStream outputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//ʵ����������������
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		init();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ���ListView��Item
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				btAdapter.cancelDiscovery();
				progressBar.setVisibility(View.INVISIBLE);
				// �õ�����������豸����
				btDevice = btAdapter.getRemoteDevice(remoteBtList.get(arg2)
						.getAddress());
				connectedDevice = remoteBtList.get(arg2).getName();
				// ��һ��AlertDialog����ʾ����
				new AlertDialog.Builder(MainActivity.this)
						.setIcon(R.drawable.ic_launcher).setTitle("���Ӵ��豸��")
						.setPositiveButton("ȷ��", new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								// ��ʼ����
								new TryToConnect().start();
							}
						}).show();
			}
		});
	}

	private void init() {
		textView = (TextView) findViewById(R.id.textview);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);

		// ListView��س�ʼ��
		listView = (ListView) findViewById(R.id.listview);
		btInfoAdapter = new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_list_item_1, showList);
		listView.setAdapter(btInfoAdapter);

		// ע�ᷢ�������Լ�ɨ������Ĺ㲥
		btFoundReceiver = new BtFoundReceiver();
		scanFinishedReceiver = new BtScanFinishedReceiver();
		IntentFilter foundFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		IntentFilter finishFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(btFoundReceiver, foundFilter);
		registerReceiver(scanFinishedReceiver, finishFilter);

		// ע���������Ӽ��Ͽ��㲥
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
			textView.setText("ɨ�赽�������豸�У�");
			// ���ɨ�赽�������豸����
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// ��ɨ�赽�������豸�����ŵ�remoteList��
			if (!remoteBtList.contains(device)) {
				remoteBtList.add(device);
				// ���������豸�����Ϣ��ŵ�showList��
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
				Toast.makeText(MainActivity.this, "û��ɨ�赽�������豸��������...",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, Menu.NONE, "������");
		menu.add(Menu.NONE, 1, Menu.NONE, "�ر�����");
		menu.add(Menu.NONE, 2, Menu.NONE, "ɨ����Χ�����豸");
		menu.add(Menu.NONE, 3, Menu.NONE, "�������̨");
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
			Toast.makeText(MainActivity.this, "����������...", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "�����ѿ���", Toast.LENGTH_SHORT)
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
			// ɨ����Χ�������豸
			btAdapter.startDiscovery();
			progressBar.setVisibility(View.VISIBLE);
			Toast.makeText(MainActivity.this, "��ʼɨ��", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "�������", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void turnToControlActivity() {
		if (isConnected) {
			// ������ӳɹ�����ת������Activity
			Intent intent = new Intent(MainActivity.this, ControlActivity.class);
			startActivity(intent);
		} else {
			Toast.makeText(MainActivity.this, "������������", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class TryToConnect extends Thread {
		public void run() {
			try {
				// ʵ����BluetoothSocket����
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
			textView.setText("����������" + connectedDevice + "����������̨");
		}

	}

	private class BtDisconnectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			isConnected = false;
			textView.setText("�����Ͽ�����");
			// �Ͽ�����������
			new TryToConnect().start();
			Toast.makeText(MainActivity.this, "�����Ͽ����ӣ�����������...",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// ȡ��ע��㲥
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
