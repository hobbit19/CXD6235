#!/bin/bash
pwd=`pwd`
until [[ $pwd == "/" ]]
do
  current_dir=`pwd` && start_num=$((${#pwd}+1)) && code_start_path=${current_dir:start_num}
  pwd=`dirname $pwd`
done
for i in `cat $1/../conf/$4/$2_dir_config`
do
   #echo "$code_start_path/$3" | grep "$i" >> $4/$5.commit_fotas.txt
   echo "$code_start_path/$3" | grep "$i"
done
