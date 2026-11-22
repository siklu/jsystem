package jsystem.framework;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class QmsReportingPreferenceTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private String originalUserDir;
	private String originalOverride;
	private Level originalLogLevel;
	private JSystemProperties properties;

	@Before
	public void setUp() throws Exception {
		originalUserDir = System.getProperty("user.dir");
		originalOverride = System.getProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString());
		originalLogLevel = Logger.getLogger("").getLevel();
		System.clearProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString());
		System.setProperty("user.dir", folder.getRoot().getAbsolutePath());
		Files.write(new File(folder.getRoot(), "jsystem.properties").toPath(),
				"logger=false\n".getBytes(StandardCharsets.US_ASCII));
		// Use an isolated instance without replacing the framework singleton.
		Constructor<JSystemProperties> constructor = JSystemProperties.class.getDeclaredConstructor(boolean.class);
		constructor.setAccessible(true);
		properties = constructor.newInstance(true);
	}

	@After
	public void tearDown() {
		System.setProperty("user.dir", originalUserDir);
		if (originalOverride == null) {
			System.clearProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString());
		} else {
			System.setProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString(), originalOverride);
		}
		Logger.getLogger("").setLevel(originalLogLevel);
	}

	@Test
	public void defaultsToDisabledAndPersistsBothCheckboxStates() {
		properties.setPreference(FrameworkOptions.RUNNER_ALOW_EDIT, "true");
		assertTrue(properties.isQmsReportingEditable());
		assertFalse(properties.isQmsReportingEnabled());

		properties.setPreference(FrameworkOptions.QMS_REPORTING_ENABLED, "true");
		properties.rereadPropertiesFile();
		assertTrue(properties.isQmsReportingEnabled());

		properties.setPreference(FrameworkOptions.QMS_REPORTING_ENABLED, "false");
		properties.rereadPropertiesFile();
		assertFalse(properties.isQmsReportingEnabled());
	}

	@Test
	public void honorsJvmOverrideForExecutionWithoutUi() {
		properties.setPreference(FrameworkOptions.RUNNER_ALOW_EDIT, "true");
		properties.setPreference(FrameworkOptions.QMS_REPORTING_ENABLED, "false");
		System.setProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString(), "true");
		assertTrue(properties.isQmsReportingEnabled());

		properties.setPreference(FrameworkOptions.QMS_REPORTING_ENABLED, "true");
		System.setProperty(FrameworkOptions.QMS_REPORTING_ENABLED.toString(), "false");
		assertFalse(properties.isQmsReportingEnabled());
	}

	@Test
	public void isForcedOnAndLockedWhenRunnerEditIsNotAllowed() {
		properties.setPreference(FrameworkOptions.QMS_REPORTING_ENABLED, "false");

		properties.setPreference(FrameworkOptions.RUNNER_ALOW_EDIT, "false");
		assertFalse(properties.isQmsReportingEditable());
		assertTrue(properties.isQmsReportingEnabled());

		properties.setPreference(FrameworkOptions.RUNNER_ALOW_EDIT, "true");
		assertTrue(properties.isQmsReportingEditable());
		assertFalse(properties.isQmsReportingEnabled());
	}
}
