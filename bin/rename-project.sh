#!/usr/bin/env bash
([[ -n "$DEBUG" ]] || [[ -n "$TRACE" ]]) && set -x
set -e

assert_clean_work_tree () {
    if [[ -z "$OVERRIDE" ]] && [[ -n "$(git status -s)" ]]; then
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
    for f in $(grep --exclude="$0" --exclude-dir=".git" -lr "$1" *); do
        #using .bak in place extension for portability between sed versions
        #is more portable (& easier) than no backup
        sed -i.bak "s/$1/$2/g" "$f"
        rm ${f}.bak
    done
    unset LC_ALL
}

rename_matching_dirs () {
    for d in $(find . -type d -name "$1"); do
        mv "$d" "$(dirname $d)/$2"
    done
}

main () {
    assert_clean_work_tree
    read -p "Renaming 'fulcro-template' to: " ns
    search_and_replace "fulcro-template" "$ns"
    local fdir="${ns//-/_}"
    search_and_replace "fulcro_template" "$fdir"
    rename_matching_dirs "fulcro_template" "$fdir"
}

main "$@"
