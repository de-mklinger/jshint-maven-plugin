package de.mklinger.maven.jshint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public final class Ignore {
	public static enum IgnoreType {
		EMPTY,
		IGNOREFILE,
		JSHINTIGNORE
	}

	private final List<File> excludes;
	private final File ignoreFile;

	private Ignore(final List<File> excludes, final File ignoreFile) {
		this.excludes = excludes;
		this.ignoreFile = ignoreFile;
	}

	public static Ignore loadIgnore(final String ignoreFileParam, final File basedir) throws IOException {
		if (StringUtils.isNotBlank(ignoreFileParam)) {
			final File ignoreFile = new File(basedir, ignoreFileParam);
			return processIgnoreFile(ignoreFile, basedir);
		}

		final File jshintignore = findJshintignore(basedir);
		if (jshintignore != null) {
			return processIgnoreFile(jshintignore, basedir);
		}

		return new Ignore(Collections.<File>emptyList(), null);
	}

	private static File findJshintignore(final File cwd) {
		File placeToLook = cwd;
		while (placeToLook.getParentFile() != null) {
			final File ignoreFile = new File(placeToLook, ".jshintignore");
			if (ignoreFile.exists()) {
				return ignoreFile;
			} else {
				placeToLook = placeToLook.getParentFile();
			}
		}

		return null;
	}

	/**
	 * Read contents of the specified ignore file and use the values defined
	 * there instead of the ones defined directly in pom.xml config.
	 */
	private static Ignore processIgnoreFile(final File ignoreFile, final File basedir) throws IOException {
		final List<String> lines = FileUtils.readLines(ignoreFile, "UTF-8");
		final List<File> excludes = new ArrayList<>();
		for (final String line : lines) {
			excludes.add(new File(basedir, line));
		}
		return new Ignore(excludes, ignoreFile);
	}

	public File getIgnoreFile() {
		return ignoreFile;
	}

	public List<File> getExcludes() {
		return excludes;
	}
}