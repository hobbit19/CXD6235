#!/bin/bash
pwd=`pwd`
until [[ $pwd == "/" ]]
do
  test -e $pwd/.repo && current_dir=`pwd`&& start_num=$((${#pwd}+1)) && code_start_path=${current_dir:start_num} && break
  pwd=`dirname $pwd`
  echo $pwd
done

