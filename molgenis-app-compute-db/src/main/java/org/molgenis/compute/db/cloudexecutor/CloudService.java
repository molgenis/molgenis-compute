package org.molgenis.compute.db.cloudexecutor;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 5/7/14
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */


@Controller
@RequestMapping("/api/cloud")
public class CloudService
{
	private static final Logger LOG = Logger.getLogger(CloudService.class);

	public static final String STATUS_STARTED = "started";
	public static final String STATUS_FINISHED = "finished";


	@Autowired
	private DataService dataService;

	@Autowired
	private CloudManager cloudManager;

	@RunAsSystem
	@RequestMapping(method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public synchronized void analyseCloudCall(HttpServletRequest request,
											  @RequestParam String jobid,
											  @RequestParam String status,
											  @RequestParam String serverid,
											  @RequestParam String backend,
											  @RequestParam(required = false) Part log_file,
											  HttpServletResponse response
	) throws IOException
	{
		LOG.debug(">> In handleRequest!");
		if (status.equalsIgnoreCase(STATUS_STARTED))
		{
			int i = 0;
		}
		else if(status.equalsIgnoreCase(STATUS_FINISHED))
		{
			int i = 0;
		}
	}

}
