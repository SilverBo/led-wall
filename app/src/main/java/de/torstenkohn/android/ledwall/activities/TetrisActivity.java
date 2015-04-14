package de.torstenkohn.android.ledwall.activities;

import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;
import de.torstenkohn.android.ledwall.core.LEDWallMessage;
import de.torstenkohn.android.ledwall.util.TetrisGameState;

public class TetrisActivity extends LEDWallActivity implements OnClickListener {

	/**
	 * DELAY is used so that the user does not have too many entries<br/>
	 * and the stone does not make so many changes in motion.
	 */
	private final int DELAY = 200; // in ms

	private TextView score;
	private TextView level;
	private ImageView nextStone;
	private ImageView currStone;
	private Button stop;
	private Button start;
	private ImageButton left;
	private ImageButton right;
	private ImageButton counterclock;
	private ImageButton clockwise;

	private TetrisGameState gameState;

	/**
	 * 
	 */
	private long lastMove;

	/**
	 * The method initializes the connection to the server<br/>
	 * At start of the activity, the function Tetris with the content init is
	 * sent<br/>
	 * to the server to initialize the game.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_tetris);
		super.onCreate(savedInstanceState);
		initConnection();
		setActionbarTitle(R.string.menu_tetris);
		initGUI();
		initTetris();
	}// onCreate

	private void initTetris() {
		if (getConnectionManager().isConnected()) {

			getConnectionManager().addMessage(
					getLedWallMessage().getFunctionTetris(
							LEDWallMessage.ACTION_INIT));
		}// if
	}// initTetris

	@Override
	protected void onStart() {
		super.onStart();
	}// onStart

	@Override
	protected void onStop() {
		getConnectionManager().addMessage(
				getLedWallMessage().getFunctionTetris(
						LEDWallMessage.ACTION_QUIT));
		changeButtonEnabled(false);
		super.onStop();
	}// onStop

	/**
	 * Initializes the buttons and text views for the Tetris game
	 */
	private void initGUI() {
		score = (TextView) findViewById(R.id.tetris_textView_score);
		score.setText("0");

		level = (TextView) findViewById(R.id.tetris_textView_level);
		level.setText("1");

		nextStone = (ImageView) findViewById(R.id.tetris_imageView_nextStone);
		currStone = (ImageView) findViewById(R.id.tetris_imageView_currentStone);

		stop = (Button) findViewById(R.id.tetris_button_stop);
		stop.setOnClickListener(this);

		start = (Button) findViewById(R.id.tetris_button_start);
		start.setOnClickListener(this);

		left = (ImageButton) findViewById(R.id.tetris_button_left);
		left.setOnClickListener(this);

		right = (ImageButton) findViewById(R.id.tetris_button_right);
		right.setOnClickListener(this);

		counterclock = (ImageButton) findViewById(R.id.tetris_button_counterclockwise);
		counterclock.setOnClickListener(this);

		clockwise = (ImageButton) findViewById(R.id.tetris_button_clockwise);
		clockwise.setOnClickListener(this);
		changeButtonEnabled(false);
	}// initGUI

	/**
	 * Each Button has registered an OnClickListener that calls the onClick
	 * method.<br/>
	 * If an item is clicked, the onClick method is invoked.<br/>
	 * Implements the control logic for the game control
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case (R.id.tetris_button_stop):
			getConnectionManager().addMessage(
					getLedWallMessage().getFunctionTetris(
							LEDWallMessage.ACTION_QUIT));
			changeButtonEnabled(false);
			start.setEnabled(true);
			break;

		case (R.id.tetris_button_start):
			if (gameState.getCurrStone() != 0 && gameState.getNextStone() != 0) {
				initTetris();
			}// if
			getConnectionManager().addMessage(
					getLedWallMessage().getFunctionTetris(
							LEDWallMessage.ACTION_START));
			changeButtonEnabled(true);
			break;

		}// switch

		long currTime = new Date().getTime();
		long timeDifference = currTime - lastMove;

		/*
		 * Not all control commands are transmitted to the server, so the server
		 * is not overloaded and that it is not too many change of movement at a
		 * time. 5 commands per second are possible
		 */
		if (timeDifference > DELAY) {
			lastMove = currTime;
			switch (v.getId()) {
			case (R.id.tetris_button_left):
				getConnectionManager().addMessage(
						getLedWallMessage().getFunctionTetris(
								LEDWallMessage.ACTION_LEFT));
				break;

			case (R.id.tetris_button_right):
				getConnectionManager().addMessage(
						getLedWallMessage().getFunctionTetris(
								LEDWallMessage.ACTION_RIGHT));
				break;

			case (R.id.tetris_button_counterclockwise):
				getConnectionManager().addMessage(
						getLedWallMessage().getFunctionTetris(
								LEDWallMessage.ACTION_COUNTERCLOCKWISE));
				break;

			case (R.id.tetris_button_clockwise):
				getConnectionManager().addMessage(
						getLedWallMessage().getFunctionTetris(
								LEDWallMessage.ACTION_CLOCKWISE));
				break;
			}// switch

		}// if
	}// onClick

	/**
	 * The method updates the game state of the message from the server
	 * 
	 * @param message
	 *            of type String, the JSON message with the current gamestate of
	 *            the server
	 */
	public void updateGameState(String message) {
		TetrisGameState newState = getLedWallMessage().getTetrisGameState(
				message);
		if (gameState == null || gameState.getNextStone() == 0) { // firstCall
			this.gameState = newState;
		} else {
			int nextCurrentStone = this.gameState.getNextStone();
			this.gameState = newState;
			newState.setCurrStone(nextCurrentStone);
		}// if
		updateGUI();
	}// updateGameState

	/**
	 * updated the games button,<br/>
	 * the level number, the score and the images for the stones
	 */
	private void updateGUI() {
		if (gameState.isRunning()) {
			changeButtonEnabled(true);
			start.setEnabled(false);
			// start.setText(R.string.tetris_button_pause);

		} else {
			changeButtonEnabled(false);
			start.setEnabled(true);
			// start.setText(R.string.tetris_button_start);
		}// if

		score.setText(gameState.getScore() + "");
		level.setText(gameState.getLevel() + "");
		changeStoneImage(nextStone, gameState.getNextStone());
		changeStoneImage(currStone, gameState.getCurrStone());
	}// updateGUI

	/**
	 * indicates that the game buttons are enabled or disabled
	 * 
	 * @param enabled
	 *            of type boolean, true -> buttons are enabled, false -> buttons
	 *            are disabled
	 */
	private void changeButtonEnabled(boolean enabled) {
		stop.setEnabled(enabled);
		left.setEnabled(enabled);
		right.setEnabled(enabled);
		counterclock.setEnabled(enabled);
		clockwise.setEnabled(enabled);
	}// changeButtonEnabled

	/**
	 * The method changes the image of the ImageView to the specified stone.
	 * 
	 * @param image
	 *            of type ImageView, the image that needs to be changed.
	 * @param stone
	 *            of type Integer, the stone which serves as a new template for
	 *            the image.
	 */
	private void changeStoneImage(ImageView image, int stone) {
		switch (stone) {
		case TetrisGameState.STONE_I:
			image.setImageResource(R.drawable.tetris_i);
			break;
		case TetrisGameState.STONE_J:
			image.setImageResource(R.drawable.tetris_j);
			break;
		case TetrisGameState.STONE_L:
			image.setImageResource(R.drawable.tetris_l);
			break;
		case TetrisGameState.STONE_O:
			image.setImageResource(R.drawable.tetris_o);
			break;
		case TetrisGameState.STONE_S:
			image.setImageResource(R.drawable.tetris_s);
			break;
		case TetrisGameState.STONE_T:
			image.setImageResource(R.drawable.tetris_t);
			break;
		case TetrisGameState.STONE_Z:
			image.setImageResource(R.drawable.tetris_z);
			break;
		}// switch
	}// changeStoneImage

}// class TetrisActivity
