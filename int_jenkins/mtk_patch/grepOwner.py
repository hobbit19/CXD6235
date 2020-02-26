#!/usr/bin/python
# -*- coding: utf-8 -*-
import time
import sys
sys.path.append('/local/int_jenkins/fxd')
selenium = __import__('selenium')
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.support import expected_conditions
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support.select import Select
from selenium.common.exceptions import StaleElementReferenceException,NoSuchElementException
from selenium.webdriver.remote.switch_to import SwitchTo
from selenium.webdriver.firefox.webdriver import WebDriver
from selenium.webdriver.firefox.firefox_profile import FirefoxProfile
from selenium.common.exceptions import WebDriverException,NoSuchWindowException
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support.ui import WebDriverWait
import sendkeys
import os,re
import imaplib
import email
import sys,locale
xlrd = __import__('xlrd')
xlwt = __import__('xlwt')
xlutils = __import__('xlutils')
#import xlrd,xlwt
from xlutils.copy import copy
import commands
import smtplib
from email.header import Header
from email.MIMEText import MIMEText
from email.MIMEMultipart import MIMEMultipart
from itertools import product
import xml.dom.minidom

class login_internet(object):
	def __init__(self,url):
		self.url = url
		self.firefox_binary = '/local/int_jenkins/fxd/firefox/firefox-bin'
		self.executable_path = '/local/int_jenkins/fxd/geckodriver'
		self.firefox_profile = FirefoxProfile()
		self.set_profile(self.firefox_profile)
		self.driver = webdriver.Firefox(firefox_profile=self.firefox_profile,firefox_binary=self.firefox_binary,executable_path=self.executable_path)
		self.driver.implicitly_wait(60)
		self.function_dict = \
		{
		'name':'find_elements_by_name',
		'xpath':'find_elements_by_xpath',
		'css':'find_elements_by_css_selector', 
		'tag':'find_elements_by_tag_name',
		'id':'find_elements_by_id'
		}
		self.default_module = \
		'''
		<table>
			<tr>
				<td>
					text
				</td>
			</tr>
		</table>
		'''
		self.default_module = self.simple_module(self.default_module)
		self.mlist = self.str2mlist(self.default_module)
		self._object = ''
		#self.touchactions = TouchActions(self.driver)
		#self.poiteractions = PointerActions()
		self.actionchains = ActionChains(self.driver)
		self.webdriverwait = WebDriverWait(self.driver,30)
		try:
			self.driver.get(self.url)
		except WebDriverException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(10)
			self.driver.close()
			print "-----sys exit-------"
			sys.exit(1)
		except NoSuchWindowException,e:
			print "--------------"
			print e
			print "--------------"
			main_network_security()
			self.firefox_profile = FirefoxProfile()
			self.set_profile(self.firefox_profile)
			self.driver = webdriver.Firefox(firefox_profile=self.firefox_profile,firefox_binary=self.firefox_binary,executable_path=self.executable_path)
			self.driver.implicitly_wait(3)
			self.driver.get(url)
	def get_xml_value(self,filename,tag_name="label"):
		text_list = []
		if os.path.exists(filename):
			domtree = xml.dom.minidom.parse(filename)
			children = domtree.getElementsByTagName(tag_name)
			for child in children:
				text_list.append(child.childNodes[0].data)
		return text_list
	def simple_module(self,_str):
		return _str.strip().replace('\t','').replace('\n','') if type(_str) is str else ''
	def str2mlist(self,_str):
		assert type(_str) is str,"module must be strings."
		mstr = _str.replace('<',' ').replace('>',' ')
		_mlist = mstr.split(' ')
		mlist = []
		#print _mlist
		text_id = _mlist.index("text")
		#print text_id
		for num in xrange(0,text_id):
			if not _mlist[num] or _mlist[num] =="table":
				continue
			#print _mlist[num],_mlist[len(_mlist)-num-1]
			assert '/' + _mlist[num] == _mlist[len(_mlist)-num-1],"module check error!"
			mlist.append(_mlist[num])
		print mlist
		return mlist
	def set_profile(self,profile,preference={"browser.download.dir":"/local/sdb/mtk_patch_import/TODO","browser.download.folderList":2,"browser.download.manager.showWhenStarting":False,"plugin.disable_full_page_plugin_for_types":"application/pdf","pdfjs.disabled":True,"browser.helperApps.neverAsk.saveToDisk":"application/x-gzip,application/zip,application/x-gtar,application/vnd.ms-excel,application/x-tar,application/octet-stream,text/csv,application/xml,application/vnd.ms-powerpoint,application/octet-stream,text/plain,video/x-msvideo,video/x-sgi-movie,video/mpeg,application/pdf,image/png,image/jpeg,image/bmp,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.presentationml.presentaton,audio/mpeg","layout.css.devPixelsPerPx":"1"}):
		for key in preference.keys():
			profile.set_preference(key,preference[key])

	#login is a method of logining internet by some user,password
	#@params
	#@send_keys_list	a list of key to send,when a value in the list is '',send nothing.
	#@fun_value		a list,the memeber is the value to take for functions
	#@function		a list,the memeber is the short name of functions
	def login(self,send_keys_list=[],fun_value=[],function=[]):
		
		self.driver.implicitly_wait(60)
		assert len(send_keys_list)==len(fun_value)==len(function),"wrong length in send_keys,fun_value and function"
		for i in xrange(len(send_keys_list)):
			element = getattr(self.driver,self.function_dict[function[i]])(fun_value[i])[0]
			element.click()
			if send_keys_list[i]:
				element.clear()
				element.send_keys(send_keys_list[i])
		time.sleep(3)
	def fillform(self,send_keys_list=[],fun_value=[],function=[]):
		
		self.driver.implicitly_wait(60)
		assert len(send_keys_list)==len(fun_value)==len(function),"wrong length in send_keys,fun_value and function"
		for i in xrange(len(send_keys_list)):
			element = getattr(self.driver,self.function_dict[function[i]])(fun_value[i])
			element.click()
			if send_keys_list[i]:
				element.clear()
				element.send_keys(send_keys_list[i])
		time.sleep(3)
	def get_url(self,url):
		self.driver.implicitly_wait(10)
		self.driver.get(url)
	def close(self):
		self.driver.close()
	#@params
	#@linkdata_name_col	the link data name mapping the position of the column
	#@vector_rules		data structure of an vector
	#@vector		a list,[case_number,custorme_project,mtime,status]
	#@v_data		a list of vector,[vector1,vector2,...]	
	#@css_selector		a list of css selector of the table
	def get_row_linkdata(self,v_data=[],linkdata_name_col = {'case_number':1},vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=[".list > tbody:nth-child(1)"]):

		vector = [''*i for i in xrange(len(vector_rules))]
		self.driver.implicitly_wait(30)
		tbody = self.driver.find_element_by_css_selector(css_selector[0])
		trows =  tbody.find_elements_by_tag_name("tr")
		for i in xrange(1,len(trows)):
			tcols = trows[i].find_elements_by_tag_name("a")
			#for k in xrange(len(tcols)):
				#print "====tcols[%s].text.encode('utf-8')===="%k,tcols[k].text.encode('utf-8')
			for j in xrange(len(vector_rules)):
				if vector_rules[j] in linkdata_name_col.keys():
					vector[j] = tcols[linkdata_name_col[vector_rules[j]]].text.encode('utf-8')
			self.v_data_update_vector(v_data,i,vector)
			vector = [''*i for i in xrange(len(vector_rules))]
		print "v_data=====",v_data

	#@params
	#@data_name_col			the data name mapping the position of the column
	#@vector			a list,[case_number,custorme_project,mtime,status]
	#@vector_rules			data structure of an vector
	#@v_data			a list of vector,[vector1,vector2,...]
	#@css_selector			a list of css selector of the table
	def get_row_data(self,v_data=[],data_name_col={'customer_project':5,'status':2,'subject':3,'mtime':4,'contact_name':8},vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=[".list > tbody:nth-child(1)"]):

		vector = [''*i for i in xrange(len(vector_rules))]
		self.driver.implicitly_wait(30)
		tbody = self.driver.find_element_by_css_selector(css_selector[0])
		trows =  tbody.find_elements_by_tag_name("tr")
		for i in xrange(1,len(trows)):
			tcols = trows[i].find_elements_by_tag_name("td")
			#for k in xrange(len(tcols)):
				#print "tcols[%s].text.encode('utf-8')===="%k,tcols[k].text.encode('utf-8')
			for j in xrange(len(vector_rules)):
				if vector_rules[j] in data_name_col.keys() and data_name_col[vector_rules[j]] < len(tcols):
					vector[j] = tcols[data_name_col[vector_rules[j]]].text.encode('utf-8')
			self.v_data_update_vector(v_data,i,vector)
			vector = [''*i for i in xrange(len(vector_rules))]
		print "v_data=====",v_data
	
	#@v_data			a list of vector,[vector1,vector2,...]
	#@vector			a list,[case_number,custorme_project,mtime,status]
	#@num				a number of position where is the vector insert in or  update
	#@params
	#@linkdata_name_col	the link data name mapping the position of the column
	#@vector_rules		data structure of an vector
	#@vector		a list,[case_number,custorme_project,mtime,status]
	#@v_data		a list of vector,[vector1,vector2,...]	
	#@css_selector		a list of css selector of the table
	def get_row_propdata(self,v_data=[],propdata_name_col = {'href':'[href].1'},vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=[".list > tbody:nth-child(1)"],order=['tr','a'],skip_row_col=[0],orientation=True,table_key_value=['labelCol','dataCol']):
		tmp_tcols = {}
		vector = [''*i for i in xrange(len(vector_rules))]
		self.driver.implicitly_wait(30)
		print "find_element_by_css_selector(css_selector[0])"
		try:
			tbody = self.driver.find_element_by_css_selector(css_selector[0])
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(css_selector) > 1:
				tbody = self.driver.find_element_by_css_selector(css_selector[1])
		print "find_elements_by_tag_name(order[0])"
		trows =  tbody.find_elements_by_tag_name(order[0])
		for i in xrange(len(trows)):
			if i in skip_row_col:
				continue
			tcols = trows[i].find_elements_by_tag_name(order[1])
			for j in xrange(len(vector_rules)):
				if vector_rules[j] in propdata_name_col.keys():
					tag_props = propdata_name_col[vector_rules[j]]
					if type(tag_props) is str and tag_props.find('[')!=-1 and tag_props.find(']')!=-1:
						prop_name = tag_props.split('.')[0].replace('[','').replace(']','')
						prop_num = tag_props.split('.')[1]
					
						if tag_props.find('=')==-1:
							vector[j] = tcols[int(prop_num)].get_attribute(prop_name).encode('utf-8')
							print "tcols[int(prop_num)].get_attribute(prop_name).encode('utf-8')",tcols[int(prop_num)].get_attribute(prop_name).encode('utf-8')
						else:
							prop_value = prop_name.split('=')[1]
							prop_name = prop_name.split('=')[0]
						
							for k in xrange(len(tcols)):
								print  tcols[k].text.encode('utf-8'),tcols[k].get_attribute(prop_name).encode('utf-8')
								if k+1 < len(tcols) and tcols[k].get_attribute(prop_name).encode('utf-8') == table_key_value[0] and tcols[k+1].get_attribute(prop_name).encode('utf-8') == table_key_value[1] :
									tab_key = tcols[k].text.encode('utf-8').lower().replace(' ','_')
									tab_value = tcols[k+1].text.encode('utf-8').lower().replace(' ','_')
									tmp_tcols[tab_key]=tab_value
							print "tmp_tcols",tmp_tcols
							vector[j] = tmp_tcols.get(vector_rules[j])
					if type(tag_props) is int:
						vector[j] = tcols[tag_props].text.encode('utf-8')
					print "vector[j]===",vector[j]
			print "v_data+++",v_data
			print "vector+++",vector
			self.v_data_update_vector(v_data,i,vector)
			if orientation:
				tmp_tcols = {}
				vector = [''*i for i in xrange(len(vector_rules))]
		print "v_data=====",v_data
	def get_table_cell(self,css_selector=[".list > tbody:nth-child(1)"],table_cell=[],order=['tr','td']):
		self.driver.implicitly_wait(30)
		print "find_element_by_css_selector(css_selector[0])"
		try:
			tbody = self.driver.find_element_by_css_selector(css_selector[0])
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(css_selector) > 1:
				tbody = self.driver.find_element_by_css_selector(css_selector[1])
		trows =  tbody.find_elements_by_tag_name(order[0])
		tcols = [ ''*i for i in xrange(len(trows)) ]
		nrows = len(trows)
		#print "nrows,trows",nrows,trows
		ncols = 0
		for i in xrange(nrows):
			tcols[i] = trows[i].find_elements_by_tag_name(order[1])
			print "tcols[i],len(tcols[i])",tcols[i],len(tcols[i])
			if len(tcols) > ncols:
				ncols = len(tcols)
		#print "tcols",tcols
		vector = [''*i for i in xrange(ncols) ]
		table_cell = [ [ ''*i for i in xrange(ncols) ] for j in xrange(nrows) ]
		for i,j in product(xrange(nrows),xrange(ncols)):
			if j < len(tcols[i]):
				table_cell[i][j] = tcols[i][j]
			print "table_cell[%s][%s]"%(i,j),table_cell[i][j]
			print "table_cell[%s][%s]"%(i,j),self._text(table_cell[i][j])
		
		#print table_cell
		return table_cell
	def _cp_table_module(self,table):
		if type(table) is list and type(table[0]) is list:
			return [ [ ''*i for i in xrange(len(table[0]))] for j in xrange(len(table)) ]
	def get_table_text(self,table_object):
		table_text = self._cp_table_module(table_object)
		for i,j in product(xrange(len(table_object)),xrange(len(table_object[0]))):
			table_text[i][j] = self._text(table_object[i][j])
		print "table_text",table_text
		return table_text
	def get_table_attri(self,table_object,attri='href'):
		table_attri = self._cp_table_module(table_object)
		for i,j in product(xrange(len(table_object)),xrange(len(table_object[0]))):
			table_attri[i][j] = self._attri(table_object[i][j],attri)
		print "table_attri",table_attri
		return table_attri
	def get_table_child_node(self,table_object,tag_name='a',function='tag'):
		table_child_note = self._cp_table_module(table_object)
		for i,j in product(xrange(len(table_object)),xrange(len(table_object[0]))):
			table_child_note[i][j] = self._child_note(table_object[i][j],tag_name,function)
		print "table_child_note",table_child_note
		return table_child_note
	def get_table(self,fun_value=[".list > tbody:nth-child(1)"],function="css"):
		assert isinstance(self.driver,WebDriver),"self.driver is not instance of WebDriver."
		tbody=''
		self.driver.implicitly_wait(30)
		print "find_element_by_css_selector(css_selector[0])"
		try:
			tbody = getattr(self.driver,self.function_dict[function])(fun_value[0])
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(css_selector) > 1:
				tbody = getattr(self.driver,self.function_dict[function])(fun_value[1])
		return tbody
	def get_table_tr(self,fun_value=[".list > tbody:nth-child(1)"],function="css"):
		assert isinstance(self.driver,WebDriver),"self.driver is not instance of WebDriver."
		tbody=''
		tbody_tr=''
		self.driver.implicitly_wait(30)
		print "find_element_by_css_selector(css_selector[0])"
		try:
			tbody = getattr(self.driver,self.function_dict[function])(fun_value[0])
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(css_selector) > 1:
				tbody = getattr(self.driver,self.function_dict[function])(fun_value[1])
		print "len(tbody)",len(tbody)
		tbody=tbody[0]
		try:
			tbody_tr = getattr(tbody,self.function_dict['tag'])('tr')
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(css_selector) > 1:
				tbody_tr = getattr(tbody,self.function_dict['tag'])('tr')
		self.display_list(tbody_tr)
		return tbody_tr
	def button(self,css,function='css'):
		bt = self.get_object([css],function)
		if bt:
			print "button",bt
			time.sleep(10)
			print "button click"
			#self.ac_click(bt[0])
			bt[0].click()
	def text_input(self,css,text,function='css'):
		text_input = self.get_object(css,function)
		if text_input: 
			print "text input",text_input
			text_input = text_input[0]
			self.driver.implicitly_wait(30)
			text_input.click()
			text_input.clear()
			text_input.send_keys(text)
	def display_list(self,object_list):
		for i in xrange(len(object_list)):
			if object_list[i].rect["height"]==0.0 or object_list[i].rect["width"]==0.0:
				continue
			print "object_list[%s]====%s" % (i,object_list[i])
			print "rect",object_list[i].rect
			self.actionchains.move_to_element(object_list[i])
			self.actionchains.click()
			self.actionchains.perform()
			time.sleep(1)
	def ac_click(self,_object):
		if _object:
			self.actionchains.move_to_element(_object)
			self.actionchains.click()
			self.actionchains.perform()
		time.sleep(1)
	def get_object(self,fun_value=[".list > tbody:nth-child(1)"],function="css",_driver=""):
		_object = ''
		if not _driver:
			_driver = self.driver
		assert isinstance(self.driver,WebDriver),"self.driver is not instance of WebDriver."
		self.driver.implicitly_wait(30)
		try:
			_object = getattr(_driver,self.function_dict[function])(fun_value[0])
		except NoSuchElementException,e:
			print "--------------------"
			print e
			print "--------------------"
			self.driver.implicitly_wait(30)
			if len(fun_value) > 1:
				_object = getattr(_driver,self.function_dict[function])(fun_value[1])
		return _object
	def _child_node(self,_object,fun_value='a',function="tag"):
		
		child_note=''
		#print "type(_object)",type(_object),_object
		if isinstance(_object,WebElement) or isinstance(_object,WebDriver):
			try:
				child_note = getattr(_object,self.function_dict[function])(fun_value)
			except Exception,e:
				print "--------------------"
				print e
				print "--------------------"
				child_note=''
		else:
			child_note=''
		print "child_note",child_note
		return child_note
	def _attri(self,_object,attri='href'):
		return _object.get_attribute(attri).encode('utf-8') if isinstance(_object,WebElement) and _object.get_attribute(attri) else ''
	def _text(self,_object):
		#print "_object",_object,type(_object),isinstance(_object,WebElement),hasattr(_object,'text')
		#if isinstance(_object,WebElement):
			#print "_ininstance",_object.text.encode('utf-8')
			#_text = _object.text.encode('utf-8')
		return _object.text.encode('utf-8') if isinstance(_object,WebElement) else ''
	def get_v_data(self,table_text,vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href']):
		data = {}
		for i,j in product(xrange(len(table_text)),xrange(len(table_text[0]))):
			text = table_text[i][j]
			if text.lower().replace(' ','_') in vector_rules and j+1 < len(table_text[0]):
				data[text.lower().replace(' ','_')] = table_text[i][j+1]
		print data
		return data
	def switch_dict_vector(self,data_dict,vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href']):
		vector = [''*i for i in xrange(len(vector_rules))]
		for i in xrange(len(vector_rules)):
			if vector_rules[i] in data_dict.keys():
				vector[i] = data_dict[vector_rules[i]]
		print "vector",vector
		return vector
	def vpick_up_key(self,vector,pick_up_list=['case_number','contact_email'],vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href']):
		_vector = [''*i for i in xrange(len(vector_rules))]
		assert len(vector)==len(vector_rules),"vector's length is not equal to vector rules"
		for i in xrange(len(vector_rules)):
			if vector_rules[i] in pick_up_list:
				_vector[i] = vector[i]
		print "_vector",_vector
		return _vector
	def update_data(self,v_data,vector,pick_up_list=['case_number','contact_email'],vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href']):
		print "origin v_data",v_data
		assert len(vector) == len(vector_rules),"wronge length of vector!"
		assert 'case_number' in pick_up_list,"key value 'case_number' must be included."
		assert pick_up_list[0] == 'case_number',"the first memeber of pick up list must be 'case_number'."
		pick_up_num=[]
		for i in xrange(len(pick_up_list)):
			assert pick_up_list[i] in vector_rules,"pick_up_list key is not in vector rules."
			pick_up_num.append(vector_rules.index(pick_up_list[i]))
		main_key = vector_rules.index('case_number')
		for data in v_data:
			assert len(data) == len(vector_rules),"wronge length of v_data!"
			match = re.match(r"[0-9]+",vector[main_key])
			if match and data[main_key] == match.group():
				for i in xrange(1,len(pick_up_num)):
					data[pick_up_num[i]] = vector[pick_up_num[i]]
		print "updata v_data",v_data
	def is_not_empty(self,vector):
		value=''
		for i in xrange(len(vector)):
			if vector[i]:
				value = vector[i]
		return True if value else False
	def search_key_value_id(self,v_data,value,key='case_number',vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href']):
		assert key in vector_rules,"key no in vector_rules."
		v_j = vector_rules.index(key)
		for i in xrange(len(v_data)):
			if v_data[i][v_j] == value:
				return i
				 
						
	def v_data_update_vector(self,v_data,num,vector):
		if num > len(v_data):
			v_data.append(vector)
		elif num >= 1:
			assert len(v_data[num-1]) == len(vector),"wrong length of vector"
			for i in xrange(len(v_data[num-1])):
				if vector[i] :
					v_data[num-1][i] = vector[i]
					
		else:
			print "data number error"
			sys.exit(1)
	#pop select items whose value  is  equal to ref_value
	#@params
	#@v_data			a list of vector,[vector1,vector2,...]
	#@ref_value		ref value 
	#@vkey			a value of vector_rules which is to check all value in v_data
	#@vector_rules		data structure of an vector
	def grep_vkey_value(self,v_data,vkey,ref_value,vector_rules=['case_number','customer_project','status','subject','mtime']):
		vector_key_number = ''
		grep_data = []
		if vkey and ref_value and vkey in vector_rules:
			vector_key_number = vector_rules.index(vkey)
			#print "vector_key_number",vector_key_number
		if vector_key_number:
			for vector in v_data:
				if vector[vector_key_number].find(ref_value)!=-1:
					#print "vector",vector
					grep_data.append(vector)
		print "grep_data=====",grep_data
		return grep_data

class login_mtk(login_internet):
	def __init__(self,url="https://transfer.mediatek.com"):
		super(login_mtk,self).__init__(url)
		self.driver.maximize_window()
		self.email_text = []
		self.owner = []
		self.eserviceid = []
		self.patchs = []
		self.patch_downloaded = False
		self._releasetime = []
		self.releasetime = []
		self.href_list = []
		self._href_list = []
	def set_profile(self,profile):
		super(login_mtk,self).set_profile(profile,{"browser.download.dir":"/local/sdb/mtk_patch_import/TODO","browser.download.folderList":2,"browser.download.manager.showWhenStarting":False,"plugin.disable_full_page_plugin_for_types":"application/pdf","pdfjs.disabled":True,"browser.helperApps.neverAsk.saveToDisk":"application/x-gzip,application/zip,application/x-gtar,application/vnd.ms-excel,application/x-tar,application/octet-stream,text/csv,application/xml,application/vnd.ms-powerpoint,application/octet-stream,text/plain,video/x-msvideo,video/x-sgi-movie,video/mpeg,application/pdf,image/png,image/jpeg,image/bmp,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.presentationml.presentaton,audio/mpeg,video/x-flv,video/mp4,application/x-mpegURL,video/MP2T,video/3gpp,video/quicktime,video/x-msvideo,video/x-ms-wmv","layout.css.devPixelsPerPx":"0.2"})

	def login(self,user="susheng.ye.hz@tcl.com",passwd="yss5421789"):
		super(login_mtk,self).login([user,passwd,''],["username","password","btn"],['name','name','id'])

	def load_history_received(self,url="https://transfer.mediatek.com/History/Received"):
		self.driver.get(url)
		time.sleep(3)
	def grep_table(self):
		tables = self.driver.find_elements_by_css_selector("html body.page-header-fixed.page-footer-fixed.page-sidebar-closed-hide-logo.page-container-bg-solid div.page-container div.page-content-wrapper div.page-content div.portlet.light div.portlet-body div#ShareHistoryTable_wrapper.dataTables_wrapper.form-inline.dt-bootstrap.no-footer div.row div.col-sm-12 table#ShareHistoryTable.dt-responsive.dataTable.no-footer.table.table-bordered.table-striped.table-hover.dtr-inline tbody tr td.dt-fixwidth a")
		#print tables
		#print len(tables)
		for table in tables:
			href = table.get_attribute("href")
			if href not in self.href_list:
				self.href_list.append(href)
		self.href_list = self.href_list[::-1]
		print self.href_list
		assert len(self.href_list)==len(tables),"something wrong happened in access to datatables"
	def get_script(self):
		scripts = self.driver.find_elements_by_tag_name("script")
		for script in scripts:
			print script.get_attribute("type")
	def convert_href_to_get_email_text(self):
		for url in self.href_list:
			if not url:
				continue
			print '----',url,'-----'
			self.get_email_text(url)
			time.sleep(3)
			self.download_all()
	def get_email_text(self,url):
		self.driver.get(url)
		#time.sleep(1)
		
		emails = self.driver.find_elements_by_tag_name("pre")
		
		for email in emails:
			#print email
			
			#print email.text
			if email.text not in self.email_text:
				self.email_text.append(email.text)
		time_class = self.driver.find_element_by_css_selector("html body.page-header-fixed.page-footer-fixed.page-sidebar-closed-hide-logo.page-container-bg-solid div.page-container div.page-content-wrapper div.page-content div.note.note-warning p.font-yellow-gold.pull-right")	
		if time_class:
			print time_class.text
			self._releasetime.append(time_class.text.split()[0])
		#print len(self.email_text),len(self._releasetime)
		#assert len(self.email_text)==len(self._releasetime),'text number cannot equal to releasetime numbers'
	def get_classes(self):
		time_classes = self.driver.find_elements_by_tag_name("div")
		t_class = ''
		for time_class in time_classes:
			try:
				if time_class.get_attribute("class")=="note note-warning":
					print time_class.get_attribute("class")
					t_class = time_class
			except StaleElementReferenceException,e:
				self.get_classes()
		return t_class
	def get_owner_eserviceid(self):
		for i in xrange(0,len(self.email_text)):
			text = self.email_text[i]
			releasetime = self._releasetime[i]
			matchs = ''
			match = ''
			_eserviceid = ''
			_owner = ''
			_patchs = ''
			formate_eservice = r"In\sresponse\sto\seService\srequest\s(\w+)\."
			matchs = re.findall(formate_eservice,text)
			for match in matchs:
				print "In\sresponse\sto\seService\srequest\s(\w+)\.",match
				assert len(matchs)==1,"something wrong happened"
				_eserviceid = match
			matchs = ''
			match = ''
			formate_owner = r"EMAIL:\s(\w+\.?-?\w+?\.?\w+?@\w+\.com)"
			matchs = re.findall(formate_owner,text)
			if len(matchs)>=1:
				print "EMAIL:\s(\w+\.?-?\w+?\.?\w+?@\w+\.com)",matchs
				_owner = matchs[-1]
			matchs = ''
			match = ''
			formate_patchs = r"-\s(\w+_?\w+?\(\S+\)\.tar\.gz)"
			matchs = re.findall(formate_patchs,text)
			if matchs and matchs[0]:
				print "-\s(\w+_?\w+?\(\S+\)\.tar\.gz)",matchs
				assert len(matchs)>=1,"something wrong happened"
				matchs = self.grep_v_cmcc(matchs)
				_patchs = ','.join(matchs)
			if _eserviceid and _owner and _patchs:
				self.eserviceid.append(_eserviceid)
				self.owner.append(_owner)
				self.patchs.append(_patchs)
				self.releasetime.append(releasetime)
				self._href_list.append(self.href_list[i])
	def grep_v_cmcc(self,matchs):
		tmp = []
		for i in xrange(0,len(matchs)):
			#print i,matchs[i]
			if matchs[i].find("CMCC")==-1:
				tmp.append(matchs[i])
		print tmp
		return tmp
	def download(self,patch_list):
		download_buttons = self.driver.find_elements_by_xpath("//td/a[@data-original-title='Http']")
		print download_buttons
		#download_buttons = self.driver.find_elements_by_css_selector("html body.page-header-fixed.page-footer-fixed.page-sidebar-closed-hide-logo.page-container-bg-solid div.page-container div.page-content-wrapper div.page-content div.portlet.light div.portlet-body div.portlet-body div#FileTable_wrapper.dataTables_wrapper.form-inline.dt-bootstrap.no-footer div.row div.col-sm-12 table#FileTable.dt-responsive.dataTable.no-footer.dtr-inline.table.table-bordered.table-striped.table-hover tbody tr.odd td a.fa.fa-download.btn-action.download.http")
		if len(download_buttons)!=len(patch_list):
			print "wrong length of download button and patch list"
		for i in xrange(0,len(download_buttons)):
			if os.path.exists("/local/sdb/mtk_patch_import/TODO/"+patch_list[i]) and not os.path.exists("/local/sdb/mtk_patch_import/TODO/"+patch_list[i] + ".part"):
				continue
			download_buttons[i].click()
			time.sleep(5)
			agree_button = self.driver.find_element_by_xpath("//div[@class='modal-footer']/button[contains(text(),'Agree')]")
			#agree_button = self.driver.find_element_by_css_selector("html body.page-header-fixed.page-footer-fixed.page-sidebar-closed-hide-logo.page-container-bg-solid.modal-open div.page-container div.page-content-wrapper div.page-content div#legalModel.modal.in div.modal-dialog div.modal-content div.modal-footer button.btn.btn-primary.agree")
			time.sleep(3)
			agree_button.click()
			time.sleep(3)
	def replace_x(self,text_str):
		return text_str.replace('\x01','%01').replace('\x02','%02').replace('\x03','%03').replace('\x04','%04').replace('\x05','%05').replace('\x06','%06').replace('\x07','%07').replace('\x08','%08').replace('\x09','%09').replace('\x0a','%0a').replace('\x0b','%0b').replace('\x0c','%0c').replace('\x0d','%0d').replace('\x0e','%0e').replace('\x10','%10').replace('\x11','%11').replace('\x12','%12').replace('\x13','%13').replace('\x14','%14').replace('\x15','%15').replace('\x16','%16').replace('\x17','%17').replace('\x18','%18').replace('\x19','%19').replace('\x1a','%1a').replace('\x1b','%1b').replace('\x1c','%1c').replace('\x1d','%1d').replace('\x1e','%1e').replace('\x20','%20').replace('\x80','%80').replace('\x81','%81').replace('\x82','%82').replace('\x83','%83').replace('\x84','%84').replace('\x85','%85').replace('\x86','%86').replace('\x87','%87').replace('\x88','%88').replace('\x89','%89').replace('\x8a','%8a').replace('\x8b','%8b').replace('\x8c','%8c').replace('\x8d','%8d').replace('\x8e','%8e').replace('\x90','%90').replace('\x91','%91').replace('\x92','%92').replace('\x93','%93').replace('\x94','%94').replace('\x95','%95').replace('\x96','%96').replace('\x97','%97').replace('\x98','%98').replace('\x99','%99').replace('\x9a','%9a').replace('\x9b','%9b').replace('\x9c','%9c').replace('\x9d','%9d').replace('\x9e','%9e').replace('\xa0','%a0').replace('\xa1','%a1').replace('\xa2','%a2').replace('\xa3','%a3').replace('\xa4','%a4').replace('\xa5','%a5').replace('\xa6','%a6').replace('\xa7','%a7').replace('\xa8','%a8').replace('\xa9','%a9').replace('\xaa','%aa').replace('\xab','%ab').replace('\xac','%ac').replace('\xad','%ad').replace('\xae','%ae').replace('\xb0','%b0').replace('\xb1','%b1').replace('\xb2','%b2').replace('\xb3','%b3').replace('\xb4','%b4').replace('\xb5','%b5').replace('\xb6','%b6').replace('\xb7','%b7').replace('\xb8','%b8').replace('\xb9','%b9').replace('\xba','%ba').replace('\xbb','%bb').replace('\xbc','%bc').replace('\xbd','%bd').replace('\xbe','%be').replace('\xc0','%c0').replace('\xc1','%c1').replace('\xc2','%c2').replace('\xc3','%c3').replace('\xc4','%c4').replace('\xc5','%c5').replace('\xc6','%c6').replace('\xc7','%c7').replace('\xc8','%c8').replace('\xc9','%c9').replace('\xca','%ca').replace('\xcb','%cb').replace('\xcc','%cc').replace('\xcd','%cd').replace('\xce','%ce').replace('\xd0','%d0').replace('\xd1','%d1').replace('\xd2','%d2').replace('\xd3','%d3').replace('\xd4','%d4').replace('\xd5','%d5').replace('\xd6','%d6').replace('\xd7','%d7').replace('\xd8','%d8').replace('\xd9','%d9').replace('\xda','%da').replace('\xdb','%db').replace('\xdc','%dc').replace('\xdd','%dd').replace('\xde','%de').replace('\xe0','%e0').replace('\xe1','%e1').replace('\xe2','%e2').replace('\xe3','%e3').replace('\xe4','%e4').replace('\xe5','%e5').replace('\xe6','%e6').replace('\xe7','%e7').replace('\xe8','%e8').replace('\xe9','%e9').replace('\xea','%ea').replace('\xeb','%eb').replace('\xec','%ec').replace('\xed','%ed').replace('\xee','%ee')
	def download_all(self):
		_patch = []
		patch_tables_trs = self.driver.find_elements_by_xpath("//table[@id='FileTable']/tbody/tr")
		print "patch_tables_trs",patch_tables_trs
		for patch_tables_tr in patch_tables_trs:
			print "patch_tables_tr",patch_tables_tr
			patch_tables_tr.click()
			patch_tables_td = patch_tables_tr.find_element_by_xpath("child::td[2]")
			print "patch_tables_td",patch_tables_td
			print "patch_tables_td.text",patch_tables_td.text.encode('utf-8'),self.replace_x(patch_tables_td.text.encode('utf-8').replace(" ","_").replace("@","%40").replace("#","%23").replace("[","%5b").replace("]","%5d").replace("–","%e2%80%93").replace("&","%26").replace("+","%2b"))
			if patch_tables_td.text:
				_patch.append(self.replace_x(patch_tables_td.text.encode('utf-8').replace(" ","_").replace("@","%40").replace("#","%23").replace("[","%5b").replace("]","%5d").replace("–","%e2%80%93").replace("&","%26").replace("+","%2b")))
		self.is_patch_downloaded(_patch)
		current_driver = self.driver.current_window_handle
		if not self.patch_downloaded:
			self.download(_patch)
			for num in xrange(0,600): 
				if self.patch_downloaded:
					break
				self.is_patch_downloaded(_patch)
				time.sleep(6)
			time.sleep(3)
			self.close_other_windows(current_driver)

	def close_other_windows(self,current_driver):
		if current_driver:
			drivers = self.driver.window_handles
			for driver in drivers:
				print driver
				print driver.title
				if driver != current_driver:
					self.driver.switch_to.window(driver)
					#print "dir(other window)",dir(self.driver)
					time.sleep(1)
					self.driver.close()
			self.driver.switch_to.window(current_driver)					

	def is_patch_downloaded(self,_patchs):
		print "++++_patchs++++",_patchs
		if len(_patchs)==0:
			self.patch_downloaded = True
		for _patch in _patchs:
			print "====_patch====",_patchs
			if not _patch:
				continue
			if os.path.exists("/local/sdb/mtk_patch_import/TODO/"+_patch) and not os.path.exists("/local/sdb/mtk_patch_import/TODO/"+_patch + ".part"):
				self.patch_downloaded = True
			else:
				self.patch_downloaded = False
				break
	def echo_data(self):
		print self.eserviceid
		print self.owner
		print self.patchs
		print self.releasetime
#"In response to eService request ALPS03324166."
			#"EMAIL: kai.du.hz@tcl.com (Site:HZ)"
			#"The following files have been uploaded for you:"
#"- MOLY00257415_eServiceID(For_JHZ6737M_65_N_MOLY.LR9.W1444.MD.LWTG.MP.V110.5.P26).tar.gz"
#"- ALPS03324166(For_jhz6737m_65_n_alps-mp-n0.mp1-V1.0.2_P96).tar.gz"

#"Notes:"
	def getrandomnum(self,gap=10000):
		pass			
	def logout(self):		
		time.sleep(20)	
		self.driver.close()
class login_qualcomm(login_internet):

	def __init__(self,url="https://qualcomm-cdmatech-support.my.salesforce.com/500?fcf=00B30000005n8Sf"):
		time.sleep(5)
		super(login_qualcomm,self).__init__(url)
		time.sleep(5)

	def login(self,user="shie.zhao@tcl.com",passwd="Zhjh_1028"):
		super(login_qualcomm,self).login(send_keys_list=[user,passwd,''],fun_value=["USER","PASSWORD_INPUT","//input[@type='submit']"],function=['name','name','xpath'])
		time.sleep(3)
	def get(self,url):
		self.driver.implicitly_wait(10)
		try:
			self.driver.get(url)
		except WebDriverException,e:
			print "--------------------"
			print e
			print "--------------------"
		self.driver.implicitly_wait(10)
	def switch_case_tab(self):
		case_tab_button = self.driver.find_element_by_css_selector("html body.hasMotif.homeTab.homepage.sfdcBody.brandQuaternaryBgr div#AppBodyHeader.bPageHeader table#tabsNewBar.tabsNewBar tbody tr td div.tabNavigation table#tabBar.tab tbody tr td div#Case_Tab")
		print case_tab_button
		time.sleep(1)
		case_tab_button.click()
		time.sleep(3)
	def switch_view_case(self,view_case='All_Open_Cases'):
		select_view = self.driver.find_element_by_name("fcf")
		print select_view
		time.sleep(1)
		select_view.click()
		time.sleep(3)
		options = self.driver.find_elements_by_xpath("//[@id=fcf/option")
		for option in options:
			option_text = option.text.encode('utf-8').replace(' ','_')
			print option_text
			time.sleep(1)
			if option_text.find(view_case)!=-1:
				option.click()
				time.sleep(3)
	def swith_detail_case(self,url):
		if url:
			match = re.match(r"https://qualcomm-cdmatech-support\.my\.salesforce\.com/\w+",url)
			if match:
				url = match.group()
				print "url",url
				self.driver.implicitly_wait(60)
				try:
					self.driver.get(url)
				except WebDriverException,e:
					print "--------------------"
					print e
					print "--------------------"
					self.driver.implicitly_wait(10)
					#self.driver.get(url)
					time.sleep(3)
			else:
				print "url no match re",url
				sys.exit(1)
					
	#@params
	#@linkdata_name_col	the link data name mapping the position of the column
	#@vector_rules		data structure of an vector
	#@vector		a list,[case_number,custorme_project,mtime,status]
	#@v_data		a list of vector,[vector1,vector2,...]	
	#@css_selector		css selector of the table

	#Action			tcols[0].text.encode('utf-8')==== Edit
	#Case Number		tcols[1].text.encode('utf-8')==== 03671861
	#Subject		tcols[2].text.encode('utf-8')==== Video playback image quality on screen is bad.
	#Customer Project	tcols[3].text.encode('utf-8')==== T2-M01
	def get_row_linkdata(self,v_data=[],linkdata_name_col = {'case_number':1},vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=[".list > tbody:nth-child(1)"]):
		super(login_qualcomm,self).get_row_linkdata(v_data,linkdata_name_col,vector_rules,css_selector)
		
	#@params
	#@data_name_col			the data name mapping the position of the column
	#@vector			a list,[case_number,custorme_project,mtime,status]
	#@vector_rules			data structure of an vector
	#@v_data			a list of vector,[vector1,vector2,...]
	#@css_selector			a list of css selector of the table

	#Action				tcols[0].text.encode('utf-8')==== Edit
	#Case Record Type		tcols[1].text.encode('utf-8')==== Wireless Device Support
	#Status				tcols[2].text.encode('utf-8')==== Closed-Pending Your Approval
	#Subject			tcols[3].text.encode('utf-8')==== lte部分频段FBRX校验死机，麻烦帮忙看下！
	#Last Modified Date/Time	tcols[4].text.encode('utf-8')==== 9/17/2018 3:37 AM
	#Custumer Project		tcols[5].text.encode('utf-8')==== PM85
	#Date/Time Opened		tcols[6].text.encode('utf-8')==== 9/6/2018 10:42 PM
	#Important to me		tcols[7].text.encode('utf-8')====
	#Contact Name			tcols[8].text.encode('utf-8')==== qingke, chen
	#Problem Area 1			tcols[9].text.encode('utf-8')==== Modem Software
	#Problem Area 2			tcols[10].text.encode('utf-8')==== LTE
	#Problem Area 3			tcols[11].text.encode('utf-8')==== LTE - TDD
	#Account Name			tcols[12].text.encode('utf-8')==== Huizhou TCL Mobile Communication Co., Ltd.
	def get_row_data(self,v_data=[],data_name_col={'customer_project':5,'status':2,'subject':3,'mtime':4,'contact_name':8},vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=[".list > tbody:nth-child(1)"]):
		super(login_qualcomm,self).get_row_data(v_data,data_name_col,vector_rules,css_selector)
	def get_detail_data(self,v_data=[],vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],css_selector=["div.pbSubsection:nth-child(2) > table:nth-child(1) > tbody:nth-child(1)","div.pbSubsection:nth-child(3) > table:nth-child(1) > tbody:nth-child(1)"],url='href'):
		pick_up_list = ['case_number','contact_email']
		for data in v_data:
			assert len(data) == len(vector_rules),"wronge length of data."
			print "data[vector_rules.index(url)]",data[vector_rules.index(url)]
			self.swith_detail_case(data[vector_rules.index(url)])
			table_cell = self.get_table_cell(css_selector)
			table_text = self.get_table_text(table_cell)
			table_attri = self.get_table_attri(table_cell,'class')
			table = self._child_node(self.driver,"div.pbSubsection:nth-child(2) > table:nth-child(1) > tbody:nth-child(1)","css")
			if len(table) == 1:
				table_a = self._child_node(table[0],"a","tag")
				#table_a_text = self.get_table_text(table_a)
				#table_a_attri = self.get_table_attri(table_a,'href')
			data = self.get_v_data(table_text)
			_vector = self.switch_dict_vector(data)
			vector = self.vpick_up_key(_vector,pick_up_list,vector_rules)
			self.update_data(v_data,vector,pick_up_list,vector_rules)
			
		
	def is_care_project(self,project,care_project):
		if project in care_project:
			return True
		else:
			return False	
	def search_project(self,project):

		url = 'https://qualcomm-cdmatech-support.my.salesforce.com/search/SearchResults?searchType=1&sen=0&setLast=1&sbstr='+project+'&search=+Go%21+'
		print url
		self.driver.get(url)
		time.sleep(15)	
				
def main_qualcomm():
	customer_project = ["Watt","Benz","Gauss"]
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
	main_send_mail(data,vector_rules)

class create_excel(object):

	def __init__(self,wbname="Quacom status",vector_rules = ['case_number','customer_project','status','subject','mtime','contact_email','href']):
		self.wbname = wbname
		self.style_title = self.get_style_tiltle()
		self.style_body = ''
		self.workbook = xlwt.Workbook(encoding='utf-8')
		self.sheets = []
		self.vector_rules = vector_rules
	def add_sheet(self,sheet_name):
		return self.workbook.add_sheet(sheet_name)

	def add_row(self,sheet,vector,row_num,style=''):
		assert len(vector)==len(self.vector_rules),"length of vector in data do not match vector rules"
		for i in xrange(len(vector)):
			sheet.write(row_num,i,vector[i],style) if style else sheet.write(row_num,i,vector[i])
	def add_header(self,sheet):
		self.add_row(sheet,self.vector_rules,0,self.style_title)
	def add_body(self,sheet,v_data):
		row_num = 1
		for vector in v_data:
			self.add_row(sheet,vector,row_num)
			row_num += 1
	def sheet_content(self,sheet_name,v_data):
		sheet = self.add_sheet(sheet_name)
		self.add_header(sheet)
		self.add_body(sheet,v_data)
	def save(self):
		self.workbook.save('/tmp/'+self.wbname)
	def get_style_tiltle(self):
		font = xlwt.Font()
		font.name = 'Arial'
		font.colour_index = 2
		font.bold = True
		font.height = 240
		font.outline = True
		borders = xlwt.Borders()
		borders.left = borders.MEDIUM
		borders.right = borders.MEDIUM
		borders.top = borders.MEDIUM
		borders.bottom = borders.MEDIUM
		style = xlwt.XFStyle()
		style.font = font
		style.borders = borders
		return style

def get_eserviceid_owner_patchs(text_list=[],eserviceid_list=[],owner_list=[],patchs_list=[]):
	if type(text_list) is str:
		text_list = [ text_list ]
	for text in text_list:
		#print "text====",text
		matchs = ''
		match = ''
		formate_eservice = r"In\sresponse\sto\seService\srequest\s(\w+)\."
		matchs = re.findall(formate_eservice,text)
		for match in matchs:
			print "In\sresponse\sto\seService\srequest\s(\w+)\.",match
			assert len(matchs)==1,"something wrong happened"
			eserviceid_list.append(match)
		matchs = ''
		match = ''
		formate_owner = r"EMAIL:\s(\w+\.?-?\w+?\.?\w+?@tcl\.com)"
		matchs = re.findall(formate_owner,text)
		if len(matchs)>=1:
			print "EMAIL:\s(\w+\.?-?\w+?\.?\w+?@tcl\.com)",matchs
			owner_list.append(matchs[-1])
		matchs = ''
		match = ''
		formate_patchs = r"-\s(\w+_?\w+?\(\S+\)\.tar\.gz)"
		matchs = re.findall(formate_patchs,text)
		if len(matchs)>=1:
			print "-\s(\w+_?\w+?\(\S+\)\.tar\.gz)",matchs
			patchs_list.append(','.join(matchs))
def main_mtk():
	mtk = login_mtk()
	mtk.login()
	mtk.load_history_received()
	mtk.grep_table()
	mtk.convert_href_to_get_email_text()
	mtk.get_owner_eserviceid()
	#mtk.download_all()
	mtk.echo_data()
	mtk.logout()
	return mtk.eserviceid,mtk.owner,mtk.patchs,mtk.releasetime
class send_mail(object):
	def __init__(self,
		v_data={},
		vector_rules=['case_number','customer_project','status','subject','mtime','contact_email','href'],
		customer_project= ["Watt","Benz","Gauss"],
		cclist=['xiaodan.cheng@tcl.com'],
		tolist=['xiaodan.cheng@tcl.com'],
		mail_vector_rules = ['case_number','customer_project','status','subject','mtime','contact_email'],
		mailServer =u'mailsz.tct.tcl.com',
		mailSender=u'xiaodan.cheng@tcl.com',
		mailAccount=u'tct-hq\\xiaodan.cheng',
		domainPassword="cxd@6235"):
		self.__mailServer = mailServer
		self.__mailSender = mailSender
		self.__mailAccount = mailAccount
		self.smtpServer = smtplib.SMTP(self.__mailServer)
		self.__domainPassword = domainPassword
		self.__tolist = tolist
		self.__cclist = cclist
		self.msg = ''
		self.mailbox = []
		self.vector_rules = vector_rules
		self.mail_vector_rules = mail_vector_rules
		self.v_data = self.data_exchange(v_data,self.vector_rules,self.mail_vector_rules)
		print self.v_data
		self.str_customer_project = ' '.join(customer_project)
		self.__mailtitle = 'Qualcomm case status'
		self.ownerlist = []
	def data_exchange(self,v_data,vector_rules,mail_vector_rules):
		data = []
		vector = [''*i for i in xrange(len(vector_rules))]
		mail_vector = [''*i for i in xrange(len(mail_vector_rules))]
		if len(vector_rules) == len(mail_vector_rules):
			data = v_data
		else:
			for vd in v_data:
				v_num = 0
				assert len(vd)==len(vector_rules),"wrong length!!!"
				for i in xrange(len(mail_vector_rules)):
					if mail_vector_rules[i] in vector_rules:
						v_num = vector_rules.index(mail_vector_rules[i])
						mail_vector[i] = vd[v_num]
				data.append(mail_vector)
				mail_vector = [''*i for i in xrange(len(mail_vector_rules))]
		return data
	def data_filter(self,v_data,vector_rules,filter_value=["closed"],filter_key="status",reverse=True):
		data=[]
		if type(filter_value) is str:
			filter_value = [ filter_value ]
		elif type(filter_value) is list:
			pass
		else:
			print "type filter_value wrong!"
			sys.exit(1)
		assert filter_key in vector_rules,"wrong filter key!"
		filter_num = vector_rules.index(filter_key)
		for vd in v_data:
			assert len(vd)==len(vector_rules),"wrong length!!!"
			flag = False
			for i in xrange(len(filter_value)):
				if vd[filter_num].find(filter_value[i])!=-1:
					flag = True
			if reverse and flag:
				data.append(vd)
			if not reverse and not flag:
				data.append(vd)
		print "%s data===="%' '.join(filter_value),data
		return data
	def get_hold_data(self):
		return self.data_filter(self.v_data,self.mail_vector_rules,"Hold")
	def get_closed_data(self):
		return self.data_filter(self.v_data,self.mail_vector_rules,"Closed")
	def get_other_data(self):
		return self.data_filter(self.v_data,self.mail_vector_rules,["Closed","Hold"],"status",False)
	def add_cclist(self,cclist=['xiaodan.cheng@tcl.com']):
		for cc in cclist:
			self.__cclist.append(cc)
	def add_tolist(self,tolist=['xiaodan.cheng@tcl.com']):
		for to in tolist:
			self.__tolist.append(to)
	def get_data2list(self,v_data,vector_rules,key='contact_email'):
		data_list = []
		assert key in vector_rules,"wrong key was got."
		key_num = vector_rules.index(key)
		for vd in v_data:
			assert len(vd)==len(vector_rules),"wrong length!!!"
			data_list.append(vd[key_num])
		print "data_list",data_list
		return data_list
	def get_msg(self):
		if len(self.mailbox) > 0:
			self.msg = MIMEMultipart()
			mail_content = MIMEText(''.join(self.mailbox), 'html','utf-8')
			self.msg.set_charset('gb2312')
			self.msg['From'] = self.__mailSender
			self.msg['Subject'] = self.__mailtitle
			self.msg['To'] = ','.join(self.__tolist)
			self.msg['Cc'] = ','.join(self.__cclist)
			self.msg.attach(mail_content)
	def set_att_file(self,afile_name):
		with open(afile_name,'rb') as af:
			adjunct = MIMEText(af.read(),'xls','utf-8')
		adjunct['Content-Type'] = 'application/vnd.ms-excel'
		adjunct['Content-Disposition'] = 'attachment; filename="%s"' % afile_name.split('/')[-1]
		print adjunct.items()
		self.msg.attach(adjunct)
	def get_mail_box_head(self,head_list=['<html>','<meta http-equiv="Content-Tye" content="text/html"; charset="utf-8" />','<body>','<p align=\'Left\'><b>Dears</b></p>'],css_module='<p align=\'left\'><font color="#FF0000"><b>the qualcom project(%s) Hold case status shows as below:</b></p>'):
		head_list.append(css_module % self.str_customer_project)
		for head in head_list:
			self.mailbox.append(head)
	#<tr>
	#<td>case_number</td>
	#<td>customer_project</td>
	#
	#</tr>
	def get_table_tr(self,v_data_tr,str_table=[],css_str=''):
		str_table.append('<tr%s>' % css_str)
		for vector in  v_data_tr:
			str_table.append('<td>%s</td>' % vector)
		str_table.append('</tr>')
		
	def get_table_head(self,str_table=[]):
		self.get_table_tr(self.mail_vector_rules,str_table)
	def get_table(self,v_data='',css_str='border="2" bordercolor="black" bgcolor="#32CD32"'):
		#if not v_data:
			#v_data = self.v_data
		str_table = self.mailbox
		str_table.append('<table %s>' % css_str)
		self.get_table_head(str_table)
		for v_data_tr in v_data:
			self.get_table_tr(v_data_tr,str_table)
		str_table.append('</table>')
	def get_mail_box_tail(self,tail_list=['</body>','</html>']):
		for tail in tail_list:
			self.mailbox.append(tail)
			
	def ssend_mail(self):
		print "========begin of send mail========"
		self.smtpServer.login(self.__mailAccount, self.__domainPassword)
		self.smtpServer.sendmail(self.__mailSender, self.__tolist + self.__cclist, self.msg.as_string())
		self.smtpServer.quit()
		print "========end of send mail========"
def main_send_mail(v_data={},vector_rules=['case_number','customer_project','status','subject','mtime']):
	smail = send_mail(v_data,vector_rules)
	smail.get_mail_box_head()
	smail.get_table()
	smail.get_mail_box_tail()
	smail.get_msg()
	smail.ssend_mail()
class patch_num_email:
	def __init__(self,project,
		owner,
		eserviceid,
		number0,
		number1,
		number2,
		driveonly,
		build,
		server,
		cclist=['xiaodan.cheng@tcl.com'],
		tolist=['xiaodan.cheng@tcl.com'],
		mailServer =u'mailsz.tct.tcl.com',
		mailSender=u'xiaodan.cheng@tcl.com',
		mailAccount=u'tct-hq\\xiaodan.cheng',
		domainPassword="cxd@6235"):
		self.project = project
		self.owner = owner
		self.eserviceid = eserviceid
		self.alps = number0
		self.moly = number1
		self.sixth = number2
		self.driveonly = driveonly
		self.build = build
		self.server = server
		self.__mailServer = mailServer
		self.__mailSender = mailSender
		self.__mailAccount = mailAccount
		self.smtpServer = smtplib.SMTP(self.__mailServer)
		self.__domainPassword = domainPassword
		self.__tolist = tolist
		self.__cclist = cclist
		self.msg = ''
		self.mailbox = []
		self.__mailtitle = '[MTK Patch merge][%s]patch number error' % self.project
	def get_msg(self):
		if len(self.mailbox) > 0:
			self.msg = MIMEMultipart()
			mail_content = MIMEText(''.join(self.mailbox), 'html','utf-8')
			self.msg.set_charset('gb2312')
			self.msg['From'] = self.__mailSender
			self.msg['Subject'] = self.__mailtitle
			self.msg['To'] = ','.join(self.__tolist)
			self.msg['Cc'] = ','.join(self.__cclist)
			self.msg.attach(mail_content)
	def get_mail_box_head(self,head_list=['<html>','<meta http-equiv="Content-Tye" content="text/html"; charset="utf-8" />','<body>','<p align=\'Left\'><b>Dears</b></p>'],css_module='<p align=\'left\'><font color="#FF0000"><b>the MTK project(%s) patch number error as below:</b></p>'):
		head_list.append(css_module % self.project)
		for head in head_list:
			self.mailbox.append(head)
	def get_mail_box_body(self):
		self.mailbox.append("<p></p>")
		if self.alps:
			self.mailbox.append("<p>ALPS %s</p>" % self.alps)
		if self.moly:
			self.mailbox.append("<p>MOLY %s</p>" % self.moly)
		if self.sixth:
			self.mailbox.append("<p>SIXTH %s</p>" % self.sixth)
		self.mailbox.append("<p></p>")
	def get_mail_box_tail(self,tail_list=['</body>','</html>']):
		for tail in tail_list:
			self.mailbox.append(tail)
	def send_mail(self):
		print "========begin of send mail========"
		self.smtpServer.login(self.__mailAccount, self.__domainPassword)
		self.smtpServer.sendmail(self.__mailSender, self.__tolist + self.__cclist, self.msg.as_string())
		self.smtpServer.quit()
		print "========end of send mail========"
	def mail(self):
		self.get_mail_box_head()
		self.get_mail_box_body()
		self.get_mail_box_tail()
		self.get_msg()
		self.send_mail()
class imap_email:
	def __init__(self,host="mail.tcl.com"):
		self.conn = imaplib.IMAP4_SSL(host)
		self.datas = []
		self.eserviceid = []
		self.owner = []
		self.patchs = []
	def login(self,username="tct-hq/xiaodan.cheng",password="cxd@6235"):
		self.conn.login(username,password)
	def logout(self):
		self.conn.logout()
	def get_mail(self):
		result,message = self.conn.select()
		#print result,message
		typeq,self.datas = self.conn.search(None,"ALL")
		#print typeq,self.datas
	def fetch_mail(self):
		#import sys
		#reload(sys)
		#sys.setdefaultencoding('utf-8')
		for data in self.datas:
			msglist = data.split()
			for n in xrange(0,len(msglist)):
				typeq,tmp_datas = self.conn.fetch(msglist[n],'(RFC822)')
				#print typeq,tmp_datas
				msg = email.message_from_string(tmp_datas[0][1])
				#print 'msg.is_multipart()====',msg.is_multipart()
				#print msg.items()
				#print 'msg.get_params()====',msg.get_params()
				#[('multipart/related', ''), ('boundary', '_005_E19BF6D8FF2B2248A8A5E3793B23BEBAFF06C2CNSZEXMB02_'), ('type', 'text/html')]
				#print 'msg.get_content_type()====',msg.get_content_type()
				#multipart/mixed multipart/related
				#print msg.items()
				#[('Received', 'from CNSZEXMB02.TCT.TCL.com ([10.128.161.154]) by\r\n CNSZEXCH02.TCT.TCL.com ([10.128.161.151]) with mapi id 14.03.0123.003; Tue,\r\n 27 Jun 2017 08:33:28 +0800'), ('From', '"Yan, GONG(WMD PIC HZ SMO-HZ-TCT)" <yan.gong@tcl.com>'), ('To', 'SW-PL2-HZ <sw.pl2.hz@tcl.com>'), ('Subject', 'SW XR Status 20170627'), ('Thread-Topic', 'SW XR Status 20170627'), ('Thread-Index', 'AdLu3TP5bGdMR4yORcqqJY/L8bkb9g=='), ('Date', 'Tue, 27 Jun 2017 08:33:27 +0800'), ('Message-ID', '<E19BF6D8FF2B2248A8A5E3793B23BEBAFF06C2@CNSZEXMB02>'), ('Accept-Language', 'zh-CN, en-US'), ('Content-Language', 'zh-CN'), ('X-MS-Exchange-Organization-AuthAs', 'Internal'), ('X-MS-Exchange-Organization-AuthMechanism', '04'), ('X-MS-Exchange-Organization-AuthSource', 'CNSZEXCH02.TCT.TCL.com'), ('X-MS-Has-Attach', 'yes'), ('X-Auto-Response-Suppress', 'DR, OOF, AutoReply'), ('X-MS-Exchange-Organization-SCL', '-1'), ('X-MS-TNEF-Correlator', ''), ('Content-Type', 'multipart/related;\r\n\tboundary="_005_E19BF6D8FF2B2248A8A5E3793B23BEBAFF06C2CNSZEXMB02_";\r\n\ttype="text/html"'), ('MIME-Version', '1.0')]
				#print msg['from'],msg['to'],msg['subject'],msg['CC'],msg['Date']
				
				if not msg.is_multipart():
					payload = msg.get_payload(decode=True)
					name = msg.get_param("name")
					if name:
						print 'name====',name
					print payload
					get_eserviceid_owner_patchs(payload,self.eserviceid,self.owner,self.patchs)
				if msg.get(u'subject'):
					h = email.Header.Header(unicode(msg.get(u'subject'),'utf-8'))
					if h:
						dh = email.Header.decode_header(h)
						if dh[0][1]:
							subject = unicode(dh[0][0],dh[0][1]).encode('utf-8')
						print subject
		print self.eserviceid,self.owner,self.patchs
def main_email():
	__g_codeset = sys.getdefaultencoding()
	if "ascii"== __g_codeset:
		__g_codeset = locale.getdefaultlocale()[1]
	mail = imap_email()
	mail.login()
	mail.get_mail()
	mail.fetch_mail()
	time.sleep(10)
	mail.logout()
class jenkins(login_internet):
	def __init__(self,url='http://10.92.35.20:8080/jenkins/view/tools/job/mtk-patch-import'):
		super(jenkins,self).__init__(url)
		
	def login(self,url='http://10.92.35.20:8080/jenkins/login?from=%2Fjenkins%2Fview%2Ftools%2Fjob%2Fmtk-patch-import%2F',username='xiaoli.luo',password='123'):
		try:
			self.driver.get(url)
		except NoSuchWindowException,e:
			print "--------------"
			print e
			print "--------------"
			main_network_security()
			self.driver = webdriver.Firefox(firefox_binary=self.firefox_binary,executable_path=self.executable_path)
			time.sleep(3)
			self.driver.get(url)
		super(jenkins,self).login([username,password,''],["j_username","j_password","Submit"],["name","name","name"])	
		
	def release(self,prj,owner,eserviceid='',alps='',moly='',sixth='',has_drivonly=False,build=False,server='10.92.35.17--andoridL-108',url='http://10.92.35.20:8080/jenkins/view/tools/job/mtk-patch-import/release'):
		self.driver.get(url)
		
		patch_owner_email = self.driver.find_element_by_xpath("//input[@value='patch_owner_email']/parent::div/child::input[@type='text']")
		patch_owner_email.click()
		patch_owner_email.clear()
		patch_owner_email.send_keys(owner)
		alps_number = self.driver.find_element_by_xpath("//input[@value='alps_number']/parent::div/child::input[@type='text']")
		alps_number.click()
		alps_number.clear()
		alps_number.send_keys(alps)
		moly_number = self.driver.find_element_by_xpath("//input[@value='moly_number']/parent::div/child::input[@type='text']")
		moly_number.click()
		moly_number.clear()
		moly_number.send_keys(moly)
		sixth_number = self.driver.find_element_by_xpath("//input[@value='sixth_number']/parent::div/child::input[@type='text']")
		sixth_number.click()
		sixth_number.clear()
		sixth_number.send_keys(sixth)
		eservice_ID = self.driver.find_element_by_xpath("//input[@value='eservice_ID']/parent::div/child::input[@type='text']")
		eservice_ID.click()
		eservice_ID.clear()
		eservice_ID.send_keys(eserviceid)
		
		driveonly_import = self.driver.find_element_by_xpath("//input[@value='driveonly_import']/parent::div/child::input[@type='checkbox']")
		if  has_drivonly:
			if not driveonly_import.is_selected():
				driveonly_import.click()
			if build:
				driveonly_build = self.driver.find_element_by_xpath("//input[@value='driveonly_build']/parent::div/child::input[@type='checkbox']")
				if not driveonly_build.is_selected():
					driveonly_build.click()
		else: 
			if driveonly_import.is_selected():
				driveonly_import.click()
			if build:
				normalbranch_build = self.driver.find_element_by_xpath("//input[@value='normalbranch_build']/parent::div/child::input[@type='checkbox']")
				if not normalbranch_build.is_selected():
					normalbranch_build.click()
		project =  self.driver.find_element_by_xpath("//input[@value='project']/parent::div/child::select/option[@value='%s']"%prj)
		project.click()
		select = ''
		_selects = self.driver.find_elements_by_xpath("//input[@value='build_server']/parent::div/select/option")
		#print _selects
		for _select in _selects:
			_select.click()
			#time.sleep(3)
			str_select = _select.get_attribute("value").encode().split('-')[0]
			print str_select,type(str_select),server
			if str_select.find(server.split('-')[0])!=-1:
				print "select::",str_select
				select = _select
		if select:
			select.click()
		Schedule_Release_Build = self.driver.find_element_by_xpath("//button[contains(text(),'Schedule Release Build')]")
		#print Schedule_Release_Build,Schedule_Release_Build.get_attribute('id')
		Schedule_Release_Build.click()
		time.sleep(20)	
	def logout(self):
		self.driver.close()
def main_jenkins(project,owner,eserviceid,alps,moly,sixth,has_drivonly,build,server):
	jk = jenkins()
	jk.login()
	jk.release(project,owner,eserviceid,alps,moly,sixth,has_drivonly,build,server)
	time.sleep(10)
	jk.logout()
class read_prj_xls:
	def __init__(self,wb_file='/local/int_jenkins/mtk_patch/jb3-mp-import.xls'):
		self.project_message = {}
		wb = xlrd.open_workbook(wb_file)
		ap_st = wb.sheet_by_name(u'MTKInfo')
		mp_st = wb.sheet_by_name(u'ModemInfo')
		for row in xrange(0,ap_st.nrows):
			prj = ap_st.cell(row,0).value.strip()
			mtk_prj_alps = ap_st.cell(row,2).value.strip()
			mtk_release_alps = ap_st.cell(row,3).value.strip()
			current_alps_number = str(ap_st.cell(row,23).value)[:-2]
			has_driveonly = True if ap_st.cell(row,11).value.strip() else False
			import_branch = ap_st.cell(row,1).value.strip()
			download_type = ap_st.cell(row,27).value.strip()
			mtk_message = ap_st.cell(row,30).value
			platform = ap_st.cell(row,15).value.strip()
			if download_type == 'git':
				import_branch = 'JRD_' + import_branch
			self.project_message[prj] = {}
			self.project_message[prj]['mtk_prj_alps'] = mtk_prj_alps 
			self.project_message[prj]['mtk_release_alps'] = mtk_release_alps
			self.project_message[prj]['current_alps_number'] = current_alps_number
			self.project_message[prj]['has_driveonly'] = has_driveonly
			self.project_message[prj]['import_branch'] = import_branch
			self.project_message[prj]['download_type'] = download_type
			self.project_message[prj]['mtk_message'] = mtk_message
			self.project_message[prj]['platform'] = platform
		for row in xrange(0,mp_st.nrows):
			prj = mp_st.cell(row,0).value.strip()
			mtk_prj_moly = mp_st.cell(row,2).value.strip()
			mtk_release_moly = mp_st.cell(row,3).value.strip()
			mtk_prj_sixth = mp_st.cell(row,2).value.strip()
			mtk_release_sixth = mp_st.cell(row,4).value.strip()
			platform = ap_st.cell(row,16).value.strip()
			current_moly_number = str(mp_st.cell(row,20).value)[:-2]
			current_sixth_number = str(mp_st.cell(row,21).value)[:-2]
			self.project_message[prj]['mtk_prj_moly'] = mtk_prj_moly
			self.project_message[prj]['mtk_release_moly'] = mtk_release_moly
			self.project_message[prj]['mtk_prj_sixth'] = mtk_prj_sixth
			self.project_message[prj]['mtk_release_sixth'] = mtk_release_sixth
			self.project_message[prj]['current_moly_number'] = current_moly_number
			self.project_message[prj]['current_sixth_number'] = current_sixth_number
			self.project_message[prj]['platform'] = platform
def main_read_xls():
	xls = read_prj_xls()
	print xls.project_message
	#sys.exit(1)
	return xls.project_message
class patchs_reconise:
	def __init__(self,patchs,project_message=''):
		self.patch_list = []
		#print type(patchs),patchs
		if type(patchs) is unicode:
			self.patch_list = patchs.encode().split(',')
		elif type(patchs) is str:
			self.patch_list = patchs.split(',')
		else:
			print "type(patch) error!!!"
			sys.exit(1)
		self._project = ''
		self._patchnum_alps = ''
		self._patchnum_moly = ''
		self._patchnum_sixth = ''
		self.project_message = project_message if project_message else {}
		if not project_message:
			self.read_xls()
	def read_xls(self):
		wb = xlrd.open_workbook('/local/int_jenkins/mtk_patch/jb3-mp-import.xls')
		ap_st = wb.sheet_by_name(u'MTKInfo')
		mp_st = wb.sheet_by_name(u'ModemInfo')
		for row in xrange(0,ap_st.nrows):
			prj = ap_st.cell(row,0).value.strip()
			mtk_prj_alps = ap_st.cell(row,2).value.strip()
			mtk_release_alps = ap_st.cell(row,3).value.strip()
			current_alps_number = str(ap_st.cell(row,23).value)[:-2]
			has_driveonly = True if ap_st.cell(row,11).value.strip() else False
			import_branch = ap_st.cell(row,1).value.strip()
			download_type = ap_st.cell(row,27).value.strip()
			if download_type == 'git':
				import_branch = 'JRD_' + import_branch
			self.project_message[prj] = {}
			self.project_message[prj]['mtk_prj_alps'] = mtk_prj_alps 
			self.project_message[prj]['mtk_release_alps'] = mtk_release_alps
			self.project_message[prj]['current_alps_number'] = current_alps_number
			self.project_message[prj]['has_driveonly'] = has_driveonly
			self.project_message[prj]['import_branch'] = import_branch
		for row in xrange(0,mp_st.nrows):
			prj = mp_st.cell(row,0).value.strip()
			mtk_prj_moly = mp_st.cell(row,2).value.strip()
			mtk_release_moly = mp_st.cell(row,3).value.strip()
			mtk_prj_sixth = mp_st.cell(row,2).value.strip()
			mtk_release_sixth = mp_st.cell(row,4).value.strip()
			current_moly_number = str(mp_st.cell(row,20).value)[:-2]
			current_sixth_number = str(mp_st.cell(row,21).value)[:-2]
			self.project_message[prj]['mtk_prj_moly'] = mtk_prj_moly
			self.project_message[prj]['mtk_release_moly'] = mtk_release_moly
			self.project_message[prj]['mtk_prj_sixth'] = mtk_prj_sixth
			self.project_message[prj]['mtk_release_sixth'] = mtk_release_sixth
			self.project_message[prj]['current_moly_number'] = current_moly_number
			self.project_message[prj]['current_sixth_number'] = current_sixth_number
		
	def echo(self):
		print self.project_message
	def get_number(self,patch):
		match = re.findall(r'[._]P([0-9]+)\)\.tar\.gz',patch)
		if match:
			print match
			assert len(match)==1,"wrong length of patch number"
			return match[-1]
		else:
			match = re.findall(r'\.?([0-9]+)\.xml',patch)
			print 'match,patch',match,patch
			if match:
				print match
				assert len(match)==1,"wrong length of patch number"
				return match[-1]
			else:
				print "something wrong was found on patch number"
				return ''
	def get_project_number(self):
		patch_prj_name = ''
		patch_release_name = ''
		project = ''
		number = ['','','']
		driveonly = False
		#print 'self.patch_list',self.patch_list
		for patch in self.patch_list:
			pt = ''
			if patch.find('SIXTH')!=-1:
				pt = 'SIXTH'
				patch_prj_name = 'mtk_prj_sixth'
				patch_release_name = 'mtk_release_sixth'
			elif patch.find('MOLY')!=-1 and patch.find('CMCC')==-1:
				pt = 'MOLY'
				patch_prj_name = 'mtk_prj_moly'
				patch_release_name = 'mtk_release_moly'
			elif patch.find('ALPS')!=-1 or patch.find('alps')!=-1:
				pt = 'ALPS'
				patch_prj_name = 'mtk_prj_alps'
				patch_release_name = 'mtk_release_alps'
			else:
				print "can not find key words SIXTH OR MOLY OR ALPS"
				continue
			assert patch_prj_name and patch_release_name,"NULL were found on patch_prj_name or patch_release_name"
			for prj in self.project_message.keys():
				_prj_name = self.project_message[prj][patch_prj_name] if self.project_message[prj].has_key(patch_prj_name) else ''
				_release_name = self.project_message[prj][patch_release_name] if self.project_message[prj].has_key(patch_release_name) else ''
				mtk_message = self.project_message[prj]['mtk_message']
				if _prj_name and _release_name and patch.find(_prj_name)!=-1 and patch.find(_release_name)!=-1 and not mtk_message:
					project = prj
					driveonly = self.project_message[prj]['has_driveonly']
					_number = self.get_number(patch)
					if pt=='SIXTH':
						number[2] = _number
					elif pt=='MOLY':
						number[1] = _number
					elif pt=='ALPS':
						number[0] = _number
					else:
						pass
		print "project,number",project,number,driveonly
		return project,number,driveonly
	def reconise_number(self,project='',number=''):
		patch_type=['current_alps_number','current_moly_number','current_sixth_number']
		assert len(number)==len(patch_type),"wrong length of patch number"
		if project and number:
			for n in xrange(0,len(number)):
				if number[n]:
					print patch_type[n],self.project_message[project][patch_type[n]]
					if int(number[n])<=int(self.project_message[project][patch_type[n]]) :
						number = ''
						break
		return project,number
	def is_downloaded(self):
		_downloaded = False
		for patch in self.patch_list:
			print "patch====/local/mtk_patch_import/TODO/%s" % patch
			if os.path.exists("/local/mtk_patch_import/TODO/"+patch):
				_downloaded = True
			if patch.find('.xml')!=-1:
				_downloaded = True
		return _downloaded
	def get_opt_server(self,prj):
		
		opt_servers = []
		if prj:
			import_branch = self.project_message[prj]['import_branch']
			if os.path.exists("/local/mtk_patch_import/%s"%import_branch):
				opt_servers.append("10.92.35.17--androdL-108")
			status = commands.getoutput('if ssh int@10.92.35.18 test -e "/local/mtk_patch_import/%s"; then echo True; else echo False; fi'%import_branch)
			print "status=",status,'if ssh int@10.92.35.18 test -e "/local/mtk_patch_import/%s"; then echo True; else echo False; fi'%import_branch
			if status == "True":
				opt_servers.append("10.92.35.18--MTKpatch")
		return ','.join(opt_servers)

def main_paths_reconise(patchs,project_message):
	patchs = patchs_reconise(patchs,project_message)
	patchs.echo()
	_downloaded = patchs.is_downloaded()
	print "is_downloaded====",_downloaded
	project,number,driveonly = patchs.get_project_number()
	#print project,number
	project,number = patchs.reconise_number(project,number)
	#print project,number
	opt_servers = patchs.get_opt_server(project)
	return project,number,driveonly,_downloaded,opt_servers			
def utf8_to_mbs(s,__g_codeset):
	return s.decode("utf-8").encode(__g_codeset)
def mbs_to_utf8(s,__g_codeset):
	return s.decode(__g_codeset).encode("utf-8")
def pop_item(i,eserviceid=[],owner=[],patchs=[],releasetime=[],project=[],number=[],build=[]):
	assert len(eserviceid)==len(owner)==len(patchs)==len(releasetime),"wrong of list length"
	assert i<len(eserviceid),"pop index out of range"
	eserviceid.pop(i)
	owner.pop(i)
	patchs.pop(i)
	releasetime.pop(i)
	project.pop(i)
	number.pop(i)
	build.pop(i)
def get_build(project,number,build):
	tmp_prj = []
	tmp_num = []
	tmp_i = ''
	for i in xrange(0,len(project)):
		build.append(False)
	for i in xrange(0,len(project)):
		if not project[i] or not number[i]:
			continue
		elif number[i] and not number[i][0]:
			build[i] = False
			continue
		else:
			build[i] = True
			tmp_prj = project[i]
			tmp_num = number[i][0]
			tmp_i = i
		tmp = tmp_i+1
		for j in xrange(tmp,len(project)):
			if tmp_prj and tmp_prj == project[j]:
				if  str(tmp_num) < str(number[j][0]):
					build[tmp_i] = False
					build[j] = True
					tmp_num = number[j][0]
					tmp_i = j
	return build	
def switch_server(server='',opt_servers=[]):
	
	if len(opt_servers)== 1 and opt_servers[0]:
		server = opt_servers[0]
	if len(opt_servers)!= 1 :
		if server == '10.92.35.17--androdL-108':
			server = '10.92.35.18--MTKpatch'
		elif server == '10.92.35.18--MTKpatch':
			server = '10.92.35.17--androdL-108'
		else:
			server = '10.92.35.17--androdL-108'
	return server
def git_push(filename='mtk_patch/jb3-mp-import.xls'):
	os.chdir('/local/int_jenkins')
	print "change dir =====/local/int_jenkins"
	os.system("> /local/release/git_push.log")
	print commands.getoutput("git status | tee -a /local/release/git_push.log")
	print commands.getoutput("git add %s | tee -a /local/release/git_push.log"%filename)
	print commands.getoutput("git commit -m 'update mtk_patch/jb3-mp-import.xls for dayly update' | tee -a /local/release/git_push.log")
	print commands.getoutput("git push origin master | tee -a /local/release/git_push.log")
	error = commands.getoutput("grep -E 'fatal:|error:' /local/release/git_push.log")
	if error:
		print "====git push error start====!!!"
		print error
		print "====git push error end====!!!"
		sys.exit(1)
class modify_xls:
	def __init__(self,project,number,filename='/local/int_jenkins/mtk_patch/jb3-mp-import.xls'):
		wb = xlrd.open_workbook(filename,formatting_info=True)
		self.project = project
		self.number = number
		self.wb = copy(wb)
		self.ap_st = self.wb.get_sheet(0)#u'MTKInfo'
		self.mp_st = self.wb.get_sheet(1)#u'ModemInfo'
		self.row_list = []
		self.col_list = [23,20,21]
		self.get_row_from_prj(wb)
	def get_row_from_prj(self,wb):
		ap_st = wb.sheet_by_name(u'MTKInfo')
		mp_st = wb.sheet_by_name(u'ModemInfo')
		for i in xrange(0,len(self.project)):
			ap_row = ''
			mp_row = ''
			row = []
			for _row in xrange(0,ap_st.nrows):
				if ap_st.cell(_row,0).value.strip() == self.project[i]:
					ap_row = _row
					print "ap_row",ap_row
			for _row in xrange(0,mp_st.nrows):
				if mp_st.cell(_row,0).value.strip() == self.project[i]:
					mp_row = _row
					print "mp_row",mp_row
			if ap_row and mp_row:
				row.append(ap_row)
				row.append(mp_row)
				self.row_list.append(row)
	def modify(self):
		print self.row_list,len(self.project)
		for i in xrange(0,len(self.project)):
			#print "i===",i
			assert self.row_list[i][0] and self.row_list[i][1],"wrong row number"
			if self.number[i][0]:
				self.ap_st.write(self.row_list[i][0],self.col_list[0],int(self.number[i][0]))
			if self.number[i][1]:
				self.mp_st.write(self.row_list[i][1],self.col_list[1],int(self.number[i][1]))
			if self.number[i][2]:
				self.mp_st.write(self.row_list[i][1],self.col_list[2],int(self.number[i][2]))
	def save(self,filename='/local/int_jenkins/mtk_patch/jb3-mp-import.xls'):
		self.wb.save(filename)
def main_modify_xls(project,number):
	xls = modify_xls(project,number)
	xls.modify()
	xls.save()
class Network_security(login_internet):

	def __init__(self,url="https://10.92.63.254/auth.html"):
		super(Network_security,self).__init__(url)
			
	def login(self,username="xiaodan.cheng",passwd="cxd@6235"):
		self.driver.switch_to.frame(self.driver.find_element_by_xpath("/html/frameset/frame[@id='authFrm']"))
		super(Network_security,self).login([username,passwd,''],["html body form div#login_box div#username_line div.fieldValue input#userName","#password_line > div:nth-child(2) > input:nth-child(1)",".button"],["css","css","css"])
		try:
			self.driver.switch_to.default_content()
		except WebDriverException,e:
			print "-------------"
			print e
			print "-------------"
			self.driver.close()
	def logout(self):
		time.sleep(3)
		print self.driver.window_handles
		drivers = self.driver.window_handles
		for driver in drivers:
			self.driver.switch_to.window(driver)
			print self.driver
			print self.driver.title
			self.driver.close()

def main_network_security():
	print "exit network security check for IT changes network settings"
	return
	ns = Network_security()
	ns.login()
	ns.logout()
class git_update:
	def __init__(self,project_message={}):
		self.project_message = project_message
		self.git_update_prj = []
		self.import_branch = []
		self.patchs = []
	def get_newest_patch(self,codedir,project):
		pwd = os.getcwd()
		if os.path.exists(codedir+'/.repo/manifests'):
			print "change dir====",codedir
			os.chdir(codedir)
			os.system("git pull")
			_patchs = commands.getoutput("ls ./release_version/%s -t1 | grep %s" %(self.project_message[project]['platform'], self.project_message[project]['mtk_release_alps'])).split('\n')[0]
			print _patchs
			if _patchs:
				self.patchs.append( _patchs )
		else:
			status = commands.getoutput('if ssh int@10.92.35.18 test -e "%s/.repo/manifests"; then echo True; else echo False; fi'%codedir)
			if status:
				print "ssh int@10.92.35.18 'cd %s/.repo/manifests/ ;git pull'" % codedir
				os.system("ssh int@10.92.35.18 'cd %s/.repo/manifests/ ;git pull'" % codedir)
				print "ssh int@10.92.35.18 'cd %s/.repo/manifests/ ;ls ./release_version/%s -t1 | grep %s'" % (codedir,self.project_message[project]['platform'],self.project_message[project]['mtk_release_alps'])
				_patchs = commands.getoutput("ssh int@10.92.35.18 'cd %s/.repo/manifests/ ;ls ./release_version/%s -t1 | grep %s'" % (codedir,self.project_message[project]['platform'],self.project_message[project]['mtk_release_alps'])).split('\n')[0]
				time.sleep(3)
				print '_patchs',_patchs
				if _patchs:
					self.patchs.append( _patchs )
		print "change dir====",pwd
		os.chdir(pwd)
	def get_git_prj(self):
		for prj in self.project_message.keys():
			if self.project_message[prj]['download_type']=='git' and not self.project_message[prj]['mtk_message'] :
				self.git_update_prj.append(prj)
				print "project_message[prj]['import_branch']",project_message[prj]['import_branch']
				self.import_branch.append(project_message[prj]['import_branch'])
		print 'self.git_update_prj',self.git_update_prj
		print 'self.import_branch',self.import_branch
	def get_patchs(self):
		assert len(self.git_update_prj)==len(self.import_branch),"wrong length of git_update_prj and import_branch"
		for i in xrange(0,len(self.git_update_prj)):
			codedir = '/local/mtk_patch_import/'+self.import_branch[i]
			self.get_newest_patch(codedir,self.git_update_prj[i])
		print 'self.git_update_prj',self.git_update_prj
		print 'self.import_branch',self.import_branch
		print 'self.patchs',self.patchs
		assert len(self.git_update_prj)==len(self.import_branch)==len(self.patchs),"wrong length of git_update_prj and import_branch"
		
def main_git_update(project_message,eserviceid=[],owner=[],patchs=[],releasetime=[]):
	gp = git_update(project_message)
	gp.get_git_prj()
	gp.get_patchs()
	for _patch in gp.patchs:
		eserviceid.append('')
		owner.append('xiaodan.cheng@tcl.com')
		patchs.append(_patch)
		releasetime.append('')
	return eserviceid,owner,patchs,releasetime

if __name__ == '__main__' :
	print 'start'
	print time.ctime(time.time())
	project = []
	number = []
	driveonly = []
	build = []
	job_list = []
	job_number_list = []
	downloaded = []
	opt_servers = []
	server = ''
	patch_type=['current_alps_number','current_moly_number','current_sixth_number']
	project_message = main_read_xls()
	main_network_security()
	#main_qualcomm()
	#time.sleep(20)
	#sys.exit(1)
	eserviceid,owner,patchs,releasetime = main_mtk()
	#eserviceid,owner,patchs,releasetime = main_git_update(project_message,eserviceid,owner,patchs,releasetime)
	for i in xrange(0,len(patchs)):
		_project,_number,_driveonly,_downloaded,_opt_servers = main_paths_reconise(patchs[i],project_message)
		if _project and _number and (_number[0] or _number[1] or _number[2]):
			project.append(_project)
			number.append(_number)
			driveonly.append(_driveonly)
			
		else:
			project.append('')
			number.append('')
			driveonly.append('')
		downloaded.append(_downloaded)
		opt_servers.append(_opt_servers)
	build = get_build(project,number,build)
	server = switch_server(server)
	print project
	print number
	print driveonly
	print build
	print downloaded
	print opt_servers
	assert len(eserviceid)==len(owner)==len(patchs)==len(releasetime)==len(project)==len(number)==len(driveonly)==len(build)==len(downloaded)==len(opt_servers),"wrong of list length"
	main_network_security()
	for i in xrange(0,len(number)):
		if number[i] and project[i] not in job_list and downloaded[i] and ((number[i][0] and (int(number[i][0])==int(project_message[project[i]][patch_type[0]]) + 1)) or (number[i][1] and int(number[i][1])==int(project_message[project[i]][patch_type[1]]) + 1) or (number[i][2] and int(number[i][2])==int(project_message[project[i]][patch_type[2]]) + 1)) :
			server = switch_server(server,opt_servers[i].split(','))
			print 'server===',server
			main_jenkins(project[i],owner[i],eserviceid[i],number[i][0],number[i][1],number[i][2],driveonly[i],build[i],server)
			job_list.append(project[i])
			job_number_list.append(number[i])
		if number[i] and project[i] not in job_list and downloaded[i] and ((number[i][0] and (int(number[i][0])>int(project_message[project[i]][patch_type[0]]) + 1)) or (number[i][1] and int(number[i][1])>int(project_message[project[i]][patch_type[1]]) + 1) or (number[i][2] and int(number[i][2])>int(project_message[project[i]][patch_type[2]]) + 1)) :
			print "patch num error,please check!!!"
			email = patch_num_email(project[i],owner[i],eserviceid[i],number[i][0],number[i][1],number[i][2],driveonly[i],build[i],server)
			email.mail()
			
	print job_list,job_number_list
	#if len(job_list)!=0:
		#main_modify_xls(job_list,job_number_list)
		#git_push()

	print time.ctime(time.time())
	print 'end'
