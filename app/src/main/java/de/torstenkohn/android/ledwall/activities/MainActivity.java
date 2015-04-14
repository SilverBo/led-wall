package de.torstenkohn.android.ledwall.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.connection.ConnectionManager;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;

/**
 * The activity is the entry Acticity. So it is the activity that appears<br/>
 * when you first launch the app. The activity accesses the SharedPreferences<br/>
 * to read the ip address and port, and then handed over to the Connection Manager.<br/>
 * In addition, the activity accesses the Secure class to read the UDID.<br/>
 * From this activity, the connection can be established to the server and<br/>
 * then you can use the full features of the app.
 * 
 * @author Torsten Kohn
 * @since 08.04.2013
 *
 */
public class MainActivity extends LEDWallActivity {

	/**
	 * The method initializes the connection to the server<br/>
	 * and calls the init method.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		super.onCreate(savedInstanceState);
		initConnection();

		init();

		// If a server connection exists, the MenuActivity is launched
		if (getConnectionManager().isConnected()) {
			startLEDWallActivity(MenuActivity.class);
		}// if
	}// onCreate

	
	/**
	 * Initializes the button to establish a connection to the server.<br/>
	 * In addition, the method accesses the SharedPreferences.
	 */
	private void init(){
		ConnectionManager connMan = getConnectionManager();
		Log.d(getTAG(), "conn: " + connMan);
		if (connMan.getUdid() == 0) {
			connMan.setUdid(generateUDID());
		}// if

		SharedPreferences prefs = getSharedPreferences(DATA_FILENAME,
				MODE_PRIVATE);
		connMan.setIpaddress(prefs.getString(DATA_IP_ADDRESS, "192.168.10.110"));
		connMan.setPort(prefs.getInt(DATA_PORT, 5432));

		Button connect = (Button) findViewById(R.id.main_button_connect);
		connect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getConnectionManager().isConnected()) {
					startLEDWallActivity(MenuActivity.class);
				} else {
					connect();
				}// if
			}// onClick
		});
	}// init
	
	
	@Override
	protected void onStart() {
		super.onStart();
	}// onStart

	
	/**
	 * Terminates the thread for communication once the activity is stopped or loses focus.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		getService().stopService();
	}// onStop

	
	/**
	 * The method accesses the class Secure<br/>
	 * of the Android system and reads the Android ID.
	 * @return of type Integer, the UDID of the device
	 */
	private int generateUDID() {
		int udid = 0;

		String temp = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);
		udid = temp.hashCode();
		if (D) {
			Log.d(getTAG(), "Android ID: " + temp);
			Log.d(getTAG(), "Android ID hash: " + udid);
		}// if

		return Math.abs(udid);
	}// generateUDID

	
	/**
	 * Connection to the server is established and the progress dialog starts.
	 */
	private void connect() {
		if (!getConnectionManager().isConnected()) {
			startProgressDialog();

			String message = getLedWallMessage().getFunctionConnect("menu");
			getConnectionManager().addMessage(message);

			getService().startService();

		}// if
	}// connect

}// class MainActivity
