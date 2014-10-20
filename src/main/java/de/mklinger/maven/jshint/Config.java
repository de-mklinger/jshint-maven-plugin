package de.mklinger.maven.jshint;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import de.mklinger.maven.jshint.util.OptionsParser;

public class Config {
	private static final String COMMA = ",";
	private static final String EMPTY_STRING = "";

	public static enum ConfigType {
		OPTIONS,
		GLOBALS,
		CONFIGFILE,
		JSHINTRC
	}

	private final ConfigType configType;
	private final File configFile;
	private final String options;
	private final String globals;

	private Config(final ConfigType configType, final File configFile) throws IOException {
		this.configType = configType;
		this.configFile = configFile;

		final byte[] configFileContents = FileUtils.readFileToByteArray(configFile);

		final Set<String> globalsSet = OptionsParser.extractGlobals(configFileContents);
		final Set<String> optionsSet = OptionsParser.extractOptions(configFileContents);

		if (globalsSet.size() > 0) {
			globals = StringUtils.join(globalsSet.iterator(), COMMA);
		} else {
			globals = EMPTY_STRING;
		}

		if (optionsSet.size() > 0) {
			options = StringUtils.join(optionsSet.iterator(), COMMA);
		} else {
			options = EMPTY_STRING;
		}
	}

	public Config(final String options, final String globals) {
		this.options = options;
		this.globals = globals;
		this.configFile = null;
		if (options != null && !options.isEmpty()) {
			this.configType = ConfigType.OPTIONS;
		} else {
			this.configType = ConfigType.GLOBALS;
		}
	}

	public static Config loadConfig(final String configFileParam, final File basedir) throws IOException {
		File configFile = null;
		if (StringUtils.isNotBlank(configFileParam)) {
			configFile = new File(configFileParam);
			if (!configFile.isAbsolute()) {
				configFile = new File(basedir, configFileParam);
			}
		}
		if (configFile != null && configFile.exists()) {
			return new Config(ConfigType.CONFIGFILE, configFile);
		} else {
			final File jshintRc = findJshintrc(basedir);
			if (jshintRc != null) {
				return new Config(ConfigType.JSHINTRC, jshintRc);
			} else {
				return null;
			}
		}
	}

	private static File findJshintrc(final File cwd) {
		File placeToLook = cwd;
		while (placeToLook.getParentFile() != null) {
			final File rcFile = new File(placeToLook, ".jshintrc");
			if (rcFile.exists()) {
				return rcFile;
			} else {
				placeToLook = placeToLook.getParentFile();
			}
		}

		return null;
	}

	public ConfigType getConfigType() {
		return configType;
	}

	public File getConfigFile() {
		return configFile;
	}

	public String getOptions() {
		return options;
	}

	public String getGlobals() {
		return globals;
	}
}