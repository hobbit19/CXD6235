#!/usr/bin/python
#-*- encoding: utf-8 -*-

import sys
#sys.path.append("/local/int_tools/lib")
from Integrity import IntegrityClient



wsdl = 'https://172.24.147.72:7003/webservices/10/2/Integrity/?wsdl'

defect_fields = ('ID', 'Type', 'Created By', 'State','Blocks')
testcase_fields = ('ID', 'Type', 'Created By', 'State', 'Assigned User', 'Project', 'Summary', 'Blocked By','Last Result')
Project_Sql = '((field[Project]="/TCT/MTK MT6735M/Pixi3-4.5 4G"))'




def getProjectsTestCase(intClient,almProject_Sql):
    query = ('(' \
             '(field[Type]="Test Case") and ' \
             '%s' \
             ')')%(almProject_Sql)
    Testcases = intClient.getItemsByCustomQuery(fields=['ID','Summary','Last Result'], query=query)
    for Testcase in Testcases:
        print Testcase['Last Result'][1].value
        print Testcase['Summary'][1].value
        print Testcase['ID'][1].value



if __name__ == '__main__':

    client = IntegrityClient(wsdl, credential_username="hudson.admin.hz", credential_password="Hzsw#123")
    ""
    #use x['Description'][1].value to get the Description, do not use x['Description'].longtext.value, it may be shortext

    print 'get defect user project '
    j = client.getItem(item_id=392065, fields=defect_fields)
    print j['Blocks']

    k = client.getItem(item_id=172431, fields=testcase_fields)

    print k['Blocked By']
    print k['Last Result']
    print k['Type']
    d = {'Regression Comment': 'Regression defect'}
    resp = client.editItem(item_id=420884, **d)
    print resp
    #getProjectsTestCase(client,Project_Sql)



