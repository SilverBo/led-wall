package de.torstenkohn.android.ledwall.activities;

import java.util.Date;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;

/**
 * The activity serves a 10x16 field of ImageViews so you can draw on the LED Wall.<br/>
 * You can control each LED individually with this activity.<br/>
 * The activity also provides a method to customize that layout.
 * 
 * @author Torsten Kohn
 * @since 25.08.2013
 * 
 */
public class DrawActivity extends LEDWallActivity implements OnClickListener {

	private static final int LEDWALL_WIDTH = 10;
	private static final int LEDWALL_HEIGHT = 16;
	private static final int DELAY = 250;
	private static final int NUMBER_OF_LED = 160;
	private static final int PIXEL_NONE = 0;
	private static final int PIXEL_RED = 1;
	private static final int PIXEL_GREEN = 2;
	private static final int PIXEL_BLUE = 3;
	
	
	/**
	 * The integer array representing the Wall LED as a one-dimensional array.<br/>
	 * Each position can assume the value 0 (none), 1 (red), 2 (green) or 3 (blue)
	 */
	private int[] matrix;
	
	
	/**
	 * The variable contains the time at which the last change was sent to the server
	 */
	private long lastSend;

	
	/**
	 * The method initializes the connection to the server<br/>
	 * and calls the method to initialize the GUI.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_draw);
		super.onCreate(savedInstanceState);
		initConnection();
		setActionbarTitle(R.string.menu_draw);

		initGUI();
	}// onCreate

	
	/**
	 * The method extends the LinearLayout for a 10x16 array of ImageView elements.<br/>
	 * This field represents the Wall LED<br/>
	 * and an OnClickListener to each field is set.<br/>
	 * Through the OnClickListener each field can be changed by clicking.
	 */
	private void initGUI() {

		matrix = new int[NUMBER_OF_LED];
		LinearLayout layout = (LinearLayout) findViewById(R.id.draw_layout);
		LinearLayout tempLayout;
		ImageView image;

		/*
		 * initialized the array with PIXEL_NONE
		 */
		for (int i = 0; i < matrix.length; i++) { // create 160 Pixel
			matrix[i] = PIXEL_NONE;
		}// for

		
		for (int i = 0; i < LEDWALL_HEIGHT; i++) {
			
			// generated for each row a new LinearLayout
			tempLayout = new LinearLayout(this);
			tempLayout.setHorizontalGravity(Gravity.CENTER);
			
			for (int j = i * LEDWALL_WIDTH; j < (i * LEDWALL_WIDTH) + LEDWALL_WIDTH; j++) {
				
				// generated for each row LEDWALL_WIDTH (10) ImageView elements
				image = new ImageView(this);
				image.setImageResource(getResId(matrix[j]));
				image.setId(j);
				image.setBackgroundColor(Color.BLACK);
				image.setOnClickListener(this);
				tempLayout.addView(image);
			}// for

			layout.addView(tempLayout);

		}// for
	}// initGUI

	
	/**
	 * The method converts the integer value of the integer array (0, 1, 2, 3)<br/>
	 * into the resource id, and returns it.
	 * @param pixelValue of type Integer, 0=none 1=red 2=green 3=ble 
	 * @return of type Integer, the resource id for the image
	 */
	private int getResId(int pixelValue) {
		
		int result = 0;
		switch (pixelValue) {
		case PIXEL_NONE:
			result = R.drawable.pixel_none;
			break;
		case PIXEL_RED:
			result = R.drawable.pixel_red;
			break;
		case PIXEL_GREEN:
			result = R.drawable.pixel_green;
			break;
		case PIXEL_BLUE:
			result = R.drawable.pixel_blue;
			break;
		}// switch
		return result;
	}// getResId

	
	/**
	 * Each ImageView has registered an OnClickListener that calls the onClick method.<br/>
	 * If an item is clicked, the onClick method is invoked.<br/>
	 * The method changes the ImageView like this:<br/>
	 *     0 (none) -> 1 (red) -> 2 (green) -> 3 (blue) -> 0 (none)
	 */
	@Override
	public void onClick(View v) {
		int value = matrix[v.getId()]; // v.getId() reflects the positions in the array 
		value++;

		if (value > PIXEL_BLUE) {
			value = 0;
		}// if

		matrix[v.getId()] = value;
		((ImageView) v).setImageResource(getResId(matrix[v.getId()]));

		
		/*
		 * The modified array is sent to the server only
		 * when enough time between the last message is.
		 */
		long currTime = new Date().getTime();
		long timeDifference = currTime - lastSend;

		if (timeDifference > DELAY) {
			lastSend = currTime;
			getConnectionManager().addMessage(
					getLedWallMessage().getFunctionDraw(matrix));
		}// if
	}// onClick

}// class DrawActivity
