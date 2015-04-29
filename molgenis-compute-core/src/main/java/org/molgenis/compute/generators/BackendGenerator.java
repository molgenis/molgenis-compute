package org.molgenis.compute.generators;

import java.io.File;
import java.io.IOException;

import org.molgenis.compute.CommandLineRunContainer;
import org.molgenis.compute.model.Compute;

/**
 * This class generates parameters for different backends. These backends include PBS, SGE, and GRID, but could also
 * include other backends
 */
public interface BackendGenerator
{
	/**
	 * Generate a {@link CommandLineRunContainer}
	 * 
	 * @param compute
	 * @param targetDir
	 * @return A {@link CommandLineRunContainer}
	 * @throws IOException
	 */
	public CommandLineRunContainer generate(Compute compute, File targetDir) throws IOException;
}
