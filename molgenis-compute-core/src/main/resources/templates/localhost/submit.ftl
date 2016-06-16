# First cd to the directory with the *.sh and *.finished scripts
<#noparse>
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
HOST=$(hostname)
myTmp=""
myGroup=""
if [ "${HOST}" == "zinc-finger.gcc.rug.nl" ]
then
    	myTmp="tmp05"
        myGroup="umcg-gd"
elif [ "${HOST}" == "leucine-zipper.gcc.rug.nl" ]
then
    	myTmp="tmp06"
        myGroup="umcg-gd"
elif [ "${HOST}" == "calculon" ]
then
    	myTmp="tmp04"
        myGroup="umcg-gaf"
elif [[ "${HOST}" == *"molgenis"* ]]
then
    	myTmp="tmpTest01"
        myGroup="umcg-testgroup"
else
    	echo "unknown server, please contact helpdesk.gcc.groningen@gmail.com"
        exit 0
fi

failedFile="/groups/${myGroup}/${myTmp}/logs/${projectName}.pipeline.failed"

if [ -f ${failedFile} ]
then
    	rm ${failedFile}
        if [ -f ${failedFile}.mailed ]
        then
            	rm ${failedFile}.mailed
        fi
fi

</#noparse>

# Use this to indicate that we skip a step
skip(){
	echo "0: Skipped --- TASK '$1' --- ON $(date +"%Y-%m-%d %T")" >> molgenis.skipped.log
}

<#foreach t in tasks>
# Skip this step if step finished already successfully
if [ -f ${t.name}.sh.finished ]; then
	skip ${t.name}.sh
	echo "Skipped ${t.name}.sh"
else
	echo "--- begin step: ${t.name} ---"
	echo " "
	bash ${t.name}.sh
	echo " "
	echo "--- end step: ${t.name} ---"
	echo " "
	echo " "
fi
</#foreach>
