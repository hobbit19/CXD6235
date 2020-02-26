<?php

require("class.phpmailer.php"); //add by shuangyan.he 20160623

//modify by ruifeng.dong for ALM 20141205 begin
function get_projects($prj_link){
        global $arr_prj_remote;
        global $arr_prj_config;
        $arr_prj = array();
        $handle = fopen("$prj_link","r");
        while(!feof($handle)){

                $readline = fgets($handle,2096);		
                $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
                if(empty($readline)){continue;}
                else
                {
                        $readline = preg_split("/[\s]+/",$readline);
                        $arr_prj[] = $readline[0]; 
                         //add by zhaoshie for sdd1 PRSM 
                        $arr_prj_remote[] = $readline[1];
                        $arr_prj_config[] = $readline[2];
                       
                }
        }
        fclose($handle);
        return $arr_prj;
}
//modify by ruifeng.dong for ALM 20141205 end

//modify by yinfang.lai for ALM branch 2015-8-21 begin
function get_almbranch($almconfig_link,$product_name){
        $arr_branch = array();
        $handle = fopen("$almconfig_link","r");
        while(!feof($handle)){
                $readline = fgets($handle,2096);
                $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
                $readline = preg_replace("/(\")/m","",$readline);
                if(empty($readline)){continue;}
                else
                {
                        $readline = preg_split("/,/",$readline);
			if ($readline[0] != $product_name) {continue;}
			else
			{
				$arr_branch = $readline;
				break;
			}		
                }
        }
        fclose($handle);
        return $arr_branch;
}
//modify by yinfang.lai for ALM branch 2015-8-21 end

/** read line message from stdin */
function read_line(){
    global $stdin;
    $strinput = "";
    $strinput=trim(fgets($stdin,4096));
    return $strinput;
}

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

/** out put stdout, message from code out*/
function writeline( $str = '' ){
    global $stdout;
    fputs( $stdout , $str , strlen($str));
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

function get_bug_root_cause(){
    global $bug_root_casue_file;
    $arr_cause = array();
    
    $handle = fopen($bug_root_casue_file, "r");
    if ($handle) {
        while (!feof($handle)) {
            $arr_cause[]  = fgets($handle, 2048);
        }
        fclose($handle);
    }
    #print_r($arr_cause);    
    return $arr_cause;
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
    system("git status ./ -s | awk  '/M /' | awk '{print  $2}' >>/tmp/result_modified.git");
    system("git ls-files -d | awk '{print  $1}' >>/tmp/result_deleted.git");
    system("git status ./ -s |awk '/A /' | awk '{print  $2}' >>/tmp/result_add.git");
    system("git ls-files -o --exclude-standard | awk '{print  $1}' >>/tmp/result_add.git");

    system("git status ./ -uno | awk '/renamed/' | awk '{print  $3\" \"$4\" \"$5}' >>/tmp/result_renamed.git");
    #add by zhaoshie 20160627 for forbid unmerged file upload
    system("git ls-files -u | awk '{print $4}' >>/tmp/result_unmerged.git");  
    system("git status ./ -s | awk  '/UU /' | awk '{print  $2}' >>/tmp/result_unmerged.git");
    #end

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
    #add by zhaoshie 20160627 for forbid unmerged file upload
    $obj_file_unmerged = fopen("/tmp/result_unmerged.git", "r");
    if ($obj_file_unmerged) {
        while (!feof($obj_file_unmerged)) {
	    $str_tmp = trim(fgets($obj_file_unmerged, 2048));
	    if($str_tmp != ""){
            	$arr_add_file[$str_tmp]  = "unselect_unmerged";
	    }
        }
        fclose($obj_file_unmerged);
    }
    #end
    //delete tmp file for next push code
    system("rm -r /tmp/result_modified.git");
    system("rm -r /tmp/result_deleted.git");
    system("rm -r /tmp/result_add.git");
    system("rm -r /tmp/result_renamed.git");
    system("rm -r /tmp/result_unmerged.git");
    return $arr_add_file;
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
	    if($arr_add_file[$keys[$i]] == "select_unmerged"){
	        print "\033[32;1m   $index-unmerged : $keys[$i]\033[0m\n";
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
	    if($arr_add_file[$keys[$i]] == "unselect_unmerged"){
	        print "\033[31;1m    $index-unmerged : $keys[$i] \033[0m\n";
	    }
        }
    }
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
	//modify by lyf transfer $ special char
	$tmp_filename = str_replace("$", "\\$", $keys[$i]);
	$tmp_filename = str_replace("(", "\\(", $tmp_filename);// $tmp_filename replace $keys[$i] by hsy 20160908
	$tmp_filename = str_replace(")", "\\)", $tmp_filename);// $tmp_filename replace $keys[$i] by hsy 20160908
        if($arr_add_file[$keys[$i]] == "select_deleted"){
	    exec("git rm -rf $tmp_filename");	    
        }
	if($arr_add_file[$keys[$i]] == "select_add" || $arr_add_file[$keys[$i]] == "select_modified" || $arr_add_file[$keys[$i]] == "select_renamed"){
	    exec("git add $tmp_filename");
        }
	if($arr_add_file[$keys[$i]] == "unselect_deleted" || $arr_add_file[$keys[$i]] == "unselect_add" || $arr_add_file[$keys[$i]] == "unselect_modified" || $arr_add_file[$keys[$i]] == "unselect_renamed"){
	    exec("git reset HEAD $tmp_filename");
	}
	//end modify by lyf 
    }
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

/***
* select all update will return true
* added by Deng JianBo 2013-01-17
***/
function is_select_all_update(){

}

/***
* judge project name is right
* added by yinfang.lai 2015-03-10
***/
function is_project_name_right(){
   
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


#add by shuzhong.cui
function down_repo_branch(){
    $branch = " ";
    $branch = exec("git branch -a | grep '\->' | sed -e 's/.*jgs.//'"); 
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


function just_continue($comment){
	writeline("Input Y|N ".$comment);
	$judge_resulte = read_line();
	$judge_resulte = strtolower(trim($judge_resulte));
	while( !(( $judge_resulte =='yes' || $judge_resulte =='y' || $judge_resulte =='no' || $judge_resulte =='n' )) ){
	    writeline("Input Y|N ".$comment);
    	    $judge_resulte= read_line();
    	    $judge_resulte = strtolower(trim($judge_resulte));
   
	}
	$judge_resulte = strtolower(trim($judge_resulte));
	if ( $judge_resulte  == 'yes' || $judge_resulte =='y' ){
	    ;
	}else{
	    exit;
	}
}

//update code from git server.
/*function git_pull(){
    system("git checkout -- . && git clean -df");
    if($repo_branch == $git_branch_name && $remote_branch  == NULL && $clone_branch == NULL){
       $git_conflict = exec("git pull jgs $git_branch_name | git status | grep -c 'both modified:'");  
    }
    if($clone_branch == $git_branch_name || ($remote_branch == $git_branch_name && $clone_branch == NULL)){ 
       $git_conflict = exec("git pull origin $git_branch_name | git status | grep -c 'both modified:'");       
    }

    if($git_conflict != NULL){
       print "\033[31;1mThere is conflict,pls do with it.\033[0m\n";        
       exit;
    }
} */

// start add by shuangyan.he 20160328
function readtxt($prompt,$prompt1){
	global $txtfile;
	$fill_item = explode($prompt,$txtfile);
	$fill_item = $fill_item[1];	
	$fill_item = explode($prompt1,$fill_item);
	$fill_item = $fill_item[0];
	echo("\033[31;1m\nAffirm your $prompt"."\033[0m");
	echo $fill_item;
	if($prompt=="Module_Impact:"||$prompt=="Solution:"||$prompt=="Test_Report:"){
	$fill_item = explode(PHP_EOL,$fill_item);
	}
	else{
		$fill_item = str_replace(PHP_EOL,' ',$fill_item);
		$fill_item = ifempty($prompt,$fill_item);
	}
	echo "'Enter' to go | 'n' to amend:\n";
	$choice = readline();
	while(!($choice =='no' || $choice =='n' || $choice =='N' || $choice =='NO' || (strlen($choice) == 0))){
		echo "Input 'Enter'|'n'\n";
		$choice = readline();
	}
	if(strlen($choice) >0){
	  	echo "Input $prompt,then 'Enter'+'.':\n";
		$fill_item = read_lines(".");
		array_pop($fill_item);		
		if(!($prompt=="Module_Impact:"||$prompt=="Solution:"||$prompt=="Test_Report:")){
			$fill_item = implode(' ',$fill_item);
			$fill_item = ifempty($prompt,$fill_item);	
		}
	}
	return $fill_item;
}
//判断字符串是否为空
function ifempty($prompt,$fill_item){
	while(ctype_space($fill_item)==1||strlen($fill_item)==0){
		echo "$prompt is None,input please! then 'Enter'+'.'\n";
		$fill_item = read_lines(".");
		array_pop($fill_item);
		$fill_item = implode(' ',$fill_item);
	}
     	return $fill_item;
}
// end add by shuangyan.he 20160328

//add by zhaoshie 20150510 for ImpactAPP
function get_appupdate_from_commit_contents(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    global $index_app;
    foreach($need_commit_contents_all as $each_commit_content){
       $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
       $current_path=exec('pwd');
       $git_path=substr($current_path,strlen($repo_path)+1);
       $commit_app=exec("echo $each_commit_content | grep 'App/'");
       $commit_app_git=exec("echo $git_path | grep '/App'");
       $commit_app_mk=exec("echo $each_commit_content | grep 'zz_thirty_app.mk'");
       if(!empty($commit_app)){
           $arr_commit_apps[$index_app++]="$git_path"."/"."$commit_app";
       }
       else if(!empty($commit_app_git)){
           $arr_commit_apps[$index_app++]="$git_path"."/"."$each_commit_content";
       }
       else if(!empty($commit_app_mk)){
           $arr_commit_apps[$index_app++]="$git_path"."/"."$commit_app_mk";
       }
    }
    if(!empty($arr_commit_apps)){
    	return $arr_commit_apps;
    }
    return null; 
}
//end added by zhaoshie 20150510 for ImpactAPP

//Begain add by shuangyan.he 20160622
function get_mtkpatch_commit_gitpath(){
#function get_appupdate_from_commit_contents(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    global $index_mtkpatch;
    foreach($need_commit_contents_all as $each_commit_content){
       $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
       $current_path=exec('pwd');
       $git_path=substr($current_path,strlen($repo_path)+1);
       $arr_commit_mtkpatchs[$index_mtkpatch++]="$git_path"."/"."$each_commit_content";  
    }
    if(!empty($arr_commit_mtkpatchs)){
    	return $arr_commit_mtkpatchs;
    }
    return null; 
}
//end add by shuangyan.he 20160622

//Begain add by shuangyan.he 20160710
function getidvnum_mtkmerge($pnum,$CodeBranch,$patchtype){
	//$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$dotp_conn);
	$selectsql = "SELECT mp.id, mp.vnum FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' ORDER BY mp.TS DESC LIMIT 0,1";
	//$result = mysql_query($selectsql);
	$result = get_mysqlli_data($selectsql);
	if ($result){
		$vnum="";
		$imid="";
		$dicidvnum=array();
		//while($row=mysql_fetch_array($result)){
		while($row = $result->fetch_assoc()){
			$vnum=$row['vnum'];
			$imid=$row['id'];
			$dicidvnum[$row['vnum']]=$row['id'];
		}
		if(empty($dicidvnum)){
			print "\n--------Pnum should exist mtk_merge--------";
			return False;}
	}
	return $dicidvnum;
}

function update_mtkpatch_mtkmerge($pnum,$CodeBranch,$patchtype,$vnum,$bug_id,$gerrit_link,$owner,$TS){
	//$db_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$db_conn);
	$getidsql = "SELECT mp.id as chooseid,mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm ) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' and mp.vnum='$vnum'";
	//$mmresult = mysql_query($getidsql);
	$mmresult = get_mysqlli_data($getidsql);
	$arrmtk_id=array();
	if ($mmresult){
		//while($rows=mysql_fetch_array($mmresult)){
		while($rows = $mmresult->fetch_assoc()){
			$mtk_id=$rows['id'];
			array_push($arrmtk_id,$mtk_id);
			$bugid_old=$rows['bug_id'];
			$gerrit_link_old=$rows['gerrit_link'];
			$chooseid=$rows['chooseid'];
			if($bugid_old==""){
				$bugid_new = $bug_id;
			}else{
				$arrbugid_old=explode(",",$bugid_old);
				$arrbug_id=explode(",",$bug_id);
				$arrbugid_new=array_merge($arrbugid_old,$arrbug_id);
				$arrbugid_new=array_unique($arrbugid_new);
				$bugid_new = implode(",",$arrbugid_new);
			}
			if($gerrit_link_old==""){
				$gerrit_link_new=$gerrit_link;
			}else{
				$gerrit_link_new="$gerrit_link_old".",\n$gerrit_link";}
			if(empty($mtk_id)){
				print "\n--------mm.id should exist mtk_merge--------\n";
				return False;}
			else{
				print "\nbegin to update bug_id/owner/gerritlink/TS to $CodeBranch,mp.id=$chooseid\n";
				$updatesql = "UPDATE dotp_mtk_merge SET bug_id='$bugid_new', gerrit_link='$gerrit_link_new', owner='$owner', TS='$TS' WHERE import_id='$chooseid' and merge_patch='$CodeBranch'";
				//$result = mysql_query($updatesql);
				$result = get_mysqlli_data($updatesql);
				if (!$result){
					print "\nupdate mtk_merge $CodeBranch failed,Pls contact INT\n";
					return False;
				}else{
					print "\nupdate mtk_merge.db $CodeBranch mm.import_id=$chooseid OK\n";}
			}
		}
	}
	//begain add by shuangyan.he 20160721
	//judge other comon branch by shuangyan.he 20160720
	$gitpath_name = exec("git remote -v | tail -1 | awk -F' ' '{print $2}' | sed -e 's/.*://' -e 's/.git//'");
	$gitpath_name = substr($gitpath_name,9);
	$dicbranchgit = get_mtkcombranch($CodeBranch);
	global $arr_mmid;
	$arr_mmid=array();
	foreach($dicbranchgit as $key=>$value){
		$com_branch=$key;
		$diff_git=$value;		
		if(!strstr($diff_git,$gitpath_name)){
			print "\n[$com_branch] with [$CodeBranch] samecode,update it too.\n";
			$mmid=update_other_combranch($pnum,trim($vnum),$com_branch,$patchtype,$bug_id,$gerrit_link,$owner,$TS);
			array_push($arr_mmid,$mmid);
		}
	}
	//end add by shuangyan.he 20160721
	return $arrmtk_id;	
}
//end add by shuangyan.he 20160710
// begain add by shuangyan.he 20160720
function get_mtkcombranch($CodeBranch){
	$mtkpconfig_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/mtkcombranch";
	//$mtkpconfig_link ="/local/tools/scm_tools/conf/mtkcombranch";
        $handle = fopen("$mtkpconfig_link","r");
        $com_branch = "";
	$diff_git = "";
	$dicbranchgit=array();
        while(!feof($handle)){
                $readline = fgets($handle,2096);
                if(empty($readline)){continue;}
                else{
                        $readline = preg_split("/[\s;]+/",$readline);
			if ($readline[0]!=$CodeBranch) {continue;}
			else{
				$com_branch = $readline[1];
				$diff_git = $readline[2];
				$dicbranchgit[$readline[1]]=$readline[2];
			}		 
                }
        }
        fclose($handle);
        return $dicbranchgit;
}

function update_other_combranch($pnum,$vnum,$com_branch,$patchtype,$bug_id,$gerrit_link,$owner,$TS){
	//$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$dotp_conn);
	$select2sql = "SELECT mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND vnum='$vnum' and mm.merge_patch = '$com_branch' AND mp.patch_type = '$patchtype'";
	//$othresult = mysql_query($select2sql);
	$othresult = get_mysqlli_data($select2sql);
	if ($othresult){
		//$othresult = mysql_fetch_array($othresult);
		$othresult = $othresult->fetch_assoc();
		$mmid=$othresult['id'];
		$bugid_old2=$othresult['bug_id'];
		$gerrit_link_old2=$othresult['gerrit_link'];
		if($bugid_old2==""){
			$bugid_new2 = $bug_id;
		}else{
			$arrbugid_old2=explode(",",$bugid_old2);
			$arrbug_id2=explode(",",$bug_id);
			$arrbugid_new2=array_merge($arrbugid_old2,$arrbug_id2);
			$arrbugid_new2=array_unique($arrbugid_new2);
			$bugid_new2 = implode(",",$arrbugid_new2);
		}
		if($gerrit_link_old2==""){
			$gerrit_link_new2=$gerrit_link;
		}else{
			$gerrit_link_new2="$gerrit_link_old2".",\n$gerrit_link";}
		if(empty($mmid)){
			print "\nmm.id should exist mtk_merge.\n";
			return False;}
		else{
			//update other branch
			print "\nbegin to update bug_id/owner/gerritlink/TS to $com_branch.\n ";
			$update2sql = "UPDATE dotp_mtk_merge SET bug_id='$bugid_new2', gerrit_link='$gerrit_link_new2', owner='$owner', TS='$TS' WHERE id='$mmid' and merge_patch='$com_branch'";
			//$result2 = mysql_query($update2sql);
			$result2 = get_mysqlli_data($update2sql);
			if (!$result2){
				print "\nupdate mtk_merge $com_branch failed,Pls fill it manually or contact INT.\n";
				return False;
			}else{
				print "\nupdate mtk_merge.db $com_branch mm.id=$mmid OK.\n";}
		}
	}
	return $mmid;
}
// end add by shuangyan.he 20160720
//add by junbiao.chen 20141031
function get_apks_from_commit_contents(){
    global $need_commit_contents;
    #$tmp_path="/tmp/";
    #$author_name = get_author_name();
    #if(file_exists($tmp_path."$author_name.commit_apks.txt")){
    #	exec("rm $tmp_path"."$author_name.commit_apks.txt");
    #   }
    #  foreach($need_commit_contents as $each_commit_content){
    #     exec("echo $each_commit_content | grep '.apk'>>".$tmp_path."$author_name.commit_apks.txt");
    #}
    #$arr_commit_apks=file("$tmp_path"."$author_name.commit_apks.txt");
    #exec("rm $tmp_path"."$author_name.commit_apks.txt");
    $index=0;
    foreach($need_commit_contents as $each_commit_content){
    $commit_apk=exec("echo $each_commit_content | grep '.apk'");
    if(!empty($commit_apk)){
       $arr_commit_apks[$index++]=$commit_apk;
    }
    }
    if(!empty($arr_commit_apks)){
    	return $arr_commit_apks;
    }
    return null; 
}
//end added by junbiao.chen 20141031

//update by shie 20160618
function get_fotas_from_commit_contents_all(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    $isfotaprojectfile=false;
    foreach($need_commit_contents_all as $each_commit_content){
	$fota_file_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_file_config";
	$fota_dir_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_dir_config";
	if(file_exists($fota_file_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
	    $git_path=substr($current_path,strlen($repo_path)+1);
            #print "$git_path"."/"."$each_commit_content";
    	    $fota_file_info=exec("cat $fota_file_path_name | grep "."$git_path"."/"."$each_commit_content");
            $handle = fopen("$fota_file_path_name","r");     
            while(!feof($handle)){
                 $readline = '';
                 $readline = fgets($handle,2096);
                 $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                 $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
                 $readline = preg_replace("/(\")/m","",$readline); 
                 $isfotaprojectfile=false;
                 if(empty($readline)){continue;}
                 else{ 
                     $fota_project_lines=exec(" echo $readline | grep '%project' ");
                     if(!empty($fota_project_lines)){
                        $fota_project_line_info=explode('/%project/',$fota_project_lines);
                        $isfotaprojectfile=false;
                        for($i=0;$i<count($fota_project_line_info);$i++){
                           $fota_project_file_info=exec("echo $git_path"."/"."$each_commit_content | grep $fota_project_line_info[$i] ");
                           if(!empty($fota_project_file_info)){
                             $isfotaprojectfile=true; 
                           }else{ 
                             $isfotaprojectfile=false;
                             break;
                           }
                       }
                     }
                 }
                 if($isfotaprojectfile){$arr_commit_fota[$index++]=$fota_project_file_info;}
            }
	}
        if(!empty($fota_file_info)){
            $arr_commit_fota[$index++]=$fota_file_info;		  
        }
	if(file_exists($fota_dir_path_name)){
            $filename = "fota";
            $dirname="impact_fota_sdd1";
            #print "$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname";
	    $fota_dir_info=exec("$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname");
            #print $fota_dir_info;
	}
	if(!empty($fota_dir_info)){
	    $arr_commit_fota[$index++]=$fota_dir_info;
        }		
    }
    if(!empty($arr_commit_fota)){
        return $arr_commit_fota;
    }
    return null;
}

//add by junbiao.chen 20150122
function get_framework_from_commit_contents_all(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    foreach($need_commit_contents_all as $each_commit_content){
	$framework_file_path_name="$patch_delivery_path"."../conf/impact_framework/"."$repo_branch"."_conf/"."$repo_branch.file_conf";
	$framework_dir_path_name="$patch_delivery_path"."../conf/impact_framework/"."$repo_branch"."_conf/"."$repo_branch.dir_conf";
	if(file_exists($framework_file_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
	    $git_path=substr($current_path,strlen($repo_path)+1);
    	    $framework_file_info=exec("cat $patch_delivery_path"."../conf/impact_framework/"."$repo_branch"."_conf/"."$repo_branch.file_conf | grep "."$git_path"."/"."$each_commit_content");
	}
	if(empty($framework_file_info)){
	    if(file_exists($framework_dir_path_name)){
	    	$framework_dir_info=exec("$patch_delivery_path"."find_framework_from_dir.sh $patch_delivery_path $repo_branch $each_commit_content");
	    }
	    if(!empty($framework_dir_info)){
	        $arr_commit_framework[$index++]=$framework_dir_info;
            }	
	}else{
		$arr_commit_framework[$index++]=$framework_file_info;	
	}
    }
    if(!empty($arr_commit_framework)){
        return $arr_commit_framework;
    }
    return null;
}
//end added by junbiao.chen 20150122
//add by junbiao.chen 20141217
function getCurrentCommitId(){
 	return exec("git log -1 | head -n 1 | awk '{print $2}'");
}
function getChangeId(){
	$currentCommitId=getCurrentCommitId();
	$changeIdLine=exec("git ls-remote jgs refs/changes/* | grep '$currentCommitId' | awk '{print $2}'");
	list($refs,$changes,$index,$changeId,$num)=split('[/.-]',$changeIdLine);
	return $changeId;
}
//end add by junbiao.chen 20141217
function sendEmail($tolist,$subject,$body){
        #require_once("class.phpmailer.php");  // by shuangyan.he 20160623
	#require("class.phpmailer.php");   // by shuangyan.he 20160623  
        $mail = new PHPMailer();   
        $mail->IsSMTP();                  // send via SMTP   
        $mail->Host = "mail.tcl.com";   // SMTP servers   
        $mail->SMTPAuth = true;           // turn on SMTP authentication   
        $mail->Username = "hudson.admin.hz";     // SMTP username 
        $mail->Password = "Hzsw#123"; // SMTP password   
        $mail->From = "hudson.admin.hz@tcl.com";   
        $mail->FromName =  "hudson.admin.hz";   
        $mail->CharSet = "utf-8";
	$author_email=get_author_email();
	$mail->AddAddress($author_email,substr($author_email,0,-8));
        foreach($tolist as $to){
		$mail->AddAddress($to,substr($to,0,-8));	
	}
        $mail->AddReplyTo("hudson.admin.hz@tcl.com","hudson.admin.hz");   
        $mail->IsHTML(true);
        $mail->Subject = $subject;     
        $mail->Body = $body; 
	#$mail->SMTPDebug = true;
        if(!$mail->Send()) {
	#echo "Mailer Error: " . $mail->ErrorInfo;
	    ;
	} else {
	#echo "Mail Sent!";
	    ;
	}
}
//end by junbiao.chen 20141115

//add by zhaoshie 20160602
function get_mtkpatch_forbid_from_commit_contents_all(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    foreach($need_commit_contents_all as $each_commit_content){
	$forbid_file_path_name="$patch_delivery_path"."../conf/impact_mtkpatch/"."forbid_file_config";
	$forbid_dir_path_name="$patch_delivery_path"."../conf/impact_mtkpatch/"."forbid_dir_config";
	if(file_exists($forbid_file_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
	    $git_path=substr($current_path,strlen($repo_path)+1);
    	    $forbid_file_info=exec("cat $forbid_file_path_name | grep "."$git_path"."/"."$each_commit_content");           
	}
        if(!empty($forbid_file_info)){
            $arr_commit_forbid[$index++]=$forbid_file_info;		  
        }
	if(file_exists($forbid_dir_path_name)){
            $filename = "forbid";
            $dirname="impact_mtkpatch";
	    $forbid_dir_info=exec("$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname");
            #print $forbid_dir_info;
	}
	if(!empty($forbid_dir_info)){
	    $arr_commit_forbid[$index++]=$forbid_dir_info;
        }		
    }
    if(!empty($arr_commit_forbid)){
        return $arr_commit_forbid;
    }
    return null;
}

function get_mtkpatch_remind_from_commit_contents_all(){
   global $need_commit_contents_all;
   global $patch_delivery_path;
   $repo_branch = down_repo_branch();
   $index=0;
   foreach($need_commit_contents_all as $each_commit_content){
        $mtkprojectname="$patch_delivery_path"."../conf/impact_mtkpatch/"."remind_mtkproject"; 
        if(file_exists($mtkprojectname)){
             $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
             $current_path=exec('pwd');
             $git_path=substr($current_path,strlen($repo_path)+1);
             $arr_mtkprojectname = array();
             $handle = fopen("$mtkprojectname","r");
             while(!feof($handle)){
                 $readline = fgets($handle,2096);
                 $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                 $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
                 $readline = preg_replace("/(\")/m","",$readline); 
                 if(empty($readline)){continue;}
                 else
                 { 
                      #print $readline;
                      $remind_tag = exec("echo $git_path"."/"."$each_commit_content | grep $readline");
                      if($remind_tag){
                         $arr_commit_remind[$index++]=$remind_tag;
                      }
                 }
            }
        }

   }
   if(!empty($arr_commit_remind)){
        return $arr_commit_remind;
   }
   return null;
}
//end
//add by zhaoshie 20161101 for ImpactTEE
function get_teeupdate_from_commit_contents(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    global $index_tee;
    $repo_branch = down_repo_branch();

    foreach($need_commit_contents_all as $each_commit_content){
	$tee_dir_path_name="$patch_delivery_path"."../conf/impact_tee/"."tee_dir_config";
	if(file_exists($tee_dir_path_name)){
            $filename = "tee";
            $dirname="impact_tee";
	    $tee_dir_info=exec("$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname");
	}
	if(!empty($tee_dir_info)){
	    $arr_commit_tee[$index_tee++]=$tee_dir_info;
        }		
    }
    if(!empty($arr_commit_tee)){
        return $arr_commit_tee;
    }
    return null;
}
//end added by zhaoshie 20161101 for ImpactTEE
//add by zhaoshie 20161101 for ImpactPersist
function get_persistupdate_from_commit_contents(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    global $index_persist;
    $repo_branch = down_repo_branch();

    foreach($need_commit_contents_all as $each_commit_content){
	$persist_file_path_name="$patch_delivery_path"."../conf/impact_tee/"."persist_file_config";
	if(file_exists($persist_file_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
	    $git_path=substr($current_path,strlen($repo_path)+1);
    	    $persist_file_info=exec("cat $persist_file_path_name | grep "."$git_path"."/"."$each_commit_content");           
	}
        if(!empty($persist_file_info)){
            $arr_commit_persist[$index_persist++]=$persist_file_info;		  
        }
    }
    if(!empty($arr_commit_persist)){
        return $arr_commit_persist;
    }
    return null;
}
//end added by zhaoshie 20161101 for ImpactPersist


function get_last_mtkpatch_mergestatus($patch_delivery_path,$branch_name,$patch_num,$patchtype)
{
	print $patch_num;
	if ($patch_num <= 0)
		return 0;
	//$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$dotp_conn);
	$selectsql = "SELECT mm.`merge_status`,`bug_id`, mm.id,mm.owner FROM `dotp_mtk_merge` as mm
left join `dotp_mtk_import` as mi on mm.import_id =mi.id WHERE `merge_patch`='$branch_name' and `pnum`='P$patch_num' and patch_type='$patchtype' ";
	//print $selectsql;
	//$result = mysql_query($selectsql);
	$result = get_mysqlli_data($selectsql);
	print "============$patch_num";
	print_r($result);
	if ($result)
	{
		//$result = mysql_fetch_array($result);
		$result = $result->fetch_assoc();
		if (count($result) ==0)
		{
			return 1;#please make sure int have merge to import branch and remarked in smarttask
		}	
		$mm_status=$result['merge_status'];
		$bugs=$result['bug_id'];
		$mm_id=$result['id'];
		$mm_owner=$result['owner'];
		if ($mm_status == "1" || $mm_status == "2")
		//if ($mm_status != '0')
		{
			return 0;
		}
		else
		{
			if ($mm_id == "")
			{
				return 1;
			}
			if ($bugs == "")
			{
				print "\n\033[31;1m---------Last mtk Patch P$patch_num Owner $mm_owner -----------\033[0m  \n";
				return 2;
			}
			$defects = preg_split('/,/', $bugs);
			//print_r($defects);
			foreach($defects as $defect){
			    if ($defect == "")
			    	continue;
			    //echo "$patch_delivery_path"."ALM_check_status.py $branch_name $defect";
			    //print "check last mtkpatch defect $defect status ";
			    $error_flag = exec("$patch_delivery_path"."ALM_check_status.py $branch_name $defect",$out);
			    switch ($error_flag) {
				case 200:
				    print "OK! go next \n";
				    break;
				case 600:
				    print "The defect/task state is not Delivered or Verified_SW,Verified,Closed,Postponed,Deleted , please check! \n";
				    print "\n\033[31;1m---------Last mtk Patch P$patch_num Owner $mm_owner -----------\033[0m  \n";
				    return 2;
				    break;
			    }
			}
			//update lastmtk patch status
			$updatesql = "UPDATE dotp_mtk_merge SET merge_status=1 WHERE merge_patch='$branch_name' and id='$mm_id'";
			//print $updatesql;
			//$result = mysql_query($updatesql);
			$result = get_mysqlli_data($updatesql);
			return 0;
		}
	}else
		return 3;
}

function get_mysqlli_data($sql)
{
	$mysqli=new mysqli('10.92.35.20','INT_PATCH','Aa123456','dotproject'); 
	print $sql;
	$rs = $mysqli->query($sql); 	
	$mysqli->close();
	return $rs;
}
?>
