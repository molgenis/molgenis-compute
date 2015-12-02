package org.molgenis.compute;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

/**
 * Super class for tests containing methods to compare contents between generated and expected scripts created by
 * compute
 *
 */
public class ScriptComparator
{
	public static final String OUTPUT_DIRECTORY = "target/test/benchmark/run";

	private static final String EXPECTED_FILES_FOLDER = "target/test-classes/expected/";
	private static final String COMPUTE_PROPERTIES_FILE = ".compute.properties";

	private static final FilenameFilter EXTENSION_FILTER = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".sh") || name.endsWith(".finished") || name.endsWith(".env")
					|| name.endsWith(".started");
		}
	};

	private static final FilenameFilter BATCH_FILTER = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.startsWith("batch");
		}
	};

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		File f = new File(OUTPUT_DIRECTORY);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(COMPUTE_PROPERTIES_FILE);
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());
	}

	public void testOutputDirectoryFiles(String testMethodId) throws Exception
	{
		System.out.println("--- Test created file contents in test: " + testMethodId + "---");
		compareOutputDirectoryToExpectedDirectory(EXPECTED_FILES_FOLDER + testMethodId, "");
		for (File file : new File(EXPECTED_FILES_FOLDER + testMethodId).listFiles(BATCH_FILTER))
		{
			compareOutputDirectoryToExpectedDirectory(file.getPath(), file.getName());
		}
	}

	private void compareOutputDirectoryToExpectedDirectory(String expectedDirectory, String subDirectory)
	{
		File expectedFilesDirectory = new File(expectedDirectory);
		for (File expectedFile : expectedFilesDirectory.listFiles(EXTENSION_FILTER))
		{
			File actualFile = new File(new File(OUTPUT_DIRECTORY + File.separator + subDirectory),
					expectedFile.getName());
			Assertions.assertThat(actualFile).hasSameContentAs(expectedFile);
		}
	}
}
