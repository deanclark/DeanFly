DeanFly
======================= 

3D graphics educational rudimental flight simulator to illustrate concepts such as Clipping, Perspective, Hidden Surface Removal, Double Buffering etc.
This is by no means a polished product and should be used only to demonstrate the some of the concepts used within 3D graphics programming.  This software was originally developed in C++ during a 3D Graphics module for a BSc Computing for Real-Time Systems degree and the University of the West of England UWE.  This Watcom C++ source was later ported to Java purely as a learning exercise.  Minor changes have been made since this was ported to enable compilation under java 1.7.

View from Field 1: 
![alt text][2][1]
  [2]: Doxygen/Screenshot-DeanFly-Field1.png
  [1]: https://github.com/deanclark/DeanFly/Doxygen/Screenshot-DeanFly-Field1.png "Image of Field 1 from the ground"


Controls
-----------

S	Start
Q	Quit (Second Q will exit the application)
C	Continue following a quit

+/-	Speed
a	Roll Left
d	Roll Right
s	Nose Up
w	Nose Down 
1	Yaw Left
3	Yaw Right

i	Instrumentation
m	map
M	Clip Map
h	Help



Cheat Controls
-----------
k	Cheat Key Help
.	Reset to Air Field 1
v or ]	Scroll between each view / vehicle
N	North
E	East
S	South
W	West
(	Decrease altitude
)	Decrease altitude

Mouse Left button	move to field 1 takeoff position
Mouse Right button	move to in flight position
Mouse Center button	High Altitude, nose down


3D Educational Controls
-----------
b	Fill/Wireframe mode
c	Clipping on/off
C	Screen Clipping on/off
	Hidden Surface Removal
	Surface Sort


Map Views
-----------

The Map: 
![alt text][4][3]
  [4]: Doxygen/Screenshot-DeanFly-Map.png
  [3]: https://github.com/deanclark/DeanFly/Doxygen/Screenshot-DeanFly-Map.png "Image of Map"

The Clip Map: 
![alt text][6][5]
  [6]: Doxygen/Screenshot-DeanFly-ClipMap.png
  [5]: https://github.com/deanclark/DeanFly/Doxygen/Screenshot-DeanFly-ClipMap.png "Image of the Clip Map"


In Flight
-----------

View from High Altitude: 
![alt text][8][7]
  [8]: Doxygen/Screenshot-DeanFly-High.png
  [7]: https://github.com/deanclark/DeanFly/Doxygen/Screenshot-DeanFly-High.png "Image from High Altitude"


Wireframe View
-----------

Wireframe View from Field 1: 
![alt text][10][9]
  [10]: Doxygen/Screenshot-DeanFly-Wireframe.png
  [9]: https://github.com/deanclark/DeanFly/Doxygen/Screenshot-DeanFly-Wireframe.png "Wireframe View"



Running the application
-----------

To compile:

    ant

To run:
   
   export CLASSPATH=$CLASSPATH:DeanFly.jar
   or
   cd into folder containing DeanFly.class

   java -Xms1024m DeanFly

    

To rebuild Eclipse Project:
    mvn eclipse:clean eclipse:eclipse -DdownloadSources -Declipse:useProjectReferences=false -Dwtpversion=2.0

JUnit
	from within Eclipse right click the DeanFly project and Run As->JUnit Test
	
	
Ant build of jar file
    ant createjar
