package dev.eposs.elementsutils.feature.xpFormat;

import java.util.regex.Pattern;

public class xpFormat {
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d{4,}");

	/**
	 * Formats all numbers with at least 4 digits in the input string by adding dots as thousands separators.
	 *
	 * @param input The input string possibly containing numbers.
	 * @return The input string with numbers formatted using dots as thousands separators.
	 */
	public static String formatNumbersWithDots(String input) {
		return NUMBER_PATTERN.matcher(input).replaceAll(matchResult -> {
			String digits = matchResult.group();
			try {
				long value = Long.parseLong(digits);
				return String.format("%,d", value).replace(',', '.');
			} catch (NumberFormatException e) {
				return digits;
			}
		});
	}
}
