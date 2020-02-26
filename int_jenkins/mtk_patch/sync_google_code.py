#!/usr/bin/python
#coding=utf-8
#
#git sync google code
#

###
#git sync google code

#cd /local/sdb/cts
#git checkout master
#git pull
#git tag -l


#cd /googletest/CTS
#git checkout master
#git pull

#mv .git ../
#cp -rf ./* /googletest/CTS
#mv ../.git .

#git checkout android-cts-9.0_source

#git add .
#git commit -m "android-cts-9.0_r1"
#git push origin HEAD:android-cts-9.0_source
#
#img_dir			aosp img director
#sdk_platform_tools_dir 	sdk platform-tools director
#adb
import os,sys
import commands
import time,re
sys.path.append("/local/int_jenkins/mtk_patch")
aosp = __import__('sync_google_code')

class operation_state(object):

	def __init__(self,local_dir,google_dir,branch_name,current,current_tag,finish=False,basenames='',basepaths='',manifest="/googletest/manifest"):
		self.local_dir = local_dir
		self.google_dir = google_dir
		self.branch_name = branch_name
		self.current = current
		self.current_tag = current_tag
		self._branch = self.branch_name.split('_')[0]
		self.finish = finish
		self.manifest = manifest
		self.basenames = basenames
		self.basepaths = basepaths
	def handle(self):
		pass
class pwd(operation_state):
	def handle(self):
		print "pwd",commands.getoutput("pwd")
class cd_manifest(operation_state):
	def handle(self):
		print "cd manifest"
		print "cd '%s'" % self.manifest
		os.chdir(self.manifest)
#<project groups="pdk,tradefed" name="platform/build/blueprint" path="build/blueprint" />
class get_basenames(operation_state):
	def handle(self):
		default_xml = self.manifest + '/default.xml'
		fp_strs = ''
		with open(default_xml,'r') as fp:
			fp_strs = fp.readlines()
		fp_strs = ' '.join(fp_strs).replace('\n',' ')
		print fp_strs
		self.basenames = self.get_names(fp_strs)
		self.basepaths = self.get_paths(fp_strs)
	def get_names(self,strs):
		import re
		matchs = re.findall('name="(\S+)"',strs)
		basenames = []
		if len(matchs) >= 1:
			#remove aosp
			basenames = matchs[1:] if matchs[0]=="aosp" else matchs
		print basenames
		return basenames
	def get_paths(self,strs):
		import re
		matchs = re.findall('path="(\S+)"',strs)
		paths = []
		if len(matchs) >= 1:
			#remove aosp
			paths = matchs[1:] if matchs[0]=="aosp" else matchs
		print paths
		return paths
class git_checkout_master(operation_state):
	def handle(self):
		print "git checkout master"
		os.system("git checkout master")

class git_reset_head(operation_state):
	def handle(self):
		print "git reset --hard HEAD"
		os.system("git reset --hard HEAD")

class git_pull(operation_state):
	def handle(self):
		print "git pull"
		os.system("git pull")

class git_tag_l(operation_state):
	def handle(self):
		assert int(self.current) >= 0,"current must be above of 0 ."
		print "git tag -l"
		tmpstr = commands.getoutput("git tag -l")
		print tmpstr
		tag_list = re.findall(r'%s_r([0-9]+)' % self._branch,tmpstr)
		print tag_list
		assert len(tag_list) >= 0,"tag_list must be above of 0 ."
		for tag in tag_list:
			if int(tag) == int(self.current) + 1:
				self.current_tag = tag
		print "current_tag",self.current_tag
		if not self.current_tag:
			self.finish = True
		
class git_checkout_branch(operation_state):
	def handle(self):
		print "git checkout remotes/origin/%s" % self.branch_name
		os.system("git checkout  remotes/origin/%s" % self.branch_name)

class git_checkout_tag(operation_state):
	def handle(self):
		if not self.finish:
			print "git checkout '%s_r%s'" %(self._branch, self.current_tag)
			os.system("git checkout '%s_r%s'" %(self._branch, self.current_tag))

def git_status():
	print "git status"
	tmpstr = commands.getoutput("git status")
	print tmpstr
	matchs = re.findall(r"nothing to commit, working tree clean",tmpstr)
	if len(matchs) > 0:
		return True
	else:
		return False

class git_add(operation_state):
	def handle(self):
		if not git_status() and not self.finish:
			print "git add -A"
			os.system("git add -A")
		print "nothing to commit, working tree clean"

class git_commit(operation_state):
	def handle(self):
		if not git_status() and not self.finish:
			print "git commit -m '%s_r%s'" %(self._branch, self.current_tag)
			os.system("git commit -m '%s_r%s'" %(self._branch, self.current_tag))
		print "nothing to commit, working tree clean"

class git_push(operation_state):
	def handle(self):
		if not self.finish:
			print "git push origin HEAD:%s" % self.branch_name
			os.system("git push origin HEAD:%s" % self.branch_name)


class get_current(operation_state):
	def handle(self):
		print "git log -1"
		#android-cts-9.0_r4
		tmpstr = commands.getoutput("git log -1")
		print "tmpstr===",tmpstr
		match = re.findall(r'_r([0-9]+)$',tmpstr)
		print "match",match
		if match:
			self.current = match[0]
			print "current == %s" % self.current


class mv_git2parent(operation_state):
	def handle(self):
		if not self.finish:
			print "mv .git '%s'" % os.path.dirname(self.google_dir)
			os.system("mv .git '%s'" % os.path.dirname(self.google_dir))

class cp2local(operation_state):
	def handle(self):
		if not self.finish:
			print "cp -rf %s/* '%s'" % (self.google_dir,self.local_dir)
			os.system("cp -rf %s/* '%s'" % (self.google_dir,self.local_dir))

class mv_gitfparent(operation_state):
	def handle(self):
		if not self.finish:
			print "mv %s/.git %s/" %(os.path.dirname(self.google_dir),self.google_dir)
			os.system("mv %s/.git %s/" %(os.path.dirname(self.google_dir),self.google_dir))

class cd_local(operation_state):
	def handle(self):
		print "cd '%s'" % self.local_dir
		os.chdir(self.local_dir)

class cd_google(operation_state):
	def handle(self):
		if os.path.isdir(self.google_dir):
			print "cd '%s'" % self.google_dir
			os.chdir(self.google_dir)
		else:
			if not os.path.isdir(os.path.dirname(self.google_dir)):
				os.system("mkdir -p %s" % os.path.dirname(self.google_dir))
			print "cd %s"% os.path.dirname(self.google_dir)
			os.chdir(os.path.dirname(self.google_dir))
			pathname = self.google_dir.replace(self.manifest+"/","")
			for basename in self.basenames:
				if basename.find(pathname)!= -1:
					print "git clone https://android.googlesource.com/%s" %  basename
					os.system("git clone https://android.googlesource.com/%s" %  basename)
					print "cd '%s'" % self.google_dir
					os.chdir(self.google_dir)
			

class contextFactory(object):
	def __init__(self,operations_list,local_dir,google_dir,branch_name,current='',current_tag='',basenames=''):
		if type(operations_list) is str:
			operations_list = [operations_list]
		if type(operations_list) is list:
			self.operations_list = operations_list
		self.state = ''
		self.local_dir = local_dir
		self.google_dir = google_dir
		self.current_tag = current_tag
		self.current = current
		self.finish = False
		self.branch_name = branch_name
		self.basenames = basenames
		self.basepaths = ''
	def __next__(self):
		if len(self.operations_list) >0:
			self.operations_list.remove(self.operations_list[0])
			return True
		return False
	def __gene_state__(self):
		if len(self.operations_list)<=0:
			return ''
		_oper = self.check_expression(self.operations_list[0])
		if _oper:
			self.state = getattr(aosp,_oper)(self.local_dir,self.google_dir,self.branch_name,self.current,self.current_tag,self.finish,self.basenames,self.basepaths)
			_next = self.__next__()
			if _next:
				return self.state
			else:
				return ''
		return ''
	def check_expression(self,operation):
		if operation:
			return 	operation
		return ''
	def operate(self):
		_state = self. __gene_state__()
		if _state:
			self.state.handle()
			self.current_tag =self.state.current_tag if self.state.current_tag else self.current_tag
			self.current =self.state.current if self.state.current else self.current
			self.finish = self.state.finish
			self.basenames = self.state.basenames
			self.basepaths = self.state.basepaths
			#print "self.current_tag,self.current,self.finish",self.current_tag,self.current,self.finish
	def check_fish(self):
		if len(self.operations_list) > 0 and not self.finish :
			return True
		return False

if __name__ == '__main__':
	google_dir = '/local/sdb/cts'
	local_dir = '/googletest/CTS'
	branch_list = ['android-cts-9.0_source','android-cts-8.1_source','android-cts-10.0_source']
	pwd = os.getcwd()
	sys.path.append(pwd)
	
	for branch_name in branch_list:
		print "branch_name",branch_name
		operations = ['cd_local','git_checkout_master','git_reset_head','git_pull','git_checkout_branch','get_current',\
			'cd_google','git_checkout_master','git_reset_head','git_pull','git_tag_l',\
			'git_checkout_tag','mv_git2parent','cp2local','mv_gitfparent',\
			'cd_local','git_add','git_commit','git_push']
		factory = contextFactory(operations,local_dir,google_dir,branch_name)
		while factory.check_fish():
			factory.operate()




