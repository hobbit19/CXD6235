<?php
require("class.phpmailer.php");  
require_once('conf.inc');

function get_projects($prj_link){
    global $arr_prj_remote;
    global $arr_prj_config;
    $arr_prj = array();
    $handle = fopen("$prj_link","r");
    while(!feof($handle)){
        $readline = fgets($handle,2096);
        $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
        $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
        if(empty($readline)){
	    continue;
        }else{
	    $readline = preg_split("/[\s]+/",$readline);
	    //print_r($readline);
	    if (count($readline)<3)
	    	continue;
            $arr_prj[] = $readline[0]; 
            $arr_prj_remote[] = $readline[1];
            $arr_prj_config[] = $readline[2];        
        }
    }
    fclose($handle);
    return $arr_prj;
}

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

function read_line(){
    global $stdin;
    $strinput = "";
    $strinput=trim(fgets($stdin,4096));
    return $strinput;
}

function writeline( $str = '' ){
    global $stdout;
    fputs( $stdout , $str , strlen($str));
}

function read_lines($stopchar){
    $lines = array();
    do{
        $lines[] = read_line();
    }while($lines[count($lines)-1] != $stopchar);
    return $lines;
}

function is_correct_bugs_input($strbugnumber){
    $is_correct = true;
    if(trim($strbugnumber) == ""){
	$is_correct = false;
    }else{
	$bugs = preg_split('/,/', $strbugnumber);
        foreach($bugs as $bug){
            if(is_numeric(trim($bug))){
                $is_correct = true;
            }else if( (strtolower(trim($bug[0])) == 'p' || strtolower(trim($bug[0])) == 't' || strtolower(trim($bug[0])) == 'e' || strtolower(trim($bug[0])) == 'u') && is_numeric(substr($bug,1,strlen($bug)))){
                $is_correct = true;   
            }else if((strtolower(trim($bug[0])) == 'u') && is_numeric(substr($bug,2,strlen($bug)))){
                $is_correct = true;  }         
            else{
                $is_correct = false;       
            }
        }
    }
    return $is_correct;
}

function is_correct_hours_input($strbugnumber){
    $is_correct = true;
    if(trim($strbugnumber) == ""){
	$is_correct = false;
    }else{
	$bugs = preg_split('/\./', $strbugnumber);
        foreach($bugs as $bug){
            if(is_numeric(trim($bug))){
                $is_correct = true;        
            }else{
                $is_correct = false;       
            }
        }
    }
    return $is_correct;
}

function is_simplex_bugs($strbugnumber){
    $is_correct = false;
    if((strtolower(trim($strbugnumber[0])) == 'p' ||strtolower(trim($strbugnumber[0])) == 't' || strtolower(trim($strbugnumber[0])) == 'e') && is_numeric(substr($strbugnumber,1,strlen($strbugnumber)))){
        $is_correct = true;  
    }
    else if ((strtolower(trim($strbugnumber[0])) == 'u') && is_numeric(substr($strbugnumber,2,strlen($strbugnumber)))) {
        $is_correct = true;    }     
    return $is_correct;
}


function get_bug_root_cause(){
    global $bug_root_casue_file;
    $arr_cause = array();
    $handle = fopen($bug_root_casue_file, "r");
    if($handle){
        while(!feof($handle)){
            $arr_cause[]  = fgets($handle, 2048);
        }
        fclose($handle);
    }
    return $arr_cause;
}

function get_project_name_branch($str_project_file){
    $name_branch = array();  
    $dom = new DOMDocument('1.0', 'UTF-8');  
    $dom->load($str_project_file);  
    $projects = $dom->getElementsByTagName("project");  
    foreach($projects as $project)  
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
    return  $name_branch; 
}

function get_project_name($arg_path, $product_name){
    global $prj_manifest_link;
    $str_project_file = "";
    $arr_root_path = array();
    $arr_root_path = preg_split('/scm_tools\//', $arg_path);
    if(count($arr_root_path) != 0){
	$str_project_file = $arr_root_path[0]."scm_tools/conf/projects/".$product_name.".xml";
    }
    if (! file_exists($str_project_file))
    {
    	$str_project_file = $prj_manifest_link.$product_name.".xml";
    }
    return $str_project_file;    	
}

function get_author_name(){
    $user_name = exec("git config --global --list | grep user.name | sed -e 's/.*=//'");
    return $user_name;
}

function get_author_email(){
    $user_email = exec("git config --global --list | grep user.email | sed -e 's/.*=//'");
    return $user_email;
}

function get_git_name(){
    $path_name = exec("git remote -v | tail -1 | awk -F' ' '{print $2}' | sed -e 's/.*://' -e 's/\.git//' -e 's/\//.?/g'");
    return $path_name;
}

function get_git_name2(){
    $path_name = exec("git config remote.`git remote`.projectname | sed -e  's/\//.?/g'");
    return $path_name;
}

function get_add_array(){
    $arr_add_file = array();

    system("git ls-files -m | awk '{print  $1}' >>/tmp/result_modified.git");  
    system("git status ./ -s | awk  '/M /' | awk '{print  $2}' >>/tmp/result_modified.git");
    system("git ls-files -d | awk '{print  $1}' >>/tmp/result_deleted.git");
    system("git status ./ -s |awk '/A /' | awk '{print  $2}' >>/tmp/result_add.git");
    system("git ls-files -o --exclude-standard | awk '{print  $1}' >>/tmp/result_add.git");
    #update by zhaoshie 20170901 for renamed key error and can audo del file content .
    #system("git status -uno | awk '/renamed/' | awk '{print  $3\" \"$4\" \"$5}' >>/tmp/result_renamed.git");
    system("git status ./ -s | awk  '/R /' | awk '{print  $2}' >>/tmp/result_deleted.git");
    system("git status ./ -s | awk  '/R /' | awk '{print  $4}' >>/tmp/result_add.git");
    #end
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
    /*
    $obj_file_renamed = fopen("/tmp/result_renamed.git", "r");
    if ($obj_file_renamed) {
        while (!feof($obj_file_renamed)) {
	    $str_tmp = trim(fgets($obj_file_renamed, 2048));
	    if($str_tmp != ""){
                $arr_add_file[$str_tmp]  = "unselect_renamed"; 
	    }
        }
        fclose($obj_file_renamed);
    }  */
    //delete tmp file for next push code
    system("rm -r /tmp/result_modified.git");
    system("rm -r /tmp/result_deleted.git");
    system("rm -r /tmp/result_add.git");
    #system("rm -r /tmp/result_renamed.git");
    system("rm -r /tmp/result_unmerged.git");
    return $arr_add_file;  
}

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

function check_upload_type($arr_add_file, $git_name){
    $keys = array();
    $keys = array_keys($arr_add_file);
    $code_flag = false;
    $translate_flag = false;
    $apk_flag = false;
    for($i=0;$i<count($keys);$i++){
        $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
        if(count($tmparray)>1){
            $file_suffix = substr($keys[$i], -4);
            if($file_suffix == ".apk" || preg_match("/all_GMS_app\.mk|custpack_app_list\.mk|googleapk\.mk/", $keys[$i])){
                $apk_flag = true;
            }else if($file_suffix == ".xls" && strpos($git_name,"wlanguage") == true){
                $translate_flag = true;
            }else if($file_suffix == ".ogg" || $file_suffix == ".wav" || $file_suffix == ".mp3" || $file_suffix == ".png" || $file_suffix == ".gif" || $file_suffix == ".jpg" || $file_suffix == ".zip" || $file_suffix == ".tar" || $file_suffix == ".gz" || $file_suffix == ".gzip" || $file_suffix == ".rar" ){
                $media_flag = true;
            }else{
                $code_flag = true;
                return 0;
            }
        }
    }
    if($code_flag == true){
        return 0;
    }else if($translate_flag == true){
        return 2;
    }else if($apk_flag == true){
        return 1;
    }else if($media_flag == true){
        return 3;
    }else{
        return 0;
    }
}

function show_add_array($arr_add_file,$flag){
    $keys = array();
    $keys = array_keys($arr_add_file);
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

function get_add_file_str($arr_add_file){
    $keys = array();
    $keys = array_keys($arr_add_file);
    for($i=0;$i<count($keys);$i++){
        if($arr_add_file[$keys[$i]] == "select_deleted"){
	    $filename = $keys[$i];
	    $filename = str_ireplace("(","\(",$filename);
	    $filename = str_ireplace(")","\)",$filename);
	    $filename = str_ireplace("$","\\$",$filename);
	    $filename = str_ireplace("&","\\&",$filename);
	    exec("git rm -rf $filename");	    
        }
	if($arr_add_file[$keys[$i]] == "select_add" || $arr_add_file[$keys[$i]] == "select_modified" || $arr_add_file[$keys[$i]] == "select_renamed"){
	    $filename = $keys[$i];
	    $filename = str_ireplace("(","\(",$filename);
	    $filename = str_ireplace(")","\)",$filename);
	    $filename = str_ireplace("$","\\$",$filename);
	    $filename = str_ireplace("&","\\&",$filename);
	    exec("git add $filename");	    
        }
	if($arr_add_file[$keys[$i]] == "unselect_deleted" || $arr_add_file[$keys[$i]] == "unselect_add" || $arr_add_file[$keys[$i]] == "unselect_modified" || $arr_add_file[$keys[$i]] == "unselect_renamed"){
	    $filename = $keys[$i];
	    $filename = str_ireplace("(","\(",$filename);
	    $filename = str_ireplace(")","\)",$filename);
	    $filename = str_ireplace("$","\\$",$filename);
	    $filename = str_ireplace("&","\\&",$filename);
	    exec("git reset HEAD $filename");
	}
    }
}

function is_git_root(){
    $git_name = exec("ls -al --time-style=long-iso | grep '.git$' | awk '{print  $8}'");
    if($git_name == ".git"){
	return true;
    }else{
	return false;
    }
}


//start add by yangrenzhi 20190816
function getgitname_for_mtkpatch(){
    $gitname=exec('ls ./.git -al | grep "config" | awk "{print $11}" | sed "/.*projects\/\(.*\).git.*/ s//\1/"');    
    return $gitname;
}
//end add by yangrenzhi 20190816


function get_push_branch($push_branch){
    $result_push_branch = "";
    $arr_branch = array();
    $arr_branch = preg_split('/###/',$push_branch);
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

function down_repo_branch(){
    $branch = " ";
    $remote_name=exec("git remote -v | tail -1 | awk -F' ' '{print $1}'");
    $branch = exec("git branch -a | grep '\->' | sed -e 's/.*$remote_name.//' | sed -e 's/.*origin.//' "); 
    return $branch;
}

function get_project_branch(){
    global $patch_delivery_path;
    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
    if($repo_path!="/"){
    $project_branch = exec("ls -al ".$repo_path."/.repo/ | grep '\->' | sed -e 's/.*manifests.//'");
    $project_branch = substr($project_branch,0,-4);
    return $project_branch;}
    else{return;}
}

function clone_git_remote_branch(){  #usage:git checkout remotes/origin/branchname
    $remote_name=exec("git remote -v | tail -1 | awk -F' ' '{print $1}'");
    $checkout_remote_branch = " ";
    $file = ".git/logs/HEAD";
    if (file_exists($file)){
        $checkout_remote_branch = exec("cat .git/logs/HEAD | grep -E 'remotes/$remote_name' | tail -1 | sed -e 's/.*\///'");
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

function get_simplex_response_info($GET_URL){
    $handle = fopen("$GET_URL","r");
    $readline = fgets($handle,4096);
    $response = json_decode($readline,true);
    return $response;
}

function input_comment_info($comment_title){
    do{
	$final_comment = '';
        writeline("please input ".$comment_title.":");
        $arrcomments = read_lines(".");	
        foreach ($arrcomments as $comment){
	    if($comment != "."){
	        $final_comment .= $comment;
	    }
        }
        if($final_comment == '' ){
	    writeline($comment_title." is None, Please input again!\n");	
        }
    }while($final_comment == "");
    return $final_comment;
}


function select_option($arr_option){
    $input = true;
    for($i=0; $i<count($arr_option); $i++){
	$number = $i + 1;
	print ("    $number-".$arr_option[$i]."\n");
    }
    do{
	if($input == false){
	    writeline("Wrong choise input.\n");
	}    
	writeline("Your choise:");    
	$selected_type_number = read_line();        
	$input = false;
    }while(!is_numeric($selected_type_number)||$selected_type_number<1 || $selected_type_number >count($arr_option));
    return $selected_type_number;
}
//add by xiaoying 20180921
function select_option_NorT($arr_option){
    $input = true;
    for($i=0; $i<count($arr_option); $i++){
	$number = $i + 1;
	print ("    $number-".$arr_option[$i]."\n");
    }
    do{
	   if($input == false){
	        writeline("Wrong choise input.\n");
	    }    
	    writeline("Your choise:");    
	    $selected_type_number = read_line(); 
	    $input = false;   
	    if(is_numeric($selected_type_number)&&$selected_type_number>1&&$selected_type_number <= count($arr_option))    
	        $input = true;
	    else if(!is_numeric($selected_type_number)){
		    $read_number = 0;
            foreach($arr_option as $key=>$value){
                $read_number++;
                if ($selected_type_number == $value){
                    $input = true;
                    $selected_type_number =  $read_number;
                }
            }
	     }
    }while(!$input);
    return $selected_type_number;
}
//end by xiaoying.huang 20180921

function upload_exception_to_simplex($UPLOAD_URL, $post_data){
    $ch = curl_init(); 
    curl_setopt($ch, CURLOPT_POST, 1);  
    curl_setopt($ch, CURLOPT_URL,$UPLOAD_URL);  
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_data);  
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $result=curl_exec($ch);
    return $result;
}

function getCurrentCommitId(){
 	return exec("git log -1 | head -n 1 | awk '{print $2}'");
}

function getChangeId($author_name,$git_path_name,$server){
	$currentCommitId=getCurrentCommitId();
	$remote_name=exec("git remote -v | tail -1 | awk -F' ' '{print $1}'");
	$changeIdLine=system("git ls-remote $remote_name refs/changes/* | grep '$currentCommitId' | awk '{print $2}'");
	if(empty($changeIdLine)){
		$changeIdLine=system("git ls-remote ssh://".$author_name."@".$server.":29418/".$git_path_name."  refs/changes/* | grep '$currentCommitId' | awk '{print $2}'");
	}
	echo "==changeIdLine==$changeIdLine====";
	list($refs,$changes,$index,$changeId,$num)=explode('/',$changeIdLine);
	#list($refs,$changes,$index,$changeId,$num)=split('[/.-]',$changeIdLine);
	return $changeId;
}

function interactive_exception_info_with_simplex($xrNo, $status, $dataSourceType, $author_email,$remark_comment){
    global $HOST_URL;
    //Main Type
    $arr_main_type = '';
    $arr_main_type_id = '';
    $str_main_type = '';
    $str_main_type_id = '';
    //Sub Type
    $arr_sub_type = '';
    $arr_sub_type_id = '';
    $str_sub_type = '';
    $str_sub_type_id = '';
    $GET_MAIN = $HOST_URL.'/project/exc-reason-type/0/excTypeMark';
    $MainType = get_simplex_response_info($GET_MAIN);
    if(count($MainType['data'])){
	$i=0;
        foreach($MainType['data'] as $k => $v){
	    $arr_main_type_id[$i] = $k;
	    $arr_main_type[$i] = $v;
	    $i++;
	}
	writeline("Please chose the Main Type:\n");
    	$selected_type_number = select_option($arr_main_type);
    	$str_main_type = $arr_main_type[$selected_type_number - 1];
    	$str_main_type_id = $arr_main_type_id[$selected_type_number - 1];
    	$GET_SUB = $HOST_URL.'project/exc-reason-type/'.$str_main_type_id.'/excTypeByMark';
    	$SubType = get_simplex_response_info($GET_SUB);
	if(count($SubType['data'])){
	    for($i=0;$i<count($SubType['data']);$i++){
	    	$arr_sub_type[$i] = $SubType['data'][$i]['excTypeName'];
	    	$arr_sub_type_id[$i] = $SubType['data'][$i]['autoId'];
	    }
	    writeline("Please chose the Sub Type:\n");
   	    $selected_type_number = select_option($arr_sub_type);
    	    $str_sub_type = $arr_sub_type[$selected_type_number - 1];
    	    $str_sub_type_id = $arr_sub_type_id[$selected_type_number - 1];
    	}
    }
    $UPLOAD_URL = $HOST_URL."/project/patch-delivery/0/exception";
    $post_data = array('xrNo'=>$xrNo, 'status'=>$status, 'excTypeId'=>$str_sub_type_id, 'remarks'=>$remark_comment, 'dataSourceType'=>$dataSourceType, 'email'=>$author_email); 
    upload_exception_to_simplex($UPLOAD_URL, $post_data);
}

function upload_reg_reason_to_alm($xrNo, $regression_comment){
    global $patch_delivery_path;
    writeline("Please input assignee's ALM username:");
    $username = read_line();
    writeline("Please input assignee's ALM password:");
    $password = read_line();
    $error_flag = exec("python $patch_delivery_path"."ALM_push.py $xrNo $regression_comment $username $password");
    return $error_flag;
}

function get_fotas_from_commit_contents_all(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    foreach($need_commit_contents_all as $each_commit_content){
	$fota_file_path_name="$patch_delivery_path"."../conf/impact_fota/"."$repo_branch"."_conf/"."$repo_branch.file_conf";
	$fota_dir_path_name="$patch_delivery_path"."../conf/impact_fota/"."$repo_branch"."_conf/"."$repo_branch.dir_conf";
	if(file_exists($fota_file_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
	    $git_path=substr($current_path,strlen($repo_path)+1);
	    $fota_file_info=exec("cat $patch_delivery_path"."../conf/impact_fota/"."$repo_branch"."_conf/"."$repo_branch.file_conf | grep "."$git_path"."/"."$each_commit_content");	
	}
	if(empty($fota_file_info)){
	    if(file_exists($fota_dir_path_name)){
	        $fota_dir_info=exec("$patch_delivery_path"."find_fota_from_dir.sh $patch_delivery_path $repo_branch $each_commit_content");
	    }
	    if(!empty($fota_dir_info)){
	        $arr_commit_fotas[$index++]=$fota_dir_info;
            }
	}else{
	    $arr_commit_fotas[$index++]=$fota_file_info;
        }
    }
    if(!empty($arr_commit_fotas)){
    	return $arr_commit_fotas;
    }
    return null;
}
function get_customizations_from_commit_contents_all(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    foreach($need_commit_contents_all as $each_commit_content){
    	$customization_logo_path_name="$patch_delivery_path"."../conf/impact_customization/"."$repo_branch"."_conf/"."$repo_branch.logo_conf";
	$customization_media_path_name="$patch_delivery_path"."../conf/impact_customization/"."$repo_branch"."_conf/"."$repo_branch.media_conf";
	$customization_audio_path_name="$patch_delivery_path"."../conf/impact_customization/"."$repo_branch"."_conf/"."$repo_branch.audio_conf";
	$customization_wallpaper_path_name="$patch_delivery_path"."../conf/impact_customization/"."$repo_branch"."_conf/"."$repo_branch.wallpaper_conf";
	if(file_exists($customization_logo_path_name)){
	    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
	    $current_path=exec('pwd');
            $git_path=substr($current_path,strlen($repo_path)+1);
	    $customization_logo_info=exec("cat $patch_delivery_path"."../conf/impact_customization/"."$repo_branch"."_conf/"."$repo_branch.logo_conf | grep "."$git_path"."/"."$each_commit_content");
	}
	if(empty($customization_logo_info)){
	    if(file_exists($customization_media_path_name) or file_exists($customization_audio_path_name) or file_exists($customization_wallpaper_path_name)){
	    	$customization_dir_info=exec("$patch_delivery_path"."find_customization_from_dir.sh $patch_delivery_path $repo_branch $each_commit_content");
   	    }
	    if(!empty($customization_dir_info)){
	    	$arr_commit_customizations[$index++]=$customization_dir_info;
	    }	
	}else{
	    $arr_commit_customizations[$index++]=$customization_logo_info;	
	}
    }
    if(!empty($arr_commit_customizations)){
    	return $arr_commit_customizations;
    }
    return null;
}

function sendEmail($tolist,$subject,$body,$cclist=''){

        $mail = new PHPMailer();   
        $mail->IsSMTP();                  // send via SMTP   
        $mail->Host = "mail.tcl.com";   // SMTP servers   
        $mail->SMTPAuth = true;           // turn on SMTP authentication   
        $mail->Username = "hudson.admin.hz";     // SMTP username 
        $mail->Password = "HDS1216$!"; // SMTP password   
        $mail->From = "hudson.admin.hz@tcl.com";   
        $mail->FromName =  "hudson.admin.hz";   
        $mail->CharSet = "utf-8";
	$author_email=get_author_email();
	$mail->AddAddress($author_email,substr($author_email,0,-8));
        foreach($tolist as $to){
		$mail->AddAddress($to,substr($to,0,-8));	
	}
	if($cclist != ''){
	    foreach($cclist as $cc){
		$mail->AddCC($cc,substr($cc,0,-8));
	    }
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

// start add by shuangyan.he 20160328
function readtxt($prompt,$prompt1){
	global $txtfile;
	$fill_item = explode($prompt,$txtfile);
	$fill_item = $fill_item[1];	
	$fill_item = explode($prompt1,$fill_item);
	$fill_item = $fill_item[0];
	echo("\033[31;1m\nAffirm your $prompt"."\033[0m");
	echo $fill_item;
	if(!($prompt=="Module_Impact:"||$prompt=="Solution:"||$prompt=="Test_Report:")){
	         #$fill_item = explode(PHP_EOL,$fill_item);                
	#}
	#else{
		$fill_item = str_replace(PHP_EOL,' ',$fill_item);
		$fill_item = ifempty($prompt,$fill_item);
	}
	echo "'Enter' to go | 'n' to amend:\n";
	$choice = read_line();
	while(!($choice =='no' || $choice =='n' || $choice =='N' || $choice =='NO' || (strlen($choice) == 0))){
		echo "Input 'Enter'|'n'\n";
		$choice = read_line();
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

//begin add by yangrenzhi 20170815
function updateMtkpatchStatus($pnum,$CodeBranch,$patchtype,$vnum,$gitname,$status_value){
    $imid="";
    $gerrit_link='';
    //$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
    //mysql_select_db($db="dotproject",$dotp_conn);
    //$selectsql = "SELECT mp.id, mm.gerrit_link FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' AND mp.vnum='$vnum' ORDER BY mp.TS DESC LIMIT 0,1";
    //$result = mysql_query($selectsql);
    $dotp_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
    mysqli_select_db($dotp_conn,"dotproject");
    $selectsql = "SELECT mp.id, mm.gerrit_link FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' AND mp.vnum='$vnum' ORDER BY mp.TS DESC LIMIT 0,1";
    $result = mysqli_query($dotp_conn,$selectsql);
    if ($result){
        //while($row=mysqli_fetch_array($result)){
		while($row=mysqli_fetch_array($result)){
            $imid=$row['id'];
            $gerrit_link=$row['gerrit_link'];
        }
    }
    if($imid){        
        $updatesql = "UPDATE dotp_mtk_commit SET ismerged=1 WHERE import_id='$imid' and git_name='$gitname'";        
        try{
            //$result = mysql_query($updatesql);
            $result = mysqli_query($dotp_conn,$updatesql);
        }
        catch(Exception $e){
            print $e->getMessage();}
    }

}
//end add by yangrenzhi 20170815


//Begain add by shuangyan.he 20160710
function getidvnum_mtkmerge($pnum,$CodeBranch,$patchtype){
	//$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$dotp_conn);
	//$selectsql = "SELECT mp.id, mp.vnum FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' ORDER BY mp.TS DESC LIMIT 0,1";
	//$result = mysql_query($selectsql);
    $dotp_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	mysqli_select_db($dotp_conn,"dotproject");
	$selectsql = "SELECT mp.id, mp.vnum FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' ORDER BY mp.TS DESC LIMIT 0,1";
	$result = mysqli_query($dotp_conn,$selectsql);
	if ($result){
		$vnum="";
		$imid="";
		$dicidvnum=array();
		while($row=mysqli_fetch_array($result)){
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

function getidvnum_mtkmerge_simplex($pnum,$CodeBranch,$patchtype){
    $url = "http://10.92.35.176/pms/project/mtkk-interface/0/getidvnum_mtkmerge?pnum=$pnum&patchType=$patchtype&mergePatch=$CodeBranch";
    $dicidvnum=array();
    $vnum="";
    $imid="";
    try{
        $html = file_get_contents($url);
        $output_array = json_decode($html,true);
        if ($output_array["data"]){ 
            $length = count($output_array['data']);
            for($i=0;$i<$length;$i++){
                $vnum = $output_array["data"][$i]["vnum"];
                $imid = $output_array["data"][$i]["autoId"];
                $dicidvnum[$vnum]=$imid;}} else echo "There is no data for this patch num\n";}
    catch(Exception $e){
        print $e->getMessage();}
    return $dicidvnum;
}
function update_mtkpatch_mtkmerge_simplex($pnum,$CodeBranch,$patchtype,$vnum,$bug_id,$gerrit_link,$owner,$TS){
    $url = "http://10.92.35.176/pms/project/mtkk-interface/0/get_mtkpatch_mtkmerge?pnum=$pnum&vnum=$vnum&patchType=$patchtype&mergePatch=$CodeBranch";
    $mtk_id = '';
    try{
        $output_array = Array();
        $html = file_get_contents($url);
        $output_array = json_decode($html,true);
        }       
    catch(Exception $e){
        print $e->getMessage();}
    $length = count($output_array['data']);
    $arrmtk_id=array();
    if ($length>0){
        $mtk_id=$output_array['data']['mergeId'];
        array_push($arrmtk_id,$mtk_id);
        $bugid_old=$output_array['data']['bugId'];//bugid
        $gerrit_link_old=$output_array['data']['gerritLink'];//gerritlink
        $chooseid=$output_array['data']['importId'];
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
                print "\n--------merged id should exist mtk_merge--------\n";
                return False;}
            else{//test 
                //print "Time String $TS\n";
                print "\nbegin to update bug_id/owner/gerritlink/TS to $CodeBranch,importid ad $chooseid\n";
                $url = "http://10.92.35.176/pms/project/mtkk-interface/0/update_mtk_merge_status?importId=$chooseid&mergePatch=$CodeBranch&owner=$owner&bugId=$bugid_new&gerritLink=$gerrit_link_new&mergeTimeStampStr=$TS";
                try{
                    $url = preg_replace("/ /","%20",$url);
                    $url = preg_replace("/#/","%23",$url);
                    $html = file_get_contents($url);
                    $output_array = json_decode($html,true);
                    print "\nupdate mtk_merge.db $CodeBranch mm.import_id=$chooseid OK\n";}
                catch(Exception $e){
                    print $e->getMessage();
                    print "\nupdate mtk_merge $CodeBranch failed,Pls contact INT\n";
                    return False;}
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
            $mmid=update_other_combranch_simplex($pnum,trim($vnum),$com_branch,$patchtype,$bug_id,$gerrit_link,$owner,$TS);
            array_push($arr_mmid,$mmid);
        }
    }
    //end add by shuangyan.he 20160721
    return $arrmtk_id;  
    }
function update_mtkpatch_mtkmerge($pnum,$CodeBranch,$patchtype,$vnum,$bug_id,$gerrit_link,$owner,$TS){
	//$db_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$db_conn);
	//$getidsql = "SELECT mp.id as chooseid,mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm ) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' and mp.vnum='$vnum'";
	//$mmresult = mysql_query($getidsql);
    $db_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	mysqli_select_db($db_conn,"dotproject");
	$getidsql = "SELECT mp.id as chooseid,mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm ) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND mm.merge_patch = '$CodeBranch' AND mp.patch_type = '$patchtype' and mp.vnum='$vnum'";
	$mmresult = mysqli_query($db_conn,$getidsql);
	$arrmtk_id=array();
	if ($mmresult){
		while($rows=mysqli_fetch_array($mmresult)){
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
				print "=================$updatesql=====================\n";
				$result = mysqli_query($db_conn,$updatesql);
				print "result ,$result\n";
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
	#$mtkpconfig_link = "http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/mtkcombranch";
	global $patch_delivery_path;
	$mtkpconfig_link="$patch_delivery_path"."../conf/mtkcombranch";
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
    //$select2sql = "SELECT mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND vnum='$vnum' and mm.merge_patch = '$com_branch' AND mp.patch_type = '$patchtype'";
    //$othresult = mysql_query($select2sql);
    $dotp_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
    mysqli_select_db($dotp_conn,"dotproject");
    $select2sql = "SELECT mm.id,mm.bug_id,mm.gerrit_link FROM (dotp_mtk_merge AS mm) JOIN (dotp_mtk_import AS mp) ON mp.id = mm.import_id WHERE mp.pnum ='$pnum' AND vnum='$vnum' and mm.merge_patch = '$com_branch' AND mp.patch_type = '$patchtype'";
    $othresult = mysqli_query($dotp_conn,$select2sql);
    if ($othresult){
        $othresult = mysqli_fetch_array($othresult);
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
            $result2 = mysqli_query($dotp_conn,$update2sql);
            if (!$result2){
                print "\nupdate mtk_merge $com_branch failed,Pls fill it manually or contact INT.\n";
                return False;
            }else{
                print "\nupdate mtk_merge.db $com_branch mm.id=$mmid OK.\n";}
        }
    }
    return $mmid;
}
function update_other_combranch_simplex($pnum,$vnum,$com_branch,$patchtype,$bug_id,$gerrit_link,$owner,$TS){
	$url = "http://10.92.35.176/pms/project/mtkk-interface/0/get_mtkpatch_mtkmerge?pnum=$pnum&vnum=$vnum&patchType=$patchtype&mergePatch=$com_branch";
    try{
        $output_array = Array();
        $html = file_get_contents($url);
        //echo "get combranch importid and merged id info $html\n";
        $othresult = json_decode($html,true);
        }       
    catch(Exception $e){
        print $e->getMessage();}
    $length = count($othresult['data']);
    $arrmtk_id=array();
	if ($length>0){
		$mmid=$othresult['data']['mergeId'];
		$bugid_old2=$othresult['data']['bugId'];
		$gerrit_link_old2=$othresult['data']['gerritLink'];
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
            $url = "http://10.92.35.176/pms/project/mtkk-interface/0/update_other_combranch?mergeId=$mmid&owner=$owner&bugId=$bugid_new2&gerritLink=$gerrit_link_new2&mergeTimeStampStr=$TS";
            try{
                $url = preg_replace("/ /","%20",$url);
                $url = preg_replace("/#/","%23",$url);
                $html = file_get_contents($url);
               // echo "update_other_combranch info $html\n";
                $output_array = json_decode($html,true);
                print "\nupdate mtk_merge.db $com_branch mm.id=$mmid OK.\n";}
            catch(Exception $e){
                print $e->getMessage();
                print "\nupdate mtk_merge $com_branch failed,Pls fill it manually or contact INT.\n";}
                //var_dump($output_array);
        }
    }
    return $mmid;
}


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
    $commit_apk=exec("echo $each_commit_content | grep '.apk$'");
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



function get_check_file_git(&$arr_check_gits,$file){
	if(file_exists($file)){
	    $handle = fopen("$file","r");     
	    while(!feof($handle)){
		 $readline = '';
		 $readline = fgets($handle,2096);
		 $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
		 $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
		 $readline = preg_replace("/(\")/m","",$readline); 
		 if(empty($readline)){continue;}
		 else{ 
		    $file_git_path_arr = explode('/', $readline);
		    if (count($file_git_path_arr) > 0 )
		    {
		    	if(!array_search($file_git_path_arr[0],$arr_check_gits))
				array_push($arr_check_gits,$file_git_path_arr[0]);
		    }
		 }
		 
	    }
        }
	return $arr_check_gits;
}
//update by shie 20160618
// modify by lyf 2017-01-23
function get_fotas_from_commit_contents_all_sdd1(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
    $current_path=exec('pwd');
    $git_path=substr($current_path,strlen($repo_path)+1);
    $arr_commit_fotas = array();
    
    $arr_checkfiles = array();
    $fota_file_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_file_config";
    $fota_var_file_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_var_file_config";
    $fota_tmp_file_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_file_config_tmp";
    $fota_dir_path_name="$patch_delivery_path"."../conf/impact_fota_sdd1/"."fota_dir_config";
    //$arr_checkfiles = get_check_file_git($arr_checkfiles,$fota_file_path_name);
    //$arr_checkfiles = get_check_file_git($arr_checkfiles,$fota_dir_path_name);
    //if(!array_search($git_path,$arr_checkfiles))
    //{
    //	return null;
    //}
    
    $mtk_project_name=get_variable_dir($repo_branch);
    $arr_mtk_project_name = preg_split("/@/",$mtk_project_name);
       
    $num = 0.0;
    $total_num = count($need_commit_contents_all);
    $numStr="";
    foreach($need_commit_contents_all as $each_commit_content){
        $fota_file_info='';
        $num++;
        
	//this function take too long,so print progress
        if (($num/$total_num*100)%10 == 0)
        {
	    printf("fota file check:[%s=>]%d%%\r",$numStr,$num/$total_num*100);
	    $numStr="=".$numStr;
	}
	
	$fota_file_info = "";
	if(file_exists($fota_var_file_path_name)){
	    //echo "check fota file dir \n";
	    foreach($arr_mtk_project_name as $each_project_name){
		    exec("rm -rf $fota_tmp_file_path_name");
		    exec(" sed 's/\.\*/$each_project_name/' $fota_var_file_path_name > $fota_tmp_file_path_name");
		    if(file_exists($fota_tmp_file_path_name)){
		        $fota_file_info=exec("cat $fota_file_path_name >> $fota_tmp_file_path_name"); 
		    }
		    if(file_exists($fota_tmp_file_path_name)){
		    	 //echo "cat $fota_tmp_file_path_name | grep $each_commit_content\n";
		         $fota_file_info=exec("cat $fota_tmp_file_path_name | grep "."$git_path"."/"."$each_commit_content"); 
		         if(!empty($fota_file_info)){
		    	    $arr_commit_fotas[$index++]=$fota_file_info;
		    	    break;
			 }
		    }
            }
	}
	if(empty($fota_file_info)){
	    if(file_exists($fota_dir_path_name)){
	    	$filename = "fota";
                $dirname="impact_fota_sdd1";
	    	$fota_dir_info=exec("$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname");
	        //$fota_dir_info=exec("$patch_delivery_path"."find_fota_from_dir.sh $patch_delivery_path $repo_branch $each_commit_content");
	    }
	    if(!empty($fota_dir_info)){
	        $arr_commit_fotas[$index++]=$fota_dir_info;
            }
	}
    }
    print "\n";
    if(!empty($arr_commit_fotas)){

    	return $arr_commit_fotas;
    }
    return null;
}
// end modify by lyf 2017-01-23

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
   $mtkprojectname="$patch_delivery_path"."../conf/impact_mtkpatch/"."remind_mtkproject"; 
   $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
   $current_path=exec('pwd');
   $git_path=substr($current_path,strlen($repo_path)+1);
   
    $arr_checkfiles = array();
    $arr_checkfiles = get_check_file_git($arr_checkfiles,$mtkprojectname);
    #print $git_path;
    #if(!array_search($git_path,$arr_checkfiles))
    #{
    	#return null;
   # }
   foreach($need_commit_contents_all as $each_commit_content){

        if(file_exists($mtkprojectname)){

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


function get_last_mtkpatch_mergestatus($patch_delivery_path,$branch_name,$patch_num,$patchtype,$prj_link_file)
{
	if ($patch_num <= 0)
		return 0;
	//$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	//mysql_select_db($db="dotproject",$dotp_conn);
	//$selectsql = "SELECT mm.`merge_status`,`bug_id`, mm.id,mm.owner FROM `dotp_mtk_merge` as mm
//left join `dotp_mtk_import` as mi on mm.import_id =mi.id WHERE `merge_patch`='$branch_name' and `pnum`='P$patch_num' and patch_type='$patchtype' ";
	//print $selectsql;
	//$result = mysql_query($selectsql);
    $dotp_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
	mysqlI_select_db($dotp_conn,"dotproject");
	$selectsql = "SELECT mm.`merge_status`,`bug_id`, mm.id,mm.owner FROM `dotp_mtk_merge` as mm
left join `dotp_mtk_import` as mi on mm.import_id =mi.id WHERE `merge_patch`='$branch_name' and `pnum`='P$patch_num' and patch_type='$patchtype' ";
	//print $selectsql;
	$result = mysqli_query($dotp_conn,$selectsql);
	if ($result)
	{
		$result = mysqli_fetch_array($result);
	
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
				    print "The P$patch_num defect/task state is not Delivered or Verified_SW,Verified,Closed,Postponed,Deleted , please check! \n";
				    print "\n\033[31;1m---------Last mtk Patch P$patch_num Owner $mm_owner -----------\033[0m  \n";
				    return 2;
				    break;
			    }
			}
			//update lastmtk patch status
			$updatesql = "UPDATE dotp_mtk_merge SET merge_status=1 WHERE merge_patch='$branch_name' and id='$mm_id'";
			//print $updatesql;
			$result = mysqli_query($dotp_conn,$updatesql);
			return 0;
		}
	}else
		return 3;
}

//add by yrz 2017621 for simplex
function get_last_mtkpatch_mergestatus_simplex($patch_delivery_path,$branch_name,$patch_num,$patchtype)
{
    if ($patch_num <= 0)
        return 0; 
    $patch_num = 'P'.$patch_num;
    $url = "http://10.92.35.176/pms/project/mtkk-interface/0/check_last_mtk_patch_status?pnum=$patch_num&patchType=$patchtype&mergePatch=$branch_name";
    $output_array = Array();
    try{
        $html = file_get_contents($url);
        $output_array = json_decode($html,true);
        }
    catch(Exception $e){
        print $e->getMessage();
        return 3;}
    if (count($output_array['data']) ==0)
        {
            return 1;#please make sure int have merge to import branch and remarked in smarttask
        }
    $mm_status=$output_array['data'][0]["mergeStatus"];
    $bugs=$output_array['data'][0]["bugId"];
    $mm_id=$output_array['data'][0]["autoId"];
    $mm_owner=$output_array['data'][0]["owner"];
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
            $error_flag = exec("$patch_delivery_path"."ALM_check_status.py $branch_name $defect",$out);
            switch ($error_flag) {
            case 200:
                print "OK! go next \n";
                break;
            case 600:
                print "The P$patch_num defect/task state is not Delivered or Verified_SW,Verified,Closed,Postponed,Deleted , please check! \n";
                print "\n\033[31;1m---------Last mtk Patch P$patch_num Owner $mm_owner -----------\033[0m  \n";
                return 2;
                break;
            }
        }
        $url = "http://10.92.35.176/pms/project/mtkk-interface/0/get_last_mtkpatch_mergestatus?mergeId=$mm_id";
        try{
            $html = file_get_contents($url);
            $output_array = json_decode($html,true);
            return 0;}
        catch(Exception $e){
            print $e->getMessage();
            return 3;}
        }
}

//add by lyf 2017-02-13 for ImpactAudio
function get_audio_update_from_commit_contents(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    global $index_audio;
    $repo_branch = down_repo_branch();

    $mtk_project_name=get_variable_dir($repo_branch);
    $arr_mtk_project_name = preg_split("/@/",$mtk_project_name);
    
    $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
    $current_path=exec('pwd');
    $git_path=substr($current_path,strlen($repo_path)+1);
        
    foreach($need_commit_contents_all as $each_commit_content){
	$audio_dir_path_name="$patch_delivery_path"."../conf/impact_audio/"."audio_dir_config";
	if(file_exists($audio_dir_path_name)){
            $filename = "audio";
            $dirname="impact_audio";
            //echo "$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname";
	    $audio_dir_info=exec("$patch_delivery_path"."find_info_from_dir.sh $patch_delivery_path $filename $each_commit_content $dirname");
	}
	if(!empty($audio_dir_info)){
	    $arr_commit_audio[$index_audio++]=$audio_dir_info;
        }else{
            //adjust var diraudio 
	    foreach($arr_mtk_project_name as $each_mtk_project_name){
		    if(!empty($each_mtk_project_name)){
		    	    $config_file_path_name="$patch_delivery_path"."../conf/impact_audio/audio_var_dir_config";
		    	    if(file_exists($config_file_path_name)){
				$handle = fopen("$config_file_path_name","r");
				while(!feof($handle)){
				    $readline = fgets($handle,2096);
				    if (empty($readline)) continue;
				    $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
				    $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
				    $audio_check_dir = preg_replace('/\.\*/', $each_mtk_project_name, $readline);
				    
				    $audio_dir_info=exec("echo $git_path/$each_commit_content |grep "."$audio_check_dir");
				    if(!empty($audio_dir_info))
				    {
				       $arr_commit_audio[$index_audio++]=$audio_dir_info;
				    }
				}
			    }
		    } 
	    }
	    //end addjust var diraudio
       
       }		
    }
    
    if(!empty($arr_commit_audio)){
        return $arr_commit_audio;
    }
    return null;
}
//end by lyf 2017-02-13 form impactaudio

//add by lyf 2017-02-13 for get var dir
function get_variable_dir($branch)
{
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $config_file_path_name="$patch_delivery_path"."../conf/impactconfig";
    $mtk_project_name;
    if(file_exists($config_file_path_name)){
        $handle = fopen("$config_file_path_name","r");
        while(!feof($handle)){
            $readline = fgets($handle,2096);
            $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
            $readline = preg_replace("/(\\/\\/.*)|(\\#.*)/m","",$readline);
            if(empty($readline)){
	        continue;
            }else{
	        $readline = preg_split("/[\s]+/",$readline);
	        //print_r($readline);
	        if (count($readline)<2)
	    	    continue;
	    	if ($readline[0] == $branch){     
                    $mtk_project_name = $readline[1];
                    break;  
                }    
            }
        }
        fclose($handle);
    }
    if (empty($mtk_project_name))
    	return null;
    else
        return $mtk_project_name;
}
//end by lyf 2017-02-13 for get var dir
function get_peojects_with_onemanifestfile($project_maniest){
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $config_file_path_name="$patch_delivery_path"."../conf/diffluncher_in_onecodebranch";
    $all_project_name;
    if(file_exists($config_file_path_name)){
        $handle = fopen("$config_file_path_name","r");
        while(!feof($handle)){
            $readline = fgets($handle,2096);
            $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
            if(empty($readline)){
	        continue;
            }else{
	        $readline = preg_split("/[\s]+/",$readline);
		//print "$readline------------";
		var_dump($readline);
	        //print_r($readline);
	        if (count($readline)<2)
	    	    continue;
		//print "readline,project_maniest  $readline[0],$project_maniest\n";
	    	if ($readline[0] == $project_maniest){     
                    $all_project_name = $readline[1];
		    print "all_project_name,$all_project_name\n";
                    break;  
                }
            }
    	}
        fclose($handle);
    }

    if (empty($all_project_name))
    	return null;
    else
        return $all_project_name;
}

//add by lyf 2017-2-14 for customizations check
function get_customizations_from_commit_contents_all_new(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    
    //$repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
    //$current_path=exec('pwd');
    //$git_path=substr($current_path,strlen($repo_path)+1);
    
    $mtk_project_name=get_variable_dir($repo_branch);
    $arr_mtk_project_name = preg_split("/@/",$mtk_project_name);
    
    foreach($need_commit_contents_all as $each_commit_content){
    	$customization_path_name="$patch_delivery_path"."../conf/impact_customization/customization_dir_config";
    	$customization_tmppath_name="$patch_delivery_path"."../conf/impact_customization/customization_tmp";
	if(file_exists($customization_path_name)){
	    foreach($arr_mtk_project_name as $each_project_name){
		    exec("rm -rf $patch_delivery_path"."../conf/impact_customization/customization_tmp");
		    exec(" sed 's/\.\*/$each_project_name/' $customization_path_name > $customization_tmppath_name");
		    if(file_exists($customization_tmppath_name)){
		    	 //echo "cat $customization_tmppath_name | grep $each_commit_content\n";
		         $customization_dir_info=exec("cat $customization_tmppath_name | grep $each_commit_content");   
		         if(!empty($customization_dir_info)){
		    	    $arr_commit_customizations[$index++]=$customization_dir_info;
		    	    break;
			 }	  
		    }
            }
	}
    }
    
    //print_r($arr_commit_customizations);

    if(!empty($arr_commit_customizations)){
    	return $arr_commit_customizations;
    }
    return null;
}
//end by lyf 2017-2-14 for customizations check

//add by lyf 2017-3-15 for gapp name check
function get_gapp_name_rule_check($apk_name){
    $index = strrchr($apk_name,'/');
    $filterDirArray=array('Touchpal_Pack');
    foreach($filterDirArray as $eachdir){
        $indexDir = strpos($apk_name,$eachdir);
        #var_dump($indexDir);
        if ($indexDir){
        break;
        }
    }
    #print "index==${index}==indexStr==${indexDir}==";
    if (!empty($index))
        $apk_name = substr(strrchr($apk_name,'/'),1);
    //print "echo $apk_name | grep -i 'Global\|CN2\|Orange\|LATAM\|Naranya\|india\|telus\|MTK'\n";
    exec("echo $apk_name | grep -i 'Global\|GL\|PG\|CN2\|CN\|Orange\|LATAM\|Naranya\|india\|telus\|MTK\|NA\|EU'",$out,$status);
    if ($status and empty($indexDir)){
	writeline("\033[31;1m $apk_name name is illegitmacy,must contain country. Country can be Global|GL|PG|CN2|CN|Orange|LATAM|Naranya|india|telus|MTK|NA|EU \033[0m\n");
	writeline("\033[31;1m if you have to sumbit new Country,Please contact VER Team to Update patch_deliver tools!\033[0m\n");
	exit;
    }
}

function together_git_remind_check($togetherconfig_link,$product_name,$git_name){
	global $together_projects;
	global $together_gits;
	get_togetherinfo($togetherconfig_link,$product_name);
        foreach($together_gits as $each_git){
            if ($each_git == "GITNAME") {continue;}
            $preg_var = "/.*".$each_git.".*/";
	    if(preg_match($preg_var,$git_name) || $each_git == 'all')
	    {
	    	do{
		    	writeline("\033[31;1m \nAttention Please! This Change will affect on these projects:\033[0m\n");
			for($i=1; $i<count($together_projects); $i++){
				$number = $i;
				print ("    $number-".$together_projects[$i]."\n");
			}
			writeline("\nPress Enter key to contiune!");
			$is_resource_change= read_line();  
		}
		while($is_resource_change);
	    	break;
	    }
	}
}

function get_togetherinfo($togetherconfig_link,$product_name){
	global $together_projects;
	global $together_gits;
        $handle = fopen("$togetherconfig_link","r");
        while(!feof($handle)){
                $readline = fgets($handle,2096);
                $readline = preg_replace("/(\s*$)|(^\s*)/m","",$readline);
                $readline = preg_replace("/(\\/\\/.*)|(\/\/.*)/m","",$readline);
                $readline = preg_replace("/(\")/m","",$readline);
                if(empty($readline)){continue;}
                else
                {
                        $readline = preg_split("/,/",$readline);
			if ($readline[0] != $product_name) {continue;}
			else
			{
				if (count($readline) <3 )  {continue;}
				$together_projects = preg_split("/#/",$readline[1]);
				$together_gits = preg_split("/#/",$readline[2]);
				break;
			}		
                }
        }
        fclose($handle);
}

//add by lyf 2017-3-15 

//add by shie 2017-4-19 for impact project config dir or files 
function get_projectname_config_from_commit_contents_all_new(){
    global $need_commit_contents_all;
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $index=0;
    $forbidindex=0;
    $forbid_project = array('buzz6e');
    
   // $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
   // $current_path=exec('pwd');
   // $git_path=substr($current_path,strlen($repo_path)+1);
    
    $project_name=get_variable_dir($repo_branch);
    $arr_project_name = preg_split("/@/",$project_name);
    if (count($arr_project_name) <= 1 ){
        return null;
    }
    foreach($need_commit_contents_all as $each_commit_content){
	    foreach($arr_project_name as $each_project_name){
		         $project_name_info=exec("echo $each_commit_content | grep $each_project_name");
		         if(!empty($project_name_info)){
		    	    $arr_commit_projectname_config[$index++]=$project_name_info;
		    	    break;
			 } 
            }                
            foreach($forbid_project as $each_forbid_project){
                          $gmo_project = "$each_forbid_project"."_gmo";
                          $forbid_project_name_gmo_info=exec("echo $each_commit_content | grep $gmo_project");  
                          $forbid_project_name_info=exec("echo $each_commit_content | grep $each_forbid_project");                      	
		          if(!empty($forbid_project_name_info) and empty($forbid_project_name_gmo_info)){
		    	     $arr_forbid_projectname_config[$forbidindex++]=$forbid_project_name_info;
                             break;
			  } 
		   
            }
	
    }
    if(!empty($arr_forbid_projectname_config)){         
		writeline("\033[35;1m************************\033[0m\n");
		foreach($arr_forbid_projectname_config as $each_forbid_projectname){
			$each_forbid_projectname=trim($each_forbid_projectname);
			writeline("\033[35;1m$each_forbid_projectname\033[0m\n");		
		}
		writeline("\033[35;1m************************\033[0m\n");	
		#writeline("\033[31;1m[Error!!] the commited contents between '*' are no GMO project config files,which not allowed to upload.Pls merge it to <project>_gmo config. \033[0m\n");
                #exit;
                writeline("\033[31;1m[Notice!!] the commited contents between '*' are no GMO project config files,Please make sure to sync to <project>_gmo config. \033[0m\n");
               	writeline("\nPress Enter key to contiune!");
		$is_resource_change= read_line(); 
    }


    if(!empty($arr_commit_projectname_config)){         
		writeline("\033[35;1m************************\033[0m\n");
		foreach($arr_commit_projectname_config as $each_commit_projectname){
			$each_projectname=trim($each_commit_projectname);
			writeline("\033[35;1m$each_projectname\033[0m\n");		
		}
		writeline("\033[35;1m************************\033[0m\n");	
		writeline("\033[31;1m[Notice!!] the commited contents between '*' are project configuration files, \033[0m\n");
		writeline("\033[31;1mPlease confirm sync to other projects as follow::\033[0m\n");
		for($i=0; $i<count($arr_project_name); $i++){
			$number = $i+1;
			print ("    $number-".$arr_project_name[$i]."\n");
		}
		writeline("\nPress Enter key to contiune!");
		$is_resource_change= read_line();
                 
  
    }
    return null;
}
//end by shie 2017-4-19 for impact project config dir or files 

//add by lyf 2017-04-21 for frameworks-res 
function get_team_from_email($owner_mail){
    global $patch_delivery_path;
    global $HOST_URL;
    $frm_flag = 'other';
    $GET_URL = $HOST_URL."/project/patch-delivery/0/getOrgByEmail?email=".$owner_mail;
    $handle = fopen("$GET_URL","r");
    $readline = fgets($handle,2096);
    if ($readline){
        $Type = json_decode($readline,true);
	$keys = array_keys($Type);
	if (count($Type['data'])){
	    if($Type['data']['orgName'] == 'FRM1'){
	    	$frm_flag = 'FRM1';
	    }
	}
    }else{
    	//get team from simplex error,then search local config
    	$frm_config_file = $patch_delivery_path."../conf/frmteam_config";
    	if(file_exists($frm_config_file)){
	    $frm_info=exec("cat $frm_config_file | grep $owner_mail");	
	    if(!empty($frm_info)){
		$frm_flag = 'FRM1';
	    }
	}
    }
    return $frm_flag;
}

//dir:frameworks/base/core/res  team:FRM1
function check_frameworks_res_submit($git_name,$owner_mail){
    global $need_commit_contents_all;
    if(strpos($git_name,"frameworks")){
    	$frm_owner = get_team_from_email($owner_mail);
    	if ($frm_owner == 'FRM1')
    	    return false;
        foreach($need_commit_contents_all as $each_commit_content){
            if(strstr($each_commit_content,"base/core/res")){
                #print "\033[31;1m Only FRM1 Team can write path: frameworks/base/core/res!!!\033[0m\n";
            	#exit;
                #print "\033[31;1m Need FRM1 Team to review the patch which include file: frameworks/base/core/res!!!\033[0m\n";
                return true;
            }
        }
    	
    }
}
//end by lyf 2017-04-21

//add by shie.zhao 20170613 for FRM1review
function add_FRM1_reviewer_frameworks_res_submit($author_name,$git_path_name,$server){
  global $arr_FRM1_reivewer;
  global $patch_delivery_path;
  $reviewer="";
  $changeId = getChangeId($author_name,$git_path_name,$server);
  $reviewer_count=count($arr_FRM1_reivewer);
  for($i=0;$i<$reviewer_count;$i++){
     $reviewer.= " -a ".$arr_FRM1_reivewer[$i];
  }
  $arr_reivewer=get_confreview($patch_delivery_path,"reviewconfig");
  if($reviewer_count>0){
      for($j=0;$j<$reviewer_count;$j++){
           $reviewer.= " -a ".$arr_reivewer[$j];
      }
  }
  $reviewer_cmd = "ssh -o ConnectTimeout=8 -p 29418 $author_name@$server gerrit set-reviewers -p $git_path_name $reviewer $changeId" ;
  print $reviewer_cmd ;
  system($reviewer_cmd);
  print "\n Need FRM1 Team to review the path: frameworks/base/core/res!!! Has added FRM1 Team to review on Gerrrit!!!";

}
//end

//add by xiaoying.huang 20171020 for review
function add_reviewer_frameworks_res_submit($author_name,$git_path_name,$server){
  global $patch_delivery_path;
  $arr_reivewer=get_confreview($patch_delivery_path,"reviewconfig");
  $reviewer_count=count($arr_reivewer);
  $reviewer="";
  if($reviewer_count>0){
      $changeId = getChangeId($author_name,$git_path_name,$server);
      for($i=0;$i<$reviewer_count;$i++){
           $reviewer.= " -a ".$arr_reivewer[$i];
      }
      $reviewer_cmd = "ssh -o ConnectTimeout=8 -p 29418 $author_name@$server gerrit set-reviewers -p $git_path_name $reviewer $changeId" ;
      print $reviewer_cmd ;
      system($reviewer_cmd);
      print "\n Need Team to review the path: frameworks/base/core/res!!! Has added Team to review on Gerrrit!!!";
  }
}

function get_confreview($patch_delivery_path,$configfile){
	
    $arr_prj = array();
	$prj_list_file_path = $patch_delivery_path."../conf/".$configfile;
	if(file_exists($prj_list_file_path)){
       $handle = fopen("$prj_list_file_path","r");
       while(!feof($handle)){
           $readline = fgets($handle,2096);
           $readline = trim($readline);
           if(trim($readline) != "")
               $arr_prj[] = $readline;
       }
       fclose($handle);
    }
    return $arr_prj;
}

function update_simplex_userpass($name,$namepass){
	
    $HOST_URL = "http://10.92.35.176/pms";
	$UPLOAD_URL = $HOST_URL."/skill/user-info/0/updateUserPasswordOnline";
	$post_data = array('loginName'=>$name, 'loginPasswordOnline'=>$namepass); 
	$post_data = json_encode($post_data);
	$post_data = array('jsonContent' => $post_data);
    $result=upload_exception_to_simplex($UPLOAD_URL, $post_data);
	print "result====$result";
	return $result;
}
//end

//check plf files 20170502 shie

function get_plfs_from_commit_contents(){
    global $need_commit_contents;
    $index=0;
    foreach($need_commit_contents as $each_commit_content){
      $commit_plfs=exec("echo $each_commit_content | grep '.plf$'"); 
      if(!empty($commit_plfs)){
         $arr_commit_plfs[$index++]=$commit_plfs;
      }
    }
    if(!empty($arr_commit_plfs)){
    	return $arr_commit_plfs;
    }
    return null; 
}

function read_xml($file,$Projectflag,$each_project_name){
   $fh=fopen("$file",'r') or die($php_errormsg);
   $simple = fread($fh,filesize("$file"));
   fclose($fh) or die($php_errormsg);

   $p=xml_parser_create();
   xml_parse_into_struct($p,$simple,$vals,$index);
   xml_parser_free($p);
   $arr_SDMID_value=array();
   for($i=0;$i<count($index['SDMID']);$i++){  
      if($vals[$index['SDMID'][$i]]['value']){
          $value = $vals[$index['SDMID'][$i]]['value'];
          $arr_SDMID_value[$i]=$value;      
          if(preg_match("/[ *?~`!@#$%^&+=<>{}]/",$value)){
              print "\033[31;1m Error!!SDMID <$value> include space or special characters: *?~`!@#$%^&+=<>{} \033[0m\n" ; 
              print "=>$file \n"; 
              exit; 
          }
      }
      #$spec = array('FEATURE','ARRAY','METATYPE','C_TYPE','IS_CUSTO','DESC','VALUE');
      #foreach($spec as $each_spec){
        #$error_tag = 0;

        #if($i<count($index[$each_spec])){
           #$value_desc = $vals[$index[$each_spec][$i]]['value'];
           #if (preg_match("/[?~`@$%^&{}]/",$value_desc)){
              #$arr_desc = preg_split("/\"/",$value_desc);
              #if (count($arr_desc) == 1 ){
                  #$error_tag = 1;              
              #}
              #else{
                  #for($j=0;$j<count($arr_desc);$j++){
                      #if($j%2==0 and preg_match("/[?~`@$%^&{}]/",$arr_desc[$j]) and !preg_match("/\'@\'/",$arr_desc[$j]) ){
                          #$error_tag = 1;
                      #}
                 # }  
              #}
          # }
        #}
        #if($error_tag == 1){             #if(!preg_match("/[\"](.*?)[?~`@$%^&{}](.*?)[\"]/",$value_desc))
            #print "\033[31;1m SDMID desc Error!! <$each_spec>$value_desc</$each_spec> include special characters:?~`!@$%^&{} \033[0m\n" ;  
            #print "=>$file \n";
            #exit; 
        #}
      #}
   }

   if($Projectflag){
     if(strstr($file,$each_project_name) and !strpos($file,"isdm_thirdparty.plf")){
        $jrdplf=str_replace($each_project_name,'jrdhz',$file);
        if(file_exists($jrdplf)){
           if(!empty($arr_SDMID_value)){
                foreach($arr_SDMID_value as $each_SDMID_value){
                   $outSDMID = exec("cat $jrdplf | grep '<SDMID>$each_SDMID_value</SDMID>'");
                   if(empty($outSDMID)){
                      print "\033[31;1m Error!!$file SDMID <$each_SDMID_value> not in $jrdplf \033[0m\n" ;  
                      exit;
                   }
                }
           }
        }
     }
   }
 
}

function check_xml($file){
   $xmlparser = xml_parser_create();

   xml_parser_set_option($xmlparser,XML_OPTION_CASE_FOLDING,true);

   $fp = fopen($file,'r') or die($php_errormsg);

   $has_error = false;

   while ( $xmldata = fread($fp,4096))
   {
     if(!xml_parse($xmlparser,$xmldata,feof($fp)))
     {
         $has_error = true;
         break;
     }
   }

   if($has_error){
      $error_line = xml_get_current_line_number($xmlparser);
      $error_string = xml_error_string(xml_get_error_code($xmlparser));
      print "\033[31;1m PLF error!! $file line $error_line $error_string\033[0m\n" ;
      xml_parser_free($xmlparser);
      exit;
            
   }
   xml_parser_free($xmlparser); 

}


function check_plf_contect($git_name){
    global $patch_delivery_path;
    $repo_branch = down_repo_branch();
    $mtk_project_name=get_variable_dir($repo_branch);
    $arr_mtk_project_name = preg_split("/@/",$mtk_project_name);
    $Projectflag=false;
    $index=1;
    $arr_checked_plfs=array();

    if(strpos($git_name,"wprocedures")){
       $commit_plfs=get_plfs_from_commit_contents();
       if(!empty($commit_plfs)){
           foreach($commit_plfs as $each_commit_plf){
              $Projectflag=false;
              check_xml($each_commit_plf);
              foreach($arr_mtk_project_name as $each_project_name){
                 if(strstr($each_commit_plf,$each_project_name)){
                     $Projectflag=true;
                     foreach($arr_mtk_project_name as $each_project_name_other){
                           $otherpjplf = str_replace($each_project_name,$each_project_name_other,$each_commit_plf);
                           if(file_exists($otherpjplf) and !array_search($otherpjplf,$arr_checked_plfs) ){                          
                               read_xml($otherpjplf,$Projectflag,$each_project_name_other);
                               $arr_checked_plfs[$index++]=$otherpjplf;
                           }
                     }
                     break;
                 }
              }
              if($Projectflag==false){                  
                     read_xml($each_commit_plf,$Projectflag,'');
              }   

           }
       }
    }
}
# add by xiaoying.huang 20171216 send wentai change branch
function get_wentaiConfig($patch_delivery_path,$ss){
	
    $arr_prj = array();
	$prj_list_file_path = $patch_delivery_path."../conf/wentaiconfig";
	if(file_exists($prj_list_file_path)){
       $handle = fopen("$prj_list_file_path","r");
       $lines = fread($handle,filesize($prj_list_file_path));
       $gitstrnum=strstr($lines,$ss);
       if($gitstrnum!=false){
	       #$gitstrnum=strpos($lines,$ss);
           $gitstr=trim(substr($gitstrnum,strlen($ss),-1));
           $gitstr=trim(strstr($gitstr,"####",TRUE));
           $arr_prj=explode("\n",$gitstr);
       }
       fclose($handle);
    }
    return $arr_prj;
}
# end by xiaoying.huang 20171216

# add by xiaoying.huang 20180315 check custpack_app_odex_list
function check_custpack_app_odex_list($arr_add_file){
	$find_mkfile=exec("find -name 'custpack_app_odex_list.mk'");
	$gask="N";
	if(!empty($find_mkfile)){
        $keys = array();
        $keys = array_keys($arr_add_file);
        for($i=0;$i<count($keys);$i++){
            $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
            if(count($tmparray)>1 && empty($tmparray[0])){
                if(preg_match("/custpack_app_list.mk/", $keys[$i])){
					$gask="Y";   
                }
            }
        }
        if($gask=="Y")
        {
			 writeline("If you want to modifie 'custpack_app_odex_list.mk' by yourself now? \n1.'Y':you go to modifile now,and this will exits the scm_tools!\n2.'N':you have been modifie this file ,and this go next! \nEnter Y|N:"); 
                    $ad = read_line();
                    if($ad == "Y" || $ad == "y"){
						writeline("===============================\n please go to modified as follow files by yourselef now:\n"); 
						system("find -name 'custpack_app_odex_list.mk'"); 
						writeline("===============================\n");
						exit;
				    }
	    }
    }
}
# end by xiaoying.huang 20180315

#add by xiaoying.huang 20180531
function getCommitInfo($author_name,$git_path_name,$server){
	    $CommitArr = array();
        $gCommitdate=exec("git log -1 | head -n 3");
        $currentCommitId =exec("git log -1 | head -n 1 | awk '{print $2}'");
        $currentCommitDate =substr($gCommitdate,strlen("Date:"),-5);
        $remote_name=exec("git remote -v | tail -1 | awk -F' ' '{print $1}'");
	    $changeIdLine=system("git ls-remote  $remote_name  refs/changes/* | grep '$currentCommitId' | awk '{print $2}'");
	    if(empty($changeIdLine)){
		   $changeIdLine=system("git ls-remote ssh://".$author_name."@".$server.":29418/".$git_path_name."  refs/changes/* | grep '$currentCommitId' | awk '{print $2}'");
	    }
	    echo "==changeIdLine==$changeIdLine====";
	    list($refs,$changes,$index,$changeId,$num)=explode('/',$changeIdLine);
	    if(empty($changeId)){
	      return $CommitArr ;
        }
        $CommitArr[0]=$currentCommitId;
        $CommitArr[1]=$changeId;
        $CommitArr[2]=trim($currentCommitDate);
        return $CommitArr ;
}
#end by xiaoying.huang 20180531

//begin add by xiaoying.huang 20180517
function addgitpushID($bugid,$gitname,$CommitArr,$author_name){
	   # php 7.0 no supper mysql
       #$dotp_conn = mysql_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
       #mysql_select_db($db="dotproject",$dotp_conn);
       #$selectsql = "INSERT INTO dotp_patch_deliver (defect,git_name,commit_id,author,changeID,TS) VALUES ('$bugid','$gitname','$CommitArr[0]','$author_name','$CommitArr[1]','$CommitArr[2]')";
       #$result = mysql_query($selectsql);
       $dotp_conn = mysqli_connect("10.92.35.20", $db_user="INT_PATCH", $db_pass="Aa123456");
       mysqli_select_db($dotp_conn,"dotproject");
       $selectsql = "INSERT INTO dotp_patch_deliver (defect,git_name,commit_id,author,changeID,TS) VALUES ('$bugid','$gitname','$CommitArr[0]','$author_name','$CommitArr[1]','$CommitArr[2]')";
       $result = mysqli_query($dotp_conn,$selectsql);
       print $selectsql;
       if ($result){
           print "\n-------INSERT INTO SMARTTASK OK --------";
       }else{
		   print "\n-------INSERT INTO SMARTTASK error --------";
	   }
}
//end add by xiaoying.huang 20180517
//add by xiaoying updateRootCause to simplex 20181109
function update_simplex_RootCause($XrNo,$rootcause,$root_cause_detail,$bug_category,$tct_bug_category,$is_rom){
	
    $HOST_URL = "http://10.92.35.176/pms";
	$UPLOAD_URL = $HOST_URL."/project/xrinfo/0/updateRootCause";
	$post_data = array('numberList'=>$XrNo,'root_cause'=>$rootcause, 'root_cause_detail'=>$root_cause_detail,'bug_category'=>$bug_category,'tct_bug_category'=>$tct_bug_category,'is_rom'=>$is_rom); 
	$post_data = json_encode($post_data);
	print $post_data ;
	$post_data = array('jsonContent' => $post_data);
    $result=upload_exception_to_simplex($UPLOAD_URL, $post_data);
	print "result====$result";
	return $result;
}
//end by xiaoying updateRootCause to simplex 20181109
//end


# add note email eduse files by xiaoying.huang 2018-11-19
function check_file_note_mail_list($gitname,$arr_add_file){
    global $patch_delivery_path;
	$notefile_list=get_confreview($patch_delivery_path,'notefileconfig');
	$gask="N";
	$keys = array();
    $keys = array_keys($arr_add_file);
    for($i=0;$i<count($keys);$i++){
         $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
         if(count($tmparray)>1 && empty($tmparray[0])){
			    $commitfile = $gitname."/".$keys[$i];
			    if(in_array($commitfile,$notefile_list)){
			        $gask="Y"; 
				    return $gask;  
		        }
         }
     }
	return $gask;
}
function Get_select_file_list($arr_add_file){
	$selectfiles =array();
	$keys = array();
    $keys = array_keys($arr_add_file);
    for($i=0;$i<count($keys);$i++){
         $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
         if(count($tmparray)>1 && empty($tmparray[0])){
			    $selectfiles[]=$keys[$i];
         }
     }
	return $selectfiles;
}
# end note email eduse files by xiaoying.huang 2018-11-19

# add by xiaoying.huang 20190124 qcom check manifest modified
function check_qcom_manifest($gitname,$arr_add_file){
	$keys = array();
    $keys = array_keys($arr_add_file);  
    for($i=0;$i<count($keys);$i++){
         $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
         if(count($tmparray)>1 && empty($tmparray[0])){
			    $commitfile = $gitname."/".$keys[$i];
			    #echo "=$i====$commitfile===";
			    if($commitfile=='sdm450/device/qcom/msm8937_32/manifest.xml'||$commitfile=='sdm450/device/qcom/msm8937_64/manifest.xml'){
			        writeline(" \033[32;1m----------------***********--------------\nIs this compatible with OTA P and P both versions by your modified? \033[0m Y|N:\n"); 
                    $ad = read_line();
				    while(!( $ad == "Y" || $ad == "y" || $ad =='N' || $ad =='n' )){
                        writeline("Enter Y|N:");
                        $ad = read_line();
                    }
                    if($ad == "Y" || $ad == "y"){
						return "1";
				    }else{
						print("\033[31;1m pls send email to SPM\INT team to ask about your modified in device/qcom/msm8937_*/manifest.xml \033[0m\n");
						exit;
					}
		        }
         }
     }
     return "0";  
}
# end by xiaoying.huang 20190125

# begin add by xiaoying.huang check rom file 20190416
function check_file_about_rom_submit($arr_add_file,$branchname){
        global $patch_delivery_path;
        $keys = array();
        $aa = array();
        $selectfiles = array();
        $ROM_File = array("aosp-q-v3.0-tctrom"=>"TCTROM-Q-V3.0-dev_list.txt","mtk6761-morgan4g-q-v3.0-tctrom"=> "TCTROM-Q-V3.0-dev_list.txt","q6150-t1-q-v3.0-tctrom"=> "TCTROM-Q-V3.0-dev_qualcom.txt");
        if (array_key_exists($branchname,$ROM_File))
            $arr_list = get_confreview($patch_delivery_path,"rom_file/$ROM_File[$branchname]");  
        else
            return $aa;
        $repo_path=exec("$patch_delivery_path"."find_repo_path.sh");
        $current_path=exec('pwd');
        $git_path=substr($current_path,strlen($repo_path)+1);
        $keys = array_keys($arr_add_file); 
        for($i=0;$i<count($keys);$i++){
           $tmparray = explode("select_",$arr_add_file[$keys[$i]]);
           if(count($tmparray)>1 && empty($tmparray[0])){
			    $commitfile = $git_path."/".$keys[$i];
			    $selectfiles[]= $git_path."/".$keys[$i];
		        }
         }
         $aa = array_intersect($selectfiles,$arr_list);
         #print_r($aa);
          for($i=0;$i<count($aa);$i++){
			 if ($i ==0)
			      writeline("\n \033[32;1m----------------***********------\nPlease remember to modify the follow files for TCTROM-Q-V3.0-dev branch by yourself later ,and make sure build ROM success!! \033[0m\033[31;1m\n"); 
             writeline("$i.".$aa[$i]."\n");
          }
          writeline("\n----------------***********---------------");
          writeline("\033[0m");
}

#end by xiaoying.huang  check rom file 20190416
?>
