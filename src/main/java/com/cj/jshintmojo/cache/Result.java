package com.cj.jshintmojo.cache;

import java.io.Serializable;
import java.util.List;

import com.cj.jshintmojo.jshint.JSHint.Hint;
import com.cj.jshintmojo.jshint.JSHint.HintSeverity;

public class Result implements Comparable<Result>, Serializable {
    private static final long serialVersionUID = 1L;
    
    public final String path;
    public final Long lastModified;
    public final List<Hint> hints;
    public final boolean hasErrors;
    public final boolean hasWarnings;

	public Result(final String path, final Long lastModified, final List<Hint> hints) {
		this.path = path;
		this.lastModified = lastModified;
		this.hints = hints;
		boolean hasErrors = false;
		boolean hasWarnings = false;
		for (Hint hint : hints) {
            if (hint.severity == HintSeverity.WARNING) {
                hasWarnings = true;
            }
            if (hint.severity == HintSeverity.ERROR) {
                hasErrors = true;
            }
            if (hasErrors && hasWarnings) {
                break;
            }
		}
		this.hasErrors = hasErrors;
		this.hasWarnings = hasWarnings;
	}

    @Override
    public int compareTo(Result o) {
        return path.compareTo(o.path);
    }
}