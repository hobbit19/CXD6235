#!/usr/bin/php -q 
<?php
require_once('inc/conf_odm.inc');
require_once('inc/common_sdd1.inc');
/*
 *  script name:patch_delivery_cli.php
 *  (Li Yuanhua, 2010-04-6)
 *
 *  The SCM Tool is for android team members to do patch delivery on their own machines.
 *  If there is no php5 on client machine, client should install
 *  it with command: sudo apt-get install php5-cli.
 *  usage:
 *  ./patch_delivery_cli.php -p <product> [-f] <files>
 *
 *  #To do a patch delivery commit to commit current whole working tree
 *  ./patch_delivery_cli.php -p <product>
 *
 *  #To do a patch delivery commit to commit one or several files in current working tree.
 *  ./patch_delivery_cli.php -p <product> -f test.h test.c
 */
 
//git commit -m -a;
global $arrmtk_id;  //add by shuangyan.he 20160819
$arr_code_branch = array();
$arr_alm_branch = array();
//begin add by zhaoshie for sdd1 prsm 20150119
$arr_prj_remote = array();
$arr_prj_config = array();
$product_sdd1_tag = 0 ;
$bug_reason_number = 0 ;
$str_bug_reason = '';
$product_config = "Bugzilla";
//end add by zhaoshie for sdd1 prsm 20150119
//begin add by junbiao.chen 20141031
$need_commit_contents = array();
$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));
//end add by junbiao.chen 20141031
//begin add by junbiao.chen 20141114
$need_commit_contents_all = array();
$project_spm = '' ;
//$fota_config_name="";

/*****
* main procedure
*****/
/*** added by Deng JianBo ***/
/***delete by yinfang.lai 2015-03-12  begin ***/
/*if (!is_git_root()){
    print "\033[31;1m Please go to git repository to finish your commit.\033[0m\n";
    exit;
}

if ($argc < 3 || $argv[1] != '-p'){
    print "usage:patch_delivery_cli.php -p <product> [-f <commit files> ]\n";
    exit;
}
if ($argc > 4 && $argv[3] != '-f' || $argc == 4){
    print "usage:patch_delivery_cli.php -p <product> [-f <commit files> ]\n";
    exit;
}*/
/***delete by yinfang.lai 2015-03-12 end ***/

/*** get commit files ***/
$strcommitfiles="";
if ($argc > 4){
    for($i=4; $i<$argc;$i++){
        $strcommitfiles = $strcommitfiles." ".$argv[$i];
    }
}

/*** get the projects ***/
$prjoects = get_projects($prj_link);
for($i=0; $i<count($prjoects); $i++){
        $number = $i + 1;
        print ("    $number-".$prjoects[$i]."\n");
}

/*** add by yinfang.lai 2015-03-10 auto get the projects ***/
$product_name = down_repo_branch();
writeline("\033[31;1mProduct name:".$product_name."\033[0m\n");
writeline("Enten Y|N:\n");
$judge_prject_name= read_line();
$judge_prject_name = strtolower(trim($judge_prject_name));
while( !(( $judge_prject_name =='yes' || $judge_prject_name =='y' || $judge_prject_name =='no' || $judge_prject_name =='n' )) ){
    writeline("Enten Y|N:\n");
    $judge_prject_name= read_line();
    $judge_prject_name = strtolower(trim($judge_prject_name));
   
}
$judge_prject_name = strtolower(trim($judge_prject_name));
if ( $judge_prject_name  == 'yes' || $judge_prject_name =='y' ){
    ;
}else{
    echo "projects is Woring\n";
    $product_name = '';
}
/*** add by yinfang.lai 2015-03-10 end ***/


/*** check for results ***/
if(sizeof($prjoects) == 0){
    echo "No projects found at $link\n";
    exit;
}

//judge if the project name input by users is right or not 

$is_prj_name = false;
$prj_number = 0;
foreach($prjoects as $key=>$value){
     $prj_number++;
     if ($product_name == $value){
        //print $prj_number."\n";
	print 'Project is '.$product_name."\n";
        $is_prj_name = true;
        //begin add by zhaoshie 20150127 for sdd1 prsm
        $product_remote = $arr_prj_remote[$prj_number - 1];
        $product_config = $arr_prj_config[$prj_number - 1];
        if (preg_match(" /sdd1\/manifest.git*/ ",$product_remote)){
             $product_sdd1_tag = 1 ;
         }elseif(preg_match(" /sdd1\/odm_manifest.git*/ ",$product_remote)){
             $product_sdd1_tag = 1 ;
         }else{
             $product_sdd1_tag = 0 ;
         }
        //end
     }
}

// step1: select project name 
$input = true;
if ($is_prj_name == false){
    writeline("Wrong product name,\n".
              "Below is the product list, please choose one:\n");
    for($i=0; $i<count($prjoects); $i++){
        $number = $i + 1;
        print ("    $number-".$prjoects[$i]."\n");
    }
    do{
        if($input == false){
            writeline("Wrong choise input.\n");
        }
        writeline("Your choise:"); $input = false;
        $prj_number = read_line();
        $input = false;
    }
    while(!is_numeric($prj_number)||$prj_number<1 || $prj_number >count($prjoects));
    $product_name = $prjoects[$prj_number - 1];   
    //begin add by zhaoshie 20150119 for sdd1 prsm
    $product_remote = $arr_prj_remote[$prj_number - 1];
    $product_config = $arr_prj_config[$prj_number - 1];
    if (preg_match(" /sdd1\/manifest.git*/ ",$product_remote)){
         $product_sdd1_tag = 1 ;
    }elseif(preg_match(" /sdd1\/odm_manifest.git*/ ",$product_remote)){
         $product_sdd1_tag = 1 ;
    }else{
         $product_sdd1_tag = 0 ;
    }
   //end add by zhaoshie 20150119 for sdd1 prsm
    
    
}
$almbranchs=array();
$almbranchs = get_almbranch($almconfig_link,$product_name);
if ($product_config == "ALM" && (count($almbranchs) >=2)){
	array_shift($almbranchs);
	array_shift($almbranchs); //get ALM branch
}

/***added by jianbo.deng 2013-01-09 ***/
$str_project_file = get_project_name($argv[0], $product_name);
$alps = exec("echo $str_project_file | grep -Ec 'beetlelite|pixo[^0-9]'");

$name_branch = get_project_name_branch($str_project_file);
$author_name = get_author_name();
$author_email = get_author_email();
$git_name = get_git_name();
$repo_branch = down_repo_branch();
$remote_branch = clone_git_remote_branch();
$clone_branch = clone_git_local_branch();
$git_path_name = " ";
$git_branch_name = " ";
foreach($name_branch as $key=>$value){
    $version_babyd = exec("echo $git_name | grep -c 'version_'");
    if($version_babyd == 1){
        $pattern_key = "/version_babyd$/"; #version_babyd git repository
    }
    else{
    	$pattern_key = "/".$git_name."$/";
    }
     
    //add by lyf 2015-11-25 for gerrit download 29418.?mtk6580.?device$/
    $pattern_key = str_replace("29418", "", $pattern_key);
    //add by lyf 2015-11-25 for gerrit download 29418.?mtk6580.?device$/
    if(preg_match($pattern_key,$key)){
	$git_path_name = trim($key);
	$git_branch_name = trim($value);      
    }
}

#print "$git_path_name ==== $git_branch_name\n";
if($git_path_name == " " || $git_branch_name == " "){
    print("\033[31;1m push error: there is no ".$git_name.".git, pls send email to INT team.\033[0m\n");
    exit;
}

//Judge the selected project and branch correct or not.
#print "===the selected branch is:$git_branch_name====\n";
#print "===the repo branch is:$repo_branch====\n";
#print "===the remote clone branch is:$remote_branch====\n";
#print "===the local clone branch is:$clone_branch====\n";
#print "****************the git name is:$git_name***********\n";
#print "****************the product name is:$product_name***********\n";
#repo download code but checkout remote branch or checkout local branch
if($repo_branch == $git_branch_name && $remote_branch  == NULL && $clone_branch == NULL){ 
   ;
}
###########clone code there three situation=====
#1,checkout local branch
#2,checkout remote branch
#3,first checkout remote branch then checkout local branch,it need to judge $clone_branch == NULL,otherwise maybe commit wrong branch.
else if($clone_branch == $git_branch_name || ($remote_branch == $git_branch_name && $clone_branch == NULL)){ 
   ;    
}
else{
	if(preg_match(" /.*modem.*/ ",$git_name) || $product_name == "tcl-apk" ){
		;	
	}
	else{
	print "\033[31;1m ========================================================== \033[0m\n";
   	print "\033[31;1m The push branch is wrong,please check or contact INT team. \033[0m\n";
   	print "\033[31;1m ========================================================== \033[0m\n";
   	exit;
	}
}

// step2: add modifed and deleted files 
/***
* add by Deng JianBo 2013-01-04
* add for commit and update renamed by Deng JianBo 2013-03-01
***/
$input = true;
$arr_add_file = get_add_array();
$add_file_index = "";

if(count($arr_add_file) > 0){
	do{
	    if($input == false){
		writeline("will commit file flow:,\n");		
		show_add_array($arr_add_file, true);
	    }
	    if($input == true){		
		writeline("\033[31;1mUpdate or deleted but not add file:\033[0m\n");
		show_add_array($arr_add_file, false);
	    }
	    writeline("git add update flow here:\n".
		      "    1) If add all, pls input '*'.\n".
		      "    2) If more one file add pls use ',' split.\n".
		      "    3) If end add file, pls input 'Q|q'.\n".
		      "You choice:");
	    $add_file_index = read_line();
	    
	    if($add_file_index == "Q" || $add_file_index == "q"){
		if(is_have_add_file($arr_add_file)){
		    $input = false;
		}else{
		    print "nothing commit for this git, please select agin.\n";
		}
	    }else if($add_file_index == "*"){
                
		foreach($arr_add_file as $key=>$value){
		    if($arr_add_file[$key] == "unselect_modified"){
			$arr_add_file[$key] = "select_modified";			
		    }
		    if($arr_add_file[$key] == "unselect_deleted"){
			$arr_add_file[$key] = "select_deleted";			
		    }
		    if($arr_add_file[$key] == "unselect_add"){
			$arr_add_file[$key] = "select_add";			
		    }
		    if($arr_add_file[$key] == "unselect_renamed"){
			$arr_add_file[$key] = "select_renamed";			
		    }
		    if($arr_add_file[$key] == "unselect_unmerged"){
			$arr_add_file[$key] = "select_unmerged";			
		    }
		}
		$input = false;
		#add for show already selected files by shuzhong.cui. 
		 //if($input == false){		
		 writeline("You already selected files as following:\n");
		 show_add_array($arr_add_file, true);
		 writeline("\033[32;1m+++++++++++++++++++++++++++++++++++\033[0m\n");
                 //}	                               
		#end show already selected files.
	    }else{
		$add_file_index = $add_file_index.",";                
		$keys = array();
		$keys = array_keys($arr_add_file);
		$arr_index = array();
		$arr_index = preg_split('/,/', $add_file_index);
		for($i=0;$i<count($arr_index)-1;$i++){
		    $j = $arr_index[$i] - 1;
		    #print "****$arr_index[$i]*****\n";
		    if($j < count($keys)){
			    if(@$arr_add_file[$keys[$j]] == "unselect_modified"){
				$arr_add_file[$keys[$j]] = "select_modified";                     
			    }
			    if(@$arr_add_file[$keys[$j]] == "unselect_deleted"){
				$arr_add_file[$keys[$j]] = "select_deleted"; 
			    }
			    if(@$arr_add_file[$keys[$j]] == "unselect_add"){
				$arr_add_file[$keys[$j]] = "select_add";                                
			    }
			    if(@$arr_add_file[$keys[$j]] == "unselect_renamed"){
				$arr_add_file[$keys[$j]] = "select_renamed";
			    }	
			    if(@$arr_add_file[$keys[$j]] == "unselect_unmerged"){
				$arr_add_file[$keys[$j]] = "select_unmerged";
			    }		           
		    
		    }else{
			print "\033[31;1mError: select index out array index,pls select again.\033[0m\n";
		    }
		    
		}
                $input = false;
		#add for show already selected files by shuzhong.cui.          
		//if($input == false){		
		 writeline("You already selected files as following:\n");
		 show_add_array($arr_add_file, true);
		 writeline("\033[32;1m------------------------------------\033[0m\n");
	        //}                         
		#end show already selected files.
		foreach($arr_add_file as $key=>$value){		     
		     if ($value == "unselect_modified" || $value == "unselect_deleted" || $value == "unselect_add" || $value == "unselect_renamed" || $value == "unselect_unmerged" ){
			$input = true;
		     }
		}
	    }

	}
	while($input);      
       $arr_unmerged_file = array(); //#add by zhaoshie 20160627 for forbid unmerged file upload
       //add by junbiao.chen 20141031
       global $need_commit_contents;
       global $need_commit_contents_all;
       foreach($arr_add_file as $key=>$value){
	if($value == "select_modified" || $value == "select_add" || $value == "select_renamed"){	
	$need_commit_contents[] = $key;                
        }else if ($value == "select_unmerged" ){  //#add by zhaoshie 20160627 for forbid unmerged file upload
         $arr_unmerged_file[] = $key;
	}}
        if (!empty($arr_unmerged_file)){	
           writeline("\n\033[31;1m**********************************************\033[0m\n");
           foreach($arr_unmerged_file as $each_file){
		writeline("\033[31;1m$each_file\033[0m\n");
           }
           writeline("\033[31;1m**********************************************\033[0m\n");
           writeline("\033[31;1m[Error!!] the selected files between '***' are unmerged files,which not allowed to upload. Pls fix them up in the work tree... .\033[0m\n");
           exit;
        }

       foreach($arr_add_file as $key=>$value){
	if($value == "select_modified" || $value == "select_add" || $value == "select_renamed" || $value == "select_deleted"){	
	$need_commit_contents_all[] = $key;        
	}}
       //end add by junbiao.chen 20141031
}else{
    print "error: nothing update for this git.\n";
    exit;
}



//add by junbiao.chen 20141031
//step2.5: check debuaggable info and test key info in apks
//begin add by yinfang.lai 2016-03-14
$commit_apks=get_apks_from_commit_contents();
if(!empty($commit_apks)){
	foreach($commit_apks as $each_commit_apk){
		$each_apk=trim($each_commit_apk);
	    	$cmd=$patch_delivery_path."aapt list -v -a $each_apk | grep 'debuggable' | grep '0xffffffff'";
	 	$debuggable_info = exec($cmd);
		if(!empty($debuggable_info)){
		   	writeline("\033[31;1m$each_apk has the debuggable info,pls modify it\033[0m\n");
		   	exit;   		
		}
}
//end add by yinfang.lai 2016-03-14
if(strpos($git_name,"wcustores")){
if(!empty($commit_apks)){
    	$flag=false;
	//begin delete by yinfang.lai 2016-03-14
	/*
    	foreach($commit_apks as $each_commit_apk){
        $each_apk=trim($each_commit_apk);
    	$cmd=$patch_delivery_path."aapt list -v -a $each_apk | grep 'debuggable' | grep '0xffffffff'";
 	$debuggable_info = exec($cmd);
	if(!empty($debuggable_info)){
	   	writeline("\033[31;1m$each_apk has the debuggable info,pls modify it\033[0m\n");
	   	$flag=true;   		
	}
	*/
	//end delete by yinfang.lai 2016-03-14
	$is_GMS_apk=exec("echo $each_apk | grep '/GMS/'");
        if(empty($is_GMS_apk)){
		$test_key_info=exec("jarsigner -verify -verbose -certs $each_apk | grep -c 'CN=Android, OU=Android, O=Android'");
		if($test_key_info > 0){
			writeline("\033[31;1m$each_apk has the test key,please remove this apk or sign it with the right key\033[0m\n");
			$flag=true;
		}
	}
    }
    if($flag){exit;}
 }
}
//end add by junbiao.chen 20141031
//add by shie.zhao 20160618
//step2.7: check fota
$isImpactFota=false;
if(is_dir("$patch_delivery_path"."/../conf/impact_fota_sdd1/")){
	$commit_fotas=get_fotas_from_commit_contents_all();
	if(!empty($commit_fotas)){
		$isImpactFota=true;
		$impactFotaFileAndDir="";
		writeline("\033[35;1m************************\033[0m\n");
		foreach($commit_fotas as $each_commit_fota){
			$each_fota=trim($each_commit_fota);
			$impactFotaFileAndDir="$impactFotaFileAndDir"."<br/>"."$each_commit_fota";
			writeline("\033[35;1m$each_fota\033[0m\n");		
		}
		writeline("\033[35;1m************************\033[0m\n");	
		writeline("\033[35;1m[Notice!!] the commited contents between '*' may impact fota\033[0m\n");
	}
}
//end by shie.zhao 20160618
//add by junbiao.chen 20150122
//step 2.8:check impact framework
$framework_config_name="$repo_branch"."_conf";
$isImpactFramework=false;
if(is_dir("$patch_delivery_path"."/../conf/impact_framework/"."$framework_config_name")){
	$commit_framework=get_framework_from_commit_contents_all();
	if(!empty($commit_framework)){
		$isImpactFramework=true;
		$impactFrameworkFileAndDir="";
		writeline("\033[35;1m#####################\033[0m\n");
		foreach($commit_framework as $each_commit_framework){
			$each_framework=trim($each_commit_framework);
			$impactFrameworkFileAndDir="$impactFrameworkFileAndDir"."<br/>"."$each_commit_framework";
			writeline("\033[35;1m$each_framework\033[0m\n");		
		}
		writeline("\033[35;1m######################\033[0m\n");	
                if($product_sdd1_tag == 1){ //add by zhaoshie 20150202
                    writeline("\033[35;1m[Notice!!] the commited contents between '#' may impact system(frameworks or mediatek or FOTA )\033[0m\n");
                }else{
		    writeline("\033[35;1m[Notice!!] the commited contents between '#' may impact framework\033[0m\n");
                }
	}
}
//end added by junbiao.chen 20150122
// step3: input bugnumber and comment


//start by zhaoshie 20160602
$commit_mtkpatch_forbid = get_mtkpatch_forbid_from_commit_contents_all();
if(!empty($commit_mtkpatch_forbid)){
	foreach($commit_mtkpatch_forbid as $each_commit_forbid){
		$each_forbid=trim($each_commit_forbid);
		if(!empty($each_forbid)){
		   	writeline("\033[31;1m[Error!!]$each_forbid is MTK default file,which not allowed to upload. Pls choice right JRD file .\033[0m\n");
		   	exit;   		
		}
        }
}
$commit_mtkpatch_remind = get_mtkpatch_remind_from_commit_contents_all();
if(!empty($commit_mtkpatch_remind)){
        $impactMtkpatchFileAndDir='';
        writeline("\033[35;1m#####################\033[0m\n");
	foreach($commit_mtkpatch_remind as $each_commit_remind){
		$each_remind=trim($each_commit_remind);
	        $impactMtkpatchFileAndDir="$impactMtkpatchFileAndDir"."<br/>"."$each_commit_remind";
		writeline("\033[35;1m$each_remind\033[0m\n");		
	}
	writeline("\033[35;1m######################\033[0m\n");	
        writeline("\033[35;1m[Notice!!] the commited contents between '#' is MTK Project Config file.You need merge it to JRD Project Config file \033[0m\n");
}
//end

// start by shuangyan.he 20160321
$txt_fill = '';
if(count($argv)>1){
	$txt_path = substr($patch_delivery_path,0,strlen($patch_delivery_path)-6);
	$txt_path = $txt_path."conf/FillManually.txt";
	$txtfile = file_get_contents("$txt_path");
	$txt_fill = $argv[1];
	if($txt_fill == "-t"){
		if(strpos($txtfile,"Bug Number:")===false||strpos($txtfile,"patch comments:")===false||strpos($txtfile,"root cause detail:")===false||strpos($txtfile,"Module_Impact:")===false||strpos($txtfile,"Test_Suggestion:")===false||strpos($txtfile,"Solution:")===false||strpos($txtfile,"Test_Report:")===false){
			echo "Your FillManually.txt is wrong !!\n";
			echo "Please ensure that your file is right !!\n";
			exit;
		}
	}
}

// 1.Get Bug Number from .txt
if($txt_fill == "-t"){
	$strbugnumber = readtxt("Bug Number:","patch comments:");
// 2.Get Bug Number from input
}else{
// end by shuangyan.he 20160321
	$strbugnumber='';
	$input = true;
	do{
	    if($input == false){
		writeline("wrong bug number,\n".
			  " 1) If there is no bugnumber,this tools didn't work.\n".
		          " 2) If there are several bugs,please separate them by \",\"\n");
	    }
	     writeline("\nInput Bug Number:");
	     $strbugnumber = read_line();
	     $input = false;
	}
	while(!is_correct_bugs_input($strbugnumber));
}

//modify by ruifeng.dong for ALM 20141205 begin
$defects = preg_split('/,/', $strbugnumber);
        #print_r($bugs);
$i = 0;
foreach($defects as $defect){
    #print "$patch_delivery_path"."ALM_check_swd1.py $product_name $defect";
    $error_flag = exec("$patch_delivery_path"."ALM_check_swd1.py $product_name $defect",$out);
    if (count($out) >=($i*5+5)){
	#ALM_check_swd1 script print function control $out value 
	#i is line,one print 4 lines
	writeline("\nDefect|Task Summary:".$out[$i*5+1]."\n");  //add anterior \n by shuangyan.he 20150321
	just_continue(" to go ");
	$filterbranch=array();
	$defectbranch = $out[$i*5+2];
        $project_spm = $out[$i*5+3];
        //add by lyf 2015-11-25 for no match alm branch/

	$filterbranch=preg_grep("/^all$/",$almbranchs);
        if ( !empty($filterbranch)){

	}
	else{
       //add by lyf 2015-11-25 for no match alm branch/
		$filterbranch=preg_grep("/^$defectbranch$/",$almbranchs);
		if( empty($filterbranch)) {
		    print "\033[31;1mbug's branch is error,Pls input $product_name's pr!\033[0m\n";
		    exit;
		}
	}
    }
    $i++;
    switch ($error_flag) {
        case 200:
	    print "OK! go next \n";
	    break;
	//add by yinfang.lai for ALM status check 2015-03-11 begin
        case 600:
	    print "State isn't Resolved or Opened ,please check state of task|defect!\n";
	    exit;
	    break;
	//add by yinfang.lai for ALM status check 2015-03-11 end 
        case 206:
	    print "This id doesn't have any related utc , please check bugid or contact with SPM! \n";
	    exit;
	    break;
        case 404:
	    print "This id deosn't exist in Integrity , please check bugid! \n";
	    exit;
	    break;
        case 500:
	    print "Error while requesting the Integrity Server , please contact with INT!\n";
	    exit;
	    break;
        case 505:
	    print "NO Platform Supported , please check again or contact with SPM! \n";
	    exit;
	    break;
    }
}
//modify by ruifeng.dong for ALM 20141205 end


//begain add by shuangyan.he 20160620
$mtkpatch_on = True;
#$mtkpatch_on = False;
$mtk_patch_number = "";
$vnum_choice="";
$id_choice="";
$patchtype="";
if($mtkpatch_on){
	echo "\nIs it mtk patch? (Y/N)  ";
	$mtk_patch_ornot = readline();
	$mtk_patch_ornot = strtolower(trim($mtk_patch_ornot));
	while(!(( $mtk_patch_ornot =='yes' || $mtk_patch_ornot =='y' || $mtk_patch_ornot =='no' || $mtk_patch_ornot =='n' ))){
		echo "Pls input Y/N !  ";
		$mtk_patch_ornot = readline();
		$mtk_patch_ornot = strtolower(trim($mtk_patch_ornot));
	}
	if($mtk_patch_ornot=='yes' || $mtk_patch_ornot =='y'){
		echo "\nPls input Pnum? (eg:p20) ";
		$mtk_patch_number = readline();
		$mtk_patch_number = trim($mtk_patch_number);
		$mtk_patch_number = strtolower($mtk_patch_number);
		while(!preg_match("/^p?\d+$/", $mtk_patch_number)){
			echo "\nwrong! Pls input Pnum? (eg:p20) ";
			$mtk_patch_number = readline();
			$mtk_patch_number = trim($mtk_patch_number);
			$mtk_patch_number = strtolower($mtk_patch_number);}
		if (is_numeric($mtk_patch_number)){
			$mtk_patch_number = "p"."$mtk_patch_number";}
		$mtkpatch_number=$mtk_patch_number;
		if (!empty($mtk_patch_number)){
			print "\033[32;1m    mtkpatch Pnum: $mtk_patch_number\033[0m\n";
			$patchtypelist=array('ALPS','MOLY');
			writeline("\nPlease choose the mtkpatch type:\n");
			for($i=0; $i<count($patchtypelist); $i++){
				$number = $i + 1;
				print ("    $number-".$patchtypelist[$i]."\n");
			}
			writeline("Your choise:");
			$patchtype = read_line();
			$patchtype =trim($patchtype);
			while(!is_numeric($patchtype)||$patchtype<1 || $patchtype >count($patchtypelist)){
				writeline("\nWrong input! Your choise:\n");
				$patchtype = read_line();
				$patchtype =trim($patchtype);}
			$patchtype = $patchtypelist[$patchtype-1];
			# begain add by shuangyan.he 20160715
			$dicidvnum=getidvnum_mtkmerge($mtk_patch_number,$repo_branch,$patchtype);
			while(empty($dicidvnum)){
				echo "\n\033[31;1mdo you input Pnum right?\033[0m Y=>go on and contact INT, N=>modify Pnum.\n";
				echo "Your choise Y/N: ";
				$vpnum = readline();
				$vpnum = strtolower(trim($vpnum));
				while(!(($vpnum=='yes'||$vpnum=='y'||$vpnum=='no'||$vpnum=='n'))){
					echo "Pls input Y/N: ";
					$vpnum = readline();
					$vpnum = strtolower(trim($vpnum));}
				if($vpnum=='no'||$vpnum=='n'){
					echo "input right Pnum: ";
					$vpnum = readline();
					$vpnum = strtolower(trim($vpnum));
					$dicidvnum=getidvnum_mtkmerge($vpnum,$repo_branch,$patchtype);}
				if($vpnum=='yes'||$vpnum=='y'){
					break;}
			}
			if(!empty($dicidvnum)){
				foreach($dicidvnum as $key=>$value){
					$vnum_choice=$key;
					$id_choice=$value;}
				print "\033[32;1m    mtkpatch Vnum: $vnum_choice\033[0m\n";
			}
			# end add by shuangyan.he 20160715
		}
	}
}
//end add by shuangyan.he 20160620


// start by shuangyan.he 20160323
// 1.Get patch comments from .txt
if($txt_fill == "-t"){
	$patch_comment = readtxt("patch comments:","root cause detail:");
// 2.Get patch comments from input
}else{
// end by shuangyan.he 20160322
	$patch_comment = '';
	do{
	    writeline("\npatch comments:");  //add anterior \n by shuangyan.he 20150320
	    $arrcomments = read_lines(".");	
	    foreach ($arrcomments as $comment){
		if($comment != ".")
		    $patch_comment .= $comment;
	    }
	    if ($patch_comment == '' ){
	    	writeline("patch comments is None, Please input again!\n");	
	    }
	}
	while($patch_comment == "");
}

$str_root_cause = "";
$str_root_cause_detail = "";
$input = true;
if (trim($strbugnumber) != ""){
    writeline("\nPlease choose the bug root cause:\n");  //add anterior \n by shuangyan.he 20150320
    for($i=0; $i<count($arr_root_cause); $i++){
        $number = $i + 1;
        print ("    $number-".$arr_root_cause[$i]."\n");
    }
    do{
        if($input == false){
            writeline("Wrong choise input.\n");
        }
    
        writeline("Your choise:");
    
        $cause_number = read_line();
        
        $input = false;
    }
    while(!is_numeric($cause_number)||$cause_number<1 || $cause_number >count($arr_root_cause));
    $str_root_cause = $arr_root_cause[$cause_number - 1];

	// start by shuangyan.he 20160322
	// 1.Get root cause detail from .txt
	if($txt_fill == "-t"){
		$str_root_cause_detail = readtxt("root cause detail:","Module_Impact:");
	// 2.Get root cause detail from input
	}else{
	// end by shuangyan.he 20160322
	    //begin add by lyf 2015-06-03
	do{
		writeline("\nroot cause detail:");  //add anterior \n by shuangyan.he 20150321
		$arrdetails = read_lines(".");	
		foreach ($arrdetails as $comment){
		    if($comment != ".")
			$str_root_cause_detail .= $comment;
		}
		if ($str_root_cause_detail == '' ){
			writeline("root cause detail is None, Please input again!\n");	
		}
	    }
	while($str_root_cause_detail == "");
	   //end add by lyf 2015-06-03  
	}
}

//step4: add other message

// start by shuangyan.he 20160323
// 1.Get Module_Impact from .txt
if($txt_fill == "-t"){
	$arr_module_impact = readtxt("Module_Impact:","Test_Suggestion:");
// 2.Get Module_Impact from input
}else{	
// end by shuangyan.he 20160323
	writeline("Module_Impact:");
	$arr_module_impact = read_lines(".");
}

//begin add by lyf 2015-06-03
/*
writeline("Test_Suggestion:");
$arr_test_suggestion = read_lines(".");
*/

//bgein add by lyf2015-8-13
writeline("\nChange Menutree/image resource? (Y/N) ");  //add anterior \n by shuangyan.he 20150321
$is_resource_change = read_line();
$is_resource_change = strtolower(trim($is_resource_change));
while( !(( $is_resource_change =='yes' || $is_resource_change =='y' || $is_resource_change =='no' || $is_resource_change =='n' )) ){
    writeline("Input Y/N to contiune!");
    $is_resource_change= read_line();
    $is_resource_change = strtolower(trim($is_resource_change));
   
}
//end by lyf 2015-8-13

// start by shuangyan.he 20160323
// 1.Get Test_Suggestion from .txt
if($txt_fill == "-t"){
	$str_test_suggestion = readtxt("Test_Suggestion:","Solution:");
// 2.Get Test_Suggestion from input
}else{	
// end by shuangyan.he 20160323
	$str_test_suggestion = "";
	do{
	    writeline("Test_Suggestion:");
	    $arrtestsuggestion = read_lines(".");	
	    foreach ($arrtestsuggestion as $comment){
		if($comment != ".")
		    $str_test_suggestion .= $comment;
	    }
	    if ($str_test_suggestion == '' ){
	    	writeline("Test_Suggestion is None, Please input again!\n");	
	    }
	}
	while($str_test_suggestion == "");
}
//end add by lyf 2015-06-03

// start by shuangyan.he 20160324
// 1.Get Solution/Test_Report from .txt
if($txt_fill == "-t"){
	$arr_solution = readtxt("Solution:","Test_Report:");
	$arr_test_report = readtxt("Test_Report:",'----------------------end----------------------');
// 2.Get Solution/Test_Report from input
}else{
// end by shuangyan.he 20160324
	writeline("Solution:");
	$arr_solution = read_lines(".");
	writeline("Test_Report:");
	$arr_test_report = read_lines(".");
}


//begin add by zhaoshie for sdd1 PRSM
if ($product_sdd1_tag == 1){
  /*$arr_bug_reason = "";
  $input = true;
  if (trim($strbugnumber) != ""){
    writeline("Please choose the bug Reason:\n");
    for($i=0; $i<count($arr_bug_reason_list); $i++){
        $number = $i ;
        print ("    $number-".$arr_bug_reason_list[$i]."\n");
    }
    do{
        if($input == false){
            writeline("Wrong choise input.\n");
        }
    
        writeline("Your choise:");
    
        $bug_reason_number = read_line();
        
        $input = false;
    }
    while(!is_numeric($bug_reason_number)||$bug_reason_number< 0 || $bug_reason_number >count($arr_bug_reason_list));
    $arr_bug_reason = $arr_bug_reason_list[$bug_reason_number];
    */
    
   //end add by zhaoshie for sdd1 PRSM
}else{
   writeline("Bug_Reason:");
   $arr_bug_reason = read_lines(".");
}



//modify by ruifeng.dong for ALM 20141205 begin
$str_bug_category = "";
$input = true;
if (trim($strbugnumber) != ""){
    writeline("\nPlease choose the Bug category:\n");
    for($i=0; $i<count($arr_bug_category); $i++){
        $number = $i + 1;
        print ("    $number-".$arr_bug_category[$i]."\n");
    }
    do{
        if($input == false){
            writeline("Wrong choise input.\n");
        }

        writeline("Your choise:");

        $cause_number = read_line();

        $input = false;
    }
    while(!is_numeric($cause_number)||$cause_number<1 || $cause_number >count($arr_bug_category));
    $str_bug_category = $arr_bug_category[$cause_number - 1];
    //bebin add by lyf 2015-4-03
    $str_bug_reason = $arr_bug_category[$cause_number - 1];
    //end add by lyf 2015-4-06
}

$str_generated_by = "";
if ($str_bug_category == "TCT"){
    $input = true;
	if (trim($strbugnumber) != ""){
     	    writeline("Please choose the root case category:\n");
    	    for($i=0; $i<count($arr_jrd_bug); $i++){
        	$number = $i + 1;
       		print ("    $number-".$arr_jrd_bug[$i]."\n");
    	    }
    	    do{
        	if($input == false){
    	            writeline("Wrong choise input.\n");
       	    	}

            	writeline("Your choise:");

       	    	$cause_number = read_line();

            	$input = false;
    	    }
    	    while(!is_numeric($cause_number)||$cause_number<1 || $cause_number >count($arr_jrd_bug));
            $str_generated_by = $arr_jrd_bug[$cause_number - 1];
	    //bebin add by lyf 2015-4-03
            $str_bug_reason = $arr_jrd_bug[$cause_number - 1];
	    //end add by lyf 2015-4-06
	}

} 
//modify by ruifeng.dong for ALM 20141205 end

//begain add by shuangyan.he 20160620
if (!empty($mtk_patch_number)){
	$mtk_patch = "[mtk patch]";
	$mtk_patch_number = "[$mtk_patch_number]";
	$comments = "###%%%comment:".$mtk_patch.$mtk_patch_number.$patch_comment."\n";
}else{$comments = "###%%%comment:".$patch_comment."\n";}
//end add by shuangyan.he 20160620

$comments .= "###%%%bug number:".$strbugnumber."\n";
$comments .= "###%%%product name:".$product_name."\n";

if ($str_root_cause !=""){
    $comments .= "###%%%root cause:".$str_root_cause."\n";
    //begin add by lyf 2015-06-03 
    $comments .= "###%%%root cause detail:".$str_root_cause_detail."\n";
   //end add by lyf 2015-06-03
}

//modify by ruifeng.dong for ALM 20141205 begin
if ($str_bug_category !=""){
    $comments .= "###%%%Bug category:".$str_bug_category."\n";
}

if ($str_generated_by !=""){
    $comments .= "###%%%Generated by:".$str_generated_by."\n";
}
//modify by ruifeng.dong for ALM 20141205 end



//added by yhli 2010-08-23
$comments .= "###%%%Module_Impact:";
foreach ($arr_module_impact as $comment){
    if($comment != ".")
        $comments .= $comment."\n";
}

//begin add by lyf 2015-06-03
$comments .= "###%%%Change Menutree or image:".$is_resource_change."\n";
$comments .= "###%%%Test_Suggestion:".$str_test_suggestion."\n";
/*$comments .= "###%%%Test_Suggestion:";
foreach ($arr_test_suggestion as $comment){
    //if($comment != ".")  //  cancel by lyf 2015-06-03
    $comments .= $comment."\n";
}*/
//end add by lyf 2015-06-03

//added by cxu 2011-03-01
$comments .= "###%%%Solution:";
foreach ($arr_solution as $comment){
    if($comment != ".")
        $comments .= $comment."\n";
}

$comments .= "###%%%Test_Report:";
foreach ($arr_test_report as $comment){
    if($comment != ".")
        $comments .= $comment."\n";
}

//added by xybian 2012-01-05
//begin modify by zhaoshie for sdd1 prsm 20150119
if ($product_sdd1_tag == 1){
  if ($str_bug_reason !=""){
    $comments .= "###%%%Bug_Reason:".$str_bug_reason."\n";
   }
}else{
  $comments .= "###%%%Bug_Reason:";
  foreach ($arr_bug_reason as $comment){
    if($comment != ".")
        $comments .= $comment."\n";
  }  
}
//end modify by zhaoshie for sdd prsm 20150119

$comments .= "###%%%author email:".$author_email."\n";

//add by junbiao.chen 20141115
if($isImpactFota){
   $impactFotaNotice="[Fota Warning] some changes may impact fota!!";
   $comments .= "###%%%".$impactFotaNotice."\n";
}
//add by junbiao.chen 20150122
if($isImpactFramework){
   $impactFrameworkNotice="[Framework Warning] some changes may impact framework!!";
   $comments .= "###%%%".$impactFrameworkNotice."\n";
}

//step5: push code to gerrit web added by Deng JianBo 2013-01-03
$git_push = "";
do{
    $flag=true;
    $result_git_path_name = get_push_branch($git_branch_name);
    $git_push = "git push ssh://".$author_name."@10.92.32.10:29418/".$git_path_name." HEAD:refs/for/".$result_git_path_name;
    print("push command: ".$git_push."\n");
    writeline("please check push command (yes/no):");
    $cause_number = read_line();
    if($cause_number == "yes"){
	$flag = false;
    }
    else if ($cause_number == "no"){
       writeline("please input push command:"); //add by zhaoshie 20160406
       $git_push = read_line();
       print("push command: ".$git_push."\n");      
       $flag = false;
       #exit;  //update by zhaoshie 20160406
    }else{
        $flag=true;
    }
}while($flag);

/***
* add by luo xiaoli 2014-01-19
* update for removing newline,solved the loss problem of information submitted by sdd1  luo xiaoli 2014-01-19
***/
 $comments = str_replace("\n", "", $comments);
 $comments = str_replace('"', "'", $comments);
 $array_comments = preg_split('/###%%%/',$comments);
 //system(print $comments | sed -n 's/\.###%%%//.\n###%%%/gp');
  global $temp;
  $temp=" ";

 foreach($array_comments as $item){
 if($item != "")
  $temp.="###%%%".$item."\n";
  $comments= trim($temp);     
            }
/**** end by luo xiaoli 2014-01-19***/



get_add_file_str($arr_add_file);

if ($argc > 4){
    system("git commit -m \"$comments\" $strcommitfiles");
}else{
    system("git commit -m \"$comments\"");
}

//git_pull();

fclose($stdin);
fclose($stdout);

system($git_push,$result);
print($result."\n");
if (($result=='1') || ($result==1) || ($result==128) || ($result=='128')) {
  print "\033[31;1m push error: please check push command \033[0m\n";
  exit;
}


//add by junbiao.chen 20141217
//setp 6:if there is any commit to impact fota,this step will sendEmail
if($isImpactFota){
	$currentCommitId=getCurrentCommitId();
	$changeId=getChangeId();
	$git_path_name_arr=explode("/",$git_path_name,2);
	$git_path_name_arr1=$git_path_name_arr[0];
	$git_path_name_arr2=$git_path_name_arr[1];
        $fota_subject="[Fota Warning][$repo_branch] some changes may impact fota";
    	$fota_body="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
    	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	</head>  
    	<body>  
    	<span style='color:red'><b>"."$author_name"."</b></span>
	<span> has modified as follow files:</span><br/>
	<span>"."$impactFotaFileAndDir"."</span><br/><br/>
	<b>Pls Notice these changes,they may impact fota</b><br/><br/>
	<span style='color:green'>Change Info on Gerrit:</span><br/>
	<a href='http://10.92.32.10:8081/#/c/$changeId/'>http://10.92.32.10:8081/#/c/$changeId/</a><br/><br/>	
    	</body>  
    	</html>";
        ###<span style='color:green'>Change Info on Git-Web:</span></br>
	##<a href='http://10.92.32.10/sdd1/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId'>http://10.92.32.10/sdd1/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId</a>
        $mail_count=count($fota_sendmail_tolist);
        $fota_sendmail_tolist[$mail_count]=$project_spm;
        #$fota_sendmail_tolist=array('shie.zhao@tcl.com');
	sendEmail($fota_sendmail_tolist,$fota_subject,$fota_body);
}
//end add by junbiao.chen 20141217
//add by junbiao.chen 20150122
if($isImpactFramework){
	$currentCommitId=getCurrentCommitId();
	$changeId=getChangeId();
	$git_path_name_arr=explode("/",$git_path_name,2);
	$git_path_name_arr1=$git_path_name_arr[0];
	$git_path_name_arr2=$git_path_name_arr[1];
        $framework_subject="[Framework Warning][$repo_branch] some changes may impact framework";       
        if($product_sdd1_tag == 1){ //add by zhaoshie 20150202
    	  $framework_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>
	  <span>"."$impactFrameworkFileAndDir"."</span><br/><br/>
	  <b>Pls Notice these changes,they may impact system(frameworks or mediatek or FOTA)</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://10.92.32.10:8081/#/c/$changeId/'>http://10.92.32.10:8081/#/c/$changeId/</a><br/><br/>
	  <span style='color:green'>Change Info on Git-Web:</span></br>
	  <a href='http://10.92.32.10/sdd1/gitweb-sdd1-all/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId'>http://10.92.32.10/sdd1/gitweb-sdd1-all/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId</a>
    	  </body>  
    	  </html>";
          sendEmail($sdd1_framework_sendmail_tolist,$framework_subject,$framework_body);
        }else{
    	  $frameowrk_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>
	  <span>"."$impactFrameworkFileAndDir"."</span><br/><br/>
	  <b>Pls Notice these changes,they may impact framework</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://10.92.32.10:8081/#/c/$changeId/'>http://10.92.32.10:8081/#/c/$changeId/</a><br/><br/>
	  <span style='color:green'>Change Info on Git-Web:</span></br>
	  <a href='http://10.92.32.10/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId'>http://10.92.32.10/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId</a>
    	  </body>  
    	  </html>";
          sendEmail($framework_sendmail_tolist,$framework_subject,$framework_body);
        }
	
}
//end added by junbiao.chen 20150122




//add by zhaoshie 20150510 for ImpactAPP
$index_app=0;
$commit_app=get_appupdate_from_commit_contents();
if(strpos($git_name,"wcustores") || strpos($git_name,"wprocedures")) {
  if(!empty($commit_app)){
    	$flag=true;
	$author_name=get_author_email();
        for($i=0; $i<count($arr_perso_team); $i++){
           if ( $author_name == $arr_perso_team[$i]){
	      $flag=false;
	   }
        }
        if($flag == true){
	  $currentCommitId=getCurrentCommitId();
	  $changeId=getChangeId();
	  #$git_path_name_arr=explode("/",$git_path_name,2);
	  #$git_path_name_arr1=$git_path_name_arr[0];
	  #$git_path_name_arr2=$git_path_name_arr[1];
          $app_subject="[App Warning][$repo_branch] some changes may impact App ";
    	  $app_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>";
          
          for($i=0;$i<$index_app;$i++){
             $app_commit="<span>"."$commit_app[$i]"."</span><br/><br/>";
             $app_body .= $app_commit;
          }
          $app_body .= "
	  <b>Pls Notice these changes,they may impact App</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://10.92.32.10:8081/#/c/$changeId/'>http://10.92.32.10:8081/#/c/$changeId/</a><br/><br/>
    	  </body>  
    	  </html>";

	  #<span style='color:green'>Change Info on Git-Web:</span></br>
	  #<a href='http://10.92.32.10/gitweb.cgi?p=$git_name.git;a=commit;h=$currentCommitId'>http://10.92.32.10/gitweb.cgi?p=$git_name.git;a=commit;h=$currentCommitId</a>
          #$arr_perso_team = array('shie.zhao@tcl.com');
	  sendEmail($arr_perso_team,$app_subject,$app_body);
          }
      }
}

//end 

//Begain add by shuangyan.he 20160621
$index_mtkpatch=0;
$commit_mtkpatch=get_mtkpatch_commit_gitpath();
if (!empty($mtk_patch_number)&&!empty($patchtype)&&!empty($vnum_choice)){
	//begain add by shuangyan.he 20160710
	$changeId=getChangeId();
	$gerrit_link="http://10.92.32.10:8081/#/c/$changeId/";
	$TS=date('Y-m-d H:i:s',time());
	$arrmtk_id=update_mtkpatch_mtkmerge($mtkpatch_number,$repo_branch,$patchtype,$vnum_choice,$strbugnumber,$gerrit_link,$author_email,$TS);
	//end add by shuangyan.he 20160710
}
if (!empty($mtk_patch_number)&&!empty($patchtype)){
	//$arr_mtkpatch_recipients = array("yan.xiong@tcl.com","renzhi.yang.hz@tcl.com","shie.zhao@tcl.com","shuangyan.he@tcl.com",$author_email);
	$arr_mtkpatch_recipients = array("835344992@qq.com",$author_email);
	$mtkpatch_subject="[MTK patch remind]"."$mtk_patch_number"."[$repo_branch] MTK patch delivered to Gerrit";
	$changeId=getChangeId();
	$arrbugnumber=explode(",",$strbugnumber);
	$arrbugnumber=array_filter($arrbugnumber);
	$mtkpatch_body ="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
	</head>  
	<body>  
	<span <b>"."$author_email"."</b></span><br/><br/>
	<span style='color:green'><b>Bug number:"."$strbugnumber"."</b></span><br/>";
	while(count($arrbugnumber)>=1){
		foreach($arrbugnumber as $strbugnum){
			$mtkpatch_body .="<a href='https://alm.tclcom.com:7003/im/issues?selection=$strbugnum'>https://alm.tclcom.com:7003/im/issues?selection=$strbugnum</a><br/><br/>";}
		break;
	}
	$mtkpatch_body .="<span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
	<a href='http://10.92.32.10:8081/#/c/$changeId/'>http://10.92.32.10:8081/#/c/$changeId/</a><br/><br/>
	<span style='color:green'><b>Mtk patch type:$patchtype</b></span><br/><br/>
	<span style='color:green'><b>Pls modify Merge Status on SmartTask:</b></span><br/>";
	while(!empty($arrmtk_id)){
		foreach($arrmtk_id as $strmtk_id){
			$mtkpatch_body .="<a href='http://10.92.35.20/SmartTask/index.php?m=mtkpatch&a=view&mtk_id=$strmtk_id/'>http://10.92.35.20/SmartTask/index.php?m=mtkpatch&a=view&mtk_id=$strmtk_id/</a><br/><br/>";}
		break;
	}
	while(!empty($arr_mmid)){
		foreach($arr_mmid as $strmmid){
			$mtkpatch_body .="<a href=http://10.92.35.20/SmartTask/index.php?m=mtkpatch&a=view&mtk_id=$strmmid/>http://10.92.35.20/SmartTask/index.php?m=mtkpatch&a=view&mtk_id=$strmmid/</a><br/><br/>";}
		break;
	}
	$mtkpatch_body .= "<span style='color:red'> <b>MTK patch delivered to Gerrit,Pls Notice:</b></span><br/>";
	for($i=0;$i<$index_mtkpatch;$i++){
		$mtkpatch_commit="<span>"."$commit_mtkpatch[$i]"."</span><br/><br/>";
		$mtkpatch_body .= $mtkpatch_commit;
	}      
	$mtkpatch_body .= "
	</body>  
	</html>";
	sendEmail($arr_mtkpatch_recipients,$mtkpatch_subject,$mtkpatch_body);
	print "\n----------MTKpatch sendEmail ok----------\n\n";

}
//end add by shuangyan.he 20160621

//begin add by zhaoshie 20150119 for sdd1 PRSM
//setp 7:if there is sdd1 pr,then update bug_reason to prsm 
if ($product_sdd1_tag == 1){
   $db_server = "10.92.35.20:3306";
   $db_user = "INT_PATCH";
   $db_pass = "Aa123456";
   $db_database = "dotproject";

   // modify by yinfang.lai 2015-4-03 being
   for($i=0; $i<count($arr_bug_reason_list); $i++){
       if ($arr_bug_reason_list[$i] == $str_bug_reason) {
		$bug_reason_number = $i;
	}
   }
   //print $bug_reason_number.'-----str--'.$str_bug_reason;
	
   if ($bug_reason_number != 0) {
           $phpmysql = exec(" dpkg -l | grep 'ii  php5-mysql' ");
           if(empty($phpmysql)){
               print "You need to install php5-mysql!! \n ";
               print "Beginint to run : sudo apt-get install php5-mysql \n";
               print "Please input your password: \n";
               exec("sudo apt-get install php5-mysql");
           }

           print "----------begin to update bug_reason to smarttask--------\n\n ";
	   $g_dbhandle = mysql_connect($db_server, $db_user, $db_pass);
	   mysql_select_db($db_database,$g_dbhandle);

	   //if ($bug_reason_number == 0 ){
	   //	$bug_reason_number = 14;
	   //} 
	   // modify by yinfang.lai 2015-03-13 being
	   $defects = preg_split('/,/', $strbugnumber);
	   $defectStr = '';
	   foreach($defects as $defectItem){
		if ($product_config == "ALM")
		    $defectStr .="'D".$defectItem."',";
		else
		    $defectStr .="'".$defectItem."',";
	   }
	   if (substr($defectStr,-1) == ',')
			$defectStr = substr($defectStr,0,strlen($defectStr)-1);
	   $query = "UPDATE dotp_bugs SET bug_reason=$bug_reason_number WHERE bug_id in ($defectStr) ";
	   #print $query."\n";
	   /*
	   if ($product_config == "ALM"){ 
		$prsmbugnumber = "D".$strbugnumber ; 
		$query = "UPDATE dotp_bugs SET bug_reason=$bug_reason_number WHERE bug_id in ('$prsmbugnumber') ";
	   }else{
		$prsmbugnumber = $strbugnumber;
		$query = "UPDATE dotp_bugs SET bug_reason=$bug_reason_number WHERE bug_id in ($prsmbugnumber) ";
	   }*/
	   // modify by yinfang.lai 2015-03-13 end


	   $result = mysql_query($query);
	    // Check result
	    // This shows the actual query sent to MySQL, and the error. Useful for debugging.
	    if (!$result) {
		$message  = 'Invalid query: ' . mysql_error() . "\n";
		$message .= 'Whole query: ' . $query;
		die($message);
	    }
           print "----------UPDATE bug_reason to smarttask end--------\n ";
	    // Free the resources associated with the result set
	    // This is done automatically at the end of the script
	    #mysql_free_result($result);
    }
   // modify by yinfang.lai 2015-4-03 end
   
}

//setp 8 input any key to exit
//$input = true;
//do{
//    writeline("\033[32;1mPress Enter key to exit!\033[0m\n");
//    $strtemp = read_line();
//    $input = false;
//}
//while($input);

//fclose($stdin);
//fclose($stdout);

//exit;

//end

?>
