package de.torstenkohn.android.ledwall.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;
import de.torstenkohn.android.ledwall.util.IpInputWatcher;

/**
 * The activity is responsible for the settings of the app.<br/> 
 * It can be changed in the activity only the IP address and port.<br/>
 * When the smartphone is connected to the server,<br/>
 * it is impossible to change the settings.
 * 
 * @author Torsten Kohn
 * @since 20.04.2013
 *
 */
public class SettingsActivity extends LEDWallActivity {

	
	/**
	 * maximum port number
	 */
	private static final int PORT_MAX = 65535;

	
	/**
	 * minimum port number
	 */
	private static final int PORT_MIN = 1024;

	
	private EditText ipaddress;
	private EditText port;

	
	/**
	 * The method initializes the connection to the server
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_settings);
		super.onCreate(savedInstanceState);
		initConnection();
		setActionbarTitle(R.string.menu_settings);
	}// onCreate

	/**
	 * Calls the method init() to initialize the surface.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		init();
	}// onStart

	
	/**
	 * The method sets the input fields on the current IP address and port.<br/>
	 * The input field for the IP address is monitored with an input watcher.<br/>
	 * 
	 */
	private void init() {
		ipaddress = (EditText) findViewById(R.id.settings_editText_ipaddress);
		ipaddress.setText(getConnectionManager().getIpaddress());
		
		/*
		 * The input field for the IP address is monitored with an input watcher 
		 * de.torstenkohn.android.ledwall.IpInputWatcher,
		 * so that only valid IP addresses can be entered.
		 */
		ipaddress.addTextChangedListener(new IpInputWatcher());

		port = (EditText) findViewById(R.id.settings_editText_port);
		port.setText(getConnectionManager().getPort() + "");

		TextView infosave = (TextView)findViewById(R.id.settings_textView_infosave);

		Button save = (Button) findViewById(R.id.settings_button_save);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String ipaddress = SettingsActivity.this.ipaddress.getText()
						.toString().trim();
				int port = 0;
				
				try {
					port = Integer.parseInt(SettingsActivity.this.port
							.getText().toString().trim());
				} catch (NumberFormatException e) {
					//the exception will not occur, because only numbers can be entered 
				}//try
				
				if (isInputIpOK(ipaddress) && isInputPortOK(port)) {
					// save the new ip address and port
					setIpaddress(ipaddress);
					setPort(port);
					startLEDWallActivity(MainActivity.class);
					
					// notification to the user
					Toast.makeText(getApplicationContext(), R.string.settings_toast_save,
							Toast.LENGTH_LONG).show();	
				}// if
			}// onClick
		});
		
		
		if (getConnectionManager().isConnected()) {
			
			/*
			 * Disables the input fields, if there is a connection and 
			 * activates an info text so the user knows 
			 * why he just can not make any adjustments.
			 */
			ipaddress.setEnabled(false);
			port.setEnabled(false);
			save.setEnabled(false);
			infosave.setVisibility(View.VISIBLE);
		} else {
			
			/*
			 * Enables the text fields if there is no 
			 * connection and disables the info that is 
			 * displayed when a connection is established.
			 */
			ipaddress.setEnabled(true);
			port.setEnabled(true);
			save.setEnabled(true);
			infosave.setVisibility(View.GONE);
		}// if
		
	}// init

	
	/**
	 * The method checks the IP address. If the address is of 4 numbers separated by a dot,<br/>
	 * the return valus is true, otherwise false.
	 * The method displays an error message at the input field, if the input was incorrect. 
	 * @param ipaddress of type String, the entered ip address
	 * @return of type boolean, true -> correct ip address | false -> incorrect ip address
	 */
	private boolean isInputIpOK(String ipaddress) {
		String IPRegularExpression = "(\\d+)(\\.)(\\d+)(\\.)(\\d+)(\\.)(\\d+)";
		if (ipaddress.matches(IPRegularExpression)) {
			return true;
		}// if
		this.ipaddress.setError(getApplicationContext().getResources().getText(R.string.settings_error_ipaddress));
		return false;
	}// isInputIpOK

	
	/**
	 * The method checks the specified port if this is in the correct range.<br/>
	 * The port must be in the range 1024 to 65535.<br/>
	 * The method returns false if the port is not in the range.
	 * The method displays an error message at the input field, if the input was incorrect.
	 * @param port of type Integer, the entered port
	 * @return of type boolean, true -> port is in the range | false -> port is not in the range
	 */
	private boolean isInputPortOK(int port) {

		if (port >= PORT_MIN && port <= PORT_MAX) {
			return true;
		}// if
		this.port.setError(getApplicationContext().getResources().getText(R.string.settings_error_port));
		return false;
	}// isInputPortOK

}// class SettingsActivity
