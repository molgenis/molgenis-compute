#
## Header for 'local' backend
#

#highly recommended to use
#set -e # exit if any subcommand or pipeline returns a non-zero status
#set -u # exit if any uninitialised variable is used

# Set location of *.env and *.log files
ENVIRONMENT_DIR="."

# If you detect an error, then exit your script by calling this function
exitWithError(){
	errorCode=$1
	errorMessage=$2
	echo "$errorCode: $errorMessage --- TASK 'step3_0.sh' --- ON $(date +"%Y-%m-%d %T"), AFTER RUNNING $(( ($(date +%s) - $MOLGENIS_START) / 60 )) MINUTES" >> $ENVIRONMENT_DIR/molgenis.error.log
	exit $errorCode
}

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

# Show that the task has started
touch $ENVIRONMENT_DIR/step3_0.sh.started

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
      return;
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
## End of header for 'local' backend
#


#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step3_0"

# Make compute.properties available
rundir="TEST_PROPERTY(project.basedir)/target/test/benchmark/run"
runid="testRunLocallyA"
workflow="src/main/resources/workflows/benchmark/workflow.3.csv"
parameters="src/main/resources/workflows/benchmark/parameters.csv"
user="TEST_PROPERTY(user.name)"
database="none"
backend="localhost"
port="80"
interval="2000"
path="."


# Connect parameters to environment
workflowName="myFirstWorkflow"
mytool[0]="hello"
mytool[1]="bye"

# Validate that each 'value' parameter has only identical values in its list
# We do that to protect you against parameter values that might not be correctly set at runtime.
if [[ ! $(IFS=$'\n' sort -u <<< "${workflowName[*]}" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo "Error in Step 'step3': input parameter 'workflowName' is an array with different values. Maybe 'workflowName' is a runtime parameter with 'more variable' values than what was folded on generation-time?" >&2; exit 1; fi

#
## Start of your protocol template
#

#string workflowName
#list mytool

for s in "${mytool[@]}"
do
    echo "tool1"
    echo ${s}
done

#
## End of your protocol template
#

# Save output in environment file: '$ENVIRONMENT_DIR/step3_0.env' with the output vars of this step

echo "" >> $ENVIRONMENT_DIR/step3_0.env
chmod 755 $ENVIRONMENT_DIR/step3_0.env


#
## General footer
#

# Show that we successfully finished. If the .finished file exists, then this step will be skipped when you resubmit your workflow 
touch $ENVIRONMENT_DIR/step3_0.sh.finished

# Also do bookkeeping
echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, task step3_0.sh finished successfully" >> $ENVIRONMENT_DIR/molgenis.bookkeeping.log
