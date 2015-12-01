package org.molgenis.compute;

import org.testng.annotations.Test;

public class SeparatedListFoldingTest extends ScriptComparator
{
	@Test
	public void testFoldOnTwoString() throws Exception
	{
		System.out.println("--- Start Test Folding on two strings ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/foreachtest/workflow.csv", "--parameters",
				"src/main/resources/workflows/foreachtest/parameters.csv", "--parameters",
				"src/main/resources/workflows/foreachtest/parameters1.csv", "--rundir", OUTPUT_DIRECTORY, "--runid",
				"testFoldOnTwoString" });

		testOutputDirectoryFiles("testFoldOnTwoString");
	}

	@Test
	public void testParametersAll() throws Exception
	{
		System.out.println("--- Start Test folding all parameters ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/listOutOfTwo/workflow.csv", "--parameters",
				"src/main/resources/workflows/listOutOfTwo/parametersall.csv", "--rundir", OUTPUT_DIRECTORY, "--runid",
				"testParametersAll" });

		testOutputDirectoryFiles("testParametersAll");
	}

	/**
	 * Test shows how to iterate over two lists without creating of all possible combination of parameters
	 * 
	 * @throws Exception
	 */
	@Test
	public void testParameters2() throws Exception
	{
		System.out.println("--- Start Test Folding without all possible parameter combinations ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/listOutOfTwo/workflow2.csv",
				"--parameters", "src/main/resources/workflows/listOutOfTwo/parameters.csv", "--weave", "--rundir",
				OUTPUT_DIRECTORY, "--runid", "testParameters2" });

		testOutputDirectoryFiles("testParameters2");
	}

	@Test
	public void testParametersAllInTwoFiles() throws Exception
	{
		System.out.println("--- Start Test Folding all parameters from two files ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--workflow", "src/main/resources/workflows/listOutOfTwo/workflow.csv", "--parameters",
				"src/main/resources/workflows/listOutOfTwo/parameters.csv", "--parameters",
				"src/main/resources/workflows/listOutOfTwo/parameters1.csv", "--rundir", OUTPUT_DIRECTORY, "--runid", "testParametersAllInTwoFiles"});

		testOutputDirectoryFiles("testParametersAllInTwoFiles");
	}
}
