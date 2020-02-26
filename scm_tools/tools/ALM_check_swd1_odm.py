#!/usr/bin/python

import sys
import os
import re
import urllib
import urllib2
#import pygtk
import tempfile
#pygtk.require('2.0')
#import gtk

DEFECT_SUMMARY=''
DEFECT_BRANCH=''

def target(project):
	#url = 'http://10.92.32.10/gitweb.cgi?p=scm_tools.git;a=blob_plain;f=conf/config'
	url = "/local/tools/scm_tools/conf/config"
	f = urllib.urlopen(url, proxies={})
	for line in f:
		m = re.match('^\s*([^\s]+)\s+([^\s]+)\s+([^\s]+)', line)
		if m:
			if m.group(1) == project:
				return m.group(3)
	return None

def utc_required(response):
	p = r'<.*?:ItemField Name="UTC Required\?"><.*:boolean><.*:value>(false|true)</.*:value></.*:boolean>'
	m = re.search(p, response)
	if m:
		if m.group(1) == 'true':
			return True
	return False

def get_branch(response):
	p = r'<.*:RelatedItem FieldName="singleBranch"><ns1:IBPL>(.*)</ns1:IBPL></ns1:RelatedItem>'	
	m = re.match(p, response)
	if m:
		url = "https://172.24.147.72:7003/webservices/10/2/Integrity/"
		username = "hz.int"
		password = "ptc"
		return get_alm_branch(m.group(1), url, username, password)
	else:
		return 'Get Branch Error!!!'

def get_summary(response):
	p = r'<.*?:ItemField Name="Summary"><.*:shorttext><.*:value>(.*)</.*:value></.*:shorttext>'
	m = re.search(p, response)
	if m:
		return m.group(1)
	return 'Get Summary Error!!!'

def utc_in_response(response):
	p = r'<.*:RelatedItem FieldName="Related UTC">(<.*:Item>\d+</.*:Item>)+</.*:RelatedItem>'
	return re.search(p, response)


def defect_task_in_response(response):
	return (">Defect<" in response or ">Task<" in response)


def check_state_in_response(response):
	return (">Opened<" in response or ">Resolved<" in response)


errors = {
	200: 'OK',
	201: 'State not Resolved or Opened',
	206: 'This id doesn\'t have any related utc',
	404: 'This id deosn\'t exist in Integrity',
	500: 'Error while requesting the Integrity Server',
	505: 'NO Platform Supported'
}

def get_alm_branch(id, url, username, password):
	'''
		200 defect Y, utc Y
		206 defect Y, utc N
		404 defect N
		500 c\s error
	'''
	msg = \
'''<soapenv:Envelope
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:int="http://webservice.mks.com/10/2/Integrity"
	xmlns:sch="http://webservice.mks.com/10/2/Integrity/schema">
	<soapenv:Header/>
	<soapenv:Body>
	<int:getItem>
	 <arg0 sch:ItemId="%s">
		<sch:Username>%s</sch:Username>
		<sch:Password>%s</sch:Password>
		<sch:InputField>Related UTC</sch:InputField>
		<sch:InputField>UTC Required?</sch:InputField>
		<sch:InputField>Summary</sch:InputField>

	 </arg0>
	</int:getItem>
	</soapenv:Body>
</soapenv:Envelope>'''

	raw_data = msg%(str(id), username, password)
	headers = {'SOAPAction': u'""',
        'Content-Type': 'text/xml;charset=UTF-8',
        'Connection': 'close',
        'Content-Length': str(len(raw_data))}
	req = urllib2.Request(url, raw_data, headers)
	try:
		f = urllib2.urlopen(req, timeout=20)
		response = f.read()
	except urllib2.HTTPError, e:
		return 404
	except urllib2.URLError, e:
		return 500

	#print response

 	global DEFECT_BRANCH
	DEFECT_BRANCH = get_summary(response)
	return DEFECT_BRANCH 

def raw_alm_process(id, url, username, password):
	'''
		200 defect Y, utc Y
		206 defect Y, utc N
		404 defect N
		500 c\s error
	'''
	msg = \
'''<soapenv:Envelope
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:int="http://webservice.mks.com/10/2/Integrity"
	xmlns:sch="http://webservice.mks.com/10/2/Integrity/schema">
	<soapenv:Header/>
	<soapenv:Body>
	<int:getItem>
	 <arg0 sch:ItemId="%s">
		<sch:Username>%s</sch:Username>
		<sch:Password>%s</sch:Password>
		<sch:InputField>Type</sch:InputField>
		<sch:InputField>State</sch:InputField>
		<sch:InputField>Related UTC</sch:InputField>
		<sch:InputField>UTC Required?</sch:InputField>
		<sch:InputField>Summary</sch:InputField>
		<sch:InputField>singleBranch</sch:InputField>

	 </arg0>
	</int:getItem>
	</soapenv:Body>
</soapenv:Envelope>'''

	raw_data = msg%(str(id), username, password)
	headers = {'SOAPAction': u'""',
        'Content-Type': 'text/xml;charset=UTF-8',
        'Connection': 'close',
        'Content-Length': str(len(raw_data))}
	req = urllib2.Request(url, raw_data, headers)
	try:
		f = urllib2.urlopen(req, timeout=20)
		response = f.read()
	except urllib2.HTTPError, e:
		return 404
	except urllib2.URLError, e:
		return 500

	print response

 	global DEFECT_SUMMARY
	DEFECT_SUMMARY = get_summary(response)

 	global DEFECT_BRANCH
	DEFECT_BRANCH = get_branch(response)

	if check_state_in_response(response):
		if defect_task_in_response(response):
			if utc_required(response):
				if utc_in_response(response):
					return 200
				else:
					return 206
			else:
				return 200
		else:
			return 404
	else:
		return 600




def legal_ID(project, item_id):
	t =target(project)
	if not t:
		return 505
	if t.upper() == 'ALM':
		url = "https://172.24.147.72:7003/webservices/10/2/Integrity/"
		username = "hz.int"
		password = "ptc"
		return raw_alm_process(item_id, url, username, password)
	return 200


if __name__ == '__main__':
	#flag = legal_ID('pixi3-45-v2.0-dint',340544)
	flag = legal_ID(sys.argv[1],sys.argv[2])
	print DEFECT_SUMMARY
	print DEFECT_BRANCH
	print flag
