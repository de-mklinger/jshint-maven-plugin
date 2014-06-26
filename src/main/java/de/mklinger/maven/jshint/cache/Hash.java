package de.mklinger.maven.jshint.cache;

import java.io.Serializable;
import java.util.List;

public class Hash implements Serializable {
    private static final long serialVersionUID = 6729471159921057308L;
    public final String options;
    public final String globals;
    public final String jsHintVersion;
    public final String configFile;
    public final List<String> directories;
    public final List<String> excludes;
    
    public Hash(String options, String globals, String jsHintVersion, String configFile, List<String> directories, List<String> excludes) {
        this.options = options;
        this.globals = globals;
        this.jsHintVersion = jsHintVersion;
        this.configFile = configFile;
        this.directories = directories;
        this.excludes = excludes;
    }
}