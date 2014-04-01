package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.ApplicationUtil;
import org.molgenis.util.Ssh;
import org.molgenis.util.SshResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionHost extends Ssh
{

    public static final String CREATE_PILOT_DIR = "mkdir maverick";

    public static final String CORRECT_GLITE_RESPOND = "glite-wms-job-submit Success";
	private static final Logger LOG = Logger.getLogger(ExecutionHost.class);

	private DataService dataService = null;

	public ExecutionHost(DataService dataService, String host, String user, String password, int port) throws IOException

	{
		super(host, user, password, port);
		this.dataService = dataService;
		LOG.info("... " + host + " is started");
	}

	public void submitPilot(ComputeRun computeRun, String command,
                            String pilotID, String sh, String jdl, MolgenisUser owner) throws IOException
	{

		SshResult mkdir = executeCommand(CREATE_PILOT_DIR);

        LOG.info("Transferring file maverick" + pilotID + ".jdl ...");

        uploadStringToFile(jdl, "$HOME/maverick/maverick" + pilotID +".jdl");

        LOG.info("Transferring file maverick" + pilotID + ".sh ...");

        uploadStringToFile(sh, "$HOME/maverick/maverick" + pilotID +".sh");

        LOG.info("Executing command [" + command + "] ...");

		ComputeBackend computeBackend = computeRun.getComputeBackend();

        boolean success = false;

		//while (!success)
        {
            SshResult result = executeCommand(command);
            if (!"".equals(result.getStdErr()))
            {
				System.out.println(">>>>>>>>>>>>>>>>>> PILOT_ID: " + pilotID);
				System.out.println(result.getStdErr());
            }

            String sOut = result.getStdOut();
            LOG.info("Command StdOut result:\n" + sOut);

            if(sOut.contains(CORRECT_GLITE_RESPOND))
			{
                success = true;

                    Iterable<ComputeBackend> computeBackends = dataService.findAll(ComputeBackend.ENTITY_NAME, new QueryImpl().eq(ComputeBackend.NAME, computeBackend.getName()), ComputeBackend.class);

					//List<ComputeBackend> computeBackends = dataService.query(ComputeBackend.class).equals(ComputeBackend.NAME, computeBackend.getName()).find();

					Iterator itBackend = computeBackends.iterator();

                    if(!itBackend.hasNext())
						LOG.error("No backend found for BACKENDNAME [" + computeBackend.getName() + "]");


				Iterable<ComputeRun> computeRuns = dataService.findAll(ComputeRun.ENTITY_NAME, new QueryImpl().eq(ComputeRun.NAME, computeRun.getName()), ComputeRun.class);

				Iterator<ComputeRun> itComputeRun = computeRuns.iterator();

					ComputeRun run = null;
					if(itComputeRun.hasNext())
					{
						run = itComputeRun.next();
						int numberOfSubmittedPilots = run.getPilotsSubmitted();
						run.setPilotsSubmitted(numberOfSubmittedPilots + 1);
						dataService.update(ComputeRun.ENTITY_NAME, run);
					}
					else
						LOG.error("No compute run found [" + computeRun.getName() + "] to submit pilot job");

				Iterable<MolgenisUser> owners = dataService.findAll(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, owner.getUsername()), MolgenisUser.class);
				Iterator itUsers = owners.iterator();


                    if(!itUsers.hasNext())
                        LOG.error("No molgenis user found [" + SecurityUtils.getCurrentUsername() + "] to submit pilot job");

                    Pilot pilot = new Pilot();
                    pilot.setValue(pilotID);
                    pilot.setBackend((ComputeBackend) itBackend.next());
                    pilot.setStatus(MolgenisPilotService.PILOT_SUBMITTED);
                    pilot.setOwner((MolgenisUser) itUsers.next());
					pilot.setComputeRun(run);

                    dataService.add(Pilot.ENTITY_NAME, pilot);

            }
			else
			{
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					LOG.error("Interrupted exception while sleeping", e);
				}
			}
        }

        LOG.info("Removing maverick" + pilotID + ".jdl ...");
        executeCommand("rm maverick/maverick" + pilotID +".jdl" );

        LOG.info("Removing maverick" + pilotID + ".sh ...");
        executeCommand("rm maverick/maverick" + pilotID +".sh" );

    }

}
