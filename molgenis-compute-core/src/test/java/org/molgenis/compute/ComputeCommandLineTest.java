package org.molgenis.compute;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ComputeCommandLineTest extends ScriptComparator
{
	@Test
	public void testHelp()
	{
		try
		{
			ComputeCommandLine.main(new String[] { "-h" });
		}
		catch (Exception e)
		{
			Assert.fail("compute -h does not work");
		}

	}

	@Test
	public void testCreate()
	{
		try
		{
			ComputeCommandLine.main(new String[] { "--create", OUTPUT_DIRECTORY });
		}
		catch (Exception e)
		{
			Assert.fail("--create does not work");
		}

		File file = new File(OUTPUT_DIRECTORY + "/parameters.csv");
		if (!file.exists())
		{
			Assert.fail("workflow is not generated correctly");
		}

	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameter() throws Exception
	{
		// shows new folding
		System.out.println("--- Start Test Folding ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/doubleparameter/workflow.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters1.csv", "--rundir", OUTPUT_DIRECTORY });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterProperties() throws Exception
	{
		// shows new folding
		System.out.println("--- Start Test Folding ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/doubleparameter/workflow.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters_properties.csv", "--rundir",
				OUTPUT_DIRECTORY });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterPropertiesList() throws Exception
	{
		// shows new folding
		System.out.println("--- Start Test Folding ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/doubleparameter/workflow.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv", "--parameters",
				"src/main/resources/workflows/doubleparameter/parameters_properties_list.csv", "--rundir",
				OUTPUT_DIRECTORY });
	}

	@Test
	public void testClear()
	{
		try
		{
			ComputeCommandLine.main(new String[] { "--clear" });

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
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark/parameters.withrunid.csv", "--rundir", OUTPUT_DIRECTORY,
				"--database", "none", "--runid", "test3"

		});

		testOutputDirectoryFiles("testRunID");
	}

	@Test
	public void testRunID5() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.runid.csv", "--rundir", OUTPUT_DIRECTORY,
				"--database", "none", "--runid", "test1" });

		testOutputDirectoryFiles("testRunID5");
	}

	//@Test
	public void testBatch() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/batchesWorkflow/workflow.csv", "--parameters",
				"src/main/resources/workflows/batchesWorkflow/parameters.csv", "--rundir", OUTPUT_DIRECTORY, "--runid",
				"test1", "-weave", "-b", "pbs", "-batch", "chr=2"

		});

		testOutputDirectoryFiles("testBatch");
	}

	@Test
	public void testTwoQueues() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine
				.main(new String[] { "--generate", "--workflow", "src/main/resources/workflows/twoQueues/workflow.csv",
						"--parameters", "src/main/resources/workflows/twoQueues/parameters.csv", "--rundir",
						OUTPUT_DIRECTORY, "--runid", "test1", "-weave", "-b", "pbs"

				});

		testOutputDirectoryFiles("testTwoQueues");
	}

	@Test
	public void testParameters3Levels() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark/parameters.3levels.properties", "--rundir", OUTPUT_DIRECTORY,
				"--run", "--database", "none", "--runid", "test1" });

		testOutputDirectoryFiles("testParameters3Levels");
	}

	@Test
	public void testParameters3Levels1() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark/parameters.3levels1.properties", "--rundir", OUTPUT_DIRECTORY,
				"--run", "--database", "none", "--runid", "test1" });

		testOutputDirectoryFiles("testParameters3Levels1");
	}

	@Test
	public void testRunLocally() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--database", "none", "-header", "src/main/resources/workflows/benchmark/header.ftl", "-footer",
				"src/main/resources/workflows/benchmark/footer.ftl", "--runid", "HVNk" });

		testOutputDirectoryFiles("testRunLocally");
	}

	@Test
	public void testRunLocally5() throws Exception
	{
		System.out.println("--- Start testRunLocally5 ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--database", "none", "-header", "src/main/resources/workflows/benchmark.5.1/header.ftl", "-footer",
				"src/main/resources/workflows/benchmark.5.1/footer.ftl", "-o", "\"worksheet=lala\"", "--runid",
				"wzE1" });

		testOutputDirectoryFiles("testRunLocally5");
	}

	@Test
	public void testExtraVariable() throws Exception
	{
		System.out.println("--- Start TestExtraVariable ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.extra.variable.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--database", "none", "--runid", "fvnC" });

		testOutputDirectoryFiles("testExtraVariable");
	}

	@Test
	public void testReadSpecificHeadersFooters() throws Exception
	{
		System.out.println("--- Start testReadSpecificHeadersFooters ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--database", "none", "-header", "src/main/resources/workflows/benchmark.5.1/header.ftl", "-footer",
				"src/main/resources/workflows/benchmark.5.1/footer.ftl", "--runid", "G3fl" });

		testOutputDirectoryFiles("testReadSpecificHeadersFooters");

	}

	@Test
	public void testRunLocally5TemplatesOut() throws Exception
	{
		System.out.println("--- Start testRunLocally5TemplatesOut ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--database", "none", "--runid", "llYW" });

		testOutputDirectoryFiles("testRunLocally5TemplatesOut");
	}

	@Test
	public void testRunLocally5_1_a() throws Exception
	{
		System.out.println("--- Start testRunLocally5_1_a ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1.a/workflow.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1.a/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "test5_1_a" });

		testOutputDirectoryFiles("testRunLocally5_1_a");
	}

	@Test
	public void testRunLocally5_2parametersFiles() throws Exception
	{
		System.out.println("--- Start testRunLocally5_2parametersFiles ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "test5_2parametersFiles"

		});

		testOutputDirectoryFiles("testRunLocally5_2parametersFiles");
	}

	@Test
	public void testRunLocally5a() throws Exception
	{
		System.out.println("--- Start testRunLocally5a ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.a.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.a.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocally5a" });

		testOutputDirectoryFiles("testRunLocally5a");
	}

	@Test
	public void testRunLocally5b_1() throws Exception
	{
		System.out.println("--- Start testRunLocally5b_1 ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.b.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testLocally5b_1" });

		testOutputDirectoryFiles("testRunLocally5b_1");
	}

	@Test
	public void testRunLocally5b_runtime_automapping() throws Exception
	{
		System.out.println("--- Start testRunLocally5b_runtime_automapping ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.runtime.automapping.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocally5b_runtime_automapping" });

		testOutputDirectoryFiles("testRunLocally5b_runtime_automapping");
	}

	@Test
	public void testRunLocally5_underscore() throws Exception
	{
		System.out.println("--- Start testRunLocally5_underscore ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow._.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters_.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocally5_underscore" });

		testOutputDirectoryFiles("testRunLocally5_underscore");
	}

	@Test
	public void testRunLocally5b_2() throws Exception
	{
		System.out.println("--- Start testRunLocally5b_2 ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.b.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocally5b_2" });

		testOutputDirectoryFiles("testRunLocally5b_2");
	}

	@Test
	public void testRunLocally5c() throws Exception
	{
		System.out.println("--- Start testRunLocally5c ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.c.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.c.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocally5c" });

		testOutputDirectoryFiles("testRunLocally5c");
	}

	@Test
	public void testRunLocallyA() throws Exception
	{
		System.out.println("--- Start testRunLocallyA ---");

		ComputeCommandLine.main(new String[] { "--generate", "--run", "--workflow",
				"src/main/resources/workflows/benchmark/workflow.3.csv", "--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.3.csv", "--parameters",
				"src/main/resources/workflows/benchmark/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--runid", "testRunLocallyA" });

		testOutputDirectoryFiles("testRunLocallyA");
	}

	@Test
	public void testGenerate5PBS() throws Exception
	{
		System.out.println("--- Start testGenerate5PBS ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--backend", "pbs", "--runid", "testGenerate5PBS" });

		testOutputDirectoryFiles("testGenerate5PBS");
	}

	@Test
	public void testGenerate5SLURM() throws Exception
	{
		System.out.println("--- Start testGenerate5SLURM ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/sysparameters.csv", "--rundir",
				"target/test/benchmark/run/", "--backend", "slurm", "--runid", "testGenerate5SLURM" });

		testOutputDirectoryFiles("testGenerate5SLURM");
	}

	//@Test
	//public void testGenerate5ErrorMail() throws Exception
	//{
	//	System.out.println("--- Start testGenerate5ErrorMail ---");
	//
	//	ComputeCommandLine.main(new String[]
	//	{ "--generate", "--workflow", "src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
	//			"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
	//			"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--parameters",
	//			"src/main/resources/workflows/benchmark.5.1/sysparameters.csv", "--rundir", "target/test/benchmark/run",
	//			"--backend", "slurm", "--errorAddr", "testMail@testServer", "--runid", "testGenerate5ErrorMail"});
	//
	//	testOutputDirectoryFiles("testGenerate5ErrorMail");
	//}

	@Test(expectedExceptions = Exception.class)
	public void testGenerateUnknownBackend() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		ComputeCommandLine.main(new String[] { "--generate", "--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv", "--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv", "--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv", "--rundir", "target/test/benchmark/run",
				"--backend", "pbs2" });
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterNames() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		ComputeCommandLine
				.main(new String[] { "--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults1.csv", "--parameters",
						"src/main/resources/workflows/benchmark/wrong_parameters1.csv", "--rundir", OUTPUT_DIRECTORY,
						"--backend", "pbs", "--database", "none", "-header",
						"src/main/resources/workflows/benchmark/header.ftl", "-footer",
						"src/main/resources/workflows/benchmark/footer.ftl" });
	}

	@Test
	public void testHeaderPBS() throws Exception
	{
		System.out.println("--- Start testHeaderPBS ---");

		ComputeCommandLine
				.main(new String[] { "--generate", "--workflow", "src/main/resources/workflows/benchmark/workflowa.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.pbs.csv",
						"--parameters", "src/main/resources/workflows/benchmark/parameters.csv", "--rundir",
						OUTPUT_DIRECTORY, "--backend", "pbs", "--runid", "testHeaderPBS" });

		testOutputDirectoryFiles("testHeaderPBS");
	}

	@Test(expectedExceptions = Exception.class)
	public void testMissingParameter() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersMissingParameter ---");

		ComputeCommandLine
				.main(new String[] { "--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.missingparameter.csv",
						"--parameters", "src/main/resources/workflows/benchmark/parameters.csv", "--rundir",
						OUTPUT_DIRECTORY, "--backend", "pbs", "--database", "none" });
	}

	@Test(expectedExceptions = Exception.class)
	public void testParametersMissingValue() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersMissingValue ---");

		ComputeCommandLine
				.main(new String[] { "--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
						"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.missingvalue.csv",
						"--parameters", "src/main/resources/workflows/benchmark/parameters.csv", "--rundir",
						OUTPUT_DIRECTORY, "--backend", "pbs", "--database", "none" });
	}

	@Test
	public void testGenerateSlurmJobs() throws Exception
	{
		System.out.println("--- Start testGenerateSlurmJobs ---");

		ComputeCommandLine.main(new String[] { "--generate", "--backend", "slurm", "-p",
				"src/main/resources/workflows/testGenerateSlurmJobs/samplesheetPhasing_test.csv", "-w",
				"src/main/resources/workflows/testGenerateSlurmJobs/workflow.csv", "-p",
				"src/main/resources/workflows/testGenerateSlurmJobs/parametersPhasing_test.converted.csv", "-p",
				"src/main/resources/workflows/testGenerateSlurmJobs/chromosome_chunks.csv", "--rundir",
				OUTPUT_DIRECTORY, "--weave", "--database", "none" });
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
