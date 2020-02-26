#!/usr/bin/python
#coding=utf-8
#self upload PAI APK
#
#https://partner.android.com/
#
# kou.niu@tcl.com
# hzhzgoogle
#

#status:approved Edison product:5008Y
#https://partner.android.com/#device-search:query=status:approved%20Edison%20product:5008Y&page=0
#https://partner.android.com/?pli=1#device-search:query=Edison&page=0
#https://partner.android.com/#device:0E8ADC91

#https://partner.android.com/#product:75C4B040
#https://partner.android.com/?pli=1#product:2518C300
import sys,time,os,re
import commands
sys.path.append("/local/int_jenkins/mtk_patch")
grepOwner = __import__("grepOwner")
login_internet = getattr(grepOwner,'login_internet')
sys.path.append('/local/int_jenkins/fxd')
import sendkeys
xlrd = __import__('xlrd')

class google(login_internet):
	
	def __init__(self,url="https://partner.android.com/"):
		time.sleep(5)
		super(google,self).__init__(url)
		self.driver.maximize_window()
		self.tr_num = ''
		self.project = ''
		self.tr_num_sum = ''
		time.sleep(5)
		self.carrier_dlist = self.get_xml_value("/local/int_jenkins/PAI/carrier.xml")
		self.country_dlist = self.get_xml_value("/local/int_jenkins/PAI/country.xml")
	def in_dlist(self,_name,dlist=[]):
		name = []
		if _name in dlist:
			return _name
		else:
			for member in dlist:
				if member.find(_name)!=-1:
					name.append(member)
		if len(name)>=1:
			return name[0]
		else:
			print "more than one name in dlist"
			sys.exit(1)
					
	def login(self,user="kou.niu@tcl.com",password="hzgoogle.123"):
		print "login google"
		super(google,self).login([user,''],["identifier","#identifierNext > content:nth-child(3) > span:nth-child(1)"],["name","css"])
		super(google,self).login([password,''],["password","#passwordNext > content:nth-child(3) > span:nth-child(1)"],["name","css"])
	def sw2device(self,device=''):
		self.get_url("https://partner.android.com/?pli=1#device-search:query=%s&page=0"%device)
	def open_all(self):
		for n in xrange(1,5):
			builds = self.get_object(["table.MNVX4SC-a-yb:nth-child(%s)" % n])
			builds[0].click()
			self.actionchains.move_to_element(builds[0])
			self.actionchains.perform()
			self.driver.implicitly_wait(30)
		

	def product(self):
		#product
		product = self.get_object(["table.MNVX4SC-a-yb:nth-child(4)"])
		product[0].click()
		print "product.rect",product[0].rect
		#self.actionchains.move_to_element(product[0])
		#self.actionchains.perform()
		self.driver.implicitly_wait(30)
		#table_module = \
		"""
		<table>
			<tr>
				<div>
					<div>
						text
					</div>
				</div>
			</tr>
		</table>
		"""
	def upload(self,apk_name,carriers,countries):
		product_name = self.getproductname()
		fingerprint = self.getfingerprint()
		matchs = re.findall(r"\[(\w+)\]",','.join(carriers))
		match = matchs[0] if matchs else ''
		name = product_name + '_' + match
		countries = [ match ] if len(countries)==1 and not countries[0] else countries
		print fingerprint
		#button create
		self.button("button.gwt-Button:nth-child(4)")
		self.driver.implicitly_wait(30)
		self.name_fingerprint(name,fingerprint)
		self.textarea(countries,carriers)
		self.select_apk(apk_name)
		time.sleep(10)
		#self.bt_cancel()
		self.bt_save()
		
	def main(self,project,apk_name,carriers,countries,tr_num=0,tr_num_sum=5):
		self.sw2device(project)
		self.product()
		time.sleep(10)
		table_trs = self.get_object(["table.MNVX4SC-a-yb:nth-child(4) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > div:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(3) > tr"])
		print "len(table_trs)",len(table_trs)
		if tr_num > len(table_trs):
			print "tr_num > len(table_trs) return"
			return
		tr = table_trs[tr_num] if table_trs else ''
		if tr:
			self.ac_click(tr)
			self.upload(apk_name,carriers,countries)
			self.driver.implicitly_wait(30)
			time.sleep(10)
			
	def getproductname(self):
		return self.get_object([".MNVX4SC-d-f"])[0].text.encode("utf-8") if self.get_object([".MNVX4SC-d-f"]) else ''
	def getfingerprint(self):
		fingerprint = []
		divs =  self.get_object(["table.MNVX4SC-j-y:nth-child(2) > tbody:nth-child(3) > tr:nth-child(1) > td:nth-child(3) > div:nth-child(1) > div"])
		print "divs",divs
		for div in  divs:
			print div.text.encode("utf-8")
			fingerprint.append(div.text.encode("utf-8"))
		return "".join(fingerprint)
				
	def bt_create(self):
		create = self.get_object(["button.gwt-Button:nth-child(4)"])
		if create:
			print create
			#print dir(create[0])
			print "is_enabled",create[0].is_enabled
			self.driver.implicitly_wait(30)
			time.sleep(3)
			print "is_enabled",create[0].is_enabled
			print "create click"
			self.ac_click(create[0])
	def bt_save(self):
		print "button save"
		save = self.get_object(["button.gwt-Button:nth-child(8)"])
		print save
		if save:
			self.driver.implicitly_wait(30)
			save[0].click()
	def bt_cancel(self):
		print "button cancel"
		cancel = self.get_object(["button.gwt-Button:nth-child(11)"])
		print cancel
		if cancel:
			self.driver.implicitly_wait(30)
			cancel[0].click()
	def bt_publish(self):
		print "button cancel"
		publish = self.get_object([".MNVX4SC-a-D"])
		print publish
		if publish:
			self.driver.implicitly_wait(30)
			publish[0].click()
	def name_fingerprint(self,name="Edison test",fingerprint="TCL/5008Y_EEA/Edison:8.1.0/O11019/vBV25-0:user/release-keys",ro_oem_key=""):
		inputlist = self.get_object(["//input"],'xpath')
		print len(inputlist)
		for i in xrange(len(inputlist)):
			self.driver.implicitly_wait(30)
			print inputlist[i]
			print inputlist[i].get_attribute('class')
			print inputlist[i].get_attribute('accept')
			print inputlist[i].rect
			print dir(inputlist[i])
			if inputlist[i].rect['height'] == 0.0 or inputlist[i].rect['width'] == 0.0:
				continue
			#Name
			if inputlist[i].get_attribute('class') == 'gwt-TextBox MNVX4SC-a-G MNVX4SC-h-g':
				inputlist[i].click()
				inputlist[i].clear()
				inputlist[i].send_keys(name)
			#fingerprint
			if inputlist[i].get_attribute('class') == 'gwt-SuggestBox MNVX4SC-a-G MNVX4SC-h-d':
				inputlist[i].click()
				inputlist[i].clear()
				inputlist[i].send_keys(fingerprint)
				self.driver.implicitly_wait(30)
			#ro.oem.key1
			if inputlist[i].get_attribute('class') == 'gwt-TextBox MNVX4SC-a-G MNVX4SC-h-r':
				inputlist[i].click()
				inputlist[i].clear()
				inputlist[i].send_keys(ro_oem_key)
			#select
			#if inputlist[i].get_attribute('accept') == '.apk':
				#print "send apk"
				#inputlist[i].click()
				#inputlist[i].send_keys("/tmp/cxd/GooglePackageInstaller.apk")
	def geturl(self,url="https://partner.android.com/?pli=1#product:2518C300"):
		self.driver.implicitly_wait(30)
		self.driver.get(url)
		product_name = self.getproductname()
		fingerprint = self.getfingerprint()
		print fingerprint
		#button create
		self.button("button.gwt-Button:nth-child(4)")
		#self.driver.switch_to.frame("com.google.wireless.android.apfe.Apfe")
		self.driver.implicitly_wait(30)
		
		
	def select_apk(self,apk="/tmp/cxd/GooglePackageInstaller.apk"):
		self.driver.implicitly_wait(30)
		select_bt = self.get_object(["//a[contains(text(),'Select')]"],"xpath")
		print "select_bt",select_bt
		if select_bt:
			print select_bt[0]
			self.driver.implicitly_wait(30)
			select_bt[0].click()
			time.sleep(3)
			sendkeys.sendkeys(apk)
			sendkeys.sendkeys("\n")
			self.driver.implicitly_wait(30)
			check = self.get_object(["//span[id='\:3']"],"xpath")
			print "check",check
			if check:
				print check[0]
				print "upload success"
			
	def textarea(self,countries=[""],carriers=["[001] Telenor GSM - services in aircraft - 1766"],hardware_ids=[""]):
		textareas = self.get_object(["//textarea"],"xpath")
		self.driver.implicitly_wait(30)
		print textareas,len(textareas)
		country_area = textareas[0]
		carrier_area = textareas[1]
		hardware_ids_area = textareas[2]
		_countries = []
		_carriers = []
		print "countries",countries
		for country in countries:
			print "country",country
			if country == "-":
				_countries.append(country)
			else:
				_countries.append(self.in_dlist(country,self.country_dlist))
		print "carriers",carriers
		for carrier in carriers:
			print "carrier",carrier
			_carriers.append(self.in_dlist(carrier,self.carrier_dlist))
		countries_text = "\n".join(_countries)
		carriers_text = "\n".join(_carriers)
		hardware_ids_text = "\n".join(hardware_ids)
		texts = [countries_text,carriers_text,hardware_ids_text]
		areas = [country_area,carrier_area,hardware_ids_area]
		for i in xrange(0,3):
			areas[i].click()
			areas[i].clear()
			areas[i].send_keys(texts[i])
class xls(object):
	def __init__(self):
		self.project_config = {}
		self.project_list = []
		self.project_dict = {}
	def get_config_xls(self,wb_file="/local/int_jenkins/PAI/PAI_config.xlsx",sheet_name="all"):

		xls_vector = ['Carrier','model','trigger','Apk_Name','Perso','position','Package_Name','Class_Name']
		key_vector = ['model','Package_Name']
		key_num_vector = []
		for key in key_vector:
			assert key in xls_vector,"key must in xls_vector"
			key_num_vector.append(xls_vector.index(key))
		wb = xlrd.open_workbook(wb_file)
		all_project = wb.sheet_by_name(sheet_name)
		for row in xrange(0,all_project.nrows):
			key_value=[]
			for key_num in key_num_vector:
				key_value.append(all_project.cell(row,key_num).value.strip())
			key_value = tuple(key_value)
			if key_value not in self.project_config:
				self.project_config[key_value] = {}
			for col in xrange(len(xls_vector)):
				cell_value = all_project.cell(row,col).value.strip().replace('\u','')
				self.project_config[key_value][xls_vector[col]]=cell_value
		print self.project_config
	def get_project_name(self,local_path="/local/pai"):
		assert os.path.exists(local_path),"%s do no exists!" % local_path
		os.chdir(local_path)
		output = commands.getoutput("find -name preload.apk")
		if output and len(output) >= 1:
			outputs = output.split('\n')
			for output in outputs:
				assert os.path.exists(output),"%s do no exists!" % output
				parent_path = os.path.dirname(output)
				parent_file = os.path.basename(parent_path)
				self.project_list.append(parent_file)
		print self.project_list
	def get_package_name(self,local_path="/local/pai/"):
		for project in self.project_list:
			apk_path = local_path + project + "/preload.apk"
			assert os.path.exists(apk_path),"%s do no exists!" % apk_path
			print "CMD::","/local/int_jenkins/fxd/aapt dumpsys xmltree '%s' res/xml/default_layout.xml" % apk_path
			strs = commands.getoutput("/local/int_jenkins/fxd/aapt dumpsys xmltree '%s' res/xml/default_layout.xml" % apk_path)
			print "strs",strs
			apk_name = self.get_attri_value(strs)
			print "apk_name",apk_name
			if apk_name and (project,apk_name) in self.project_config:
				carriers = self.project_config[(project,apk_name)]['Carrier'].replace(u"–","-").split(",")
				countries = [""]
				self.project_dict[(project,apk_name)] = [project,apk_path,carriers,countries]
		print self.project_dict
	def get_attri_value(self,strs,tag_name="autoinstall",attr="packageName"):
		trees_list = strs.split("E: ")
		value = ''
		for trees in trees_list:
			if trees.find(tag_name) != -1:
				attrs_list = trees.split("A: ")
				for attrs in attrs_list:
					if attrs.find(attr) != -1:
						value = attrs.split()[0].replace(attr,"").replace('"','').replace('=','')
		return value
def main():
	xlxs =xls()
	xlxs.get_config_xls()
	xlxs.get_project_name()
	xlxs.get_package_name()
	project_dict = xlxs.project_dict
	for [project,apk_name,carriers,countries] in project_dict.values():
		#project="Edison"
		#apk_name="/tmp/cxd/preload.apk"
		#carriers=["[001] Telenor GSM - services in aircraft - 1766"]
		#countries=["AC"]
		for i in xrange(0,1):
			gl = google()
			gl.login()
			gl.main(project,apk_name,carriers,countries,i)
			time.sleep(10)
			gl.close()

if __name__ == "__main__":

	main()
