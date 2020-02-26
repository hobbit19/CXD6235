#!/bin/bash
pwd=`pwd`
until [[ $pwd == "/" ]]
do
  test -e $pwd/.repo && current_dir=`pwd`&& start_num=$((${#pwd}+1)) && code_start_path=${current_dir:start_num} && break
  pwd=`dirname $pwd`
done
for i in `cat $1/../conf/impact_customization/$2_conf/$2.media_conf`
do
   echo "$code_start_path/$3" | grep "$i"
done
for i in `cat $1/../conf/impact_customization/$2_conf/$2.audio_conf`
do
   echo "$code_start_path/$3" | grep "$i"
done
for i in `cat $1/../conf/impact_customization/$2_conf/$2.wallpaper_conf`
do
   echo "$code_start_path/$3" | grep "$i"
done
