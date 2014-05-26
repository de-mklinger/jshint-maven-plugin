package de.mklinger.maven.jshint.reporter;

import java.util.ArrayList;
import java.util.List;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.JSHint;
import de.mklinger.maven.jshint.jshint.JSHint.Hint;
import de.mklinger.maven.jshint.jshint.JSHint.HintSeverity;
import de.mklinger.maven.jshint.reporter.JSHintReporter;

/**
 * Utility class to create test data for {@link JSHintReporter}.
 */
class JSHintReporterTestUtil {

    private JSHintReporterTestUtil() {
    }

    private static JSHint.Hint createHint(final String code, final int line,
            final int character, final String evidence, final String reason)
    {
        JSHint.Hint error = new JSHint.Hint();
        error.id = "(error)";
        error.code = code;
        error.line = line;
        error.character = character;
        error.evidence = evidence;
        error.reason = reason;
        error.raw = reason;
        char c = code.charAt(0);
        switch (c) {
            case 'E':
                error.severity = HintSeverity.ERROR;
                break;
            case 'W':
                error.severity = HintSeverity.WARNING;
                break;
            case 'I':
                error.severity = HintSeverity.INFO;
                break;
            default:
                throw new IllegalArgumentException("Unexpected char c=" + c);
        }
        return error;
    }

    static Results createAllPassedResults() {
        return new Results();
    }

    static Results createSingleFileFailedResults() {
        Results results = new Results();
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createHint("W033", 5, 2, "}", "Missing semicolon."));
            hints.add(createHint("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            hints.add(createHint("E043", 1137, 26, null, "Too many hints."));
            Result result = new Result("/path/to/A.js", 1377309240000L, hints);
            results.add(result);
        }
        return results;
    }

    static Results createMultipleFilesFailedResults() {
        Results results = new Results();
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createHint("W033", 5, 2, "}", "Missing semicolon."));
            hints.add(createHint("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            hints.add(createHint("E043", 1137, 26, null, "Too many hints."));
            Result result = new Result("/path/to/A.js", 1377309240000L, hints);
            results.add(result);
        }
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createHint("I001", 10, 5, "    , [info, \"info\"]", "Comma warnings can be turned off with 'laxcomma'."));
            Result result = new Result("/path/to/B.js", 1377309240000L, hints);
            results.add(result);
        }
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createHint("W004", 3, 14, "    var args = a;", "'args' is already defined."));
            hints.add(createHint("W041", 12, 5, "    if (list.length == 0)", "Use '===' to compare with '0'."));
            Result result = new Result("/path/to/C.js", 1377309240000L, hints);
            results.add(result);
        }
        return results;
    }

}
