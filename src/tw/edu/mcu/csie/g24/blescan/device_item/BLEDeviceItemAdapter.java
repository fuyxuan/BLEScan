package tw.edu.mcu.csie.g24.blescan.device_item;

import java.util.List;

import tw.edu.mcu.csie.g24.blescan.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BLEDeviceItemAdapter extends ArrayAdapter<BLEDeviceItem> {

	private LayoutInflater inflater;

	public BLEDeviceItemAdapter(Context context, List<BLEDeviceItem> list) {
		super(context, 0, list);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int p, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.device_item, null);
		}

		final BLEDeviceItem item = (BLEDeviceItem) getItem(p);

		TextView tv;

		tv = (TextView) convertView.findViewById(R.id.tv_address);
		tv.setText(item.getAddress());

		tv = (TextView) convertView.findViewById(R.id.rssi);
		tv.setText(item.getRssi());

		return convertView;
	}
}