# First cd to the directory with the *.sh and *.finished scripts
MOLGENIS_scriptsDir=$( cd -P "$( dirname "$0" )" && pwd )
echo "cd $MOLGENIS_scriptsDir"
cd $MOLGENIS_scriptsDir

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

# Skip this step if step finished already successfully
if [ -f test2_0.sh.finished ]; then
	skip test2_0.sh
	echo "Skipped test2_0.sh"
else
	echo "--- begin step: test2_0 ---"
	echo " "
	bash test2_0.sh
	echo " "
	echo "--- end step: test2_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test2_1.sh.finished ]; then
	skip test2_1.sh
	echo "Skipped test2_1.sh"
else
	echo "--- begin step: test2_1 ---"
	echo " "
	bash test2_1.sh
	echo " "
	echo "--- end step: test2_1 ---"
	echo " "
	echo " "
fi
