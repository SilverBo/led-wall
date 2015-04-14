package de.torstenkohn.android.ledwall.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.torstenkohn.android.ledwall.connection.ConnectionManager;
import de.torstenkohn.android.ledwall.util.TetrisGameState;

/**
 * The class provides the interface between <br/>
 * the server messages and client messages. 
 * It implements the functions one by one in the document "API - Kommunikation" detained.<br/>
 * For more information on the methods please use the document.
 * @author Torsten Kohn
 * @since 05.05.2013
 */
public class LEDWallMessage {
	/**
	 * The variable "D" is used only for debugging purposes, to a central point
	 * of the app LogCat output to enable (true) or disable (false).
	 */
	private final static boolean D = true;

	/**
	 * The variable "TAG" is used as a tag for LogCat, this can identify the
	 * class. TAG should always contain the simple name of the class.
	 */
	private final String TAG;

	/*
	 * The different functions of the communication interface 
	 */
	public static final String FUNC_CONNECT = "connect";
	public static final String FUNC_BREAK = "break";
	public static final String FUNC_DISCONNECT = "disconnect";
	public static final String FUNC_TEXT = "text";
	public static final String FUNC_DRAW = "draw";
	public static final String FUNC_TETRIS = "tetris";

	/*
	 * Each keyword from the communication interface 
	 */
	private static final String KEY_FUNCTION = "function";
	private static final String KEY_STATUS = "status";
	private static final String KEY_UDID = "udid";
	private static final String KEY_TEXT_TOP = "text-top";
	private static final String KEY_TEXT_BOTTOM = "text-bottom";
	private static final String KEY_COLOR = "color";
	private static final String KEY_INVERTED = "inverted";
	private static final String KEY_ANIMATION = "animation";
	private static final String KEY_ACTION = "action";
	private static final String KEY_GAMESTATE = "gameState";
	private static final String KEY_SCORE = "score";
	private static final String KEY_LEVEL = "level";
	private static final String KEY_ISRUNNING = "isRunning";
	private static final String KEY_NEXTSTONE = "nextStone";
	private static final String KEY_DATA = "data";

	public static final String ACTION_INIT = "init";
	public static final String ACTION_START = "start";
	public static final String ACTION_QUIT = "quit";
	public static final String ACTION_LEFT = "L";
	public static final String ACTION_RIGHT = "R";
	public static final String ACTION_COUNTERCLOCKWISE = "CC";
	public static final String ACTION_CLOCKWISE = "C";
	
	private static final String STATUS_SUCCESS = "success";
	private static final String STATUS_FAILURE = "failure";

	/**
	 * Constructor of the class.<br/>
	 * Saves only the TAG for debugging output.
	 */
	public LEDWallMessage() {
		this.TAG = this.getClass().getSimpleName();
	}// constructor

	
	/*
	 * START methods for the communication 
	 */
	
	
	public String getFunctionConnect(String status) {
		ConnectionManager connMan = ConnectionManager.getInstance();
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_FUNCTION, FUNC_CONNECT);
			json.put(KEY_STATUS, status);
			json.put(KEY_UDID, connMan.getUdid());
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionConnect: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionConnect: " + json.toString());
		return json.toString();
	}// getFunctionConnect

	
	public String getFunctionBreak() {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_FUNCTION, FUNC_BREAK);
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionBreak: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionBreak: " + json.toString());
		return json.toString();
	}// getFunctionBreak

	
	public String getFunctionDisconnect() {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_FUNCTION, FUNC_DISCONNECT);
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionDisconnect: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionDisconnect: " + json.toString());
		return json.toString();
	}// getFunctionDisconnect

	
	public String getFunctionText(String top, String bottom, String color,
			boolean isInverted, String animation) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_FUNCTION, FUNC_TEXT);
			json.put(KEY_TEXT_TOP, top);
			json.put(KEY_TEXT_BOTTOM, bottom);
			json.put(KEY_COLOR, color);
			json.put(KEY_INVERTED, isInverted);
			json.put(KEY_ANIMATION, animation);
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionText: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionText: " + json.toString());
		return json.toString();
	}// getFunctionText

	
	public String getFunctionTetris(String action) {
		JSONObject json = new JSONObject();
		try {
			json.put(KEY_FUNCTION, FUNC_TETRIS);
			json.put(KEY_ACTION, action);
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionTetris: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionTetris: " + json.toString());
		return json.toString();
	}// getFunctionTetris
	
	
	public String getFunctionDraw(int[] data) {
		JSONObject json = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		try {
			json.put(KEY_FUNCTION, FUNC_DRAW);
			for (int i = 0; i < data.length; i++) {
				jsonArr.put(i, data[i]);
			}
			json.put(KEY_DATA, jsonArr);
		} catch (JSONException e) {
			Log.d(TAG, "getFunctionDraw: " + e);
		}// try
		if (D)
			Log.d(TAG, "getFunctionDraw: " + json.toString());
		return json.toString();
	}// getFunctionDraw

	
	/*
	 * END methods for the communication 
	 */
	
	
	/**
	 * The method reads the status from the JSON string <br/>
	 * and returns true if the status is success and false otherwise.
	 * @param message of type String, the JSON message
	 * @return of type boolean, success -> true | failure -> false
	 */
	public boolean readStatus(String message) {
		boolean result = false;
		try {
			JSONObject json = new JSONObject(message);
			if (json.getString(KEY_STATUS).equals(STATUS_SUCCESS)) {
				result = true;
			}// if
		} catch (JSONException e) {
			Log.d(TAG, "readStatus: " + e);
		}// try
		return result;
	}// readStatus

	
	/**
	 * The method reads the name of function from the JSON string and returns it.
	 * @param message of type String, the JSON message
	 * @return of type String, the function name
	 */
	public String getFunction(String message) {
		String function = "";
		try {
			JSONObject json = new JSONObject(message);
			function = json.getString(KEY_FUNCTION);
		} catch (JSONException e) {
			Log.d(TAG, "getFunction: " + e);
		}// try
		return function;
	}// getFunction
	
	
	/**
	 * The method reads the game state from a JSON message<br/>
	 * and created a TetrisGameState object and returns it.
	 * @param message of type String, the JSON message
	 * @return of type TetrisGameState, the current game state from the message
	 */
	public TetrisGameState getTetrisGameState(String message){
		TetrisGameState result = null;
		try {
			JSONObject json = new JSONObject(message);
			json = json.getJSONObject(KEY_GAMESTATE);
			int score = json.getInt(KEY_SCORE);
			int level = json.getInt(KEY_LEVEL);
			boolean isRunning = json.getBoolean(KEY_ISRUNNING);
			String nextStone = json.getString(KEY_NEXTSTONE);
			result = new TetrisGameState(score, level, isRunning, nextStone);
			
		} catch (JSONException e) {
			Log.d(TAG, "getTetrisGameState: " + e);
		}// try
		return result;
	}// getTetrisGameState

}// class LEDWallMessage
