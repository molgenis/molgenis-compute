package org.molgenis.compute.db.cloudexecutor;

/**
 * Created by hvbyelas on 5/19/14.
 */
public class CloudCurlBuilder
{
	private String curlStartedTemplate = "curl -s -S -u ${apiuser}:${api:pass} -F jobid=${jobid} -F host=`hostname`" +
			" -F status=started -F backend=${backend} http://${IP}:8080/api/cloud";

	private String curlFinishedTemplate = "curl -s -S -u ${apiuser}:${api:pass} -F jobid=${jobid} -F host=`hostname`" +
			" -F status=started -F backend=${backend} " +
			"-F log_file=log.log" +
			"http://${IP}:8080/api/cloud";


	private static String buildScript()
	{
		return null;
	}
}
