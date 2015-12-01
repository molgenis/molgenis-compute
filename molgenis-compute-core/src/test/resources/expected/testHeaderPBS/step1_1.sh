#PBS -N step1_1
#PBS -q gaf
#PBS -l nodes=1:ppn=1
#PBS -l walltime=05:59:00
#PBS -l mem=4
#PBS -e step1_1.err
#PBS -o step1_1.out
#PBS -W umask=0007

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

#
## Header for PBS backend
#

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
        MC_tmpFolder=$dir/tmp_step1_1_$myMD5array/
        mkdir -p $MC_tmpFolder
        if [[ -d $1 ]]
        then
                MC_tmpFile="$MC_tmpFolder"
        else
                MC_tmpFile="$MC_tmpFolder/$base"
        fi
}
echo Running on node: `hostname`

#highly recommended to use
set -e # exit if any subcommand or pipeline returns a non-zero status
set -u # exit if any uninitialised variable is used

# Set location of *.env files
ENVIRONMENT_DIR="$PBS_O_WORKDIR"

# If you detect an error, then exit your script by calling this function
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage --- TASK 'step1_1.sh' --- ON $(date +"%Y-%m-%d %T"), AFTER $(( ($(date +%s) - $MOLGENIS_START) / 60 )) MINUTES" >> $ENVIRONMENT_DIR/molgenis.error.log
	exit $errorCode
}

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

# Show that the task has started
touch $ENVIRONMENT_DIR/step1_1.sh.started


# Define the root to all your tools and data
WORKDIR=${WORKDIR}

# Source getFile, putFile, inputs, alloutputsexist
include () {
	if [[ -f "$1" ]]; then
		source "$1"
		echo "sourced $1"
	else
		echo "File not found: $1"
	fi		
}
include $GCC_HOME/gcc.bashrc
getFile()
{
        ARGS=($@)
        NUMBER="${#ARGS[@]}";
        if [ "$NUMBER" -eq "1" ]
        then
                myFile=${ARGS[0]}

                if test ! -e $myFile;
                then
                                echo "WARNING in getFile/putFile: $myFile is missing" 1>&2
                fi

        else
                echo "Example usage: getData \"\$TMPDIR/datadir/myfile.txt\""
        fi
}

putFile()
{
        `getFile $@`
}

inputs()
{
  for name in $@
  do
    if test ! -e $name;
    then
      echo "$name is missing" 1>&2
      exit 1;
    fi
  done
}

outputs()
{
  for name in $@
  do
    if test -e $name;
    then
      echo "skipped"
      echo "skipped" 1>&2
      exit 0;
    else
      return 0;
    fi
  done
}

alloutputsexist()
{
  all_exist=true
  for name in $@
  do
    if test ! -e $name;
    then
        all_exist=false
    fi
  done
  if $all_exist;
  then
      echo "skipped"
      echo "skipped" 1>&2
      sleep 30
      exit 0;
  else
      return 0;
  fi
}

#
## End of header for PBS backend
#



#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step1_1"

# Make compute.properties available
rundir="/Users/mdehaan/git/molgenis-compute/molgenis-compute-core/target/test/benchmark/run"
runid="testHeaderPBS"
workflow="src/main/resources/workflows/benchmark/workflowa.csv"
parameters="src/main/resources/workflows/benchmark/parameters.csv"
user="mdehaan"
database="none"
backend="pbs"
port="80"
interval="2000"
path="."

# Connect parameters to environment
in="bye"

# Validate that each 'value' parameter has only identical values in its list
# We do that to protect you against parameter values that might not be correctly set at runtime.
if [[ ! $(IFS=$'\n' sort -u <<< "${in[*]}" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo "Error in Step 'step1': input parameter 'in' is an array with different values. Maybe 'in' is a runtime parameter with 'more variable' values than what was folded on generation-time?" >&2; exit 1; fi

#
## Start of your protocol template
#

#string in
#output out

# Let's do something with string 'in'
echo "${in}_hasBeenInStep1"
out=${in}_hasBeenInStep1

#
## End of your protocol template
#

# Save output in environment file: '$ENVIRONMENT_DIR/step1_1.env' with the output vars of this step
if [[ -z "$out" ]]; then echo "In step 'step1', parameter 'out' has no value! Please assign a value to parameter 'out'." >&2; exit 1; fi
echo "step1__has__out[1]=\"${out[0]}\"" >> $ENVIRONMENT_DIR/step1_1.env

echo "" >> $ENVIRONMENT_DIR/step1_1.env
chmod 755 $ENVIRONMENT_DIR/step1_1.env


#
## General footer
#

# Show that we successfully finished
# If this file exists, then this step will be skipped when you resubmit your workflow 
touch $ENVIRONMENT_DIR/step1_1.sh.finished

echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, task step1_1 finished successfully" >> $ENVIRONMENT_DIR/molgenis.bookkeeping.log

if [ -d ${MC_tmpFolder:-} ];
	then
	echo "removed tmpFolder $MC_tmpFolder"
	rm -r $MC_tmpFolder
fi
