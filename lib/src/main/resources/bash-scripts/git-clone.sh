

(( ${#} == 2 ))|| { echo "Usage: ${0} <repository-url> <target-directory>"; exit 1; }
echo "repository url   : $1"
echo "target directory : $2"
git clone ${1} ${2} 2>&1
