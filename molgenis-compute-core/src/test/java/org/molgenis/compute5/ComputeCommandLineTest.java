package org.molgenis.compute5;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.TestNG;

import org.testng.annotations.Test;

public class ComputeCommandLineTest
{
	private String outputDir = "target/test/benchmark/run";

	@Test
	public void testHelp()
	{
		try
		{
			ComputeCommandLine.main(new String[]{"-h"});
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
			ComputeCommandLine.main(new String[]{"--create", outputDir});
		}
		catch (Exception e)
		{
			Assert.fail("--create does not work");
		}

		File file = new File(outputDir + "/parameters.csv");
		if (!file.exists())
		{
			Assert.fail("workflow is not generated correctly");
		}

	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameter() throws Exception
	{
		//shows nieuwe folding
		System.out.println("--- Start Test Folding ---");

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
				"src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters1.csv",
				"--rundir",
				outputDir
		});
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterProperties() throws Exception
	{
		//shows nieuwe folding
		System.out.println("--- Start Test Folding ---");

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
				"src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters_properties.csv",
				"--rundir",
				outputDir
		});
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterPropertiesList() throws Exception
	{
		//shows nieuwe folding
		System.out.println("--- Start Test Folding ---");

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
				"src/main/resources/workflows/doubleparameter/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/doubleparameter/parameters_properties_list.csv",
				"--rundir",
				outputDir
		});
	}


	@Test
	public void testClear()
	{
		try
		{
			ComputeCommandLine.main(new String[]{"--clear"});

			File f = new File(".compute.properties");

			if(f.exists())
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

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters","src/main/resources/workflows/benchmark/parameters.withrunid.csv",
				"--rundir",outputDir,
				"--run",
				"--database","none",
				"--runid", "test3"

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

	@Test
	public void testRunID5() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters","src/main/resources/workflows/benchmark.5.1/parameters.runid.csv",
				"--rundir",outputDir,
				"--run",
				"--database","none",
				"--runid", "test1"

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

    @Test
    public void testBatch() throws Exception
    {
        System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

        File f = new File(outputDir);
        FileUtils.deleteDirectory(f);
        Assert.assertFalse(f.exists());

        ComputeCommandLine.main(new String[]{
                "--generate", "--workflow", "src/main/resources/workflows/batchesWorkflow/workflow.csv",
                "--parameters","src/main/resources/workflows/batchesWorkflow/parameters.csv",
                "--rundir",outputDir,
                "--runid",
                "test1",
                "-weave",
                "-b",
                "pbs",
                "-batch",
                "chr=2"

        });

        System.out.println("--- Test Created Files ---");

        File file = new File(outputDir + "/batch0/step1_0.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step1_1.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step1_2.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step1_3.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step1_4.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step2_0.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch0/step2_1.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch1/step1_5.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch1/step1_6.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch1/step2_2.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }

        file = new File(outputDir + "/batch1/step2_3.sh");
        if (!file.exists())
        {
            Assert.fail("wrong batch construction");
        }
    }

    @Test
    public void testTwoQueues() throws Exception
    {
        System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

        File f = new File(outputDir);
        FileUtils.deleteDirectory(f);
        Assert.assertFalse(f.exists());

        String superQueue = "#PBS -q super";
        String queue = "#PBS -q default";

        ComputeCommandLine.main(new String[]{
                "--generate", "--workflow", "src/main/resources/workflows/twoQueues/workflow.csv",
                "--parameters","src/main/resources/workflows/twoQueues/parameters.csv",
                "--rundir",outputDir,
                "--runid",
                "test1",
                "-weave",
                "-b",
                "pbs"

        });

        String text =getFileAsString(outputDir + "/step1_0.sh");

        if(!text.contains(superQueue))
        {
            Assert.fail("wrong queue substitution");
        }

        text =getFileAsString(outputDir + "/step2_0.sh");

        if(!text.contains(queue))
        {
            Assert.fail("wrong default queue");
        }

    }


    @Test
	public void testParameters3Levels() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters","src/main/resources/workflows/benchmark/parameters.3levels.properties",
				"--rundir",outputDir,
				"--run",
				"--database","none",
				"--runid", "test1"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step1_0.sh.started");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh.started is not generated");
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

		file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}
	}

	@Test
	public void testParameters3Levels1() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--run", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters","src/main/resources/workflows/benchmark/parameters.3levels1.properties",
				"--rundir",outputDir,
				"--run",
				"--database","none",
				"--runid", "test1"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step1_0.sh.started");
		if (!file.exists())
		{
			Assert.fail("step1_0.sh.started is not generated");
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

		file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}
	}

	@Test
	public void testRunLocally() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--database",
				"none",
				"-header",
				"src/main/resources/workflows/benchmark/header.ftl",
				"-footer",
				"src/main/resources/workflows/benchmark/footer.ftl"
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

	@Test
	public void testRunLocally5() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--database",
				"none",
				"-header",
				"src/main/resources/workflows/benchmark.5.1/header.ftl",
				"-footer",
				"src/main/resources/workflows/benchmark.5.1/footer.ftl",
				"-o",
				"\"worksheet=lala\""

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

	@Test
	public void testExtraVariable() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.extra.variable.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--database",
				"none"
		});
	}


	@Test
	public void testReadSpecificHeadersFooters() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		String footer = "# My own custom footer";
		String header = "# My own custom header";

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--database",
				"none",
				"-header",
				"src/main/resources/workflows/benchmark.5.1/header.ftl",
				"-footer",
				"src/main/resources/workflows/benchmark.5.1/footer.ftl"
		});

		System.out.println("--- Test header is added ---");

		String text =getFileAsString(outputDir + "/step1_0.sh");

		if(!text.contains(footer) || !text.contains(header))
		{
			Assert.fail("header/footer insertion fails");
		}

	}


	@Test
	public void testRunLocally5TemplatesOut() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--database",
				"none"
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


	@Test
	public void testRunLocally5_1_a() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1.a/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1.a/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"
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


	@Test
	public void testRunLocally5_2parametersFiles() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"

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

	@Test
	public void testRunLocally5a() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.a.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.a.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step3_0.sh");
		if (!file.exists())
		{
			Assert.fail("step3_0.sh.started is not generated");
		}

		file = new File(outputDir + "/step3_1.sh");
		if (file.exists())
		{
			Assert.fail("step3_1.sh should not be generated");
		}

	}

	@Test
	public void testRunLocally5b_1() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.b.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}

	}

	@Test
	public void testRunLocally5b_runtime_automapping() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow.runtime.automapping.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}

	}

	@Test
	public void testRunLocally5_underscore() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark.5.1/workflow._.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters_.csv",
				"--rundir",
				"target/test/benchmark/run"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step2_0.sh.finished");
		if (!file.exists())
		{
			Assert.fail("step2_0.sh.finished is not generated");
		}

	}



//	@Test(expectedExceptions = Exception.class)
//	public void testRunLocally5b_2() throws Exception
//	{
//		System.out.println("--- Start TestRunLocally ---");
//
//		File f = new File(outputDir);
//		FileUtils.deleteDirectory(f);
//		Assert.assertFalse(f.exists());
//
//		f = new File(".compute.properties");
//		FileUtils.deleteQuietly(f);
//		Assert.assertFalse(f.exists());
//
//		ComputeCommandLine.main(new String[]{
//				"--generate",
//				"--run",
//				"--workflow",
//				"src/main/resources/workflows/benchmark.5.1/workflow.b.csv",
//				"--defaults",
//				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv",
//				"--parameters",
//				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
//				"--rundir",
//				"target/test/benchmark/run"
//		});
//	}
//
//	@Test(expectedExceptions = Exception.class)
//	public void testRunLocally5c() throws Exception
//	{
//		System.out.println("--- Start TestRunLocally ---");
//
//		File f = new File(outputDir);
//		FileUtils.deleteDirectory(f);
//		Assert.assertFalse(f.exists());
//
//		f = new File(".compute.properties");
//		FileUtils.deleteQuietly(f);
//		Assert.assertFalse(f.exists());
//
//		ComputeCommandLine.main(new String[]{
//				"--generate",
//				"--run",
//				"--workflow",
//				"src/main/resources/workflows/benchmark.5.1/workflow.c.csv",
//				"--defaults",
//				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.b.csv",
//				"--parameters",
//				"src/main/resources/workflows/benchmark.5.1/parameters.c.csv",
//				"--rundir",
//				"target/test/benchmark/run"
//		});
//	}

	@Test
	public void testRunLocallyA() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

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
				"src/main/resources/workflows/benchmark/workflow.3.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.3.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir",
				"target/test/benchmark/run"
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/step3_0.sh");
		if (!file.exists())
		{
			Assert.fail("step3_0.sh is not generated");
		}

		file = new File(outputDir + "/step3_1.sh");
		if (file.exists())
		{
			Assert.fail("step3_1.sh should not be generated");
		}

	}

    @Test
    public void testNumbering() throws Exception {
        System.out.println("--- Start TestRunLocally ---");

        File f = new File(outputDir);
        FileUtils.deleteDirectory(f);
        Assert.assertFalse(f.exists());

        f = new File(".compute.properties");
        FileUtils.deleteQuietly(f);
        Assert.assertFalse(f.exists());

        ComputeCommandLine.main(new String[]{
                "--generate",
                "--workflow",
                "src/main/resources/workflows/numbering/workflow.csv",
                "--parameters",
                "src/main/resources/workflows/numbering/parameters.csv",
                "--rundir",
                "target/test/benchmark/run",
                "--backend", "pbs", "--weave"
        });

        File file = new File(outputDir + "/01_step0_0.sh");
        if (!file.exists())
        {
            Assert.fail("01_step0_0.sh is not generated");
        }

        file = new File(outputDir + "/01_step0_1.sh");
        if (!file.exists())
        {
            Assert.fail("01_step0_1.sh is not generated");
        }

        file = new File(outputDir + "/02_step1_0.sh");
        if (!file.exists())
        {
            Assert.fail("02_step1_0.sh is not generated");
        }

        file = new File(outputDir + "/02_step1_1.sh");
        if (!file.exists())
        {
            Assert.fail("02_step1_1.sh is not generated");
        }

        file = new File(outputDir + "/03_step2_0.sh");
        if (!file.exists())
        {
            Assert.fail("03_step2_0.sh is not generated");
        }

        file = new File(outputDir + "/03_step2_1.sh");
        if (file.exists())
        {
            Assert.fail("03_step2_1.sh should not be generated");
        }

        String script = getFileAsString(outputDir + "/03_step2_0.sh");

        if(!script.contains("input=\"map\""))
        {
            Assert.fail("Parameters mapping is not correct");
        }

        if(!script.contains("strings[0]=${02_step1__has__out[0]}"))
        {
            Assert.fail("Run-time parameters are not correct");
        }

        String submitDependencies = "dependencies=\"-W depend=afterok\"\n" +
                "\t\tif [[ -n \"$01_step0_1\" ]]; then\n" +
                "\t\t\tdependenciesExist=true\n" +
                "\t\t\tdependencies=\"${dependencies}:$01_step0_1\"\n" +
                "\t\tfi\n" +
                "\t\tif [[ -n \"$01_step0_0\" ]]; then\n" +
                "\t\t\tdependenciesExist=true\n" +
                "\t\t\tdependencies=\"${dependencies}:$01_step0_0\"\n" +
                "\t\tfi\n" +
                "\t\tif [[ -n \"$02_step1_0\" ]]; then\n" +
                "\t\t\tdependenciesExist=true\n" +
                "\t\t\tdependencies=\"${dependencies}:$02_step1_0\"\n" +
                "\t\tfi\n" +
                "\t\tif [[ -n \"$02_step1_1\" ]]; then\n" +
                "\t\t\tdependenciesExist=true\n" +
                "\t\t\tdependencies=\"${dependencies}:$02_step1_1\"\n" +
                "\t\tfi\n";

        script = getFileAsString(outputDir + "/submit.sh");

        if(!script.contains(submitDependencies))
        {
            Assert.fail("Submit is not correct");
        }
    }


	@Test
	public void testGenerate5PBS() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--backend","pbs"
		});

		String script = getFileAsString(outputDir + "/submit.sh");

		String stepDependencies = "#\n" +
				"##step2_0\n" +
				"#\n" +
				"\n" +
				"# Skip this step if step finished already successfully\n" +
				"if [ -f step2_0.sh.finished ]; then\n" +
				"\tskip step2_0.sh\n" +
				"\techo \"Skipped step2_0.sh\"\t\n" +
				"else\n" +
				"\t# Build dependency string\n" +
				"\tdependenciesExist=false\n" +
				"\tdependencies=\"-W depend=afterok\"\n" +
				"\t\tif [[ -n \"$step1_1\" ]]; then\n" +
				"\t\t\tdependenciesExist=true\n" +
				"\t\t\tdependencies=\"${dependencies}:$step1_1\"\n" +
				"\t\tfi\n" +
				"\t\tif [[ -n \"$step1_0\" ]]; then\n" +
				"\t\t\tdependenciesExist=true\n" +
				"\t\t\tdependencies=\"${dependencies}:$step1_0\"\n" +
				"\t\tfi\n" +
				"\tif ! $dependenciesExist; then\n" +
				"\t\tunset dependencies\n" +
				"\tfi\n" +
				"\n" +
				"\tid=step2_0\n" +
				"\tstep2_0=$(qsub -N step2_0 $dependencies step2_0.sh)\n" +
				"\techo \"$id:$step2_0\"\n" +
				"\tsleep 0\n" +
				"fi";

		if(!script.contains(stepDependencies))
		{
			Assert.fail("PBS dependencies is not generated correctly");
		}

	}

	@Test
	public void testGenerate5SLURM() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

        ComputeCommandLine.main(new String[]{
                "--generate",
                "--workflow",
                "src/main/resources/workflows/benchmark.5.1/workflow.csv",
                "--defaults",
                "src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
                "--parameters",
                "src/main/resources/workflows/benchmark.5.1/parameters.csv",
                "--parameters",
                "src/main/resources/workflows/benchmark.5.1/sysparameters.csv",
                "--rundir",
                "target/test/benchmark/run",
                "--backend","slurm"
        });

		String script = getFileAsString(outputDir + "/submit.sh");

		String stepDependencies = "# Skip this step if step finished already successfully\n" +
				"if [ -f step2_0.sh.finished ]; then\n" +
				"skip step2_0.sh\n" +
				"echo \"Skipped step2_0.sh\"\n" +
				"else\n" +
				"# Build dependency string\n" +
				"dependenciesExist=false\n" +
				"dependencies=\"--dependency=afterok\"\n" +
				"    if [[ -n \"$step1_1\" ]]; then\n" +
				"    dependenciesExist=true\n" +
				"    dependencies=\"${dependencies}:$step1_1\"\n" +
				"    fi\n" +
				"    if [[ -n \"$step1_0\" ]]; then\n" +
				"    dependenciesExist=true\n" +
				"    dependencies=\"${dependencies}:$step1_0\"\n" +
				"    fi\n" +
				"if ! $dependenciesExist; then\n" +
				"unset dependencies\n" +
				"fi\n" +
				"output=$(sbatch $dependencies step2_0.sh)\n" +
				"id=step2_0\n" +
				"step2_0=${output##\"Submitted batch job \"}\n" +
				"echo \"$id:$step2_0\"\n" +
				"fi\n";

		if(!script.contains(stepDependencies))
		{
			Assert.fail("SLURM dependencies is not generated correctly");
		}

	}

	@Test
	public void testGenerate5ErrorMail() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/sysparameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--backend","slurm",
				"--errorAddr", "testMail@testServer"
		});

		String script = getFileAsString(outputDir + "/step0_0.sh");

		String mailAddress = "testMail@testServer";

		if(!script.contains(mailAddress))
		{
			Assert.fail("Sending error message is not generated correctly");
		}

	}


	@Test(expectedExceptions = Exception.class)
	public void testGenerateUnknownBackend() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark.5.1/workflow.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark.5.1/workflow.defaults.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark.5.1/parameters.csv",
				"--rundir",
				"target/test/benchmark/run",
				"--backend","pbs2"
		});
	}

	@Test(expectedExceptions = Exception.class)
	public void testDoubleParameterNames() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersComputePropertiesFilesCreated ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults1.csv",
				"--parameters","src/main/resources/workflows/benchmark/wrong_parameters1.csv",
				"--rundir",outputDir,
				"--backend","pbs",
				"--database","none",
				"-header", "src/main/resources/workflows/benchmark/header.ftl",
				"-footer", "src/main/resources/workflows/benchmark/footer.ftl"
		});
	}

	@Test
	public void testHeaderPBS() throws Exception
	{
		System.out.println("--- Start TestRunLocally ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate",
				"--workflow",
				"src/main/resources/workflows/benchmark/workflowa.csv",
				"--defaults",
				"src/main/resources/workflows/benchmark/workflow.defaults.pbs.csv",
				"--parameters",
				"src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir",
				outputDir,
				"--backend","pbs"
		});

		String script = getFileAsString(outputDir + "/step1_0.sh");

		if(!script.contains("05:59:00"))
		{
			Assert.fail("header is not created correctly");
		}

		script = getFileAsString(outputDir + "/step2_0.sh");

		if(!script.contains("00:59:00"))
		{
			Assert.fail("header is not created correctly");
		}

	}

	@Test(expectedExceptions = Exception.class)
	public void testMissingParameter() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersMissingParameter ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.missingparameter.csv",
				"--parameters","src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir",outputDir,
				"--backend","pbs",
				"--database","none"
		});
	}

	@Test(expectedExceptions = Exception.class)
	public void testParametersMissingValue() throws Exception
	{
		System.out.println("--- Start TestCommandLineParametersMissingValue ---");

		File f = new File(outputDir);
		FileUtils.deleteDirectory(f);
		Assert.assertFalse(f.exists());

		f = new File(".compute.properties");
		FileUtils.deleteQuietly(f);
		Assert.assertFalse(f.exists());

		ComputeCommandLine.main(new String[]{
				"--generate", "--workflow", "src/main/resources/workflows/benchmark/workflow.csv",
				"--defaults", "src/main/resources/workflows/benchmark/workflow.defaults.missingvalue.csv",
				"--parameters","src/main/resources/workflows/benchmark/parameters.csv",
				"--rundir",outputDir,
				"--backend","pbs",
				"--database","none"
		});
	}


	public static String getFileAsString(String filename) throws IOException
	{
		File file = new File(filename);

		if (!file.exists())
		{
			Assert.fail("file does not exist");
		}
		final BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(file));
		final byte[] bytes = new byte[(int) file.length()];
		bis.read(bytes);
		bis.close();
		return new String(bytes);
	}

}
