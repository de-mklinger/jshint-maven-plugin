package de.mklinger.maven.jshint.reporter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.mklinger.maven.jshint.cache.Results;

/**
 * Test cases of {@link CheckStyleReporter}.
 */
public class CheckStyleReporterTest {
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void formatType() {
		assertEquals("checkstyle", CheckStyleReporter.FORMAT);
	}

	@Test
	public void reportNullResults() throws MojoExecutionException, IOException {
		final String report = report(null);
		assertEquals("", report);
	}

	@Test
	public void reportAllPassedResults() throws MojoExecutionException, IOException {
		final String report = report(
				JSHintReporterTestUtil.createAllPassedResults());
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<checkstyle version=\"4.3\">\n" +
						"</checkstyle>\n", report);
	}

	@Test
	public void reportSingleFileFailedResults() throws MojoExecutionException, IOException {
		final String report = report(
				JSHintReporterTestUtil.createSingleFileFailedResults());
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<checkstyle version=\"4.3\">\n" +
						"\t<file name=\"/path/to/A.js\">\n" +
						"\t\t<warning line=\"5\" column=\"2\" message=\"Missing semicolon.\" source=\"jshint.W033\" severity=\"warning\" />\n" +
						"\t\t<warning line=\"12\" column=\"5\" message=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" source=\"jshint.W014\" severity=\"warning\" />\n" +
						"\t\t<error line=\"1137\" column=\"26\" message=\"Too many hints.\" source=\"jshint.E043\" severity=\"error\" />\n" +
						"\t</file>\n" +
						"</checkstyle>\n", report);
	}

	@Test
	public void reportMultipleFilesFailedResults() throws MojoExecutionException, IOException {
		final String report = report(
				JSHintReporterTestUtil.createMultipleFilesFailedResults());
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<checkstyle version=\"4.3\">\n" +
						"\t<file name=\"/path/to/A.js\">\n" +
						"\t\t<warning line=\"5\" column=\"2\" message=\"Missing semicolon.\" source=\"jshint.W033\" severity=\"warning\" />\n" +
						"\t\t<warning line=\"12\" column=\"5\" message=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" source=\"jshint.W014\" severity=\"warning\" />\n" +
						"\t\t<error line=\"1137\" column=\"26\" message=\"Too many hints.\" source=\"jshint.E043\" severity=\"error\" />\n" +
						"\t</file>\n" +
						"\t<file name=\"/path/to/B.js\">\n" +
						"\t\t<info line=\"10\" column=\"5\" message=\"Comma warnings can be turned off with &apos;laxcomma&apos;.\" source=\"jshint.I001\" severity=\"info\" />\n" +
						"\t</file>\n" +
						"\t<file name=\"/path/to/C.js\">\n" +
						"\t\t<warning line=\"3\" column=\"14\" message=\"&apos;args&apos; is already defined.\" source=\"jshint.W004\" severity=\"warning\" />\n" +
						"\t\t<warning line=\"12\" column=\"5\" message=\"Use &apos;===&apos; to compare with &apos;0&apos;.\" source=\"jshint.W041\" severity=\"warning\" />\n" +
						"\t</file>\n" +
						"</checkstyle>\n", report);
	}

	private String report(final Results results) throws IOException, MojoExecutionException {
		final File file = tmp.newFile();
		new CheckStyleReporter(file).report(results);
		return FileUtils.readFileToString(file, "UTF-8");
	}
}
