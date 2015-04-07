package de.mklinger.maven.jshint.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.maven.plugin.MojoExecutionException;

import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.util.Util;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public abstract class BaseXmlFileReporter implements JSHintReporter {
	private final File outputFile;

	public BaseXmlFileReporter(final File outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void report(final Results results) throws MojoExecutionException {
		if (results == null) {
			return;
		}
		try {
			final File outputDirectory = outputFile.getParentFile();
			Util.mkdirs(outputDirectory);
			final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			try {
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				writeXmlBody(writer, results);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} catch (final IOException e) {
			throw new MojoExecutionException("Error creating report: " + outputFile.getAbsolutePath(), e);
		}
	}

	protected String encode(final String str) {
		if (str == null) {
			return "";
		}
		return str
				.replace("&", "&amp;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	protected abstract void writeXmlBody(Writer writer, Results results) throws IOException;
}
