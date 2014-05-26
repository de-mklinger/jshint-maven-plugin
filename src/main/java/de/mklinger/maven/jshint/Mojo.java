package de.mklinger.maven.jshint;

import static de.mklinger.maven.jshint.util.Util.mkdirs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import de.mklinger.maven.jshint.cache.Cache;
import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.EmbeddedJshintCode;
import de.mklinger.maven.jshint.jshint.FunctionalJava;
import de.mklinger.maven.jshint.jshint.JSHint;
import de.mklinger.maven.jshint.jshint.FunctionalJava.Fn;
import de.mklinger.maven.jshint.jshint.JSHint.Hint;
import de.mklinger.maven.jshint.jshint.JSHint.HintSeverity;
import de.mklinger.maven.jshint.reporter.CheckStyleReporter;
import de.mklinger.maven.jshint.reporter.JSHintReporter;
import de.mklinger.maven.jshint.reporter.JSLintReporter;
import de.mklinger.maven.jshint.reporter.LogReporter;
import de.mklinger.maven.jshint.util.OptionsParser;
import de.mklinger.maven.jshint.util.Util;

/**
 * @goal lint
 * @phase compile
 * @threadSafe
 */
public class Mojo extends AbstractMojo {

	/**
	 * @parameter property="directories"
	 */
	private final List<String> directories = new ArrayList<String>();

	/**
	 * @parameter property="excludes"
	 */
	private final List<String> excludes = new ArrayList<String>();

	/**
	 * @parameter property="options"
	 */
	private String options = null;

	/**
	 * @parameter
	 */
	private String globals = "";

	/**
	 * @parameter property="configFile"
	 */
	private String configFile = "";

	/**
	 * @parameter property="reporter"
	 */
	private String reporter = "";

	/**
	 * @parameter property="reportFile"
	 */
	private String reportFile = "";

	/**
	 * @parameter property="ignoreFile"
	 */
	private String ignoreFile = "";

    /**
     * @parameter expression="${jshint.version}"
     */
    private final String version = "2.5.1";

    /**
     * @parameter
     */
    private Boolean verbose = false;

	/**
	 * @parameter
	 */
	private Boolean failOnError = true;
	
	/**
     * @parameter
     */
    private Boolean failOnWarning = true;

    /**
     * @parameter
     */
    private Set<String> errorHints;

    /**
     * @parameter
     */
    private Set<String> warningHints;

    /**
     * @parameter
     */
    private Set<String> infoHints;

	/**
	 * @parameter default-value="${basedir}
	 * @readonly
	 * @required
	 */
	private File basedir;

	public Mojo() {}

	public Mojo(final String options, final String globals, final File basedir, final List<String> directories, 
	        final List<String> excludes, final boolean failOnError, final boolean failOnWarning, boolean verbose,
	        final String configFile, final String reporter, final String reportFile, final String ignoreFile) {
		super();
		this.options = options;
		this.globals = globals;
		this.basedir = basedir;
		this.directories.addAll(directories);
		this.excludes.addAll(excludes);
        this.failOnError = failOnError;
        this.failOnWarning = failOnWarning;
        this.verbose = verbose;
		this.configFile = configFile;
		this.reporter = reporter;
		this.reportFile = reportFile;
		this.ignoreFile = ignoreFile;
	}

	@Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<File> files = findFilesToCheck();
        if (files.isEmpty()) {
            getLog().info("No files to process.");
            return;
        } else {
            getLog().info("Processing " + files.size() + " files.");
        }
	    
        final Config config = readConfig(this.options, this.globals, this.configFile, this.basedir);
        final Cache.Hash cacheHash = new Cache.Hash(config.options, config.globals, this.version, this.configFile, this.directories, this.excludes);

        debug("Using jshint version " + version);
        final String jshintCode = getEmbeddedJshintCode(version);
        final JSHint jshint = new JSHint(jshintCode);

		try {
			final File targetPath = new File(basedir, "target");
			mkdirs(targetPath);
			final File cachePath = new File(targetPath, "lint.cache");
			final Cache cache = readCache(cachePath, cacheHash);
			final Results currentResults = lintTheFiles(jshint, cache, files, config, getLog());
			Util.writeObject(new Cache(cacheHash, currentResults), cachePath);
            handleResults(currentResults);
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Something bad happened", e);
		}
	}

	static class Config {
	    final String options, globals;

        public Config(final String options, final String globals) {
            this.options = options;
            this.globals = globals;
        }

	}

    private Config readConfig(final String options, final String globals, final String configFileParam, final File basedir) throws MojoExecutionException {
        final Config config;
        if (options != null) {
            config = new Config(options, globals);
        } else {
            File configFile = null;
            if (StringUtils.isNotBlank(configFileParam)) {
                configFile = new File(configFileParam);
                if (!configFile.isAbsolute()) {
                    configFile = new File(basedir, configFileParam);
                }
            }
            if (configFile != null && configFile.exists()) {
                debug("Using configured 'configFile' from: " + configFile.getAbsolutePath());
                config = processConfigFile(configFile);
            } else {
                final File jshintRc = findJshintrc(basedir);
                if (jshintRc != null) {
                    debug("No configFile configured or found, but found a '.jshintrc' file: " + jshintRc.getAbsolutePath());
                    config = processConfigFile(jshintRc);
                } else {
                    debug("No options, configFile or '.jshintrc' file found. Only using configured globals.");
                    config = new Config("", globals);
                }
            }
        }
        return config;
    }

    static class Ignore {

        final List<String> lines;

        public Ignore(final List<String> lines) {
            this.lines = lines;
        }

    }

    private static Ignore readIgnore(final String ignoreFileParam, final File basedir, final Log log) throws MojoExecutionException {
        final File jshintignore = findJshintignore(basedir);
        final File ignoreFile = StringUtils.isNotBlank(ignoreFileParam) ? new File(basedir, ignoreFileParam) : null;

        final Ignore ignore;
        if (ignoreFile != null) {
            log.info("Using ignore file: " + ignoreFile.getAbsolutePath());
            ignore = processIgnoreFile(ignoreFile);
        } else if (jshintignore != null) {
            log.info("Using ignore file: " + jshintignore.getAbsolutePath());
            ignore = processIgnoreFile(jshintignore);
        } else {
            ignore = new Ignore(Collections.<String>emptyList());
        }

        return ignore;
    }

    private List<File> findFilesToCheck() throws MojoExecutionException {
        if(directories.isEmpty()){
            directories.add("src");
        }
        if (this.excludes.isEmpty() || (this.ignoreFile != null && !this.ignoreFile.isEmpty())) {
            this.excludes.addAll(readIgnore(this.ignoreFile, this.basedir, getLog()).lines);
        }

        List<File> javascriptFiles = new ArrayList<File>();
        for(String next: directories){
        	File path = new File(basedir, next);
        	if(!path.exists() && !path.isDirectory()){
        		debug("You told me to find tests in " + next + ", but there is nothing there (" + path.getAbsolutePath() + ")");
        	}else{
        		collect(path, javascriptFiles);
        	}
        }

        List<File> matches = FunctionalJava.filter(javascriptFiles, new Fn<File, Boolean>(){
        	@Override
            public Boolean apply(final File i) {
        		for(String exclude : excludes){
        			File e = new File(basedir, exclude);
        			if(i.getAbsolutePath().startsWith(e.getAbsolutePath())){
        				debug("Excluding " + i);
        				return Boolean.FALSE;
        			}
        		}

        		return Boolean.TRUE;
        	}
        });
        return matches;
    }

    private Results lintTheFiles(final JSHint jshint, final Cache cache, final List<File> filesToCheck, final Config config, final Log log) throws FileNotFoundException {
        final Results currentResults = new Results();
        for (File file : filesToCheck) {
            Result previousResult = cache.previousResults.getResult(file.getAbsolutePath());
            Result theResult;
            if (previousResult == null || (previousResult.lastModified.longValue() != file.lastModified())) {
                debug(file.getAbsolutePath());
                List<Hint> hints = jshint.run(new FileInputStream(file), config.options, config.globals);
                mapSeverities(hints);
                theResult = new Result(file.getAbsolutePath(), file.lastModified(), hints);
            } else {
                debug(file.getAbsolutePath() + " [no change]");
                theResult = previousResult;
            }

            if (theResult != null) {
                currentResults.add(theResult);
            }
        }
        return currentResults;
    }

    private void mapSeverities(List<Hint> hints) {
        if (hints == null || hints.isEmpty() || (errorHints == null && warningHints == null && infoHints == null)) {
            return;
        }
        for (Hint hint : hints) {
            HintSeverity newSeverity = null;
            if (errorHints != null && errorHints.contains(hint.code)) {
                newSeverity = HintSeverity.ERROR;
            } else if (warningHints != null && warningHints.contains(hint.code)) {
                newSeverity = HintSeverity.WARNING;
            } else if (infoHints != null && infoHints.contains(hint.code)) {
                newSeverity = HintSeverity.INFO;
            }
            if (newSeverity != null && hint.severity != newSeverity) {
                debug("Mapping hint with code " + hint.code + " from severity " + hint.severity + " to severity " + newSeverity);
                hint.severity = newSeverity;
            }
        }
    }

    private void handleResults(final Results results) throws MojoExecutionException {
            JSHintReporter reporter = getConfiguredReporter();
            if (reporter != null) {
                reporter.report(results);
            }
            new LogReporter(getLog()).report(results);
            
            if ((failOnError && results.hasErrors()) || (failOnWarning && results.hasWarnings())) {
                throw new MojoExecutionException(getErrorMessage(results));
            }
    }

    private JSHintReporter getConfiguredReporter() {
        File file = StringUtils.isNotBlank(reportFile) ? new File(reportFile) : new File("target/jshint.xml");
        if (JSLintReporter.FORMAT.equalsIgnoreCase(reporter)) {
            return new JSLintReporter(file);
        }
        if(CheckStyleReporter.FORMAT.equalsIgnoreCase(reporter)) {
            return new CheckStyleReporter(file);
        }
        if(StringUtils.isNotBlank(reporter)) {
            getLog().warn("Unknown reporter \"" + reporter + "\". Skip reporting.");
        }
        return null;
    }

    private String getErrorMessage(Results results) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("\nJSHint found ");
        
        if (results.hasErrors()) {
            errorMessage.append("errors ");
        }
        if (results.hasErrors() && results.hasWarnings()) {
            errorMessage.append("and ");
        }
        if (results.hasWarnings()) {
            errorMessage.append("warnings ");
        }
        
        errorMessage.append("in ");
        errorMessage.append(results.getNumberOfHints());
        errorMessage.append(" file");
        // pluralise
        if (results.getNumberOfHints() > 1) {
            errorMessage.append("s");
        }
        errorMessage.append("! Please see errors/warnings above!");
        
        errorMessage.append("\nJSHint is ");
        if (!failOnError && !failOnWarning) {
            errorMessage.append("not configured to fail on error or warning.");
        } else {
            errorMessage.append("configured to fail on ");
            if (failOnError) {
                errorMessage.append("error ");
            }
            if (failOnError && failOnWarning) {
                errorMessage.append("or ");
            }
            if (failOnWarning) {
                errorMessage.append("warning ");
            }
        }
        return errorMessage.toString();
    }
    
    private static String getEmbeddedJshintCode(final String version) throws MojoFailureException {
        final String resource = EmbeddedJshintCode.EMBEDDED_VERSIONS.get(version);
        if(resource==null){
            StringBuffer knownVersions = new StringBuffer();
            for(String v : EmbeddedJshintCode.EMBEDDED_VERSIONS.keySet()){
                knownVersions.append("\n    " + v);
            }
            throw new MojoFailureException("I don't know about the \"" + version + "\" version of jshint.  Here are the versions I /do/ know about: " + knownVersions);
        }
        return resource;
    }

    private static File findJshintrc(final File cwd) {
        File placeToLook = cwd;
        while(placeToLook.getParentFile()!=null){
            File rcFile = new File(placeToLook, ".jshintrc");
            if(rcFile.exists()){
                return rcFile;
            }else{
                placeToLook = placeToLook.getParentFile();
            }
        }

        return null;
    }

    private static File findJshintignore(final File cwd) {
        File placeToLook = cwd;
        while (placeToLook.getParentFile() != null) {
            File ignoreFile = new File(placeToLook, ".jshintignore");
            if (ignoreFile.exists()) {
                return ignoreFile;
            } else {
                placeToLook = placeToLook.getParentFile();
            }
        }

        return null;
    }

	private Cache readCache(final File path, final Cache.Hash hash){
		try {
			if(path.exists()){
				Cache cache = Util.readObject(path);
		        if(EqualsBuilder.reflectionEquals(cache.hash, hash)){
		            return cache;
		        }else{
		        	getLog().warn("Something changed ... clearing cache");
		            return new Cache(hash);
		        }

			}
		} catch (Throwable e) {
			super.getLog().warn("I was unable to read the cache. This may be because of an upgrade to the plugin.");
		}

		return new Cache(hash);
	}

	private void collect(final File directory, final List<File> files) {
		for(File next : directory.listFiles()){
			if(next.isDirectory()){
				collect(next, files);
			}else if(next.getName().endsWith(".js")){
				files.add(next);
			}
		}
	}

	/**
	 * Read contents of the specified config file and use the values defined there instead of the ones defined directly in pom.xml config.
	 *
	 * @throws MojoExecutionException if the specified file cannot be processed
	 */
	private static Config processConfigFile(final File configFile) throws MojoExecutionException {
		byte[] configFileContents;
		try {
			configFileContents = FileUtils.readFileToByteArray(configFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to read config file located in " + configFile);
		}

		Set<String> globalsSet = OptionsParser.extractGlobals(configFileContents);
		Set<String> optionsSet = OptionsParser.extractOptions(configFileContents);

		final String globals, options;

		if (globalsSet.size() > 0) {
			globals = StringUtils.join(globalsSet.iterator(), ",");
		}else{
		    globals = "";
		}

		if (optionsSet.size() > 0) {
			options = StringUtils.join(optionsSet.iterator(), ",");
		}else{
		    options = "";
		}

		return new Config(options, globals);
	}

    /**
     * Read contents of the specified ignore file and use the values defined
     * there instead of the ones defined directly in pom.xml config.
     *
     * @throws MojoExecutionException if the specified file cannot be processed
     */
    private static Ignore processIgnoreFile(final File ignoreFile) throws MojoExecutionException {
        try {
            return new Ignore(FileUtils.readLines(ignoreFile, "UTF-8"));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read ignore file located in " + ignoreFile, e);
        }
    }
    
    private void debug(String s) {
        if (verbose) {
            getLog().info(s);
        } else {
            getLog().debug(s);
        }
    }
}
