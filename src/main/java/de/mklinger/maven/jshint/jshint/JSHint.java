package de.mklinger.maven.jshint.jshint;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import de.mklinger.maven.jshint.util.Rhino;

public class JSHint {
	private static final String PREQUEL = "print=function(){};"
			+ "quit=function(){};"
			+ "readFile=function(){};"
			+ "arguments=[];";
	private final Rhino rhino;
	private final String srcFileEncoding;

	public JSHint(final String jshintResourceName, final String srcFileEncoding) throws IOException {
		this.srcFileEncoding = srcFileEncoding;
		rhino = new Rhino();
		try {
			rhino.eval(PREQUEL);
			rhino.eval(commentOutTheShebang(resourceAsString(jshintResourceName, "UTF-8")));
		} catch (final EcmaError e) {
			throw new RuntimeException("Javascript eval error:" + e.getScriptStackTrace(), e);
		}
	}

	private String commentOutTheShebang(final String code) {
		if (code.startsWith("#!")) {
			return "//" + code;
		} else {
			return code;
		}
	}

	public List<Hint> run(final InputStream source, final String options, final String globals) throws IOException {
		final List<Hint> results = new ArrayList<Hint>();

		final String sourceAsText = toString(source, srcFileEncoding);

		final NativeObject nativeOptions = toJsObject(options);
		final NativeObject nativeGlobals = toJsObject(globals);

		final Boolean codePassesMuster = rhino.call("JSHINT", sourceAsText, nativeOptions, nativeGlobals);

		if (!codePassesMuster) {
			final NativeArray errors = rhino.eval("JSHINT.errors");

			for (final Object next : errors) {
				if (next != null) { // sometimes it seems that the last error in the list is null
					final JSObject jso = new JSObject((NativeObject)next);
					results.add(new Hint(jso));
				}
			}
		}

		return results;
	}

	private NativeObject toJsObject(final String options) {
		final NativeObject nativeOptions = new NativeObject();
		for (final String nextOption : options.split(",")) {
			final String option = nextOption.trim();
			if (!option.isEmpty()) {
				final String name;
				final Object value;

				final int valueDelimiter = option.indexOf(':');
				if (valueDelimiter == -1) {
					name = option;
					value = Boolean.TRUE;
				} else {
					name = option.substring(0, valueDelimiter);
					final String rest = option.substring(valueDelimiter + 1).trim();
					if (rest.matches("[0-9]+")) {
						value = Integer.parseInt(rest);
					} else if ("true".equals(rest)) {
						value = Boolean.TRUE;
					} else if ("false".equals(rest)) {
						value = Boolean.FALSE;
					} else {
						value = rest;
					}
				}
				nativeOptions.defineProperty(name, value, ScriptableObject.READONLY);
			}
		}
		return nativeOptions;
	}

	private static String toString(final InputStream in, final String encoding) throws IOException {
		return IOUtils.toString(in, encoding);
	}

	private String resourceAsString(final String name, final String encoding) throws IOException {
		try (final InputStream in = getClass().getResourceAsStream(name)) {
			return toString(in, encoding);
		}
	}
}
