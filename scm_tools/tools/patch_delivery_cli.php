#!/usr/bin/php -q 
<?php
require_once('inc/conf.inc');
require_once('inc/common.inc');
$codepath = getcwd();
if (!is_git_root()){
    print "\033[31;1m Please go to git repository to finish your commit.\033[0m\n";
    exit;
}
$gitbranch= exec("git branch | grep '* '|sed -e 's/* //'");
$down_repo_branch = down_repo_branch();
$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));
$arr_ningbo_branches=get_confreview($patch_delivery_path,"NBbranch");
$is_nb_branch=false;
#print "gitbranch $gitbranch\n";
#print "down_repo_branch $down_repo_branch\n";
foreach($arr_ningbo_branches as $key=>$value){
#print "key: --- value: $key $value\n";
 if(trim($value) == trim($gitbranch) | trim($value) == trim($down_repo_branch)){
$is_nb_branch=true;}
}
if($is_nb_branch){
print "Go NB patch delivery\n";
chdir($patch_delivery_path);
system("pwd");
system("python patch_delivery_cli_gui_NB.py");
}
else{
print "Go Huizhou patch delivery\n";
$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));

chdir($patch_delivery_path);
system("pwd");

print "--------start update scm_tools!-----------\n";
system("git reset --hard HEAD && git clean -df && git pull origin master");
print "--------end update scm_tools!-------------\n";
chdir($codepath);
system("pwd");

print "--------start to do patch_deliver-------------\n";
// start add by shuangyan.he 20160322 
if(count($argv)>1){
	$txt_fill = $argv[1];
	if($txt_fill == "-t"){
		print $patch_delivery_path."patch_delivery_cli_main.php -t\n";
		passthru("php"." ".$patch_delivery_path."patch_delivery_cli_main.php -t");
		}
}
// end add by shuangyan.he 20160322
else{
	print $patch_delivery_path."patch_delivery_cli_main.php\n";
	passthru("php"." ".$patch_delivery_path."patch_delivery_cli_main.php");
}
}
print "\n--------end to do patch_deliver-------------\n";

?>
