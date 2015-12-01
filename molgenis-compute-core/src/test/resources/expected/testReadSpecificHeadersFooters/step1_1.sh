# My own custom header

if [ ! -e $0.started ] ; then 
	touch $0.started
fi

#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step1_1"

# Make compute.properties available
rundir="/Users/mdehaan/git/molgenis-compute/molgenis-compute-core/target/test/benchmark/run"
runid="G3fl"
workflow="src/main/resources/workflows/benchmark.5.1/workflow.csv"
parameters="src/main/resources/workflows/benchmark.5.1/parameters.csv"
user="mdehaan"
database="none"
backend="localhost"
port="80"
interval="2000"
path="."source $ENVIRONMENT_DIR/step0_1.env


# Connect parameters to environment
input="bye"

# Validate that each 'value' parameter has only identical values in its list
# We do that to protect you against parameter values that might not be correctly set at runtime.
if [[ ! $(IFS=$'\n' sort -u <<< "${input[*]}" | wc -l | sed -e 's/^[[:space:]]*//') = 1 ]]; then echo "Error in Step 'step1': input parameter 'input' is an array with different values. Maybe 'input' is a runtime parameter with 'more variable' values than what was folded on generation-time?" >&2; exit 1; fi

#
## Start of your protocol template
#

#string input
#output out

# Let's do something with string 'in'
echo "${input}_hasBeenInStep1"
out=${input}_hasBeenInStep1

#
## End of your protocol template
#

# Save output in environment file: '$ENVIRONMENT_DIR/step1_1.env' with the output vars of this step
if [[ -z "$out" ]]; then echo "In step 'step1', parameter 'out' has no value! Please assign a value to parameter 'out'." >&2; exit 1; fi
echo "step1__has__out[1]=\"${out[0]}\"" >> $ENVIRONMENT_DIR/step1_1.env

echo "" >> $ENVIRONMENT_DIR/step1_1.env
chmod 755 $ENVIRONMENT_DIR/step1_1.env


# My own custom footer

if [ ! -e $(basename $0 .sh).env ] ; then 
	touch $0.finished
fi