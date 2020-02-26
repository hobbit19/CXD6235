#!/usr/bin/python
#
#create xml file for GEO file
#e.g
#<pathlist>
#<excludepath dir="/system/app/FaceLock" mcc="659,634" />
#</pathlist>
import xml.dom.minidom
import sys
sys.path.append("/local/int_jenkins/fxd")
xlrd = __import__("xlrd")
class geo(object):
	
	def __init__(self):
		self.wb = ''
		self.sheet = ''
		self.country_name_mcc = {}
		self.apk_countries = {}
		self.geo_filename = "/local/int_jenkins/geo/Google_Product_Geo-Availability_Chart_on_Android.xlsx"
		self.sname_list = ["Optional GMS Apps","Regular GMS Apps","Go GMS Apps"]
		self.option_sheet_name = "Optional GMS Apps"
		self.regular_sheet_name = "Regular GMS Apps"
		self.go_sheet_name = "Go GMS Apps"
		self.geo_eea_filename = "/local/int_jenkins/geo/Google_Product_Geo-Availability_Chart_for_the_EEA.xlsx"
		self.apk_mcc = {}
	def main(self):
		self.read_xml()
		print self.country_name_mcc
		self.read_xls(self.geo_filename,self.sname_list,5,2)
		print self.apk_countries
		self.read_xls(self.geo_eea_filename,self.sname_list,5,2)
		print self.apk_countries
		self.get_apk_mcc()
		self.display()
		#print self.apk_mcc
	def display(self):
		for (apk,mcc) in self.apk_mcc.items():
			if mcc == ['460', '634']:
				continue
			else:
				print apk,','.join(mcc)
	def read_xls(self,filename,sheetnames,drow,dcol):

		wb = xlrd.open_workbook(filename)
		for sheetname in sheetnames:
			sheet = wb.sheet_by_name(sheetname)
			for row in xrange(drow,sheet.nrows):
				for col in xrange(dcol,sheet.ncols):
					apk_name = sheet.cell(2,col).value.replace("\n"," ")
					country_name = sheet.cell(row,0).value
					if apk_name not in self.apk_countries:
						self.apk_countries[apk_name] = []
					cell_value = sheet.cell(row,col).value.strip()
					print apk_name,country_name,cell_value
					if cell_value == "N":
						self.apk_countries[apk_name].append(country_name)
		
	def read_xml(self,filename="/local/int_jenkins/geo/countries.xml",Tag_name="country",a_name="name",a_mcc="mcc"):
	
		domtree = xml.dom.minidom.parse(filename)
		countries = domtree.getElementsByTagName(Tag_name)
		for country in countries:
			name = country.getAttribute(a_name)
			mcc = country.getAttribute(a_mcc)
			self.country_name_mcc[name] = mcc

	def get_apk_mcc(self):
		for (apk,countries) in self.apk_countries.items():
			if apk not in self.apk_mcc:
				self.apk_mcc[apk] = []
			for country in countries:
				if country in self.country_name_mcc and self.country_name_mcc[country] and self.country_name_mcc[country] not in self.apk_mcc[apk]:
					self.apk_mcc[apk].append(self.country_name_mcc[country])
if __name__ == '__main__':

	geo1 = geo()
	geo1.main()
