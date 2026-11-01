DeanFly
======================= 

3D graphics educational rudimental flight simulator to illustrate concepts such as Clipping, Perspective, Hidden Surface Removal, Double Buffering etc.
This is by no means a polished product and should be used only to demonstrate the some of the concepts used within 3D graphics programming.  This software was originally developed in C++ during a 3D Graphics module for a BSc Computing for Real-Time Systems degree and the University of the West of England UWE.  This Watcom C++ source was later ported to Java purely as a learning exercise.  Minor changes have been made since this was ported to enable compilation under modern Java (now built with Gradle on JDK 25).

View from Field 1: 
!["Image of Field 1 from the ground"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-Field1.png)


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
!["Wireframe View<BR>"](https://github.com/deanclark/DeanFly/blob/master/Doxygen/Screenshot-DeanFly-Wireframe.png)



Running the application
-----------

#### Requirements
* JDK 17 or newer (built and tested with JDK 25; the Gradle toolchain is set to 25 in `build.gradle`)
* Nothing else - the Gradle wrapper downloads Gradle 9.8.2 on first use. No external libraries are needed.
* Optional: [Doxygen](https://www.doxygen.nl) (and Graphviz) to build the documentation

#### To compile:

*     ./gradlew build        (Windows: gradlew.bat build)

This produces `build/libs/DeanFly.jar`.

#### To run (must be run from the project root, the world data is loaded relative to it):

*     ./gradlew run                       (islandWorld, the default)
*     ./gradlew run -Pworld=islandWorld
*     ./gradlew run -Pworld=cubeWorld

Or directly with Java after building:

*     java -Xms1024m -cp build/classes/java/main DeanFly cubeWorld

#### VS Code
Open the folder in VS Code with the "Extension Pack for Java" installed. Use the **Run and Debug** panel
("DeanFly - islandWorld" / "DeanFly - cubeWorld"), or **Terminal > Run Task** for the Gradle build, run and Doxygen tasks.

#### Documentation (Doxygen)

*     ./gradlew doxygen

Output goes to `build/doxygen/html/index.html` (the checked-in `Doxygen/html` is left untouched).

#### Eclipse / NetBeans
The old Ant/NetBeans build has been removed. Import the project into Eclipse or NetBeans as a Gradle project.

#### Java version notes
The code compiles on JDK 25 with no removal warnings. The former `JApplet` base class has been replaced by `JPanel`, so
the Applet API (removed from the JDK in version 26) is no longer used. The applet HTML pages under `web/` are obsolete.
See `CLAUDE.md` for details.
