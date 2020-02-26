#!/usr/bin/python
#coding=utf-8

import os
import re
import sys
import commands


def diffManifest():
	projectInfos = {'pixi335-v1.0-dint':'pixi335', 'yarism-v1.0-dint':'yarism', 'pixo7-v1.0-dint':'pixo7', 'yaris35-orange-v1.0-dint':'yaris35_orange', 'mtk6582-v1.0-dint':'soul45', 'yaris35-v1.0-dint':'yaris35', 'pixo-v1.0-dint':'pixo', 'beetlelite-v1.0-dint':'beetle_lite', 'pixi34-tf-v2.0-dint':'pixi34tf', 'pixi3-35-tf-dint-v1.0':'pixi335tf', 'pixi335tf-noEfuse-v1.0-dint':'pixi335tf', 'pixi335tf-v1.0-MR':'pixi335tf', 'pixi3-4.5-4g-v1.0-dint':'pixi3-45-4g','pixi3-4.5-4g-orange-v1.0-dint':'pixi3-45-4g','mtk6755m-m-global-v1.0-dint':'SHINE-PLUS','pixi4-5-4g-v1.0-dint':'pixi4-5-4g','mtk6755m-m-cn-v3.0-dint':'SHINE-PLUS','mtk6737-m-v1.0-dint':['shine-lite', 'shine-4g'], 'mtk6737m-m-cn-v1.0-dint':['shine-lite', 'shine-4g'], 'mickey6-v1.0-dint':['mickey6'],'mtk6737_53-n-v1.0-dint':['mickey6t','simba6l','buzz6e'],'elsa6p-n-v2.0-dint':['elsa6p']}#项目名保证和alps/manifest/int/下面的一致
	bandDict={'pixi335':['EU','US'],'yarism':['EU','US'],'pixo7':[],'yaris35_orange':['EU'],'soul45':['EU'],'yaris35':['EU'],'pixo':[],'beetle_lite':[],'pixi34tf':['US_TF','US_ATT'],'pixi335tf':['US_TF','US_ATT'],'pixi3-45-4g':['US','US_NA'],'SHINE-PLUS':['EU','US','CN','APAC'],'pixi4-5-4g':['EU','US','US1'],'shine-lite':['EU','CN','APAC'],'shine-4g':['EU','CN','APAC'], 'mickey6': ['US2', 'US-2S-DTV'],'mickey6t':['US'],'simba6l':['EU'],'buzz6e':['EU'],'elsa6p':['EU']}#项目名保证和alps/manifest/int/下面的一致，如果value为空，表示该项目的daily版本名没有跟band后缀
	getManifestCmd="ls -l --time-style=long-iso .repo | grep 'manifest.xml' | awk '{print $10}'"#为了保证每次都能取到正确的manifest名字，所以需要加入--time-style=long-iso
	local_manifest_path=commands.getoutput(getManifestCmd).strip()
	if not local_manifest_path:
		for i in range(0, len(projectInfos.keys())):
			print '%d----%s' % (i+1, projectInfos.keys()[i])
		selectedId = raw_input('Please select your branch:')
		selectedId = int(selectedId.strip())
	    	branch_name = projectInfos.keys()[selectedId - 1]
	else:
	    	branch_name=local_manifest_path.split("/")[1][0:-4]
	print "branch_name:",branch_name
	projectName=projectInfos[branch_name]
	if type(projectName) == list:
	    	for i in range(0, len(projectName)):
			print '%d----%s' % (i+1, projectName[i])
	    	selectedId = raw_input('Please select your project:')
		selectedId = int(selectedId.strip())
	    	projectName = projectName[selectedId - 1]
	print 'projectName:',projectName
	lastVersion,lastManifest=getLastInfo(bandDict,projectName)
	curVersion,curManifest=getCurrentInfo(bandDict,projectName,local_manifest_path)
	lastDict=getLastDict(lastManifest)
	curDict=getCurDict(curManifest)
	diffDict=getDiffDict(lastDict,curDict)
	diffInfoDict=getDiffInfoDict(diffDict,branch_name)
	geneHtml(diffInfoDict,lastVersion,curVersion,projectName)

def getLastInfo(bandDict,projectName):
	lastVersion = sys.argv[1]
	isDailyVersionLast=isDailyVersion(lastVersion)
	lastManifest=".repo/manifests/int/"+projectName+"/v"+lastVersion+".xml"#正式版本和daily版本名没有跟band后缀的情况
	if isDailyVersionLast and bandDict[projectName]:#daily版本名有band后缀的情况
		for eachBand in bandDict[projectName]:
			if os.path.exists(".repo/manifests/int/"+projectName+"/v"+lastVersion+"-"+eachBand+".xml"):
				lastManifest = ".repo/manifests/int/"+projectName+"/v"+lastVersion+"-"+eachBand+".xml"
				break
	return lastVersion,lastManifest

def getCurrentInfo(bandDict,projectName,local_manifest_path):
	if len(sys.argv) == 3:#要比较的新版本是一个确定的版本
		curVersion = sys.argv[2]
		isDailyVersionCurrent=isDailyVersion(curVersion)
		curManifest = ".repo/manifests/int/"+projectName+"/v"+curVersion+".xml"#正式版本和daily版本名没有跟band后缀的情况
		if isDailyVersionCurrent and bandDict[projectName]:#daily版本名有band后缀的情况
			for eachBand in bandDict[projectName]:
				if os.path.exists(".repo/manifests/int/"+projectName+"/v"+curVersion+"-"+eachBand+".xml"):
					curManifest = ".repo/manifests/int/"+projectName+"/v"+curVersion+"-"+eachBand+".xml"
					break
	elif len(sys.argv) == 2:#要比较的新版本就是当前本地最新代码
		curVersion = "Local Code"
		curManifest = ".repo/"+local_manifest_path
	return curVersion,curManifest

def getLastDict(lastManifest):
	lastDict={}
	for line in file(lastManifest).readlines():
		match = re.search('<project\s+name=\"([^\"]+)\"\s+path=\"([^\"]+)\"\s+revision=\"([^\"]+)\"', line)
		if match:
			lastDict[match.group(2).strip()] = [match.group(1).strip(), match.group(3).strip()]
	return lastDict

def getCurDict(curManifest):
	curDict={}
	for line in file(curManifest).readlines():
		if len(sys.argv) == 3: 
			match = re.search('<project\s+name=\"([^\"]+)\"\s+path=\"([^\"]+)\"\s+revision=\"([^\"]+)\"', line)
			if match:
				curDict[match.group(2).strip()] = [match.group(1).strip(), match.group(3).strip()]
		elif len(sys.argv) == 2:
			match = re.search('<project\s+name=\"([^\"]+)\"\s+path=\"([^\"]+)\"', line)
			if match:
				localGitPath=match.group(2).strip()
				localCommitId=commands.getoutput('cd '+localGitPath+';git log -1 | head -n 1 |awk \'{print $2}\'').strip()
				curDict[match.group(2).strip()] = [match.group(1).strip(), localCommitId]
	return curDict

def getDiffDict(lastDict,curDict):
	diffDict={}
	deletedPaths=list(set(lastDict.keys()) - set(curDict.keys()))#find deleted git
	if deletedPaths:
		for eachPath in deletedPaths:		
			lastGit=lastDict[eachPath][0]
			if eachPath[:8] in ['version', 'version-', 'version_'] or os.path.basename(lastGit)[:8] in ['version', 'version-', 'version_']:
				continue
			lastCommit=lastDict[eachPath][1]
			diffDict[eachPath]=[lastGit,[lastCommit,""]]
			lastDict.pop(eachPath)
	addedPaths=list(set(curDict.keys()) - set(lastDict.keys()))#find added git
	if addedPaths:
		for eachPath in addedPaths:
			curGit=curDict[eachPath][0]
			if eachPath[:8] in ['version', 'version-', 'version_'] or os.path.basename(curGit)[:8] in ['version', 'version-', 'version_']:
				continue
			curCommit=curDict[eachPath][1]
			diffDict[eachPath]=[curGit,["",curCommit]]
			curDict.pop(eachPath)
	for eachPathCur in sorted(curDict.keys()):
		for eachPathLast in sorted(lastDict.keys()):
			curGit=curDict[eachPathCur][0]
			lastGit=lastDict[eachPathLast][0]
			if eachPathLast[:8] in ['version', 'version-', 'version_'] or os.path.basename(curGit)[:8] in ['version', 'version-', 'version_']:
				continue
			curCommit=curDict[eachPathCur][1]
			lastCommit=lastDict[eachPathLast][1]
			if not isCommitId(curCommit):
				curCommit=tagToCommitId(eachPathCur,curCommit)
			if not isCommitId(lastCommit):
				lastCommit=tagToCommitId(eachPathLast,lastCommit)
			if curGit==lastGit and curCommit==lastCommit:
				lastDict.pop(eachPathLast)
				curDict.pop(eachPathCur)
				break
			if curGit==lastGit and curCommit!=lastCommit:#两个版本之间某一个库有差异
				diffDict[eachPathCur]=[curGit,[lastCommit,curCommit]]
				lastDict.pop(eachPathLast)
				curDict.pop(eachPathCur)
				break
	return diffDict

def getDiffInfoDict(diffDict,branch_name):
	diffInfoDict={}
	for eachPath in diffDict.keys():
		gitName=diffDict[eachPath][0]
		lastCommit=diffDict[eachPath][1][0]
		curCommit=diffDict[eachPath][1][1]
		if not lastCommit:
			diffInfoDict[eachPath]={'gitName':gitName,'commitList':[],'modifiedFiles':'','remark':'last none'}
		elif not curCommit:
			diffInfoDict[eachPath]={'gitName':gitName,'commitList':[],'modifiedFiles':'','remark':'cur none'}
		else:
			commitList=commands.getoutput('cd '+eachPath+';git rev-list '+lastCommit+'..'+curCommit).splitlines()
			commitList.reverse()
			modifiedFiles=commands.getoutput('cd '+eachPath+';git diff --name-only '+lastCommit+'..'+curCommit+' | sed \':label;N;s/\\n/<br\/>/;b label\'')
			modifiedFiles_new=checkFotaAndSys(modifiedFiles,branch_name,eachPath)#检查两个版本之间修改的文件是否影响fota和system
			diffInfoDict[eachPath]={'gitName':gitName,'commitList':commitList,'modifiedFiles':modifiedFiles_new,'remark':''}
	return diffInfoDict
		



def geneHtml(diffInfoDict,lastVersion,curVersion,projectName):
	html=geneHtmlHead()
	html += "<script> function toggle(id,key){ var show=document.getElementById(id).style.display;if(show=='none'){document.getElementById(id).style.display='block';document.getElementById(key).innerHTML='[Comments Hide]'} else{document.getElementById(id).style.display='none';document.getElementById(key).innerHTML='[Comments Details]'}}</script>"
	html += "<script> function toggle_(id,key){ var show=document.getElementById(id).style.display;if(show=='none'){document.getElementById(id).style.display='block';document.getElementById(key).innerHTML='Modified Files Hide'} else{document.getElementById(id).style.display='none';document.getElementById(key).innerHTML='Modified Files Details'}}</script>"
	html += '<body><h1><span style="color:blue">'+projectName+'</span><br/><span style="color:red">'+lastVersion+'</span>'+' and '+'<span style="color:red">'+curVersion+'</span>'+' Diff Result</h1><br/>'
	html += '<table>'
	index=1
	idIndex=1
	bugNumberLists=[]
	bugNumbers=''
	for eachPath in diffInfoDict.keys():
		gitName=diffInfoDict[eachPath]['gitName']
		splitPath=gitName.split("/",1)
		gitName1=splitPath[0]
		gitName2=splitPath[1]
		commitList=diffInfoDict[eachPath]['commitList']
		modifiedFiles=diffInfoDict[eachPath]['modifiedFiles']
		remark=diffInfoDict[eachPath]['remark']
		isRemarked=False
		if commitList:#commitList不为空的时候，remark肯定为空
			commitInfoDict,bugNumberList=getCommitInfoDict(eachPath,commitList)
			if bugNumberList:
				for eachBug in bugNumberList:
					if eachBug not in bugNumberLists:
						bugNumberLists.append(eachBug)
			if not commitInfoDict:#如果getCommitInfoDict为空，表示两个版本之间的commit都是merge的，表示这个git库根本没有被修改，所以此时不需要列出来
				continue
			else:
				html += '<tr><td><b style="font-size:18px">'+str(index)+')'+gitName+'</b></td></tr>'
				for eachCommit in commitList:
					if eachCommit in commitInfoDict.keys():
						html += '<tr><td><a href=http://10.92.32.10/sdd2/gitweb-'+gitName1+'/?p='+gitName2+'.git;a=commit;h='+eachCommit+'>10.92.32.10/sdd2/gitweb-'+gitName1+'/?p='+gitName2+'.git;a=commit;h='+eachCommit+'</a><span id=\'show'+str(idIndex)+'\' onclick=\"toggle(\'comment'+str(idIndex)+'\',\'show'+str(idIndex)+'\');\" style="cursor:pointer;color:#FFCC33">[Comments Details]</span></td></tr>'
						html += '<tr><td><div id=\'comment'+str(idIndex)+'\' style="display:none;background-color:#FFCC33">'+commitInfoDict[eachCommit]+'<span></div></td></tr>'
						idIndex += 1
		else:#commitList为空，remark肯定不为空
			html += '<tr><td><b style="font-size:18px">'+str(index)+')'+gitName+'</b></td></tr>'
			if remark=='last none':
				isRemarked=True
				html += '<tr><td style="color:red">this git did not exists in '+lastVersion+' but added in '+curVersion+'</td><tr/>'
			elif remark=='cur none':
				isRemarked=True
				html += '<tr><td style="color:red">this git exists in '+lastVersion+' but has been deleted in '+curVersion+'</td><tr/>'
		if not isRemarked:#没有remark，表示两个版本之间对于当前git库都存在，需要输出modifiedFiles
			#如果两个版本之间修改的文件中存在影响fota和system的文件，则会在页面的modefied files前面提示
			isOnlyImpactFota=modifiedFiles.find('color:red')#仅仅存在影响fota的文件
			isOnlyImpactSys=modifiedFiles.find('color:purple')#仅仅存在影响system的文件
			isImpactFotaAndSys=modifiedFiles.find('color:yellow')#同时存在影响fota和system的文件
			if isOnlyImpactFota != -1:
				html += '<tr><td><b style="font-size:15px;color:red">There are some changes may impact fota</b></td></tr>'
			if isOnlyImpactSys !=-1:
				html += '<tr><td><b style="font-size:15px;color:red">There are some changes may impact system</b></td></tr>'
			if isImpactFotaAndSys != -1:
				html += '<tr><td><b style="font-size:15px;color:red">There are some changes may impact fota and system</b></td></tr>'
			html += '<tr><td><div id=\'showDiff'+str(index)+'\' onclick=\"toggle_(\'diff'+str(index)+'\',\'showDiff'+str(index)+'\');\" style="font-size:20px;cursor:pointer;color:#006699">Modified Files Details</div></td></tr>'
			html += '<tr><td><div id=\'diff'+str(index)+'\' style="display:none;background-color:#3399CC">'+modifiedFiles+'</div></td></tr>'
		index +=1
	html += '</table>'
	html += '<h2><span style="color:blue">Between </span><span style="color:red">'+lastVersion+'</span>'+' and '+'<span style="color:red">'+curVersion+'</span> Tasks/Defects:</h2>'
	html += '<span style="font-size:20px;color:red">Total: </span><span style="color:green">'+str(len(bugNumberLists))+'</span><br/>'
	html += '<span style="font-size:20px;color:red">Tasks/Defects List:</span><br/>'
	for eachBug in sorted(bugNumberLists):
		bugNumbers += eachBug+'<br/>'
	html += '<div>'+bugNumbers+'</div>'
	html += '</body></html>'
	result_file_name = "%s_%s.html" % (lastVersion, curVersion)
	output=open(result_file_name, 'w')
	output.write(html)
	output.close()
			
def checkFotaAndSys(modifiedFiles,branch_name,path):
	diffManifest_name = os.popen("basename "+sys.argv[0]).read().strip()
	diffManifest_path = sys.argv[0][0:-len(diffManifest_name)]
	fota_conf_path=diffManifest_path+"../conf/impact_fota/"+branch_name+"_conf"
	sys_conf_path=diffManifest_path+"../conf/impact_sys/"+branch_name+"_conf"
	modifiedFiles_list=modifiedFiles.strip('\n').split('<br/>')
	modifiedFiles_new=""
	if modifiedFiles_list:
		for each_modifiedFile in modifiedFiles_list:
			isImpactFota=checkFota(each_modifiedFile,fota_conf_path,branch_name,diffManifest_path,path)
			isImpactSys=checkSys(each_modifiedFile,sys_conf_path,branch_name,diffManifest_path,path)
			if isImpactFota and not isImpactSys:#两个版本之间修改的文件中只是存在影响fota的文件
				modifiedFiles_new += "<span style='color:red'>"+each_modifiedFile+"</span><br/>"
			elif not isImpactFota and isImpactSys:#两个版本之间修改的文件中只是存在影响system的文件
				modifiedFiles_new += "<span style='color:purple'>"+each_modifiedFile+"</span><br/>"
			elif isImpactFota and isImpactSys:#两个版本之间修改的文件中同时存在影响fota和影响system的文件
				modifiedFiles_new += "<span style='color:yellow'>"+each_modifiedFile+"</span><br/>"
			else:
				modifiedFiles_new += each_modifiedFile+"<br/>"
	return modifiedFiles_new

def checkFota(each_modifiedFile,fota_conf_path,branch_name,diffManifest_path,path):
	isImpactFota=False
	if os.path.exists(fota_conf_path+"/"+branch_name+".file_conf"):
 		fota_file_info=os.popen("cat "+fota_conf_path+"/"+branch_name+".file_conf | grep "+path+"/"+each_modifiedFile).read().strip()
		if fota_file_info == '':
			if os.path.exists(fota_conf_path+"/"+branch_name+".dir_conf"):
				fota_dir_info=os.popen(diffManifest_path+"find_fota_from_dir.sh "+diffManifest_path+" "+branch_name+" "+each_modifiedFile).read().strip();
				if fota_dir_info != '':
					isImpactFota=True
				else:
					isImpactFota=False
		else:
			isImpactFota=True
	return isImpactFota

def checkSys(each_modifiedFile,sys_conf_path,branch_name,diffManifest_path,path):
	isImpactSys=False
	if os.path.exists(sys_conf_path+"/"+branch_name+".file_conf"):
		sys_file_info=os.popen("cat "+sys_conf_path+"/"+branch_name+".file_conf | grep "+path+"/"+each_modifiedFile).read().strip()
		if sys_file_info == '':
			if os.path.exists(sys_conf_path+"/"+branch_name+".dir_conf"):
				sys_dir_info=os.popen(diffManifest_path+"find_sys_from_dir.sh "+diffManifest_path+" "+branch_name+" "+each_modifiedFile).read().strip();
				if sys_dir_info != '':
					isImpactSys=True
				else:
					isImpactSys=False
		else:
			isImpactSys=True
	return isImpactSys
	

def geneHtmlHead():
	html = '<html xmlns="http://www.w3.org/1999/xhtml"><br/>'
        html += '<head><br>'
        html += '<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />'
        html += '<style type="text/css">'
        html += '</style>'
        html += '<title>Diff Manifest</title>'
        html += '</head>'
	return html

def getCommitInfoDict(eachPath,commitList):
	commitInfoDict={}
	bugNumberList=[]
	for eachCommit in commitList:
		isMerge=False
		isEmptyCommit=True
		gitLog = commands.getoutput('cd '+eachPath+';git log -n1 --raw %s' % eachCommit.strip())
		for line in gitLog.split('\n'):
			match = re.match('^Merge:\s', line)
			if match:
				isMerge=True
				break
			match = re.match(':(\w{6})\s(\w{6})\s(\w{7})\.\.\.\s(\w{7})\.\.\.\s([AMD])\s+(.+)', line)
			if match:
				isEmptyCommit=False
				break
			match = re.match('^[\s]{4}###%%%bug\snumber:(.+)', line)
			if not isMerge and match:
                                bugNumber = match.group(1).strip()
				if bugNumber.find(','):
                                        allBugs=bugNumber.split(',')
                                else:
                                        allBugs=list(bugNumber)
				for eachBug in allBugs:
					if eachBug not in bugNumberList:
						bugNumberList.append(eachBug)
		if isMerge or isEmptyCommit:
			continue
		else:
			commitInfo=commands.getoutput('cd '+eachPath+';git log '+eachCommit+' -1 | sed \':label;N;s/\\n/<br\/>/;b label\'').strip()
			commitInfoDict[eachCommit]=commitInfo
	return commitInfoDict,bugNumberList
	


def isDailyVersion(version):
	if version.find('-')!=-1:
		return True
	else:
		return False

def tagToCommitId(key,tag):
	commitId=''
	commitIdLines=commands.getoutput('cd '+key+';git show -s --format=%H '+tag).strip()
	commitIdInfos=commitIdLines.split('\n')
	for eachInfo in commitIdInfos:
		match=re.search('[a-z0-9]{40}',eachInfo)
		if match:
			commitId=eachInfo.strip()
	return commitId
			
def isCommitId(commitIdOrTag):
	match=re.match('[a-z0-9]{40}',commitIdOrTag)
	if match:
		return True
	else:
		return False
	
def checkArgs(args):
	if len(args)>2:
		print "the args must be 1 or 2 versions"
		sys.exit(1)
	else:
		return True


if __name__ == '__main__':
	isArgsRight=checkArgs(sys.argv[1:])
	if  isArgsRight:
		diffManifest()










