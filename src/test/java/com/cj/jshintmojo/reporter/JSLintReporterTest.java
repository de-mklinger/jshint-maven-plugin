package com.cj.jshintmojo.reporter;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.cj.jshintmojo.cache.Results;

/**
 * Test cases of {@link JSLintReporter}.
 */
public class JSLintReporterTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void formatType() {
        assertEquals("jslint", JSLintReporter.FORMAT);
    }

    @Test
    public void reportNullResults() throws MojoExecutionException, IOException {
        String report = report(null);
        assertEquals("", report);
    }

    @Test
    public void reportAllPassedResults() throws MojoExecutionException, IOException {
        String report = report(
                JSHintReporterTestUtil.createAllPassedResults());
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jslint>\n" +
                "</jslint>\n", report);
    }

    @Test
    public void reportSingleFileFailedResults() throws MojoExecutionException, IOException {
        String report = report(
                JSHintReporterTestUtil.createSingleFileFailedResults());
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jslint>\n" +
                "\t<file name=\"/path/to/A.js\">\n" +
                "\t\t<issue line=\"5\" char=\"2\" reason=\"Missing semicolon.\" evidence=\"}\" severity=\"W\" />\n" +
                "\t\t<issue line=\"12\" char=\"5\" reason=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" evidence=\"    &amp;&amp; window.setImmediate;\" severity=\"W\" />\n" +
                "\t\t<issue line=\"1137\" char=\"26\" reason=\"Too many hints.\" evidence=\"\" severity=\"E\" />\n" +
                "\t</file>\n" +
                "</jslint>\n", report);
    }

    @Test
    public void reportMultipleFilesFailedResults() throws MojoExecutionException, IOException {
        String report = report(
                JSHintReporterTestUtil.createMultipleFilesFailedResults());
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jslint>\n" +
                "\t<file name=\"/path/to/A.js\">\n" +
                "\t\t<issue line=\"5\" char=\"2\" reason=\"Missing semicolon.\" evidence=\"}\" severity=\"W\" />\n" +
                "\t\t<issue line=\"12\" char=\"5\" reason=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" evidence=\"    &amp;&amp; window.setImmediate;\" severity=\"W\" />\n" +
                "\t\t<issue line=\"1137\" char=\"26\" reason=\"Too many hints.\" evidence=\"\" severity=\"E\" />\n" +
                "\t</file>\n" +
                "\t<file name=\"/path/to/B.js\">\n" +
                "\t\t<issue line=\"10\" char=\"5\" reason=\"Comma warnings can be turned off with &apos;laxcomma&apos;.\" evidence=\"    , [info, &quot;info&quot;]\" severity=\"I\" />\n" +
                "\t</file>\n" +
                "\t<file name=\"/path/to/C.js\">\n" +
                "\t\t<issue line=\"3\" char=\"14\" reason=\"&apos;args&apos; is already defined.\" evidence=\"    var args = a;\" severity=\"W\" />\n" +
                "\t\t<issue line=\"12\" char=\"5\" reason=\"Use &apos;===&apos; to compare with &apos;0&apos;.\" evidence=\"    if (list.length == 0)\" severity=\"W\" />\n" +
                "\t</file>\n" +
                "</jslint>\n", report);
    }

    private String report(Results results) throws IOException, MojoExecutionException {
        File file = tmp.newFile();
        new JSLintReporter(file).report(results);
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
