package de.mklinger.maven.jshint.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class Results implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, Result> resultsByFile = new HashMap<String, Result>();
	private boolean hasErrors;
	private boolean hasWarnings;
	private int numberOfHints;

	public void add(final Result result) {
		resultsByFile.put(result.path, result);
		if (result.hasErrors) {
			hasErrors = true;
		}
		if (result.hasWarnings) {
			hasWarnings = true;
		}
		numberOfHints += result.hints.size();
	}

	public Result getResult(final String file) {
		return resultsByFile.get(file);
	}

	public List<Result> getResultsSorted() {
		final List<Result> results = new ArrayList<Result>(resultsByFile.size());
		results.addAll(resultsByFile.values());
		Collections.sort(results);
		return results;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public boolean hasWarnings() {
		return hasWarnings;
	}

	public int getNumberOfHints() {
		return numberOfHints;
	}
}
