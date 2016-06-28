#!/bin/bash
#SBATCH --job-name=${project}_${taskId}
#SBATCH --output=${taskId}.out
#SBATCH --error=${taskId}.err
#SBATCH --partition=${queue}
#SBATCH --time=${walltime}
#SBATCH --cpus-per-task ${ppn}
#SBATCH --mem ${mem}
#SBATCH --nodes ${nodes}
#SBATCH --open-mode=append
#SBATCH --export=NONE
#SBATCH --get-user-env=L

ENVIRONMENT_DIR="."
set -e
set -u
#-%j

function errorExitandCleanUp()
{
        echo "TRAPPED"
	failedFile="/groups/${groupname}/${tmpName}/logs/${project}.pipeline.failed"
	printf "${taskId}\n" > <#noparse>${failedFile}</#noparse>
	if [ -f ${taskId}.err ]
	then
		printf "Last 50 lines of ${taskId}.err :\n" >> <#noparse>${failedFile}</#noparse>
		tail -50 ${taskId}.err >> <#noparse>${failedFile}</#noparse>
		printf "\nLast 50 lines of ${taskId}.out: \n" >> <#noparse>${failedFile}</#noparse>
		tail -50 ${taskId}.out >> <#noparse>${failedFile}</#noparse>
	fi
	rm -rf /groups/${groupname}/${tmpName}/tmp/${project}/*/tmp_${taskId}*
}

declare MC_tmpFolder="tmpFolder"
declare MC_tmpFile="tmpFile"

function makeTmpDir {
        base=$(basename $1)
        dir=$(dirname $1)
        echo "dir $dir"
        echo "base $base"
        if [[ -d $1 ]]
        then
            	dir=$dir/$base
        fi
	myMD5=$(md5sum $0)
        IFS=' ' read -a myMD5array <<< "$myMD5"
        MC_tmpFolder=$dir/tmp_${taskId}_$myMD5array/
        mkdir -p $MC_tmpFolder
        if [[ -d $1 ]]
        then
            	MC_tmpFile="$MC_tmpFolder"
        else
            	MC_tmpFile="$MC_tmpFolder/$base"
        fi
}

trap "errorExitandCleanUp" HUP INT QUIT TERM EXIT ERR

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

touch ${taskId}.sh.started
if [ -f Timesheet.txt ]
then
DATE=`date +%Y-%m-%d`
	printf "\n\n${DATE}" >> Timesheet.txt
fi
SECONDS=0
