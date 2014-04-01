package org.molgenis.compute5;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 7/16/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FoldingTest
{
	private String outputDir = "target/test/benchmark/run";

	@Test
	public void testFoldingAssign() throws Exception
	{
		System.out.println("--- Start Test Folding 1---");

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
				"src/main/resources/workflows/testfolding/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/test1_0.sh");
		if (!file.exists())
		{
			Assert.fail("test1_0.sh is not generated");
		}

		file = new File(outputDir + "/test1_4.sh");
		if (!file.exists())
		{
			Assert.fail("test1_5.sh is not generated");
		}

		file = new File(outputDir + "/test2_1.sh");
		if (!file.exists())
		{
			Assert.fail("test2_1.sh is not generated");
		}

		file = new File(outputDir + "/test2_2.sh");
		if (file.exists())
		{
			Assert.fail("test2_2.sh should not be generated");
		}

		file = new File(outputDir + "/test3_2.sh");
		if (!file.exists())
		{
			Assert.fail("test3_2.sh is not generated");
		}

		file = new File(outputDir + "/test3_3.sh");
		if (file.exists())
		{
			Assert.fail("test3_3.sh should not be generated");
		}

		file = new File(outputDir + "/test4_1.sh");
		if (!file.exists())
		{
			Assert.fail("test4_1.sh is not generated");
		}

		file = new File(outputDir + "/test4_2.sh");
		if (file.exists())
		{
			Assert.fail("test4_2.sh should not be generated");
		}

		file = new File(outputDir + "/test5_1.sh");
		if (!file.exists())
		{
			Assert.fail("test5_1.sh is not generated");
		}

		file = new File(outputDir + "/test5_2.sh");
		if (file.exists())
		{
			Assert.fail("test5_2.sh should not be generated");
		}

		file = new File(outputDir + "/test6_1.sh");
		if (!file.exists())
		{
			Assert.fail("test6_1.sh is not generated");
		}

		file = new File(outputDir + "/test6_2.sh");
		if (file.exists())
		{
			Assert.fail("test6_2.sh should not be generated");
		}


		System.out.println("--- Test Lists Correctness ---");

		//this conditions can be change later, when compute will weave parameters directly

		String test2_0_list1 = "chunk[0]=\"a\"\n" +
				"chunk[1]=\"b\"\n" +
				"chunk[2]=\"c\"\n";
		String test2_0_list2 ="chr=\"1\"";

		String test2_1_list1 = "chunk[0]=\"a\"\n" +
				"chunk[1]=\"b\"\n";
		String test2_1_list2 ="chr=\"2\"";

		String test3_0_list1 = "chunk=\"a\"";
		String test3_0_list2 ="chr[0]=\"1\"\n" +
				"chr[1]=\"2\"";

		String test3_1_list1 = "chunk=\"b\"";
		String test3_1_list2 = "chr[0]=\"1\"\n" +
				"chr[1]=\"2\"";

		String test3_2_list1 = "chunk=\"c\"\n";
		String test3_2_list2 =	"chr[0]=\"1\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");

		if(!t.contains(test2_0_list1) || !t.contains(test2_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test2_1_list1) || !t.contains(test2_1_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_0.sh");
		if(!t.contains(test3_0_list1) || !t.contains(test3_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_1.sh");

		if(!t.contains(test3_1_list1) || !t.contains(test3_1_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test3_2.sh");
		if(!t.contains(test3_2_list1) || !t.contains(test3_2_list2))
		{
			Assert.fail("folding broken");
		}

		System.out.println("Test concatination with run-time parameters");

		String test_6_0_list1 = "runtime_concat[0]=${test1__has__outputLALA[0]}";
		String test_6_0_list2 = "runtime_concat[1]=${test1__has__outputLALA[1]}";
		String test_6_0_list3 = "runtime_concat[2]=${test1__has__outputLALA[2]}";
		String test_6_1_list1 = "runtime_concat[0]=${test1__has__outputLALA[3]}";
		String test_6_1_list2 = "runtime_concat[1]=${test1__has__outputLALA[4]}";
		String test_6_list = "for s in \"${runtime_concat[@]}\"";

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test6_0.sh");
		if(!t.contains(test_6_0_list1) ||
				!t.contains(test_6_0_list2) ||
				!t.contains(test_6_0_list3) ||
				!t.contains(test_6_list))
		{
			Assert.fail("concatination of run-time parameters is broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test6_1.sh");
		if(!t.contains(test_6_1_list1) ||
				!t.contains(test_6_1_list2) ||
				!t.contains(test_6_list))
		{
			Assert.fail("concatination of run-time parameters is broken");
		}


	}

	@Test
	public void testFoldingAssign2ParametersFiles() throws Exception
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
				"src/main/resources/workflows/testfolding/workflow2.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters2.csv",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/test1_0.sh");

		file = new File(outputDir + "/test2_1.sh");
		if (!file.exists())
		{
			Assert.fail("test2_1.sh is not generated");
		}

		file = new File(outputDir + "/test2_2.sh");
		if (file.exists())
		{
			Assert.fail("test2_2.sh should not be generated");
		}

		System.out.println("--- Test Lists Correctness ---");

		//this conditions can be change later, when compute will weave parameters directly

		String test2_0_list1 = "chunk[0]=\"a\"\n" +
				"chunk[1]=\"b\"\n" +
				"chunk[2]=\"c\"\n";
		String test2_0_list2 ="chr=\"1\"";

		String test2_1_list1 = "chunk[0]=\"a\"\n" +
				"chunk[1]=\"b\"\n";
		String test2_1_list2 ="chr=\"2\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");

		if(!t.contains(test2_0_list1) || !t.contains(test2_0_list2))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test2_1_list1) || !t.contains(test2_1_list2))
		{
			Assert.fail("folding broken");
		}

		System.out.println("Test concatination with run-time parameters");
	}

	@Test
	public void testFoldingAssign2doubleParametersFiles() throws Exception
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
				"src/main/resources/workflows/testfolding/workflow3.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters2.csv",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/test1_0.sh");

		file = new File(outputDir + "/test2_1.sh");
		if (!file.exists())
		{
			Assert.fail("test2_1.sh is not generated");
		}

		file = new File(outputDir + "/test2_2.sh");
		if (file.exists())
		{
			Assert.fail("test2_2.sh should not be generated");
		}

		System.out.println("--- Test Lists Correctness ---");

		//this conditions can be change later, when compute will weave parameters directly

		String test2_0_list1 = "just[0]=\"1\"\n" +
				"just[1]=\"2\"\n" +
				"just[2]=\"1\"\n" +
				"just[3]=\"2\"\n" +
				"just[4]=\"1\"\n" +
				"just[5]=\"2\"";

		String test2_0_list2 ="chunk[0]=\"a\"\n" +
				"chunk[1]=\"a\"\n" +
				"chunk[2]=\"b\"\n" +
				"chunk[3]=\"b\"\n" +
				"chunk[4]=\"c\"\n" +
				"chunk[5]=\"c\"";

		String test2_0_list3 ="chr=\"1\"";

		String test2_1_list1 = "just[0]=\"1\"\n" +
				"just[1]=\"2\"\n" +
				"just[2]=\"1\"\n" +
				"just[3]=\"2\"";

		String test2_1_list2 ="chunk[0]=\"a\"\n" +
				"chunk[1]=\"a\"\n" +
				"chunk[2]=\"b\"\n" +
				"chunk[3]=\"b\"";

		String test2_1_list3 ="chr=\"2\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");

		if(!t.contains(test2_0_list1) || !t.contains(test2_0_list2) || !t.contains(test2_0_list3))
		{
			Assert.fail("folding broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test2_1_list1) || !t.contains(test2_1_list2) || !t.contains(test2_1_list3))
		{
			Assert.fail("folding broken");
		}

		System.out.println("Test concatination with run-time parameters");
	}




	@Test
	public void testFoldingWeaving() throws Exception
	{
		System.out.println("--- Start Test Folding 1---");

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
				"src/main/resources/workflows/testfolding/workflow.csv",
				"--parameters",
				"src/main/resources/workflows/testfolding/parameters.csv",
				"--weave",
				"--rundir",
				outputDir
		});

		System.out.println("--- Test Created Files ---");

		File file = new File(outputDir + "/test1_0.sh");
		if (!file.exists())
		{
			Assert.fail("test1_0.sh is not generated");
		}

		file = new File(outputDir + "/test1_4.sh");
		if (!file.exists())
		{
			Assert.fail("test1_5.sh is not generated");
		}

		file = new File(outputDir + "/test2_1.sh");
		if (!file.exists())
		{
			Assert.fail("test2_1.sh is not generated");
		}

		file = new File(outputDir + "/test2_2.sh");
		if (file.exists())
		{
			Assert.fail("test2_2.sh should not be generated");
		}

		file = new File(outputDir + "/test3_2.sh");
		if (!file.exists())
		{
			Assert.fail("test3_2.sh is not generated");
		}

		file = new File(outputDir + "/test3_3.sh");
		if (file.exists())
		{
			Assert.fail("test3_3.sh should not be generated");
		}

		file = new File(outputDir + "/test4_1.sh");
		if (!file.exists())
		{
			Assert.fail("test4_1.sh is not generated");
		}

		file = new File(outputDir + "/test4_2.sh");
		if (file.exists())
		{
			Assert.fail("test4_2.sh should not be generated");
		}

		file = new File(outputDir + "/test5_1.sh");
		if (!file.exists())
		{
			Assert.fail("test5_1.sh is not generated");
		}

		file = new File(outputDir + "/test5_2.sh");
		if (file.exists())
		{
			Assert.fail("test5_2.sh should not be generated");
		}

		file = new File(outputDir + "/test6_1.sh");
		if (!file.exists())
		{
			Assert.fail("test6_1.sh is not generated");
		}

		file = new File(outputDir + "/test6_2.sh");
		if (file.exists())
		{
			Assert.fail("test6_2.sh should not be generated");
		}


		System.out.println("--- Test Lists Correctness ---");

//		left for old way testing
//		String test2_0_list1 = "chunk[0]=${global_chunk[0]}\n" +
//				"chunk[1]=${global_chunk[1]}\n" +
//				"chunk[2]=${global_chunk[2]}\n";
//		String test2_0_list2 ="chr[0]=${global_chr[0]}\n" +
//				"chr[1]=${global_chr[1]}\n" +
//				"chr[2]=${global_chr[2]}";
//
//		String test2_1_list1 = "chunk[0]=${global_chunk[3]}\n" +
//				"chunk[1]=${global_chunk[4]}\n";
//		String test2_1_list2 ="chr[0]=${global_chr[3]}\n" +
//				"chr[1]=${global_chr[4]}";
//
//		String test3_0_list1 = "chunk[0]=${global_chunk[0]}\n" +
//				"chunk[1]=${global_chunk[3]}\n";
//		String test3_0_list2 ="chr[0]=${global_chr[0]}\n" +
//				"chr[1]=${global_chr[3]}";
//
//		String test3_1_list1 = "chunk[0]=${global_chunk[1]}\n" +
//				"chunk[1]=${global_chunk[4]}\n";
//		String test3_1_list2 = "chr[0]=${global_chr[1]}\n" +
//				"chr[1]=${global_chr[4]}";
//
//		String test3_2_list1 = "chunk[0]=${global_chunk[2]}\n";
//		String test3_2_list2 =	"chr[0]=${global_chr[2]}";

		String test_weaving_2_0 = "for s in \"a\" \"b\" \"c\"";
		String test_weaving_2_1 = "for s in \"a\" \"b\"";

		String t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");

//		We do not weave parameters now
		System.out.println("Test Weaving Correctness");

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_0.sh");
		if(!t.contains(test_weaving_2_0))
		{
			Assert.fail("weaving is broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test2_1.sh");
		if(!t.contains(test_weaving_2_1))
		{
			Assert.fail("weaving is broken");
		}

		System.out.println("Test concatination with run-time parameters");

		String test_6_0_list1 = "runtime_concat[0]=${test1__has__outputLALA[0]}";
		String test_6_0_list2 = "runtime_concat[1]=${test1__has__outputLALA[1]}";
		String test_6_0_list3 = "runtime_concat[2]=${test1__has__outputLALA[2]}";
		String test_6_1_list1 = "runtime_concat[0]=${test1__has__outputLALA[3]}";
		String test_6_1_list2 = "runtime_concat[1]=${test1__has__outputLALA[4]}";
		String test_6_list = "for s in \"${runtime_concat[@]}\"";

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test6_0.sh");
		if(!t.contains(test_6_0_list1) ||
				!t.contains(test_6_0_list2) ||
				!t.contains(test_6_0_list3) ||
				!t.contains(test_6_list))
		{
			Assert.fail("concatination of run-time parameters is broken");
		}

		t = ComputeCommandLineTest.getFileAsString(outputDir + "/test6_1.sh");
		if(!t.contains(test_6_1_list1) ||
				!t.contains(test_6_1_list2) ||
				!t.contains(test_6_list))
		{
			Assert.fail("concatination of run-time parameters is broken");
		}


	}


}
