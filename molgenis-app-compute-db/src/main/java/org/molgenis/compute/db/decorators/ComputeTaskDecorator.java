package org.molgenis.compute.db.decorators;

import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Automatically adds a new entry to the ComputeTaskHisory if the statuscode changed
 * 
 * @author erwin
 * 
 */
public class ComputeTaskDecorator<E extends ComputeTask> extends CrudRepositoryDecorator<E>
{

	public ComputeTaskDecorator(CrudRepository<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public void add(Iterable<E> entities)
	{
		super.add(entities);

		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);

		for (ComputeTask task : entities)
		{
			ComputeTaskHistory history = new ComputeTaskHistory();
			history.setComputeTask(task);
			history.setStatusTime(new Date());
			history.setNewStatusCode(task.getStatusCode());
			dataService.add(ComputeTaskHistory.ENTITY_NAME, history);
		}

	}

	@Override
	public void update(Iterable<E> entities)
	{
		DataService dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);

		for (ComputeTask task : entities)
			{
				ComputeTask old = dataService.findOne(ComputeTask.ENTITY_NAME,
						new QueryImpl().eq(ComputeTask.NAME, task.getName()));

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
