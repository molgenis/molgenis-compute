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
# Skip this step if step finished already successfully
if [ -f test1_1.sh.finished ]; then
	skip test1_1.sh
	echo "Skipped test1_1.sh"
else
	echo "--- begin step: test1_1 ---"
	echo " "
	bash test1_1.sh
	echo " "
	echo "--- end step: test1_1 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test1_2.sh.finished ]; then
	skip test1_2.sh
	echo "Skipped test1_2.sh"
else
	echo "--- begin step: test1_2 ---"
	echo " "
	bash test1_2.sh
	echo " "
	echo "--- end step: test1_2 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test1_3.sh.finished ]; then
	skip test1_3.sh
	echo "Skipped test1_3.sh"
else
	echo "--- begin step: test1_3 ---"
	echo " "
	bash test1_3.sh
	echo " "
	echo "--- end step: test1_3 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test1_4.sh.finished ]; then
	skip test1_4.sh
	echo "Skipped test1_4.sh"
else
	echo "--- begin step: test1_4 ---"
	echo " "
	bash test1_4.sh
	echo " "
	echo "--- end step: test1_4 ---"
	echo " "
	echo " "
fi
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
# Skip this step if step finished already successfully
if [ -f test3_0.sh.finished ]; then
	skip test3_0.sh
	echo "Skipped test3_0.sh"
else
	echo "--- begin step: test3_0 ---"
	echo " "
	bash test3_0.sh
	echo " "
	echo "--- end step: test3_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test3_1.sh.finished ]; then
	skip test3_1.sh
	echo "Skipped test3_1.sh"
else
	echo "--- begin step: test3_1 ---"
	echo " "
	bash test3_1.sh
	echo " "
	echo "--- end step: test3_1 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test3_2.sh.finished ]; then
	skip test3_2.sh
	echo "Skipped test3_2.sh"
else
	echo "--- begin step: test3_2 ---"
	echo " "
	bash test3_2.sh
	echo " "
	echo "--- end step: test3_2 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test4_0.sh.finished ]; then
	skip test4_0.sh
	echo "Skipped test4_0.sh"
else
	echo "--- begin step: test4_0 ---"
	echo " "
	bash test4_0.sh
	echo " "
	echo "--- end step: test4_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test4_1.sh.finished ]; then
	skip test4_1.sh
	echo "Skipped test4_1.sh"
else
	echo "--- begin step: test4_1 ---"
	echo " "
	bash test4_1.sh
	echo " "
	echo "--- end step: test4_1 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test5_0.sh.finished ]; then
	skip test5_0.sh
	echo "Skipped test5_0.sh"
else
	echo "--- begin step: test5_0 ---"
	echo " "
	bash test5_0.sh
	echo " "
	echo "--- end step: test5_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test5_1.sh.finished ]; then
	skip test5_1.sh
	echo "Skipped test5_1.sh"
else
	echo "--- begin step: test5_1 ---"
	echo " "
	bash test5_1.sh
	echo " "
	echo "--- end step: test5_1 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test6_0.sh.finished ]; then
	skip test6_0.sh
	echo "Skipped test6_0.sh"
else
	echo "--- begin step: test6_0 ---"
	echo " "
	bash test6_0.sh
	echo " "
	echo "--- end step: test6_0 ---"
	echo " "
	echo " "
fi
# Skip this step if step finished already successfully
if [ -f test6_1.sh.finished ]; then
	skip test6_1.sh
	echo "Skipped test6_1.sh"
else
	echo "--- begin step: test6_1 ---"
	echo " "
	bash test6_1.sh
	echo " "
	echo "--- end step: test6_1 ---"
	echo " "
	echo " "
fi
