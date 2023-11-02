
get_pids_for_pattern() {
    local pattern="$1"
    local pids=()
    while IFS= read -r pid; do
        pids+=("$pid")
    done < <(pgrep -if "${pattern}")
    echo "${pids[@]}"
}

get_child_pids_for_pid() {
    local parentPid="$1"
    local childPids=()
    while IFS= read -r child; do
        childPids+=("$child")
    done < <(pgrep -P "${parentPid}")
    echo "${childPids[@]}"
}

kill_pattern() {
    local pattern="$1"
    local parentPids=($(get_pids_for_pattern "${pattern}"))

    if (( ${#parentPids[@]} > 0 )); then
        for parentPid in "${parentPids[@]}"; do
            local childPids=($(get_child_pids_for_pid "${parentPid}"))
            if (( ${#childPids[@]} == 0 )); then
                echo "  o No child processes found for parent PID $parentPid"
            else
                echo "  o Killing children PIDs: ${childPids[*]}"
                kill ${childPids[*]}
            fi
        done
        echo "  o Killing parent PIDs: ${parentPids[*]}"
        kill ${parentPids[*]}
    else
        echo "  o No processes found for pattern '${pattern}'"
    fi
}


echo "o PID search for '${1}'"
kill_pattern "${1}"
