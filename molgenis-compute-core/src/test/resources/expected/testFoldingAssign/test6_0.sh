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
	echo "$errorCode: $errorMessage --- TASK 'test6_0.sh' --- ON $(date +"%Y-%m-%d %T"), AFTER RUNNING $(( ($(date +%s) - $MOLGENIS_START) / 60 )) MINUTES" >> $ENVIRONMENT_DIR/molgenis.error.log
	exit $errorCode
}

# For bookkeeping how long your task takes
MOLGENIS_START=$(date +%s)

# Show that the task has started
touch $ENVIRONMENT_DIR/test6_0.sh.started

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
taskId="test6_0"

# Make compute.properties available
rundir="TEST_PROPERTY(project.basedir)/target/test/benchmark/run"
runid="testFoldingAssign"
workflow="src/main/resources/workflows/testfolding/workflow.csv"
parameters="src/main/resources/workflows/testfolding/parameters.csv"
user="TEST_PROPERTY(user.name)"
database="none"
backend="localhost"
port="80"
interval="2000"
path="."
source $ENVIRONMENT_DIR/test1_0.env
source $ENVIRONMENT_DIR/test1_1.env
source $ENVIRONMENT_DIR/test1_2.env


# Connect parameters to environment
chr="1"
runtime_concat[0]=${test1__has__outputLALA[0]}
runtime_concat[1]=${test1__has__outputLALA[1]}
runtime_concat[2]=${test1__has__outputLALA[2]}

# Validate that each 'value' parameter has only identical values in its list
# We do that to protect you against parameter values that might not be correctly set at runtime.
if [[ ! $(IFS=$'\n' sort -u <<< "${chr[*]}" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo "Error in Step 'test6': input parameter 'chr' is an array with different values. Maybe 'chr' is a runtime parameter with 'more variable' values than what was folded on generation-time?" >&2; exit 1; fi

#
## Start of your protocol template
#

#string chr
#list runtime_concat

for s in "${runtime_concat[@]}"
do
    echo ${s}
done

#
## End of your protocol template
#

# Save output in environment file: '$ENVIRONMENT_DIR/test6_0.env' with the output vars of this step

echo "" >> $ENVIRONMENT_DIR/test6_0.env
chmod 755 $ENVIRONMENT_DIR/test6_0.env


#
## General footer
#

# Show that we successfully finished. If the .finished file exists, then this step will be skipped when you resubmit your workflow 
touch $ENVIRONMENT_DIR/test6_0.sh.finished

# Also do bookkeeping
echo "On $(date +"%Y-%m-%d %T"), after $(( ($(date +%s) - $MOLGENIS_START) / 60 )) minutes, task test6_0.sh finished successfully" >> $ENVIRONMENT_DIR/molgenis.bookkeeping.log
