# First cd to the directory with the *.sh and *.finished scripts
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir

touch molgenis.submit.started

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

#
##step1_0
#

# Skip this step if step finished already successfully
if [ -f step1_0.sh.finished ]; then
	skip step1_0.sh
	echo "Skipped step1_0.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_0
	step1_0=$(qsub -N step1_0 $dependencies step1_0.sh)
	echo "$id:$step1_0"
	sleep 0
fi


#
##step1_1
#

# Skip this step if step finished already successfully
if [ -f step1_1.sh.finished ]; then
	skip step1_1.sh
	echo "Skipped step1_1.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_1
	step1_1=$(qsub -N step1_1 $dependencies step1_1.sh)
	echo "$id:$step1_1"
	sleep 0
fi


#
##step2_0
#

# Skip this step if step finished already successfully
if [ -f step2_0.sh.finished ]; then
	skip step2_0.sh
	echo "Skipped step2_0.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
		if [[ -n "$step1_1" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_1"
		fi
		if [[ -n "$step1_0" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_0"
		fi
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step2_0
	step2_0=$(qsub -N step2_0 $dependencies step2_0.sh)
	echo "$id:$step2_0"
	sleep 0
fi



touch molgenis.submit.finished
