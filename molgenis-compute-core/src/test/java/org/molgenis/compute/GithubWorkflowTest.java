package org.molgenis.compute;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute.ComputeCommandLine;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 11/6/13
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GithubWorkflowTest
{
	private String outputDir = "target/test/benchmark/run";

	private String workflowRoot = "https://github.com/molgenis/molgenis-compute/tree/master/" +
			"molgenis-compute-core/src/main/resources/workflows/benchmark.5.1";

	private String rundir = "target/test/benchmark/run";

	@Test
	public void readWorkflowGithub() throws Exception
	{
		System.out.println("--- Workflow Execution from Github ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--run",
				"--web", workflowRoot,
				"--workflow",
				"workflow.csv",
				"--defaults",
				"workflow.defaults.csv",
				"--parameters",
				"parameters.csv",
				"--rundir",
				rundir,
				"--database",
				"none",
				"-header",
				"header.ftl",
				"-footer",
				"footer.ftl"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step1_0.sh.started");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh.started is not generated");
		}

		file = new File(outputDir + "/step1_1.sh.started");
		if (!file.exists())
		{
			Assert.fail("step1_1.sh.started is not generated");
		}

		file = new File(outputDir + "/step2_0.sh.started");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.started is not generated");
		}

		file = new File(outputDir + "/step1_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh.finished is not generated");
		}

		file = new File(outputDir + "/step1_1.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step1_1.sh.finished is not generated");
		}

		file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}


	}
}
