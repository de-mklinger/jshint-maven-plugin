package com.cj.jshintmojo.reporter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.cache.Results;
import com.cj.jshintmojo.jshint.JSHint.Hint;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class LogReporter implements JSHintReporter {
    private static final String NL = "\n";
    private Log log;

    public LogReporter(Log log) {
        this.log = log;
    }     

    @Override
    public void report(Results results) throws MojoExecutionException {
        if (results.getNumberOfHints() == 0) {
            // be quiet
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Result result : results.getResultsSorted()) {
            if (result.hints.isEmpty()) {
                continue;
            }
            sb.append(result.path);
            sb.append(NL);
            for (Hint hint : result.hints) {
                sb.append("   ");
                sb.append(hint.severity);
                sb.append(" at ");
                sb.append(hint.line.intValue());
                sb.append(',');
                sb.append(hint.character.intValue());
                sb.append(": ");
                sb.append(hint.reason);
                sb.append(" \t(");
                sb.append(hint.code);
                sb.append(")");
                sb.append(NL);
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
