package org.molgenis.compute.db.decorators;

import java.util.Date;

import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.ApplicationContextProvider;

/**
 * Automatically adds a new entry to the ComputeTaskHisory if the statuscode changed
 * 
 * @author erwin
 * 
 */
public class ComputeTaskDecorator extends CrudRepositoryDecorator
{

	public ComputeTaskDecorator(CrudRepository generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		super.add(entities);

		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);

		for (Entity e : entities)
		{
			ComputeTask task = (ComputeTask) e;
			ComputeTaskHistory history = new ComputeTaskHistory();
			history.setComputeTask(task);
			history.setStatusTime(new Date());
			history.setNewStatusCode(task.getStatusCode());
			dataService.add(ComputeTaskHistory.ENTITY_NAME, history);
		}

	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);

		for (Entity e: entities)
			{
				ComputeTask task = (ComputeTask) e;
				ComputeTask old = dataService.findOne(ComputeTask.ENTITY_NAME,
						new QueryImpl().eq(ComputeTask.NAME, task.getName()), ComputeTask.class);

				if ((old != null) && !old.getStatusCode().equalsIgnoreCase(task.getStatusCode()))
				{
					ComputeTaskHistory history = new ComputeTaskHistory();
					history.setComputeTask(task);
					history.setRunLog(task.getRunLog());
					history.setStatusTime(new Date());
					history.setStatusCode(old.getStatusCode());
					history.setNewStatusCode(task.getStatusCode());

					dataService.add(ComputeTaskHistory.ENTITY_NAME, history);
				}

			}

		super.update(entities);
	}
}
