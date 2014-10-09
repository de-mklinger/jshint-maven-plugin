package de.mklinger.maven.jshint.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;

public class Util {

	@SuppressWarnings("unchecked")
	public static <T> T readObject(final File path) {
		try {
			final ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			try {
				return (T) in.readObject();
			} finally {
				in.close();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeObject(final Object o, final File path) {
		try {
			final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
			try {
				out.writeObject(o);
			} finally {
				out.close();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void deleteDirectory(final File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static File tempDir() {
		try {
			final File path = File.createTempFile("tempdirectory", ".dir");
			delete(path);
			mkdirs(path);
			return path;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void delete(final File path) {
		if (!path.delete()) {
			throw new RuntimeException("Could not delete " + path.getAbsolutePath());
		}
	}

	public static File mkdirs(final File directory, final String string) {
		final File path = new File(directory, string);
		mkdirs(path);
		return path;
	}

	public static void mkdirs(final File path) {
		if (!path.exists() && !path.mkdirs()) {
			throw new RuntimeException("Could not create directory: " + path.getAbsolutePath());
		}
	}

}
