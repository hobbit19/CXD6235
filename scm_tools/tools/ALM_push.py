#!/usr/bin/python


import sys
#sys.path.append("/local/int_tools/lib")
import ssl
ssl.match_hostname = lambda cert, hostname: True
if hasattr(ssl, '_create_unverified_context'):
    ssl._create_default_https_context = ssl._create_unverified_context

from Integrity import IntegrityClient



wsdl = 'https://alm.tclcom.com:7003/webservices/10/2/Integrity/?wsdl'

defect_fields = ('ID', 'Type', 'Created By', 'State','Blocks')
testcase_fields = ('ID', 'Type', 'Created By', 'State', 'Assigned User', 'Project', 'Summary', 'Blocked By','Last Result')


def alm_push():
    client = IntegrityClient(wsdl, credential_username=sys.argv[3], credential_password=sys.argv[4])
    d = {'Regression Response': 'YES','Regression Comment': sys.argv[2]}
    try:
        resp = client.editItem(item_id=sys.argv[1], **d)
    except Exception, e:
        error_message = e.fault['detail']['MKSException'].value
        if error_message.strip().startswith('Authentication failed'):
            print 'AuthFail'

if __name__ == '__main__':
    alm_push()



