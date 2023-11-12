
(( ${#} == 0 ))  &&  { echo "Usage: ${0} <pom-file-path> <settings-xml-file-path> <args>"; exit 1; }
declare pomFilePath=${1}
declare settingsXmlFilePath=${2}
echo pomFilePath is $pomFilePath
echo settingsXmlFilePath is $settingsXmlFilePath
shift 2
echo args are "${@}"
mvn --file ${pomFilePath} --settings ${settingsXmlFilePath} "${@}" 2>&1