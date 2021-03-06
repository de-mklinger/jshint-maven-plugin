package de.mklinger.maven.jshint.reporter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.Hint;

/**
 * CheckStyle style xml reporter class.
 */
public class CheckStyleReporter extends BaseXmlFileReporter {
	/**
	 * format type of this reporter.
	 */
	public static final String FORMAT = "checkstyle";

	public CheckStyleReporter(final File outputFile) {
		super(outputFile);
	}

	@Override
	protected void writeXmlBody(final Writer writer, final Results results) throws IOException {
		writer.write("<checkstyle version=\"4.3\">\n");
		for (final Result result : results.getResultsSorted()) {
			writer.write("\t<file name=\"" + result.path + "\">\n");
			for (final Hint hint : result.hints) {
				writer.write(String.format("\t\t<" + severity(hint) + " line=\"%d\" column=\"%d\" message=\"%s\" source=\"jshint.%s\" severity=\"%s\" />\n",
						hint.getLine().intValue(), hint.getCharacter().intValue(), encode(hint.getReason()), encode(hint.getCode()), severity(hint)));
			}
			writer.write("\t</file>\n");
		}
		writer.write("</checkstyle>\n");
	}

	private String severity(final Hint hint) {
		return hint.getSeverity().name().toLowerCase();
	}
}
