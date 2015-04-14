package de.torstenkohn.android.ledwall.util;

import java.util.regex.Pattern;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * IpTextInputWatcher checks when entering a ip-address in an EditText.<br/>
 * Allowed IP-addreses: 0.0.0.0 - 255.255.255.255<br/>
 * Quelle: http://stackoverflow.com/questions/3698034/validating-ip-in-android/11545229#11545229
 * 
 * @author Torsten Kohn
 * @since 02.11.2012
 */
public class IpInputWatcher implements TextWatcher {

	private String previousText = "";
	private static final Pattern PARTIAl_IP_ADDRESS = Pattern
			.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"
					+ "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");

	public void afterTextChanged(Editable s) {
		if (PARTIAl_IP_ADDRESS.matcher(s).matches()) {
			previousText = s.toString();
		} else {
			s.replace(0, s.length(), previousText);
		}// if
	}// method afterTextChanged

	/**
	 * This Method is not implement
	 */
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}// method beforeTextChanged

	/**
	 * This Method is not implement
	 */
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}// method onTextChanged

}// class IpTextInputWatcher
