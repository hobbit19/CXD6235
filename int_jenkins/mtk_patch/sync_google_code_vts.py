#!/usr/bin/python
#coding=utf-8

import os,sys
import commands
import time,re
sys.path.append("/local/int_jenkins/mtk_patch")
aosp = __import__('sync_google_code')
print aosp
contextFactory = getattr(aosp,"contextFactory")

#AfwTestHarness	https://android.googlesource.com/platform/test/AfwTestHarness
#framework https://android.googlesource.com/platform/test/framework
#mlts/benchmark https://android.googlesource.com/platform/test/mlts/benchmark
#mlts/models https://android.googlesource.com/platform/test/mlts/models
#p2pts https://android.googlesource.com/platform/test/p2pts
#sts https://android.googlesource.com/platform/test/sts
#suite_harness https://android.googlesource.com/platform/test/suite_harness
#vti/alert https://android.googlesource.com/platform/test/vti/alert
#vti/dashboard https://android.googlesource.com/platform/test/vti/dashboard
#vti/fuzz_test_serving https://android.googlesource.com/platform/test/vti/fuzz_test_serving
#vti/test_serving https://android.googlesource.com/platform/test/vti/test_serving
#vts https://android.googlesource.com/platform/test/vts
#vts-testcase/fuzz https://android.googlesource.com/platform/test/vts-testcase/fuzz
#vts-testcase/hal https://android.googlesource.com/platform/test/vts-testcase/hal
#vts-testcase/hal-trace https://android.googlesource.com/platform/test/vts-testcase/hal-trace
#vts-testcase/kernel https://android.googlesource.com/platform/test/vts-testcase/kernel
#vts-testcase/nbu https://android.googlesource.com/platform/test/vts-testcase/nbu
#vts-testcase/performance https://android.googlesource.com/platform/test/vts-testcase/performance
#vts-testcase/security https://android.googlesource.com/platform/test/vts-testcase/security
#vts-testcase/vndk https://android.googlesource.com/platform/test/vts-testcase/vndk


if __name__ == '__main__':
	google_basenames = ['suite_harness','vts','framework','sts','vti/alert','vti/test_serving','vti/dashboard','vti/fuzz_test_serving','vts-testcase/nbu','vts-testcase/security','vts-testcase/kernel','vts-testcase/hal','vts-testcase/fuzz','vts-testcase/performance','vts-testcase/hal-trace','vts-testcase/vndk']
	#google_dir = '/local/sdb/test'
	google_dir = '/local/sdb/auto_sync'
	local_dir = '/googletest/VTS'
	branch_list = ['android-vts-9.0_source','android-vts-8.1_source','android-vts-10.0_source']
	
	for branch_name in branch_list:
		print "branch_name",branch_name
		#checkout local
		operations = ['cd_local','git_checkout_master','git_reset_head','git_pull','git_checkout_branch','get_current']
		factory = contextFactory(operations,local_dir,google_dir,branch_name)
		while factory.check_fish():
			factory.operate()
		current = factory.current
		current_tag = factory.current_tag
		print "current,current_tag",current,current_tag
		#current = 6
		#current_tag = 6
		#get manifest basename
		operations = ['cd_manifest','git_checkout_master','git_reset_head','git_pull','git_tag_l',\
			'git_checkout_tag','get_basenames']
		factory = contextFactory(operations,local_dir,google_dir,branch_name,current,current_tag)
		while factory.check_fish():
			factory.operate()
		google_basenames = factory.basenames
		google_basepaths = factory.basepaths
		current = factory.current
		current_tag = factory.current_tag
		#print "google_basenames",google_basenames
		#sys.exit(1)
		
		for google_basepath in google_basepaths:
			ldir = local_dir + '/' + google_basepath
			gdir = google_dir+'/' + google_basepath
			print "ldir,gdir",ldir,gdir
			operations = ['cd_google','git_checkout_master','git_reset_head','git_pull','git_tag_l',\
				'git_checkout_tag','mv_git2parent','cp2local','mv_gitfparent']
			factory = contextFactory(operations,ldir,gdir,branch_name,current,current_tag,google_basenames)
			while factory.check_fish():
				factory.operate()
		current = factory.current
		current_tag = factory.current_tag
		print "current,current_tag",current,current_tag
		#commit to local
		operations = ['cd_local','git_add','git_commit','git_push']
		factory = contextFactory(operations,local_dir,google_dir,branch_name,current,current_tag)
		while factory.check_fish():
			factory.operate()
