#!/usr/bin/php -q 
<?php

$codepath = getcwd();

$patch_delivery_name=exec("basename $argv[0]");
$patch_delivery_path = substr($argv[0],0,-strlen($patch_delivery_name));

chdir($patch_delivery_path);
system("pwd");

print "--------start update scm_tools!-----------\n";
system("git reset --hard HEAD && git pull origin master");
print "--------end update scm_tools!-------------\n";
chdir($codepath);
system("pwd");

print "--------start to do patch_deliver-------------\n";
print $patch_delivery_path."patch_delivery_cli_odm.php\n";
passthru($patch_delivery_path."patch_delivery_cli_odm.php");
print "--------end to do patch_deliver-------------\n";



?>
