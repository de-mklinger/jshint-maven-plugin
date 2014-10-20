package de.mklinger.maven.jshint.reporter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.Hint;

/**
 * JSLint style xml reporter class.
 */
public class JSLintReporter extends BaseXmlFileReporter {
	/**
	 * format type of this reporter.
	 */
	public static final String FORMAT = "jslint";

	public JSLintReporter(final File reportFile) {
		super(reportFile);
	}

	@Override
	protected void writeXmlBody(final Writer writer, final Results results) throws IOException {
		writer.write("<jslint>\n");
		for (final Result result : results.getResultsSorted()) {
			writer.write("\t<file name=\"" + result.path + "\">\n");
			for (final Hint hint : result.hints) {
				writer.write(String.format("\t\t<issue line=\"%d\" char=\"%d\" reason=\"%s\" evidence=\"%s\" ",
						hint.getLine().intValue(), hint.getCharacter().intValue(), encode(hint.getReason()), encode(hint.getEvidence())));
				writer.write("severity=\"" + hint.getSeverity().toString().charAt(0) + "\" ");
				writer.write("/>\n");
			}
			writer.write("\t</file>\n");
		}
		writer.write("</jslint>\n");
	}
}
