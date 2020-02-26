#!/usr/bin/php -q 
<?php
require_once('inc/conf.inc');
require_once('inc/common.inc');

exec('dpkg -l php*-curl', $output, $return_status);
if ($return_status != 0){
    echo "You should install php5-curl before!! Please run:sudo apt-get purge -y php5-common && sudo apt-get install -y php5-curl php5-cli\n";
    exit;
}

exec('dpkg -l php*-mysql', $output, $return_status);
if ($return_status != 0){
    echo "You should install php5-mysql before!! Please run:sudo apt-get install -y php5-mysql\n";
    exit;
}

$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));
/*** check current dir is git root or not  and check args***/

if (!is_git_root()){
    print "\033[31;1m Please go to git repository to finish your commit.\033[0m\n";
    exit;
}

/*
if ($argc < 3 || $argv[1] != '-p'){
    print "usage:patch_delivery_cli.php -p <product> [-f <commit files> ]\n";
    exit;
}
if ($argc > 4 && $argv[3] != '-f' || $argc == 4){
    print "usage:patch_delivery_cli.php -p <product> [-f <commit files> ]\n";
    exit;
}
*/
$product_name = '';
#start add by renzhiyang 20170816
$gitname_for_mtkpatch='';
$gitname_for_mtkpatch=getgitname_for_mtkpatch();
#end add by renzhiyang 20170816

/*** get the projects ***/
$arr_prj_remote = array();
$arr_prj_config = array();
/*** for manaus team 20170711***/
$git_name = get_git_name();
$manaus_git = exec("echo $git_name | grep -c 'manaus'");
$ismanaus=false;
$isputchpk='N';
$autobuildflag=false;
$istopic='N';
if($manaus_git == 1){
   $prj_link_file=$patch_delivery_path."../conf/".$prj_manaus_file;
   if(!file_exists("$prj_link_file")){
      $prj_link_file = $prj_manaus_link;
   }
   #$ismanaus=true;
}else{
   $prj_link_file=$patch_delivery_path."../conf/".$prj_file;
   if(!file_exists("$prj_link_file")){
      $prj_link_file=$prj_link;
   }
}

$prjoects = get_projects($prj_link_file);//this function will also get $arr_prj_remote and $arr_prj_config
/*** check for results ***/
if(sizeof($prjoects) == 0){
       echo "No projects found at $prj_link_file\n";
       exit;
}

/*** judge if the project name input by users is right or not ***/
$is_prj_name = false;
$prj_number = 0;
foreach($prjoects as $key=>$value){
     $prj_number++;
     if ($product_name == $value){
        $is_prj_name = true;
        $product_remote = $arr_prj_remote[$prj_number - 1];
        $product_config = $arr_prj_config[$prj_number - 1];
     }
}

// step1: auto select project name
//$repo_branch = down_repo_branch();//rm by renzhi.yang.hz 2018-11-29
$auto_project_branch_select = false;
if ($is_prj_name == false){
    $product_name = get_project_branch();
    if (!empty($product_name)){
       writeline("\033[32;1mDo you want to commit the branch: \033[0m\033[31;1m".$product_name."\033[0m\n");
       writeline("Enter Y|N:");  
       $judge_prject_name= read_line();
       $judge_prject_name = strtolower(trim($judge_prject_name));
    }else{ //add for git clone can not auto get project name 20170711
       $judge_prject_name='no';
    }


    while( !(( $judge_prject_name =='yes' || $judge_prject_name =='y' || $judge_prject_name =='no' || $judge_prject_name =='n' )) ){
        writeline("Enter Y|N:");
        $judge_prject_name= read_line();
        $judge_prject_name = strtolower(trim($judge_prject_name));
    }
    $judge_prject_name = strtolower(trim($judge_prject_name));
    if ( $judge_prject_name  == 'yes' || $judge_prject_name =='y' ){
        $auto_project_branch_select = true;
	$prj_number = 0;
	foreach($prjoects as $key=>$value){
	    $prj_number++;
            if ($product_name == $value){
                $is_prj_name = true;
                $product_remote = $arr_prj_remote[$prj_number - 1];
                $product_config = $arr_prj_config[$prj_number - 1];
            }
 	}
    }else{
        $product_name = '';
    }
}


// step1: select project name
if ($is_prj_name == false && $auto_project_branch_select == false){
    //add for git clone branch check 20170711
    $gitbranch= exec("git branch | grep '* '|sed -e 's/* //'");
    //end
    writeline("Current git branch is \033[32;1m $gitbranch \033[0m\n");       
    writeline("Below is the product list, please chose one:\n");
    /*******xiaoying.huang***20180921**************/
    //$prj_number = select_option($prjoects);
    $prj_number = select_option_NorT($prjoects);
    /*******xiaoying.huang***20180921**************/
    $product_name = $prjoects[$prj_number - 1];   
    $product_remote = $arr_prj_remote[$prj_number - 1];
    $product_config = $arr_prj_config[$prj_number - 1];
}

#start add by renzhi.yang.hz 2018-11-23
$Repo_path='';
$Repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
$arr_moregits_name = array();
print "current code repo path =========$Repo_path==========\n";
$arr_moregits_branches=get_confreview($patch_delivery_path,"moregitsconfig");
#start add by xiaoying.huang 2018-12-20
$ifcheck_moregits=false;
if (in_array($product_name,$arr_moregits_branches) &&( $Repo_path!="/")){
	writeline("\033[32;1mDo you want to get all modified gits from this project,It may need few minute\033[0m\n");
    writeline("Enter Y|N:");
    $judge_moregits= read_line();
    if ( $judge_moregits == 'Y' || $judge_moregits =='y' )
        $ifcheck_moregits=true;
}
if ($ifcheck_moregits){
#end add by xiaoying.huang 2018-12-20
	$k=0;
	print "$product_name need check moregits,please waitting.....\n";
	exec("cd $Repo_path");
	$which_repo=exec("which repo",$find_repo_result);
	$repo_status_result=exec("$which_repo status | grep 'project'",$repo_status_out);
	foreach($repo_status_out as $eachlocalgit){
		$keywordstmp=preg_split("/\s*\([\*]+/",$eachlocalgit);
		$keywordstmp1 = preg_split("/[\s]+/",$keywordstmp[0]);
		$keywords=substr($keywordstmp1[1],0,-1);
		$arr_moregits_name[$k]=$keywords;
		$k = $k+1;
	}
}
else{
$git_name_tmp = get_git_name();
$arr_moregits_name[0]=$git_name_tmp;
}
#end add by renzhi.yang.hz 2018-11-23

if (empty($product_config) ){
    echo "\033[31;1mCan't get project info in config file! Please contact to VER!\033[0m\n ";
    exit;
}else if($product_config == "ALMNO"){
	echo "\033[31;1mThis branch pause submission of code,please wait and try again!\033[0m\n ";
    exit;
}

$xr_tool_value = 0;//1:Bugzilla,2:ALM
if(strtoupper($product_config) == 'BUGZILLA'){
    $xr_tool_value = 1;
}else if(strtoupper($product_config) == 'ALM'){
    $xr_tool_value = 2;
}

$almbranchs=array();
$almconfig_link_file=$patch_delivery_path."../conf/".$almconfig_file;
if(!file_exists("$almconfig_link_file")){
      $almconfig_link_file=$almconfig_link;
}
$almbranchs = get_almbranch($almconfig_link_file,$product_name);
if ($product_config == "ALM" && (count($almbranchs) >=2)){
	array_shift($almbranchs);
	array_shift($almbranchs); //get ALM branch
}

//start add by renzhi.yang.hz 20181128
$gits_addfile_array=array();
$git_path_name_array=array();
$git_branch_name_array=array();
$git_local_path_array=array();
$repo_branch_array=array();
//end add by renzhi.yang.hz 20181128

//begin add by renzhi.yang.hz 20191008
$arr_xmls_needed_check=get_confreview($patch_delivery_path,"the_projects_with_samebranchgits");
$xmls_needed_check_count=count($arr_xmls_needed_check);
$xmlname_branch=array();
if($xmls_needed_check_count>0){
 for($i=0;$i<$xmls_needed_check_count;$i++){
    $each_xmlname=$arr_xmls_needed_check[$i];
    $str_xml_file = get_project_name($argv[0], $each_xmlname);
    $xmlname_branch[$each_xmlname] = get_project_name_branch($str_xml_file);
 }
}
//end add by renzhi.yang.hz 20191008


foreach($arr_moregits_name as $eachgitname){//this  line and { add by renzhi.yang.hz 20181128
print "current git name ,$eachgitname\n";
$current_git_dir = $Repo_path.'/'.$eachgitname;//add by renzhi.yang.hz 20181128
$git_local_path_array[$eachgitname]=$current_git_dir;
writeline("current git path is \033[31;1m $current_git_dir\033[0m\n");
if(file_exists($current_git_dir)){
chdir($current_git_dir);//add by renzhi.yang.hz 20181128
}
$str_project_file = get_project_name($argv[0], $product_name);
$name_branch = get_project_name_branch($str_project_file);
$author_name = get_author_name();
$author_email = get_author_email();
$git_name = get_git_name();
$git_name2 = get_git_name2();
$repo_branch = down_repo_branch();//modified by renzhi.yang.hz 2018-11-29
$repo_branch_array[$eachgitname]=$repo_branch;//add by renzhi.yang.hz 2018-11-29
$remote_branch = clone_git_remote_branch();
$clone_branch = clone_git_local_branch();
$git_path_name = " ";
$git_branch_name = " ";
$server="10.92.32.10";
$author_pass = ""; //add by xiaoying.huang 2017-11-8
$send_pass = false; 
$addfile_flag = false; //add by xiaoying.huang 20190523
$keymaster_remind=false;//add by renzhi.yang.hz 2019-09-24
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
    }else if (!empty($git_name2) && preg_match("/".$git_name2."$/", $key)){
        $git_path_name = trim($key);
        $git_branch_name = trim($value);
    }
}

if($git_path_name == " " || $git_branch_name == " "){
    print("\033[31;1m push error: there is no ".$git_name.".git, pls send email to INT team.\033[0m\n");
    exit;
}
//Judge the selected project and branch correct or not.
print "======$repo_branch====$git_branch_name=====\n";
print "clone_branch,remote_branch,$clone_branch,$remote_branch";
if($repo_branch == $git_branch_name && $remote_branch  == NULL && $clone_branch == NULL){ 
   ;
}
###########clone code there three situation=====
else if($clone_branch == $git_branch_name || ($remote_branch == $git_branch_name && $clone_branch == NULL)){ 
   ;    
}
else{
	if(preg_match(" /.*modem.*/ ",$git_name) || preg_match(" /.*JrdMusic.*/ ",$git_name) || $product_name == "tcl-apk" || preg_match("/.*InCallUI.*/",$git_name ) || preg_match("/.*Mms.*/",$git_name ) || preg_match(" /.*hz_wlanguage.*/ ",$git_name)){
		;	
	}
	else{
	print "\033[31;1m ========================================================== \033[0m\n";
   	print "\033[31;1m The push branch is wrong,please check or contact INT team. \033[0m\n";
   	print "\033[31;1m ========================================================== \033[0m\n";
   	exit;
	}
}

//start add by renzhi.yang.hz 20191008
$xml_array=array();
foreach($xmlname_branch as $key=>$value){
 foreach($value as $key1=>$value1){  
  $key1 = exec("echo $key1 | sed -e  's/\//.?/g'");
  $key1 = '.?'.''.$key1;
  if ($key1 == $git_name && $value1 == $git_branch_name && $product_name != $key){
  $xml_array[]=$key;
  continue;  
  }}
}
//end add by renzhi.yang.hz 20191008*/

// step2: add modifed and deleted files 
$input = true;
$arr_add_file = get_add_array();
$add_file_index = "";
$need_commit_contents = array();
$need_commit_contents_all = array();

$patch_git=false;
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
		  "    2) If add continuous files, pls use '-' to connect.\n".
		  "    3) If add more one file, pls use ',' to split.\n".
		  "    4) If end add file, pls input 'Q|q'.\n".
		  "    5) If do not patch delivery this git repository, pls input 'N|n'.\n".
		  "You choice:");
	$add_file_index = read_line();
	if ($add_file_index == "N" || $add_file_index == "n"){
		writeline("\033[31;1mDo not patch delivery for $eachgitname\033[0m\n");
		$patch_git = true;
		$input = false;
	}
	else if($add_file_index == "Q" || $add_file_index == "q"){
	    if(is_have_add_file($arr_add_file)){
		$input = false;
	    }else{
		print "nothing commit for this git, please select again.\n";
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
	    writeline("You already selected files as following:\n");
	    show_add_array($arr_add_file, true);
	    writeline("\033[32;1m+++++++++++++++++++++++++++++++++++\033[0m\n");
	}else{
	    $add_file_index = $add_file_index.",";
	    $keys = array_keys($arr_add_file);
	    $final_arr_index = array();
	    $j = 0;
	    $arr_index = preg_split('/,/', $add_file_index);
	    foreach($arr_index as $each_index){
		if(strpos($each_index,"-")){
		    $se_arr_index = preg_split('/-/', $each_index);
		    $start_index = $se_arr_index[0];
		    $end_index = $se_arr_index[1];
		    if($start_index <= $end_index){
			for($i=$start_index;$i<=$end_index;$i++,$j++){
			    $final_arr_index[$j] = $i;
			} 			
		    }else{
			print "\033[31;1mError: ".$start_index." is greater than ".$end_index.",pls select again.\033[0m\n";
		    }
		}else{
		    $final_arr_index[$j] = $each_index;
		    $j++;
		}
	    }
	    for($i=0;$i<count($final_arr_index)-1;$i++){
		$j = $final_arr_index[$i] - 1;
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
	    writeline("You already selected files as following:\n");
	    show_add_array($arr_add_file, true);
	    writeline("\033[32;1m------------------------------------\033[0m\n");
	    foreach($arr_add_file as $key=>$value){		     
 	        if ($value == "unselect_modified" || $value == "unselect_deleted" || $value == "unselect_add" || $value == "unselect_renamed" || $value == "unselect_unmerged"){
		    $input = true;
	        }
	    }
        }
    }while($input);
	if($patch_git == true){
		continue;
	}	
    global $need_commit_contents;
    global $need_commit_contents_all;
    // add by xiaoying.huang 20190307 no allow to push apecial symbol
        print "git name===$git_path_name====$git_name======";
    if ($git_path_name == 'sdm450/vendor/jrdcom')
        foreach($arr_add_file as $key=>$value){
        if($value == "select_modified" || $value == "select_add" || $value == "select_renamed"){
            if(preg_match("/[\',;:*?~!`@#$%^&=<>(){}]/", $key)){ 
               writeline("\n\033[31;1mYou cannot submit files containing special symbols!\033[0m\n");
               print "file:  $key \n";
               exit;
            }
         }           
	}
	// end by xiaoying.huang 20190307 no allow to push apecial symbol
    foreach($arr_add_file as $key=>$value){
        if($value == "select_modified" || $value == "select_add" || $value == "select_renamed"){
            $key=str_replace("(", "\\(", $key); // add by hsy 20160908
	    $key=str_replace(")", "\\)", $key); // add by hsy 20160908	
	    $key=str_replace("$", "\\$", $key); // add by renzhi.yang.hz 20181119
	    $key=str_replace("&", "\\&", $key); // add by xiaoying.huang 20191231
	    $need_commit_contents[] = $key;        
	}
	else if ($value == "select_unmerged" ){  //#add by zhaoshie 20160627 for forbid unmerged file upload
         $arr_unmerged_file[] = $key;
	}
    }
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
	    $key=str_replace("(", "\\(", $key); // add by hsy 20160908
	    $key=str_replace(")", "\\)", $key); // add by hsy 20160908
	    $key=str_replace("$", "\\$", $key); // add by renzhi.yang.hz 20181119
	    $key=str_replace("&", "\\&", $key); // add by xiaoying.huang 20191231
	    $need_commit_contents_all[] = $key;        
	}
	if ($value == "select_add" ){$addfile_flag =true;}
    }
    foreach($arr_add_file as $key=>$value){
	if($value == "select_modified"){
	    $key=str_replace("(", "\\(", $key); // add by hsy 20160908
	    $key=str_replace(")", "\\)", $key); // add by hsy 20160908	
	    $key=str_replace("$", "\\$", $key); // add by renzhi.yang.hz 20181119
	    $key=str_replace("&", "\\&", $key); // add by xiaoying.huang 20191231	
	    $need_commit_modified_contents[] = $key;        
	}
    }
}else{
    print "error: nothing update for this git.\n";
    exit;
}

//add by junbiao.chen 20141031
//step2.5: check debuaggable info and test key info in apks
//begin add by yinfang.lai 2016-03-14

/*
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
	print "Check apk debug info finish!\n";		
}
*/

//end add by yinfang.lai 2016-03-14

/***add by xiaoying.huang 20190124 qcom check manifest modified  ****/
$send_qcom_flag = false;
if($product_name == 'sdm429-p-v2.0-dint'){
    if(check_qcom_manifest($git_path_name,$arr_add_file)=='1'){
		$send_qcom_flag = true;
    }
}

/***end by xiaoying.huang 20190124 qcom check manifest modified  ****/

$commit_apks=get_apks_from_commit_contents();

if(strpos($git_name,"wcustores") || strpos($git_name,"wprocedures")  )
{
   if(!empty($commit_apks)){
    	$flag=false;
    	foreach($commit_apks as $each_commit_apk){
		$each_apk=trim($each_commit_apk);
		exec("echo $each_apk | grep '/google/\|/GMS/'",$outapk,$status);
		if(!$status){
			$test_key_info=exec("jarsigner -verify -verbose -certs $each_apk | grep -c 'CN=Android, OU=Android, O=Android'");
			if($test_key_info > 0){
				writeline("\033[31;1m$each_apk has the test key,please remove this apk or sign it with the right key\033[0m\n");
				$flag=true;
			}
		}else{
	    	        $cmd=$patch_delivery_path."aapt list -v -a $each_apk | grep 'debuggable' | grep '0xffffffff'";
	 	        $debuggable_info = exec($cmd);
			if(!empty($debuggable_info)){
			   	writeline("\033[31;1m$each_apk has the debuggable info,pls modify it\033[0m\n");
			   	$flag=true;   		
			}
	        }
    	}
    	if($flag){exit;}
   }
 
}

//end add by junbiao.chen 20141031
//add by shie.zhao 20160618
//step2.6: check impact customization
$isImpactCustomization=false;
if(strpos($git_name,"wcustores")){
    //modify by lyf 2017-2-14
    //$customization_config_name="$repo_branch"."_conf";
    //if(is_dir("$patch_delivery_path"."/../conf/impact_customization/"."$customization_config_name")){
    //$commit_customizations=get_customizations_from_commit_contents_all();
    //check rest key

    foreach($commit_apks as $each_commit_apk){
    	$each_apk=trim($each_commit_apk);
    	exec("echo $each_apk | grep '/IME/\|/GMS/'" ,$outapk,$status);
    	if($status){
    		get_gapp_name_rule_check($each_apk);
    	}
    }
    if(is_dir("$patch_delivery_path"."/../conf/impact_customization")){
	$commit_customizations=get_customizations_from_commit_contents_all_new();	
    //end modify by lyf 2017-2-14	
	if(!empty($commit_customizations)){
	    $isImpactCustomization=true;
	}
    }   
}

//step2.6.2: check plf file contect 20170502 shie
//print $TS=date('Y-m-d H:i:s',time());
check_plf_contect($git_name);
//print $TS=date('Y-m-d H:i:s',time());
//end


//step2.6.2: check impact project config dir or files
if(strpos($git_name,"wcustores") || strpos($git_name,"device") || strpos($git_name,"kernel") || strpos($git_name,"wprocedures") || strpos($git_name,"JrdMMITest") || strpos($git_name,"vendor") ){
	get_projectname_config_from_commit_contents_all_new();	    	
}

//step2.7: check fota
$fota_config_name="$repo_branch"."_conf";
$isImpactFota=false;
if(is_dir("$patch_delivery_path"."/../conf/impact_fota/"."$fota_config_name")){
    $commit_fotas=get_fotas_from_commit_contents_all();
    if(!empty($commit_fotas)){
	$isImpactFota=true;
	$impactFotaFileAndDir="";
	foreach($commit_fotas as $each_commit_fota){
            $each_fota=trim($each_commit_fota);
            $impactFotaFileAndDir="$impactFotaFileAndDir"."<br/>"."$each_commit_fota";
        }
    }       
}
//add by shie.zhao 20160618
else if(is_dir("$patch_delivery_path"."/../conf/impact_fota_sdd1/")){
	$commit_fotas=get_fotas_from_commit_contents_all_sdd1();
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
//step 2.9:check mtk patch
//start by zhaoshie 20160602
$commit_mtkpatch_forbid = get_mtkpatch_forbid_from_commit_contents_all();
//$commit_mtkpatch_forbid = '' ;
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

//start add xiaoying.huang 20180315 
if(strpos($git_name,"wprocedures"))
{
   check_custpack_app_odex_list($arr_add_file);
}
//end by xiaoying.huang 20180315
$gits_addfile_array[$eachgitname]=$arr_add_file;//add by renzhi.yang.hz 20181128


//add by yinfang.lai 2017-03-15
$together_projects = array();
$together_gits = array();
$togetherconfig_link_file=$patch_delivery_path."../conf/".$togetherconfig_file;
if(!file_exists("$togetherconfig_link_file")){
      $togetherconfig_link_file=$togetherconfig_link;
}
together_git_remind_check($togetherconfig_link_file,$product_name,$git_name);
//add by yinfang.lai 2017-03-15

$git_path_name_array[$eachgitname]=$git_path_name;
$git_branch_name_array[$eachgitname]=$git_branch_name;
}//add } by renzhi.yang.hz 20181128
if (empty($gits_addfile_array)){
writeline("\033[31;1mYou have choiced no file to patch delivery! please check!\033[0m\n");
exit;
}

// start by shuangyan.he 20160321
$txt_fill = '';
if(count($argv)>1){
	$txt_path = substr($patch_delivery_path,0,strlen($patch_delivery_path)-6);
	$txt_path = $txt_path."conf/FillManually.txt";
	$txtfile = file_get_contents("$txt_path");
	$txt_fill = $argv[1];
	if($txt_fill == "-t"){
		if(strpos($txtfile,"Bug Number:")===false||strpos($txtfile,"patch comments:")===false||strpos($txtfile,"root cause detail:")===false||strpos($txtfile,"Module_Impact:")===false||strpos($txtfile,"Test_Suggestion:")===false){ #||strpos($txtfile,"Solution:")===false||strpos($txtfile,"Test_Report:")===false
			echo "Your FillManually.txt is wrong !!\n";
			echo "Please ensure that your file is right !!\n";
			exit;
		}
	}
}

//add by renzhi.yang.hz 20191017
$projects_with_onemanifestfile=get_peojects_with_onemanifestfile($product_name);
print "projects_with_onemanifestfile    $projects_with_onemanifestfile\n";
if($projects_with_onemanifestfile){
$all_projects=array();
$projects_with_onemanifestfile_list=preg_split("/@/",$projects_with_onemanifestfile);
for($k=0;$k<count($projects_with_onemanifestfile_list);$k++){
print "k $k projects_with_onemanifestfile_list $projects_with_onemanifestfile_list[$k]\n";
$all_projects[$k]=preg_split("/:/",$projects_with_onemanifestfile_list[$k]);
}}
var_dump($all_projects);
//end add by renzhi.yang.hz 20191017


//add by renzhi.yang.hz 20191009
if(count($xml_array)>0){
$branch_string="";
for($k=0;$k<count($xml_array);$k++){
$branch_string=$branch_string." ".$xml_array[$k].'.xml';
}
$current_gitname=exec("echo $git_name | sed -e  's/.?/\//g'");
writeline("\n\033[31;1mPay attention: \033[32;1mThese project's manifest:\033[0m\033[0m\033[31;1m$branch_string\033[32;1m also have this git->$current_gitname with the same branch->$git_branch_name\n\033[31;1mPlease confirm this patch whether effects these projects.\n Press anykey to continue: \033[0m");
  $isputxmlcheck= read_line();
  $isputxmlcheck = strtoupper(trim($isputxmlcheck));
}
//end add by renzhi.yang.hz 20191009

// step3: input bugnumber and comment
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
	     $strbugnumber = trim($strbugnumber);
	     $input = false;
	}
	while(!is_correct_bugs_input($strbugnumber));
}

/*** check bugNumber ***/
$strbugnumber = strtoupper($strbugnumber);
$defects = preg_split('/,/', $strbugnumber);
$i = 0;
$XR_ID = "";
$XR_type = "";
$XR_Summary = "";
$mtkpatch_on = False;
$isDefectType=false;
foreach($defects as $defect){
  if(!is_simplex_bugs($defect)){
    $mtkpatch_on = True;
    $XR_ID[$i] = $defect;
    $error_flag = exec("$patch_delivery_path"."ALM_check.py $product_name $defect",$out);
    if (count($out) >=($i*6+6)){
	$XR_Summary[$i] = $out[$i*6+1];
	$XR_type[$i] = $out[$i*6+2];
	writeline("\nDefect|Task Summary:".$out[$i*6+1]."\n");  //add anterior \n by shuangyan.he 20150321
	just_continue(" to go ");
	$filterbranch=array();
	$defectbranch = $out[$i*6+3];
        $project_spm = $out[$i*6+4];
        //print $project_spm; 
        //print $XR_type[$i];
        //add by lyf 2015-11-25 for no match alm branch/

	$filterbranch=preg_grep("/^all$/",$almbranchs);
        if ( !empty($filterbranch)){

	}
	else if ( !empty($almbranchs)){
       //add by lyf 2015-11-25 for no match alm branch/
		$filterbranch=preg_grep("/^$defectbranch$/",$almbranchs);
		//print $defectbranch;
		if( empty($filterbranch)) {
		    print "\033[31;1malmbranchs\033[0m\n";
		    var_dump($almbranchs);
		    print "\033[31;1mdefectbranch,$defectbranch\033[0m\n";
		    print "\033[31;1mbug's branch is error,Pls input $product_name's pr!\033[0m\n";
		    exit;
		}
	}
    }else if (count($out) >=($i*3+3)){
	$XR_Summary[$i] = $out[$i*3+0];
	$XR_type[$i] = $out[$i*3+1];
    }else{
	$XR_Summary[$i] = '';
	$XR_type[$i] = '';
    }
    switch ($error_flag) {
        case 200:
	    print "OK! go next \n";
	    break;
        case 201:
	    print "State isn't Resolved or Opened ,please check status of task or defect!\n";
	    exit;
	    break;
        case 206:
	    print "This id doesn't have any related utc , please check bugid or contact with SPM! \n";
	    exit;
	    break;
        case 404:
	    print "This id doesn't exist in Integrity , please check bugid! \n";
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
	case 600:
	    print "The defect/task state is not Opened or Resolved , please check! \n";
	    exit;
	    break;
    }
    if ($XR_type[$i] == 'Defect'){
        $isDefectType=true;
    }
    $i++; 
  }else{ //for simplex bugs
    $HOST_URL = "http://10.92.35.176/pms";
    $UPLOAD_URL = $HOST_URL."/project/xrinfo/0/getProductTaskStateInfo";
    $post_data = array('taskNo' => $defect);
    $post_data = json_encode($post_data);
    $post_data = array('jsonContent' => $post_data);
    $result=upload_exception_to_simplex($UPLOAD_URL, $post_data);
    if($result == ''){
       print "\033[31;1m login Simplex time out.\033[0m\n";
       exit;
    }
    $response = json_decode($result,true);
    if(count($response['data'])){
        writeline("\nDefect|Task Summary:".$response['data']['summary']."\n");
        just_continue(" to go ");
        /* add by xiaoying.huang check numberID  2018-12-10*/
        $filterbranch=preg_grep("/^all$/",$almbranchs);
        $simplexbranch = $response['data']['almbranch'];
        if (!empty($filterbranch) || empty($simplexbranch)){

	     }
	    else if ( !empty($almbranchs)){
           //add by lyf 2015-11-25 for no match alm branch /
           
		   $filterbranch=preg_grep("/^$simplexbranch$/",$almbranchs);
		   if( empty($filterbranch)) {
		    print "\033[31;1malmbranchs\033[0m\n";
		    var_dump($almbranchs);
		    print "\033[31;1mdefectbranch,$simplexbranch\033[0m\n";
		    print "\033[31;1mbug's branch is error,Pls input $product_name's pr!\033[0m\n";
		    exit;
		    }
	     }
        /*   xiaoying.huang check numberID  2018-12-10*/
        switch ($response['data']['result']){
            case 0:
	        print "\033[31;This id doesn't exist in Integrity , please check bugid! \033[0m\n";
	        exit;
	        break;
            case 1:
	        print "OK! go next \n";
                break;
            case 2:
	        print "\033[31;1m The defect/task state is not Opened or Resolved , please check! \033[0m\n";
                exit;
                break;		
        }
    }
    // begin add send user pass to simplex by xiaoying.huang 2017-11-8
    while($send_pass){
	   writeline("\nInput your password of realm name:");
       $author_pass = read_line(); 
       $resultd=update_simplex_userpass($author_name,$author_pass);
         $resultdic = json_decode($resultd,true);
	     if($resultdic['status'] == 0)
	         $send_pass=false;
    }
    // end by xiaoying.huang 2017-11-9
  }
}

//step 4: Simplex Check XR
if($xr_tool_value == 0){
    print "\033[31;1m config error XR tool, not ALM or Bugzilla.\033[0m\n"; 
    exit;
}
$xrNo = '';
$status = '';
$TypeList = '';
$defects = preg_split('/,/', $strbugnumber);
foreach($defects as $defect){
 if(!is_simplex_bugs($defect)){
    $HOST_URL = "http://10.92.35.176/pms";
    $GET_URL = $HOST_URL."/project/patch-delivery/0/xrInfo?xrNo=".$defect."&email=".$author_email."&dataSourceType=".$xr_tool_value;
    $Type = get_simplex_response_info($GET_URL);
    if(count($Type['data'])){
	if($Type['msg'] == 'checked'){
	    print "\033[35;1m The XR is exception, but it has been recoreded on Simplex, do not need input again\033[0m\n";
	    continue;
	}
        $xrNo = $Type['data'][0]['xrNo'];
        $status = $Type['data'][0]['status'];
    }
    /*if($status == 'delay' || $status == 'reg' || $status == 'both'){
	if($status == 'both'){
	    $remark_comment  = input_comment_info('English Regression or delay Reason:');
	    writeline("The defect ".$defect." was both delay and regression status, begin to input the info:\n");
	    interactive_exception_info_with_simplex($xrNo, 'both', $xr_tool_value, $author_email, $remark_comment);
	    //$regression_comment = input_comment_info("English Regression Reason");
            $remark_comment = str_ireplace(" ","\ ",$remark_comment);
	    $error_flag = '';
	    do{
	        $error_flag = upload_reg_reason_to_alm($xrNo, $remark_comment);
		if($error_flag == 'AuthFail'){
		    writeline("\nUsername or Password Error, please input again:\n");
		}
	    }while($error_flag == 'AuthFail');

	   // interactive_exception_info_with_simplex($xrNo, 'reg', $xr_tool_value, $author_email, $remark_comment);

	}else if($status == 'reg'){
	    writeline("The defect ".$defect." was ".$status." status, begin to input the ".$status." info:\n");
 	    $remark_comment  = input_comment_info('English Regression Reason:');
	    interactive_exception_info_with_simplex($xrNo, $status, $xr_tool_value, $author_email, $remark_comment);
            $remark_comment = str_ireplace(" ","\ ",$remark_comment);
	    $error_flag = '';
	    do{
	        $error_flag = upload_reg_reason_to_alm($xrNo, $remark_comment);
		if($error_flag == 'AuthFail'){
		    writeline("\nUsername or Password Error, please input again:\n");
		}
	    }while($error_flag == 'AuthFail');
	}else{
	    writeline("The defect ".$defect." was ".$status." status, begin to input the ".$status." info:\n");
 	    $remark_comment  = input_comment_info('English delay Reason:');
	    interactive_exception_info_with_simplex($xrNo, $status, $xr_tool_value, $author_email, $remark_comment);
	}
	
    }*/
    if($status == 'reg'){
	writeline("The defect ".$defect." was ".$status." status, begin to input the ".$status." info:\n");
        $remark_comment  = input_comment_info('English Regression Reason:');
        interactive_exception_info_with_simplex($xrNo, $status, $xr_tool_value, $author_email, $remark_comment);
        $remark_comment = str_ireplace(" ","\ ",$remark_comment);
        $error_flag = '';
        do{
            $error_flag = upload_reg_reason_to_alm($xrNo, $remark_comment);
            if($error_flag == 'AuthFail'){
                writeline("\nUsername or Password Error, please input again:\n");
            }
        }while($error_flag == 'AuthFail');
    }
 }
}


//add by xiaoying.huang 2018-12-11
echo "\nIs it rom issue? (Y/N)  ";
$isrom = read_line();
$isrom = strtoupper(trim($isrom));
while(!(($isrom =='Y' || $isrom =='N' ))){
		echo "Pls input Y/N !  ";
		$isrom = read_line();
		$isrom = strtoupper(trim($isrom));
}
//end by xiaoying.huang 2018-12-11

#start add by xiaoying.huang 20190613 
if (in_array($product_name,get_confreview($patch_delivery_path,"autobuildconfig"))){
$autobuildflag=true;
}
#end add by xiaoying.huang 20190613 
//add by xiaoying.huang 2019-05-29
#if($autobuildflag){
#  echo "\nDoes this patch depend on others? (Y/N)  ";
#  $istopic = read_line();
#  $istopic = strtoupper(trim($istopic));
#  while(!(($istopic =='Y' || $istopic =='N' ))){
#		echo "Pls input Y/N !  ";
#		$istopic = read_line();
#		$istopic = strtoupper(trim($istopic));
#  }
#}
$istopic='Y';
//end by xiaoying.huang 20190529

//begain add by shuangyan.he 20160620
$mtk_patch_number = "";
$vnum_choice="";
$id_choice="";
$patchtype="";
if($mtkpatch_on && $isrom == 'N'){
	echo "\nIs it mtk patch? (Y/N)  ";
	$mtk_patch_ornot = read_line();
	$mtk_patch_ornot = strtolower(trim($mtk_patch_ornot));
	while(!(( $mtk_patch_ornot =='yes' || $mtk_patch_ornot =='y' || $mtk_patch_ornot =='no' || $mtk_patch_ornot =='n' ))){
		echo "Pls input Y/N !  ";
		$mtk_patch_ornot = read_line();
		$mtk_patch_ornot = strtolower(trim($mtk_patch_ornot));
	}
	if($mtk_patch_ornot=='yes' || $mtk_patch_ornot =='y'){
		echo "\nPls input Pnum? (eg:p20) ";
		$mtk_patch_number = read_line();
		$mtk_patch_number = trim($mtk_patch_number);
		$mtk_patch_number = strtolower($mtk_patch_number);
		$last_mtkpatch_num = 0;
		$lastest_mtkpatch_num = array();
		while(!preg_match("/^p?(\d+)$/", $mtk_patch_number,$lastest_mtkpatch_num)){
			echo "\nwrong! Pls input Pnum? (eg:p20) ";
			$mtk_patch_number = read_line();
			$mtk_patch_number = trim($mtk_patch_number);
			$mtk_patch_number = strtolower($mtk_patch_number);}
		if (is_numeric($mtk_patch_number)){
			$mtk_patch_number = "p"."$mtk_patch_number";}
		$mtkpatch_number=$mtk_patch_number;
		if (count($lastest_mtkpatch_num)>1)
		{
			$last_mtkpatch_num = $lastest_mtkpatch_num[1] -1;
		}
		if (!empty($mtk_patch_number)){
			print "\033[32;1m    mtkpatch Pnum: $mtk_patch_number\033[0m\n";
			$patchtypelist=array('ALPS','MOLY','SIXTH');
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
			
			//add by lyf 2016-12-09 check last mtk patch number is merged
			$lastpatch_status = get_last_mtkpatch_mergestatus($patch_delivery_path,$product_name,$last_mtkpatch_num,$patchtype,$prj_link_file);
			$lastpatch_status_simplex = get_last_mtkpatch_mergestatus_simplex($patch_delivery_path,$product_name,$last_mtkpatch_num,$patchtype);
			if ($lastpatch_status == 0)
				print "last mtk patch is merged!";
			elseif ($lastpatch_status == 1)
			{
				print "\n\033[31;1mNot found Last mtk Patch P$last_mtkpatch_num record in SmartTask! Please Contact to INT(shie.zhao)!\033[0m \n";
				exit;
			}
			elseif ($lastpatch_status == 2)
			{
				print "\n\033[31;1mLast mtk Patch P$last_mtkpatch_num not merged to $git_branch_name branch! Please contact last mtkpatch owner!\033[0m \n";
				exit;
			}
			elseif ($lastpatch_status == 3)
			{
				print "\n\033[31;1m Can't connect to SmartTask!  Please Contact to INT!\033[0m \n";
				exit;
			}		
			// end by lyf 2016-12-09
			# begain add by shuangyan.he 20160715
			$dicidvnum=getidvnum_mtkmerge($mtk_patch_number,$product_name,$patchtype);
			$dicidvnum_simplex=getidvnum_mtkmerge_simplex($mtk_patch_number,$product_name,$patchtype);
			while(empty($dicidvnum)){
				echo "\n\033[31;1mdo you input Pnum right?\033[0m Y=>go on and contact INT, N=>modify Pnum.\n";
				echo "Your choise Y/N: ";
				$vpnum = read_line();
				$vpnum = strtolower(trim($vpnum));
				while(!(($vpnum=='yes'||$vpnum=='y'||$vpnum=='no'||$vpnum=='n'))){
					echo "Pls input Y/N: ";
					$vpnum = read_line();
					$vpnum = strtolower(trim($vpnum));}
				if($vpnum=='no'||$vpnum=='n'){
					echo "input right Pnum: ";
					$vpnum = read_line();
					$vpnum = strtolower(trim($vpnum));
					$dicidvnum=getidvnum_mtkmerge($vpnum,$product_name,$patchtype);
					$dicidvnum_simplex=getidvnum_mtkmerge_simplex($vpnum,$product_name,$patchtype);}
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


//setp 5: input some comments
$str_root_cause = '';
$str_bug_category = '';
$str_generated_by = '';
$str_para_Component = '';
$str_para_IC_information = '';
$str_para_Supplier = '';
$str_para_hw_para_version = ''; 

if($txt_fill == "-t"){
	$patch_comment = readtxt("patch comments:","root cause detail:");
// 2.Get patch comments from input
}else{
	$patch_comment = input_comment_info("patch comments");
}

if(trim($strbugnumber) != ""){
    writeline("Please chose the bug root cause:\n");
    $selected_type_number = select_option($arr_root_cause);
    $str_root_cause = $arr_root_cause[$selected_type_number - 1];
}

//begin add Relate FR for root cause is Specification 20170724 
$strrelatedfr='';
if($str_root_cause == 'Specification' && $isDefectType){
	$input = true;
	do{
	    if($input == false){
		writeline("wrong Problem FR number,\n".
			  " 1) If there is no number,this tools didn't work.\n".
		          " 2) If there are several FR,please separate them by \",\"\n");
	    }
	     writeline("\nInput Problem FR Number:");
	     $strrelatedfr = read_line();
	     $input = false;
	}
	while(!is_correct_bugs_input($strrelatedfr));
}
//end add Relate FR for root cause is Specification 20170724 

// 1.Get root cause detail from .txt
if($txt_fill == "-t"){
	$str_root_cause_detail = readtxt("root cause detail:","Module_Impact:");
// 2.Get root cause detail from input
}else{
	$str_root_cause_detail =input_comment_info("root cause detail");
}

if(trim($strbugnumber) != ""){
    writeline("Please chose the Bug category:\n");
    $selected_type_number = select_option($arr_bug_category);
    $str_bug_category = $arr_bug_category[$selected_type_number - 1];
}
if($str_bug_category == "TCT"){
    writeline("Please chose the category:\n");
    $selected_type_number = select_option($arr_jrd_bug);
    $str_generated_by_array = explode("-",$arr_jrd_bug[$selected_type_number - 1]);
    $str_generated_by = $str_generated_by_array[1];
}
if($str_generated_by == "Parameters"){
    writeline("Please chose the Parameter Component:\n");
    $selected_type_number = select_option($arr_para_component);
    $str_para_Component = $arr_para_component[$selected_type_number - 1];
    $str_para_IC_information = input_comment_info("IC information");
    $str_para_Supplier = input_comment_info("Supplier"); 
    $str_para_hw_para_version = input_comment_info("HW parameter version"); 

}
#begin add by xiaoying.huang only for telecom 'issue caused by who is:20181016'
$arr_whocause=get_confreview($patch_delivery_path,"issuecaused_who_config");

$str_bug_whocause='';
if($str_generated_by == "SW Code" and count($arr_whocause)>0){
    writeline("issue caused by who is:\n");
    $selected_type_number = select_option($arr_whocause);
    $str_bug_whocause = $arr_whocause[$selected_type_number - 1];
}
#end add by xiaoying.huang only for telecom 'issue caused by who is:20181016'

// 1.Get Module_Impact from .txt
if($txt_fill == "-t"){
	$str_module_impact = readtxt("Module_Impact:","Test_Suggestion:");
}else{	
	#$str_module_impact = input_comment_info("Module_Impact");
	$str_module_impact = 'none';
}

	
//bgein add by lyf2015-8-13
//20170725 for del Menutree for SQE requestion
/***writeline("\nChange Menutree/image resource? (Y/N) ");  //add anterior \n by shuangyan.he 20150321
$is_resource_change = read_line();
$is_resource_change = strtolower(trim($is_resource_change));
while( !(( $is_resource_change =='yes' || $is_resource_change =='y' || $is_resource_change =='no' || $is_resource_change =='n' )) ){
    writeline("Input Y/N to contiune!");
    $is_resource_change= read_line();
    $is_resource_change = strtolower(trim($is_resource_change));
   
}
***/
//end by lyf 2015-8-13
// start by shuangyan.he 20160323
// 1.Get Test_Suggestion from .txt

if($txt_fill == "-t"){
	$str_test_suggestion = readtxt("Test_Suggestion:","----------------------end----------------------");
// 2.Get Test_Suggestion from input
}else{	
	#writeline("Test_Suggestion:");
	#$arr_test_suggestion = read_lines(".");
        $str_test_suggestion = input_comment_info("Test_Suggestion"); 
}
$str_solution='none';
/*** 20170724 del solution\test_report for SQE requesion***/
/**
if($txt_fill == "-t"){
	$arr_solution = readtxt("Solution:","Test_Report:");
	$arr_test_report = readtxt("Test_Report:",'----------------------end----------------------');
// 2.Get Solution/Test_Report from input
}else{
// end by shuangyan.he 20160324
	writeline("Solution:");
	$str_solution = input_comment_info("Solution");
	#writeline("Test_Report:");
	#$arr_test_report = read_lines(".");
}
**/
//
//begin add Defect Quality for defect ID 20170724 
$str_defect_quality='Normal';

/**$str_defect_quality='';
if ($isDefectType){
    writeline("Please chose the Defect Quality:\n");
    $selected_type_number = select_option($arr_defect_quality);
    $str_defect_quality = $arr_defect_quality[$selected_type_number - 1];
}
**/
$stractualhours='2';
/**
$input = true;
do{
    if($input == false){
       writeline("wrong hours number, please input right hours time,eg. 0.5\n");
    }
    writeline("\nInput Actual Hours:");
    $stractualhours = read_line();
    $input = false;
}
while(!is_correct_hours_input($stractualhours));
**/
//end

//begain add by shuangyan.he 20160620
if (!empty($mtk_patch_number)){
	$mtk_patch = "[mtk patch]";
	$mtk_patch_number = "[$mtk_patch_number]";
	$comments = "###%%%comment:".$mtk_patch.$mtk_patch_number.$patch_comment."\n";
}else{
	$comments = "###%%%comment:".$patch_comment."\n";
}
//end add by shuangyan.he 20160620

/*** get all comments, will display on gerrit ***/
//$comments = "###%%%comment:".$patch_comment."\n";
$comments .= "###%%%bug number:".trim($strbugnumber)."\n";
$comments .= "###%%%product name:".$product_name."\n";
if($str_root_cause !=""){
    $comments .= "###%%%root cause:".$str_root_cause."\n";
    if ($strrelatedfr != ""){
        $comments .= "###%%%Problem FR:".$strrelatedfr."\n";
    }
    $comments .= "###%%%root cause detail:".$str_root_cause_detail."\n";
}

if ($str_bug_category !=""){
    if ($str_bug_category == "GMS/3rd Party/MIBC") { $str_bug_category = "3rd Party";}
    $comments .= "###%%%Bug category:".$str_bug_category."\n";
}
if ($str_generated_by !=""){
    if ($str_generated_by == "Customization") { $str_generated_by = "Customization mechanism";}
    $comments .= "###%%%Generated by:".$str_generated_by."\n";
}
if ($str_para_Component !=""){
   $comments .= "###%%%Component:".$str_para_Component."\n";
}
if ($str_para_IC_information !=""){
   $comments .= "###%%%IC information:".$str_para_IC_information."\n";
}
if ($str_para_Supplier !=""){
   $comments .= "###%%%Supplier:".$str_para_Supplier."\n";
}
if ($str_para_hw_para_version !=""){
   $comments .= "###%%%HW parameter version:".$str_para_hw_para_version."\n";
}
#begin add by xiaoying.huang only for telecom 'issue caused by who is:20181016'
if ($str_bug_whocause !=""){
   $comments .= "###%%%issue caused by who is:".$str_bug_whocause."\n";
}
#end add by xiaoying.huang only for telecom 'issue caused by who is:20181016'

$comments .= "###%%%Module_Impact:".$str_module_impact."\n";
//begin add by lyf 2015-06-03
//$comments .= "###%%%Change Menutree or image:".$is_resource_change."\n";
//end add by lyf 2015-06-03

$comments .= "###%%%Test_Suggestion:".$str_test_suggestion."\n";
//foreach ($arr_test_suggestion as $comment){
//    if($comment != ".")
//        $comments .= $comment."\n";
//}
//$str_solution='none';
$arr_test_report=array('none');
$comments .= "###%%%Solution:".$str_solution."\n";
$comments .= "###%%%Test_Report:";
foreach ($arr_test_report as $comment){
    if($comment != ".")
        $comments .= $comment."\n";
}
//$str_bug_reason = $str_root_cause;
//if ($str_bug_reason !=""){
   //$comments .= "###%%%Bug_Reason:".$str_bug_reason."\n";
//}

if ($str_defect_quality != ""){
   $comments .= "###%%%Defect Quality:".$str_defect_quality."\n";
}
if ($stractualhours != ""){
   $comments .= "###%%%Actual Hours:".$stractualhours."\n";
}


$comments .= "###%%%author email:".$author_email."\n";
if($isImpactFota){
   $impactFotaNotice="[Fota Warning] some changes may impact fota!!";
   $comments .= "###%%%".$impactFotaNotice."\n";
}
$comments = str_replace("\n", "", $comments);
$comments = str_replace('"', "'", $comments);
$array_comments = preg_split('/###%%%/',$comments);
$temp=" ";
foreach($array_comments as $item){
    if($item != ""){
      $temp.="###%%%".$item."\n";
    }
    $comments= trim($temp);     
}

foreach($gits_addfile_array as $git_name=>$arr_add_file){//add by renzhi.yang.hz 2018-11-28
if(file_exists($current_git_dir)){
chdir($git_local_path_array[$git_name]);
print "The current git you are pushing is $git_name\n";
}

$git_path_name=$git_path_name_array[$git_name];
$git_branch_name=$git_branch_name_array[$git_name];
$repo_branch=$repo_branch_array[$git_name];
//add by lyf 2017-04-21 for frameworks-res 
$isFRM1review=false;
if(empty($mtk_patch_number))
{
   $isFRM1review = check_frameworks_res_submit($git_name,$author_email);
}
//end by lyf 2017-04-21 for frameworks-res 


//add by xiaoying.huang 20180202 for add remind other branch send
$cread='';
if(strpos($git_name,"wcustores") || strpos($git_name,"wprocedures")){
	writeline("If this patch need to sync to other project ,Please remember to git push this patch to other project later by yourself(ok/no?):"); 
    $cread = read_line();	    	
}
//end by xiaoying.huang

//add by xiaoying.huang add by topic 20190529
$topicflag="";
if($istopic =='Y'){
    $topicflag='%topic=ALM-'.trim($defects[0]);
}
//end by xiaoying.huang add by topic 20190529
//step5: push code to gerrit web

//add by renzhi.yang.hz for patch delivery to shenzhen server 10.129.93.179
$arr_sz_branches=get_confreview($patch_delivery_path,"SZbranch");
$is_sz_branch=false;
$result_git_path_name = get_push_branch($git_branch_name);
print "result_git_path_name $result_git_path_name\n";
print "repo_branch $repo_branch\n";
foreach($arr_sz_branches as $key=>$value){
 print "key: --- value: $key $value\n";
 if(trim($value) == trim($result_git_path_name) | trim($value) == trim($repo_branch)){
$is_sz_branch=true;}
}
//end by renzhi.yang.hz for patch delivery to shenzhen server 10.129.93.179
$git_push = "";
do{
    if($is_sz_branch){
    $server="10.129.93.179";
}
    $flag=true;    
    $git_push = "git push ssh://".$author_name."@".$server.":29418/".$git_path_name." HEAD:refs/for/".$result_git_path_name.$topicflag;
    $git_push_nothin = "git push --no-thin ssh://".$author_name."@".$server.":29418/".$git_path_name." HEAD:refs/for/".$result_git_path_name.$topicflag; //--no-thin 
    print("push command: ".$git_push."\n");
    writeline("please check command (yes/no):");
    $cause_number = read_line();
    if($cause_number == "yes"){
	$flag = false;
    }
    else if ($cause_number == "no"){
       writeline("please input push command:"); //add by zhaoshie 20160406
       $git_push = read_line();
       print("push command: ".$git_push."\n");
       $string = explode('ssh://',$git_push);     
       $git_push_nothin = $string[0]."--no-thin ssh://".$string[1];   
       $flag = false;
       #exit;  //update by zhaoshie 20160406
    }else{
        $flag=true;
    }
}while($flag);

/*** execute git add,git commit, git push command***/
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
    system("git commit -m \"$comments\" $strcommitfiles");  //rm --no-verify for change-id
}else{
    system("git commit -m \"$comments\"");
}
system($git_push,$result);
#print($result."\n");

if (($result=='1') || ($result==1) || ($result==128) || ($result=='128')) {
  print "push fail,try again....\n";
  print("push command :".$git_push_nothin."\n");
  system($git_push_nothin,$result);
  if (($result=='1') || ($result==1) || ($result==128) || ($result=='128')) {    
     print "\033[31;1m push error!! please check push command !!\033[0m\n";
     exit;
  }
}
$autobuildflag=true;
if($autobuildflag){
  echo "\nWhether to start server compilation? 所有关联patch是否已经提交完？ (Y/N) \n（1.如果只提交一个patch，则直接输入Y;\n 2.如果提交几个patch并且有依赖关系的非共库分支，则需要最后一个patch输入Y;\n 3.如果提交有共库分支的几个patch，需要按照共库范围最大的输入Y，比如TCTROM-Q-V3.0-dev>TCTROM-Q-QCT-V3.0-dev(TCTROM-Q-MTK-V3.0-dev),这样能保证在mtk/qual两个项目上编译） ";
  $isputchpk= read_line();
  $isputchpk = strtoupper(trim($isputchpk));
  while(!(($isputchpk =='Y' || $isputchpk =='N' ))){
		echo "Pls input Y/N !  ";
		$isputchpk = read_line();
		$isputchpk = strtoupper(trim($isputchpk));
  }
  if($isputchpk =='Y' || $istopic =='N'){
	    $changeId=getChangeId($author_name,$git_path_name,$server);
        $git_push_ok = "ssh -o ConnectTimeout=60 -p 29418 ".$author_name."@".$server." gerrit review  -m '\"uploadOK!!\"' ".$changeId.",1";
        echo "isputchpk==========$git_push_ok \n";
        system($git_push_ok,$result);
  }
}
//add by shie.zhao 20170613 for FRM1review
if($isFRM1review){
  add_FRM1_reviewer_frameworks_res_submit($author_name,$git_path_name,$server);
}else{
  add_reviewer_frameworks_res_submit($author_name,$git_path_name,$server);
}
//end

//add by junbiao.chen 20150610
if($isImpactCustomization){
	global $commit_customizations;
	$currentCommitId=getCurrentCommitId();
	$changeId=getChangeId($author_name,$git_path_name,$server);
	$git_path_name_arr=explode("/",$git_path_name,2);
	$git_path_name_arr1=$git_path_name_arr[0];
	$git_path_name_arr2=$git_path_name_arr[1];
	$impactCustomizationFileAndDir='';
        foreach ($commit_customizations as $each){
            $impactCustomizationFileAndDir .= $each.'<br/>';
        }
        $customization_subject="[Customization Warning][$repo_branch] some changes may impact customization on CLID";
    	$customization_body="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
    	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	</head>  
    	<body>  
    	<span style='color:red'><b>"."$author_name"."</b></span>
	<span> has modified as follow files:</span><br/>
	<span>"."$impactCustomizationFileAndDir"."</span><br/><br/>
	<b>Pls Notice these changes,they may impact customization on CLID,pls customize them on CLID immediately</b><br/><br/>
	<span style='color:green'>Change Info on Gerrit:</span><br/>
	<a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
	<span style='color:green'>Change Info on Git-Web:</span></br>
	<a href='http://$server/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId'>http://$server/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId</a>
    	</body>  
    	</html>";
    	//$customization_sendmail_tolist=array('yinfang.lai@tcl.com');
	sendEmail($customization_sendmail_tolist,$customization_subject,$customization_body,$customization_sendmail_cclist);
}


//step 6:send email if has some commits impact fota
if($isImpactFota){
    $currentCommitId=getCurrentCommitId();
    $changeId=getChangeId($author_name,$git_path_name,$server);
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
    <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    </body>  
    </html>";
    ###<span style='color:green'>Change Info on Git-Web:</span></br>
    ###<a href='http://10.92.32.10/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId'>http://10.92.32.10/sdd2/gitweb-$git_path_name_arr1/?p=$git_path_name_arr2.git;a=commit;h=$currentCommitId</a>

    #sendEmail($fota_sendmail_tolist,$fota_subject,$fota_body);
    $mail_count=count($fota_sendmail_tolist);
    $fota_sendmail_tolist[$mail_count]=$project_spm;
    //$fota_sendmail_tolist=array('yinfang.lai@tcl.com');
    sendEmail($fota_sendmail_tolist,$fota_subject,$fota_body);
}

//Begain add by shuangyan.he 20160621
$index_mtkpatch=0;
$commit_mtkpatch=get_mtkpatch_commit_gitpath();
if (!empty($mtk_patch_number)&&!empty($patchtype)&&!empty($vnum_choice)){
	//begain add by shuangyan.he 20160710
	$changeId=getChangeId($author_name,$git_path_name,$server);
	$gerrit_link="http://$server:8081/#/c/$changeId/";
	$TS=date('Y-m-d H:i:s',time());
	$arrmtk_id=update_mtkpatch_mtkmerge($mtkpatch_number,$product_name,$patchtype,$vnum_choice,$strbugnumber,$gerrit_link,$author_email,$TS);
	$arrmtk_id_simplex=update_mtkpatch_mtkmerge_simplex($mtkpatch_number,$product_name,$patchtype,$vnum_choice,$strbugnumber,$gerrit_link,$author_email,$TS);
	#start add by renzhi.yang 20170816
	updateMtkpatchStatus($mtkpatch_number,$product_name,$patchtype,$vnum_choice,$gitname_for_mtkpatch,1);
	#end add by renzhi.yang 20170816
	//end add by shuangyan.he 20160710
}
if (!empty($mtk_patch_number)&&!empty($patchtype)){
	$arr_mtkpatch_recipients = array("xiaodan.cheng@tcl.com","shie.zhao@tcl.com",$author_email);
	//$arr_mtkpatch_recipients = array("835344992@qq.com",$author_email);
	$mtkpatch_subject="[MTK patch remind]"."$mtk_patch_number"."[$repo_branch] MTK patch delivered to Gerrit";
	$changeId=getChangeId($author_name,$git_path_name,$server);
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
	<a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
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


//add by zhaoshie 20161101 for ImpactTEE
$index_tee=0;
$commit_tee=get_teeupdate_from_commit_contents();
if(!empty($commit_tee)){
	  $author_name=get_author_email();
	  $currentCommitId=getCurrentCommitId();
	  $changeId=getChangeId($author_name,$git_path_name,$server);
          $tee_subject="[TEE Warning][$repo_branch]TEE模块有更新，请负责人点检相关功能";
    	  $tee_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>";          
          for($i=0;$i<$index_tee;$i++){           
             $tee_commit="<span>"."$commit_tee[$i]"."</span><br/><br/>";
             $tee_body .= $tee_commit;
          }
          $tee_body .= "
	  <b>Pls Notice these changes,they may impact TEE</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    	  </body>  
    	  </html>";
          $mail_count=count($arr_tee_sendmail_tolist);
          $arr_tee_sendmail_tolist[$mail_count]=$project_spm;
          #$arr_tee_sendmail_tolist=array('shie.zhao@tcl.com');
	  sendEmail($arr_tee_sendmail_tolist,$tee_subject,$tee_body);
}
$index_persist=0;
$commit_persist=get_persistupdate_from_commit_contents();
if(!empty($commit_persist)){
	  $author_name=get_author_email();
	  $currentCommitId=getCurrentCommitId();
	  $changeId=getChangeId($author_name,$git_path_name,$server);
          $persist_subject="[Persist Warning][$repo_branch]分区表有变化，请FOTA, HDCP负责人点检相关模块";
    	  $persist_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>";          
          for($i=0;$i<$index_persist;$i++){
             $persist_commit="<span>"."$commit_persist[$i]"."</span><br/><br/>";
             $persist_body .= $tee_commit;
          }
          $persist_body .= "
	  <b>Pls Notice these changes,Persist partition table update, they may impact FOTA or HDCP</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    	  </body>  
    	  </html>";
          $mail_count=count($arr_persist_sendmail_tolist);
          $arr_persist_sendmail_tolist[$mail_count]=$project_spm;
          #$arr_tee_sendmail_tolist=array('shie.zhao@tcl.com');
	  sendEmail($arr_persist_sendmail_tolist,$tee_subject,$tee_body);
}
//end 
//*/
/* add remind for diff projects with same xml by renzhi.yang.hz 2019-10-21*/
$need_remind_lunch_projects=array();
if(!empty($all_projects)){
	  print "all_project\n";
	  //var_dump($all_projects);
	  for($j=0;$j<count($all_projects);$j++){
          foreach($need_commit_contents_all as $each_commit_content){
              //print "all_projects[j][1]]\n";
              //var_dump($all_projects[$j][1]);
              print "each_commit_content,$each_commit_content\n";
              print "git_path_name,$git_path_name\n";
              if(strpos($each_commit_content,$all_projects[$j][1]) | strpos($git_path_name,$all_projects[$j][1])){
                  $need_remind_lunch_projects[$all_projects[$j][0]]=$all_projects[$j][1];
                  unset($all_projects[$j]);
	          }
          }
	  }
	  //print "need_remind_lunch_projects";
	  //var_dump($need_remind_lunch_projects);
	  $need_also_modifie_array=array();
	  if(count($need_remind_lunch_projects)>0 ){
	  	  for($j=0;$j<count($all_projects);$j++){
	  	  $tmp_string=$all_projects[$j][0].":".$all_projects[$j][1];
	  	  //print "tmp_string,$tmp_string";
	  	  $need_also_modifie_array[]=$tmp_string;
	  	  }
	  	  //print "need_also_modifie_array $need_also_modifie_array";
	  	  $need_also_modified=implode(';',$need_also_modifie_array);
	  	  //print "get removed all_projects";
    	  //var_dump($all_projects);
    	  $need_remind_lunch_projects_key=array_keys($need_remind_lunch_projects);
    	  print "need_remind_lunch_projects_key,";
    	  //var_dump($need_remind_lunch_projects_key );
    	  $remind_content_project=$need_remind_lunch_projects_key[0];
     	  $remind_content_project_linch=$need_remind_lunch_projects[$remind_content_project];
     	  print "remind_content_project remind_content_project_linch $remind_content_project $remind_content_project_linch\n";
	  	  $author_name=get_author_email();
	  	  $currentCommitId=getCurrentCommitId();
	  	  $changeId=getChangeId($author_name,$git_path_name,$server);
          	  $lunch_project_subject="[$remind_content_project]"."[$remind_content_project_linch]"."配置相关文件被修改，请确认所有共用该代码的项目是否需要同步修改";
    	  	  $lunch_project_body="<html><head>
    	  	  <meta http-equiv='Content-Language' content='zh-cn'>
    	  	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
   	  	  </head>
    	  	  <body>
    	  	  <b>Pls Notice:$remind_content_project modified some files about project $remind_content_project_linch,please check whether need to modfied $need_also_modified.</b><br/><br/>
          	  <span style='color:green'>Change Info on Gerrit:</span><br/>
          	  <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    	 	  <span style='color:red'><b>"."$author_name"."</b></span>
	  	      <span> has modified as follow files:</span><br/>";
	  	      var_dump($need_commit_contents_all);
	  	      for($i=0;$i<count($need_commit_contents_all);$i++){
              $lunch_project_commit="<span>"."$need_commit_contents_all[$i]"."</span><br/><br/>";
              $lunch_project_body .= $lunch_project_commit;
              }
              $lunch_project_body .= "
          	  </body>  
          	  </html>";
          	  $arr_diffproject_samecode_remind_tolist=array();
          	  $mail_count=count($arr_diffproject_samecode_remind_tolist);
          	  $arr_diffproject_samecode_remind_tolist[$mail_count]=$project_spm;
          	  $arr_diffproject_samecode_remind_tolist[$mail_count+1]=$author_name;
          	  var_dump($arr_diffproject_samecode_remind_tolist);
          	  sendEmail($arr_diffproject_samecode_remind_tolist,$lunch_project_subject,$lunch_project_body);
      }
}
//end

/* add patchchange_email_remindwentai by xiaoying,huang 20171216*/
$gitarr=get_wentaiConfig($patch_delivery_path,$repo_branch);
if(in_array($git_path_name,$gitarr)){
    $arr_chpatch_recipients = array("baoge.li.hz@tcl.com","zhihua.fang.hz@jrdcom.com","TCT-HZ-ODM-WT-SW@jrdcom.com","xiaoying.huang@tcl.com","renzhi.yang.hz@tcl.com");
	#$arr_chpatch_recipients = array("xiaoying.huang@tcl.com");
	$chpatch_subject="[Patch Change remind]"."[$repo_branch] Patch delivered to Gerrit";
	$changeId=getChangeId($author_name,$git_path_name,$server);
	$chpatch_body ="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
	</head>  
	<body>  
	<span <b>"."Dear $author_email".":</b></span><br/><br/>";
	$chpatch_body .="<span><b>you already patch delivered to gerrit.</b></span><br/>
	<span style='color:green'><b>Branch:</b></span><br/>
	<span><b>$repo_branch</b></span><br/>
	<span style='color:green'><b>Git name:</b></span><br/>
	<span><b>$git_path_name</b></span><br/>
	<span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
	<a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>";   
	$chpatch_body .= "
	</body>  
	</html>";
	sendEmail($arr_chpatch_recipients,$chpatch_subject,$chpatch_body);
	print "\n----------Change patch sendEmail to remind wentai ok----------\n\n";
 }
/********end by xiaoying,huang 20171216********************/
/*** XR analyze for SIMPLEX ***/
if($str_generated_by != 'GAPP' ){
	foreach($defects as $defect){
           if(!is_simplex_bugs($defect)){
		$HOST_URL = "http://10.92.35.176/pms";
        	$UPLOAD_URL = $HOST_URL."/project/xrinfo/0/updateUploadedCodeStatus";
                $upload_type = check_upload_type($arr_add_file, $git_name);
                $post_data = array('xrNo' => $defect, 'uploadedCodeUserMail' => $author_email, 'uploadedType' => $upload_type);
        	$post_data = json_encode($post_data);
        	$post_data = array('jsonContent' => $post_data);
        	upload_exception_to_simplex($UPLOAD_URL, $post_data);
           }
	}
}
/*** XR rootcause for simplex ***/
if($str_root_cause !=""){
	foreach($defects as $defect){
           if(!is_simplex_bugs($defect)){
		$HOST_URL = "http://10.92.35.176/pms";
                $GET_URL = $HOST_URL."/project/patch-delivery/0/addRootCause?xrNo=".$defect."&rootCause=".$str_root_cause."&problemFrNo=".$strrelatedfr."&defectQuality=".$str_defect_quality."&actualResolvedTime=".$stractualhours;             
                get_simplex_response_info($GET_URL);
                #$handle = fopen("$GET_URL","r");
                #$readline = fgets($handle,2096);
                #print $readline;
           }
	}
}

/*** XR updaterootcause for simplex by xiaoying20181109***/
if($str_root_cause !=""){
	update_simplex_RootCause($strbugnumber,$str_root_cause,$str_root_cause_detail,$str_bug_category,$str_generated_by,$isrom);
}
/*** XR updaterootcause for simplex by xiaoying20181109***/

/**********add git push patche to smarttask by xiaoying.huang 20180514***/
$CommitArr = getCommitInfo($author_name,$git_path_name);
if(!empty($CommitArr))
    foreach($defects as $defect){	   
       addgitpushID($defect,$git_path_name,$CommitArr,$author_name);
    }
/**********add git push patche to smarttask by xiaoying.huang 20180514***/

//add by lyf 2017-02-13 for ImpactAudio
$index_audio=0;
$commit_audio=get_audio_update_from_commit_contents();
if(!empty($commit_audio)){
	  $author_name=get_author_email();
	  $currentCommitId=getCurrentCommitId();
	  $changeId=getChangeId($author_name,$git_path_name,$server);
          $audio_subject="[Audio Warning][$repo_branch]Audio模块有更新，请负责人点检相关功能";
    	  $audio_body="<html><head>
	  <meta http-equiv='Content-Language' content='zh-cn'>  
    	  <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   	  </head>  
    	  <body>  
    	  <span style='color:red'><b>"."$author_name"."</b></span>
	  <span> has modified as follow files:</span><br/>";          
          for($i=0;$i<$index_audio;$i++){           
             $audio_commit="<span>"."$commit_audio[$i]"."</span><br/><br/>";
             $audio_body .= $audio_commit;
          }
          $audio_body .= "
	  <b>Pls Notice these changes,they may impact audio</b><br/><br/>
	  <span style='color:green'>Change Info on Gerrit:</span><br/>
	  <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    	  </body>  
    	  </html>";
          $mail_count=count($arr_audio_sendmail_tolist);
          $arr_audio_sendmail_tolist[$mail_count]=$project_spm;
	  sendEmail($arr_audio_sendmail_tolist,$audio_subject,$audio_body);
}
//end by lyf 2017-02-13 for ImpactAudio

/***  inform reivewer to review***/
$i = 0;
/**
foreach($defects as $defect){
    #if(!is_simplex_bugs($defect) && $ismanaus == false){
	if(!is_simplex_bugs($defect)){	
	if ($XR_type[$i] == 'Defect' || $XR_type[$i] == 'Task'){
		$reqSourceType = 4;
	}else if ($XR_type[$i] == 'FR'){
		$reqSourceType = 2;
	}else{
		$reqSourceType = 3;
	}
	$HOST_URL = "http://10.92.35.176/pms";
	$UPLOAD_URL = $HOST_URL."/autotest/test-json/0/saveReqFromPatch?jsonContent=";
	$XR_Summary[$i] = str_ireplace("\"","'",$XR_Summary[$i]);
	$XR_Summary[$i] = urlencode($XR_Summary[$i]);
	$post_data = array('reqSourceType'=>$reqSourceType, 'reqSourceNo'=>$XR_ID[$i], 'reqDesc'=>$XR_Summary[$i],); 
	$post_data = json_encode($post_data);
	$UPLOAD_URL = $UPLOAD_URL.$post_data;
	$handle = fopen("$UPLOAD_URL","r");
	$readline = fgets($handle,2096);
	$result_dic = json_decode($readline,true);
	if(count($result_dic) && $result_dic['status'] == 0){
		$xrid = $XR_ID[$i];
		$autoid = $result_dic['data']['autoId'];
		$edit_addr = "http://10.92.35.176/pms/webpages/autoReq/edit.jsp?id=$autoid&fromPatch=true";
		$teamleaderEmail = '';
		$GET_URL = $HOST_URL."/project/patch-delivery/0/tlEmail?authorEmail=".$author_email;
		$handle = fopen("$GET_URL","r");
		$readline = fgets($handle,2096);
		$Type = json_decode($readline,true);
		$keys = array_keys($Type);
		if (count($Type['data'])){
			$teamleaderEmail = $Type['data'];
		}
		#print $teamleaderEmail;
		if ($teamleaderEmail != $author_email && $teamleaderEmail != ''){
			$auto_sendmail_tolist=array($author_email,$teamleaderEmail);
		}else{
			$auto_sendmail_tolist=array($author_email);
		}
		$auto_sendmail_cclist=array('jie.cao@tcl.com','guangming.yang.hz@tcl.com','yi.xie@tcl.com');
		$autocase_subject="[Auto team remind][$xrid]Please Update the autotest demand of XR $xrid in Simplex";
		$autocase_body="<html><head>
		<meta http-equiv='Content-Language' content='zh-cn'>  
    		<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
   		</head>  
    		<body>
		<span> Dear "."$author_name".":</span><br/>  
		<span> XR $xrid has been Resolved , please update the autotest demand in Simplex:</span><br/>
		<a href='http://10.92.35.176/pms/webpages/autoReq/edit.jsp?id=$autoid&fromPatch=true'>http://10.92.35.176/pms/webpages/autoReq/edit.jsp?id=$autoid&fromPatch=true</a><br/><br/>
		<span> If it can realize auto test,please transcribe or develop this testcase, Thank you!</span><br/>
    		</body>  
    		</html>";
		sendEmail($auto_sendmail_tolist,$autocase_subject,$autocase_body,$auto_sendmail_cclist);
	}

	
	$i++;
    }#else{ //update simplex Task ti verified_sw
	#$HOST_URL = "http://10.92.35.176/pms";
	#$UPLOAD_URL = $HOST_URL."/project/xrinfo/0/updateProductTaskToVerifiedSW";
	#$post_data = array('taskNo' => $defect,'patchComments' => $patch_comment,'testComments' => $arr_test_suggestion,'email' =>$author_email );
	#$post_data = json_encode($post_data);
	#$post_data = array('jsonContent' => $post_data);
	#$result=upload_exception_to_simplex($UPLOAD_URL, $post_data);
 
    #}       
}
*/
/* add note email eduse files by xiaoying.huang 2018-11-19*/

if(check_file_note_mail_list($git_path_name,$arr_add_file)=='Y'){
	$select_file_list = Get_select_file_list($arr_add_file);
	#print_r($select_file_list);
    $arr_chpatch_recipients = array("jin.yan@tcl.com","yuduan.xie@tcl.com");
	#$arr_chpatch_recipients = array("xiaoying.huang@tcl.com");
	$chpatch_subject="[Patch Change remind]"."[$repo_branch] Patch delivered to Gerrit";
	$changeId=getChangeId($author_name,$git_path_name,$server);
	$chpatch_body ="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
	</head>  
	<body>  
	<span <b>"."Dear $author_email".":</b></span><br/><br/>";
	$chpatch_body .="<span><b>you already patch delivered to gerrit.</b></span><br/>
	<span style='color:green'><b>Branch:</b></span><br/>
	<span><b>$repo_branch</b></span><br/>
	<span style='color:green'><b>Git name:</b></span><br/>
	<span><b>$git_path_name</b></span><br/>
	<span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
	<a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
	<span style='color:red'> <b>patch delivered to Gerrit,Pls Notice:</b></span><br/>";   
    for($i=0;$i<count($select_file_list);$i++){
          $note_list="<span>"."$select_file_list[$i]"."</span><br/><br/>";
          $chpatch_body .= $note_list;
    }
    $chpatch_body .= "
	</body>  
	</html>";
	sendEmail($arr_chpatch_recipients,$chpatch_subject,$chpatch_body);
	print "\n----------Change patch sendEmail to remind efuce ok----------\n\n";
 }
/***end note email eduse files by xiaoying.huang 2018-11-19******/

/* add note email qcom check manifest modified by xiaoying.huang 2019-01-25*/
if($send_qcom_flag){
	$select_file_list = Get_select_file_list($arr_add_file);
    $arr_chpatch_recipients = array("yong.zhang.hz@tcl.com","lei.shi.hz@tcl.com",$author_email);
	#$arr_chpatch_recipients = array("xiaoying.huang@tcl.com");
	$chpatch_subject="[Patch Change remind]"."[$repo_branch] Patch delivered to Gerrit";
	$changeId=getChangeId($author_name,$git_path_name,$server);
	$chpatch_body ="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
	</head>  
	<body>  
	<span <b>"."Dear $author_email".":</b></span><br/><br/>";
	$chpatch_body .="<span><b>you already patch delivered to gerrit.</b></span><br/>
	<span style='color:green'><b>Branch:</b></span><br/>
	<span><b>$repo_branch</b></span><br/>
	<span style='color:green'><b>Git name:</b></span><br/>
	<span><b>$git_path_name</b></span><br/>
	<span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
	<a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
	<span style='color:red'> <b>patch delivered to Gerrit,Pls Notice:</b></span><br/>";   
    for($i=0;$i<count($select_file_list);$i++){
          $note_list="<span>"."$select_file_list[$i]"."</span><br/><br/>";
          $chpatch_body .= $note_list;
    }
    $chpatch_body .= "
	</body>  
	</html>";
	sendEmail($arr_chpatch_recipients,$chpatch_subject,$chpatch_body);
	print "\n----------Change patch sendEmail to remind SPM ok----------\n\n";
 }
/***end note email qcom check manifest modified by xiaoying.huang 2019-01-25******/

/***start by xiaoying.huang 20190523 rom 3.0Q add file  ****/
if($addfile_flag && $product_name == 'TCTROM-Q-V3.0-dev'){
    $rompatch_recipients = array("xiaoying.huang@tcl.com","shie.zhao@tcl.com");
	#$rompatch_recipients = array("xiaoying.huang@tcl.com");
	$chpatch_subject="[ROM Patch Add remind]"."[$repo_branch] Patch delivered to Gerrit";
	$chpatch_body ="<html><head>
	<meta http-equiv='Content-Language' content='zh-cn'>  
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
	</head>  
	<body>  
	<span <b>"."Dear $author_email".":</b></span><br/><br/>";
	$chpatch_body .="<span><b>Somebody already add file to TCTROM-Q-V3.0-dev branch.</b></span><br/>
	<span style='color:green'><b>Branch:</b></span><br/>
	<span><b>$repo_branch</b></span><br/>
	<span style='color:green'><b>Git name:</b></span><br/>
	<span><b>$git_path_name</b></span><br/>
	<span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
	<span style='color:red'> <b>patch delivered to Gerrit,Pls Notice:</b></span><br/>"; 
	$keys2 = array_keys($arr_add_file);
    $current_path=exec('pwd');
    $file_gitpath=substr($current_path,strlen($Repo_path)+1);
    /***
    for($i=0;$i<count($keys2);$i++){
         $tmparray = explode("select_add",$arr_add_file[$keys[$i]]);
         print_r($tmparray);
         if(count($tmparray)>1 && empty($tmparray[0])){
			    $note_list="<span>"."$git_path."/".$keys2[$i];"."</span><br/><br/>";
                $chpatch_body .= $note_list;  
         }
     }***/
     foreach($arr_add_file as $key=>$value){		     
 	      if ($value == "select_add"){
		    	#$note_list="<span>"."$git_path."/".$key"."</span><br/><br/>";
		    	$note_list="<span>"."$file_gitpath"."/"."$key"."</span><br/><br/>";
                $chpatch_body .= $note_list; 
                #print "body==$note_list====" ;
	      }
	  }
    $chpatch_body .= "
	</body>  
	</html>";
	sendEmail($rompatch_recipients,$chpatch_subject,$chpatch_body);
	print "\n----------Change patch sendEmail to remind SPM ok----------\n\n";
 }

/***end by xiaoying.huang 20190523 rom 3.0Q add file  ****/

/***end by xiaoying.huang 20190523 rom 3.0Q add file  ****/



/***start by renzhi.yang.hz 20190923 for keymaster ta verify add file  ****/
foreach($arr_add_file as $key=>$value){
   if (strpos($key,'tlTeeKeymaster.axf' )){
        $keymaster_remind=true;
	break;}}
if($keymaster_remind){	
    $keymasterpatch_recipients = array("haihui.jiang.hz@tcl.com","yan.jin@tcl.com");
    #keymasterpatch_recipients = array("haihui.jiang.hz@tcl.com","yan.jin@tcl.com");
    $chpatch_subject="[Mini keymaster Patch Add remind]"."[$repo_branch] Patch delivered to Gerrit";
    $chpatch_body ="<html><head>
    <meta http-equiv='Content-Language' content='zh-cn'>  
    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>  
    </head>  
    <body>  
    <span <b>"."Dear $author_email".":</b></span><br/><br/>";
    $chpatch_body .="<span><b>Somebody already changed file tlTeeKeymaster.axf.</b></span><br/>
    <span style='color:green'><b>Branch:</b></span><br/>
    <span><b>$repo_branch</b></span><br/>
    <span style='color:green'><b>Git name:</b></span><br/>
    <span><b>$git_path_name</b></span><br/>
    <span style='color:green'><b>Change Info on Gerrit:</b></span><br/>
    <a href='http://$server:8081/#/c/$changeId/'>http://$server:8081/#/c/$changeId/</a><br/><br/>
    <span style='color:red'> <b>patch delivered to Gerrit,Pls Notice:</b></span><br/>"; 
    $keys2 = array_keys($arr_add_file);
    $current_path=exec('pwd');
    $file_gitpath=substr($current_path,strlen($Repo_path)+1);
     foreach($arr_add_file as $key=>$value){ 
	        $note_list="<span>"."$git_path."/".$key"."</span><br/><br/>";
		$note_list="<span>"."$file_gitpath"."/"."$key"."</span><br/><br/>";
                $chpatch_body .= $note_list; 
                print "body==$note_list====" ;
	      }	  
    $chpatch_body .= "
	</body>  
	</html>";
	sendEmail($keymasterpatch_recipients,$chpatch_subject,$chpatch_body);
	print "\n----------Change keymaster patch sendEmail to remind owner ok----------\n\n";
 }

/***end by renzhi.yang.hz 20190923 for keymaster ta verify add file  ****/


# begin add by xiaoying.huang check rom file 20190428
check_file_about_rom_submit($arr_add_file,$product_name);
# end add by xiaoying.huang check rom file 20190428

//add by xiaoying.huang 20180202 for add remind other branch send

if($cread == "ok"){
	 print "\n-------------------------------------\n
	 -----------------------------------------------\n
	 Please go to git push this patch to other project now !!\n
	 -----------------------------------------------\n
	 -----------------------------------------------\n";
}
}//add { by renzhi.yang.hz 2018-11-28	 
fclose($stdin);
fclose($stdout);   	

//end by xiaoying.huang

?>

















