#!/bin/bash
set -e

errorExit()
{
    if [ -f log.log ];
    then
        curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
        -F status=failed -F backend=${backend} -F log_file=@log.log http://${IP}:${PORT}/api/cloud
    else
        curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
        -F status=failed -F backend=${backend} http://${IP}:${PORT}/api/cloud
    fi
    exit 1
}

cleanEverything()
{
    #rm -f *
    touch attachedStorage.flag
    exit 0
}

trap "cleanEverything" EXIT
trap "errorExit" ERR

cd /storage

ENVIRONMENT_DIR="."

curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
-F status=started -F backend=${backend} http://${IP}:${PORT}/api/cloud

touch $0.started

MOLGENIS_START=$(date +%s)
