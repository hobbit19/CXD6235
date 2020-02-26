#!/usr/bin/python
#################################################################################################
# sign project from here, if project didn't sign, pls don't process this class
# create by jianbo.deng 2013-03-15
#################################################################################################

import os
import sys

from Utils import *
from Config import *

def main():
	conf = Config();
	conf.addFromArg(sys.argv[1:])
	signProject = conf.getConf('signProject','signProject','jrd_common')
	## add by jianbo.deng for mtk project 2014-01-24
	mtkProject = conf.getConf('mtkProject','mtkProject', 'none')
	#Added by Yufei.qin for Pixi3-45-4g new branch eng version signature---1BCX  20150423
	version = conf.getConf('version','version', 'none')
	docmd('cp /local/int_tools/securityteam/%s/signStart.py .' %signProject)
	docmd('cp /local/int_tools/securityteam/AutoSign.sh .')
	docmd('chmod a+x signStart.py')
	docmd('chmod a+x AutoSign.sh')
        #Modified by Yufei.qin for Pixi3-45-4g new branch eng version signature---1BCX  20150423
        if version == 'none':
            if mtkProject == 'none':
		docmd('./AutoSign.sh %s' %signProject)
	    else:
		docmd('./AutoSign.sh %s %s' %(signProject, mtkProject))
	else:
		docmd('./AutoSign.sh %s %s %s' %(signProject, mtkProject, version))
        
if __name__ == '__main__':
	main()
