package tw.edu.mcu.csie.g24.blescan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btnSearch;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		setView();
		setAction();
	}

	private void setView() {
		btnSearch = (Button) findViewById(R.id.btn_bluetooth_search);

	}

	private void setAction() {

		btnSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(MainActivity.this,ScanBLEActivity.class);
				startActivity(intent);
			}
		});

	}

}
