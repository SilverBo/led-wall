package de.torstenkohn.android.ledwall.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;
import de.torstenkohn.android.ledwall.core.LEDWallMessage;

/**
 * The class uses the socket connection from the ConnectionManager<br/>
 * and enables the exchange of messages between the smartphone and the server.<br/>
 * Communication is outsourced to a thread to prevent the GUI thread is blocked.
 * 
 * @author Torsten Kohn
 * @since 08.05.2013
 *
 */
public class LEDWallService {

	/**
	 * The buffersize for the incoming messages
	 */
	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * The socket timeout in ms
	 */
	private static final int TIMEOUT_SOCKET = 2500;
	
	/**
	 * To debug the class, change boolean D to true. So you could see messages
	 * on LogCat
	 */
	private final boolean D = true;
	
	/**
	 * The class name is shown as TAG in the LogCat output
	 */
	private final String TAG;

	/**
	 * thread counter for debugging
	 */
	private static int threadCounter = 1;

	
	/**
	 * The handler object to send messages between activity and LEDWallSerice
	 */
	private final Handler handler;
	
	
	private ConnectionManager connectionManager;
	private ServiceThread thread;
	private LEDWallMessage ledWallMessage;

	
	/**
	 * Constructor for the LEDWallService
	 * @param handler of type Handler, the handler for the communication between Activity and thread
	 * @param connectionManager of type ConnectionManager, the class for the communication between Smartphone and server
	 * @param TAG of type String, for debugging informations
	 */
	public LEDWallService(Handler handler, ConnectionManager connectionManager,
			String TAG) {
		this.handler = handler;
		this.TAG = TAG + " " + getClass().getSimpleName();
		this.connectionManager = connectionManager;
		ledWallMessage = new LEDWallMessage();
	}// constructor

	
	/**
	 * Creates and starts a new thread for the communication
	 */
	public void startService() {
		if (D)
			Log.d(TAG, "startService");
		thread = new ServiceThread();
		thread.start();
	}// startService

	
	/**
	 * Stops the threads for the communication
	 */
	public void stopService() {
		if (D)
			Log.d(TAG, "stopService");
		if (thread != null && thread.isAlive()) {
			thread.stopService();
		}// if
	}// stopService

	
	/**
	 * The service thread is used for communication between the smartphone and the server.<br/>
	 * Via the Connection Manager, the thread has access to the Socket, BufferedReader<br/>
	 * and BufferedWriter. The class initializes the connection to the server<br/>
	 * and sends messages to the server and receives messages.
	 * 
	 * @author Torsten Kohn
	 * @since 08.05.2013
	 *
	 */
	private class ServiceThread extends Thread {

		/**
		 * The class name is shown as TAG in the LogCat output
		 */
		private static final String TAG = "ServiceThread";
		
		
		/**
		 * The handler object to send messages between activity and LEDWallSerice
		 */
		private final Handler handler = LEDWallService.this.handler;
		
		
		/**
		 * the socket for the communication
		 */
		private Socket socket;
		

		/**
		 * BufferedReader as input stream
		 */
		private BufferedReader in;

		
		/**
		 * BufferedWriter as output stream
		 */
		private BufferedWriter out;
		
		
		private boolean isRunning;

		
		/**
		 * Constructor of the ServiceThread class.<br/>
		 * Gives the class name and the current threadCounter as name to the super constructor<br/>
		 * and sets the socket, BufferedReader and BufferedWriter.
		 */
		private ServiceThread() {
			super(TAG + "_" + threadCounter);
			synchronized (this) {
				threadCounter++;
			}// synchronized
			socket = connectionManager.getSocket();
			in = connectionManager.getIn();
			out = connectionManager.getOut();
		}// constructor

		/**
		 * The method initializes the connection to the server.<br/>
		 * After the CONNECT function is transmitted to the server and<br/>
		 * if it worked, the method jumps in the data exchange mode
		 */
		@Override
		public void run() {

			// 1. initializes and starts the connection to the server
			initConnection();

			if (socket != null && socket.isConnected()) {
				// 2. message connect
				startExchange();
			}// if

			if (socket != null && socket.isConnected()) {
				// 3. data exchange
				isRunning = true;
				runExchange();
			}// if
		}// run

		
		private void stopService() {
			isRunning = false;
		}// stopService

		
		/**
		 * The method creates a new socket and connects to the IP address and
		 * port for the server and sets the timeout. In addition the
		 * BufferedReader and BufferedWriter is initialized.
		 */
		private void initConnection() {
			if (D)
				Log.d(TAG, "initConnection");
			
			if (socket == null || !socket.isConnected()) {
				socket = new Socket();
				
				try {
					socket.connect(
							new InetSocketAddress(connectionManager
									.getIpaddress(), connectionManager
									.getPort()), TIMEOUT_SOCKET);
					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					out = new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "initConnection()", e);
					isRunning = false;
					connectionManager.setConnected(false);
					messageToActivity(LEDWallActivity.FAILURE, e.toString());
				}// try

				// connection is successful
				connectionManager.setIn(in);
				connectionManager.setOut(out);
				connectionManager.setSocket(socket);
			}// if
		}// initConnection

		
		/**
		 * The method sends over the BufferedWriter the CONNECT function to the server<br/>
		 * and waits for a response. If the server responds with status success,<br/>
		 * the connection is established successfully.<br/>
		 * Over the handler the activity is informed about the successful connection.
		 */
		private void startExchange() {
			if (D)
				Log.d(TAG, "startExchange");
			if ((socket != null && socket.isConnected() && !connectionManager
					.isConnected())) {
				if (connectionManager.hasMessage()) {
					write(connectionManager.getMessage());
					String message = read();

					if (D)
						Log.d(TAG, "startExchange - message: " + message);
					if (isSuccess(message)) {
						connectionManager.setConnected(true);
						messageToActivity(LEDWallActivity.CONNECTED, message);
					} else {
						connectionManager.setConnected(false);
						messageToActivity(LEDWallActivity.FAILURE, message);
					}// if
				}// if

			}// if
		}// startExchange

		
		/**
		 * Once the connection is established to the server<br/>
		 * and the CONNECT function is successful, the method is used for<br/>
		 * sending messages to the server and receive messages.
		 */
		private void runExchange() {
			if (D)
				Log.d(TAG, "runExchange");

			String messageFrom = "";

			if (socket != null || socket.isConnected()) {
				while (isRunning) {
					
					if (connectionManager.hasMessage()) {
						// Sent the message from the list of the ConnectionManager
						write(connectionManager.getMessage());
					}// if
					
					try {
						if (in.ready() && LEDWallService.this.TAG.contains("TetrisActivity")) {
							
							// receive messages
							messageFrom = read();
							
							String function = ledWallMessage.getFunction(messageFrom);
							
							/*
							 * The server sends only two variants of messages in the exchange mode. 
							 * On the one hand the DISCONNECT function can be sent and otherwise 
							 * the function TETRIS will be sent from the server.
							 */
							if(function.equals(LEDWallMessage.FUNC_DISCONNECT)){
								messageToActivity(LEDWallActivity.DISCONNECTED, messageFrom);
							}else if(function.equals(LEDWallMessage.FUNC_TETRIS)){
								messageToActivity(LEDWallActivity.TETRIS, messageFrom);
							}// if
							
							// is used to reduce the GarbageCollector
							Thread.sleep(50);
							
						}// if
					} catch (IOException e) {
						messageToActivity(LEDWallActivity.FAILURE, e.toString());
					} catch (InterruptedException e) {
						// nothing to do, because there is no fault
					}// try
				}// while
			}// if
		}// runExchange

		
		private boolean isSuccess(String message) {
			return ledWallMessage.readStatus(message);
		}// isSuccess

		
		/**
		 * The method reads the stream and returns the characters read as a
		 * string. The maximum length of the message is based on the
		 * BUFFER_SIZE.
		 * 
		 * @return of type String, the message from the server (JSON)
		 */
		private String read() {
			String message = "";
			if (socket != null && socket.isConnected()) {

				try {
					char[] buf = new char[BUFFER_SIZE];
					int readingCount = in.read(buf);

					if (buf != null && buf.length > 0 && buf[0] == '{') {
						message = new String(buf, 0, readingCount).trim();
					}// if
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "IOException: ", e);
				}// try
			}// if
			return message;
		}// method read

		
		/**
		 * Sends a string to the server via the BufferedWriter stream
		 * 
		 * @param message
		 *            of type String, the message to the server (JSON)
		 */
		private void write(String message) {
			if (socket != null && socket.isConnected()) {
				try {
					out.write(message);
					out.flush();
					Log.d(TAG, "write: " + message);
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "write()", e);
					isRunning = false;
					connectionManager.setConnected(false);
					messageToActivity(LEDWallActivity.FAILURE, e.toString());
				}// try
			}// if
		}// method write

		
		/**
		 * This method sends a message to the activity by the handler
		 * 
		 * @param what
		 *            of type Integer, the message key
		 * @param json
		 *            of type String, the message to the Activity
		 */
		private void messageToActivity(int what, String json) {
			Message msg = handler.obtainMessage(what);
			Bundle bundle = new Bundle();
			bundle.putString(LEDWallActivity.JSON, json);
			msg.setData(bundle);
			handler.sendMessage(msg);
		}// method messageToActivity

	}// inner class ServiceThread
}// class LEDWallService
