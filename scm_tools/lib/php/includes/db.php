<?php
$db_server = "172.24.61.199:3306";
$db_user = "scm_tools";
$db_pass = "SCM_TOOLS123!";
$db_database = "bugs";

$g_dbhandle = mysql_connect($db_server, $db_user, $db_pass);
mysql_select_db($db_database,$g_dbhandle);
?>
