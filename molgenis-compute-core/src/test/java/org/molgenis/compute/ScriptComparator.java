package org.molgenis.compute;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * Super class for tests containing methods to compare contents between generated and expected scripts created by
 * compute
 *
 */
public class ScriptComparator
{
	public static final String OUTPUT_DIRECTORY = "target/test/benchmark/run/";

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

	@BeforeSuite
	public void beforeSuite() throws IOException
	{
		/*
		 * Make sure we start with a clean folder for the generated results of all tests.
		 */
		File outdir = new File(OUTPUT_DIRECTORY);
		FileUtils.deleteDirectory(outdir);
		Assert.assertFalse(outdir.exists());
	}
	
	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		/*
		 *  Don't delete the complete OUTPUT_DIRECTORY with generated results here!
		 *  Leave them on disk for inspection when a test fails instead.
		 *  Do remove the COMPUTE_PROPERTIES_FILE each time a test is performed though
		 *  to prevent a previous invocation of molgenis-compute affecting the test.
		 */
		File f = new File(COMPUTE_PROPERTIES_FILE);
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());
	}
	
	public void testOutputDirectoryFiles(String testMethodId) throws Exception
	{
		System.out.println("--- Comparing created content for test: " + testMethodId + " to expected results ---");
		
		/*
		 * Regular script generation without batches.
		 */
		compareOutputDirectoryToExpectedDirectory(testMethodId, "");
		
		/*
		 * Scripts generated in two or more batches: compare the results in each batch* subfolder.
		 * Note: confusingly File object in old API is for files and folders; in this case we are 
		 * processing folders and .listFiles produces a list of batch* sub folders.
		 */
		for (File folder : new File(EXPECTED_FILES_FOLDER + testMethodId).listFiles(BATCH_FILTER))
		{
			compareOutputDirectoryToExpectedDirectory(testMethodId, folder.getName());
		}
	}

	private void compareOutputDirectoryToExpectedDirectory(String testMethodId, String subDirectory)
	{
		File expectedFilesDirectory = new File(EXPECTED_FILES_FOLDER + testMethodId + "/" + subDirectory);
		for (File expectedFile : expectedFilesDirectory.listFiles(EXTENSION_FILTER))
		{
			File actualFile = new File(
					new File(OUTPUT_DIRECTORY + testMethodId + "/" + subDirectory),
					expectedFile.getName()
			);
			Assertions.assertThat(actualFile).hasSameContentAs(expectedFile);
		}
	}
}
