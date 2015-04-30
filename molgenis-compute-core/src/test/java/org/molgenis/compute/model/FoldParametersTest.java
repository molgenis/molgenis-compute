package org.molgenis.compute.model;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.molgenis.compute.model.impl.FoldParametersImpl;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class FoldParametersTest
{
	private FoldParametersImpl foldParameters;
	Map<String, List<String>> parametersInFile = ImmutableMap.<String, List<String>> of("project",
			ImmutableList.<String> of("project1", "project1", "project1", "project2"), "dir",
			ImmutableList.<String> of("dir1", "dir2", "dir2", "dir2"), "sample",
			ImmutableList.<String> of("sample1", "sample2", "sample3", "sample4"));

	@Test
	public void testFold()
	{
		foldParameters = new FoldParametersImpl(Arrays.<Map<String, List<String>>> asList(parametersInFile));
		List<String> folded = foldParameters.getFilteredParameterValues("sample", ImmutableMap.of("dir", "dir1", "project", "project1"));
		assertEquals(folded, Arrays.asList("sample1"));
	}

	@Test
	public void testFold2()
	{
		foldParameters = new FoldParametersImpl(Arrays.<Map<String, List<String>>> asList(parametersInFile));
		List<String> folded = foldParameters.getFilteredParameterValues("dir", ImmutableMap.of("dir", "dir1", "project", "project1"));
		assertEquals(folded, Arrays.asList("dir1"));
	}

	@Test
	public void testFoldNonExistingCombination()
	{
		foldParameters = new FoldParametersImpl(Arrays.<Map<String, List<String>>> asList(parametersInFile));
		List<String> folded = foldParameters.getFilteredParameterValues("sample", ImmutableMap.of("dir", "dir1", "project", "project2"));
		assertEquals(folded, Arrays.asList());
	}

	@Test
	public void testFold4()
	{
		foldParameters = new FoldParametersImpl(Arrays.<Map<String, List<String>>> asList(parametersInFile));
		List<String> folded = foldParameters.getFilteredParameterValues("sample", ImmutableMap.of("dir", "dir2", "project", "project1"));
		assertEquals(folded, Arrays.asList("sample2", "sample3"));
	}
}
