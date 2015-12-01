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
	public static final FilenameFilter EXTENSION_FILTER = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".sh") || name.endsWith(".finished") || name.endsWith(".env")
					|| name.endsWith(".started");
		}
	};

	public static final FilenameFilter BATCH_FILTER = new FilenameFilter()
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

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());
	}

	public void compareOutputDirToExpectedDir(String expectedDirectory, String subDirectory)
	{
		File expectedFilesDirectoryAfterGenerate = new File(expectedDirectory);
		for (File expectedFile : expectedFilesDirectoryAfterGenerate.listFiles(EXTENSION_FILTER))
		{

			File actualFile = new File(new File(OUTPUT_DIRECTORY + File.separator + subDirectory),
					expectedFile.getName());
			Assertions.assertThat(actualFile).hasSameContentAs(expectedFile);
		}
	}

	public void testOutputDirectoryFiles(String testMethodId) throws Exception
	{
		System.out.println("--- Test created file contents in test: " + testMethodId + "---");
		compareOutputDirToExpectedDir("src/test/resources/expected/" + testMethodId, "");
		for (File file : new File("src/test/resources/expected/" + testMethodId).listFiles(BATCH_FILTER))
		{
			compareOutputDirToExpectedDir(file.getPath(), file.getName());
		}
	}
}
