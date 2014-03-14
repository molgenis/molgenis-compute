package org.molgenis.compute.db.decorators;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;


public class ComputeRunDecorator<E extends ComputeRun> extends CrudRepositoryDecorator<E>
{
	public ComputeRunDecorator(CrudRepository<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public void add(Iterable<E> entities)
	{
		validate(entities);
		super.add(entities);
	}

	@Override
	public void update(Iterable<E> entities)
	{
		validate(entities);
		super.update(entities);
	}

	private void validate(Iterable<E> entities)
	{
		for (ComputeRun run : entities)
		{
			if (StringUtils.isEmpty(run.getName()) || !StringUtils.isAlphanumeric(run.getName()))
			{
				throw new ComputeDbException("Illegal run name [" + run.getName() + "] should be non empty alphnumeric");
			}

			if ((run.getPollDelay() != null) && (run.getPollDelay() < 2000))
			{
				throw new ComputeDbException("Illegal pollDelay value. Should be bigger than 2000ms");
			}
		}

	}

}
