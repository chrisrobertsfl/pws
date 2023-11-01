declare pomFilePath=/Users/TKMA5QX/projects/olm-meta-repo/olm-stubs/pom.xml
declare settingsXmlFilePath=/Users/TKMA5QX/data/repo/maven/settings.xml
declare HTTPS_PROXY=http://proxy.kohls.com:3128
(( ${#} == 0 ))  &&  { echo "Usage: ${0} <args>"; exit 1; }
echo args are "${@}"
echo pomFilePath is $pomFilePath
echo settingsXmlFilePath is $settingsXmlFilePath
mvn --file ${pomFilePath} --settings ${settingsXmlFilePath} "${@}" 2>&1
