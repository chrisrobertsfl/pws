

(( ${#} == 2 ))|| { echo "Usage: ${0} <target-directory> <branch-name>"; exit 1; }
echo "target directory : $1"
echo "branch name      : $2"
git -C ${1} checkout ${2} 2>&1
