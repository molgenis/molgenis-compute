# My own custom header

if [ ! -e $0.started ] ; then 
	touch $0.started
fi

#
## Generated header
#

# Assign values to the parameters in this script

# Set taskId, which is the job name of this task
taskId="step3_0"

# Make compute.properties available
rundir="TEST_PROPERTY(project.basedir)/target/test/benchmark/run/testRunLocally5"
runid="testRunLocally5"
workflow="src/main/resources/workflows/benchmark.5.1/workflow.csv"
parameters="src/main/resources/workflows/benchmark.5.1/parameters.csv"
user="TEST_PROPERTY(user.name)"
database="none"
backend="localhost"
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


# My own custom footer

if [ ! -e $(basename $0 .sh).env ] ; then 
	touch $0.finished
fi