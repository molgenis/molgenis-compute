package org.molgenis.compute;

import org.testng.annotations.Test;

public class GithubWorkflowTest extends ScriptComparator
{
	private String workflowRoot = "https://github.com/molgenis/molgenis-compute/tree/master/"
			+ "molgenis-compute-core/src/main/resources/workflows/benchmark.5.1";

	@Test
	public void readWorkflowGithub() throws Exception
	{
		System.out.println("--- Workflow Execution from Github ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--web", workflowRoot, "--workflow", "workflow.csv", "--defaults",
				"workflow.defaults.csv", "--parameters", "parameters.csv", "--rundir", OUTPUT_DIRECTORY, "--database", "none",
				"-header", "header.ftl", "-footer", "footer.ftl", "--runid", "readWorkflowGithub"});

		testOutputDirectoryFiles("readWorkflowGithub");
	}
}
