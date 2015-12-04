# First cd to the directory with the *.sh and *.finished scripts
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir
cd ../
runnumber=`pwd`
echo basename $runnumber
run=$(basename $runnumber)

cd ..
project=`pwd`
projectName=$(basename $project)
cd $MOLGENIS_scriptsDir

touch molgenis.submit.started

# Use this to indicate that we skip a step
skip(){
echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

#
##step0_0
#

# Skip this step if step finished already successfully
if [ -f step0_0.sh.finished ]; then
skip step0_0.sh
echo "Skipped step0_0.sh"
else
# Build dependency string
dependenciesExist=false
dependencies="--dependency=afterok"
if ! $dependenciesExist; then
unset dependencies
fi
output=$(sbatch $dependencies step0_0.sh)
id=step0_0
step0_0=${output##"Submitted batch job "} 
echo "$id:$step0_0" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

#
##step0_1
#

# Skip this step if step finished already successfully
if [ -f step0_1.sh.finished ]; then
skip step0_1.sh
echo "Skipped step0_1.sh"
else
# Build dependency string
dependenciesExist=false
dependencies="--dependency=afterok"
if ! $dependenciesExist; then
unset dependencies
fi
output=$(sbatch $dependencies step0_1.sh)
id=step0_1
step0_1=${output##"Submitted batch job "} 
echo "$id:$step0_1" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

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
dependencies="--dependency=afterok"
    if [[ -n "$step0_0" ]]; then
    dependenciesExist=true
    dependencies="${dependencies}:$step0_0"
    fi
if ! $dependenciesExist; then
unset dependencies
fi
output=$(sbatch $dependencies step1_0.sh)
id=step1_0
step1_0=${output##"Submitted batch job "} 
echo "$id:$step1_0" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

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
dependencies="--dependency=afterok"
    if [[ -n "$step0_1" ]]; then
    dependenciesExist=true
    dependencies="${dependencies}:$step0_1"
    fi
if ! $dependenciesExist; then
unset dependencies
fi
output=$(sbatch $dependencies step1_1.sh)
id=step1_1
step1_1=${output##"Submitted batch job "} 
echo "$id:$step1_1" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

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
dependencies="--dependency=afterok"
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
output=$(sbatch $dependencies step2_0.sh)
id=step2_0
step2_0=${output##"Submitted batch job "} 
echo "$id:$step2_0" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

#
##step3_0
#

# Skip this step if step finished already successfully
if [ -f step3_0.sh.finished ]; then
skip step3_0.sh
echo "Skipped step3_0.sh"
else
# Build dependency string
dependenciesExist=false
dependencies="--dependency=afterok"
    if [[ -n "$step2_0" ]]; then
    dependenciesExist=true
    dependencies="${dependencies}:$step2_0"
    fi
if ! $dependenciesExist; then
unset dependencies
fi
output=$(sbatch $dependencies step3_0.sh)
id=step3_0
step3_0=${output##"Submitted batch job "} 
echo "$id:$step3_0" >> submitted_jobIDs.txt
fi

chmod g+w submitted_jobIDs.txt

touch molgenis.submit.finished
