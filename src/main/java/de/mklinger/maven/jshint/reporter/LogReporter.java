package de.mklinger.maven.jshint.reporter;

import org.apache.maven.plugin.logging.Log;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.Hint;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class LogReporter implements JSHintReporter {
	private static final char NL = '\n';
	private final Log log;

	public LogReporter(final Log log) {
		this.log = log;
	}

	@Override
	public void report(final Results results) {
		if (results.getNumberOfHints() == 0) {
			// be quiet
			return;
		}
		final StringBuilder sb = new StringBuilder();
		for (final Result result : results.getResultsSorted()) {
			if (result.hints.isEmpty()) {
				continue;
			}
			sb.append(result.path);
			sb.append(NL);
			for (final Hint hint : result.hints) {
				sb.append("   ");
				sb.append(hint.getSeverity());
				sb.append(" at ");
				sb.append(hint.getLine().intValue());
				sb.append(',');
				sb.append(hint.getCharacter().intValue());
				sb.append(": ");
				sb.append(hint.getReason());
				sb.append(" \t(");
				sb.append(hint.getCode());
				sb.append(")");
				sb.append('\n');
			}
		}
		if (results.hasErrors()) {
			log.error(sb.toString());
		} else if (results.hasWarnings()) {
			log.warn(sb.toString());
		} else {
			log.info(sb.toString());
		}
	}
}
