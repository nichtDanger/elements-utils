package dev.eposs.elementsutils.feature.xpformat;

import java.util.regex.Pattern;

public class XpFormat {

	/**
	 * Formats all numbers with at least 4 digits in the input string by adding dots as thousands separators.
	 *
	 * @param input The input string possibly containing numbers.
	 * @return The input string with numbers formatted using dots as thousands separators.
	 */
	public static String formatNumbersWithDots(String input) {
		Pattern pattern = Pattern.compile("(?<![\\p{L}\\d])(\\d{4,})(?![\\p{L}\\d])", Pattern.UNICODE_CHARACTER_CLASS);
		return pattern.matcher(input).replaceAll(matchResult -> {
			String digits = matchResult.group(1);
			try {
				long value = Long.parseLong(digits);
				return String.format("%,d", value).replace(',', '.');
			} catch (NumberFormatException e) {
				return digits;
			}
		});
	}
}
