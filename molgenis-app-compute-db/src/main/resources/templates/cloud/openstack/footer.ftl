curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
-F status=finished -F backend=${backend} -F log_file=@log.log http://${IP}:${PORT}/api/cloud
