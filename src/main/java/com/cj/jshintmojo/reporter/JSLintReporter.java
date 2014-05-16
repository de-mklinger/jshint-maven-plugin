package com.cj.jshintmojo.reporter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.cache.Results;
import com.cj.jshintmojo.jshint.JSHint.Hint;

/**
 * JSLint style xml reporter class.
 */
public class JSLintReporter extends BaseXmlFileReporter {
    /**
     * format type of this reporter.
     */
    public static final String FORMAT = "jslint";

    public JSLintReporter(File reportFile) {
        super(reportFile);
    }

    @Override
    protected void writeXmlBody(Writer writer, Results results) throws IOException {
        writer.write("<jslint>\n");
        for (Result result : results.getResultsSorted()) {
            writer.write("\t<file name=\"" + result.path + "\">\n");
            for (Hint hint : result.hints) {
                writer.write(String.format("\t\t<issue line=\"%d\" char=\"%d\" reason=\"%s\" evidence=\"%s\" ",
                        hint.line.intValue(), hint.character.intValue(), encode(hint.reason), encode(hint.evidence)));
                writer.write("severity=\"" + hint.severity.toString().charAt(0) + "\" ");
                writer.write("/>\n");
            }
            writer.write("\t</file>\n");
        }
        writer.write("</jslint>\n");
    }
}
