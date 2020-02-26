checkDir() {
    if [ ! -d $1 ]; then
        echo "$2"
        exit
    fi;
}


checkFile() {
    if [ ! -f "$1" ]; then
        echo "Unable to locate $1."
        exit
    fi;
}

CTS_ROOT=./../..
RUNCTS=${CTS_ROOT}/tools/cts-tradefed
PARSECTS_ROOT=$CTS_ROOT/tools/athrun
if [ $# -eq 4 ]
then
    DEVICESID=$1
    CHOOSERESULT=$2
    SHARDSRUN=$3
    PPLATFORM=$4
    selectresult=false
elif [ $# -eq 0 ]
then
    
    echo "Which platform do you want to run"
    echo "1:	kk"
    echo "2:	androidM"
    echo "3:	androidN"
    read -p "choose your platform:" platformnum
    if [ $platformnum -eq 1 ]
    then
	PPLATFORM='kk'
    elif [ $platformnum -eq 2 ]
    then
	PPLATFORM='androidM'
    elif [ $platformnum -eq 3 ]
    then
	PPLATFORM='androidN'
    else
	echo "Wrong number of platform!"
	exit
    fi

    read -p "would your like to shard running?(y/n)" SHARDS
    if [ $SHARDS = 'y' ] || [ $SHARDS = 'Y' ]
    then
	SHARDSRUN='true'
    else
	SHARDSRUN='false'
    fi

    if ! $SHARDSRUN
    then
	    tmp=`adb devices`
	    devices=`echo $tmp | sed -n 's/List of devices attached//p' | sed 's/device/\n/g'`
	    num=1
	    if [ -z "$devices" ]
	    then 
		echo "None of devieces were attached!"
		echo "Checkout!"
		exit
		
	    fi
	    echo "List of devices attached:"

	    echo $tmp | sed -n 's/List of devices attached//p' | sed 's/device/\n/g' | sed '{
	    :start
	    /^\n*$/{$d ; N; b start }
	    }' | sed '=' | sed 'N; s/\n/ /'

	    read -p "choose your devices(from 1 to n):" number
	    DEVICESID=`echo $devices | awk -v num=$number '{print $num}'`
	    if [ -n $DEVICESID ]
	    then
		echo "Your device is $DEVICESID"
	    else
		echo "Wrong number of device!"
		exit
	    fi
    else
    	    tmp=`adb devices`
	    devices=`echo $tmp | sed -n 's/List of devices attached//p' | sed 's/device/\n/g'`
            if [ -z "$tmp" ]
	    then 
		echo "None of devieces were attached!"
		echo "Checkout!"
		exit
		
	    fi
            DEVICESID=`echo $devices | awk -v num=1 '{print $num}'`
    fi

    selectresult=true
	
fi
checkDir ${PARSECTS_ROOT} "Error: Cannot locate parse file in \"${PARSECTS_ROOT}\". Please check your configuration in $0"
checkDir ${CTS_ROOT} "Error: Cannot locate startcts in \"${CTS_ROOT}\". Please check your configuration in $0"
checkFile ${RUNCTS}

_xarray=(a b c)
if [ -z "${_xarray[${#_xarray[@]}]}" ]
then
    _arrayoffset=1
else
    _arrayoffset=0
fi

TESTRESULT=
function chooseresult()
{
    # Find the makefiles that must exist for a product.
    # Send stderr to /dev/null in case partner isn't present.
    local -a choices
    if [ "$PPLATFORM" == "androidN" ];then
      choices=(`/bin/ls ${CTS_ROOT}/results/*/test_result.xml`)
    else
      choices=(`/bin/ls ${CTS_ROOT}/repository/results/*/testResult.xml`)
    fi
    
    local choice
    local -a prodlist
    #echo $choices
    for choice in ${choices[@]}
    do
        # The product name is the name of the directory containing
        # the makefile we found, above.
        prodlist=(${prodlist[@]} `dirname ${choice} | xargs basename`)
    done

    local index=1
    local p
    if ! $selectresult
    then
        local default_value=0
    else
        local default_value=1
    fi
    echo "Session choices are:"

    for p in ${prodlist[@]}
    do
        echo "     $index. $p"
        let "index = $index + 1"
        if [ "$p" = "$CHOOSERESULT" ] ; then
           let "default_value = $index - 1"
        fi
    done

    if ((index == 1)) || ((default_value == 0)) ; then
       echo "No session exists!";
       exit
    fi

    local ANSWER

    while [ -z "$TESTRESULT" ]
    do
        echo -n "Which session would you like? [$default_value] "
        if $selectresult
        then
            if [ -z "$1" ] ; then
                read ANSWER
            else
                echo $1
                ANSWER=$1
            fi
        fi
        if [ -n "$1" ] ; then
            ANSWER=$1
        fi

        if [ -z "$ANSWER" ] ; then
           ANSWER=$default_value
        fi

        if (echo -n $ANSWER | grep -q -e "^[0-9][0-9]*$") ; then
            local poo=`echo -n $ANSWER`
            if [ $poo -le ${#prodlist[@]} ] ; then
              if [ "$PPLATFORM" == "androidN" ];then
                TESTRESULT=${CTS_ROOT}/results/${prodlist[$(($ANSWER-$_arrayoffset))]}/test_result.xml
              else
                TESTRESULT=${CTS_ROOT}/repository/results/${prodlist[$(($ANSWER-$_arrayoffset))]}/testResult.xml
              fi
                
                if (($index > 2)) ; then
                    let "SESSION = $ANSWER - 1"
                fi

            else
                echo "** Bad session selection: $ANSWER"
            fi
        else
            echo "** Bad session selection: $ANSWER"
       	fi

    done

}
chooseresult
checkFile ${TESTRESULT}
MODIFYTYPE=

function choosetype()
{
    echo "Choose type of cases:"
    echo "    1.Fail"
    echo "    2.Timeout"
    echo "    3.Fail & Timeout"
    echo "Note: All notExcute cases will also be run."
    local default_value=3
    local ANSWER

    while [ -z "$MODIFYTYPE" ]
    do
        echo -n "Which type would you like? [$default_value] "

        if [ -z "$1" ] ; then
            read ANSWER
        else
            echo $1
            ANSWER=$1
        fi

        if [ -z "$ANSWER" ] ; then
           ANSWER=$default_value
        fi

        case $ANSWER in
            1)
                MODIFYTYPE="failed"
                ;;
            2)
                MODIFYTYPE="timeout"
                ;;
            3)
                MODIFYTYPE="all"
                ;;
            *)
                echo
                echo "I didn't understand your response.  Please try again."
                echo
                ;;
            esac

        if [ -n "$1" ] ; then
            break
        fi
    done

}

choosetype 3

RUNMODE=

function choosemode()
{
    echo "Choose mode of cases:"
    echo "    1.Quick"
    local default_value=1
    local ANSWER

    while [ -z "$RUNMODE" ]
    do
        echo -n "Which mode would you like? [$default_value] "
        if [ -z "$1" ] ; then
            read ANSWER
        else
            echo $1
            ANSWER=$1
        fi
        if [ -z "$ANSWER" ] ; then
           ANSWER=$default_value
        fi

        case $ANSWER in
            1)
                RUNMODE="quick"
                ;;
            *)
                echo
                echo "I didn't understand your response.  Please try again."
                echo
                ;;
            esac

        if [ -n "$1" ] ; then
            break
        fi
    done

}

choosemode 1

if [ "$RUNMODE" = "quick" ]; then
    #echo "-----"
    PARSECTSXML=${PARSECTS_ROOT}/modifyresult.py
    #echo $PARSECTSXML
    checkFile ${PARSECTSXML}
    $PARSECTSXML $TESTRESULT $MODIFYTYPE $PPLATFORM
    #echo  $PARSECTSXML $TESTRESULT $MODIFYTYPE $PPLATFORM
    echo "RUMMODE = quick"
fi

if [ -n "$SESSION" ]; then
    ./runcts.py ${RUNCTS} ${SESSION} ${DEVICESID} $SHARDSRUN $PPLATFORM
else
    ./runcts.py ${RUNCTS} ${DEVICESID} $SHARDSRUN $PPLATFORM
fi
