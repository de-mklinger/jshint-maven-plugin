package de.mklinger.maven.jshint.jshint;

import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.NativeObject;

public class JSObject {
	private final NativeObject a;

	public JSObject(final NativeObject o) {
		if (o == null) {
			throw new NullPointerException();
		}
		this.a = o;
	}

	public String getString(final String name) {
		final Object value = a.get(name);
		if (value instanceof ConsString) {
			final ConsString cs = (ConsString)value;
			return cs.toString();
		} else {
			return (String)value;
		}
	}

	public Number getNumber(final String name) {
		return (Number)a.get(name);
	}
}