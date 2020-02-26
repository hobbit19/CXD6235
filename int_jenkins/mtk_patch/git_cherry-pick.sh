#!/bin/bash
#/home/int/git_cherry-pick.txt dir_import patch_num branch
#E.G /home/int/git_cherry-pick.txt /local/mtk_patch_import/t-alps-release-o1.mp1-V1.112-import/system/bt 3 mtk6739-mp1-1.112-o1-a3a-v2.0-bsp
#
#
if [ $# -ge 3 ]
then 
   dir_import=$1
   patch_num=$2
   branch=$3
else
   dir_import=/local/mtk_patch_import/t-alps-release-o1.mp1-V1.112-import/system/bt
   patch_num=3
   branch=mtk6739-mp1-1.112-o1-a3a-v2.0-bsp
fi
dir_current=`pwd`
folder=`echo $dir_current | awk -F / '{print $6}'`
#cd to import to get log
cd "$dir_import"
tmpstr=`git log -$patch_num | grep commit | awk '{print $2}'`
cd "$dir_current"
#revert strings
for tmp in $tmpstr; do tmpstrs="$tmp $tmpstrs"; done
#git cherry-pick
for tmp in $tmpstrs; do git cherry-pick $tmp; sleep 0.1; done
#git push
git push jgs HEAD:$branch
