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
	public static <T> T readObject(final File path) throws IOException, ClassNotFoundException {
		final ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
		try {
			return (T) in.readObject();
		} finally {
			in.close();
		}
	}

	public static void writeObject(final Object o, final File path) throws IOException {
		final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
		try {
			out.writeObject(o);
		} finally {
			out.close();
		}
	}

	public static void deleteDirectory(final File directory) throws IOException {
		FileUtils.deleteDirectory(directory);
	}

	public static File tempDir() throws IOException {
		final File path = File.createTempFile("tempdirectory", ".dir");
		delete(path);
		mkdirs(path);
		return path;
	}

	public static void delete(final File path) throws IOException {
		if (!path.delete()) {
			throw new IOException("Could not delete " + path.getAbsolutePath());
		}
	}

	public static File mkdirs(final File baseDirectory, final String name) throws IOException {
		final File path = new File(baseDirectory, name);
		mkdirs(path);
		return path;
	}

	public static void mkdirs(final File path) throws IOException {
		if (!path.exists()) {
			path.mkdirs();
			if (!path.isDirectory()) {
				throw new IOException("Could not create directory: " + path.getAbsolutePath());
			}
		}
	}
}
