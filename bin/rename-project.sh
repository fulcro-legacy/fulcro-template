#!/usr/bin/env bash
set -o errexit
([[ -n "$DEBUG" ]] || [[ -n "$TRACE" ]]) && set -o xtrace

assert_clean_work_tree () {
    if [[ -n "$(git status -s)" ]]; then
        echo "[ERROR]: Uncommited changes!"
        git status
        exit 1
    fi
}

search_and_replace () {
    #LC_ALL=C is important because we have utf-8 symbols in the readme
    #and sed will corrupt them & git will explode otherwise
    #stackoverflow.com/questions/19242275/re-error-illegal-byte-sequence-on-mac-os-x
    #stackoverflow.com/questions/1115854/how-to-resolve-error-bad-index-fatal-index-file-corrupt-when-using-git
    export LC_ALL=C
    grep --exclude="$0" --exclude-dir=".git" -lr "$1" * | xargs sed -i '' "s/$1/$2/g"
    unset LC_ALL
}

rename_matching_dirs () {
    for d in $(find . -type d -name "$1"); do
        mv "$d" "$(dirname $d)/$2"
    done
}

main () {
    assert_clean_work_tree
    read -p "Renaming namespace 'untangled-template' to: " ns
    read -p "Renaming file/dir 'untangled_template' to: " fdir
    search_and_replace "untangled-template" "$ns"
    search_and_replace "untangled_template" "$fdir"
    rename_matching_dirs "untangled_template" "$fdir"
}

main "$@"
