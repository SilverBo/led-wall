package de.torstenkohn.android.ledwall.core;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.sidenavigation.ISideNavigationCallback;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.activities.DrawActivity;
import de.torstenkohn.android.ledwall.activities.InfoActivity;
import de.torstenkohn.android.ledwall.activities.MainActivity;
import de.torstenkohn.android.ledwall.activities.MenuActivity;
import de.torstenkohn.android.ledwall.activities.SettingsActivity;
import de.torstenkohn.android.ledwall.activities.TetrisActivity;
import de.torstenkohn.android.ledwall.activities.TextActivity;
import de.torstenkohn.android.ledwall.connection.ConnectionManager;
import de.torstenkohn.android.ledwall.connection.LEDWallService;

/**
 * The class LEDWallActivity serves as the backbone for all Activities of the
 * app.<br/>
 * The class inherits from SherlockActivity to use the Android action bar in
 * older Android versions.<br/>
 * Additionally the class implements the interface ISideNavigationCallback to
 * use a SlideNavigation like the Facebook App.<br/>
 * The class provides the following functions:<br/>
 * - debug information for all Activities<br/>
 * - navigation logic<br/>
 * - menu logic<br/>
 * - Handler logic<br/>
 * 
 * @author Torsten Kohn
 * @since 08.04.2013
 */
public class LEDWallActivity extends SherlockActivity implements
		ISideNavigationCallback {

	
	/**
	 * The variable "D" is used only for debugging purposes, to a central point
	 * of the app LogCat output to enable (true) or disable (false).
	 */
	public final static boolean D = true;

	
	/**
	 * The variable "TAG" is used as a tag for LogCat, this can identify the
	 * class. TAG should always contain the simple name of the class.
	 */
	private String TAG;
	
	
	/**
	 * filename for the SharedPreferences
	 */
	public final static String DATA_FILENAME = "LEDWall";
	
	
	/**
	 * keyword for the ip address in the SharedPreferences
	 */
	public final static String DATA_IP_ADDRESS = "ipaddress";
	
	
	/**
	 * keyword for the port in the SharedPreferences
	 */
	public final static String DATA_PORT = "port";

	
	/*
	 * status code for the Handler message
	 */
	public static final int CONNECTED = 0;
	public static final int TETRIS = 1;
	public static final int DISCONNECTED = 10;
	public static final int FAILURE = 20;
	
	
	/**
	 * keyword for the JSON message by the Handler
	 */
	public static final String JSON = "json";


	/**
	 * View of the side navigation It is used for the library SlideNavigation.
	 */
	private SideNavigationView sideNavigationView;
	
	
	private ProgressDialog progressDialog;
	private LEDWallService service;
	private ConnectionManager connectionManager;
	private LEDWallMessage ledWallMessage;

	
	/**
	 * The method saves the assigned IP address in the SharedPreferences from Android system
	 * @param ipaddress of type String, the ip address to save in the SharedPreferences
	 */
	public void setIpaddress(String ipaddress) {
		ConnectionManager connMan = ConnectionManager.getInstance();
		connMan.setIpaddress(ipaddress);
		SharedPreferences prefs = getSharedPreferences(DATA_FILENAME,
				MODE_PRIVATE);
		prefs.edit().putString(DATA_IP_ADDRESS, connMan.getIpaddress())
				.commit();
	}// setIpaddress

	
	/**
	 * The method saves the given port number in the SharedPreferences from Android system
	 * @param port of type Integer, the port number to save in the SharedPreferences
	 */
	public void setPort(int port) {
		ConnectionManager connMan = ConnectionManager.getInstance();
		connMan.setPort(port);
		SharedPreferences prefs = getSharedPreferences(DATA_FILENAME,
				MODE_PRIVATE);
		prefs.edit().putInt(DATA_PORT, connMan.getPort()).commit();
	}// setPort

	
	protected void setTAG(String TAG) {
		this.TAG = TAG;
	}// setTAG

	
	protected String getTAG() {
		return this.TAG;
	}// getTAG

	
	public LEDWallService getService() {
		return service;
	}// getService

	
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}// getConnectionManager

	
	public LEDWallMessage getLedWallMessage() {
		return ledWallMessage;
	}// getLedWallMessage

	
	/**
	 * The method initializes the navigation on the left side<br/>
	 * and is the entry into the Android App
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTAG(this.getLocalClassName());
		if (D)
			Log.d(TAG, "+++ onCreate +++");

		/*
		 * initialized SideNavigation
		 * Source: https://github.com/johnkil/SideNavigation
		 * - Initializes the slideshowNavigationView with the navigation menu<br/>
		 *   from the resource folder /res/menu/
		 * - Sets the menu on the left side
		 */
		sideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
		sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
		sideNavigationView.setMenuClickCallback(this);
		sideNavigationView.setMode(Mode.LEFT);
		// Enables the logo as a button to use the menu
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}// onCreate

	
	/**
	 * The method disables the logo as a button when the app is not connected to the server.<br/>
	 * In addition, a new service thread is started when the socket connection to the server.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		if (D)
			Log.d(TAG, "++ onStart ++");
		
		if (connectionManager.isConnected()) {
			service.startService();
		}// if
		if (!connectionManager.isConnected()) {
			getSupportActionBar().setHomeButtonEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}// if
	}// onStart

	
	/**
	 * Once the activity is changed or the app loses the focus,<br/>
	 * the thread of communication is terminated.<br/>
	 * The socket connection remains intact.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		service.stopService();
	}// onStop

	
	/**
	 * Sets the title in the ActionBar
	 * @param resId of type Integer, the string resource by the title
	 */
	public void setActionbarTitle(int resId) {
		getSupportActionBar().setTitle(resId);
	}// setActionbarTitle
	
	
	/**
	 * Initializes the default menu for Android apps, which is accessible via
	 * the function key or the menu button. It uses the main_menu.xml
	 * (res/menu/) therefor.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getConnectionManager().isConnected()) {
			getSupportMenuInflater().inflate(R.menu.main_menu_connected, menu);
		} else {
			getSupportMenuInflater().inflate(R.menu.main_menu_disconnected,
					menu);
		}// if
		return super.onCreateOptionsMenu(menu);
	}// onCreateOptionsMenu

	
	/**
	 * The method starts the appropriate Activity<br/>
	 * when in the SideNavigationMenu an entry is clicked.
	 */
	@Override
	public void onSideNavigationItemClick(int itemId) {
		switch (itemId) {
		case R.id.side_navigation_menu_draw:
			startLEDWallActivity(DrawActivity.class);
			break;

		case R.id.side_navigation_menu_text:
			startLEDWallActivity(TextActivity.class);
			break;

		case R.id.side_navigation_menu_tetris:
			startLEDWallActivity(TetrisActivity.class);
			break;
			
		case R.id.side_navigation_menu_settings:
			startLEDWallActivity(SettingsActivity.class);
			break;
			
		case R.id.side_navigation_menu_about:
			startLEDWallActivity(InfoActivity.class);
			break;
		}// switch
	}// onSideNavigationItemClick

	
	/**
	 * The method starts the next Activity and ends the current Activity
	 * @param nextActivity of type Class<LEDWallActivity>, the activity to be launched
	 */
	public void startLEDWallActivity(Class<?> nextActivity) {
		Intent i = new Intent(this, nextActivity);
		Log.d(TAG, nextActivity.getSimpleName());

		startActivity(i);
		overridePendingTransition(0, 0);

		finish();
	}// startLEDWallActivity

	
	/**
	 * The method is responsible for the back press logic.<br/>
	 * If the menu is displayed, it will be hidden. <br/>
	 * Otherwise, the activity is bound back or the app loses the focus.
	 */
	@Override
	public void onBackPressed() {
		// hide menu if it shown
		if (sideNavigationView.isShown()) {
			sideNavigationView.hideMenu();
		} else {
			if (TAG.equals("MainActivity") || TAG.equals("MenuActivity")) {
				super.onBackPressed();
			} else {
				startLEDWallActivity(MainActivity.class);
			}// if
		}// if
	}// onBackPressed

	
	/**
	 * The method starts the appropriate Activity<br/>
	 * when in the menu an entry is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			sideNavigationView.toggleMenu();
			break;
			
		case R.id.menu_settings:
			startLEDWallActivity(SettingsActivity.class);
			break;
			
		case R.id.menu_about:
			startLEDWallActivity(InfoActivity.class);
			break;
			
		case R.id.menu_disconnect:
			disconnect();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}// switch
		return true;
	}// onOptionsItemSelected

	
	/**
	 * The method creates an AlertDialog to show the user an error message
	 * 
	 * @param message
	 *            of type String, the error message which should be displayed
	 */
	public void createErrorDialog(String message) {
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		String button = "";
		button = this.getResources().getString(R.string.connection_ok);
		builder.setPositiveButton(button, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startLEDWallActivity(MainActivity.class);
			}// onClick
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}// method createErrorDialog


	/**
	 * Creates a ProgressDialog and starts the Dialog.<br/>
	 * The ProgressDialog is used to provide feedback to the user when establishing a connection. 
	 */
	public void startProgressDialog() {
		String title = "";
		title = this.getResources().getString(R.string.connection_title);
		
		String message = "";
		message = this.getResources().getString(R.string.connection_message);
		
		progressDialog = ProgressDialog.show(this, title, message, true);
	}// startProgressDialog

	
	/**
	 * Disables the ProgressDialog, if it is present.
	 */
	public void stopProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}// if
	}// stopProgressDialog

	
	/**
	 * Initializes the connection to the server.<br/>
	 * It is an instance requested by the ConnectionManager to manage<br/>
	 * the communication between the smartphone and the server.<br/>
	 * In addition, a LEDWallService object is created, which serves messages <br/>
	 * between phone and server to send.<br/>
	 * It also creates an instance of the class LEDWallMessage<br/>
	 * to generate messages in JSON format.
	 */
	public void initConnection() {
		if (connectionManager == null) {
			connectionManager = ConnectionManager.getInstance();
		}// if
		Log.d(getTAG(), "conn: " + connectionManager);
		if (service == null) {
			service = new LEDWallService(handler, connectionManager, TAG);
		}// if
		if (ledWallMessage == null) {
			ledWallMessage = new LEDWallMessage();
		}// if
	}// initConnection

	
	/**
	 * The method sends the Disconnect function to the server<br/>
	 * and then stops the LEDWallService.
	 * Thereafter, the socket connection is terminated and<br/>
	 * the MainActivity is shown.
	 */
	public void disconnect() {
		if (connectionManager.isConnected()) {
			String message = ledWallMessage.getFunctionDisconnect();
			connectionManager.addMessage(message);
			service.stopService();
			connectionManager.stopConnection();
			startLEDWallActivity(MainActivity.class);
		}// if
	}// disconnect

	/**
	 * The handler object to send messages between activity and LEDWallSerice
	 */
	public final LEDWallHandler handler = new LEDWallHandler(this);

	/**
	 * The handler class is used to communicate between Activity and LEDWallService.<br/>
	 * The class inherits from the android.os.Handler class and<br/>
	 * implements the handleMessage method.<br/>
	 * In addition, a Handler object is created by a constructor,<br/>
	 * which receives as a parameter the activity.<br/>
	 * Thus, the handler is assigned to the correct activity.
	 * 
	 * @author Torsten Kohn
	 * @since 03.05. 2013
	 *
	 */
	private class LEDWallHandler extends Handler {
		
		/**
		 * The variable stores an instance of the current activity, to access these functions.
		 */
		private final WeakReference<LEDWallActivity> ledActivity;

		/**
		 * Constructor for the LEDWallHandler
		 * @param activity of type LEDWallActivity, the current Activity
		 */
		LEDWallHandler(LEDWallActivity activity) {
			ledActivity = new WeakReference<LEDWallActivity>(activity);
		}// constructor

		/**
		 * The method handles the messages from LEDWallService.<br/>
		 * The messages are divided into four categories.<br/><br/>
		 * 
		 * - CONNECTED<br/>
		 *   Connection to the server has been established successfully.<br/>
		 *   The Activity MenuActivity starts.<br/><br/>
		 *   
		 * - TETRIS<br/>
		 *   Reads the JSON message and updates the GameState<br/><br/>
		 * 
		 * - DISCONNECTED<br/>
		 *   The server has closed the connection.<br/>
		 *   The socket connection to the server is stopped and<br/>
		 *   the message in the ConnectionManager are deleted.<br/><br/>
		 *   
		 * - FAILURE<br/>
		 *   During the connection setup or there are communication problems<br/>
		 *   e.g. Wireless connection lost, server is no longer available.<br/>
		 *   There is an ErrorDialog is displayed to inform the user about it.<br/>
		 *   In addition, the socket connection is terminated and<br/>
		 *   deleted the messages from the Connection Manager. 
		 */
		@Override
		public void handleMessage(Message msg) {
			if (D)
				Log.d(TAG, "Handler msg.what: " + msg.what);
			stopProgressDialog();

			switch (msg.what) {
			
			case CONNECTED:
				String activity = LEDWallActivity.this.getLocalClassName();
				if (activity.equals("MainActivity")) {
					startLEDWallActivity(MenuActivity.class);
				}// if
				break;
				
			case TETRIS:
				if (ledActivity != null) {
					TetrisActivity tetrisActivity = (TetrisActivity) ledActivity.get();
					String temp = msg.getData().getString(JSON);
					Log.d(TAG, "Handler TETRIS: "+temp);
					tetrisActivity.updateGameState(temp);
				}// if
				break;
				
			case DISCONNECTED:
				startLEDWallActivity(MainActivity.class);
				connectionManager.stopConnection();
				connectionManager.clearMessages();
				break;
				
			case FAILURE:
				String error = "";
				if (ledActivity != null) {
					error = ledActivity.get().getResources()
							.getString(R.string.connection_error);
				}// if
				createErrorDialog(error);
				connectionManager.stopConnection();
				connectionManager.clearMessages();
				break;
			}// switch case

		}// handleMessage
	}// inner class LEDWallHandler
}// class LEDWallActivity
