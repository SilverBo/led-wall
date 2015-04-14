package de.torstenkohn.android.ledwall.util;

/**
 * The class stores the state of the Tetris game.<br/>
 * Is used only in the Activity TetrisActivity and LEDWallMessage.
 * @author Torsten Kohn
 * @since 05.05.2013
 */
public class TetrisGameState {

	/*
	 * Represents all the Tetriminos (Stones) as integer
	 */
	public static final int STONE_I = 10;
	public static final int STONE_J = 20;
	public static final int STONE_L = 30;
	public static final int STONE_O = 40;
	public static final int STONE_S = 50;
	public static final int STONE_T = 60;
	public static final int STONE_Z = 70;

	/*
	 * Represents the visible game information.
	 */
	private int score;
	private int level;
	private int nextStone;
	private int currStone;
	
	/**
	 * Stores whether the game is running at the time<br/> 
	 * or if it is paused or stopped
	 */
	private boolean isRunning;

	/**
	 * The constructor of the class
	 * @param score of type Integer, the current score
	 * @param level of type Integer, the current level
	 * @param isRunning of type boolean, the current game state
	 * @param nextStone of type String, the next stone
	 */
	public TetrisGameState(int score, int level, boolean isRunning,
			String nextStone) {
		this.score = score;
		this.level = level;
		this.isRunning = isRunning;
		this.nextStone = convertStone(nextStone);
	}// constructor

	public int getScore() {
		return score;
	}// getScore

	public int getLevel() {
		return level;
	}// getLevel

	public boolean isRunning() {
		return isRunning;
	}// isRunning

	public int getNextStone() {
		return nextStone;
	}// getNextStone

	public void setNextStone(int nextStone) {
		this.nextStone = nextStone;
	}// setNextStone

	public int getCurrStone() {
		return currStone;
	}// getCurrStone
	
	public void setCurrStone(int currStone) {
		this.currStone = currStone;
	}// setCurrStone

	/**
	 * The method converts the Tetriminos (Stone),<br/>
	 * which is sent by the server as a string value to an Integer
	 * @param stone of type String, the to be converted stone
	 * @return of type Integer, the stone as an Integer
	 */
	private int convertStone(String stone) {
		int result = 0;
		if (stone.equals("I")) {
			result = STONE_I;
		} else if (stone.equals("J")) {
			result = STONE_J;
		} else if (stone.equals("L")) {
			result = STONE_L;
		} else if (stone.equals("O")) {
			result = STONE_O;
		} else if (stone.equals("S")) {
			result = STONE_S;
		} else if (stone.equals("T")) {
			result = STONE_T;
		} else if (stone.equals("Z")) {
			result = STONE_Z;
		}// if
		return result;
	}// convertStone
}// class TetrisGameState
