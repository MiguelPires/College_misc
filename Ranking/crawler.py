#!/usr/bin/python
from PIL import Image
import urllib
import cStringIO
from fabric.api import local
import os
import GeodeticDegreeLen
from geopy.geocoders import GoogleV3
import csv
import logging
from os.path import expanduser
import math

# URL parameters
BASE_URL = "https://maps.googleapis.com/maps/api/streetview?"
SIZE = "640x640"
PITCH = "10"
FOV = "120"

RADIUS = 1000 # meters
STEP = 50 # meters
QUESTION =  "Some random question?"
KEY = "AIzaSyChrmC0RhokOSwREzgN15SJEmXo03Ij7lw"

numCities = 0
citiesFile = open('cities', 'r')

fileNum = 0
#taskWriter = None

percentages = []
taskNumber = 0


logging.basicConfig(filename=expanduser("~")+"/Documents/ranking2/crawler.log",
                    filemode='a', 
                    level=logging.INFO, 
                    format='%(asctime)s %(message)s', 
                    datefmt='%m/%d/%Y %I:%M:%S %p')

def openTaskFile(fileNumber):
    global csvfile
    csvfile = open('tasks/tasks_'+str(fileNumber)+'.csv', 'w')
    global taskWriter
    taskWriter = csv.writer(csvfile, delimiter=',',
                                quotechar='"', quoting=csv.QUOTE_MINIMAL)
    taskWriter.writerow(["question", "city", "url"])

def closeTaskFile():
    csvfile.close()

openTaskFile(fileNum)

for city in citiesFile:
    numCities += 1
    total = 0.0
    misses = 0.0

    print city
    geolocatorGoogle = GoogleV3()
    loc = geolocatorGoogle.geocode(city)
    centerLatitude = loc.latitude
    centerLongitude = loc.longitude

    degreeLength = GeodeticDegreeLen.get_degree_len(loc.latitude, 'm')
    latLength, lonLength = degreeLength
    latDelta = STEP / latLength
    lonDelta = STEP / lonLength

    minimumLat = centerLatitude - RADIUS / latLength 
    maximumLat = centerLatitude + RADIUS / latLength 

    minimumLon = centerLongitude - RADIUS / lonLength
    maximumLon = centerLongitude + RADIUS / lonLength

    print(centerLatitude, centerLongitude)

    latitude = minimumLat
    while latitude <= maximumLat:
        longitude = minimumLon

        while longitude <= maximumLon:

            url = BASE_URL+"size="+SIZE+"&location=" + str(latitude) + "," + str(longitude) + \
            "&pitch="+PITCH+"&key="+KEY
            print url
            file = cStringIO.StringIO(urllib.urlopen(url).read())

            img = Image.open(file)
            img.save("image.jpeg", "jpeg")
            sizeFile = (os.path.getsize("image.jpeg"))
            local("rm -f image.jpeg")

            if sizeFile == 8290 or sizeFile == 16743:
                print "No image"
                misses += 1.0
                longitude += lonDelta
                continue

            taskWriter.writerow([QUESTION, city.strip(), url])

            total += 1.0
            taskNumber += 1
            if taskNumber == 100:
                logging.info("Opening file no. "+str(fileNum))
                closeTaskFile()
                taskNumber = 0
                fileNum += 1
                openTaskFile(fileNum)


            longitude += lonDelta
        latitude += latDelta

    percentages.append(misses/total)
    logging.info("Misses in {}: {0:.2f}%".format(str(city).strip(), float(misses/total*100.0)))

percentagesTotal = 0.0
for per in percentages:
    percentagesTotal += per
    logging.info('Global misses: {0:.2f}%'.format(float(percentagesTotal / numCities*100.0)))

closeTaskFile()

