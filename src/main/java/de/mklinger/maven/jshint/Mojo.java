package de.mklinger.maven.jshint;

import static de.mklinger.maven.jshint.util.Util.mkdirs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
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
import de.mklinger.maven.jshint.cache.Hash;
import de.mklinger.maven.jshint.cache.Result;
import de.mklinger.maven.jshint.cache.Results;
import de.mklinger.maven.jshint.jshint.EmbeddedJshintCode;
import de.mklinger.maven.jshint.jshint.JSHint;
import de.mklinger.maven.jshint.jshint.JSHint.Hint;
import de.mklinger.maven.jshint.jshint.JSHint.HintSeverity;
import de.mklinger.maven.jshint.reporter.CheckStyleReporter;
import de.mklinger.maven.jshint.reporter.JSHintReporter;
import de.mklinger.maven.jshint.reporter.JSLintReporter;
import de.mklinger.maven.jshint.reporter.LogReporter;
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
     * @parameter property="jshint.version"
     */
    private String version = "2.5.6";

    /**
     * @parameter property="project.build.sourceEncoding"
     */
    private String sourceEncoding;

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
            final List<String> excludes, final boolean failOnError, final boolean failOnWarning, final boolean verbose,
            final String configFile, final String reporter, final String reportFile, final String ignoreFile,
            final String version, final String sourceEncoding) {
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
        if (version != null) {
            this.version = version;
        }
        this.sourceEncoding = sourceEncoding;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doExecute();
        } catch (MojoExecutionException | MojoFailureException e) {
            throw e;
        } catch (final Exception e) {
            throw new MojoExecutionException("Error executing jshint mojo", e);
        }
    }

    private void doExecute() throws IOException, MojoExecutionException, MojoFailureException {
        final List<File> files = findFilesToCheck();
        if (files.isEmpty()) {
            getLog().info("No files to process.");
            return;
        } else {
            getLog().info("Processing " + files.size() + " files.");
        }

        final Config config = getConfig();
        final Hash cacheHash = new Hash(config.getOptions(), config.getGlobals(), this.version, this.configFile, this.directories, this.excludes);

        debug("Using jshint version " + version);
        final String jshintResourceName = getEmbeddedJshintResourceName(version);
        final JSHint jshint = new JSHint(jshintResourceName, getActualSourceEncoding());

        final File targetPath = new File(basedir, "target");
        mkdirs(targetPath);
        final File cachePath = new File(targetPath, "lint.cache");
        final Cache cache = readCache(cachePath, cacheHash);
        final Results currentResults = lintTheFiles(jshint, cache, files, config, getLog());
        Util.writeObject(new Cache(cacheHash, currentResults), cachePath);
        handleResults(currentResults);
    }

    private Config getConfig() throws IOException {
        Config config;
        if (options != null) {
            config = new Config(options, globals);
        } else {
            config = Config.loadConfig(configFile, basedir);
        }
        if (config == null) {
            config = new Config("", globals);
        }
        debugConfig(config);
        return config;
    }

    private void debugConfig(final Config config) {
        switch (config.getConfigType()) {
            case OPTIONS:
                debug("Using configured options.");
                break;
            case CONFIGFILE:
                debug("Using configured 'configFile' from: " + config.getConfigFile().getAbsolutePath());
                break;
            case JSHINTRC:
                debug("No configFile configured or found, but found a '.jshintrc' file: " + config.getConfigFile().getAbsolutePath());
                break;
            case GLOBALS:
                debug("No options, configFile or '.jshintrc' file found. Only using configured globals.");
                break;
            default:
                break;
        }
    }

    private String getActualSourceEncoding() {
        if (sourceEncoding != null && !sourceEncoding.isEmpty()) {
            return sourceEncoding;
        }
        final String platformEncoding = Charset.defaultCharset().name();
        getLog().warn("Using platform encoding (" + platformEncoding + " actually) to load jshint source files, i.e. build is platform dependent!");
        return platformEncoding;
    }

    public static class Ignore {
        private final List<String> lines;

        public Ignore(final List<String> lines) {
            this.lines = lines;
        }
    }

    private Ignore readIgnore(final String ignoreFileParam, final File basedir) throws IOException {
        final File jshintignore = findJshintignore(basedir);
        final File ignoreFile = StringUtils.isNotBlank(ignoreFileParam) ? new File(basedir, ignoreFileParam) : null;

        final Ignore ignore;
        if (ignoreFile != null) {
            debug("Using ignore file: " + ignoreFile.getAbsolutePath());
            ignore = processIgnoreFile(ignoreFile);
        } else if (jshintignore != null) {
            debug("Using ignore file: " + jshintignore.getAbsolutePath());
            ignore = processIgnoreFile(jshintignore);
        } else {
            ignore = new Ignore(Collections.<String>emptyList());
        }

        return ignore;
    }

    private List<File> findFilesToCheck() throws IOException {
        final List<File> actualDirectories = getActualDirectories();
        final List<File> actualExcludes = getActualExcludes();

        final List<File> javascriptFiles = new ArrayList<File>();
        for (final File directory : actualDirectories){
            if (!directory.exists() || !directory.isDirectory()){
                debug("You told me to find tests in " + directory + ", but there is nothing there");
            } else {
                collect(directory, javascriptFiles, actualExcludes);
            }
        }

        return javascriptFiles;
    }

    private List<File> getActualExcludes() throws IOException {
        final List<File> actualExcludes = new ArrayList<>();
        for (final String exclude : excludes) {
            actualExcludes.add(new File(basedir, exclude));
        }
        if (actualExcludes.isEmpty() || (ignoreFile != null && !ignoreFile.isEmpty())) {
            for (final String exclude : readIgnore(ignoreFile, basedir).lines) {
                actualExcludes.add(new File(basedir, exclude));
            }
        }
        return actualExcludes;
    }

    private List<File> getActualDirectories() {
        final List<File> actualDirectories = new ArrayList<>();
        for (final String dirName : this.directories) {
            actualDirectories.add(new File(basedir, dirName));
        }
        if (actualDirectories.isEmpty()) {
            actualDirectories.add(new File(basedir, "src"));
        }
        return actualDirectories;
    }

    private Results lintTheFiles(final JSHint jshint, final Cache cache, final List<File> filesToCheck, final Config config, final Log log) throws IOException {
        final Results currentResults = new Results();
        for (final File file : filesToCheck) {
            final Result previousResult = cache.previousResults.getResult(file.getAbsolutePath());
            Result theResult;
            if (previousResult == null || (previousResult.lastModified.longValue() != file.lastModified())) {
                debug(file.getAbsolutePath());
                final List<Hint> hints;
                try (FileInputStream in = new FileInputStream(file)) {
                    hints = jshint.run(in, config.getOptions(), config.getGlobals());
                }
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

    private void mapSeverities(final List<Hint> hints) {
        if (hints == null || hints.isEmpty() || (errorHints == null && warningHints == null && infoHints == null)) {
            return;
        }
        for (final Hint hint : hints) {
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

    private void handleResults(final Results results) throws MojoFailureException, MojoExecutionException {
        final JSHintReporter reporter = getConfiguredReporter();
        if (reporter != null) {
            reporter.report(results);
        }
        new LogReporter(getLog()).report(results);

        if ((failOnError && results.hasErrors()) || (failOnWarning && results.hasWarnings())) {
            throw new MojoFailureException(getErrorMessage(results));
        }
    }

    private JSHintReporter getConfiguredReporter() {
        final File file = StringUtils.isNotBlank(reportFile) ? new File(reportFile) : new File("target/jshint.xml");
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

    private String getErrorMessage(final Results results) {
        final StringBuilder errorMessage = new StringBuilder();
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

    private static String getEmbeddedJshintResourceName(final String version) throws MojoExecutionException {
        final String resource = EmbeddedJshintCode.EMBEDDED_VERSIONS.get(version);
        if (resource == null){
            final StringBuilder knownVersions = new StringBuilder();
            for (final String v : EmbeddedJshintCode.EMBEDDED_VERSIONS.keySet()){
                knownVersions.append("\n    ");
                knownVersions.append(v);
            }
            throw new MojoExecutionException("I don't know about the \"" + version + "\" version of jshint.  Here are the versions I /do/ know about: " + knownVersions.toString());
        }
        return resource;
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

    private Cache readCache(final File path, final Hash hash){
        try {
            if (path.exists()){
                final Cache cache = Util.readObject(path);
                if (EqualsBuilder.reflectionEquals(cache.hash, hash)){
                    return cache;
                } else {
                    getLog().warn("Something changed ... clearing cache");
                    return new Cache(hash);
                }

            }
        } catch (final Throwable e) {
            getLog().warn("I was unable to read the cache. This may be because of an upgrade to the plugin.");
        }

        return new Cache(hash);
    }

    private void collect(final File directory, final List<File> files, final List<File> actualExcludes) {
        for (final File candidateFile : directory.listFiles()){
            if (candidateFile.isDirectory()){
                collect(candidateFile, files, actualExcludes);
            } else if (candidateFile.getName().endsWith(".js")) {
                if (isExcluded(candidateFile, actualExcludes)) {
                    debug("Excluding " + candidateFile.getAbsolutePath());
                } else {
                    files.add(candidateFile);
                }
            }
        }
    }

    private boolean isExcluded(final File candidateFile, final List<File> actualExcludes) {
        boolean excluded = false;
        for (final File excludeFile : actualExcludes) {
            if (candidateFile.getAbsolutePath().startsWith(excludeFile.getAbsolutePath())) {
                excluded = true;
            }
        }
        return excluded;
    }

    /**
     * Read contents of the specified ignore file and use the values defined
     * there instead of the ones defined directly in pom.xml config.
     */
    private static Ignore processIgnoreFile(final File ignoreFile) throws IOException {
        return new Ignore(FileUtils.readLines(ignoreFile, "UTF-8"));
    }

    private void debug(final String s) {
        if (verbose) {
            getLog().info(s);
        } else {
            getLog().debug(s);
        }
    }
}