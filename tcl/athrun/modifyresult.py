#!/usr/bin/python
##############################
#
##############################

import os
import sys
import re


class modifyresult:

    def __init__(self, path, kind,platform):
        if kind != 'all' and kind != 'failed' and kind != 'timeout':
            print 'Error: wrong kind!'
            sys.exit()

        self.xmlPath = path
        self.kind = kind
        self.platform = platform

    def parse(self):
        if os.path.exists(self.xmlPath):
            xmlFile = open(self.xmlPath, 'r')
        else:
            print 'Error: you give the wrong path:%s' % (self.xmlPath)
            sys.exit()

        lines = xmlFile.readlines()
        xmlFile.close()

        if os.path.exists(self.xmlPath + '.ind'):
            os.remove(self.xmlPath + '.ind')
        if os.path.exists(self.xmlPath + '.bak'):
            os.remove(self.xmlPath + '.bak')
        #print self.xmlPath
        os.rename(self.xmlPath, self.xmlPath + '.bak')

        xmlFile = open(self.xmlPath, 'w')
        isfailed = False
        isskip = False
        testitemresult = 'notExecuted'
        #print lines
        if self.platform == "androidN":
          for line in lines:
#<Summary pass="344409" failed="494" not_executed="0" modules_done="121" modules_total="277" />
            if line.find('<Summary pass') > 0:
                m_txt = r' *<Summary\s+pass=\"(.+)\"\s+failed=\"(.+)\"\s' \
                        r'not_executed=\"(.+)\"\s+modules_done=\"(.+)\"\s' \
                        r'modules_total=\"(.+)\" />'
                m = re.match(m_txt, line)
                if m is None:
                    print 'Error: XML file format error(wrong <Summary...)!'
                    isfailed = True
                    break

                xmlFile.write(line)

            elif line.find('<Test result') > 0:
                #print line
                test = r'<Test\s+result=\"(.+)\"\s+name=\"(.+)\"'
                m = re.search(test, line)
                if m is None:
                    print 'Error: XML file format error(wrong <Test endtime.)!'
                    isfailed = True
                    break
                testitemresult = m.group(1)
                #print testitemresult
                #t = '<Test result="%s" name="%s"/>\n'
                #if self.kind == 'failed' and m.group(1) == 'fail':
                  #  line = t % ('not_executed',m.group(2))
                   # print "fail"
                    #print line

               #elif self.kind == 'timeout' and m.group(1) == 'timeout':
                   # line = t % ('not_executed',m.group(2))

               #elif self.kind == 'all' and m.group(1) != 'pass':
                  #  line = t % ('not_executed',m.group(2))

                xmlFile.write(line)

            elif line.find('<FailedScene') > 0:
                if self.kind != 'failed' and self.kind != 'all':
                    xmlFile.write(line)
                    continue

                if testitemresult == 'pass':
                    xmlFile.write(line)
                    continue

                isskip = True

            elif line.find('</Test>') > 0:
                if isskip is False:
                    xmlFile.write(line)

                isskip = False

            else:
                if isskip is False:
                    xmlFile.write(line)


         
        else:
          for line in lines:
            if line.find('<Summary') > 0:
                m_txt = r' *<Summary\s+failed=\"(.+)\"\s+not' \
                        r'Executed=\"(.+)\"\s+time' \
                        r'out=\"(.+)\"\s+pass=\"(.+)\" />'
                m = re.match(m_txt, line)
                if m is None:
                    print 'Error: XML file format error(wrong <Summary...)!'
                    isfailed = True
                    break

                linet = '  <Summary failed=\"%s\" not'  \
                    'Executed=\"%s\"  timeout=\"%s\" pass=\"%s\" />\n'
                if self.kind == 'failed':
                    notExecutednum = int(m.group(1)) + int(m.group(2))
                    line = linet % (0, notExecutednum, m.group(3), m.group(4))

                elif self.kind == 'timeout':
                    notExecutednum = int(m.group(2)) + int(m.group(3))
                    line = linet % (m.group(1), notExecutednum, 0, m.group(4))

                elif self.kind == 'all':
                    notExecutednum = int(m.group(1)) + int(m.group(2)) + \
                        int(m.group(3))
                    line = linet % (0, notExecutednum, 0, m.group(4))

                xmlFile.write(line)

            elif line.find('<Test name') > 0:
                test = r'<Test\s+name=\"(.+)\"\s+resul' \
                    r't=\"(.+)\"\s+starttime=\"(.+)\"\s+endtime=\"(.+)\"'
                m = re.search(test, line)
                if m is None:
                    print 'Error: XML file format error(wrong <Test endtime.)!'
                    isfailed = True
                    break
                testitemresult = m.group(2)
                t = '<Test name="%s" result="%s" starttime="%s" end'
                x = 'time="%s"/>\n'
                if self.kind == 'failed' and m.group(2) == 'fail':
                    line = t % (m.group(1), 'notExecuted', m.group(3)) + \
                        x % m.group(4)

                elif self.kind == 'timeout' and m.group(2) == 'timeout':
                    line = t % (m.group(1), 'notExecuted', m.group(3)) + \
                        x % m.group(4)

                elif self.kind == 'all' and m.group(2) != 'pass':
                    line = t % (m.group(1), 'notExecuted', m.group(3)) + \
                        x % m.group(4)

                xmlFile.write(line)

            elif line.find('<FailedScene') > 0:
                if self.kind != 'failed' and self.kind != 'all':
                    xmlFile.write(line)
                    continue

                if testitemresult == 'pass':
                    xmlFile.write(line)
                    continue

                isskip = True

            elif line.find('</Test>') > 0:
                if isskip is False:
                    xmlFile.write(line)

                isskip = False

            else:
                if isskip is False:
                    xmlFile.write(line)

        xmlFile.close()

        if isfailed is True:
            os.remove(self.xmlPath)
            os.rename(self.xmlPath + '.bak', self.xmlPath)
        else:
            os.remove(self.xmlPath + '.bak')

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print 'Please give the xml file path!'
        sys.exit()

    if len(sys.argv) == 2:
        px = modifyresult(sys.argv[1], 'all')
    elif len(sys.argv) == 3:
        px = modifyresult(sys.argv[1], 'all','androidM')
    else:
        px = modifyresult(sys.argv[1], sys.argv[2],sys.argv[3])

    px.parse()
