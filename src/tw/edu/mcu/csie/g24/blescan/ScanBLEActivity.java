package tw.edu.mcu.csie.g24.blescan;

import java.util.ArrayList;

import tw.edu.mcu.csie.g24.blescan.device_item.BLEDeviceItem;
import tw.edu.mcu.csie.g24.blescan.device_item.BLEDeviceItemAdapter;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScanBLEActivity extends Activity implements BluetoothAdapter.LeScanCallback {

	protected BluetoothManager bluetooth_manager;
	protected BluetoothAdapter bluetooth_adapter;
	protected Handler handler = new Handler();
	protected boolean is_scannning = false;
	
	static final String Tag = "msg";

	ArrayList<BLEDeviceItem> device_list = new ArrayList<BLEDeviceItem>();
	ArrayList<BLEDeviceItem> device_list_copy = new ArrayList<BLEDeviceItem>(); // for list_view
	BLEDeviceItemAdapter list_view_adaptor;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SCAN_DULATION = 1000;//1000
	private static final int SCAN_INTERVAL = 500;

	ListView list_view;
	TextView empty_view;
	WebView myBrowser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_ble);
		
		
		
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { //判斷手機是否支援搜尋低功率藍芽
			toast_message(R.string.ble_not_supported);
			return;
		}
		
		if (setupBluetooth() == false) {//手藍芽雅是否開啟
			return;
		}		
		
		setListView();
		setWebSite();
	}

	
	
	@Override
	public void onResume() {
		Log.d(Tag, "onResume()");

		super.onResume();
		startBLEScan();
	}

	@Override
	public void onPause() {
		Log.d(Tag, "onPause()");

		stopBLEScan(false);
		super.onPause();
	}

	private void setListView() {
		list_view = (ListView) findViewById(R.id.listView1);
		list_view_adaptor = new BLEDeviceItemAdapter(this, device_list_copy);
		list_view.setAdapter(list_view_adaptor);
		empty_view = (TextView) findViewById(R.id.empty_view);

	}
	
	private void setWebSite(){
			String myURL = "http://www.google.com/";         
	        myBrowser=(WebView)findViewById(R.id.mybrowser);  
	  
	        WebSettings websettings = myBrowser.getSettings();  
	        websettings.setSupportZoom(true);  
	        websettings.setBuiltInZoomControls(true);   
	        myBrowser.setWebViewClient(new WebViewClient());  
	        myBrowser.loadUrl(myURL);  
	        myBrowser.setVisibility(View.INVISIBLE);
	}



	protected boolean setupBluetooth() {
		// check for the presence of bluetooth adaptor.
		bluetooth_manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		bluetooth_adapter = bluetooth_manager.getAdapter();
		if (bluetooth_adapter == null) {
			toast_message(R.string.bluetooth_not_supported);
			return false;
		}
		
		return true;
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(Tag, "onActivityResult()");

		if (requestCode == REQUEST_ENABLE_BT&& resultCode == Activity.RESULT_CANCELED) {
			toast_message(R.string.bluetooth_not_enabled);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
		
		// Order of function call : onCreate()->onResume()->call startActivityForResult()->onPause()->[intent]->onActivityResult()->onResume()->...
	}

	private void startBLEScan() {
		Log.d(Tag, "startBLEScan()");

		if (is_scannning == false) {
			device_list.clear();
			bluetooth_adapter.startLeScan(this);
		}
		is_scannning = true;

		handler.postDelayed(handle_stop_ble_scan, SCAN_DULATION);
	}

	private void stopBLEScan(boolean flag) {
		Log.d(Tag, "stopBLEScan()");

		if (is_scannning == true) {
			bluetooth_adapter.stopLeScan(this);
		}

		updateListView();

		is_scannning = false;

		if (flag == true) {
			handler.postDelayed(handle_start_ble_scan, SCAN_INTERVAL);
		} else {
			handler.removeCallbacks(handle_start_ble_scan);
			handler.removeCallbacks(handle_stop_ble_scan);
		}
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		BLEDeviceItem device_item = new BLEDeviceItem();
		device_item.setName(device.getName());
		device_item.setAddress(device.getAddress());
		device_item.setRssi(Integer.toString(rssi));
		
		
		if(rssi>(-50)){
			myBrowser.setVisibility(View.VISIBLE);
		}else{
			myBrowser.setVisibility(View.INVISIBLE);
		}

		device_list.add(device_item);
		//==========
		
		 int startByte = 2;
         boolean patternFound = false;
         // 寻找ibeacon
         // 先依序尋找第2到第8陣列的元素
         while (startByte <= 5) {
             if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && 
                     // Identifies an iBeacon
                     ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { 
                         // Identifies correct data length
                 patternFound = true;
                 break;
             }
             startByte++;
         }
         
      // 如果找到了的话
         if (patternFound) {
        	 bluetooth_adapter.stopLeScan(this);  
             // 轉換為16進制
             byte[] uuidBytes = new byte[16];
             // 來源、起始位置
             System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
             String hexString = bytesToHex(uuidBytes);

             // ibeacon的UUID值
             String uuid = hexString.substring(0, 8) + "-"
                     + hexString.substring(8, 12) + "-"
                     + hexString.substring(12, 16) + "-"
                     + hexString.substring(16, 20) + "-"
                     + hexString.substring(20, 32);

             // ibeacon的Major值
             int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

             // ibeacon的Minor值
             int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

             int txPower = (scanRecord[startByte + 24]);
             double distance = calculateAccuracy(txPower,rssi);
             Log.d(Tag,bytesToHex(scanRecord));
             Log.d(Tag, " \nUUID：" + uuid + "\nMajor：" + major + "\nMinor："
                     + minor + "\nTxPower：" + txPower + "\nrssi：" + rssi);

             Log.d(Tag,"distance："+calculateAccuracy(txPower,rssi));
             Intent intent = new Intent();
             intent.setAction("i_am_hungry");
             intent.putExtra("HELLO", distance);
             sendBroadcast(intent);

         }
		
	}
	
	protected void updateListView() {
		Log.d(Tag, "updateListView() : device_list.size()=" + device_list.size());

		device_list_copy.clear();
		for (BLEDeviceItem item : device_list) {
			device_list_copy.add(item);
		}
		list_view_adaptor.notifyDataSetChanged();
		list_view.invalidate();
		device_list.clear();
		
		if (device_list_copy.size() > 0) {
			empty_view.setVisibility(View.INVISIBLE);
		}
		else {
			empty_view.setVisibility(View.VISIBLE);
		}
	}



	private void toast_message(int message_id) {
		Toast.makeText(this, message_id, Toast.LENGTH_LONG).show();
		finish();
	}

	protected Runnable handle_start_ble_scan = new Runnable() {
		@Override
		public void run() {
			startBLEScan();
		}
	};

	protected Runnable handle_stop_ble_scan = new Runnable() {
		@Override
		public void run() {
			stopBLEScan(true);
		}
	};
	
	// 計算距離
    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
    
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    // 字組轉16進制
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
