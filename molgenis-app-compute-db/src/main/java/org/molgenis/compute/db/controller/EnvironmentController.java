package org.molgenis.compute.db.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Serves the user environment and task output environments of a run
 * 
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping(EnvironmentController.URI)
public class EnvironmentController
{
	public static final String URI = "/environment";
	private final DataService database;

	@Autowired
	public EnvironmentController(DataService database)
	{
		this.database = database;
	}

	@RequestMapping(value = "/{runName}/user.env", method = RequestMethod.GET)
	public void getUserEnv(@PathVariable("runName")
	String runName, HttpServletResponse response) throws IOException
	{


		Iterable<ComputeRun> runs = database.findAll(ComputeRun.ENTITY_NAME,
				new QueryImpl().eq(ComputeRun.NAME, runName), ComputeRun.class);
		for(ComputeRun run : runs)
		{
			PrintWriter pw = response.getWriter();
			pw.write(run.getUserEnvironment());
			pw.flush();
		}
	}

	@RequestMapping(value = "/{runName}/{taskName}.env", method = RequestMethod.GET)
	public void getOutputEnv(@PathVariable("runName")
	String runName, @PathVariable("taskName")
	String taskName, HttpServletResponse response) throws IOException
	{
		ComputeRun computeRun = database.findOne(ComputeRun.ENTITY_NAME, new QueryImpl().eq(ComputeRun.NAME, runName), ComputeRun.class);
		Iterable<ComputeTask> tasks = database.findAll(ComputeTask.ENTITY_NAME, new QueryImpl()
				.eq(ComputeTask.COMPUTERUN, computeRun).and()
				.eq(ComputeTask.NAME, taskName), ComputeTask.class);

		if (tasks.iterator().hasNext())
		{
			ComputeTask task = tasks.iterator().next();
			PrintWriter pw = response.getWriter();
			try
			{
				pw.write(task.getOutputEnvironment());
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}

	}
}
