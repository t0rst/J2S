#!/bin/bash
cmd_full="$_"
cmd_path="$(dirname "$cmd_full")"
cmd_name="$(basename "$cmd_full")"
cmd_stem="${cmd_name%\.*}"

CP_ANTLR="/usr/local/Cellar/antlr/4.5.3/antlr-4.5.3-complete.jar"
CP_J2S="${cmd_path}/out/production/J2S"

if [[ ! -f "${CP_J2S}/com/satisfyingstructures/J2S/J2S.class" ]]; then
    echo "Oops - expected J2S classes relative to me at ${CP_J2S}"
    exit 1
fi

if [[ ! -f "${CP_ANTLR}" ]]; then
    echo "Oops - expected antlr jar at ${CP_ANTLR}"
    exit 1
fi

#	Special params
timed=0
declare -a jx=
jn=0
while [[ "$1" != "" ]]; do
    case "$1" in
        --timed ) timed=1 ; shift 1 ;;
        --java* ) jx[$jn]="${1#--java}" ; jn=$(($jn + 1)) ; shift 1 ;;
        # java options of interest: -Xmx1g -XX:+UseConcMarkSweepGC -XX:-UseGCOverheadLimit
        * ) break;
    esac
done

if (($timed==1)); then
    TIMEFORMAT="Elapsed:    %1U     (real %R, sys %S)"
    time java $jx -cp "${CP_ANTLR}:${CP_J2S}:." com.satisfyingstructures.J2S.J2S "$@"
else
    java $jx -cp "${CP_ANTLR}:${CP_J2S}:." com.satisfyingstructures.J2S.J2S "$@"
fi

