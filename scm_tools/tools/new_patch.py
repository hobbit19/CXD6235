#!/usr/bin/python
import os
import sys
from commands import *

sourcepatch = getoutput("pwd")

scriptfile = sys.argv[0]
scriptpath = os.path.dirname(scriptfile)

os.chdir(scriptpath)
os.system("pwd")
print "--------start update scm_tools!-----------"
os.system("git reset --hard HEAD && git pull origin master")
print "--------end update scm_tools!-------------"
os.chdir(sourcepatch)
os.system("pwd")

print "--------------start to do patch_deliver-------------"
print "%s/swd1_patch.php" %scriptpath
os.system("%s/swd1_patch.php" %scriptpath)
print "--------end to do patch_deliver-------------"


