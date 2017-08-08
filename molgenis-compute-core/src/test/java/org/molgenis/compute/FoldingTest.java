package org.molgenis.compute;

import org.testng.annotations.Test;

public class FoldingTest extends ScriptComparator
{
	private String runID;
	
	@Test
	public void testFoldingAssign() throws Exception
	{
		String runID="testFoldingAssign";
		System.out.println("--- Started test " + runID + " ---");
		
		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/testfolding/workflow.csv",
				"--parameters", "src/main/resources/workflows/testfolding/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testFoldingAssign2ParameterFiles() throws Exception
	{
		String runID="testFoldingAssign2ParameterFiles";
		System.out.println("--- Started test " + runID + " ---");
		
		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/testfolding/workflow2.csv",
				"--parameters", "src/main/resources/workflows/testfolding/parameters.csv",
				"--parameters", "src/main/resources/workflows/testfolding/parameters2.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testFoldingWeaving() throws Exception
	{
		String runID="testFoldingWeaving";
		System.out.println("--- Started test " + runID + " ---");
		
		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--weave",
				"--workflow",   "src/main/resources/workflows/testfolding/workflow.csv",
				"--parameters", "src/main/resources/workflows/testfolding/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}
}
