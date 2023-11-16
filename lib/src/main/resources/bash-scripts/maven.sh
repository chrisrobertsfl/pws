

(( ${#} == 0 ))  &&  { echo "Usage: ${0} <args>"; exit 1; }
echo args are "${@}"
echo pomFilePath is $pomFilePath
echo settingsXmlFilePath is $settingsXmlFilePath
export HTTPS_PROXY=http://proxy.kohls.com:3128
mvn --file ${pomFilePath} --settings ${settingsXmlFilePath} "${@}" 2>&1
