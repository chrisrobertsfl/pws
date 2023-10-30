
echo runDirectory is $runDirectory
echo args are $args
cd "${runDirectory}" || exit 1
mvn ${args} 2>&1