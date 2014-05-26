package de.mklinger.maven.jshint.reporter;

import org.apache.maven.plugin.MojoExecutionException;

import de.mklinger.maven.jshint.cache.Results;

/**
 * A interface for JSHint reporting class.
 */
public interface JSHintReporter {
    /**
     * Created the lint report. 
     * 
     * @param results lint results to report.
     * @throws MojoExecutionException In case of an error
     */
    public void report(Results results) throws MojoExecutionException;
}
