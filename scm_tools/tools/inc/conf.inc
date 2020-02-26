<?php
$fota_sendmail_tolist=array('tlspm.swd2.hz@tcl.com');
$prj_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/config";
$prj_manaus_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/manausconfig";
$prj_file = "config";
$prj_manaus_file = "manausconfig";
$prj_manifest_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/projects/";
$almconfig_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/almconfig";
$almconfig_file = "almconfig";

#$togetherconfig_link = "/local/tools/scm_tools/conf/togetherconfig";
$togetherconfig_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/togetherconfig";
$togetherconfig_file = "togetherconfig";

$stdin=fopen('php://stdin','r');
$stdout = fopen('php://stdout','w');
$HOST_URL = "http://10.92.35.176/pms/";
#$HOST_URL = "http://10.92.33.43:8080/pms/";
$arr_root_cause = array('Unknown_Today', 'Architecture', 'Specification', 'Design', 'Coding','Regression','Evolution');
#$arr_bug_category = array('Platform', 'Android', '3rd Party', 'TCT');
$arr_bug_category = array('Platform', 'Android', 'GMS/3rd Party/MIBC', 'TCT');
#$arr_jrd_bug = array('Internal-SW Code', 'Internal-SW Translation Error', 'Internal-Customization mechanism', 'Internal-Parameters Integration', 'Internal-UE Implementation', 'Internal-Perso tool chain','Internal-FOTA Tools Server Chain','External-UE Design', 'External-UE Improvement','External-Translation feedback','External-Customization config','External-HW Related Implementation','External-New Requirement','External-Parameters','External-PC Tools','External-Others');
$arr_jrd_bug = array('-SW Code', '-SW Translation Error', '-Customization','-UE Implementation', '-UE Improvement','-New Requirement','-Google Certification','-Performance/Power Consumption','BSP-Parameters');
$arr_para_component =array('CTP','LCD','Main camera','Front camera','Audio','RF','NFC','BATTERY');

$fota_sendmail_tolist=array('jinguo.zheng@tcl.com','yangning.hong.hz@tcl.com','shie.zhao@tcl.com','xuanfeng.ye.hz@tcl.com','yanhong.liu@tcl.com','chuanbo.qiu.hz@tcl.com','xiangjm@tcl.com','haihui.jiang.hz@tcl.com','yange.zhang@tcl.com','chaofei.wu.hz@tcl.com','wenhui.xu.hz@tcl.com','haibo.zhong.hz@tcl.com');
//add by junbiao.chen 20150122
$framework_sendmail_tolist=array('can.song.hz@tcl.com','tlspm.swd2.hz@tcl.com');
//end by junbiao.chen 20141114
//add by zhaoshie 20150202 
$sdd1_framework_sendmail_tolist=array('jinhan.li@tcl.com','hanwu.xie@tcl.com','yunqing.huang@tcl.com','zhiquan.wen.hz@tcl.com','zhixiong.liu.hz@tcl.com','zongmin.lu@tcl.com');
//end
$arr_perso_team = array('junbo.zeng@tcl.com','liyun.liu@tcl.com','forong.li@tcl.com','xiaodan.cheng@tcl.com','shie.zhao@tcl.com','ting.liu.hz@tcl.com','ruping.pan.hz@tcl.com','yan.xiong@tcl.com','dingyuan.he@tcl.com','tianyu.wang@tcl.com');
//modify by ruifeng.dong for ALM 20141205 begin
#Bug category
$customization_sendmail_tolist=array('junbo.zeng@tcl.com','yunna.hua@tcl.com');
$customization_sendmail_cclist=array('ting.liu.hz@tcl.com','ruping.pan.hz@tcl.com','yan.xiong@tcl.com','dingyuan.he@tcl.com','tianyu.wang@tcl.com','shie.zhao@tcl.com','liyun.liu@tcl.com','forong.li@tcl.com','xiaodan.cheng@tcl.com','shuzhong.cui.hz@tcl.com','xiaobo.qiu@tcl.com');
//end by junbiao.chen 20141114
$arr_audio_sendmail_tolist=array('meng.zhang@tcl.com');
$arr_FRM1_reivewer=array('yadong.yang@tcl.com','gaoxiang.li@tcl.com');
$arr_defect_quality=array('Unclear_Description','Invalid_Test_Case','Invalid_Bug','No_Relationship','No_Comparison_Behavior','No_APK_Version','No_Log','Normal');

?>
