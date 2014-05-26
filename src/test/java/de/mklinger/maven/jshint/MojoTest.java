package de.mklinger.maven.jshint;

import static de.mklinger.maven.jshint.util.Util.deleteDirectory;
import static de.mklinger.maven.jshint.util.Util.mkdirs;
import static de.mklinger.maven.jshint.util.Util.tempDir;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.mklinger.maven.jshint.Mojo;

public class MojoTest {

    @Test
    public void walksTheDirectoryTreeToFindAndUseJshintFiles() throws Exception {
        String[] jshintIgnorePaths = {".jshintignore", "foo/.jshintignore", "foo/bar/.jshintignore", "foo/bar/baz/.jshintignore"};

        for(String jshintIgnorePath: jshintIgnorePaths){
            File directory = tempDir();
            try{
                // given
                File ignoreFile = new File(directory, jshintIgnorePath);
                FileUtils.writeStringToFile(ignoreFile, "src/main/resources/foo.qunit.js");

                File projectDirectory = mkdirs(directory, "foo/bar/baz");
                File resourcesDirectory = mkdirs(projectDirectory, "src/main/resources");

                File fileToIgnore = new File(resourcesDirectory, "foo.qunit.js");
                FileUtils.writeStringToFile(fileToIgnore, "whatever, this should be ignored");

                LogStub log = new LogStub();
                Mojo mojo = new Mojo("", "",
                        projectDirectory,
                        Collections.singletonList("src/main/resources"),
                        Collections.<String>emptyList(), true, true, true, null, null, null, null);
                mojo.setLog(log);

                // when
                mojo.execute();

                // then
                assertTrue("Sees ignore files", log.hasMessage("info", "Using ignore file: " + ignoreFile.getAbsolutePath()));
                assertTrue("Uses ignore files", log.hasMessage("info", "Excluding " + fileToIgnore.getAbsolutePath()));

            }finally{
                deleteDirectory(directory);
            }
        }
    }

	@Test
	public void warnsUsersWhenConfiguredToWorkWithNonexistentDirectories() throws Exception {
		File directory = tempDir();
		try{
			// given
			mkdirs(directory, "src/main/resources");
			LogStub log = new LogStub();
			Mojo mojo = new Mojo("", "",
							directory,
							Collections.singletonList("src/main/resources/nonexistentDirectory"),
							Collections.<String>emptyList(),true, true, true, null, null, null, null);
			mojo.setLog(log);

			// when
			mojo.execute();

			// then
			assertEquals(0, log.messagesForLevel("warn").size());
			assertEquals(2, log.messagesForLevel("info").size());
			assertTrue(log.hasMessage("info", "You told me to find tests in src/main/resources/nonexistentDirectory, but there is nothing there (" + directory.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "nonexistentDirectory)"));
		}finally{
			deleteDirectory(directory);
		}
	}

    @Test
    public void doesntWarnUsersWhenConfiguredToWorkWithNonexistentDirectoriesAndNonVerbose() throws Exception {
        File directory = tempDir();
        try{
            // given
            mkdirs(directory, "src/main/resources");
            LogStub log = new LogStub();
            Mojo mojo = new Mojo("", "",
                            directory,
                            Collections.singletonList("src/main/resources/nonexistentDirectory"),
                            Collections.<String>emptyList(),true, true, false, null, null, null, null);
            mojo.setLog(log);

            // when
            mojo.execute();

            // then
            assertEquals(0, log.messagesForLevel("warn").size());
            assertEquals(1, log.messagesForLevel("info").size());


        }finally{
            deleteDirectory(directory);
        }
    }
	
	@Test
    public void resolvesConfigFileRelativeToMavenBasedirProperty() throws Exception {
        File directory = tempDir();
        try{
            // given
            File resourcesDirectory = mkdirs(directory, "src/main/resources");
            File jsFile = new File(resourcesDirectory, "test.js");
            FileUtils.writeStringToFile(jsFile, "var x = 0;");

            mkdirs(directory, "foo/bar");
            FileUtils.writeLines(new File(directory, "foo/bar/my-config-file.js"), Arrays.asList(
                    "{",
                    "  \"globals\": {", 
                    "    \"require\": false",
                    "  }",     
                    "}"
                    ));
            
            Mojo mojo = new Mojo(null, "", 
                            directory, 
                            Collections.singletonList("src/main/resources/"), 
                            Collections.<String>emptyList(), true, true, true, "foo/bar/my-config-file.js", null, null, null);
            
            LogStub log = new LogStub();
            mojo.setLog(log);

            // when
            mojo.execute();
            
            // then
            final String properPathForConfigFile = new File(directory, "foo/bar/my-config-file.js").getAbsolutePath();
            assertTrue(log.hasMessage("info", "Using configured 'configFile' from: " + properPathForConfigFile));
            
        }finally{
            deleteDirectory(directory);
        }
    }
    
    @Test
    public void resolvesConfigFileByAbsolutePath() throws Exception {
        File directory = tempDir();
        File baseDir = tempDir();
        try{
            // given
            File resourcesDirectory = mkdirs(baseDir, "src/main/resources");
            File jsFile = new File(resourcesDirectory, "test.js");
            FileUtils.writeStringToFile(jsFile, "var x = 0;");

            mkdirs(directory, "foo/bar");
            File jshintConf = new File(directory, "foo/bar/jshint.conf.json");
            FileUtils.writeLines(jshintConf, Arrays.asList(
                    "{",
                    "  \"globals\": {", 
                    "    \"require\": false",
                    "  }",     
                    "}"
                    ));
            
            Mojo mojo = new Mojo(null, "", 
                    baseDir,
                            Collections.singletonList("src/main/resources/"), 
                            Collections.<String>emptyList(), true, true, true, jshintConf.getAbsolutePath(), null, null, null);
            
            LogStub log = new LogStub();
            mojo.setLog(log);

            // when
            mojo.execute();
            
            // then
            assertTrue(log.hasMessage("info", "Using configured 'configFile' from: " + jshintConf.getAbsolutePath()));
        }finally{
            deleteDirectory(directory);
        }
    }
}
