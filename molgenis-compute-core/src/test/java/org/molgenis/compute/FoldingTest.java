package org.molgenis.compute;

import org.testng.annotations.Test;

public class FoldingTest extends ScriptComparator
{
	@Test
	public void testFoldingAssign() throws Exception
	{
		System.out.println("--- Start Test Folding 1---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/testfolding/workflow.csv", "--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv", "--rundir", OUTPUT_DIRECTORY, "--runid",
				"testFoldingAssign" });

		testOutputDirectoryFiles("testFoldingAssign");
	}

	@Test
	public void testFoldingAssign2ParametersFiles() throws Exception
	{
		System.out.println("--- Start Test Folding 2---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/testfolding/workflow2.csv", "--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv", "--parameters",
				"src/main/resources/workflows/testfolding/parameters2.csv", "--rundir", OUTPUT_DIRECTORY, "--runid", "testFoldingAssign2ParametersFiles"});

		testOutputDirectoryFiles("testFoldingAssign2ParametersFiles");
	}

	@Test
	public void testFoldingWeaving() throws Exception
	{
		System.out.println("--- Start Test Folding 3---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/testfolding/workflow.csv", "--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv", "--weave", "--rundir", OUTPUT_DIRECTORY, "--runid", "testFoldingWeaving"});

		testOutputDirectoryFiles("testFoldingWeaving");
	}
}
