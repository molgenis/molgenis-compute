# First cd to the directory with the *.sh and *.finished scripts
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

# Skip this step if step finished already successfully
if [ -f step0_0.sh.finished ]; then
	skip step0_0.sh
	echo "Skipped step0_0.sh"
else
	echo "--- begin step: step0_0 ---"
	echo " "
	bash step0_0.sh
	echo " "
	echo "--- end step: step0_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f step0_1.sh.finished ]; then
	skip step0_1.sh
	echo "Skipped step0_1.sh"
else
	echo "--- begin step: step0_1 ---"
	echo " "
	bash step0_1.sh
	echo " "
	echo "--- end step: step0_1 ---"
	echo " "
	echo " "
fi
