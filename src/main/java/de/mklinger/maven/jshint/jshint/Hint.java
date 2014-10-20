package de.mklinger.maven.jshint.jshint;

import java.io.Serializable;

public class Hint implements Serializable {
	private static final long serialVersionUID = 1L;

	public static enum HintSeverity {
		ERROR,
		WARNING,
		INFO
	}

	private HintSeverity severity;
	private String id;
	private String code;
	private String raw;
	private String evidence;
	private String reason;
	private Number line;
	private Number character;

	public Hint() {
	}

	public Hint(final JSObject o) {
		this(o.getString("id"),
				o.getString("code"),
				o.getString("raw"),
				o.getString("evidence"),
				o.getNumber("line"),
				o.getNumber("character"),
				o.getString("reason"));
	}

	public Hint(final String id, final String code, final String raw, final String evidence, final Number line, final Number character, final String reason) {
		this.id = id;
		this.code = code;
		this.raw = raw;
		this.evidence = evidence;
		this.line = line;
		this.character = character;
		this.reason = reason;
		this.severity = getSereverity(code);
	}

	private static HintSeverity getSereverity(final String code) {
		final char c = code.charAt(0);
		switch (c) {
			case 'E':
				return HintSeverity.ERROR;
			case 'W':
				return HintSeverity.WARNING;
			case 'I':
				return HintSeverity.INFO;
			default:
				throw new IllegalArgumentException("Unexpected char c=" + c);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(final String raw) {
		this.raw = raw;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(final String evidence) {
		this.evidence = evidence;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public Number getLine() {
		return line;
	}

	public void setLine(final Number line) {
		this.line = line;
	}

	public Number getCharacter() {
		return character;
	}

	public void setCharacter(final Number character) {
		this.character = character;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public HintSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(final HintSeverity severity) {
		this.severity = severity;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Hint [severity=");
		builder.append(severity);
		builder.append(", id=");
		builder.append(id);
		builder.append(", code=");
		builder.append(code);
		builder.append(", raw=");
		builder.append(raw);
		builder.append(", evidence=");
		builder.append(evidence);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", line=");
		builder.append(line);
		builder.append(", character=");
		builder.append(character);
		builder.append("]");
		return builder.toString();
	}
}