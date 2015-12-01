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
##step1_2
#

# Skip this step if step finished already successfully
if [ -f step1_2.sh.finished ]; then
	skip step1_2.sh
	echo "Skipped step1_2.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_2
	step1_2=$(qsub -N step1_2 $dependencies step1_2.sh)
	echo "$id:$step1_2"
	sleep 0
fi


#
##step1_3
#

# Skip this step if step finished already successfully
if [ -f step1_3.sh.finished ]; then
	skip step1_3.sh
	echo "Skipped step1_3.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_3
	step1_3=$(qsub -N step1_3 $dependencies step1_3.sh)
	echo "$id:$step1_3"
	sleep 0
fi


#
##step1_4
#

# Skip this step if step finished already successfully
if [ -f step1_4.sh.finished ]; then
	skip step1_4.sh
	echo "Skipped step1_4.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step1_4
	step1_4=$(qsub -N step1_4 $dependencies step1_4.sh)
	echo "$id:$step1_4"
	sleep 0
fi


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
		if [[ -n "$step1_2" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_2"
		fi
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step2_0
	step2_0=$(qsub -N step2_0 $dependencies step2_0.sh)
	echo "$id:$step2_0"
	sleep 0
fi


#
##step2_1
#

# Skip this step if step finished already successfully
if [ -f step2_1.sh.finished ]; then
	skip step2_1.sh
	echo "Skipped step2_1.sh"	
else
	# Build dependency string
	dependenciesExist=false
	dependencies="-W depend=afterok"
		if [[ -n "$step1_3" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_3"
		fi
		if [[ -n "$step1_4" ]]; then
			dependenciesExist=true
			dependencies="${dependencies}:$step1_4"
		fi
	if ! $dependenciesExist; then
		unset dependencies
	fi

	id=step2_1
	step2_1=$(qsub -N step2_1 $dependencies step2_1.sh)
	echo "$id:$step2_1"
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
