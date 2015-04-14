package de.torstenkohn.android.ledwall.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;

/**
 * The activity is displayed when the connection is established to the server.<br/>
 * Then you have access to the SideNavigation and has access to Draw, Tetris and Text.
 * 
 * @author Torsten Kohn
 * @since 08.04.2013
 *
 */
public class MenuActivity extends LEDWallActivity {
	

	/**
	 * The method initializes the connection to the server<br/>
	 * and provides a button available to disconnect from the server.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_menu);
		super.onCreate(savedInstanceState);
		initConnection();
		
		Button disconnect = (Button) findViewById(R.id.menu_button_disconnect);
		disconnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				disconnect();
			}// onClick
		});
	}// onCreate

	@Override
	protected void onStart() {
		super.onStart();
	}// onStart

}// MenuActivity
