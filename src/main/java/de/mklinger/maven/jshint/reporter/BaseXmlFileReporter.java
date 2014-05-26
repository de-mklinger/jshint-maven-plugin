package de.mklinger.maven.jshint.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.maven.plugin.MojoExecutionException;

import de.mklinger.maven.jshint.cache.Results;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public abstract class BaseXmlFileReporter implements JSHintReporter {
    private File outputFile;

    public BaseXmlFileReporter(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void report(Results results) throws MojoExecutionException {
        if (results == null) {
            return;
        }
        try {
            File outputDirectory = outputFile.getParentFile();
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    throw new IOException("Error creating directory: " + outputDirectory.getAbsolutePath());
                }
            }
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            try {
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writeXmlBody(writer, results);
            } finally{
                if(writer != null){
                    writer.close();
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating report: " + outputFile.getAbsolutePath(), e);
        }
    }

    protected String encode(final String str) {
        if(str == null) {
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
