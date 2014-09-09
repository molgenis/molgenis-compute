#!/bin/bash
set -e

errorExit()
{
    curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
    -F status=failed -F backend=${backend} -F log_file=@log.log http://${IP}:${PORT}/api/cloud

     exit 1
}

cleanEverything()
{
    rm -f *
    touch attachedStorage.flag
    exit 0
}

trap "cleanEverything" EXIT
trap "errorExit" ERR

cd /storage

curl -s -S -u ${apiuser}:${apipass} -F jobid=${jobid} -F serverid=${serverid} \
-F status=started -F backend=${backend} http://${IP}:${PORT}/api/cloud