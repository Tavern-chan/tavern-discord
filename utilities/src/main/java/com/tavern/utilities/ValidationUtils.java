package com.tavern.utilities;

public class ValidationUtils {

	public static void requireNonBlank(String value, String errorMessage) throws NullPointerException {
		if (StringUtils.isNullOrBlank(value)) {
			throw new NullPointerException(errorMessage);
		}
	}

}
