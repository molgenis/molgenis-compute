# My own custom header

if [ ! -e $0.started ] ; then 
	touch $0.started
fi



#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step1_0"

# Make compute.properties available
rundir="TEST_PROPERTY(project.basedir)/target/test/benchmark/run/testRunLocally"
runid="testRunLocally"
workflow="src/main/resources/workflows/benchmark/workflow.csv"
parameters="src/main/resources/workflows/benchmark/parameters.csv"
user="TEST_PROPERTY(user.name)"
database="none"
backend="localhost"
port="80"
interval="2000"
path="."


# Connect parameters to environment
in="hello"

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

# Save output in environment file: '$ENVIRONMENT_DIR/step1_0.env' with the output vars of this step
if [[ -z "$out" ]]; then echo "In step 'step1', parameter 'out' has no value! Please assign a value to parameter 'out'." >&2; exit 1; fi
echo "step1__has__out[0]=\"${out[0]}\"" >> $ENVIRONMENT_DIR/step1_0.env

echo "" >> $ENVIRONMENT_DIR/step1_0.env
chmod 755 $ENVIRONMENT_DIR/step1_0.env


# My own custom footer

if [ ! -e $(basename $0 .sh).env ] ; then 
	touch $0.finished
fi