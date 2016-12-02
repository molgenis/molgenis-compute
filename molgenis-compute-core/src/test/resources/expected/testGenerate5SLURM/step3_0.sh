#!/bin/bash
#SBATCH --job-name=step3_0
#SBATCH --output=step3_0.out
#SBATCH --error=step3_0.err
#SBATCH --time=00:30:00
#SBATCH --cpus-per-task 4
#SBATCH --mem 500
#SBATCH --open-mode=append
#SBATCH --export=NONE
#SBATCH --get-user-env=30L

set -e
set -u

ENVIRONMENT_DIR='.'

#
# Variables declared in MOLGENIS Compute headers/footers always start with a MC_ prefix.
#
declare MC_jobScript="step3_0.sh"
declare MC_jobScriptSTDERR="step3_0.err"
declare MC_jobScriptSTDOUT="step3_0.out"
declare MC_failedFile="molgenis.pipeline.failed"

declare MC_singleSeperatorLine=$(head -c 120 /dev/zero | tr '\0' '-')
declare MC_doubleSeperatorLine=$(head -c 120 /dev/zero | tr '\0' '=')
declare MC_tmpFolder='tmpFolder'
declare MC_tmpFile='tmpFile'
declare MC_tmpFolderCreated=0

#
##
### Header functions.
##
#

function errorExitAndCleanUp() {
	local signal=${1}
	local problematicLine=${2}
	local exitStatus=${3:-$?}
	local executionHost=${SLURMD_NODENAME:-$(hostname)}
	local errorMessage="FATAL: Trapped ${signal} signal in ${MC_jobScript} running on ${executionHost}. Exit status code was ${exitStatus}."
	if [ $signal == 'ERR' ]; then
		local errorMessage="FATAL: Trapped ${signal} signal on line ${problematicLine} in ${MC_jobScript} running on ${executionHost}. Exit status code was ${exitStatus}."
	fi
	local errorMessage=${4:-"${errorMessage}"} # Optionally use custom error message as third argument.
	local format='INFO: Last 50 lines or less of %s:\n'
	echo "${errorMessage}"
	echo "${MC_doubleSeperatorLine}"                > ${MC_failedFile}
	echo "${errorMessage}"                         >> ${MC_failedFile}
	if [ -f "${MC_jobScriptSTDERR}" ]; then
		echo "${MC_singleSeperatorLine}"           >> ${MC_failedFile}
		printf "${format}" "${MC_jobScriptSTDERR}" >> ${MC_failedFile}
		echo "${MC_singleSeperatorLine}"           >> ${MC_failedFile}
		tail -50 "${MC_jobScriptSTDERR}"           >> ${MC_failedFile}
	fi
	if [ -f "${MC_jobScriptSTDOUT}" ]; then
		echo "${MC_singleSeperatorLine}"           >> ${MC_failedFile}
		printf "${format}" "${MC_jobScriptSTDOUT}" >> ${MC_failedFile}
		echo "${MC_singleSeperatorLine}"           >> ${MC_failedFile}
		tail -50 "${MC_jobScriptSTDOUT}"           >> ${MC_failedFile}
	fi
	echo "${MC_doubleSeperatorLine}"               >> ${MC_failedFile}
}

#
# Create tmp dir per script/job.
# To be called with with either a file or folder as first and only argument.
# Defines two globally set variables:
#  1. MC_tmpFolder: a tmp dir for this job/script. When function is called multiple times MC_tmpFolder will always be the same.
#  2. MC_tmpFile:   when the first argument was a folder, MC_tmpFile == MC_tmpFolder
#                   when the first argument was a file, MC_tmpFile will be a path to a tmp file inside MC_tmpFolder.
#
function makeTmpDir {
	#
	# Compile paths.
	#
	local originalPath=$1
	local myMD5=$(md5sum ${MC_jobScript})
	myMD5=${myMD5%% *} # remove everything after the first space character to keep only the MD5 checksum itself.
	local tmpSubFolder="tmp_${MC_jobScript}_${myMD5}"
	local dir
	local base
	if [[ -d "${originalPath}" ]]; then
		dir="${originalPath}"
		base=''
	else
		base=$(basename "${originalPath}")
		dir=$(dirname "${originalPath}")
	fi
	MC_tmpFolder="${dir}/${tmpSubFolder}/"
	MC_tmpFile="$MC_tmpFolder/${base}"
	echo "DEBUG ${MC_jobScript}::makeTmpDir: dir='${dir}';base='${base}';MC_tmpFile='${MC_tmpFile}'"
	#
	# Cleanup the previously created tmpFolder first if this script was resubmitted.
	#
	if [[ ${MC_tmpFolderCreated} -eq 0 && -d ${MC_tmpFolder} ]]; then
		rm -rf ${MC_tmpFolder}
	fi
	#
	# (Re-)create tmpFolder.
	#
	mkdir -p ${MC_tmpFolder}
	MC_tmpFolderCreated=1
}

trap 'errorExitAndCleanUp HUP  NA $?' HUP
trap 'errorExitAndCleanUp INT  NA $?' INT
trap 'errorExitAndCleanUp QUIT NA $?' QUIT
trap 'errorExitAndCleanUp TERM NA $?' TERM
trap 'errorExitAndCleanUp EXIT NA $?' EXIT
trap 'errorExitAndCleanUp ERR  $LINENO $?' ERR

touch ${MC_jobScript}.started



#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step3_0"

# Make compute.properties available
rundir="TEST_PROPERTY(project.basedir)/target/test/benchmark/run"
runid="testGenerate5SLURM"
workflow="src/main/resources/workflows/benchmark.5.1/workflow.csv"
parameters="src/main/resources/workflows/benchmark.5.1/parameters.csv,src/main/resources/workflows/benchmark.5.1/sysparameters.csv"
user="TEST_PROPERTY(user.name)"
database="none"
backend="slurm"
port="80"
interval="2000"
path="."
source $ENVIRONMENT_DIR/step2_0.env


# Connect parameters to environment
mytool="bestTool"

# Validate that each 'value' parameter has only identical values in its list
# We do that to protect you against parameter values that might not be correctly set at runtime.
if [[ ! $(IFS=$'\n' sort -u <<< "${mytool[*]}" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo "Error in Step 'step3': input parameter 'mytool' is an array with different values. Maybe 'mytool' is a runtime parameter with 'more variable' values than what was folded on generation-time?" >&2; exit 1; fi

#
## Start of your protocol template
#

#string mytool

echo "I am using ${mytool}"


#
## End of your protocol template
#

# Save output in environment file: '$ENVIRONMENT_DIR/step3_0.env' with the output vars of this step

echo "" >> $ENVIRONMENT_DIR/step3_0.env
chmod 755 $ENVIRONMENT_DIR/step3_0.env




if [ -d ${MC_tmpFolder:-} ]; then
	echo -n "INFO: Removing MC_tmpFolder ${MC_tmpFolder} ..."
	rm -rf ${MC_tmpFolder}
	echo 'done.'
fi

tS=${SECONDS:-0}
tM=$((SECONDS / 60 ))
tH=$((SECONDS / 3600))
echo "On $(date +"%Y-%m-%d %T") ${MC_jobScript} finished successfully after ${tM} minutes." >> molgenis.bookkeeping.log
printf '%s:\t%d seconds\t%d minutes\t%d hours\n' "${MC_jobScript}" "${tS}" "${tM}" "${tH}" >> molgenis.bookkeeping.walltime

mv "${MC_jobScript}.started" "${MC_jobScript}.finished"

trap - EXIT
exit 0

