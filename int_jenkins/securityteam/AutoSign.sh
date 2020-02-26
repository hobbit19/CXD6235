#!/bin/bash

################################################
## user to automent sign build
## create by jianbo.deng 2013-11-27
################################################
#set -x
set -e


E_BADARGS=65

## no input arg
if [ ! -n "$1" ]
then
   echo "Usage: `basename $0` argument1 etc."
   exit $E_BADARGS
fi 
echo '---------'
echo "the paraments length is $#"
echo "---the first parament is $1"
echo "---the second parament is $2"
echo "---the third parament is $3"


###compare python path
if [ -x /opt/python-2.7.1/bin/python ]; then
	PYTHON=/opt/python-2.7.1/bin/python
else
	PYTHON=/usr/bin/python
fi

SIGNPW=""

## if add other project pls add code like this
#$1 is the same int_tools/securityteam signProject name!!
if [ "$1" == "yarism" ]
then
    SIGNPW="Yaris1305095962"

elif [ "$1" == "twin" ]
then
    SIGNPW="twin1401033162"

elif [ "$1" == "pixi3-4" ]
then
    SIGNPW="TCL_1010"

elif [ "$1" == "yaris35_orange" ]
then
    SIGNPW="Yaris1307188246"

elif [ "$1" == "yaris35" ]
then
    SIGNPW="Yaris1307188246"

elif [ "$1" == "soul4" ]
then
    SIGNPW="Yaris1307188246"

elif [ "$1" == "soul45" ]
then
    SIGNPW="mtk821401067509"

elif [ "$1" == "pixo_global_sensors" ]
then
    SIGNPW="beetlelite0927"

elif [ "$1" == "pixo_global" ]
then
    SIGNPW="beetlelite0927"

elif [ "$1" == "beetlelite_cu" ]
then
    SIGNPW="beetlelite0927"

elif [ "$1" == "beetlelite_global" ]
then
    SIGNPW="beetlelite0927"

elif [ "$1" == "beetlelite_china" ]
then
    SIGNPW="beetlelite0927"

elif [ "$1" == "pixo7" ]
then
    SIGNPW="pixo71401222963"

elif [ "$1" == "soul5" ]
then
    SIGNPW="TCL_1010"

elif [ "$1" == "pixi34na" ]
then
    SIGNPW="TCL_1010"

elif [ "$1" == "pixi335" ]
then
    SIGNPW="TCL_1010"

elif [ "$1" == "pixi34tf" ]
then
    SIGNPW="TCL_1010"

elif [ "$1" == "pixi3-45-4G" ] || [ "$1" == "pixi3-45-4G-Bell" ]
then
    SIGNPW="TCL_1010"
elif [ "$1" == "pixi335tf" ] && [ ! ${3:1:1} == 'D' ]
then
    SIGNPW="TCL_1010"
elif [ "$1" == "pixi335tf" ] && [ ${3:1:1} == 'D' ]
then
    SIGNPW="TF_0727"
elif [ "$1" == "pixi34-telus" ]
then
    SIGNPW="TCL_1010"
fi
#Note:shell script arglist length is not include $0. It is different from python script.
#Added by yufei.qin for pixi3-45-4g eng version signature--1BCX 20150423
if [ $# -eq 3 ]
then
   exec $PYTHON signStart.py $SIGNPW $2 $3
elif [ $# -eq 2 ]
then
   exec $PYTHON signStart.py $SIGNPW $2
else
    ###start run signe module
    exec $PYTHON signStart.py $SIGNPW
fi
