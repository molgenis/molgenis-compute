touch ${taskId}.sh.finished

echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, task ${taskId} finished successfully" >> molgenis.bookkeeping.log
<#noparse>
if [ -d ${MC_tmpFolder:-} ];
        then
	echo "removed tmpFolder $MC_tmpFolder"
        rm -r $MC_tmpFolder
fi

trap - EXIT
exit 0
</#noparse>
