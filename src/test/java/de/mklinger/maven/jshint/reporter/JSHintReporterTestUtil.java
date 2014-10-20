package de.mklinger.maven.jshint.reporter;

import java.util.ArrayList;
import java.util.List;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.Hint;

/**
 * Utility class to create test data for {@link JSHintReporter}.
 */
public abstract class JSHintReporterTestUtil {
	private JSHintReporterTestUtil() {
	}

	private static Hint createHint(final String code, final int line, final int character, final String evidence, final String reason) {
		return new Hint("(error)", code, reason, evidence, line, character, reason);
	}

	static Results createAllPassedResults() {
		return new Results();
	}

	static Results createSingleFileFailedResults() {
		final Results results = new Results();
		final List<Hint> hints = new ArrayList<Hint>();
		hints.add(createHint("W033", 5, 2, "}", "Missing semicolon."));
		hints.add(createHint("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
		hints.add(createHint("E043", 1137, 26, null, "Too many hints."));
		final Result result = new Result("/path/to/A.js", 1377309240000L, hints);
		results.add(result);
		return results;
	}

	static Results createMultipleFilesFailedResults() {
		final Results results = new Results();

		final List<Hint> hints1 = new ArrayList<Hint>();
		hints1.add(createHint("W033", 5, 2, "}", "Missing semicolon."));
		hints1.add(createHint("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
		hints1.add(createHint("E043", 1137, 26, null, "Too many hints."));
		results.add(new Result("/path/to/A.js", 1377309240000L, hints1));

		final List<Hint> hints2 = new ArrayList<Hint>();
		hints2.add(createHint("I001", 10, 5, "    , [info, \"info\"]", "Comma warnings can be turned off with 'laxcomma'."));
		results.add(new Result("/path/to/B.js", 1377309240000L, hints2));

		final List<Hint> hints3 = new ArrayList<Hint>();
		hints3.add(createHint("W004", 3, 14, "    var args = a;", "'args' is already defined."));
		hints3.add(createHint("W041", 12, 5, "    if (list.length == 0)", "Use '===' to compare with '0'."));
		results.add(new Result("/path/to/C.js", 1377309240000L, hints3));

		return results;
	}
}
