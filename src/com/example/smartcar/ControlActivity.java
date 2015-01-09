package com.example.smartcar;

import java.io.IOException;
import java.io.OutputStream;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class ControlActivity extends ActionBarActivity implements
		OnTouchListener {

	// 按键控制
	private Button upButton, downButton, leftButton, rightButton;

	// 重力感应控制
	private Sensor sensor;
	private SensorManager sensorManager;

	// 防止重力感应控制时发送大量数据
	private boolean upOneTime = true;
	private boolean downOneTime = true;
	private boolean leftOneTime = true;
	private boolean rightOneTime = true;
	private boolean stopOneTime = true;

	// 输出流
	private OutputStream outputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		init();
	}

	private void init() {
		upButton = (Button) findViewById(R.id.upButton);
		downButton = (Button) findViewById(R.id.downButton);
		leftButton = (Button) findViewById(R.id.leftButton);
		rightButton = (Button) findViewById(R.id.rightButton);
		upButton.setOnTouchListener(this);
		downButton.setOnTouchListener(this);
		leftButton.setOnTouchListener(this);
		rightButton.setOnTouchListener(this);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, 0, Menu.NONE, "按键控制");
		menu.add(Menu.NONE, 1, Menu.NONE, "重力感应控制");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == 0) {
			upButton.setEnabled(true);
			downButton.setEnabled(true);
			leftButton.setEnabled(true);
			rightButton.setEnabled(true);
			upButton.setText("上");
			downButton.setText("下");
			leftButton.setText("左");
			rightButton.setText("右");
			// 取消注册传感器监听器
			sensorManager.unregisterListener(listener);
		} else if (id == 1) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			leftButton.setEnabled(false);
			rightButton.setEnabled(false);
			upButton.setText("左");
			downButton.setText("右");
			leftButton.setText("下");
			rightButton.setText("上");
			// 注册传感器监听器
			sensorManager.registerListener(listener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		int id = view.getId();
		switch (id) {
		case R.id.upButton:
			writeData(event, "a", "e");
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				upButton.setBackgroundResource(R.drawable.up1);
			if (event.getAction() == MotionEvent.ACTION_UP)
				upButton.setBackgroundResource(R.drawable.up);
			break;

		case R.id.downButton:
			writeData(event, "b", "e");
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				downButton.setBackgroundResource(R.drawable.down1);
			if (event.getAction() == MotionEvent.ACTION_UP)
				downButton.setBackgroundResource(R.drawable.down);
			break;

		case R.id.leftButton:
			writeData(event, "c", "e");
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				leftButton.setBackgroundResource(R.drawable.left1);
			if (event.getAction() == MotionEvent.ACTION_UP)
				leftButton.setBackgroundResource(R.drawable.left);
			break;

		case R.id.rightButton:
			writeData(event, "d", "e");
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				rightButton.setBackgroundResource(R.drawable.right1);
			if (event.getAction() == MotionEvent.ACTION_UP)
				rightButton.setBackgroundResource(R.drawable.right);
			break;
		}
		return false;
	}

	private void writeData(MotionEvent event, String data1, String data2) {
		outputStream = MainActivity.outputStream;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			byte[] buffer = new byte[8];
			buffer = data1.getBytes();
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			byte[] buffer = new byte[8];
			buffer = data2.getBytes();
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private SensorEventListener listener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

				if (arg0.values[0] < -2.4 && upOneTime) {
					writeData("a");
					upOneTime = false;
					downOneTime = true;
					leftOneTime = true;
					rightOneTime = true;
					stopOneTime = true;
					upButton.setBackgroundResource(R.drawable.up);
					downButton.setBackgroundResource(R.drawable.down);
					leftButton.setBackgroundResource(R.drawable.left);
					rightButton.setBackgroundResource(R.drawable.right1);
				}
				if (arg0.values[0] > 2.4 && downOneTime) {
					writeData("b");
					upOneTime = true;
					downOneTime = false;
					leftOneTime = true;
					rightOneTime = true;
					stopOneTime = true;
					upButton.setBackgroundResource(R.drawable.up);
					downButton.setBackgroundResource(R.drawable.down);
					leftButton.setBackgroundResource(R.drawable.left1);
					rightButton.setBackgroundResource(R.drawable.right);
				}
				if (arg0.values[1] < -2.2 && leftOneTime) {
					writeData("c");
					upOneTime = true;
					downOneTime = true;
					leftOneTime = false;
					rightOneTime = true;
					stopOneTime = true;
					upButton.setBackgroundResource(R.drawable.up1);
					downButton.setBackgroundResource(R.drawable.down);
					leftButton.setBackgroundResource(R.drawable.left);
					rightButton.setBackgroundResource(R.drawable.right);
				}
				if (arg0.values[1] > 2.2 && rightOneTime) {
					writeData("d");
					upOneTime = true;
					downOneTime = true;
					leftOneTime = true;
					rightOneTime = false;
					stopOneTime = true;
					upButton.setBackgroundResource(R.drawable.up);
					downButton.setBackgroundResource(R.drawable.down1);
					leftButton.setBackgroundResource(R.drawable.left);
					rightButton.setBackgroundResource(R.drawable.right);
				}
				if ((-2.4 < arg0.values[0] && arg0.values[0] < 2.4)
						&& (-2.4 < arg0.values[1] && arg0.values[1] < 2.4)
						&& stopOneTime) {
					writeData("e");
					upOneTime = true;
					downOneTime = true;
					leftOneTime = true;
					rightOneTime = true;
					stopOneTime = false;
					upButton.setBackgroundResource(R.drawable.up);
					downButton.setBackgroundResource(R.drawable.down);
					leftButton.setBackgroundResource(R.drawable.left);
					rightButton.setBackgroundResource(R.drawable.right);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub

		}
	};

	private void writeData(String data) {
		outputStream = MainActivity.outputStream;
		byte[] buffer = new byte[8];
		buffer = data.getBytes();
		try {
			outputStream.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(listener);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			if (outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
