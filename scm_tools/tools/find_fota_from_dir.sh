#!/bin/bash
pwd=`pwd`
until [ $pwd == "/" ]
do
  test -e $pwd/.repo && current_dir=`pwd`&& start_num=$((${#pwd}+1)) && code_start_path=${current_dir:start_num} && break
  pwd=`dirname $pwd`
done
for i in `cat $1/../conf/impact_fota/$2_conf/$2.dir_conf`
do
   #echo "$code_start_path/$3" | grep "$i" >> $4/$5.commit_fotas.txt
   echo "$code_start_path/$3" | grep "$i"
done
