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
##step1_5
#

# Skip this step if step finished already successfully
if [ -f step1_5.sh.finished ]; then
	skip step1_5.sh
	echo "Skipped step1_5.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_5
	step1_5=$(qsub -N step1_5 $dependencies step1_5.sh)
	echo "$id:$step1_5"
	sleep 0
fi


#
##step1_6
#

# Skip this step if step finished already successfully
if [ -f step1_6.sh.finished ]; then
	skip step1_6.sh
	echo "Skipped step1_6.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_6
	step1_6=$(qsub -N step1_6 $dependencies step1_6.sh)
	echo "$id:$step1_6"
	sleep 0
fi


#
##step2_2
#

# Skip this step if step finished already successfully
if [ -f step2_2.sh.finished ]; then
	skip step2_2.sh
	echo "Skipped step2_2.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
		if [[ -n "$step1_5" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_5"
		fi
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step2_2
	step2_2=$(qsub -N step2_2 $dependencies step2_2.sh)
	echo "$id:$step2_2"
	sleep 0
fi


#
##step2_3
#

# Skip this step if step finished already successfully
if [ -f step2_3.sh.finished ]; then
	skip step2_3.sh
	echo "Skipped step2_3.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
		if [[ -n "$step1_6" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_6"
		fi
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step2_3
	step2_3=$(qsub -N step2_3 $dependencies step2_3.sh)
	echo "$id:$step2_3"
	sleep 0
fi



touch molgenis.submit.finished
