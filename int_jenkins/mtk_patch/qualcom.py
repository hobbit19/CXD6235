#!/usr/bin/python
#coding=utf-8
#job of qualcom
import sys
import os
import time
sys.path.append("/local/int_jenkins/mtk_patch")
grepOwner = __import__("grepOwner")
qualcom = __import__("qualcom")
def main_qualcom():
	login_qualcomm = getattr(grepOwner,'login_qualcomm')
	print dir(login_qualcomm)
	customer_project = ["Watt","Benz","Gauss"]
	spm_list=["yong.zhang.hz@tcl.com","lei.shi.hz@tcl.com","lei.shi.hz@tcl.com"]
	data = []
	qualcomm = login_qualcomm()
	qualcomm.login()
	for project in customer_project:
		#project = "Benz"
		url = 'https://qualcomm-cdmatech-support.my.salesforce.com/search/SearchResults?searchType=1&sen=0&setLast=1&sbstr='+project+'&search=+Go%21+'
		name_col = {'case_number':'2','customer_project':'7','mtime':'6','status':'4'}
		qualcomm.get(url)
		v_data = []
		vector_rules = ['case_number','customer_project','status','subject','mtime','contact_email','href']
		#Action				tcols[0].text.encode('utf-8')==== Edit
		#Case Record Type		tcols[1].text.encode('utf-8')==== Closed(Wireless)
		#Chipset			tcols[2].text.encode('utf-8')==== MSM8953 Pro
		#Status				tcols[3].text.encode('utf-8')==== Closed
		#Pirioty			tcols[4].text.encode('utf-8')==== 1 - Critical
		#Subject			tcols[5].text.encode('utf-8')==== [TCL_LongNa][Mercury][BT] Failed to pair with Mercedes-Benz car kit, error unable to authorize
		#Account Name			tcols[6].text.encode('utf-8')==== Huizhou TCL Mobile 	Communication Co., Ltd.
		#Custumer Project		tcols[7].text.encode('utf-8')==== Mercury
		#Last Modified Date/Time	tcols[8].text.encode('utf-8')==== 3/24/2017 1:55 AM
		#Date/Time Opened		tcols[9].text.encode('utf-8')==== 3/13/2017 4:59 AM
		data_name_col={'customer_project':7,'status':3,'subject':5,'mtime':8}
		qualcomm.get_row_data(v_data,data_name_col,vector_rules)
		#Action				====tcols[0].text.encode('utf-8')==== Edit
		#Case Number			====tcols[1].text.encode('utf-8')==== 02853995
		#Subject			====tcols[2].text.encode('utf-8')==== [TCL_LongNa][Mercury][BT] Failed to pair with Mercedes-Benz car kit, error unable to authorize
		#Custumer Project		====tcols[3].text.encode('utf-8')==== Mercury
		linkdata_name_col = {'case_number':1}
		qualcomm.get_row_linkdata(v_data,linkdata_name_col,vector_rules)
		qualcomm.get_row_propdata(v_data)
		qualcomm.get_detail_data(v_data,vector_rules)
		v_data = qualcomm.grep_vkey_value(v_data,'customer_project',project)
		time.sleep(1)
		for v_d in v_data:
			data.append(v_d)
	qualcomm.close()
	print data
	main_send_mail(data,vector_rules,spm_list)

def main_send_mail(v_data={},
		vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],
		spm_list=["yong.zhang.hz@tcl.com","lei.shi.hz@tcl.com","lei.shi.hz@tcl.com"],
		customer_project= ["Watt","Benz","Gauss"],
		cclist=['xiaodan.cheng@tcl.com','shie.zhao@tcl.com','shuzhong.cui.hz@tcl.com'],
		tolist=['xiaodan.cheng@tcl.com'],
		mail_vector_rules = ['case_number','customer_project','status','subject','mtime','contact_email']):
	excel_filename = "Quacom_status.xls"
	send_mail = getattr(grepOwner,'send_mail')
	smail = send_mail(v_data,vector_rules,customer_project,cclist,tolist,mail_vector_rules)
	hold_data = smail.get_hold_data()
	closed_data = smail.get_closed_data()
	other_data = smail.get_other_data()
	excel = getattr(grepOwner,'create_excel')(excel_filename,mail_vector_rules)
	excel.sheet_content("hold_data",hold_data)
	excel.sheet_content("other_data",other_data)
	excel.sheet_content("closed_data",closed_data)
	excel.save()
	owner_list = smail.get_data2list(hold_data,mail_vector_rules)
	smail.add_cclist(spm_list)
	smail.add_tolist(owner_list)
	smail.get_mail_box_head()
	smail.get_table(hold_data)
	smail.get_mail_box_tail()
	smail.get_msg()
	smail.set_att_file("/tmp/"+excel_filename)
	smail.ssend_mail()

class base_status(object):
	def __init__(self,num='2',status='',text='',filename='/local/release/status.txt',timing = 9):
		self.filename = filename
		self.status = status
		self.text = text
		self.num = num
		self.timing = timing
	def handle(self):
		pass
	def getfile(self):
		text = ''
		if not os.path.exists(self.filename):
			print "no such of file /local/release/status.txt"
			sys.exit(1)
		with open(self.filename,'rb') as f:
			text = f.readline().strip()
		return text
	def setfile(self):
		if not os.path.exists(self.filename):
			print "touch /local/release/status.txt"
			os.system("touch /local/release/status.txt")
		with open(self.filename,'wb') as f:
			f.write(self.status)

class read_status(base_status):
	def handle(self):
		self.text = self.getfile()
class write_status(base_status):
	def handle(self):
		self.setfile()
class num_status(base_status):
	def handle(self):
		if not self.text and int(time.strftime('%H',time.localtime(time.time()))) < self.timing:
			self.status = self.num
		if self.text:
			main_qualcom()
			self.status = str(int(self.text)-1) if int(self.text)>1 else ''
class status_operation(object):
	def __init__(self,operation_list=['read_status','num_status','write_status'],num='2'):
			self.operation_list = operation_list
			self.num = num
			self.operation = ''
			self.status = ''
			self.text = ''
	def check_finish(self):
		return True if len(self.operation_list) > 0 else False
	def __next__(self):
		self.operation_list.remove(self.operation_list[0])
	def oper(self):
		print "self.operation_list[0]",self.operation_list[0]
		self.operation = getattr(qualcom,self.operation_list[0])(self.num,self.status,self.text)
		self.operation.handle()
		self.status = self.operation.status
		self.text = self.operation.text
		self.__next__()
	
if __name__ == '__main__':
	#v_data = [['03614222', 'Watt GL', 'Closed', '[RF Service Lab][Device Bring Up][Bring Up Verification][TCL][WATT_GL] RF Bring Up Verification', '9/1/2018 9:53 PM', 'zhiquan.wen.hz@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qShsZ?srPos=0&srKp=500'], ['03639845', 'Watt GL', 'Closed', 'watt 2G CSE fail', '9/9/2018 6:40 PM', 'shengqiang.fang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qULb8?srPos=1&srKp=500'], ['03607606', 'Watt GL', 'Closed', "[urgent issue]our Watt Project can't Lightup backlight by PMI632_GPIO_06, how to porting it ,Please help\xef\xbc\x81", '8/23/2018 1:02 AM', 'meng.zhang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rLX79?srPos=2&srKp=500'], ['03623655', 'Watt GL', 'Closed-Customer Requested', '[WATT][kernel crash] \xe7\xb3\xbb\xe7\xbb\x9f\xe8\xbf\x9b\xe5\x85\xa5\xe4\xbc\x91\xe7\x9c\xa0\xe5\x90\x8eLCD\xe5\x8f\x91\xe7\x94\x9f\xe7\xa9\xba\xe6\x8c\x87\xe9\x92\x88carsh', '9/2/2018 7:57 AM', 'zihaogu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qTHvW?srPos=3&srKp=500'], ['03616490', 'Watt GL', 'Closed', 'watt modem bring up fail', '8/20/2018 12:03 AM', 'zhiquan.wen.hz@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qSq1n?srPos=4&srKp=500'], ['03617060', 'Watt GL', 'Closed', 'TCL Watt GL PDN review and Capcitance Optimization', '8/27/2018 7:46 PM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qSrJP?srPos=5&srKp=500'], ['03608038', 'Watt GL', 'Closed-Customer Requested', '[Watt][PMI632] BAT_ID ADC \xe8\xaf\xbb\xe5\x8f\x96\xe4\xb8\x8d\xe5\x87\x86', '8/6/2018 7:50 PM', 'zihaogu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rLZWf?srPos=6&srKp=500'], ['03571750', 'Watt GL', 'Closed', 'watt rfc design', '7/30/2018 2:21 AM', 'zhiquan.wen.hz@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000r2QLY?srPos=7&srKp=500'], ['03579891', 'Watt GL', 'Closed-Customer Requested', 'Watt GL PCB review', '7/24/2018 8:53 PM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000r2sbb?srPos=8&srKp=500'], ['03560849', 'Watt GL', 'Closed-Customer Requested', 'TCL Watt GL project DDR review', '7/12/2018 12:09 AM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qcZCG?srPos=9&srKp=500'], ['03560893', 'Watt GL', 'Closed-Customer Requested', 'TCL Watt GL project PDN review', '7/13/2018 12:20 AM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qcZG8?srPos=10&srKp=500'], ['03604051', 'Watt GL', 'Closed', '[RF Service Lab][Device Bring Up][SW Analysis][TCL][WATT GL] RFC and device driver review', '8/17/2018 11:24 PM', 'zhiquan.wen.hz@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rLFgd?srPos=11&srKp=500'], ['03559585', 'Watt GL', 'Closed-Customer Requested', 'TCL-Watt GL project Schematic Review', '7/12/2018 12:15 AM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qcVgb?srPos=12&srKp=500'], ['03479464', 'Watt GL', 'Closed', 'Watt GL sch review', '5/28/2018 7:15 PM', 'yudong.liu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000q7rRB?srPos=13&srKp=500'], ['03491773', 'Watt GL', 'Closed-Customer Requested', 'Watt_MainBoard_SDM450 Review', '5/22/2018 7:05 PM', 'yudong.liu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000q8hb6?srPos=15&srKp=500'], ['03688183', 'Benz', 'Closed-Customer Requested', "[Android P][Benz][Fingerprint TA can't Load succeed]", '10/22/2018 7:49 PM', 'yuduan.xie@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s3iF7?srPos=0&srKp=500'], ['03695451', 'Benz', 'Closed-Customer Requested', '[Android P][Benz][Fingerprint Enroll is crash in last step]', '10/15/2018 7:35 PM', 'yuduan.xie@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s4W1n?srPos=1&srKp=500'], ['03712410', 'Benz', 'Hold-Customer Information Required', '[SDM429][Benz][Media][CTS]test cts case android.media.cts.ResourceManagerTest#testReclaimResourceMixVsSecure fail', '10/22/2018 2:48 AM', 'zhaoyun.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s5i1N?srPos=2&srKp=500'], ['03703236', 'Benz', 'Closed-Pending Your Approval', '[SDM429][Benz][Media][CTS]test cts case android.media.cts.MediaCodecCapabilitiesTest#testGetMaxSupportedInstances fail', '10/17/2018 7:30 PM', 'zhaoyun.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s50NF?srPos=3&srKp=500'], ['03602081', 'Benz', 'Closed', 'TCL benz project PDN review', '9/17/2018 10:53 PM', 'zewei.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000r4O6e?srPos=4&srKp=500'], ['03679397', 'Benz', 'Closed-Pending Your Approval', '[TCL][Benz][SDM429] Camera design review', '10/18/2018 11:52 PM', 'kdeshang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rmCW3?srPos=5&srKp=500'], ['03658065', 'Benz', 'Closed-Customer Requested', 'TCL Benz PDN simulation', '9/12/2018 11:30 PM', 'xianliang.kuang@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rj9NJ?srPos=6&srKp=500'], ['03679129', 'Benz', 'Closed-Customer Requested', '[Benz][Android P][Does not generate the firmware/image]', '9/27/2018 8:21 PM', 'yuduan.xie@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rmBhY?srPos=7&srKp=500'], ['03670320', 'Benz', 'Closed-Customer Requested', '[SDM429][Benz][Audio][VTS][WMAP10]test vts case VtsHalMediaOmxV1_0Host#ComponentHidlTest.EnumeratePortFormat.OMX.qcom.audio.decoder.wma10Pro_audio_decoder.wma_64bit fail', '9/18/2018 3:05 AM', 'zhaoyun.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rlX1r?srPos=8&srKp=500'], ['03689972', 'Benz', 'Closed', '[Android P][Benz][Fingerprint Enroll is crash in last step]', '10/7/2018 3:04 AM', 'yuduan.xie@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s3qVs?srPos=9&srKp=500'], ['03659493', 'Benz', 'Closed-Customer Requested', '[SDM429][Benz][gpsOne]does this platform support BDS/Glonass/Galileo and ABDS/AGlonass/AGalileo', '9/11/2018 12:20 AM', 'zhaoyun.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rjCgi?srPos=10&srKp=500'], ['03666449', 'Benz', 'Hold-Pending Action Item', '[TCL-Benz] TLp034F1 Battery characterization need to help to build', '10/8/2018 8:36 PM', 'guanchen.liu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rlFTn?srPos=11&srKp=500'], ['03606429', 'Benz', 'Closed', 'TCL Benz project DDR review', '8/19/2018 1:13 AM', 'zewei.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rLRCA?srPos=12&srKp=500'], ['03624396', 'Benz', 'Closed', 'TCL-Benz project PCB layout review', '8/31/2018 3:24 AM', 'zewei.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000qTN9u?srPos=14&srKp=500'], ['03593716', 'Benz', 'Closed', 'Benz-SDM429 Schematic review', '8/11/2018 8:49 PM', 'zewei.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000r3mrn?srPos=15&srKp=500'], ['03660938', 'Benz', 'Hold-Pending Action Item', '[TCL-Benz] TLp034F7 Battery characterization need to help to build', '9/18/2018 12:17 AM', 'guanchen.liu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rjLiR?srPos=17&srKp=500'], ['03718991', 'Benz', 'Customer Updated Case', '[BENZ][GCF][36.523-1][10.7.1]UE requested bearer resource allocation accepted by the network / New EPS bearer context', '10/22/2018 8:16 PM', 'quan.luo@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000s6BbT?srPos=20&srKp=500'], ['03661286', 'Gauss', 'Closed-Customer Requested', '[SDM439][Gauss][gpsOne]does this platform support BDS/Glonass/Galileo and ABDS/AGlonass/AGalileo', '9/14/2018 2:15 AM', 'zhaoyun.wu@tcl.com', 'https://qualcomm-cdmatech-support.my.salesforce.com/5003A00000rjMN5?srPos=0&srKp=500']]
	#main_qualcom()
	#main_send_mail(v_data)
	status = status_operation()
	while status.check_finish():
		status.oper()
