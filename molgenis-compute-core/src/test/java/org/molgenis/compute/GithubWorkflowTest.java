package org.molgenis.compute;

import org.testng.annotations.Test;

public class GithubWorkflowTest extends ScriptComparator
{
	private String runID;
	private String workflowRoot = "https://github.com/molgenis/molgenis-compute/tree/master/"
			+ "molgenis-compute-core/src/main/resources/workflows/benchmark.5.1";

	@Test
	public void readWorkflowGithub() throws Exception
	{
		String runID="readWorkflowGithub";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--web", workflowRoot,
				"--workflow",   "workflow.csv",
				"--defaults",   "workflow.defaults.csv",
				"--parameters", "parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID,
				"-header", "header.ftl",
				"-footer", "footer.ftl" });

		testOutputDirectoryFiles(runID);
	}
}
