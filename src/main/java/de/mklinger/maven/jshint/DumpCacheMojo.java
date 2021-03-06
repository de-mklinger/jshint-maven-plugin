package de.mklinger.maven.jshint;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.util.Util;

/**
 * @goal dumpCache
 */
public class DumpCacheMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${basedir}
	 * @readonly
	 * @required
	 */
	private File basedir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			final File cachePath = new File(basedir, "target/lint.cache");

			final Map<String, Result> entries;
			if (cachePath.exists()) {
				entries = Util.readObject(cachePath);
			} else {
				entries = new HashMap<String, Result>();
			}

			for (final Result r : entries.values()) {
				final String status = r.hints.isEmpty() ? "[OK]" : "[BAD]";
				System.out.println(status + " " + r.path);
			}

		} catch (final Exception e) {
			throw new MojoExecutionException("Something bad happened", e);
		}
	}
}
