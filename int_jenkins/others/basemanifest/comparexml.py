import re
import sys
import os
#dint=open("/local/code/upload/int_jenkins/others/basemanifest/mtk6739-n-v1.0-import-update.xml")
#impor=open("mtk6739-n-v1.0-import.xml")
dint=open(sys.argv[1])
impor=open(sys.argv[2])
dlines=dint.readlines()
ilines=impor.readlines()
dgits=[]
igits=[]
for line in dlines:
    m=re.search(r"name(.*)path",line)
    if (m):
        dgits.append(m.group(0).split('"')[1]) 
dint.close()
for line in ilines:
    m=re.search(r"name(.*)path",line)
    if (m):
        igits.append(m.group(0).split('"')[1]) 
impor.close()
ff=open("more.txt","w+")
for i in range(len(dgits)):
    if dgits[i] not in igits:
        ff.write(dgits[i]+"\n")
ff.close()
print ("compare end")
os.system('gedit ./more.txt')

