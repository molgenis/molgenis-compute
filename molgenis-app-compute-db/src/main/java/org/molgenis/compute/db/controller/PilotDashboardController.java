package org.molgenis.compute.db.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.elasticsearch.search.suggest.Suggest;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute5.db.api.RunStatus;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Start and stop pilots, show status
 * 
 * @author erwin
 */
@Controller
@RequestMapping(PilotDashboardController.URI)
public class PilotDashboardController extends MolgenisPluginController
{
	public static final String URI = "/plugin/dashboard";
	private static final String VIEW_NAME = "PilotDashboard";
	private final DataService database;
	private final RunService runService;
	private static final Logger LOG = Logger.getLogger(PilotDashboardController.class);


	@Autowired
	public PilotDashboardController(DataService database, RunService runService)
	{
		super(URI);
		
		this.database = database;
		this.runService = runService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		model.addAttribute("runs", getRunModels());
		return VIEW_NAME;
	}

	@RequestMapping("/start")
	public String start(@RequestParam("run")
	String runName, @RequestParam("username")
	String username, @RequestParam("password")
	String password, Model model) throws IOException
	{
		LOG.debug(">> In PilotDashboardController:start");
		runService.start(runName, username, password);
		return init(model);
	}

	@RequestMapping("/stop")
	public String stop(@RequestParam("run")
	String runName, Model model)
	{
		LOG.debug(">> In PilotDashboardController:stop");
		runService.stop(runName);
		return init(model);
	}

	@RequestMapping("/close")
	public String close(@RequestParam("run")
	String runName, Model model)
	{
		runService.removeFromDashboard(runName);
		return init(model);
	}

    @RequestMapping(value = "/activate", method = RequestMethod.POST)
    public String activate(@RequestParam("run")
    String runName, Model model)
    {
        runService.activate(runName);
        return init(model);
    }

    @RequestMapping(value = "/inactivate", method = RequestMethod.POST)
    public String inactivate(@RequestParam("run")
    String runName, Model model)
    {
        	runService.inactivate(runName);
        	return init(model);
    }

	@RequestMapping("/cancel")
	public String cancel(@RequestParam("run")
	String runName, Model model)
	{
		runService.cancel(runName);
		return init(model);
	}


	@RequestMapping("/resubmit")
	public String resubmitFailedTasks(@RequestParam("run")
	String runName, Model model)
	{
		int count = runService.resubmitFailedTasks(runName);
		model.addAttribute("message", "Resubmitted " + count + " failed tasks for '" + runName + "'");
		return init(model);
	}

	@RequestMapping(value = "/status", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RunStatus> status(@RequestParam("run")
	String runName)
	{
		RunStatus status = runService.getStatus(runName);
		return new ResponseEntity<RunStatus>(status, HttpStatus.OK);
	}

	@ExceptionHandler(ComputeDbException.class)
	public String showComputeDbException(ComputeDbException e, HttpServletRequest request)
	{
		request.setAttribute("runs", getRunModels());
		request.setAttribute("error", e.getMessage());

		return VIEW_NAME;
	}

	private List<RunModel> getRunModels()
	{
		List<RunModel> runModels = new ArrayList<RunModel>();

		Iterable<ComputeRun> runs = database.findAll(ComputeRun.ENTITY_NAME,
				new QueryImpl().eq(ComputeRun.SHOWINDASHBOARD, true).sort(new Sort("creationTime")), ComputeRun.class);

		String userLogin = SecurityUtils.getCurrentUsername();

		Iterator it = runs.iterator();
		while (it.hasNext())
		{
			ComputeRun run = (ComputeRun) it.next();
			String runOwner = run.getOwner().getUsername();
			boolean isSame = false;
			if(userLogin.equalsIgnoreCase(runOwner))
				isSame = true;

			runModels.add(new RunModel(run.getName(),
                    runService.isRunning(run.getName()),
                    runService.isSubmitting(run.getName()),
                    runService.isComplete(run.getName()),
					runService.isCancelled(run.getName()),
					isSame,
                    run.getComputeBackend().getBackendUrl(),
					run.getCreationTime(),
					runOwner));
		}

		return runModels;
	}
}
