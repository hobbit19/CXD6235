#!/usr/bin/python
#-*- encoding: utf-8 -*- 
import sys
sys.path.append('/local/int_tools/lib')
from Integrity import IntegrityClient

wsdl = 'http://172.24.147.71:7003/webservices/10/2/Integrity/?wsdl'

client = IntegrityClient(wsdl, credential_username="ruifeng.dong", credential_password="mobile#3")
""
#use x['Description'][1].value to get the Description, do not use x['Description'].longtext.value, it may be shortext

x = client.createDefect(**{ "Project":"/TCT/MTK MT6582M/Soul-4.5",
                                "Summary":"test_by_feng",
                                "Component":"Framework",
                                "Function":"APN",
                                "Priority":"P2 (Normal)",
                                "State":"New",
                                "SW Release": "SW6A22",
                                "Regression": "NO",
                                "Detection": "2 - very difficult to detect-> <1%",
                                "Perso ID": "ppppppp",
                                "HOMO": "NO",
                                "Severity": "2 - no gravity->no or little trouble",
                                "Frequency": "2 - very difficult to reproduce->less than 1 time out of 100",
                                "singleBranch":"mtk6582-v1.0-dint"})
print x







