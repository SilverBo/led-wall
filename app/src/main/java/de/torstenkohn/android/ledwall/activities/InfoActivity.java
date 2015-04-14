package de.torstenkohn.android.ledwall.activities;

import android.os.Bundle;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;

/**
 * The activity is only used to display information about the project
 * @author Torsten Kohn
 * @since 08.04.2013
 */
public class InfoActivity extends LEDWallActivity {

	/**
	 * The method initializes the connection to the server
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_info);
		super.onCreate(savedInstanceState);
		initConnection();
		setActionbarTitle(R.string.menu_about);
	}// onCreate

	@Override
	protected void onStart() {
		super.onStart();
	}// onStart
	
}// class InfoActivity
