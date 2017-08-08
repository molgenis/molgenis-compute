#!/bin/bash

#
# Bash sanity.
#
set -e
set -u

#
# Get commandline arguments.
# Any arguments specified are passed on to bash unparsed "as is"...
#
MC_submitOptions="${@:-}"

#
##
### Functions.
##
#

function processJob () {
	local _jobName="${1}"
	local _jobScript="${_jobName}.sh"
	local _submitOptions="${2:-}" # Optional.
	local _submitCommand
	
	#
	# Skip this job if it already finished successfully.
	#
	if [ -e "${_jobName}.sh.finished" ]; then
		echo "INFO: Skipped ${_jobScript}"
		echo "0: Skipped --- TASK ${_jobScript} --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
		return
	fi
	
	#
	# Submit to 'local' backend; a.k.a. execute job script.
	#
	_submitCommand="bash ${_submitOptions} ${_jobScript}"
	echo "INFO: Trying to execute job:"
	echo "          ${_submitCommand}"
	set +e
	$(${_submitCommand} 1>${_jobName}.out 2>${_jobName}.err)
	if [[ ${?} -eq 0 ]]; then
		set -e
		echo "      Ok. See ${_jobName}.out for details."
	else
		set -e
		echo "      Ooops. See ${_jobName}.err for details."
		echo "FATAL: Failed to execute job ${_jobScript}"
		exit 1
	fi
}

#
##
### Main.
##
#

#
# First find our where this submit.sh script and the job *.sh scripts were created
# Then change to that directory to make sure relative paths 
# further down in this script can be resolved correctly.
#
MC_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo -n "INFO: Changing working directory to ${MC_scriptsDir}... "
cd "${MC_scriptsDir}"
echo 'done.'

touch molgenis.submit.started


#
# Process jobs: either skip if job previously finished successfully or excute job script on localhost.
#
processJob "step0_0" "${MC_submitOptions}"
processJob "step0_1" "${MC_submitOptions}"

mv molgenis.submit.{started,finished}
