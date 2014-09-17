echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, test1 finished successfully"
echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, test1 finished successfully" >> molgenis.bookkeeping.log

touch ${taskId}.sh.finished

curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
-F status=finished -F backend=${backend} -F log_file=@log.log http://${IP}:${PORT}/api/cloud
