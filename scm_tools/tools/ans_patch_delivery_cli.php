#!/usr/bin/php -q 
<?php

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
$stdin=fopen('php://stdin','r');
$stdout = fopen('php://stdout','w');
$bug_root_casue_file = "../conf/bugzilla/list_oo_SW_cf_cause";
// $prj_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/config";
$prj_file = "ansconfig";

// $arr_root_cause = array('Unknown_Today', 'Architecture', 'Specification', 'Design', 'Coding','Regression','Evolution');
//begin add by zhaoshie for sdd1 prsm 20150119
// $arr_bug_reason_list = array('','Platform','Android','3rd Party','T2M','UE Design','SW Code','Perso Tool Chain','Translation','Customization','HW Related Implementation','New Requirement','UE Improvement','HW Design','Others','Not Reproduced','Not A Bug','Direct Duplicated','Deep Duplicated','Parameters','PC Tools','FOTA Tools Server Chain','Non GApp Scope','Non Isolation GApp','UE Implementation');
$arr_prj_remote = array();
$arr_prj_config = array();
$product_sdd1_tag = 0 ;
$bug_reason_number = 0 ;
$product_config = "Bugzilla";
//end add by zhaoshie for sdd1 prsm 20150119
//add by junbiao.chen 20141031
$need_commit_contents = array();
$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));
//end add by junbiao.chen 20141031
//add by junbiao.chen 20141114
$need_commit_contents_all = array();
$fota_config_name="";
$customization_config_name="";
// $fota_sendmail_tolist=array('tlspm.swd2.hz@tcl.com');
// $customization_sendmail_tolist=array('shuzhong.cui.hz@tcl.com','xiaobo.qiu@tcl.com','ying.chen.hz@tcl.com','yu.he.hz@tcl.com','ting.liu.hz@tcl.com','ruping.pan.hz@tcl.com','junbiao.chen@tcl.com','xia.zhang@tcl.com','yufei.qin@tcl.com');
// $customization_sendmail_cclist=array('jie.cao@tcl.com','can.song.hz@tcl.com','huan.liu@tcl.com','huixing.wang@tcl.com','jianlei.zhang.hz@tcl.com','juan.zhu.hz@tcl.com','susheng.ye.hz@tcl.com','huilian.liu@tcl.com','yao.lu@tcl.com','ming.chen.hz@tcl.com','baoge.li.hz@tcl.com');
// $stringRes_sendmail_tolist=array('shuzhong.cui.hz@tcl.com','yufei.qin@tcl.com','ruping.pan.hz@tcl.com','ting.liu.hz@tcl.com','xia.zhang@tcl.com','huixin.wang@tcl.com','huan.liu@tcl.com','huilian.liu@tcl.com','susheng.ye.hz@tcl.com','ming.chen.hz@tcl.com','yao.lu@tcl.com','baoge.li.hz@tcl.com');
//add by junbiao.chen 20150122
// $framework_sendmail_tolist=array('can.song.hz@tcl.com','tlspm.swd2.hz@tcl.com');
//end by junbiao.chen 20141114
//add by zhaoshie 20150202 
// $sdd1_framework_sendmail_tolist=array('jinhan.li@tcl.com','hanwu.xie@tcl.com','yunqing.huang@tcl.com','zhiquan.wen.hz@tcl.com','zhixiong.liu.hz@tcl.com','zongmin.lu@tcl.com');
//end

//modify by ruifeng.dong for ALM 20141205 begin
#$arr_bug_category = array('MTK platform issue', 'Android platform issue', '3rd party issue', 'JRD bug');
// $arr_bug_category = array('Platform', 'Android', '3rd Party', 'TCT','T2M');//modified by junbiao.chen 20150407
#$arr_jrd_bug = array('Ergo definition defect', 'GUI design defect', 'SW code defect', 'Perso Tool chain', 'Translation', 'Country Perso Issue', 'HW related issue', 'New Requirement', 'Others');
// $arr_jrd_bug = array('UE Implementation', 'UE Design', 'SW Code', 'Perso Tool Chain', 'Translation', 'Customization', 'HW Desgin', 'HW Related Implementation', 'New Requirement','UE Improvement','Not Reproduced','Not A Bug','Duplicated','Parameters','PC Tools','FOTA Tools Server Chain','Non GApp Scope','Non Isolation GApp', 'Others');//modified by junbiao.chen 20150407
//modify by ruifeng.dong for ALM 20141205 end



/** read lines message from stdin, the mothed user for comments, 
    you input '.' expression input end*/
function read_lines($stopchar){
    $lines = array();
    do
    {
        $lines[] = read_line();
    }
    while ($lines[count($lines)-1] != $stopchar);
    
    return $lines;
}


/** read line message from stdin */
function read_line(){
    global $stdin;
    $strinput = "";
    $strinput=trim(fgets($stdin,4096));
    return $strinput;
}


/***
* pwd is a git 
* added by Deng JianBo 2013-01-08
* add pattern for .git and .gitignore
***/
function is_git_root(){
    $git_name = exec("ls -al --time-style=long-iso | grep '.git$' | awk '{print  $8}'");
    if($git_name == ".git"){
    return true;
    }else{
    return false;
    }
}


function get_projects($arg_path,$prj_file){
    $arr_prj = array();

    $arr_root_path = array();
    $arr_root_path = preg_split('/scm_tools\//', $arg_path);

    if(count($arr_root_path) != 0){
        $prj_list_file_path = $arr_root_path[0]."scm_tools/conf/".$prj_file;
        }
        $handle = fopen("$prj_list_file_path","r");
        while(!feof($handle)){
                $readline = fgets($handle,2096);
                $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
                if(empty($readline)){continue;}
                else{
                    $readline = preg_split("/[\s]+/",$readline);
                    $arr_prj[] = $readline[0];
                }
        }
        fclose($handle);
        return $arr_prj;
}


/***
* get project name and return, for example /local/tools/scm_tools/conf/projects/brandy.xml
* add by Deng JianBo 2012-01-03
***/
function get_project_name($arg_path, $product_name){
    $str_project_file = "";
    $arr_root_path = array();
    $arr_root_path = preg_split('/scm_tools\//', $arg_path);
    if(count($arr_root_path) != 0){
    $str_project_file = $arr_root_path[0]."scm_tools/conf/projects/".$product_name.".xml";
    }
    return $str_project_file;        
}


/***
* get project git name and ref
* add by Deng JianBo 2012-01-03
****/
function get_project_name_branch($str_project_file){
    $name_branch = array();  
    $dom = new DOMDocument('1.0', 'UTF-8');  
    $dom->load($str_project_file);  
  
    //get <project> dom 
    $projects = $dom->getElementsByTagName("project");  
    foreach ($projects as $project)  
    {  
        $name = $project->getElementsByTagName("name")->item(0)->nodeValue;  
        $branch = $project->getElementsByTagName("branch")->item(0)->nodeValue;
        $branch = substr($branch,11);
        if (array_key_exists($name,$name_branch) && $name_branch[$name] != ""){
            $name_branch[$name] = $name_branch[$name]."###$branch";
        }else{
            $name_branch[$name] = $branch;  
        }
    }  
    //out result
    #var_dump($name_branch);  
    return  $name_branch;
     
} 


/***
*get author name
*add by Deng JianBo 2013-01-03
***/ 
function get_author_name(){
    $user_name = exec("git config --list | grep user.name | sed -e 's/.*=//'");
    return $user_name;
}


/***
*get author email
*add by ruifeng.dong 2014-10-09
***/ 
function get_author_email(){
    $user_email = exec("git config --list | grep user.email | sed -e 's/.*=//'");
    return $user_email;
}



/***
* get git name
* add by Deng JianBo 2013-01-03
***/
function get_git_name(){
    $path_name = exec("git remote -v | tail -1 | awk -F' ' '{print $2}' | sed -e 's/.*://' -e 's/.git//' -e 's/\//.?/g'");
    return $path_name;
}


#add by shuzhong.cui
function down_repo_branch(){
    $branch = " ";
    $branch = exec("git branch -a | grep '\->' | sed -e 's/.*origin.//'"); 
    return $branch;
}


#add for clone git repository by shuzhong.cui
function clone_git_remote_branch(){  #usage:git checkout remotes/origin/branchname
    $checkout_remote_branch = " ";
    $file = ".git/logs/HEAD";
    if (file_exists($file)){
        $checkout_remote_branch = exec("cat .git/logs/HEAD | grep -E 'remotes/jgs|remotes/origin' | tail -1 | sed -e 's/.*\///'");
        return $checkout_remote_branch;
    }    
}

function clone_git_local_branch(){ #usage:git checkout branchname
    $checkout_branch = " ";  
    $file = ".git/HEAD";
    if (file_exists($file)){
        $checkout_branch = exec("cat .git/HEAD | grep 'ref: refs/heads' | sed -e 's/.*\///'");
        return $checkout_branch;
   }        
}

/***
* get git add file list
* add by Deng JianBo 2013-01-03
* update for add file and renamed file by Deng JianBo 2013-03-01
* update for compatible with different git versions by sdd1  luo xiaoli 2013-12-31
***/
function get_add_array(){
    $arr_add_file = array();
   /* system("git status -uno | awk  '/modified/' | awk '{print  $3}' >>/tmp/result_modified.git");
    system("git status -uno | awk  '/deleted/' | awk '{print  $3}' >>/tmp/result_deleted.git");
    system("git status -uno | awk  '/new file/' | awk '{print  $4}' >>/tmp/result_add.git");
    system("git status -uall | sed -n '/Untracked files:/,/to commit/p' | sed -n '4,/to commit/p' | awk '/#/' | awk '{print $2}' >>/tmp/result_add.git");
*/
    system("git ls-files -m | awk '{print  $1}' >>/tmp/result_modified.git");  
    system("git status -s | awk  '/M /' | awk '{print  $2}' >>/tmp/result_modified.git");
    system("git ls-files -d | awk '{print  $1}' >>/tmp/result_deleted.git");
    system("git status -s |awk '/A /' | awk '{print  $2}' >>/tmp/result_add.git");
    system("git ls-files -o --exclude-standard | awk '{print  $1}' >>/tmp/result_add.git");

    system("git status -uno | awk '/renamed/' | awk '{print  $3\" \"$4\" \"$5}' >>/tmp/result_renamed.git");
    $obj_file_modified = fopen("/tmp/result_modified.git", "r");
    if ($obj_file_modified) {
        while (!feof($obj_file_modified)) {
        $str_tmp = trim(fgets($obj_file_modified, 2048));
        if($str_tmp != ""){
                $arr_add_file[$str_tmp]  = "unselect_modified";
        }
        }
        fclose($obj_file_modified);
    }
    $obj_file_deleted = fopen("/tmp/result_deleted.git", "r");
    if ($obj_file_deleted) {
        while (!feof($obj_file_deleted)) {
        $str_tmp = trim(fgets($obj_file_deleted, 2048));
        if($str_tmp != ""){
                $arr_add_file[$str_tmp]  = "unselect_deleted";
        }
        }
        fclose($obj_file_deleted);
    }
    $obj_file_add = fopen("/tmp/result_add.git", "r");
    if ($obj_file_add) {
        while (!feof($obj_file_add)) {
        $str_tmp = trim(fgets($obj_file_add, 2048));
        if($str_tmp != ""){
                $arr_add_file[$str_tmp]  = "unselect_add";
        }
        }
        fclose($obj_file_add);
    }
    $obj_file_renamed = fopen("/tmp/result_renamed.git", "r");
    if ($obj_file_renamed) {
        while (!feof($obj_file_renamed)) {
        $str_tmp = trim(fgets($obj_file_renamed, 2048));
        if($str_tmp != ""){
                $arr_add_file[$str_tmp]  = "unselect_renamed";
        }
        }
        fclose($obj_file_renamed);
    }  
   
    //delete tmp file for next push code
    system("rm -r /tmp/result_modified.git");
    system("rm -r /tmp/result_deleted.git");
    system("rm -r /tmp/result_add.git");
    system("rm -r /tmp/result_renamed.git");
    return $arr_add_file;
}


/***
* show add file
* add by Deng JianBo 2012-01-03
* add renamed for abandon step by Deng JianBo 2013-03-01
***/
function show_add_array($arr_add_file,$flag){
    $keys = array();
    $keys = array_keys($arr_add_file);
   #var_dump($arr_add_file);
    for($i=0;$i<count($keys);$i++){
    $index = $i+1;
    if($flag == true){        
            if($arr_add_file[$keys[$i]] == "select_deleted"){
            print "\033[32;1m    $index-deleted : $keys[$i]\033[0m\n";
            }
        if($arr_add_file[$keys[$i]] == "select_add"){
            print "\033[32;1m    $index-added   : $keys[$i]\033[0m\n";
        }
        if($arr_add_file[$keys[$i]] == "select_modified"){        
            print "\033[32;1m    $index-modified: $keys[$i]\033[0m\n";
        }
        if($arr_add_file[$keys[$i]] == "select_renamed"){
            print "\033[32;1m   $index-renamed : $keys[$i]\033[0m\n";
        }
        }else{
        #$index = $i+1;
        if($arr_add_file[$keys[$i]] == "unselect_deleted"){
            print "\033[31;1m    $index-deleted : $keys[$i] \033[0m\n";
            }
        if($arr_add_file[$keys[$i]] == "unselect_add"){
            print "\033[31;1m    $index-added   : $keys[$i] \033[0m\n";
        }
        if($arr_add_file[$keys[$i]] == "unselect_modified"){
            print "\033[31;1m    $index-modified: $keys[$i] \033[0m\n";
        }
        if($arr_add_file[$keys[$i]] == "unselect_renamed"){
            print "\033[31;1m    $index-renamed : $keys[$i] \033[0m\n";
        }
        }
    }
}


/***
* is select add file 
* add by Deng JianBo 2012-01-04
***/
function is_have_add_file($arr_add_file){
    $keys = array();
    $keys = array_keys($arr_add_file);
    $flag = false;
    for($i=0;$i<count($keys);$i++){    
    if($arr_add_file[$keys[$i]] == "select_deleted" || $arr_add_file[$keys[$i]] == "select_add" || $arr_add_file[$keys[$i]] == "select_modified" || $arr_add_file[$keys[$i]] == "select_renamed"){
        $flag = true;
        break;
    }
    }
    return $flag;
}


function is_correct_bugs_input($strbugnumber){
    $is_correct = true;
    //updated by Deng JianBo 2013-01-03 for bugnumber
    if(trim($strbugnumber) == ""){
		$is_correct = false;
    }else{
		$bugs = preg_split('/,/', $strbugnumber);
        #print_r($bugs);
        foreach($bugs as $bug){
            if (is_numeric(trim($bug))){
                $is_correct = true;
            }else{
                $is_correct = false;       
            }
        }
    }
    return $is_correct;
}


/***
* get push branch for select
* added by Deng JianBo 2013-01-17
***/
function get_push_branch($push_branch){
    $result_push_branch = "";
    $arr_branch = array();
    $arr_branch = preg_split('/###/',$push_branch);
    #var_dump($arr_branch);
    if(count($arr_branch) > 1){
		$index = 0;
		do{
			writeline("git more one branch, plsease select branch for you update:\n");
			for($i=0;$i<count($arr_branch);$i++){
			    $index = $i + 1;
			    print "    $index: $arr_branch[$i] \n";
			}
			writeline("Your choise:");
			$prj_number = read_line();
	    }while($prj_number > $index || $prj_number < 1);
		$j = $prj_number - 1;
		$result_push_branch = $arr_branch[$j];
	}else{
		$result_push_branch = $arr_branch[0];
    }
    return $result_push_branch;
}


/** out put stdout, message from code out*/
function writeline( $str = '' ){
    global $stdout;
    fputs( $stdout , $str , strlen($str));
}


/***
* get add file str
* added by Deng JianBo 2013-01-04
* add reset HEAD for unselect by Deng JianBo 2013-03-01
***/
function get_add_file_str($arr_add_file){
    $keys = array();
    $keys = array_keys($arr_add_file);
    for($i=0;$i<count($keys);$i++){
        if($arr_add_file[$keys[$i]] == "select_deleted"){
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 begin
		    $filename = $keys[$i];
		    $filename = str_ireplace("(","\(",$filename);
		    $filename = str_ireplace(")","\)",$filename);
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 end
		    exec("git rm -rf $filename");	    
	    }
		if($arr_add_file[$keys[$i]] == "select_add" || $arr_add_file[$keys[$i]] == "select_modified" || $arr_add_file[$keys[$i]] == "select_renamed"){
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 begin
		    $filename = $keys[$i];
		    $filename = str_ireplace("(","\(",$filename);
		    $filename = str_ireplace(")","\)",$filename);
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 end
		    exec("git add $filename");	    
	    }
		if($arr_add_file[$keys[$i]] == "unselect_deleted" || $arr_add_file[$keys[$i]] == "unselect_add" || $arr_add_file[$keys[$i]] == "unselect_modified" || $arr_add_file[$keys[$i]] == "unselect_renamed"){
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 begin
		    $filename = $keys[$i];
		    $filename = str_ireplace("(","\(",$filename);
		    $filename = str_ireplace(")","\)",$filename);
		    //add by ruifeng for modem's makefile JHZ6735M_65C_L(LWG_DSDS)_EXT.mak 20150507 end
		    exec("git reset HEAD $filename");
		}
    }
}

/*****
* main procedure
*****/
/*** added by Deng JianBo ***/
if (!is_git_root()){
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
}

/*** get commit files ***/
$strcommitfiles="";
if ($argc > 4){
    for($i=4; $i<$argc;$i++){
        $strcommitfiles = $strcommitfiles." ".$argv[$i];
    }
}

$product_name = $argv[2];

/*** get the projects ***/
$prjoects = get_projects($argv[0],$prj_file);

/*** check for results ***/
if(sizeof($prjoects) == 0){
    echo "No projects found at $link\n";
    exit;
}

//judge if the project name input by users is right or not 
$is_prj_name = false;
foreach($prjoects as $key=>$value){
     if ($product_name == $value){
        $is_prj_name = true;
     }
}

// step1: select project name 
$input = true;
if ($is_prj_name == false){
    writeline("Wrong product name,\n".
              "Below is the product list, please chose one:\n");
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
}

/***added by jianbo.deng 2013-01-09 ***/
$str_project_file = get_project_name($argv[0], $product_name);
// $alps = exec("echo $str_project_file | grep -Ec 'beetlelite|pixo[^0-9]'");

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
                if ($value == "unselect_modified" || $value == "unselect_deleted" || $value == "unselect_add" || $value == "unselect_renamed"){
                    $input = true;
                }
            }
        }
    }while($input);
}else{
    print "error: nothing update for this git.\n";
    exit;
}

$strbugnumber='';
$input = true;
do{
    if($input == false){
        writeline("wrong bug number,\n".
		  " 1) If there is no bugnumber,this tools didn't work.\n".
                  " 2) If there are several bugs,please separate them by \",\"\n");
    }
    writeline("Bug Number:");
    $strbugnumber = read_line();
    $input = false;
}
while(!is_correct_bugs_input($strbugnumber));

$patch_comment = '';
do{
    writeline("patch comments:");
    $arrcomments = read_lines(".");    
    foreach ($arrcomments as $comment){
        if($comment != ".")
        $patch_comment .= $comment;
    }
    if ($patch_comment == '' ){
        writeline("patch comments is None, Please input again!\n");    
    }
    print "+++++++++++++$patch_comment";
}while($patch_comment == "");

$comments = "###%%%comment:".$patch_comment."\n";
$comments .= "###%%%bug number:".$strbugnumber."\n";
$comments .= "###%%%product name:".$git_path_name."\n";
$comments .= "###%%%author email:".$author_email."\n";

//step5: push code to gerrit web added by Deng JianBo 2013-01-03
$git_push = "";
do{
    $flag=true;
    $result_git_path_name = get_push_branch($git_branch_name);
    $git_push = "git push ssh://".$author_name."@10.92.32.10:29418/".$git_path_name." HEAD:refs/for/".$result_git_path_name;
    print("push command: ".$git_push."\n");
    writeline("please check command (yes/no):");
    $cause_number = read_line();
    if($cause_number == "yes"){
    	$flag = false;
    }
    else if ($cause_number == "no"){
       exit;
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
	if($item != ""){
		$temp.="###%%%".$item."\n";
		$comments= trim($temp);
	}    
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

system($git_push);
//add by junbiao.chen 20141217
