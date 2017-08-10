package org.molgenis.compute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComputeCommandLineTest extends ScriptComparator
{
	private String runID;
	
	@Test
	public void testHelp()
	{
		String runID="testHelp";
		System.out.println("--- Started test " + runID + " ---");
		
		try
		{
			ComputeCommandLine.main(new String[]
			{ "-h" });
		}
		catch (Exception e)
		{
			Assert.fail("compute -h does not work");
		}

	}

	@Test
	public void testCreate()
	{
		String runID="testCreate";
		System.out.println("--- Started test " + runID + " ---");
		
		try
		{
			ComputeCommandLine.main(new String[]
			{ "--create", OUTPUT_DIRECTORY + runID});
		}
		catch (Exception e)
		{
			Assert.fail("--create does not work");
		}

		File file = new File(OUTPUT_DIRECTORY + runID + "/parameters.csv");
		if (!file.exists())
		{
			Assert.fail("workflow is not generated correctly");
		}

	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameter() throws Exception
	{
		String runID="testDoubleParameter";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters1.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterProperties() throws Exception
	{
		runID="testDoubleParameterProperties";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters_properties.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterPropertiesList() throws Exception
	{
		runID="testDoubleParameterPropertiesList";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters", "src/main/resources/workflows/doubleparameter/parameters_properties_list.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	@Test
	public void testClear()
	{
		runID="testClear";
		System.out.println("--- Started test " + runID + " ---");
		
		try
		{
			ComputeCommandLine.main(new String[]
			{ "--clear" });

			File f = new File(".compute.properties");

			if (f.exists())
			{
				Assert.fail(".compute.properties is not deleted");
			}

		}
		catch (Exception e)
		{
			Assert.fail("compute --clear does not work");
		}
	}

	@Test
	public void testRunID() throws Exception
	{
		String runID="testRunID";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.withrunid.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunID5() throws Exception
	{
		String runID="testRunID5";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.runid.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	/*
	 * Temporarily disable batch test until batching is reimplemented.
	 */
	@Test(enabled = false)
	public void testBatch() throws Exception
	{
		String runID="testBatch";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "-weave", "-b", "pbs", "-batch", "chr=2",
				"--workflow",   "src/main/resources/workflows/batchesWorkflow/workflow.csv",
				"--parameters", "src/main/resources/workflows/batchesWorkflow/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testTwoQueues() throws Exception
	{
		String runID="testTwoQueues";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "-weave", "-b", "pbs",
				"--workflow",   "src/main/resources/workflows/twoQueues/workflow.csv",
				"--parameters", "src/main/resources/workflows/twoQueues/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testParameters3Levels() throws Exception
	{
		String runID="testParameters3Levels";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.3levels.properties",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testParameters3Levels1() throws Exception
	{
		String runID="testParameters3Levels1";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.3levels1.properties",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally() throws Exception
	{
		String runID="testRunLocally";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID,
				"-header", "src/main/resources/workflows/benchmark/header.ftl",
				"-footer", "src/main/resources/workflows/benchmark/footer.ftl" });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5() throws Exception
	{
		String runID="testRunLocally5";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID,
				"-header", "src/main/resources/workflows/benchmark.5.1/header.ftl",
				"-footer", "src/main/resources/workflows/benchmark.5.1/footer.ftl",
				"-o", "\"worksheet=lala\"" });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testExtraVariable() throws Exception
	{
		String runID="testExtraVariable";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.extra.variable.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testUseHeadersFooters() throws Exception
	{
		String runID="testUseCustomHeadersFooters";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID,
				"-header", "src/main/resources/workflows/benchmark.5.1/header.ftl",
				"-footer", "src/main/resources/workflows/benchmark.5.1/footer.ftl" });

		testOutputDirectoryFiles(runID);

	}

	@Test
	public void testRunLocally5TemplatesOut() throws Exception
	{
		String runID="testRunLocally5TemplatesOut";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", 
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5_1_a() throws Exception
	{
		String runID="testRunLocally5_1_a";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", 
				"--workflow",   "src/main/resources/workflows/benchmark.5.1.a/workflow.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1.a/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5_2parametersFiles() throws Exception
	{
		String runID="testRunLocally5_2parametersFiles";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5a() throws Exception
	{
		String runID="testRunLocally5a";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.a.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.a.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5b_1() throws Exception
	{
		String runID="testRunLocally5b_1";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.b.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5b_runtime_automapping() throws Exception
	{
		String runID="testRunLocally5b_runtime_automapping";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", 
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.runtime.automapping.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5_underscore() throws Exception
	{
		String runID="testRunLocally5_underscore";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow._.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters_.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5b_2() throws Exception
	{
		String runID="testRunLocally5b_2";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.b.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocally5c() throws Exception
	{
		String runID="testRunLocally5c";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run", 
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.c.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.c.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testRunLocallyA() throws Exception
	{
		String runID="testRunLocallyA";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--run",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.3.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.3.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testGenerate5PBS() throws Exception
	{
		String runID="testGenerate5PBS";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test
	public void testGenerate5SLURM() throws Exception
	{
		String runID="testGenerate5SLURM";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "slurm",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv", 
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", 
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv", 
				"--parameters", "src/main/resources/workflows/benchmark.5.1/sysparameters.csv", 
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	/*
	 * Disabled test for script generating scripts that send email when somthing goes wrong: 
	 * although most scheduler implementations do support emailing when a job enters a
	 * specified state, this may easily result in a spam flood, when a silly bug causes 
	 * thousands of generated scripts to fail in a split second: not something we want to support.
	 */
	@Test(enabled = false)
	public void testGenerate5ErrorMail() throws Exception
	{
		String runID="testGenerate5ErrorMail";
		System.out.println("--- Started test " + runID + " ---");
	
		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "slurm",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/sysparameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID,
				"--errorAddr", "testMail@testServer" });
	
		testOutputDirectoryFiles(runID);
	}

	@Test(expectedExceptions = Exception.class)
	public void testGenerateUnknownBackend() throws Exception
	{
		String runID="testGenerateUnknownBackend";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs2",
				"--workflow",   "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters", "src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterNames() throws Exception
	{
		String runID="testDoubleParameterNames";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults1.csv",
				"--parameters", "src/main/resources/workflows/benchmark/wrong_parameters1.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"-header", "src/main/resources/workflows/benchmark/header.ftl",
				"-footer", "src/main/resources/workflows/benchmark/footer.ftl" });
	}

	@Test
	public void testHeaderPBS() throws Exception
	{
		String runID="testHeaderPBS";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs",
				"--workflow",   "src/main/resources/workflows/benchmark/workflowa.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.pbs.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID,
				"--runid", runID });

		testOutputDirectoryFiles(runID);
	}

	@Test(expectedExceptions = Exception.class)
	public void testMissingParameter() throws Exception
	{
		String runID="testMissingParameter";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.missingparameter.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	@Test(expectedExceptions = Exception.class)
	public void testParametersMissingValue() throws Exception
	{
		String runID="testParametersMissingValue";
		System.out.println("--- Started test " + runID + " ---");

		ComputeCommandLine.main(new String[]
		{ "--generate", "--backend", "pbs", "--database", "none",
				"--workflow",   "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",   "src/main/resources/workflows/benchmark/workflow.defaults.missingvalue.csv",
				"--parameters", "src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir", OUTPUT_DIRECTORY + runID });
	}

	public static String getFileAsString(String filename) throws IOException
	{
		File file = new File(filename);

		if (!file.exists())
		{
			Assert.fail("file does not exist");
		}
		final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		final byte[] bytes = new byte[(int) file.length()];
		bis.read(bytes);
		bis.close();
		return new String(bytes);
	}

}
