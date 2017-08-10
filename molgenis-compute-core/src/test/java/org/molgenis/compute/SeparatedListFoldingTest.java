package org.molgenis.compute;

import org.testng.annotations.Test;

public class SeparatedListFoldingTest extends ScriptComparator
{
	private String runID;
	
	@Test
	public void testFoldOnTwoStrings() throws Exception
	{
		String runID="testFoldOnTwoStrings";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/foreachtest/workflow.csv",
				"--parameters", "src/main/resources/workflows/foreachtest/parameters.csv",
				"--parameters", "src/main/resources/workflows/foreachtest/parameters1.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	/**
	 * Test shows how to fold all parameters.
	 */ 
	@Test
	public void testParametersAll() throws Exception
	{
		String runID="testParametersAll";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/listOutOfTwo/workflow.csv",
				"--parameters", "src/main/resources/workflows/listOutOfTwo/parametersall.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	/**
	 * Test shows how to iterate over two lists without creating of all possible combination of parameters
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParameters2() throws Exception
	{
		String runID="testParameters2";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--weave",
				"--workflow", "src/main/resources/workflows/listOutOfTwo/workflow2.csv",
				"--parameters", "src/main/resources/workflows/listOutOfTwo/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	/**
	 * Test shows how to fold all parameters from two parameter files.
	 */ 
	@Test
	public void testParametersAllInTwoFiles() throws Exception
	{
		String runID="testParametersAllInTwoFiles";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/listOutOfTwo/workflow.csv",
				"--parameters", "src/main/resources/workflows/listOutOfTwo/parameters.csv",
				"--parameters", "src/main/resources/workflows/listOutOfTwo/parameters1.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}
}
