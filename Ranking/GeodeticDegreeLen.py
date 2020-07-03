"""
LENGTH OF A DEGREE OF LATITUDE AND LONGITUDE BY COORDINATE

Calculates length of a degree of latitude and longitude based on geodetic 
meridian for any latitude and longitude position on an elipsoid without need of 
any external API or data. Constants based on elipsoid values used in WGS84, 
replicating calculation used b National Geospatial Agency (NDA) and CSGnet.

Formula is in a format that minimizes error at high latitudes by not dividing 
by cosines (like haversine calculations).

:param lat_degrees:		Latitude at postion for which you want degree lengths
:param units: 			Unit of measure for distnace returned. Default is 'km'
            			Other values: 'mi', 'nm', 'm', 'ft'

Result is a pair of floating point distances (lat_length, long_length)
	in unit distance 

Recommended for simple distance and geofence calculations below 88 degrees 
latutude and for distances and dimensions up to 200 km. Calculation has 
error rate <0.1% error at equator, under 3% (longitude) at the poles.  
"""
import sys
from math import sin, cos, pi, pow, degrees, radians

## Values for Earth, could swap in Mars, the Moon, etc.
EARTH_EQUATORIAL_RADIUS = 6378.137 # km
EARTH_POLAR_RADIUS = 6356.7523142 # km
EARTH_ECCENTRICITY_SQUARED = 0.00669437999014 

a = EARTH_EQUATORIAL_RADIUS
b = EARTH_POLAR_RADIUS
e_squared = EARTH_ECCENTRICITY_SQUARED

def format_latitude(latitude):
	# Cleanup latitudes that have gone over 90 degrees
	# Needed for correct radian conversion.
	# Then conversion to radians
	latitude = float(latitude or 0.0)
	if abs(latitude) > 90:
		latitude = ((latitude + 90) % 180) - 90
	return radians(latitude)

def format_longitude(longitude):
	# Cleanup longitudes that have gone over 180 degrees
	# Needed for correct radian conversion.
	# Then conversion to radians
	longitude = float(longitude or 0.0)
	if abs(longitude) > 180:
		longitude = ((longitude + 180) % 360) - 180
	return radians(longitude)

def get_degree_len(lat_degrees, units='km'):
	lat_radians = format_latitude(lat_degrees)

	# Meters (and Km) are based on EARTH_POLAR RADIUS
	# As such, meters and km distances are default of merdian calculation
	# Non-metric units will need to be adjusted based on conversion
	if units == 'km':
		conversion = 1.0 # km per km
	elif units == 'mi':
		conversion = 1/1.609 # mi per km
	elif units == 'm':
		conversion = 1000.0 # m per km
	elif units == 'ft':
		conversion = 5280/1.609 # ft per km
	elif units == 'nm':
		conversion = 1/1.852 # nm per km
	else:
		# Bail on invalid unity, can raise exception instead
		sys.exit("Error: %r is an invalid unit." % units)

	lat_length = (pi * a * (1.0 - e_squared)) / \
		(180.0 * pow(1 - (e_squared * pow(sin(lat_radians), 2)), 1.5)) \
		* conversion

	long_length = (pi * a * cos(lat_radians)) / \
		(180.0 * pow(1 - (e_squared * pow(sin(lat_radians), 2)), 0.5)) \
		* conversion
	return lat_length, long_length

if __name__ == '__main__':
	### TESTING IT OUT FOR YOURSELF
	# Input is latitude in degrees and unit measure
	lat = float(raw_input("Latitude (degrees)?: "))
	unit = str(raw_input("Units (km, m, mi, nm, ft)?: "))

	# Showing how pair values are returned
	dlat, dlong = get_degree_len(lat, unit)
	print "At %f degrees Latitude:" % lat
	print "\t 1 Degree of Latitude is %f %s long" % (dlat, unit)
	print "\t 1 Degree of Longitude is %f %s long\n" % (dlong, unit)