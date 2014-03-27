package org.molgenis.compute.db.executor;

import org.molgenis.compute.db.pilot.MolgenisPilotService;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: hvbyelas
 * Date: 6/14/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class PilotManager
{
	@Autowired
	private DataService dataService;

	@RunAsSystem
	public void checkExperiredPilots()
	{
		Iterable<Pilot> pilots = dataService.findAll(Pilot.ENTITY_NAME,
				new QueryImpl().eq(Pilot.STATUS, MolgenisPilotService.PILOT_SUBMITTED), Pilot.class);


		for (Pilot pilot : pilots)
		{
			Calendar calendar = Calendar.getInstance();
			long now = calendar.getTimeInMillis();
			long creationTime = pilot.getCreationTime().getTime();

			long difference = now - creationTime;
			long lifeTerm = pilot.getLifeTerm() * 60 * 1000; //in milliseconds

			if (difference > lifeTerm)
			{
				pilot.setStatus(MolgenisPilotService.PILOT_EXPIRED);
				dataService.update(Pilot.ENTITY_NAME, pilot);
			}
		}
	}
}
