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
	printf "${taskId}\n" > /groups/${groupname}/${tmpName}/logs/${project}.failed
	if [ -f ${taskId}.err ]
	then
		printf "Last 50 lines of ${taskId}.err :\n" /groups/${groupname}/${tmpName}/logs/${project}.failed
		tail -50 ${taskId}.err >> /groups/${groupname}/${tmpName}/logs/${project}.failed
		printf "\nLast 50 lines of ${taskId}.out: \n"
		tail -50 ${taskId}.out >> /groups/${groupname}/${tmpName}/logs/${project}.failed
		
		if [ ! -f /groups/${groupname}/${tmpName}/logs/${project}.failed.mailed ] 
		then
			mailTo="helpdesk.gcc.groningen@gmail.com"
			if [ $groupname == "umcg-gaf" ]
			then
				echo "mailTo umcg-gaf"
				mailTo="helpdesk.gcc.groningen@gmail.com"
			elif [ "${groupname}" == "umcg-gd" ]
			then
				echo "mailTo is umcg-gd"
				if [ -f /groups/umcg-gd/${tmpName}/logs/mailinglistDiagnostiek.txt ]
				then
					mailTo=$(cat /groups/umcg-gd/${tmpName}/logs/mailinglistDiagnostiek.txt)
				else
					echo "mailingListDiagnostiek.txt bestaat niet!!"
					exit 0
				fi
			fi	
			cat /groups/${groupname}/${tmpName}/logs/${project}.failed | mail -s "The NGS_DNA pipeline has crashed for project ${project} on step ${taskId}" <#noparse>${mailTo}</#noparse>
			touch /groups/${groupname}/${tmpName}/logs/${project}.failed.mailed
		fi
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
