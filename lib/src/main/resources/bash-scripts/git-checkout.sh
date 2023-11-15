

(( ${#} == 1 ))|| { echo "Usage: ${0} <branch-name>"; exit 1; }
echo "branch name      : $1"
echo "git checkout ${1}"
git checkout ${1} 2>&1
