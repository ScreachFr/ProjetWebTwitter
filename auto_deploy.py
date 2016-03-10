# Dependencies: requests, pyinotify(optionnaly, for the watch feature)

# Configuration:
archiveName = 'NOM1_NOM2'
baseurl = ''
user = ''
password = ''

# Usage:
#	Build and deploy:
#		execute the script inside the root directort of your project.
#		the script will create the .war file and deploy it.
#
#	Watch and deploy:
#		execute the script with the argument 'watch' (python3 auto_deploy.py watch)
#		in the directory where you export your .war file
#		every time you export your .war file with eclipse, the script will automatically deploy it.
#

#-----------------------------------------------

baseurl='http://'+user+':'+password+'@'+baseurl


import requests, time, os, subprocess

from html.parser import HTMLParser

class MyHTMLParser(HTMLParser):
	def __init__(self):
		super().__init__()
		self.undeployUrl = None
		self.uploadUrl = None
	def handle_starttag(self, tag, attrs):
		uploadUrl = None
		undeployUrl = None
		if tag == 'form':
			for k, v in attrs:
				if k == 'action' and archiveName.replace('_','%5F') in v and 'undeploy' in v:
					undeployUrl = v
				if k == 'action' and '/manager/html/upload' in v:
					uploadUrl = v
		if undeployUrl:
			self.undeployUrl = undeployUrl
		if uploadUrl:
			self.uploadUrl = uploadUrl
	



def trigger(fileWar=archiveName+'.war'):
	r = requests.get(baseurl+'/manager/html')
	parser = MyHTMLParser()	
	parser.feed(r.text)
	if parser.undeployUrl:
		r = requests.post(baseurl+parser.undeployUrl)
		print('removed project')
		time.sleep(1)
		r = requests.get(baseurl+'/manager/html')
		parser = MyHTMLParser()	
		parser.feed(r.text)
	if parser.uploadUrl:
		files = {'deployWar': open(fileWar, 'rb')}
		r = requests.post(baseurl+parser.uploadUrl, files=files)
		print('added project')

def build():
	if os.path.isfile('/tmp/'+ archiveName+'.war'):
		os.remove('/tmp/'+ archiveName+'.war')
	if os.path.isdir('/tmp/'+ archiveName+'WarArchive'):
		subprocess.call('rm -r /tmp/'+ archiveName+'WarArchive', shell=True)
	subprocess.call('cp -r WebContent /tmp/'+ archiveName+'WarArchive', shell=True)
	subprocess.call('cp -r build/classes /tmp/'+ archiveName+'WarArchive/WEB-INF/classes', shell=True)
	subprocess.call('jar -cvf /tmp/'+ archiveName+'.war ' + '*', shell=True, cwd='/tmp/'+ archiveName+'WarArchive/')
	return '/tmp/'+ archiveName+'.war'


def build_and_deploy():
	trigger(fileWar = build())

def watch():
	import pyinotify

	class Handler(pyinotify.ProcessEvent):
		def process_IN_CLOSE_WRITE(self, event):
			trigger()


	handler = Handler()
	wm = pyinotify.WatchManager()
	notifier = pyinotify.Notifier(wm, handler)
	wdd = wm.add_watch(archiveName+'.war', pyinotify.IN_CLOSE_WRITE)
	notifier.loop()

import sys
if __name__ == '__main__':
	if len(sys.argv) > 1 and sys.argv[1] == 'watch':
		watch()
	if len(sys.argv) > 1 and sys.argv[1] == 'deploy':
		trigger()
	else:
		build_and_deploy()