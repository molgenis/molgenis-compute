package org.molgenis.compute5.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.molgenis.compute5.model.Parameters;
import org.molgenis.data.csv.CsvWriter;
import org.molgenis.data.support.MapEntity;

/** Generates graphvis diagram */
public class DocTotalParametersCsvGenerator
{
	public void generate(File file, Parameters parameters) throws IOException
	{
		try
		{
			file.getParentFile().mkdirs();
			List<MapEntity> values = parameters.getValues();

			CsvWriter w = new CsvWriter(new FileWriter(file));


			for (int i = 0; i < values.size(); i++)
			{

			}
			
			w.close();
			
			System.out.println("Generated "+file.getAbsolutePath());
		}
		catch (Exception e)
		{
			throw new IOException("Failed to write all parameters to "+file+": " + e.getMessage());
		}
	}
}
