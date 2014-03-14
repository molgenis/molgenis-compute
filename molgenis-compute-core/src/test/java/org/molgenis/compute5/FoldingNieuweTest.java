package org.molgenis.compute5;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 7/16/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FoldingNieuweTest
{
	private String outputDir = "target/test/benchmark/run";

	@Test
	public void testFoldOnTwoString() throws Exception
	{
		System.out.println("--- Start Test Folding 1---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--run",
				"--workflow",
				"src/main/resources/workflows/foreachtest/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/foreachtest/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/foreachtest/parameters1.csv",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Folding on Two Strings ---");

		String test1_0_list1 = "chr[0]=\"1\"\n" +
				"chr[1]=\"2\"";
		String test1_0_list2 = "sample[0]=\"sample1\"";
		String test1_1_list1 = "chr[0]=\"1\"\n" +
				"chr[1]=\"2\"";
		String test1_1_list2 = "sample[0]=\"sample2\"\n" +
				"sample[1]=\"sample3\"";
		String test1_2_list1 = "chr[0]=\"1\"\n" +
				"chr[1]=\"2\"";
		String test1_2_list2 = "sample[0]=\"sample4\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test1_0.sh");

		if(!t.contains(test1_0_list1) || !t.contains(test1_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test1_1.sh");

		if(!t.contains(test1_1_list1) || !t.contains(test1_1_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test1_2.sh");

		if(!t.contains(test1_2_list1) || !t.contains(test1_2_list2))
		{
			Assert.fail("folding broken");
		}

	}

}
