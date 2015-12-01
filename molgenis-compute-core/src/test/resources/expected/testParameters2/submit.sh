# First cd to the directory with the *.sh and *.finished scripts
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

# Skip this step if step finished already successfully
if [ -f test1_0.sh.finished ]; then
	skip test1_0.sh
	echo "Skipped test1_0.sh"
else
	echo "--- begin step: test1_0 ---"
	echo " "
	bash test1_0.sh
	echo " "
	echo "--- end step: test1_0 ---"
	echo " "
	echo " "
fi
