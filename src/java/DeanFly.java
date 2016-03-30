/**
 ***************************************************************************************************
* Copyright Crystalwink Ltd. 2000.  All Rights Reserved.  The copyright to the computer program(s)
* herein is the property of Crystalwink Ltd. The program(s) may be used and/or copied only with the 
* written permission of Crystalwink Ltd or in accordance with the terms & conditions in the agreement  
* or contract under which the program(s) are supplied. This copyright notice must not be removed. 
****************************************************************************************************
** Flight Simulator
*                  
* By:            Dean Clark
* Date:        16-April-2001
*                           
* Compiled:    Java Development Kit jdk1.3.0_01 or jdk1.2.2
* Compiled:    Java Development Kit C:\j2sdk1.4.1_02\bin\javac.exe %f
* Editor:        UltraEdit v10.10b TabStop=4 Indent=4                 
*                                                                      
*    History:                                                         
*        31/10/2000                                                   
*        xx/xx/2001 - Bug Fix - 3D rotation error                     
*        14/04/2001 -                                                 
*        16/04/2001 - First Port from Watcom-C source using JDK 1.2.2 
*        17/04/2001 -
*        09/05/2001 -
*        16/05/2001 -
*        18/05/2001 -
*        21/05/2001 -
*        09/06/2001 -
*        18/06/2001 -
*        20/06/2001 -
*        25/06/2001 -
*        08/07/2001 -
*        xx/xx/2001 - Add object loader                              
*        xx/xx/2001 - World loader                                   
*        xx/xx/2001 - Craft loader                                   
*        xx/xx/2001 - Screen Clipping                                
*        xx/xx/2001 - CubeWorld                                      
*        03/12/2003 - Rebuild using JDK 1.4.1_02                     
*        04/12/2003 - Investigate KeyEvent Input issue               
*        07/12/2003 - Fix Copy Object method, and relocate cloned object
*        08/12/2003 -     
*        25/01/2004 - Attempt to build as an application for JProbe optimisation
*                Current state Builds but no graphic displayed                                                        
*        23/01/2007 - Fix graphic breakage and build using NetBeans 4.1
*                     after building using NetBeans 4.1 copy .class files to ..\build\web
*            2016 - build with jdk 1.7
*            2016 - add Doxygen support
*            2016 - fix continue/quit dialog box
*            2016 - make screen scaler consistent through out
*            2016 - add switch for local and online world object loading 
*            2016 - Red Bull Air Race Style tri-colour pylon cone, 25m tall, 5m diameter and the base and 0.75m at the top
*            
* Wish List:
*    Check for memory leaks
*    Object file, Only load one object file for each object and copy any duplicate objects from the first
*    *Screen clipping, clip the view against the screen rectangle
*    Texture mapping, on buildings etc.
*    Lighting, guess what!
*    other Crafts, add other crafts to the world that interact with the player
*    Landing, differentiate between landing and crashing
*    Collision detection (craft against any object)
*    *World loader, load the world information from file to allow for different worlds
*    *Craft loader, load craft information to allow for different types of craft and movement characteristics
*    Moving objects within the world, allow for things such as windmills etc.
*    Hidden surface removal
*    Remote Controlled view point - RC helicopter, Plane, Car, WalkerBot etc.
*    Port to Xbox and or Playstation II
*    Auto Pilot and way point guidance
*    Airbus Drones:  Eurohak HALE UAS, DVF 2000, Harfang MALE UAS, KZO UAS, Tracker, TR-50 SCOUT UAS, Barracuda
***************************************************************************************************
*/

import java.lang.Math;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * @author dean.clark
 *
 */
public class DeanFly extends JApplet implements ActionListener, ItemListener {
    static GraphPnl mainPanel;

    // controlPanel used for testing the print-to-printer function
    static JPanel controlPanel; // 2007 JPanel
    public static String hostURLparameter = ""; // "http://dean.seetech.com/DeanFly/"
    public static String globalDebug = "false";
    public static String worldName = "islandWorld";  // "cubeWorld"; // cubeWorld or islandWorld (case sensitive)
    public static String worldLocation;
    public static String serverObjects;
    public static String landMass;
    public static String landItems;
    public static String craftType;

    // DeanFly Debug Trace
    static boolean appletLanuch = false;
    static boolean debug = false, verboseDebug = false;
        static boolean applicationMode = false;

    public void init() {
        // DeanFly Debug Trace
        // debug = false;
        // debug = true;
        // verboseDebug = false;
        // verboseDebug = true;
        setLayout(new BorderLayout());

        if (debug && verboseDebug) {
            System.out.println("init");
        }

        mainPanel = new GraphPnl(this);
        add("Center", mainPanel);

        // controlPanel used for testing the print-to-printer function
        controlPanel = new JPanel(); // 2007 JPanel
        add("South", controlPanel);
        
        if(appletLanuch)
        {
                hostURLparameter = getParameter("homeServerPath");
                // DC-Debug: Nothing is done with this Debug Trace parameter yet
                globalDebug = getParameter("verbose");
                worldName = getParameter("world");
        }

        worldLocation = new String(hostURLparameter.concat(worldName));    
        serverObjects = new String(worldLocation.concat("/objectsnew/"));    
        landMass = new String(worldLocation.concat("/landMass.txt"));    
        landItems = new String(worldLocation.concat("/landItems.txt"));    
        craftType = new String(worldLocation.concat("/craftType.txt"));    

        if (debug && verboseDebug) {
            System.out.println("end init");
        }
        
        // controlPanel used for testing the print-to-printer function
        //controlPanel.add(print); print.addActionListener(this);
    }

        ////////////////////////////////////////////////////////////////////////////////////
    static int width = 50;
    static int height = 50;
    static int defaultWidth = 50;
    static int defaultHeight = 50;
    static Dimension screenSizeDimention;
    static Dimension viewSizeDimention;
    static Thread mainExecutionThread = null;
    

    public void mainInit() {
        // DeanFly Debug Trace
        //        debug = false;
        //        boolean debug = true;
        //        verboseDebug = false;
        //        verboseDebug = true;
        //        setLayout(new BorderLayout());

        if (debug && verboseDebug) {
            System.out.println("mainInit");
        }

        
        //hostURLparameter = "http://127.0.0.1/DeanFlyJava/";
        //hostURLparameter = "http://localhost:8084/DeanFly/";   // NetBeans Debug 4.1
        hostURLparameter = "/home/dean.clark/DeanFly-29-03-2016/";
        if(!GraphPnl.runlocal)
            hostURLparameter = "http://www.dean.seetech.com/DeanFly/build/web/";   // NetBeans Debug 4.1
        // DCDebug: Nothing is done with this Debug Trace parameter yet
        globalDebug = "true";
        
        worldLocation = new String(hostURLparameter.concat(worldName));    
        serverObjects = new String(worldLocation.concat("/objectsnew/"));    
        landMass = new String(worldLocation.concat("/landMass.txt"));    
        landItems = new String(worldLocation.concat("/landItems.txt"));    
        craftType = new String(worldLocation.concat("/craftType.txt"));    

                System.out.println(" mainInit() " + craftType + "!\n");

        if (debug && verboseDebug) {
            System.out.println("end init");
        }

        
        // controlPanel used for testing the print-to-printer function
        //controlPanel.add(print); print.addActionListener(this);
        
        GraphPnl.loadCraft();
        //  craft = new loadCraft();
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            GraphPnl.initAircraftNew(GraphPnl.crafts[i]);
        }

        GraphPnl.loadLandMass();
        GraphPnl.nLandMassObjects = GraphPnl.nWorldObjects;

        GraphPnl.loadLandItems();


        // reset all sorted_lists of polys to initial values
        for (int eachObject=0; eachObject<GraphPnl.nWorldObjects; eachObject++)
        {
            // Move objects to each desired location
            GraphPnl.initObject(GraphPnl.objects[eachObject], GraphPnl.objects[eachObject].initX, GraphPnl.objects[eachObject].initY, GraphPnl.objects[eachObject].initZ, GraphPnl.objects[eachObject].preRotation, GraphPnl.objects[eachObject].scale);     // set object location and rotation to 0
            //initObject(objects[eachObject], initX, initY, initZ, preRotation, scale);     // set object location and rotation to 0

            // sorted object list
            GraphPnl.sortedObjectList[eachObject] = eachObject;

            // sorted poly list for each object
            for (int i=0; i<(GraphPnl.objects[eachObject].polys); i++)
            {
                GraphPnl.objects[eachObject].sorted_list[i] = i;
            }
        }

        // start with the Splash screen

        // TODO DCDEBUG uncomment the following line to reenable the splash screen when the keyboard issue is resolved
        //GraphPnl.quit = true;
    
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        JFrame frame = new JFrame("DeanFly");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        screenSizeDimention = Toolkit.getDefaultToolkit().getScreenSize();
        int width = defaultWidth = screenSizeDimention.width;
        int height = defaultHeight = screenSizeDimention.height;
    
        //client.setPreferredSize(new Dimension(width, height));
        frame.setSize(width, height);
        frame.setLocation((screenSizeDimention.width - width) / 2, (screenSizeDimention.height - height) / 2);
    
        GraphPnl mainPanel = new GraphPnl(GraphPnl.graphDeanFly);
        try {
            frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        } catch (java.lang.NullPointerException e) {
                System.out.println(" NullPointerException in main() when adding mainPanel!\n");
        }
        
        // visible screen size
        GraphPnl.craft.windowWidth = screenSizeDimention.width   / GraphPnl.SCREEN_SCALER;
        GraphPnl.craft.windowHeight = screenSizeDimention.height / GraphPnl.SCREEN_SCALER;
    
        viewSizeDimention = Toolkit.getDefaultToolkit().getScreenSize();
        viewSizeDimention.setSize(GraphPnl.craft.windowWidth, GraphPnl.craft.windowHeight);  // small display size
        
        frame.setMinimumSize(viewSizeDimention);
        frame.pack();  // makes tiny see setMinimumSize
        frame.setVisible(true);

        // main game loop
        while (!GraphPnl.exit)
        {
            // display Title Screen
            //displaySplash();
            mainPanel.displaySplash(GraphPnl.offgraphics, GraphPnl.crafts[GraphPnl.currentCraft]);
            
    
            while ( (GraphPnl.relaxer == mainExecutionThread) && (GraphPnl.quit == false))
            {
    
                // determine new craft location and orientation
                GraphPnl.distance = GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.distance_travelled;
                if (GraphPnl.crafts[GraphPnl.currentCraft].Speed > 0)
                    GraphPnl.curvature = (GraphPnl.Gravity * Math.sin(GraphPnl.crafts[GraphPnl.currentCraft].Roll)) / (GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.crafts[GraphPnl.currentCraft].Speed * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Roll));
            
                GraphPnl.crafts[GraphPnl.currentCraft].Yaw  -= GraphPnl.distance * 10.00 * GraphPnl.curvature;  // ???? this should be in Radians ???????
                GraphPnl.crafts[GraphPnl.currentCraft].eyeX += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeZ += GraphPnl.distance * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeY += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Pitch);
    
                // move this
                GraphPnl.crafts[GraphPnl.currentCraft].windowWidth = defaultWidth/GraphPnl.SCREEN_SCALER;
                GraphPnl.crafts[GraphPnl.currentCraft].windowHeight = defaultHeight/GraphPnl.SCREEN_SCALER;
    
    
                
                if (GraphPnl.crafts[GraphPnl.currentCraft].eyeY < Craft.MINIMUM_ALTITUDE)
                {
                    GraphPnl.crash = true;
                    GraphPnl.quit = true;
                    GraphPnl.continueAllowed = false;
    
                    // DCDebug: not implemented yet
                    // Switch to demo mode
                    GraphPnl.demoMode = true;
                }
    
                mainPanel.repaint();
                    
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            } // end while ( (relaxer == me) && (quit == false))
    
    
            
            //repaint();
            if (GraphPnl.quit == true)
            {
                // give the processor a rest
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
        } // end while (exit == false)
    
        //repaint();
        try {
            Thread.sleep(10000);
    
        } catch (InterruptedException e) {
            }
        
    }


    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
    }

    
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        boolean on = e.getStateChange() == ItemEvent.SELECTED;
        //if (src == stress) mainPanel.stress = on;
    }

    
    public void destroy() {
        remove(mainPanel);

        // controlPanel used for testing the print-to-printer function
        remove(controlPanel);
    }


    public void start() {
        mainPanel.start();
    }

    
    public void stop() {
        mainPanel.stop();
    }

    
    int XfromPolar (int modulus, double argument) {
    /* returns the value of X when given valid polar parameters */
            int x;
            argument *= (Math.PI / 180);                            /* degrees to radians. */
            x = (int)(modulus * (Math.cos (argument )));
            return (x);
    }

    
    int YfromPolar (int modulus, double argument) {
    /* returns the value of Y when given valid polar parameters */
        int y;
        argument *= (Math.PI / 180);                            /* degrees to radians. */
        y = (int)(modulus * (Math.sin(argument )));
        return (y);
    }

    
    double Modulus (int x, int y) {
    /* returns the modulas of the given rectangular parameters */
        double modulus;
        modulus = Math.sqrt( (x * x) + (y * y) );
        return (modulus);
    }

    
    double Argument (double x, double y) {
        double argument = 0.0;

        if ( (x > 0.0) && (y > 0.0) )    {                /* first quad */
            argument = (Math.atan (y / x));
            argument = (argument / (Math.PI / 180));            /* radians to degrees. */
            return (argument); }
        if ( (x < 0.0) && (y > 0.0) )    {                /* second quad */
            argument = Math.atan (y / -x);
            argument = argument / (Math.PI / 180);            /* radians to degrees. */
            argument = 180 - argument;
            return (argument);          }
        if ( (x < 0.0) && (y < 0.0) )    {                /* third quad */
            argument = Math.atan (y / x);
            argument = argument / (Math.PI / 180);            /* radians to degrees. */
            argument += 180;
            return (argument); }
        if ( (x > 0.0) && (y < 0.0) )  {                /* forth quad */
            argument = (Math.atan (y / x));
            argument = (argument / (Math.PI / 180));            /* radians to degrees. */
            argument = (360 + argument);
            return (argument); }
        if ( (x > 0.0) && (y == 0.0) ) {
            argument = 0;
            return (argument); }
        if ( (x == 0.0) && (y > 0.0) ) {
            argument = 90;
            return (argument); }
        if ( (x < 0.0) && (y == 0.0) ) {
            argument = 180;
            return (argument); }
        if ( (x == 0.0) && (y < 0.0) ) {
            argument = 270;
            return (argument); }
        return (argument);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // 2006
    public static void main(String argv[]) {
        // initialise global debug trace
        boolean debug = false, verboseDebug = false;

        screenSizeDimention = Toolkit.getDefaultToolkit().getScreenSize();
        defaultWidth = screenSizeDimention.width / 10;
        defaultHeight = screenSizeDimention.height / 10;
        int width = defaultWidth;
        int height = defaultHeight;

        mainExecutionThread = Thread.currentThread();
        GraphPnl.relaxer = mainExecutionThread;

        // Application/Applet differences
        applicationMode = true;
        
        // Reset Crafts 23-01-2007
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            GraphPnl.crafts[i] = null;
        }
        GraphPnl.availableCrafts = 0;
                
        // Create graphics frame
        DeanFly aDeanFly = new DeanFly();
        aDeanFly.mainInit();

    } // end main 2006

    
    

    public void run() {
        // initialise global debug trace
        boolean debug = false, verboseDebug = false;

        if(GraphPnl.availableCrafts != 0 )
            System.out.println("GraphPnl.availableCrafts not zero at init time - possible memory leak");
                    
        Thread mainRunThread = Thread.currentThread();

                // Reset Crafts 23-01-2007
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            GraphPnl.crafts[i] = null;
        }
                GraphPnl.availableCrafts = 0;
                
        GraphPnl.loadCraft();
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            GraphPnl.initAircraftNew(GraphPnl.crafts[i]);
        }


        GraphPnl.loadLandMass();
        GraphPnl.nLandMassObjects = GraphPnl.nWorldObjects;

        GraphPnl.loadLandItems();
    

        // reset all sorted_lists of polys to initial values
        for (int eachObject=0; eachObject<GraphPnl.nWorldObjects; eachObject++)
        {
            // Move objects to each desired location
            GraphPnl.initObject(GraphPnl.objects[eachObject], GraphPnl.objects[eachObject].initX, GraphPnl.objects[eachObject].initY, GraphPnl.objects[eachObject].initZ, GraphPnl.objects[eachObject].preRotation, GraphPnl.objects[eachObject].scale);     // set object location and rotation to 0

            // sorted object list
            GraphPnl.sortedObjectList[eachObject] = eachObject;
            
            // sorted poly list for each object
            for (int i=0; i<(GraphPnl.objects[eachObject].polys); i++)
            {
                GraphPnl.objects[eachObject].sorted_list[i] = i;
            }
        }

        // start with the Splash screen
// DCDEBUG uncomment the following line to reenable the splash screen when the keyboard issue is resolved
//        GraphPnl.quit = true;

        // main game loop
        while (GraphPnl.exit == false)
        {
            // display Title Screen
            GraphPnl.displaySplash(GraphPnl.offgraphics, GraphPnl.crafts[GraphPnl.currentCraft]);  // update must be called before the GraphPnl.offgraphics will be ready

            while ( (GraphPnl.relaxer == mainRunThread) && (GraphPnl.quit == false))
            {

                // determine new craft location and orientation
                GraphPnl.distance = GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.distance_travelled;
                if (GraphPnl.crafts[GraphPnl.currentCraft].Speed > 0)
                    GraphPnl.curvature = (GraphPnl.Gravity * Math.sin(GraphPnl.crafts[GraphPnl.currentCraft].Roll)) / (GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.crafts[GraphPnl.currentCraft].Speed * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Roll));
            
                GraphPnl.crafts[GraphPnl.currentCraft].Yaw  -= GraphPnl.distance * 10.00 * GraphPnl.curvature;    // ???? this should be in Radians ???????
                GraphPnl.crafts[GraphPnl.currentCraft].eyeX += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeZ += GraphPnl.distance * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeY += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Pitch);

                if (GraphPnl.crafts[GraphPnl.currentCraft].eyeY < Craft.MINIMUM_ALTITUDE)
                {
                    GraphPnl.crash = true;
                    GraphPnl.quit = true;
                    GraphPnl.continueAllowed = false;

                    // DCDebug: not implemented yet
                    // Switch to demo mode
                    GraphPnl.demoMode = true;
                }

                repaint();
                    
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            } // end while ( (relaxer == me) && (quit == false))

            // display Title Screen
            GraphPnl.displaySplash(GraphPnl.offgraphics, GraphPnl.crafts[GraphPnl.currentCraft]);  // update must be called before the GraphPnl.offgraphics will be ready

            
            repaint();
            if (GraphPnl.quit == true)
            {
                // give the processor a rest
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
        } // end while (exit == false)

        repaint();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
            
    } // end run


    
} // end DeanFly
