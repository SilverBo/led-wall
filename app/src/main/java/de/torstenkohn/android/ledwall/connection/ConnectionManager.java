package de.torstenkohn.android.ledwall.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

import android.util.Log;

/**
 * The class stores the Socket, BufferedReader and BufferedWriter<br/>
 * and manages a LinkedList to manage the messages.<br/> 
 * The class implements the Singelton pattern to avoid<br/>
 * that there are several instances of this class.
 * 
 * @author Torsten Kohn
 * @since 08.05.2013
 *
 */
public class ConnectionManager {

	
	/**
	 * instance of ConnectionManager (Singleton)
	 */
	private static ConnectionManager instance;
	
	
	/**
	 * The class name is shown as TAG in the LogCat output
	 */
	private static final String TAG = "ConnectionManager";
	
	
	/**
	 * To debug the class, change boolean D to true. So you could see messages
	 * on LogCat
	 */
	private static final boolean D = false;
	
	
	private String ipaddress;
	private int port;
	private int udid;
	
	
	/**
	 * There is a thread-safe LinkedBlockingDeque used<br/>
	 * to prevent two threads access the list at an inopportune moment.
	 */
	private static LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<String>();

	private boolean isConnected;
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	
	/**
	 * private constructor to implement the singleton pattern
	 */
	private ConnectionManager() {
	}// constructor

	
	/**
	 * The method returns a instance of the class.
	 * @return of type ConnectionManager, only one Object of the class ConnectionManager
	 */
	public static ConnectionManager getInstance() {
		if (instance == null) {
			instance = new ConnectionManager();
		}// if
		return instance;
	}// getInstance

	
	// START getter & setter
	
	public String getIpaddress() {
		return ipaddress;
	}// getIpaddress

	
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}// setIpaddress

	
	public int getPort() {
		return port;
	}// getPort

	
	public void setPort(int port) {
		this.port = port;
	}// setPort

	
	public int getUdid() {
		return udid;
	}// getUdid

	
	public void setUdid(int udid) {
		this.udid = udid;
	}// setUdid

	
	public Socket getSocket() {
		return socket;
	}// getSocket

	
	public BufferedReader getIn() {
		return in;
	}// getIn

	
	public BufferedWriter getOut() {
		return out;
	}// getOut

	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}// setSocket

	
	public void setIn(BufferedReader in) {
		this.in = in;
	}// setIn

	
	public void setOut(BufferedWriter out) {
		this.out = out;
	}// setOut
	
	
	public void setConnected(boolean isConnected){
		this.isConnected = isConnected;
	}// setConnected
	
	
	public boolean isConnected() {
		return isConnected;
	}// isConnected
	
	// END getter & setter
	
	/**
	 * The method adds a message to the list
	 * @param message of type String, the message for the server
	 */
	public void addMessage(String message) {
		messages.offer(message);
	}// addMessage

	
	/**
	 * The method checks the list if there are entries
	 * @return of type boolean, true -> list is not empty | false -> list is empty
	 */
	public boolean hasMessage(){
		return !messages.isEmpty();
	}// hasMessage
	
	
	/**
	 * The method returns the first item from the list<br/>
	 * if the list is empty, an empty string ("") is returned.
	 * @return of type String, the message for the server. "" for an empty list or the JSON String
	 */
	public String getMessage() {

		if (messages.isEmpty()) {
			return "";
		} else {
			return messages.poll();
		}// if

	}// getMessage

	
	/**
	 * Deletes all messages from the list
	 */
	public void clearMessages() {
		messages.clear();
	}// clearMessages

	
	/**
	 * This method closes the socket connection to the server.<br/>
	 * It creates a thread, possibly because the method is blocked by the BufferedReader.
	 */
	public void stopConnection() {
		if(D)
		Log.d(TAG, "stopConnection");
		isConnected = false;
		if (socket != null && socket.isConnected()) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					
					/*
					 * The Thread.sleep is to ensure that the server
					 * receives the DISCONNECT function and
					 * the connection is not closed before.
					 */
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// nothing to do, because there is no fault
					}// try
					
					
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}//try
					Log.d(TAG, "stopConnection");
					socket= null;
				}// run
				
			});
			thread.start();
		}// if
	}// stopConnection
	
}// class ConnectionManager
