package org.fife.rsta.ac.java.custom;

import java.util.ArrayList;
import java.util.List;

// Somehow methods missed in org.fife.ui.autocomplete.Util
public class AutoCompleteUtils {

	public static boolean matchTextParts(List<String> enteredText, List<String> toBeMatched) {
		if (toBeMatched.size() < enteredText.size())
			return false;
		for (int i = 0; i < enteredText.size(); i++) {
			if (!toBeMatched.get(i).startsWith(enteredText.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static List<String> getTextParts(String text) {
		List<String> textParts = new ArrayList<String>();
		if (text == null || "".equals(text))
			return textParts;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (Character.isUpperCase(text.charAt(i))) {
				if (sb.length() > 0) {
					textParts.add(sb.toString());
					sb = new StringBuilder();
				}
			}
			sb.append(text.charAt(i));
		}
		if (sb.length() > 0)
			textParts.add(sb.toString());

		return textParts;
	}

}
