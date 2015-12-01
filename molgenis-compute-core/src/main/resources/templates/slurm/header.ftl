#!/bin/bash
#SBATCH --job-name=${project}_${taskId}
#SBATCH --output=${taskId}.out
#SBATCH --error=${taskId}.err
#SBATCH --partition=${queue}
#SBATCH --time=${walltime}
#SBATCH --cpus-per-task ${ppn}
#SBATCH --mem-per-cpu ${mem}
#SBATCH --nodes ${nodes}
#SBATCH --open-mode=append

ENVIRONMENT_DIR="."
set -e
set -u
#-%j

errorExit()
{
    if [ "${errorAddr}" = "none" ]; then
        echo "mail is not specified"
        exit 1
    fi

    if [ ! -f errorMessageSent.flag ]; then
        echo "script $0 from directory $(pwd) reports failure" | mail -s "ERROR OCCURS" ${errorAddr}
        touch errorMessageSent.flag
    fi
    exit 1
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


trap "errorExit" ERR

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

touch ${taskId}.sh.started
