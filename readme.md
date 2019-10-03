DeanFly
======================= 

3D graphics educational rudimental flight simulator to illustrate concepts such as Clipping, Perspective, Hidden Surface Removal, Double Buffering etc.
This is by no means a polished product and should be used only to demonstrate the some of the concepts used within 3D graphics programming.  This software was originally developed in C++ during a 3D Graphics module for a BSc Computing for Real-Time Systems degree and the University of the West of England UWE.  This Watcom C++ source was later ported to Java purely as a learning exercise.  Minor changes have been made since this was ported to enable compilation under java 1.7.

View from Field 1: 
!["Image of Field 1 from the ground"]("https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-Field1.png")


Controls
-----------

* S	Start
* Q	Quit (Second Q will exit the application)
* C	Continue following a quit

* +/-	Speed
* a	Roll Left
* d	Roll Right
* s	Nose Up
* w	Nose Down 
* 1	Yaw Left
* 3	Yaw Right

* i	Instrumentation
* m	map
* M	Clip Map
* h	Help



Cheat Controls
-----------
* k	Cheat Key Help
* '.'	Reset to Air Field 1
* v or ]	Scroll between each view / vehicle
* N	North
* E	East
* S	South
* W	West
* (	Decrease altitude
* )	Decrease altitude

* Mouse Left button	move to field 1 takeoff position
* Mouse Right button	move to in flight position
* Mouse Center button	High Altitude, nose down


3D Educational Controls
-----------
* b	Fill/Wireframe mode
* c	Clipping on/off
* C	Screen Clipping on/off
* h	Hidden Surface Removal
* ?	Surface Sort


Map Views
-----------

The Map: 
!["Image of Map"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-Map.png)

The Clip Map: 
!["Image of the Clip Map"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-ClipMap.png)


In Flight
-----------

View from High Altitude: 
!["Image from High Altitude"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-High.png)


Wireframe View
-----------

Wireframe View from Field 1: 
!["Wireframe View"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-Wireframe.png)



Running the application
-----------

 
#### To compile:

*     ant

#### To run:
   
   * export CLASSPATH=$CLASSPATH:DeanFly.jar
   * or
   * cd into folder containing DeanFly.class

   * java -Xms1024m DeanFly

    

#### To rebuild Eclipse Project:
*     mvn eclipse:clean eclipse:eclipse -DdownloadSources -Declipse:useProjectReferences=false -Dwtpversion=2.0

#### JUnit
* 	from within Eclipse right click the DeanFly project and Run As->JUnit Test
	
	
#### Ant build of jar file
*     ant createjar
