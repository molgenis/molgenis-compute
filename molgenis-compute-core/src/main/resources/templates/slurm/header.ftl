#!/bin/bash
#SBATCH --job-name=${taskId}
#SBATCH --output=${taskId}-%j.out
#SBATCH --error=${taskId}-%j.err
#SBATCH --partition=${queue}
#SBATCH --time=${walltime}
#SBATCH --cpus-per-task ${ppn}
#SBATCH --mem-per-cpu ${mem}
#SBATCH --nodes ${nodes}

ENVIRONMENT_DIR="."

set -e

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

trap "errorExit" ERR

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

touch ${taskId}.sh.started
