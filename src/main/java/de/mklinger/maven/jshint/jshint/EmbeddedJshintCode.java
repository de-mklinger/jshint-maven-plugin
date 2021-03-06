package de.mklinger.maven.jshint.jshint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedJshintCode {
	public static final Map<String, String> EMBEDDED_VERSIONS = Collections.unmodifiableMap(getVersions());

	private static Map<String, String> getVersions() {
		final Map<String, String> versions = new HashMap<>();
		versions.put("2.5.6", "jshint-rhino-2.5.6.js");
		versions.put("2.5.1", "jshint-rhino-2.5.1.js");
		versions.put("2.4.3", "jshint-rhino-2.4.3.js");
		versions.put("2.4.1", "jshint-rhino-2.4.1.js");
		versions.put("2.1.9", "jshint-rhino-2.1.9.js");
		return versions;
	}
}
