#!/usr/bin/python

from subprocess import Popen, PIPE
import notify2
from os.path import expanduser
import os
import logging
import time

APROX_DELAY = 40
PATH = expanduser("~")+"/Documents/ranking2/"

logging.basicConfig(filename=PATH+'upload.log',
					filemode='a', 
					level=logging.INFO, 
					format='%(asctime)s %(message)s', 
					datefmt='%m/%d/%Y %I:%M:%S %p')

os.chdir(PATH)

nextFile = open("tasks/nextTaskFile.txt", 'r');
nextFileNum = nextFile.readline().strip();
nextFile.close();

nextFile = open('tasks/nextTaskFile.txt', 'w');
nextFile.write(str(int(nextFileNum)+1));
nextFile.close();

logging.info('## File '+nextFileNum+' ##')

time.sleep(APROX_DELAY)

try:
	notify2.init('Task Uploader');
	notif = notify2.Notification('Task file number '+nextFileNum+' is uploading.')
	notif.show()
except:
	pass

p = Popen(['/usr/local/bin/pbs add_tasks --tasks-file tasks/tasks_'+nextFileNum+'.csv --tasks-type csv'], stderr=PIPE, stdout=PIPE, shell=True)
logging.info("Tasks added")
out, err = p.communicate()

if out != "":
	logging.info(out)

if err != "":
	logging.critical(err)

try:
	notif.update('Task file number '+nextFileNum+' finished uploading')
	notif.show()
except:
	pass