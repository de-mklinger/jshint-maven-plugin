package de.mklinger.maven.jshint.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

public class OptionsParser {
	private static final Pattern GLOBALS_PATTERN = Pattern.compile("\"globals\"\\s*:\\s*\\{(.*?)}", Pattern.DOTALL);

	/**
	 * @param withComments
	 *            input JSON string with optional comments
	 * @return input with comments removed (i.e. valid JSON)
	 */
	private static String removeComments(final String withComments) {
		final String withoutBlockComments = withComments.replaceAll("\\/\\*(?:(?!\\*\\/)[\\s\\S])*\\*\\/", "");

		// Everything after '//'
		final String withoutAllComments = withoutBlockComments.replaceAll("\\/\\/[^\\n\\r]*", "");

		return withoutAllComments;
	}

	/**
	 * @param jsonString
	 *            string wrapped in {} braces (assuming valid JSON, i.e. no text
	 *            outside the braces)
	 * @return string inside the {} braces
	 */
	private static String insideCurly(String jsonString) {
		jsonString = jsonString.trim();
		return jsonString.substring(1, jsonString.length() - 1);
	}

	/**
	 * @param configFileContentsBytes
	 *            JSON-like file contents
	 * @return set of JSHint options, excluding globals
	 */
	public static Set<String> extractOptions(final byte[] configFileContentsBytes) {
		final String configFileContents = new String(configFileContentsBytes);
		final String withoutComments = removeComments(configFileContents);
		final Matcher matcher = GLOBALS_PATTERN.matcher(withoutComments);
		final String optionsCsv = matcher.replaceAll("").replaceAll("\"", "").replaceAll("\\s", "");
		final String optionsBody = insideCurly(optionsCsv);

		final Set<String> options = new HashSet<String>();
		for (final String option : optionsBody.split(",")) {
			if (StringUtils.isBlank(option)) {
				continue;
			}
			options.add(option);
		}
		return options;

	}

	/**
	 * @param configFileContentsBytes
	 *            JSON-like file contents
	 * @return set of JSHint allowed globals
	 */
	public static Set<String> extractGlobals(final byte[] configFileContents) {
		final String withoutComments = removeComments(new String(configFileContents));
		final Matcher matcher = GLOBALS_PATTERN.matcher(withoutComments);
		matcher.find();
		final String globalsCsv = matcher.group(1).replaceAll("\\s", "").replaceAll("\"", "");

		final Set<String> globalsSet = new HashSet<String>();
		for (final String global : globalsCsv.split(",")) {
			globalsSet.add(global);
		}
		return globalsSet;
	}
}
