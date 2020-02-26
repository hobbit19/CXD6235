#!/usr/bin/python

import sys
import os
import re
import urllib
import urllib2
#import pygtk
import tempfile
from commands import *
import json
import xml.etree.ElementTree as ET
import MySQLdb
import datetime
import time
#import Tkinter

audio_tolist=['meng.zhang@tcl.com']
perso_tolist=['junbo.zeng@tcl.com','liyun.liu@tcl.com','forong.li@tcl.com','xiaodan.chen@tcl.com','ting.liu.hz@tcl.com','ruping.pan.hz@tcl.com','shuangyan.he@tcl.com','dingyuan.he@tcl.com','shie.zhao@tcl.com']
fota_tolist=['jinguo.zheng@tcl.com','yangning.hong.hz@tcl.com','shie.zhao@tcl.com','xuanfeng.ye.hz@tcl.com','yanhong.liu@tcl.com','chuanbo.qiu.hz@tcl.com','xiangjm@tcl.com','haihui.jiang.hz@tcl.com','yange.zhang@tcl.com','chaofei.wu.hz@tcl.com','wenhui.xu.hz@tcl.com','haibo.zhong.hz@tcl.com']
tee_tolist=['lixiang.he@tcl.com','haihui.jiang.hz@tcl.com','jinguo.zheng@tcl.com','haibo.zhong.hz@tcl.com','shie.zhao@tcl.com']
persist_tolist=['lixiang.he@tcl.com','yizhi.fu@tcl.com','haihui.jiang.hz@tcl.com','jinguo.zheng@tcl.com','haibo.zhong.hz@tcl.com','shie.zhao@tcl.com']
FRM1_reviewer=['yadong.yang@tcl.com','gaoxiang.li@tcl.com']
ROM_File={"aosp-q-v3.0-tctrom": "TCTROM-Q-V3.0-dev_list.txt","mtk6761-morgan4g-q-v3.0-tctrom": "TCTROM-Q-V3.0-dev_list.txt","q6150-t1-q-v3.0-tctrom":"TCTROM-Q-V3.0-dev_qualcom.txt"}
#ROM_File={'sdm429-p-v2.0-dint': 'qualcomm_list.txt', "mtk6762-a5x-p-v1.0-tctrom": "mediatek_list.txt", "aosp-q-v3.0-tctrom": "TCTROM-Q-V3.0-dev_list.txt","mtk6761-morgan4g-q-v3.0-tctrom": "TCTROM-Q-V3.0-dev_list.txt"}

togetherconfig_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/togetherconfig";
db_conn = MySQLdb.connect(host="10.92.35.20", port=3306, user="INT_PATCH", passwd="Aa123456",db="dotproject",charset="utf8") #, charset="gbk"latin1
db_cursor = db_conn.cursor()
#togetherconfig_link = "/local/tools/scm_tools/conf/togetherconfig";



#add by yinfang.lai 2017-02-16
def getmtkproject(project):
    url = 'http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/impactconfig'
    f = urllib.urlopen(url, proxies={})
    mtkprojectList =[]
    for line in f:
        m = re.match('^\s*([^\s]+)\s+([^\s]+)', line)
        #print line
        if m:                             
            if m.group(1) == project:
                projectStr = m.group(2)
                mtkprojectList = projectStr.split("@")
                return mtkprojectList		
            else:
                continue
    #return 0
    return mtkprojectList
#add by yinfang.lai 2017-02-16 end	


#add by lyf 2017-3-15 for gapp name check
def check_gapp_name_rule_check(commitlist):
    gitname = get_gitname()
    delapklist = get_del_apks_list()
    if "wcustores" in gitname:
	    for file_name in commitlist :
		if '.apk' in file_name:
		    tmp = file_name[3:]
		    if tmp in delapklist:
		    	continue
		    get_gapp_name_rule_check(tmp)

    return True
    
def get_gapp_name_rule_check(apk_name):
    print "apk_name",apk_name
    if not re.search("IME|GMS",apk_name):
        filterDirArray=['Touchpal_Pack']
        indexDir = False
        for item in filterDirArray:
            if re.search(item,apk_name):
                indexDir = True
                break;
        apk_name[apk_name.rfind('/')+1:]
        #print "echo %s | grep -i 'Global\|CN2\|Orange\|LATAM\|Naranya\|india'\n" %apk_name;
        cmd = getoutput("echo %s | grep -i 'Global\|GL\|PG\|CN2\|CN\|Orange\|LATAM\|Naranya\|india\|telus\|MTK\|NA\|EU'"%apk_name);
        if cmd == '' and indexDir==False:
	   print "\033[31;1m %s name is illegitmacy,must contain country. Country can be Global|GL|PG|CN2|CN|Orange|LATAM|Naranya|india\|telus\|MTK\|NA\|EU \033[0m\n"%apk_name;
	   print "\033[31;1m if you have to sumbit new Country,Please contact VER Team to Update patch_deliver tools!\033[0m\n";
	   sys.exit(1)
    return True
    
    
def get_gitname():
        patch_delivery_path = sys.path[0]                               
        repo_path_1 = getoutput('bash %s/find_repo_path.sh' %patch_delivery_path) 
        repo_path_2 = repo_path_1.split("\n")
        repo_path = repo_path_2[-1]+'/'
        current_path = getoutput("pwd")
        git_path = current_path.replace(repo_path,'')    
        return git_path
          
def together_git_remind_check(product_name):
	together_project=[]
	together_gits=[]
	
	together_projects,together_gits = get_togetherinfo(product_name)
	git_name = get_gitname()
        for each_git in together_gits: 
            if each_git == "GITNAME":
                continue
            if each_git == "all":
                return together_projects  
            preg_var = "/.*%s.*/"%each_git;
            m = re.match(preg_var, each_git)
            if m:  
            	return together_projects
        return []


def get_togetherinfo(product_name):
    together_projects=[]
    together_gits = []
    f = urllib.urlopen(togetherconfig_link, proxies={})
    for line in f:
        m = re.match('^\s*([^\s]+)\s+([^\s]+)', line)
        listproject = line.split(",")
        if len(listproject) <3 or listproject[0] != product_name:
            continue
        together_projects = listproject[1].split("#")
        del together_projects[0]
        together_gits = listproject[2].split("#")
    return together_projects,together_gits

#end by lyf 2017-3-15 for together info check

def get_del_apks_list():
    delList = []
    temp5 = os.popen("git ls-files -d | awk '{print  $1}'")
    for lines in temp5.readlines():
        delList.append(lines[:-1])
    return delList
       
def check_upload_type(commitlist):
    gitname = get_gitname()
    print gitname
    apk_flag = False
    code_flag = False;
    translate_flag = False;
    for	file_name in commitlist:
        file_name = file_name[3:]
        print file_name 
        if '.apk' in file_name or re.match('.*all_GMS_app.mk$|.*custpack_app_list.mk$|.*googleapk.mk', file_name):
            apk_flag = True
        elif '.xls' in file_name and 'wlanguage' in gitname:
            translate_flag = True
        else:
            conde_flag = True
        if code_flag:
            return 0
        elif translate_flag:
            return 2
        elif apk_flag:
            return 1
        else:
            return 0
								
def upload_exception_to_simplex(UPLOAD_URL, post_data):
    data = urllib.urlencode(post_data)
    req = urllib2.Request(UPLOAD_URL, data)
    response = urllib2.urlopen(req)
    the_page = response.read()
    response.close()
    the_page=json.loads(the_page)
    return the_page
				
def deal_exception_to_simplex(jrd_bug_cause,defects,commitlist,author_email):
    if jrd_bug_cause != 'GAPP':
        defectList = defects.split(',')
        for defect in defectList:
            HOST_URL = 'http://10.92.35.176/pms'
            UPLOAD_URL = HOST_URL+"/project/xrinfo/0/updateUploadedCodeStatus"
            upload_type = check_upload_type(commitlist);
            post_data = {}
            post_data['xrNo'] = defect
            post_data['uploadedCodeUserMail'] = author_email
            post_data['uploadedType']=upload_type
            post_data_str = json.dumps(post_data)
            post_data = {}
            post_data['jsonContent'] =post_data_str
            upload_exception_to_simplex(UPLOAD_URL, post_data)    
            
def get_owner_team_from_simplex(author_email):
    if jrd_bug_cause != 'GAPP':
        defectList = defects.split(',')
        for defect in defectList:
            HOST_URL = 'http://10.92.35.176/pms'
            UPLOAD_URL = HOST_URL+"/project/xrinfo/0/updateUploadedCodeStatus"
            upload_type = check_upload_type(commitlist);
            post_data = {}
            post_data['xrNo'] = defect
            post_data['uploadedCodeUserMail'] = author_email
            post_data['uploadedType']=upload_type
            post_data_str = json.dumps(post_data)
            post_data = {}
            post_data['jsonContent'] =post_data_str
            upload_exception_to_simplex(UPLOAD_URL, post_data)    
            
#add by lyf 2017-04-21 for frameworks-res 
def get_team_from_email(owner_mail):
    frm_flag = 'other'
    GET_URL ="http://10.92.35.176/pms/project/patch-delivery/0/getOrgByEmail?email="+owner_mail;
    try:  
        response = urllib2.urlopen(GET_URL)  
        print response.info()  
        readline = response.read()  
        if readline:
            Type = json.loads(readline)
	    if (len(Type['data'])):
	        if(Type['data']['orgName'] == 'FRM1'):
	    	    frm_flag = 'FRM1'
    except urllib2.HTTPError, e: 
    	print "get team error from simplex!!" 
        #print e.info()  
        #print e.read()
    	#get team from simplex error,then search local config
    	patch_delivery_path = sys.path[0]  
    	frm_config_file = patch_delivery_path+"../conf/frmteam_config"
    	cmd_frmres = getoutput("cat %s | grep %s " %(frm_config_file ,owner_mail))
    	if cmd_frmres != '':
            frm_flag = 'FRM1'
    
    print frm_flag
    return frm_flag

def update_xr_rootcause_to_simplex(defects,rootcause,problemfr,defectQuality,actualHour):
    defectList = defects.split(',')
    for defect in defectList:
        GET_URL ="http://10.92.35.176/pms/project/patch-delivery/0/addRootCause?xrNo="+defect+"&rootCause="+rootcause+"&problemFrNo="+problemfr+"&defectQuality="+defectQuality+"&actualResolvedTime"+actualHour
        try:
            response = urllib2.urlopen(GET_URL)
            readline = response.read() 
        except urllib2.HTTPError, e:  
            print "update xr rootcause to simplex error !!" 

            
def check_frameworks_res_submit(owner_mail,commitlist):
    git_name = get_gitname()
    print git_name 
    if git_name == "frameworks":
    	frm_owner = get_team_from_email(owner_mail)
    	if frm_owner == 'FRM1':
    	    return False
        for file_name in commitlist:
            file_name = file_name[3:]
            if "base/core/res" in file_name:
                #print "\033[31;1m Only FRM1 Team can write path: frameworks/base/core/res!!!\033[0m\n"
            	#sys.exit(0)
                return True

def add_FRM1_reviewer_frameworks_res_submit(name,git,changeId,server):
    arr_reviewer = read_review_Config('reviewconfig')
    FRM1_reviewer.extend(arr_reviewer)
    reviewer= ' -a '.join(FRM1_reviewer)
    reviewer = ' -a ' + reviewer
    reviewer_cmd = "ssh -o ConnectTimeout=8 -p 29418 %s@%s gerrit set-reviewers -p %s %s %s" %(name,server,git,reviewer,changeId)
    print reviewer_cmd
    os.system(reviewer_cmd)
    print "\n Need FRM1 Team to review the path: frameworks/base/core/res!!! Has added FRM1 Team to review on Gerrrit!!!";
# begin add by xiaoying.huang
def add_reviewer_frameworks_res_submit(name,git,changeId):
    arr_reviewer = read_review_Config('reviewconfig')
    if arr_reviewer.__len__()>0:
        reviewer= ' -a '.join(arr_reviewer)
        reviewer = ' -a ' + reviewer
        reviewer_cmd = "ssh -o ConnectTimeout=8 -p 29418 %s@%s gerrit set-reviewers -p %s %s %s" %(name,server,git,reviewer,changeId)
        print reviewer_cmd
        os.system(reviewer_cmd)
        print "\n Need Team to review the path: frameworks/base/core/res!!! Has added FRM1 Team to review on Gerrrit!!!";

def read_review_Config(configfile):
    plf=sys.path[0]+"/../conf/"+configfile;
    Dict = []
    if os.path.exists(plf):
        plfFile = open(plf, 'r')
        lines = plfFile.readlines()
        for line in lines:
            Dict.append(line.strip())
        plfFile.close()
    return Dict
    
#end by xiaoying.huang

def check_plf_contect(commitlist,branchname):
    print "===begin to check plfs=== "
    gitname = get_gitname()
    delapklist = get_del_apks_list()
    mtkprojectList = getmtkproject(branchname)
    checked_plfs=[]
    flag=False
    if "wprocedures" in gitname:
	    for file_name in commitlist :
		if '.plf' in file_name:
		    tmp = file_name[3:]
		    if tmp in delapklist:
		    	continue
                    for each_project_name in mtkprojectList:
                        if each_project_name in tmp:
                           flag=True
                           for each_project_name_other in mtkprojectList:
                                checkplf = tmp.replace(each_project_name,each_project_name_other)
                                if os.path.exists(checkplf) and checkplf not in checked_plfs:
                                    get_plfs_contect_check(checkplf,branchname,each_project_name_other)
                                    checked_plfs.append(checkplf)
                           break
                             
                    if flag == False:
		        get_plfs_contect_check(tmp,branchname,'')
    return True

def readConfig(plf):
        Dict = {}
        plfFile = open(plf, 'r')
        lines = plfFile.readlines()
        for line in lines:
                line = line.strip();
                if line.startswith('<SDMID>'):
                        SMNID = re.match('<SDMID>(.+)</SDMID>', line).group(1)
                elif line.startswith('<VALUE>'):
                        VALUE = re.match('<VALUE>(.+)</VALUE>', line).group(1)
                        Dict[SMNID] = VALUE
        plfFile.close()
        return Dict

def get_plfs_contect_check(subFilePath,branchname,each_project_name):
        #print "check plf:%s\n" %subFilePath
        subtree = ET.parse(subFilePath)
        #if subtree:
            #print "Elementtree ok,go next!";
        #else:
            #print "\033[31;1m PLF error!! %s ElementTree error \033[0m\n" %subFilePath           
            #sys.exit(0)
        subsimple_vars = subtree.getiterator(tag='SIMPLE_VAR')       
        for simple_var in subtree.getiterator(tag='SIMPLE_VAR'):
                sdmid = simple_var.find("SDMID")
                if ET.iselement(sdmid):
                        for eachsubvar in subsimple_vars:
                                if sdmid.text == eachsubvar.find("SDMID").text:                                 
                                        if ' ' in sdmid.text:
                                            print "\033[31;1m Error!! SDMID < %s > include space characters \033[0m\n" %sdmid.text
                                            print "\033[31;1mFile is %s \033[0m\n" %subFilePath 
                                            sys.exit(0)
        if each_project_name and "isdm_thirdparty.plf" not in subFilePath:           
                  jrdplf = subFilePath.replace(each_project_name,'jrdhz')
                  if os.path.exists(jrdplf):
                      SDMID_B=readConfig(jrdplf)
                      SDMID_A=readConfig(subFilePath)               
                      for SDMID in SDMID_A.keys():
                         if SDMID not in SDMID_B.keys():
                             print "\033[31;1m Error!! %s SDMID<%s> not in %s \033[0m\n" %(subFilePath,SDMID, jrdplf)
                             sys.exit(0)

def get_defect_result_from_simplex(defect):
        HOST_URL = 'http://10.92.35.176/pms'
        UPLOAD_URL = HOST_URL+"/project/xrinfo/0/getProductTaskStateInfo"
        post_data = {}
        post_data['taskNo'] = defect
        post_data_str = json.dumps(post_data)
        post_data = {}
        post_data['jsonContent'] =post_data_str
        result=upload_exception_to_simplex(UPLOAD_URL, post_data) 
        return result


def check_simplex_xr_status(defect,author_email,xr_tool_value):
        GET_URL ="http://10.92.35.176/pms/project/patch-delivery/0/xrInfo?xrNo="+defect+"&email="+author_email+"&dataSourceType="+xr_tool_value

        print GET_URL
        try:
            response = urllib2.urlopen(GET_URL)
            readline = response.read() 
            reslut = json.loads(readline)
        except urllib2.HTTPError, e:  
            print "login to to simplex error !!" 
        print reslut

        return result

#start add by renzhi.yang 20170817
def close_db(db_conn):
    print "%s Close the connection of prsm database..." % datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    if db_conn:
        print 'Closed DB connecion.'
        db_conn.close()

def query(sql, one_record=True):
    try:
        db_cursor.execute(sql)
        if one_record:
            result = db_cursor.fetchone()
        else:
            result = db_cursor.fetchall()
        return result
    except Exception, e:
        print "Exception:%s\nWhile execute %s" % (e, sql)
        sys.exit(1)

def getUnpatchedGits(importid,mergestatus):
    allUnmergesGits = []    
    if len(mergestatus)==2:
        mysql = 'SELECT git_name FROM dotp_mtk_commit WHERE import_id=%s And ismerged!=1'% importid
    else:
        mysql = 'SELECT git_name FROM dotp_mtk_commit WHERE import_id=%s And ismerged=%s'% (importid,mergestatus[0])
    print mysql
    try:
        result = query(mysql, False)
        if result:
            for item in result:
                if item[0] not in allUnmergesGits:
                     allUnmergesGits.append(item[0])                     
        else:
            print "no git name from dotp_mtk_commit which status is %s" % mergestatus
            print "all gits has been patched for this mtk patch"
            #sys.exit(1)                
    except Exception, e:
        print e
        print "connect to prsm failed"
    return allUnmergesGits

def updateMtkpatchStatus(pnum,CodeBranch,patchtype,vnum,gitname,status_value):
    getidsql = "SELECT mp.id as chooseid,mm.gerrit_link FROM (dotp_mtk_merge AS mm ) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='%s' AND mm.merge_patch = '%s' AND mp.patch_type = '%s' and mp.vnum='%s'"%(pnum,CodeBranch,patchtype,vnum)
    print "getidsql",getidsql
    mmresult = query(getidsql, True)
    if not mmresult:
        print "\n--------mm.id should exist mtk_merge--------\n"
        return False
    importid=mmresult[0]
    gerrit_link=mmresult[1]
    print "importid,gerrit_link",importid,gerrit_link
    insertsql = 'UPDATE dotp_mtk_commit SET `ismerged` = %s WHERE import_id = "%s" AND git_name = "%s"' % (status_value,importid,gitname)
    print "insertsql",insertsql
    try:
        db_cursor.execute(insertsql)
        db_conn.commit()
    except Exception, e:
        print e
        print "connect to prsm failed"


def is_git_root():
    gitname = getoutput("ls -al --time-style=long-iso | grep '.git$' | awk '{print  $8}'")
    if gitname==".git":
        return True
    else:
        return False

def get_gitname_mtkpatch():
    mtkcurrentgitname = ''
    tem=getoutput('ls ./.git -al | grep "config"')
    match=re.search('.*.repo/projects/(.*).git/config',tem)
    if match:
        mtkcurrentgitname = match.group(1)
    return mtkcurrentgitname

def get_project_branch(repodir):
    if not repodir:
        return
    project_branch = ''
    status,project_branch = getstatusoutput("ls -al " + repodir + "/.repo/ | grep '\->' | sed -e 's/.*manifests.//'")
    print "status",status
    if status==0:
        project_branch = project_branch.split('.xml')[0]
        print "project_branch",project_branch
        return project_branch
#end add by renzhi.yang 20170817

# begin add by xiaoying.huang 2017-11-09
def get_defect_autoid_from_simplex(reqSourceType,defect,XR_Summary):
        UPLOAD_URL = "http://10.92.35.176/pms/autotest/test-json/0/saveReqFromPatch?"
        post_data = {}
        post_data['reqSourceType'] = reqSourceType
        post_data['reqSourceNo'] = defect
        post_data['reqDesc'] = XR_Summary
        post_data_str = json.dumps(post_data)
        post_data = {}
        post_data['jsonContent'] =post_data_str
        result=upload_exception_to_simplex(UPLOAD_URL, post_data) 
        return result

def update_simplex_userpass(name,author_pass):
        HOST_URL = 'http://10.92.35.176/pms'
        UPLOAD_URL = HOST_URL+"/skill/user-info/0/updateUserPasswordOnline"
        post_data = {}
        post_data['loginName'] = name
        post_data['loginPasswordOnline'] = author_pass
        post_data_str = json.dumps(post_data)
        post_data = {}
        post_data['jsonContent'] =post_data_str
        result=upload_exception_to_simplex(UPLOAD_URL, post_data) 
        return result

# end add by xiaoying.huang 2017-11-09

#begin add by xiaoying.huang 2017-12-15
def get_wentaiConfig(ss):
        plf=sys.path[0]+"/../conf/wentaiconfig";
        gitarr = []
        ss = ss + ":"
        if os.path.exists(plf):
                plfFile = open(plf, 'r')
                lines = plfFile.read()
                if ss in lines:
                        gitstrnum = lines.index(ss)+len(ss)
                        #gitstr = lines.rstrip(ss).rstrip("####").strip()
                        gitstr = lines[gitstrnum:]
                        gitstrnum = gitstr.index("####")
                        gitstr = gitstr[0:gitstrnum].strip()
                        gitarr = gitstr.split('\n')
                plfFile.close()
        return gitarr
# end add by xiaoying.huang 2017-12-15

#add by xiaoying.huang 20180315 check custpack_app_odex_list
def check_custpack_app_odex_list(commitlist):
        find_mkfile = getoutput("find -name 'custpack_app_odex_list.mk'")
        print "find_mkfile========\n",find_mkfile
	gask = False
	if find_mkfile != "":
                for file_name in commitlist:
                        file_name = file_name[3:]
                        if re.match('.*custpack_app_list.mk', file_name):
                              gask = True
	return gask

# end add by xiaoying.huang 2018-03-16

# begin add by xiaoying.huang 20180514
def addgitpushID(bugid,gitname,CommitArr,author_name):
    insertsql = 'INSERT INTO dotp_patch_deliver (defect,git_name,commit_id,author,changeID,TS) VALUES ("%s","%s","%s","%s","%s","%s")'%(bugid,gitname,CommitArr[0],author_name,CommitArr[1],CommitArr[2])
    print "insertsql",insertsql
    try:
        db_cursor.execute(insertsql)
        db_conn.commit()
        print "\n-------INSERT INTO SMARTTASK OK --------"
    except Exception, e:
        print e
        print "\n-------INSERT INTO SMARTTASK error --------"

# end add by xiaoying.huang 20180514
# add by xiaoying.huang 20180530
def getCommitInfo(): 
        CommitArr = []       
        gitcommitlog = getoutput("git log -1 | head -n 3") 
        print "gitcommitlog",gitcommitlog 
        getarr=gitcommitlog.split("\n") 
        gCommitId=getarr[0]
        gCommitdate=getarr[2]
        currentCommitId = gCommitId[len("commit"):].strip()  
        currentCommitDate = gCommitdate[len("Date:"):-5].strip()
        #timestruct=time.strptime(currentCommitDate,'%a %b %d %H:%M:%S %Y')
        #currentCommitDate=time.strftime("%Y-%m-%d %H:%M:%S",timestruct)
        #currentCommitDate=datetime.datetime.strptime(currentCommitDate,'%a %b %d %H:%M:%S %Y').strftime("%Y-%m-%d %H:%M:%S")
        remote_name = getoutput("git remote -v | tail -1 | awk -F' ' '{print $1}'")
        changeIdLine = getoutput("git ls-remote %s refs/changes/* | grep %s | awk '{print $2}'" % (remote_name, currentCommitId))
        changes=changeIdLine.split("/")
        changeId=changes[3]
        CommitArr.append(currentCommitId)
        CommitArr.append(changeId)
        CommitArr.append(currentCommitDate)
        return CommitArr
# add by xiaoying.huang 20180531


# add by xiaoying updateRootCause to simplex 20181109
	
def update_simplex_RootCause(XrNo,rootcause,root_cause_detail,bug_category,tct_bug_category,is_rom):
        HOST_URL = 'http://10.92.35.176/pms'
        UPLOAD_URL = HOST_URL+"/project/xrinfo/0/updateRootCause"
        post_data = {}
        post_data['numberList'] = XrNo
        post_data['root_cause'] = rootcause
        post_data['root_cause_detail'] = root_cause_detail
        post_data['bug_category'] = bug_category
        post_data['tct_bug_category'] = tct_bug_category
        post_data['is_rom'] = is_rom
        post_data_str = json.dumps(post_data)
        post_data = {}
        post_data['jsonContent'] =post_data_str
        result=upload_exception_to_simplex(UPLOAD_URL, post_data)
        return result

def get_peojects_with_onemanifestfile(repo_branch,project_maniest):
    patch_delivery_path = sys.path[0]
    print "patch_delivery_path",patch_delivery_path
    config_file_path_name=patch_delivery_path+"/../conf/diffluncher_in_onecodebranch"
    all_project_name=''
    contenr_list=[]
    if os.path.exists(config_file_path_name):
    	with open(config_file_path_name, 'r') as read:
            for line in read.read().split('\n'):
	        line=line.strip()
	        line=line.strip('\n')
	        contenr_list.append(line)
    	print "contenr_list",contenr_list	
    	for readline in contenr_list:
            if not readline:
	        continue
            else:
	        readline = filter(None,readline.split(" "))
	        print "readline------------",readline
	        if len(readline)<2:
	    	    continue
	        print "readline,project_maniest", readline[0],project_maniest
	        if readline[0] == project_maniest:    
	    	    all_project_name = readline[1]
	    	    print "all_project_name",all_project_name
	    	    break             
           
    if not all_project_name:
    	return 
    else:
        return all_project_name


#add  read szbranch name config by renzhi.yang.hz 2020-1-6
def read_szbranch_list(branchname):
        szbranch_list = read_review_Config('SZbranch')
        szbranch = False
        if branchname in szbranch_list:
                szbranch = True
        return szbranch
#end  read szbranch name config by renzhi.yang.hz 2020-1-6
#add note email eduse files by xiaoying.huang 2018-11-19
def check_file_note_mail_list(gitname,commitlist):
        notefile_list = read_review_Config('notefileconfig')
        gask = False
        for file_name in commitlist :
                file_name = gitname+"/"+file_name[3:]
                if file_name in notefile_list:
                        gask = True
                        return gask
        return gask

# end note email eduse files by xiaoying.huang 2018-11-19
# add by xiaoying.huang 20190128 qcom check manifest modified
def check_qcom_manifest(gitname,commitlist):
        print "commitlist====",commitlist
        for file_name in commitlist :
                file_name = gitname+"/"+file_name[3:]
                #print "=file_name===",file_name
                if(file_name=='sdm450/device/qcom/msm8937_32/manifest.xml'or file_name=='sdm450/device/qcom/msm8937_64/manifest.xml'):
                        return "1";
        return "0"
# end by xiaoying.huang 20190128

# begin add by xiaoying.huang check rom file 20190412
def check_file_about_rom_submit(gitname,commitlist,branchname):
        commitlist2 = []
        if branchname in ROM_File.keys():
                arr_list = read_review_Config('rom_file/'+ROM_File[branchname])    
        else:
                return ""
        gitname = get_gitname()  
        for file_name in commitlist :
                file_name = gitname+"/"+file_name[3:]
                commitlist2.append(file_name)
        #print "arr_list",arr_list
        print "commitlist",commitlist2
        srom_str = ''
        gitarr = set(arr_list).intersection(set(commitlist2))
        for sa in gitarr:
                srom_str = '%s%s\n'%(srom_str,sa)
        return srom_str

#end by xiaoying.huang  check rom file 20190412
 

