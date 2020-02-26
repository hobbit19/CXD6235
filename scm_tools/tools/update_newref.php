#!/usr/bin/php -q 
<?php
/*
 *  script name:update_newref.php
 *  (Li Yuanhua, 2010-02-22)
 *
 *  The SCM Tool is for android team members to update the field new ref of PR in bugzilla
 *  a batch of PRs once. If there is no php5 on client machine, client should install
 *  it with command: sudo apt-get install php5-cli and sudo apt-get install php5-mysql.
 *  usage:
 *  ./update_newref.php <new_ref> -f  prlist.txt [-c ]
 *
 *  #add new ref
 *  ./update_newref.php v1.2.1 -f prlist.txt
 *
 *  #remove wrong ref
 *  ./update_newref.php v1.2.1 -f prlist.txt -c
 */

 
require_once("../lib/php/includes/smtp.inc.php");
require_once("../lib/php/includes/db.php");
require_once("../lib/php/includes/class.phpmailer.php");
require_once("../lib/php/includes/class.smtp.php");
 
function get_bug_status($bugid, &$status, &$target_milestone){
    
    $query = "SELECT bug_status,target_milestone FROM bugs WHERE bug_id=$bugid";
    
    $result = mysql_query($query);

    // Check result
    // This shows the actual query sent to MySQL, and the error. Useful for debugging.
    if (!$result) {
        $message  = 'Invalid query: ' . mysql_error() . "\n";
        $message .= 'Whole query: ' . $query;
        die($message);
    }
    
    // Use result
    // Attempting to print $result won't allow access to information in the resource
    // One of the mysql result functions must be used
    // See also mysql_result(), mysql_fetch_array(), mysql_fetch_row(), etc.
    while ($row = mysql_fetch_assoc($result)) {
        $status = $row['bug_status'];
        $target_milestone = $row['target_milestone'];
        #echo $row['bug_status'];
        #echo $row['target_milestone'];
    }
    
    // Free the resources associated with the result set
    // This is done automatically at the end of the script
    mysql_free_result($result);
}

function get_user_email($user){
    
    $login_name = "";
    
    $query = "SELECT login_name FROM profiles WHERE login='$user'";
    
    $result = mysql_query($query);

    // Check result
    // This shows the actual query sent to MySQL, and the error. Useful for debugging.
    if (!$result) {
        $message  = 'Invalid query: ' . mysql_error() . "\n";
        $message .= 'Whole query: ' . $query;
        die($message);
    }
    
    // Use result
    // Attempting to print $result won't allow access to information in the resource
    // One of the mysql result functions must be used
    // See also mysql_result(), mysql_fetch_array(), mysql_fetch_row(), etc.
    while ($row = mysql_fetch_assoc($result)) {
        $login_name = $row['login_name'];
        #echo $row['login_name'];
    }
    
    // Free the resources associated with the result set
    // This is done automatically at the end of the script
    mysql_free_result($result);
    
    return $login_name;
}


function sendmail_to_bugzilla($from,$to,$subject,$body,$cc=""){
    
    /*$smtp=new SMTPMAIL;
    
    if(!$smtp->send_smtp_mail($to,$subject,$body,$cc,$from)){
	    echo "Error in sending mail!<BR>Error: ".$smtp->error;
	    exit;
	}*/
    $mail = new PHPMailer();
    $mail->From = $from;                                     
    #$mail->FromName = "yhli";                    
        
    $mail->AddAddress($to);     
  
    $mail->Subject = $subject;
    $mail->Body    = $body; 
    if(!$mail->Send()){
        echo "Mailer Error: " . $mail->ErrorInfo;      
    }else{
        #echo "Mail sent succesfully!";
    }
	
	
}

  
if ($argc <> 4 && $argc <> 5){
    print "usage:update_newref.php <new ref name> -f <pr list file> [-c]  \n";
    print "option:-f put all bug numbers into the file, and each bug number occupies one line \n";
    print "option:[-c] delete wrong ref  \n";
    exit;
}elseif($argv[2] != "-f"){
    print "usage:update_newref.php <new ref name> -f <pr list file> [-c]  \n";
    print "option:-f put all bug numbers into the file, and each bug number occupies one line \n";
    print "option:[-c] delete wrong ref  \n";
    exit;
}elseif($argc == 5 && $argv[4] != "-c"){
    print "usage:update_newref.php <new ref name> -f <pr list file> [-c]  \n";
    print "option:-f put all bug numbers into the file, and each bug number occupies one line \n";
    print "option:[-c] delete wrong ref  \n";
    exit;
}


//get user name form environment variables;
$user= getenv("USER");


$from = get_user_email($user);
if($from == ""){
    print "can not find current user in bugzilla database!\n";
    exit;
}

$to = 'root';
$release = $argv[1];

if(!is_file($argv[3])){
    print "bug list file:$argv[3] doesn't exsit!\n";
    exit;
}
    
$buglist = fopen($argv[3], "r");



while (!feof($buglist)){
       $line = fgets($buglist);
       $bug_status ="";
       $new_ref ="";
       $bug_id = trim($line);
       if($bug_id != ""){
           get_bug_status($bug_id,$bug_status,$new_ref);
       }else{
           continue;
       }
        
       #delete wrong release ref
       if ($argc == 5){
        
            $refs = preg_split("/ /",$new_ref);
            $new_ref="";
            foreach ($refs as $ref){
                #print "ref=".$ref."\n";
    		    if ($ref != $release){
    		    	$new_ref=$new_ref . $ref;
    		    	#$new_ref="cctest_scm_training_p2.0-6";
    		    }
    		 }
    		 if ($new_ref == ""){
    		 	  $new_ref="unknown";
    		 }
    		 #print "new_ref=".$new_ref."\n";
        }else{
            $new_ref = $new_ref . " " . $release;
        }
        
       
#       if ($bug_status != 'DELIVERED' && $bug_status != 'RESOLVED'){
       if ($bug_status != 'DELIVERED' && $bug_status != 'VERIFIED_SW'){
#           print "this pr's satus is not DELIVERED or RESOLVED";
           print $bug_id. ":this pr's satus is not DELIVERED or VERIFIED_SW.\n";
#       }elseif($bug_status == 'RESOLVED' && $argc != 5){
       }elseif($bug_status == 'VERIFIED_SW' && $argc != 5){
#           print "this pr's satus is RESOLVED, status will be updated to DELIVERED by this tool.\n";
           print $bug_id. ":this pr's satus is VERIFIED_SW, status will be updated to DELIVERED by this tool.\n";
           
           $subject = "[Bug $bug_id]";
           $body = "@bug_status = DELIVERED\n";
           $body = $body."@target_milestone = $new_ref\n\n";
           $body = $body."The new ref is updated by $user with update_newref";
           #sendmail to bugzilla server 
           #print $body;
           sendmail_to_bugzilla($from,$to,$subject,$body);
       }else{
           $subject = "[Bug $bug_id]";
           $body = "@bug_status = DELIVERED\n";
           $body = $body."@target_milestone = $new_ref\n\n";
           $body = $body."The new ref is updated by $user with update_newref";
           #sendmail to bugzilla server 
           #print $body;
           sendmail_to_bugzilla($from,$to,$subject,$body);
       }
       #echo $line;
    }
fclose($buglist);

if ($g_dbhandle != null)
    mysql_close($g_dbhandle);
    
?>
