
curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=`hostname` \
-F status=finished -F backend=${backend} out_log_file=@${jobname}.out err_log_file=@${jobname}.err \
http://${IP}:${PORT}/api/cluster
