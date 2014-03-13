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
public class ForEachTest
{
	private String outputDir = "target/test/benchmark/run";

	@Test
	public void testWithoutForEach() throws Exception
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

		System.out.println("--- Test Without For Each ---");
	}

}
