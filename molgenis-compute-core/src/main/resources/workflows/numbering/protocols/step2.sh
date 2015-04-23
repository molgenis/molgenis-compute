#list strings
#string input

echo ${input}

echo "Result of step1.sh:"
for s in "${strings[@]}"
do
    echo ${s}
done

echo "(FOR TESTING PURPOSES: your runid is ${runid})"