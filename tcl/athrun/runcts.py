#!/usr/bin/python
##############################
#
##############################
import os
import sys
import pexpect


class runcts:
    def __init__(self, ctspath, sessionid,dvicesid,shardsrun,platform):
        self.ctspath = ctspath
        self.sessionid = sessionid
        self.dvicesid = dvicesid
        self.shardsrun = shardsrun
        self.platform = platform

    def run(self):
        if self.platform == 'androidN':
          try:
            os.system("chmod 0755 "+self.ctspath)		
            child = pexpect.spawn(self.ctspath)
            child.expect('cts-tf >')
            if self.shardsrun == "true":
                child.sendline('run cts --retry ' + self.sessionid)
            else:
                child.sendline('run cts -s '+ self.dvicesid + ' --retry ' + self.sessionid)
            child.interact()
          except OSError:
            sys.exit(0)
        else:
          try:
            os.system("chmod 0755 "+self.ctspath)		
            child = pexpect.spawn(self.ctspath)
            child.expect('cts-tf >')
            if self.shardsrun == "true":
                child.sendline('run cts  --continue-session ' + self.sessionid)
            else:
                child.sendline('run cts -s '+ self.dvicesid + ' --continue-session ' + self.sessionid)
            child.interact()
          except OSError:
            sys.exit(0)

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print 'Please give the run cts file path!'
        sys.exit()
    #print "sys.argv",sys.argv
    if len(sys.argv) == 5:
        rc = runcts(sys.argv[1], '0',sys.argv[2],sys.argv[3],sys.argv[4])
    else:
        rc = runcts(sys.argv[1], sys.argv[2],sys.argv[3],sys.argv[4],sys.argv[5])

    rc.run()
