<?php

	class SMTPMAIL
	{
		var $host="";
		var $port=25;
		var $error;
		var $state;
		var $con=null;
		var $greets="";
		
		function SMTPMAIL()
		{
			$this->host="172.24.61.83";
			$this->port=25;
			$this->state="DISCONNECTED";
		}
		function set_host($host)
		{
			$this->host=$host;
		}
		function set_port($port=25)
		{
			$this->port=$port;
		}
		function error()
		{
			return $this->error;
		}
		function connect($host="",$port=25)
		{
			if(!empty($host))
				$this->host($host);
			$this->port=$port;
			if($this->state!="DISCONNECTED")
			{
				$this->error="Error : connection already open.";
				return false;
			}
			
			$this->con=@fsockopen($this->host,$this->port,$errno,$errstr);
			if(!$this->con)
			{
				$this->error="Error($errno):$errstr";
				return false;
			}
			$this->state="CONNECTED";
			$this->greets=$this->get_line();
			return true;
		}
		function send_smtp_mail($to,$subject,$data,$cc="",$from='HARISH')
		{
			$ret=$this->connect();
			if(!$ret)
				return $ret;
			$this->put_line("MAIL FROM: $from");
			$response=$this->get_line();
			if(intval(strtok($response," "))!=250)
			{
				$this->error=strtok($response,"\r\n");
				return false;
			}
			$to_err=preg_split("/[,;]/",$to);
			foreach($to_err as $mailto)
			{
				$this->put_line("RCPT TO: $mailto");
				$response=$this->get_line();
				if(intval(strtok($response," "))!=250)
				{
					$this->error=strtok($response,"\r\n");
					return false;
				}
			}
			if(!empty($cc))
			{
				$to_err=preg_split("/[,;]/",$cc);
				foreach($to_err as $mailto)
				{
					$this->put_line("RCPT TO: $mailto");
					$response=$this->get_line();
					if(intval(strtok($response," "))!=250)
					{
						$this->error=strtok($response,"\r\n");
						return false;
					}
				}
			}
			$this->put_line("DATA");
			$response=$this->get_line();
			if(intval(strtok($response," "))!=354)
			{
				$this->error=strtok($response,"\r\n");
				return false;
			}
			$this->put_line("TO: $to");
			$this->put_line("SUBJECT: $subject");
			$this->put_line($data);
			$this->put_line(".");
			$response=$this->get_line();
			if(intval(strtok($response," "))!=250)
			{
				$this->error=strtok($response,"\r\n");
				return false;
			}
			$this->close();
			return true;
		}
		// This function is used to get response line from server
		function get_line()
		{
			while(!feof($this->con))
			{
				$line.=fgets($this->con);
				if(strlen($line)>=2 && substr($line,-2)=="\r\n")
					return(substr($line,0,-2));
			}
		}
		////This functiuon is to retrive the full response message from server

		////This functiuon is to send the command to server
		function put_line($msg="")
		{
			return @fputs($this->con,"$msg\r\n");
		}
		
		function close()		
		{
			@fclose($this->con);
			$this->con=null;
			$this->state="DISCONNECTED";
		}
	}

?>
