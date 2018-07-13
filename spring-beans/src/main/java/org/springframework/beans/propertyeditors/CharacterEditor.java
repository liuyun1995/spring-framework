package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class CharacterEditor extends PropertyEditorSupport {

	/**
	 * The prefix that identifies a string as being a Unicode character sequence.
	 */
	private static final String UNICODE_PREFIX = "\\u";

	/**
	 * The length of a Unicode character sequence.
	 */
	private static final int UNICODE_LENGTH = 6;

	private final boolean allowEmpty;

	/**
	 * Create a new CharacterEditor instance.
	 * <p>
	 * The "allowEmpty" parameter controls whether an empty String is to be allowed
	 * in parsing, i.e. be interpreted as the {@code null} value when
	 * {@link #setAsText(String) text is being converted}. If {@code false}, an
	 * {@link IllegalArgumentException} will be thrown at that time.
	 * 
	 * @param allowEmpty
	 *            if empty strings are to be allowed
	 */
	public CharacterEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasLength(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else if (text == null) {
			throw new IllegalArgumentException("null String cannot be converted to char type");
		} else if (isUnicodeCharacterSequence(text)) {
			setAsUnicode(text);
		} else if (text.length() == 1) {
			setValue(Character.valueOf(text.charAt(0)));
		} else {
			throw new IllegalArgumentException("String [" + text + "] with length " + text.length()
					+ " cannot be converted to char type: neither Unicode nor single character");
		}
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

	private boolean isUnicodeCharacterSequence(String sequence) {
		return (sequence.startsWith(UNICODE_PREFIX) && sequence.length() == UNICODE_LENGTH);
	}

	private void setAsUnicode(String text) {
		int code = Integer.parseInt(text.substring(UNICODE_PREFIX.length()), 16);
		setValue(Character.valueOf((char) code));
	}

}
