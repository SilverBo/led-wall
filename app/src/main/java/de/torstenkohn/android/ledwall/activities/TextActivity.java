package de.torstenkohn.android.ledwall.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import de.torstenkohn.android.ledwall.R;
import de.torstenkohn.android.ledwall.core.LEDWallActivity;

/**
 * The activity serves text messages to send<br/>
 * to the server to display this on the LED wall.<br/>
 * The texts can be only five characters long and<br/>
 * consist only of the 26 letters of the alphabet.
 * 
 * @author Torsten Kohn
 * @since 20.04.2013
 * 
 */
public class TextActivity extends LEDWallActivity {

	private EditText textTop;
	private EditText textBottom;

	/**
	 * The color in which the text is to be displayed.<br/>
	 * Default value is red.
	 */
	private String colorString = "red";

	/**
	 * Variable for the animation of the text.<br/>
	 * The default value is none. Currently there are no animations for the
	 * text.
	 */
	private String animationString = "none";

	/**
	 * The method initializes the connection to the server and <br/>
	 * Calls the initGUI method to initialize the GUI.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_text);
		super.onCreate(savedInstanceState);
		initConnection();
		setActionbarTitle(R.string.menu_text);
		initGui();
	}// onCreate

	/**
	 * Initializes the spinner to have access to the colors menu.<br/>
	 * Initializes the input fields for the text.<br/>
	 * Currently the second input field is disabled.
	 */
	private void initGui() {

		textTop = (EditText) findViewById(R.id.text_editText_textTop);
		textBottom = (EditText) findViewById(R.id.text_editText_textBottom);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.color, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner color = (Spinner) findViewById(R.id.text_spinner_color);
		color.setAdapter(adapter);

		color.setOnItemSelectedListener(new OnItemSelectedListener() {

			/**
			 * With a choice the variable colorString is updated.
			 */
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				switch (pos) {
				case 0:
					colorString = "red";
					break;
				case 1:
					colorString = "green";
					break;
				case 2:
					colorString = "blue";
					break;
				case 3:
					colorString = "multicolored";
					break;
				}// switch
			}// onItemSelected

			/**
			 * not implemented, and is not used
			 */
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}// onNothingSelected
		});

		adapter = ArrayAdapter.createFromResource(this, R.array.animation,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		/**
		 * The spinner for the animation is not fully implemented,<br/>
		 * because it is not used.
		 */
		Spinner animation = (Spinner) findViewById(R.id.text_spinner_animation);
		animation.setAdapter(adapter);

		((Button) findViewById(R.id.text_button_send))
				.setOnClickListener(new OnClickListener() {

					/**
					 * If the button SEND is pressed, the input fields are checked.
					 * As soon as the input is okay, the function TEXT<br/>
					 * message is sent to the server.
					 */
					@Override
					public void onClick(View v) {
						boolean isInverted = ((CheckBox) findViewById(R.id.text_checkBox_colorInverted))
								.isChecked();

						String top = textTop.getText().toString().trim();
						String bottom = textBottom.getText().toString().trim();

						if (isTextOk(top, bottom)) {
							String message = getLedWallMessage()
									.getFunctionText(top, bottom, colorString,
											isInverted, animationString);
							getConnectionManager().addMessage(message);
							if (!getConnectionManager().isConnected()) {
								getService().startService();
							}// if
						} else {
							textTop.setError(getApplicationContext()
									.getResources()
									.getText(R.string.text_error));
						}// if
					}// onClick

					/**
					 * The method checks if a entry is made
					 * @param top of type String, the first input field
					 * @param bottom of Type String, the second input field
					 * @return of type boolean, true -> input okay | false -> no input
					 */
					private boolean isTextOk(String top, String bottom) {
						if ((top.length() + bottom.length()) > 0) {
							return true;
						}// if
						return false;
					}// isTextOk
				});
	}// initGui
	
}// class TextActivity
