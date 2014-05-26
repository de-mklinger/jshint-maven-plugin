package de.mklinger.maven.jshint.reporter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.JSHint.Hint;

/**
 * CheckStyle style xml reporter class.
 */
public class CheckStyleReporter extends BaseXmlFileReporter {
    /**
     * format type of this reporter.
     */
    public static final String FORMAT = "checkstyle";

    public CheckStyleReporter(File outputFile) {
        super(outputFile);
    }
    
    @Override
    protected void writeXmlBody(Writer writer, Results results) throws IOException {
        writer.write("<checkstyle version=\"4.3\">\n");
        for (Result result : results.getResultsSorted()) {
            writer.write("\t<file name=\"" + result.path + "\">\n");
            for (Hint hint : result.hints) {
                writer.write(String.format("\t\t<" + severity(hint) + " line=\"%d\" column=\"%d\" message=\"%s\" source=\"jshint.%s\" severity=\"%s\" />\n",
                        hint.line.intValue(), hint.character.intValue(), encode(hint.reason), encode(hint.code), severity(hint)));
            }
            writer.write("\t</file>\n");
        }
        writer.write("</checkstyle>\n");
    }
    
    private String severity(Hint hint) {
        return hint.severity.name().toLowerCase();
    }
}
