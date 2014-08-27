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

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

touch ${taskId}.sh.started
