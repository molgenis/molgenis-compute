curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=`hostname` \
-F status=started -F backend=${backend} http://${IP}:${PORT}/api/cluster
