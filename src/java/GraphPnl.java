/****************************************************************************************************
* Copyright Crystalwink Ltd. 2000.  All Rights Reserved.  The copyright to the computer program(s)  *
* herein is the property of Crystalwink Ltd. The program(s) may be used and/or copied only with the *
* written permission of Crystalwink Ltd or in accordance with the terms & conditions in the agreement   *
* or contract under which the program(s) are supplied. This copyright notice must not be removed.  *
***************************************************************************************************/
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.JFrame;

/**
 * 
 * @author Dean.Clark
 *
 */
class GraphPnl extends Panel //JPanel for KeyListener 2007
    implements KeyListener, Runnable, MouseListener, MouseMotionListener
{
    final double PI                 = Math.PI;  // 3.141592653589793;    //
    final double ONE_DEGREE         = 0.0175;   // 1 degree in radians
    final double DEGREE_CHANGE      = 0.0175;   // 1 degree in radians
    final static int MAX_OBJECTS    = 1000;     // maximum number of objects in the world
    final static int FOCAL_DISTANCE = 400;      // used to calculate perspective (was 200)
    static final int SCREEN_SCALER  = 3;        // Small Screen View

    final Color fixedColor = new Color(250, 220, 100);
    final Color selectColor = Color.pink;
    final int Y1min = 1, Y1max = 2, X1min = 4, X1max = 8, Y2min = 16, Y2max = 32, X2min = 64, X2max = 128; // axis bounds flag

    static DeanFly graphDeanFly = new DeanFly();
    static WorldItem objects[] = new WorldItem[MAX_OBJECTS];
    static int nWorldObjects = 0;            // number of objects to draw
    static WorldItem box;
    static Craft craft = new Craft();
    static Craft crafts[] = new Craft[10];
    static int availableCrafts = 0, currentCraft = 0;
    static Screen screen = new Screen();
    static boolean exit = false, quit = false, crash = false, continueAllowed = false;
    static boolean demoMode = true;
    static Thread relaxer;

    static boolean debug = false, verboseDebug = false;
    static Image offscreen;
    static Dimension offscreensize;
    static Graphics offgraphics;
    /*
    DeanFly graph;
    WorldItem objects[] = new WorldItem[MAX_OBJECTS];
    int nWorldObjects = 0;            // number of objects to draw
    WorldItem box;
    Craft craft = new Craft();
    Craft crafts[] = new Craft[10];
    int availableCrafts = 0, currentCraft = 0;
    Screen screen = new Screen();
    boolean exit = false, quit = false, crash = false, continueAllowed = false;
    boolean demoMode = true;
    Thread relaxer;

    boolean debug = false, verboseDebug = false;
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;
        */
    static Font screenFont = new Font("Serif", 0, 10);
    static Font menuFont = new Font("Serif", 0, 30);


    static int    iii;
    static int    charin;                                      // keyboard input character
    static double distance;                                    // in meters
    static double distance_travelled = 0.1;                    // in meters
    static double curvature;                                   // fight curve of the aircraft
    static double Gravity = 9.81;                              // used to calculate curvature and physics behaviour
    static boolean mapOn = false, displayClipMap = !mapOn;     // turn 2d Map ON=1, OFF=0   // share the same screen space so must be inverse
    //int    totalObjects   = 0;                               // number of objects to draw
    static boolean simulationEnd = false;                      // when 0 the simulation exits
    static boolean Instuments = false, Dials = false;          // display cockpit and whereAmI info
    static int     sortedObjectList[] = new int[MAX_OBJECTS];  // list of objects in z order
    //union  Picture *cockpit;                                 // cockpit image file
    //char   *imageBuff;                                       // buffer for _getimage _putImage
    static boolean wireframe = false, clip = true, clipScreen = true;
    static boolean instumentDisplay = true, keyHelpDisplay = !instumentDisplay;  // share the same screen space so must be inverse
    static boolean hiddenSurfaces = false;
    static boolean flipped = false, perspective = true;
    static int nLandMassObjects = 0;
    
    static int maxScreenX, maxScreenY;
    
    public GraphPnl(DeanFly graph) {
        this.graphDeanFly = graph;
        addMouseListener(this);
        //typingArea = new JTextField(20);
        addKeyListener(this);
        setFocusable(true);
    }

    
/**
 * 
 * @param argv
 */
    public static void main(String argv[]) {
        // initialise global debug trace
        boolean debug = false, verboseDebug = false;
//      debug = true;
//      verboseDebug = true;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int defaultWidth = screenSize.width * 8 / 10;
        int defaultHeight = screenSize.height * 8 / 10;
        int width = defaultWidth;
        int height = defaultHeight;

        Thread meThread = Thread.currentThread();

        // Application/Applet differences
        graphDeanFly.applicationMode = true;
        
        // Reset Crafts 23-01-2007
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            GraphPnl.crafts[i] = null;
        }
        GraphPnl.availableCrafts = 0;

                
        // Create graphics frame
        graphDeanFly.mainInit();
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
    //quit = true;

    //Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);
    //Create and set up the window.
    JFrame frame = new JFrame("Simulation");
    frame.setSize(screenSize.width /3, screenSize.height / 4);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    

    DeanFly aDeanFly = new DeanFly();
    aDeanFly.init();

    frame.add(aDeanFly);
    frame.setVisible(true);
    
            // main update loop
        while (!GraphPnl.exit)
        {
            // display Title Screen
            GraphPnl.displaySplash(offgraphics, crafts[currentCraft]);

            //while ( (GraphPnl.relaxer == meThread) && (GraphPnl.quit == false))
            while (!GraphPnl.quit)
            {

                
                // determine new craft location and orientation
                GraphPnl.distance = GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.distance_travelled;
                if (GraphPnl.crafts[GraphPnl.currentCraft].Speed > 0)
                    GraphPnl.curvature = (GraphPnl.Gravity * Math.sin(GraphPnl.crafts[GraphPnl.currentCraft].Roll)) / (GraphPnl.crafts[GraphPnl.currentCraft].Speed * GraphPnl.crafts[GraphPnl.currentCraft].Speed * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Roll));
            
                GraphPnl.crafts[GraphPnl.currentCraft].Yaw  -= GraphPnl.distance * 10.00 * GraphPnl.curvature;    // ???? this should be in Radians ???????
                GraphPnl.crafts[GraphPnl.currentCraft].eyeX += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeZ += GraphPnl.distance * Math.cos(GraphPnl.crafts[GraphPnl.currentCraft].Yaw);
                GraphPnl.crafts[GraphPnl.currentCraft].eyeY += GraphPnl.distance * Math.sin(-GraphPnl.crafts[GraphPnl.currentCraft].Pitch);

                // impact due to altitude low
                if (GraphPnl.crafts[GraphPnl.currentCraft].eyeY < 25)
                {
                    GraphPnl.crash = true;
                    GraphPnl.quit = true;
                    GraphPnl.continueAllowed = false;

                    // DCDebug: not implemented yet
                    // Switch to demo mode
                    GraphPnl.demoMode = true;
                }
                
                aDeanFly.repaint();
                frame.repaint();
                //repaint();
                    
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            } // end while ( (relaxer == me) && (quit == false))

            System.exit(0);

            aDeanFly.repaint();
            frame.repaint();
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
            

    } // end main

    
/**
 *     
 */
    public void run() {
        // initialise global debug trace
//        boolean debug = false, verboseDebug = false;
        debug = true;
//        verboseDebug = true;

                if(GraphPnl.availableCrafts != 0 )
                    System.out.println("GraphPnl.availableCrafts not zero at init time - possible memory leak");
                    
        Thread me = Thread.currentThread();

                // Reset Crafts 23-01-2007
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            crafts[i] = null;
        }
                GraphPnl.availableCrafts = 0;
                
        loadCraft();
        for (int i=0; i<GraphPnl.availableCrafts; i++)
        {
            initAircraftNew(crafts[i]);
        }


        loadLandMass();
        nLandMassObjects = nWorldObjects;

        loadLandItems();
    

        // reset all sorted_lists of polys to initial values
        for (int eachObject=0; eachObject<nWorldObjects; eachObject++)
        {
            // Move objects to each desired location
            GraphPnl.initObject(objects[eachObject], objects[eachObject].initX, objects[eachObject].initY, objects[eachObject].initZ, objects[eachObject].preRotation, objects[eachObject].scale);     // set object location and rotation to 0
//            initObject(objects[eachObject], initX, initY, initZ, preRotation, scale);     // set object location and rotation to 0

            // sorted object list
            sortedObjectList[eachObject] = eachObject;
            
            // sorted poly list for each object
            for (int i=0; i<(objects[eachObject].polys); i++)
            {
                objects[eachObject].sorted_list[i] = i;
            }
        }

        // start with the Splash screen
// DCDEBUG uncomment the following line to reenable the splash screen when the keyboard issue is resolved
//        quit = true;
        
        // main update loop
        while (exit == false)
        {
            // display Title Screen
            displaySplash(offgraphics, crafts[currentCraft]);

//            while ( (relaxer == me) && (quit == false))
            while ( (quit == false))
            {

                repaint();

                // determine new craft location and orientation
                distance = crafts[currentCraft].Speed * distance_travelled;
                if (crafts[currentCraft].Speed > 0)
                    curvature = (Gravity * Math.sin(crafts[currentCraft].Roll)) / (crafts[currentCraft].Speed * crafts[currentCraft].Speed * Math.cos(crafts[currentCraft].Roll));
            
                crafts[currentCraft].Yaw  -= distance * 10.00 * curvature;    // ???? this should be in Radians ???????
                crafts[currentCraft].eyeX += distance * Math.sin(-crafts[currentCraft].Yaw);
                crafts[currentCraft].eyeZ += distance * Math.cos(crafts[currentCraft].Yaw);
                crafts[currentCraft].eyeY += distance * Math.sin(-crafts[currentCraft].Pitch);

                if (crafts[currentCraft].eyeY < 25)
                {
                    crash = true;
                    quit = true;
                    continueAllowed = false;

                    // DCDebug: not implemented yet
                    // Switch to demo mode
                    demoMode = true;
                }
                    
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            } // end while ( (relaxer == me) && (quit == false))


            
            repaint();
        } // end while (exit == false)

        //repaint();
        if (quit == true)
        {
            // display Title Screen
            displaySplashContinue(offgraphics, craft);//s[currentCraft]);

            // give the processor a rest
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
            
    } // end run



/**
 * 
 */
    public synchronized void update(Graphics g) {
        //Dimension d = getSize();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        if (debug && verboseDebug) {
            System.out.println("running update");
        }
//        if ((offscreen == null) || (crafts[currentCraft].windowWidth != offscreensize.width) ||
//                    (crafts[currentCraft].windowHeight != offscreensize.height))
        if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height))
        {
            if(crafts[currentCraft] != null)
            {
                crafts[currentCraft].windowHeight = d.height / GraphPnl.SCREEN_SCALER;        // screen size height
                crafts[currentCraft].windowWidth  = d.width / GraphPnl.SCREEN_SCALER;        // screen size width
                offscreen = createImage(crafts[currentCraft].windowWidth, crafts[currentCraft].windowHeight);
            }
            else
            {
                //offscreen = createImage(d.width, d.height);
                offscreen = createImage(craft.windowWidth, craft.windowHeight);
            }

            screen.xmin = 2;                    // screen dimensions
            screen.ymin = 2;
            screen.xmax = d.width - 2;
            screen.ymax = d.height - 2;
            screen.xmid = screen.xmax / 2;
            screen.ymid = screen.ymax / 2;

            offscreensize = d;
            offgraphics = offscreen.getGraphics();
            offgraphics.setFont(screenFont);

            maxScreenX = offscreensize.width;
            maxScreenY = offscreensize.height;
        }
        
        if(crafts[currentCraft] != null)
        {
            offgraphics.setColor(getBackground());
            offgraphics.fillRect(0, 0, crafts[currentCraft].windowWidth, crafts[currentCraft].windowHeight);
            
            // sort objects by Z order
  //        SortObjects(objects, craft, sortedObjectList, nLandMassObjects, nWorldObjects);
            SortObjects(objects, crafts[currentCraft], nLandMassObjects, nWorldObjects);
            if (hiddenSurfaces)
                removeHiddenSurfaces(objects, crafts[currentCraft], nLandMassObjects, nWorldObjects);
    
            // draw Islands
            for (int i=0; i<nLandMassObjects; i++)
            {
                // draw each object in Z order
                drawObject(offgraphics, objects[i], crafts[currentCraft], flipped);
            }
            
            // draw Islands Objects
            for (int i=nLandMassObjects; i<nWorldObjects; i++)
            {
                // draw each object in Z order
                drawObject(offgraphics, objects[sortedObjectList[i]], crafts[currentCraft], flipped);
            }
    
            if (mapOn)
                draw2DMap(objects, offgraphics, crafts[currentCraft]);
    
            FontMetrics fm = offgraphics.getFontMetrics();
            quitBox(offgraphics, box, fm);
    
            if (exit == true)
            {
                displayExit(offgraphics, crafts[currentCraft]);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                System.exit(0);

            }
            else
            {
                if (crash == true) {
                    displayCrash(offgraphics, crafts[currentCraft]);
                    
                    g.drawImage(offscreen, 0, 0, null);
                    crash = false;
                    quit = true;
                    demoMode = true;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                else
                    if (quit == true)
                    {
                        if (continueAllowed == true)
                        {
                            displaySplashContinue(offgraphics, crafts[currentCraft]);
                        }
                        else
                        {
                            displaySplash(offgraphics, crafts[currentCraft]);
                        }
                        demoMode = true;

                        displaySplashContinue(offgraphics, crafts[currentCraft]);
                        g.drawImage(offscreen, 0, 0, null);
                    }
            }
            g.drawImage(offscreen, 0, 0, null);
        }
        
        g.drawImage(offscreen, 0, 0, null);
        
    } // end update


/**
 * 
 * @param anItem
 * @param startIndex
 * @param endIndex
 * @param vertexCount
 * @param newX
 * @param newY
 * @param newZ
 * @param ClipValue
 */
void getIntersectPoint(WorldItem anItem, int startIndex, int endIndex, int vertexCount, double newX[], double newY[], double newZ[], double ClipValue) {
    double zdist, dx, dy;
    //    getIntersectPoint(xpoints[vertex], ypoints[vertex], zpoints[vertex], xpoints[vertex+1], ypoints[vertex+1], zpoints[vertex+1], &tempx[vertexCount], &tempy[vertexCount], &tempz[vertexCount], ClipPoint);

    // create new point at the intersection of the z craft
    zdist = anItem.zpoints[endIndex];
            
    dx = ( makeBigger( (anItem.xpoints[startIndex] - anItem.xpoints[endIndex])) / makeBigger( (anItem.zpoints[startIndex] - anItem.zpoints[endIndex]) ));
    dy = ( makeBigger( (anItem.ypoints[startIndex] - anItem.ypoints[endIndex])) / makeBigger( (anItem.zpoints[startIndex] - anItem.zpoints[endIndex]) ));

    newX[vertexCount] = (anItem.xpoints[endIndex] + (-zdist * dx));
    newY[vertexCount] = (anItem.ypoints[endIndex] + (-zdist * dy));
    newZ[vertexCount] = ClipValue;

} // end getIntersectPoint


/**
 * 
 * @param anItem
 * @param viewer
 * @param startPoint
 * @param endPoint
 * @param vertexCount
 * @param newX
 * @param newY
 * @param axis
 * @param screen
 * @return
 */
int getIntersectPointScreen(WorldItem anItem, Craft viewer, int startPoint, int endPoint, int vertexCount, int[] newX, int[] newY, int axis, Screen screen){
//    Note: Y1=0 y2=max X1=0 X2=max  top left (x,y)=(0,0)
    int        startX, startY, endX, endY;
    double  dx, dy;
    boolean turningPointAdded = false;

    startX    = anItem.xpoly[startPoint] - screen.xmin;
    startY    = anItem.ypoly[startPoint] - screen.ymin;
    endX    = anItem.xpoly[endPoint] - screen.xmin;
    endY    = anItem.ypoly[endPoint] - screen.ymin;

    dx = ( makeBigger( (anItem.xpoly[startPoint] - anItem.xpoly[endPoint]) - screen.xmin) / makeBigger( (anItem.ypoly[startPoint] - anItem.ypoly[endPoint] - screen.ymin) ));
    dy = ( makeBigger( (anItem.ypoly[startPoint] - anItem.ypoly[endPoint]) - screen.ymin) / makeBigger( (anItem.xpoly[startPoint] - anItem.xpoly[endPoint] - screen.xmin) ));


    // Ymin (horizontal y=min)
    if ( axis == Y1min ) {
        newX[vertexCount] = (int) (anItem.xpoly[startPoint] - ((anItem.ypoly[startPoint] - screen.ymin) * dx));
        newY[vertexCount] = screen.ymin;
    }

    // Ymax (horizontal y=max)
    else if ( axis == Y1max ) {
        newX[vertexCount] = (int) (anItem.xpoly[startPoint] + ((screen.ymax - anItem.ypoly[startPoint]) * dx));
        newY[vertexCount] = screen.ymax;
    }

    // Xmin (vertical x=min)
    else if ( axis == X1min ) {
        newX[vertexCount] = screen.xmin;
        newY[vertexCount] = (int) (anItem.ypoly[startPoint] - ((anItem.xpoly[startPoint] - screen.xmin) * dy));
    }

    // Xmax (vertical x=max)
    else if ( axis == X1max ) {
        newX[vertexCount] = screen.xmax;
        newY[vertexCount] = (int) (anItem.ypoly[startPoint] + ((screen.xmax - anItem.xpoly[startPoint]) * dy));
    }
    
    // leftY
    else if ( axis == Y2min ) {
        // check for corner point
        if (vertexCount > 0) {
            if (newX[vertexCount-1] == screen.xmin)
            {
                newX[vertexCount] = screen.xmin;
                newY[vertexCount] = screen.ymin;
                vertexCount++;
                turningPointAdded = true;
            }
            else if (newX[vertexCount-1] == screen.xmax)
            {
                newX[vertexCount] = screen.xmax;
                newY[vertexCount] = screen.ymin;
                vertexCount++;
                turningPointAdded = true;
            }
        }
        newX[vertexCount] = (int) (anItem.xpoly[startPoint] - ((anItem.ypoly[startPoint] - screen.ymin) * dx));
        newY[vertexCount] = screen.ymin;
    }

    // rightY
    else if ( axis == Y2max ) {
        // check for corner point
        if (vertexCount > 0) {
            if (newX[vertexCount-1] == screen.xmin)
            {
                newX[vertexCount] = screen.xmin;
                newY[vertexCount] = screen.ymax;
                vertexCount++;
                turningPointAdded = true;
            }
            else if (newX[vertexCount-1] == screen.xmax)
            {
                newX[vertexCount] = screen.xmax;
                newY[vertexCount] = screen.ymax;
                vertexCount++;
                turningPointAdded = true;
            }
        }
        newX[vertexCount] = (int) (anItem.xpoly[startPoint] + ((screen.ymax - anItem.ypoly[startPoint]) * dx));
        newY[vertexCount] = screen.ymax;
    }

    // upperX
    else if ( axis == X2min ) {
        // check for corner point
        if (vertexCount > 0) {
            if (newY[vertexCount-1] == screen.ymin)
            {
                newX[vertexCount] = screen.xmin;
                newY[vertexCount] = screen.ymin;
                vertexCount++;
                turningPointAdded = true;
            }
            else if (newY[vertexCount-1] == screen.ymax)
            {
                newX[vertexCount] = screen.xmin;
                newY[vertexCount] = screen.ymax;
                vertexCount++;
                turningPointAdded = true;
            }
        }
        newX[vertexCount] = screen.xmin;
        newY[vertexCount] = (int) (anItem.ypoly[startPoint] - ((anItem.xpoly[startPoint] - screen.xmin) * dy));
    }

    // lowerX
    else if ( axis == X2max ) {
        // check for corner point
        if (vertexCount > 0) {
            if (newY[vertexCount-1] == screen.ymin)
            {
                newX[vertexCount] = screen.xmax;
                newY[vertexCount] = screen.ymin;
                vertexCount++;
                turningPointAdded = true;
            }
            else if (newY[vertexCount-1] == screen.ymax)
            {
                newX[vertexCount] = screen.xmax;
                newY[vertexCount] = screen.ymax;
                vertexCount++;
                turningPointAdded = true;
            }
        }
        
        newX[vertexCount] = screen.xmax;
        newY[vertexCount] = (int) (anItem.ypoly[startPoint] + ((screen.xmax - anItem.xpoly[startPoint]) * dy));
    }
    
    else
        System.out.println("getIntersectPointScreen no match");

    vertexCount++;        // increment for intersect point added

    return(vertexCount);
} // end getIntersectPointScreen



/**
 * 
 * @param anItem
 * @param startPoint
 * @param endPoint
 * @param vertexCount
 * @param newX
 * @param newY
 * @param screen
 * @return
 */
int checkForTurningPoint(WorldItem anItem, int startPoint, int endPoint, int vertexCount, int[] newX, int[] newY, Screen screen) {
    // left top/bottom
    if ( newX[startPoint] == screen.xmin ) {
        // check for corner point
        if (newY[endPoint] == screen.ymin)
        {
            newX[vertexCount] = screen.xmin;
            newY[vertexCount] = screen.ymin;
            vertexCount++;
        }
        else if (newY[endPoint] == screen.ymax)
        {
            newX[vertexCount] = screen.xmin;
            newY[vertexCount] = screen.ymax;
            vertexCount++;
        }
    }

    else if ( newX[startPoint] == screen.xmax ) {
        // check for corner point
        if (newY[endPoint] == screen.ymin)
        {
            newX[vertexCount] = screen.xmax;
            newY[vertexCount] = screen.ymin;
            vertexCount++;
        }
        else if (newY[endPoint] == screen.ymax)
        {
            newX[vertexCount] = screen.xmax;
            newY[vertexCount] = screen.ymax;
            vertexCount++;
        }
    }

    else if ( newY[startPoint] == screen.ymin ) {
        // check for corner point
        if (newX[endPoint] == screen.xmin)
        {
            newX[vertexCount] = screen.xmin;
            newY[vertexCount] = screen.ymin;
            vertexCount++;
        }
        else if (newX[endPoint] == screen.xmax)
        {
            newX[vertexCount] = screen.xmax;
            newY[vertexCount] = screen.ymin;
            vertexCount++;
        }
    }


    else if ( newY[startPoint] == screen.ymax ) {
        // check for corner point
        if (newX[endPoint] == screen.xmin)
        {
            newX[vertexCount] = screen.xmin;
            newY[vertexCount] = screen.ymax;
            vertexCount++;
        }
        else if (newX[endPoint] == screen.xmax)
        {
            newX[vertexCount] = screen.xmax;
            newY[vertexCount] = screen.ymax;
            vertexCount++;
        }
    }

    return(vertexCount);
} // end checkForTurningPoint


/**
 * 
 * @param anObject
 * @param viewer
 */
void RotateObjectXYZ(WorldItem anObject, Craft viewer) {
    // set new values
    for (int index = 0; index < anObject.vertices; index++) {
        anObject.newX[index] = anObject.RealX[index] - viewer.eyeX;
        anObject.newY[index] = anObject.RealY[index] - viewer.eyeY;
        anObject.newZ[index] = anObject.RealZ[index] - viewer.eyeZ;
    }

    // rotate object to world view
    rotationY(anObject, viewer);
    rotationX(anObject, viewer);
    rotationZ(anObject, viewer);
} // RotateObjectXYZ


/**
 * Using the 3D-xyz values and rot-xyz , new-xyz values are calculated.
 * @param anObject
 * @param viewer
 */
void rotationX(WorldItem anObject, Craft viewer) {
    double mat1[]    = new double[] {0,0,0,1};         // matrix (x,y,z)
    double mat2[]    = new double[] {1,0,0,0,          // rotation matrix
                    0,1,0,0,
                    0,0,1,0,
                    0,0,0,1};
                        
    double mat3[]    = new double[] {0,0,0,1};         // result matrix
        
    double angle = -viewer.Pitch;               // rotate about x.
//    angle = viewer.Yaw;               // rotate about y.
//    angle = viewer.Roll;              // rotate about z.


// Rotation matrix for rotation about z axis.

    mat2[5] = Math.cos(angle);    // rotate y points (-,x,-,0)  y
    mat2[6] = Math.sin(angle);    // rotate z points (-,-,z,0)  z
    mat2[9] = -Math.sin(angle);   // rotate y points (-,x,-,0)  y
    mat2[10] = Math.cos(angle);   // rotate z points (-,-,z,0)  z

    // for each point in the object, obtain the homogeneous coordinates.
    for (int i=0; i<anObject.vertices; i++) {

//        mat1[0] = (float)anObject.newX[i] - viewer.eyeX;  // copy xyz for each point to homogeneous array (x,y,x,1)
//        mat1[1] = (float)anObject.newY[i] - viewer.eyeY;
//        mat1[2] = (float)anObject.newZ[i] - viewer.eyeZ;

        mat1[0] = (double)anObject.newX[i];  // copy xyz for each point to homogeneous array (x,y,x,1)
        mat1[1] = (double)anObject.newY[i];
        mat1[2] = (double)anObject.newZ[i];

        threeDcross14(mat1, mat2, mat3);          // multiply mat1 with mat2 = mat3.

        anObject.newX[i] = (int) mat3[0];             // copy homogeneous array to new xyz
        anObject.newY[i] = (int) mat3[1];
        anObject.newZ[i] = (int) mat3[2];

    }

}   // end rotationX


/** 
 * Using the 3D-xyz values and rot-xyz , new-xyz values are calculated. 
 * @param anObject
 * @param viewer
 */
void rotationY(WorldItem anObject, Craft viewer) {
    double mat1[]    = new double[] {0,0,0,1};         // matrix (x,y,z)
    double mat2[]    = new double[] {1,0,0,0,          // rotation matrix
                    0,1,0,0,
                    0,0,1,0,
                    0,0,0,1};

    double mat3[]    = new double[] {0,0,0,1};         // result matrix
        
//    double angle = viewer.Pitch;             // rotate about x.
      double angle = viewer.Yaw;               // rotate about y.
//    double angle = viewer.Roll;              // rotate about z.


// Rotation matrix for rotation about y axis.

    mat2[0]  = Math.cos(angle);    // rotate x points (x,-,-,0)  x
    mat2[2]  = -Math.sin(angle);   // rotate z points (-,-,z,0)  z
    mat2[8]  = Math.sin(angle);    // rotate x points (x,-,-,0)  x
    mat2[10] = Math.cos(angle);    // rotate z points (-,-,z,0)  z


    // for each point in the object, obtain the homogeneous coordinates.
    for (int i=0; i<anObject.vertices; i++) {

//        mat1[0] = ( (float)anObject.newX[i] - (float)viewer.eyeX );                // copy xyz for each point to homogeneous array (x,y,x,1)
//        mat1[1] = ( (float)anObject.newY[i] - (float)viewer.eyeY );                
//        mat1[2] = ( (float)anObject.newZ[i] - (float)viewer.eyeZ );

        mat1[0] = ( (double)anObject.newX[i]);        // copy xyz for each point to homogeneous array (x,y,x,1)
        mat1[1] = ( (double)anObject.newY[i]);
        mat1[2] = ( (double)anObject.newZ[i]);

        threeDcross14(mat1, mat2, mat3);        // multiply mat1 with mat2 = mat3.
        
        anObject.newX[i] = (int) mat3[0];        // copy homogeneous array to new xyz
        anObject.newY[i] = (int) mat3[1];
        anObject.newZ[i] = (int) mat3[2];
    }

    if (debug && verboseDebug)
    {
    System.out.println("Run rotationY");
    }
}   // end rotationY


/**
 * Using the 3D-xyz values and rot-xyz , new-xyz values are calculated.
 * @param anObject
 * @param viewer
 */
void rotationZ(WorldItem anObject, Craft viewer) {
    double mat1[]    = new double[] {0,0,0,1};         // matrix (x,y,z)
    double mat2[]    = new double[] {1,0,0,0,          // rotation matrix
                    0,1,0,0,
                    0,0,1,0,
                    0,0,0,1};
                        
    double mat3[]    = new double[] {0,0,0,1};         // result matrix
        
//    double angle = viewer.Pitch;        // rotate about x.
//    double angle = viewer.Yaw;            // rotate about y.
    double angle = -viewer.Roll;        // rotate about z.


// Rotation matrix for rotation about z axis.

    mat2[0] = Math.cos(angle);        // rotate x points (x,-,-,0)  x
    mat2[1] = -Math.sin(angle);        // rotate y points (-,x,-,0)  y
    mat2[4] = Math.sin(angle);        // rotate x points (x,-,-,0)  x
    mat2[5] = Math.cos(angle);        // rotate y points (-,x,-,0)  y


    // for each point in the object, obtain the homogeneous coordinates.
    for (int i=0; i<anObject.vertices; i++) {

//        mat1[0] = ( (float)anObject.newX[i] - (float)viewer.eyeX );    // copy xyz for each point to homogeneous array (x,y,x,1)
//        mat1[1] = ( (float)anObject.newY[i] - (float)viewer.eyeY );
//        mat1[2] = ( (float)anObject.newZ[i] - (float)viewer.eyeZ );

        mat1[0] = ( (double)anObject.newX[i]);        // copy xyz for each point to homogeneous array (x,y,x,1)
        mat1[1] = ( (double)anObject.newY[i]);
        mat1[2] = ( (double)anObject.newZ[i]);

        threeDcross14(mat1, mat2, mat3);            // multiply mat1 with mat2 = mat3.
        
        anObject.newX[i] = (int) mat3[0];            // copy homogeneous array to new xyz
        anObject.newY[i] = (int) mat3[1];
        anObject.newZ[i] = (int) mat3[2];

    }
}   // end rotationZ




/**
 * Using the 3D-xyz values and rot-xyz , new-xyz values are calculated.
 *  
 * @param anObject
 * @param viewpoint
 */
/*
void rotatePointZ(double pointX, double pointY, double pointZ, double angle, double NewX, double NewY, double NewZ)         

{
    double mat1[]    = new double[] {0,0,0,1};         // matrix (x,y,z)
    double mat2[]    = new double[] {1,0,0,0,          // rotation matrix
                    0,1,0,0,
                    0,0,1,0,
                    0,0,0,1};
                        
    double mat3[]    = new double[] {0,0,0,1};         // result matrix

        

// Rotation matrix for rotation about z axis.

    mat2[0] =  Math.cos(angle);            // rotate x points (x,-,-,0)  x
    mat2[1] = -Math.sin(angle);            // rotate y points (-,x,-,0)  y
    mat2[4] =  Math.sin(angle);            // rotate x points (x,-,-,0)  x
    mat2[5] =  Math.cos(angle);            // rotate y points (-,x,-,0)  y

    mat1[0] = (double)pointX;            // copy xyz for each point to homogeneous array (x,y,x,1)
    mat1[1] = (double)pointY;
    mat1[2] = (double)pointZ;

    threeDcross14(mat1, mat2, mat3);    // multiply mat1 with mat2 = mat3.
        
    NewX = (int) mat3[0];                // copy homogeneous array to new xyz
    NewY = (int) mat3[1];
    NewZ = (int) mat3[2];

    if (debug && verboseDebug) {
        System.out.println("RotatePointZ");
    }


}   // end rotatePointZ
*/


/**
 * Sorts a list of polygons into z order, with the farthest poly at the start and
 *  the closest being last in the list.  This can be used to ensure that farthest
 *  polys never obsure the closest. * 
 * @param anObject
 * @param viewpoint
 */
static void SortPolys(WorldItem anObject, Craft viewpoint) {
    boolean debug_method = false;
    int debug_counter = 0;
    //final int POLYS        = 300;  // max number of polygons per object
    int points;
    int total_Z;
    int total_X;
    int temparrayZ[] = new int[WorldItem.POLYS];
    int temparrayX[] = new int[WorldItem.POLYS];
    double temparrayDistanceFromView[] = new double[WorldItem.POLYS];
    boolean sorted = false;
    int temp;
    double tempDouble;


    anObject.averageZposition = 0;
    anObject.averageXposition = 0;
    anObject.averageDistanceFromViewer = 0;
    
    // reset current sorted poly list with an unsorted list
    for (int poly=0; poly<anObject.polys; poly++)
    {
        anObject.sorted_list[poly] = poly;
    }

    // for each poly that makes the current object
    for (int poly=0; poly<anObject.polys; poly++)
    {
        total_Z = 0;        // clear last meassure
        total_X = 0;        // clear last meassure

        // number of points on the current poly
        points = anObject.polylist[poly][0];
        
        // add up the z values for each point on the poly.
        for (int j=0; j<points; j++) {
            //total_Z += anObject.newZ[ (anObject.poly[poly][j+1]) ];
            total_Z += (int) anObject.RealZ[ (anObject.polylist[poly][j+1]) ];
            total_X += (int) anObject.RealX[ (anObject.polylist[poly][j+1]) ];
        }

        // average distance of current poly from (z distance)
        temparrayZ[poly] = (int) ((total_Z / points) - viewpoint.eyeZ);
        temparrayX[poly] = (int) ((total_X / points) - viewpoint.eyeX);
        temparrayDistanceFromView[poly] = Math.sqrt((temparrayZ[poly] *  temparrayZ[poly]) + ( temparrayX[poly] *  temparrayX[poly]) );

        // add average Z to object averageZposition variable
        anObject.averageZposition += temparrayZ[poly];
        anObject.averageXposition += temparrayX[poly];
        anObject.averageDistanceFromViewer += temparrayDistanceFromView[poly];
    }

    // get the average Z for the number of polys (used to sort objects by z order
    anObject.averageZposition = anObject.averageZposition / anObject.polys;
    anObject.averageXposition = anObject.averageXposition / anObject.polys;
    anObject.averageDistanceFromViewer = anObject.averageDistanceFromViewer / anObject.polys;


    // sort distance values
    while (sorted == false)
    {
        sorted = true;          // assume sorted.

        // for each poly that makes the WorldItem
        for (int i=0; i<anObject.polys; i++)
        {
            // if its not the last poly
            if (i < (anObject.polys-1))
            {
                if (temparrayDistanceFromView[i] < temparrayDistanceFromView[i+1])
                {
                    debug_counter++;

                    // swap the values
                    tempDouble = temparrayDistanceFromView[i];
                    temparrayDistanceFromView[i] = temparrayDistanceFromView[i+1];
                    temparrayDistanceFromView[i+1] = tempDouble;
               
                    // update object structure
                    temp = anObject.sorted_list[i];
                    anObject.sorted_list[i]   = anObject.sorted_list[i+1];
                    anObject.sorted_list[i+1] = temp;

                    sorted = false;          // not sorted
                } // end if
            } // end if
        } // end for
    } // end while



/*
    anObject.averageZposition = 0;
    anObject.averageXposition = 0;
    anObject.averageDistanceFromViewer = 0;


    // waste a bit of time!
    // reset current sorted poly list with an unsorted list
    for (int poly=0; poly<anObject.polys; poly++)
    {
        anObject.sorted_list[poly] = poly;
    }


    // for each poly that makes the current object
    for (int poly=0; poly<anObject.polys; poly++)
    {
        total_Z = 0;        // clear last meassure
        total_X = 0;        // clear last meassure

        // number of points on the current poly
        points = anObject.polylist[poly][0];
        
        // add up the z values for each point on the poly.
        for (int j=0; j<points; j++) {
            //total_Z += anObject.newZ[ (anObject.poly[poly][j+1]) ];
            total_Z += (int) anObject.RealZ[ (anObject.polylist[poly][j+1]) ];
            total_X += (int) anObject.RealX[ (anObject.polylist[poly][j+1]) ];
        }

        // average distance of current poly from (z distance)
        temparrayZ[poly] = (int) ((total_Z / points) - viewpoint.eyeZ);
        temparrayX[poly] = (int) ((total_X / points) - viewpoint.eyeX);
        temparrayDistanceFromView[poly] = Math.sqrt((temparrayZ[poly] *  temparrayZ[poly]) + ( temparrayX[poly] *  temparrayX[poly]) );
        // should this be forced to unsigned positive value only?

        // add average Z to object averageZposition variable
        anObject.averageZposition += temparrayZ[poly];
        anObject.averageXposition += temparrayX[poly];
        anObject.averageDistanceFromViewer += temparrayDistanceFromView[poly];
    }

    // get the average Z for the number of polys (used to sort objects by z order
    anObject.averageZposition = anObject.averageZposition / anObject.polys;
    anObject.averageXposition = anObject.averageXposition / anObject.polys;

    anObject.averageDistanceFromViewer = anObject.averageDistanceFromViewer / anObject.polys;
    
    // if average is negative make positive, a distance is the same regardless of direction in this case.
    if ( anObject.averageDistanceFromViewer < 0)
            anObject.averageDistanceFromViewer *= -1;


    // sort distance values
    while (sorted == false)
    {
        sorted = true;          // assume sorted.

        // for each poly that makes the WorldItem
        for (int poly=0; poly<anObject.polys; poly++)
        {
            // if its not the last poly
            if (poly < (anObject.polys-1))
            {
                if (temparrayDistanceFromView[anObject.sorted_list[poly]] < temparrayDistanceFromView[anObject.sorted_list[poly]+1])
                {
                    debug_counter++;

                    // swap the values
                    tempDouble = temparrayDistanceFromView[anObject.sorted_list[poly]];
                    temparrayDistanceFromView[anObject.sorted_list[poly]] = temparrayDistanceFromView[anObject.sorted_list[poly]+1];
                    temparrayDistanceFromView[anObject.sorted_list[poly]+1] = tempDouble;
               
                    // update object structure
                    temp = anObject.sorted_list[anObject.sorted_list[poly]];
                    anObject.sorted_list[anObject.sorted_list[poly]]   = anObject.sorted_list[anObject.sorted_list[poly]+1];
                    anObject.sorted_list[anObject.sorted_list[poly]+1] = temp;

                    sorted = false;          // not sorted
                } // end if

            } // end if
        } // end for
    } // end while
*/

/*
    // sort z values
    while (sorted == false)
    {
        sorted = true;          // assume sorted.

        // for each poly that makes the WorldItem
        for (int i=0; i<anObject.polys; i++)
        {
            // if its not the last poly
            if (i < (anObject.polys-1))
            {
                if (temparrayZ[i] < temparrayZ[i+1])
                {
                    // swap the values
                    temp = temparrayZ[i];
                    temparrayZ[i] = temparrayZ[i+1];
                    temparrayZ[i+1] = temp;
               
                    // update object structure
                    temp = anObject.sorted_list[i];
                    anObject.sorted_list[i]   = anObject.sorted_list[i+1];
                    anObject.sorted_list[i+1] = temp;

                    sorted = false;          // not sorted
                } // end if
            } // end if
        } // end for
    } // end while
*/
    // debug
    if (debug_method)
    {
        System.out.println("SortPolys " + debug_counter + " polys sorted");
    } // end if

}   // end SortPolys


/**
//void SortObjects(WorldItem allObjects[], Craft viewpoint, int[] sortedList, int startIndex, int numberOfObjects)
//    SortObjects(objects, craft, sortedObjectList, totalObjects);
//void getIntersectPoint(WorldItem anItem, int startIndex, int endIndex, int vertexCount, double newX[], double newY[], double newZ[], double ClipValue)
//void getIntersectPoint(double StartX, double StartY, double StartZ, double EndX, double EndY, double EndZ, double *newX, double *newY, double *newZ, double ClipValue)
 * Sorts a list of objects into z order, with the furthest object at the start and
 *  the closest being last in the list.  This can be used to ensure that furthest
 *  objects never obsure the closest.
 * 
 * @param allObjects
 * @param viewpoint
 * @param startIndex
 * @param numberOfObjects
 */
void SortObjects(WorldItem allObjects[], Craft viewpoint, int startIndex, int numberOfObjects){
    boolean debug_method = false;
    int debug_counter = 0;
//    int total_Z;
    double temparray[] = new double[MAX_OBJECTS];
    boolean not_sorted = false;
    int temp;


    // init arrays
    for (int object=startIndex; object<numberOfObjects; object++)
    {
        // clear current sorted object list
        //        sortedList[object] = object;
//        sortedObjectList[object] = object;

        // for each object pre load the temparray with the average distance from craft value
        // average distance of current poly (z distance)
//        temparray[object] = (double) (allObjects[object].averageZposition) - viewpoint.eyeZ;
//        temparray[object] = allObjects[object].averageDistanceFromViewer;
        temparray[sortedObjectList[object]] = allObjects[sortedObjectList[object]].averageDistanceFromViewer;
    }


    // sort distance values
    while (not_sorted == false)
    {
       not_sorted = true;          // assume sorted.

       for (int object=startIndex; object<numberOfObjects; object++)
       {
//        if (not_sorted == false)
//        {
//            // restart sort
//            not_sorted = true;          // assume sorted.
//        } // end if
/*
            // sort each object
            if (object < numberOfObjects)
            {
                if (temparray[object] < temparray[object+1])
                {
                    debug_counter++;
                   
                    // swap the values
                    temp = (int)temparray[object];
                    temparray[object] = temparray[object+1];
                    temparray[object+1] = temp;
               
                    // update object structure

                    temp = sortedObjectList[object];
                    sortedObjectList[object]   = sortedObjectList[object+1];
                    sortedObjectList[object+1] = temp;
//                    temp = sortedList[object];
//                    sortedList[object]   = sortedList[object+1];
//                    sortedList[object+1] = temp;

                    not_sorted = false;          // not sorted
                } // end if
            } // end if
*/

            // sort each object
            if (object < (numberOfObjects-1))
            {
                if ( allObjects[sortedObjectList[object]].averageDistanceFromViewer < allObjects[sortedObjectList[object+1]].averageDistanceFromViewer )
//                if (temparray[sortedObjectList[object]] < temparray[sortedObjectList[object+1]])
                {
                    debug_counter++;
                   
                    // swap the values
                    temp = (int)temparray[sortedObjectList[object]];
                    temparray[sortedObjectList[object]] = temparray[sortedObjectList[object+1]];
                    temparray[sortedObjectList[object+1]] = temp;
               
                    // update object structure

                    temp = sortedObjectList[object];
                    sortedObjectList[object]   = sortedObjectList[object+1];
                    sortedObjectList[object+1] = temp;
//                    temp = sortedList[object];
//                    sortedList[object]   = sortedList[object+1];
//                    sortedList[object+1] = temp;

                    not_sorted = false;          // not sorted
                } // end if
            } // end if


       } // end for
    } // end while


    // debug
    if (debug_method)
    {
        System.out.println("SortObjects " + debug_counter + " objects sorted");
        for (int object=startIndex; object<numberOfObjects; object+=2)
        {
//            if (object<numberOfObjects-1)
                //System.out.println("%d %4.2f \t%d %4.2f\n", sortedList[object], temparray[object], sortedList[object+1], temparray[object+1]);
        } // end for
    } // end if

}   // end SortObjects



/**
 *  determines if all the points in the poly are inclosed within the foreground poly 
 * @param anObject
 * @param polyIndex
 * @param foregroundObject
 * @param forgroundPoly
 * @return
 */
boolean polyInside(WorldItem anObject, int polyIndex, WorldItem foregroundObject, int forgroundPoly) {
    double angle = 0;
    boolean hidden = false;
//    anObject[object].hiddenObject = false;


//    crafts[currentCraft].eyeX += 10 * Math.sin(-crafts[currentCraft].Yaw);
//    crafts[currentCraft].eyeZ += 10 * Math.cos(crafts[currentCraft].Yaw);

    // for each point in the poly teat against the bounds of the foreground poly
    for (int point=0; point< anObject.polylist[polyIndex][0]; point++)
    {
        // for each point in the foreground object add up the angle to determin if the point is inside
        for (int comparePoly=0; comparePoly<foregroundObject.polylist[polyIndex][0]; comparePoly++)
        {
            angle += Math.sin(anObject.polylist[polyIndex][0]);
        }

        if (angle == (2*Math.PI))
        {
            hidden = true;
        }

    }

    // not hidden
    return (hidden);
    
} // end polyInside




/**
 * Flags each face that is obschured by the face of another or the same object as Hidden.
 * This prevents the need for hidden faces to be drawn and then masked out by another face later when drawing
 * a sorted list of objects
 * This module should be executed after SortObjects()
 * 
 * @param allObjects
 * @param viewpoint
 * @param startIndex
 * @param numberOfObjects
 */
void removeHiddenSurfaces(WorldItem allObjects[], Craft viewpoint, int startIndex, int numberOfObjects) {
    boolean debug_method = false;
    int debug_counter = 0;
//    int total_Z;
    double temparray[] = new double[MAX_OBJECTS];
    boolean not_sorted = false;
    int temp;


    // for each object initialise them to NOT Hidden
    for (int object=0; object<numberOfObjects; object++)
    {
        // clear hidden object list
        allObjects[object].hiddenObject = false;

        // clear hidden poly for each poly in the object
        for (int poly=0; poly<allObjects[object].polys; poly++)
        {
            allObjects[object].hiddenPolys[poly] = false;
        }
    }

    // for each object in sorted order check for hidden faces
    for (int object=0; object<numberOfObjects; object++)
    {

        // for each object from the current to the last unless found to be hidden earlier
        for (int foregroundObject=object; foregroundObject<numberOfObjects; foregroundObject++)
        {
//            allObjects[sortedObjectList[object]]

            // for each poly in the current object check against each poly in the forground objects
            for (int poly=0; poly<allObjects[sortedObjectList[object]].polys; poly++)
            {
                // for each poly in the current object check against each poly in the forground objects
                for (int foregroundPoly=allObjects[sortedObjectList[foregroundObject]].polys-1; foregroundPoly>=0; --foregroundPoly)
                {
                    if ( polyInside(allObjects[sortedObjectList[object]], poly,
                                allObjects[sortedObjectList[foregroundObject]], foregroundPoly))
                    {
                        allObjects[sortedObjectList[object]].hiddenPolys[poly] = true;
                    }
                    

                } // end for each foregroundPloy

            } // end for each poly

        } // end for each foregroundObject

    } // end for each object


}   // end removeHiddenSurfaces



/**
 *  mat1 = 1*4, mat2 = 4*4, mat3 = 1*4 
 *     (0,1,2,3) X ( 0, 1, 2, 3)        = (0,1,2,3)
 *     ( 4, 5, 6, 7)
 *     ( 8, 9,10,11)
 *     (12,13,14,15)
 * 
 * @param mat1
 * @param mat2
 * @param mat3
 */
static void threeDcross14(double mat1[], double mat2[], double mat3[]) {
    float temp;

    for (int i=0; i<4; i++) {   // clear output mat3
        mat3[i] = 0;
    }
    
    for (int j=0; j<4; j++) {
        temp = 0;
        for (int k=0; k<4; k++){
            temp += mat1[k] * mat2[k+(k*3)+j];
        }
        mat3[j] = temp;
    }

} /* end threeDcross14 */


/**
 *  threeDcross44
 *   mat1 = 4*4, mat2 = 4*4, mat3 = 1*4
 *      ( 0, 1, 2, 3)   X   ( 0, 1, 2, 3)    =    ( 0, 1, 2, 3)
 *      ( 4, 5, 6, 7)       ( 4, 5, 6, 7)         ( 4, 5, 6, 7)
 *      ( 8, 9,10,11)       ( 8, 9,10,11)         ( 8, 9,10,11)
 *      (12,13,14,15)       (12,13,14,15)         (12,13,14,15)
 * 
 * @param mat1
 * @param mat2
 * @param mat3
 */
void threeDcross44(final float mat1[], final float mat2[], float mat3[]) {
    float temp;

    for (int i=0; i<16; i++) {   // clear output mat3
        mat3[i] = 0;
    }

    for (int j=0; j<16; j++) {
        temp = 0;
        for (int k=0; k<4; k++){
            temp += mat1[k] * mat2[k+(k*3)+j];
        }
        mat3[j] = temp;
    }

    if (debug && verboseDebug)
    {
        System.out.println("Run threeDcross44");
    }

} /* end threeDcross44 */


/**
 *  mat1 = 4*4, mat2 = 4*4, mat3 = 4*4
 *     ( 0, 1, 2, 3)   .   ( 0, 1, 2, 3)    =  ( 0, 1, 2, 3)
 *     ( 4, 5, 6, 7)       ( 4, 5, 6, 7)       ( 4, 5, 6, 7)
 *     ( 8, 9,10,11)       ( 8, 9,10,11)       ( 8, 9,10,11)
 *     (12,13,14,15)       (12,13,14,15)       (12,13,14,15)
 * 
 * @param mat1
 * @param mat2
 * @param mat3
 */
void threeDdot(final float mat1[], final float mat2[], float mat3[]) {

        for (int i=0; i<16; i++){
                mat3[i] = mat1[i] + mat2[i];
        }

        if (debug && verboseDebug)
        {
                System.out.println("Run treeDdot");
        }

} /* end threeDdot */


/**
 * 
 */
public static boolean runlocal = true;

public static void loadLandItems() {
    String objectName;
    int discardDegRotation = 0;
    int initX = 0, initY = 0, initZ = 0;
    double preRotation = 0.0;
    int scale = 0;
    boolean skipItem = false;
    int objectsCopied = 0;

    objectName = graphDeanFly.landItems;
    BufferedReader in =     readUrlOrLocalFile(objectName);

    try {
        String str;
        str = in.readLine();

        StringTokenizer st = new StringTokenizer(str);
        String tokenStr = st.nextToken();
        objectName = tokenStr;
        
        boolean copied;        // was the object copied from an existing one or loaded from URL/File

        while (objectName.compareTo("endOfObjects") != 0)
        {
            // ignore lines starting with #
            if (objectName.startsWith("#") == false)
            {

                try {
                    initX = Integer.parseInt(st.nextToken());
                    initY = Integer.parseInt(st.nextToken());
                    initZ = Integer.parseInt(st.nextToken());
                    discardDegRotation = Integer.parseInt(st.nextToken());
                    preRotation = discardDegRotation * (Math.PI/180);
                    scale = Integer.parseInt(st.nextToken());
                } catch (java.lang.NumberFormatException e) {
                    System.out.println("NumberFormatException in loadLandItems!");
                    skipItem = true;
                }
        
                if (skipItem == false)
                {
                    if (nWorldObjects < MAX_OBJECTS)
                    {
                        copied = false;

                        ///*                        
                        // DCDebug: to be fixed - Object once copied must be relocated to the intended location
                        // check if we have loaded an object of the same type
                        for (int loadedObjects=0; loadedObjects<nWorldObjects; loadedObjects++)
                        {
                            if (objects[loadedObjects].objectName == null)
                            {
                                System.out.println("loadLandItems: Oject " + loadedObjects + " of " + nWorldObjects + " is NULL");
                                break;
                            }
                            else
                            {
                                // if so copy an existing object rather than loading it from a URL/File
                                if (objects[loadedObjects].objectName.compareTo(objectName) == 0)
                                {
                                    objects[nWorldObjects] = copyObject(crafts[currentCraft], objects[loadedObjects]);
//                                    scale = 0;
                                    copied = true;
                                    objectsCopied++;
                                    
                                    System.out.println("loadLandItems: Copied Object " + loadedObjects + " to " + nWorldObjects + " " + objects[loadedObjects].objectName);

                                    // Copied Object must be relocated to the intended position
                                    break;
                                }
                            }
                        }
//*/
                        // if an existing object could not be found load from URL/File
                        if (copied != true)
                        {
                            // Attempt to import a .dat object file or Wavefront .obj file https://en.wikipedia.org/wiki/Wavefront_.obj_file
                            // ignore lines starting with #
                            if ( (objectName.endsWith(".dat") == true) || (objectName.endsWith(".obj") == true))
                            {
                                    objects[nWorldObjects] = loadObjectURLdat(crafts[currentCraft], objectName);
                                    if(objects[nWorldObjects] == null)
                                    {
                                        objects[nWorldObjects] = loadObject(crafts[currentCraft], objectName);
                                    }
                            }
                            else
                            {
                                objects[nWorldObjects] = loadObjectURL(crafts[currentCraft], objectName);
                            }
                        }

                        // check that the object details were loaded from file/url
                        if(objects[nWorldObjects] != null)
                        {
                            // DCDEBUG                        
                            //initObject(objects[nWorldObjects], initX, initY, initZ, preRotation, scale);     // set object location and rotation to 0
                            objects[nWorldObjects].initX = initX ;
                            objects[nWorldObjects].initY = initY;
                            objects[nWorldObjects].initZ = initZ;
                            objects[nWorldObjects].discardDegRotation = discardDegRotation;
                            objects[nWorldObjects].preRotation = preRotation;
                            objects[nWorldObjects].scale = scale;

                            // sort polys of the new object
                            // DCDEBUG
                            //SortPolys(objects[nWorldObjects], crafts[currentCraft]);

                            nWorldObjects++;

                        }
                        else {
                            System.out.println("Warning: " + objectName + " not loaded, due to object not found!");
                        }
                    }
                    else {
                        System.out.println("Warning: " + objectName + " not loaded, Max objects exceeded!");
                    }
                }

            } // end if not '#'
        
            skipItem = false;

            // get poly list
            str = in.readLine();
            st = new StringTokenizer(str);

            tokenStr = st.nextToken();
            objectName = tokenStr;

        } // end while


         in.close();
     
     
     System.out.println("Copied " + objectsCopied + " of " + nWorldObjects + " objects loaded");

    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadLandItems!");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadLandItems\n");
    }

} // end loadLandItems


/**
 * 
 */
public static void loadLandMass() {
    String objectName;
    if(debug)
    {
        System.out.println("loadLandMass - Begin");
    }

    objectName = graphDeanFly.landMass;
    BufferedReader in =     readUrlOrLocalFile(objectName);
    try {

        String str;
        
        str = in.readLine();
        
        StringTokenizer st = new StringTokenizer(str);
        String tokenStr = st.nextToken();
        objectName = tokenStr;

        while (objectName.compareTo("endOfObjects") != 0)
        {
            // ignore lines starting with #
            if (objectName.startsWith("#") == false)
            {

                if (objectName.compareTo("CircleObject") == 0)
                {
                    int size;
                    int red, green, blue;
                    Color colour;
                    int points;
                    String spacer;
                    int initX, initY, initZ, scale;
                    int discardDegRotation;

                    double preRotation;
            
                    try {
                        size    = Integer.parseInt(st.nextToken());
                        red     = Integer.parseInt(st.nextToken());
                        green   = Integer.parseInt(st.nextToken());
                        blue    = Integer.parseInt(st.nextToken());
                        colour  = new Color(red, green, blue);
//                        colour  = st.nextToken();
                        points  = Integer.parseInt(st.nextToken());
                        spacer  = st.nextToken();
                        initX   = Integer.parseInt(st.nextToken());
                        initY = Integer.parseInt(st.nextToken());
                        initZ = Integer.parseInt(st.nextToken());
                        discardDegRotation = Integer.parseInt(st.nextToken());
                        preRotation = discardDegRotation * (Math.PI/180);
//                        scale = Integer.parseInt(st.nextToken());

                        // create Circle object and initialize
                        objects[nWorldObjects] = CircleObject (size, colour, points);
//                        initObject(objects[nWorldObjects], initX, initY, initZ, preRotation, 1);     // set object location and rotation to 0
                        objects[nWorldObjects].initX = initX ;
                        objects[nWorldObjects].initY = initY;
                        objects[nWorldObjects].initZ = initZ;
                        objects[nWorldObjects].discardDegRotation = discardDegRotation;
                        objects[nWorldObjects].preRotation = preRotation;
                        objects[nWorldObjects].scale = 1;

                        objects[nWorldObjects].objectName = new String("CircleObject");
                        nWorldObjects++;
            

                    } catch (java.lang.NumberFormatException e) {
                        System.out.println(" NumberFormatException in loadLandMass!\n");
                    }
        
                } // end if CircleObject

                else if (objectName.compareTo("file") == 0)
                {
                    int discardDegRotation = 0;
                    int initX = 0, initY = 0, initZ = 0;
                    double preRotation = 0.0;
                    int scale = 0;
                    boolean skipItem = false;

                    try {
                        objectName = st.nextToken();
                        initX = Integer.parseInt(st.nextToken());
                        initY = Integer.parseInt(st.nextToken());
                        initZ = Integer.parseInt(st.nextToken());
                        discardDegRotation = Integer.parseInt(st.nextToken());
                        preRotation = discardDegRotation * (Math.PI/180);
                        scale = Integer.parseInt(st.nextToken());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println(" NumberFormatException in loadLandMass!\n");
                        skipItem = true;
                    }
        
                    if (skipItem == false)
                    {
                        if (nWorldObjects < MAX_OBJECTS) {
                            objects[nWorldObjects] = loadObjectURL(crafts[currentCraft], objectName);
//                            initObject(objects[nWorldObjects], initX, initY, initZ, preRotation, scale);     // set object location and rotation to 0
                        objects[nWorldObjects].initX = initX ;
                        objects[nWorldObjects].initY = initY;
                        objects[nWorldObjects].initZ = initZ;
                        objects[nWorldObjects].discardDegRotation = discardDegRotation;
                        objects[nWorldObjects].preRotation = preRotation;
                        objects[nWorldObjects].scale = scale;

                            nWorldObjects++;
                        }
                        else {
                            System.out.println("Warning: " + objectName + " not loaded, Max objects exceeded!");
                        }
                    }
                    
                }

                else
                {
                    System.out.println("Unknown object type " + objectName + " in file ");
                }

            } // end if not '#'

            // get poly list
            str = in.readLine();
            st = new StringTokenizer(str);

            tokenStr = st.nextToken();
            objectName = tokenStr;
        } // end while


         in.close();
     
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadLandMass!\n");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadLandMass\n");
    }

    if(debug)
    {
        System.out.println("loadLandMass - Exit");
    }
} // end loadLandMass


/**
 * 
 */
public static void loadCraft() {
    String objectName;

    objectName = graphDeanFly.craftType;
    BufferedReader in =     readUrlOrLocalFile(objectName);
    try {
        String str;
        
        str = in.readLine();
        
        StringTokenizer st = new StringTokenizer(str);
        String tokenStr = st.nextToken();
        objectName = tokenStr;

        while (objectName.compareTo("endOfFile") != 0)
        {
            // ignore lines starting with #
            if (objectName.startsWith("#") == false)
            {

                // Walker startX startY startZ height orientationDeg
                if (objectName.compareTo("Walker") == 0)
                {
                    int startX, startY, startZ, height, orientationDeg;

                    try {
                        startX  = Integer.parseInt(st.nextToken());
                        startY  = Integer.parseInt(st.nextToken());
                        startZ  = Integer.parseInt(st.nextToken());
                        height  = Integer.parseInt(st.nextToken());
                        orientationDeg  = Integer.parseInt(st.nextToken());

                        craft.orgX = startX;
                        craft.orgY = startY + height;
                        craft.orgZ = startZ;

                        // create a new Craft
                        Craft newCraft = new Craft();

                        newCraft.type    = objectName;
                        newCraft.orgX    = startX;
                        newCraft.orgY    = startY + height;
                        newCraft.orgZ    = startZ;
                        newCraft.orgYaw    = orientationDeg;

                        crafts[GraphPnl.availableCrafts] = newCraft;
                        availableCrafts++;

                    } catch (java.lang.NumberFormatException e) {
                        System.out.println(" NumberFormatException in loadCraft!\n");
                    } catch (java.lang.NullPointerException e) {
                        System.out.println(" NullPointerException in loadCraft!\n");
                    }
        
                } // end if Walker

                // AirCraft startX startY startZ Roll Pitch Yaw maxSpeed
                else if (objectName.compareTo("AirCraft") == 0)
                {
                    int startX, startY, startZ, roll, pitch, yaw, maxSpeed;
            
                    try {
                        startX   = Integer.parseInt(st.nextToken());
                        startY   = Integer.parseInt(st.nextToken());
                        startZ   = Integer.parseInt(st.nextToken());
                        roll     = Integer.parseInt(st.nextToken());
                        pitch    = Integer.parseInt(st.nextToken());
                        yaw      = Integer.parseInt(st.nextToken());
                        maxSpeed = Integer.parseInt(st.nextToken());

                        craft.orgX = startX;
                        craft.orgY = startY;
                        craft.orgZ = startZ;

                        // create a new Craft
                        Craft newCraft = new Craft();

                        newCraft.type     = objectName;
                        newCraft.orgX     = startX;
                        newCraft.orgY     = startY;
                        newCraft.orgZ     = startZ;
                        newCraft.orgRoll  = roll;
                        newCraft.orgPitch = pitch;
                        newCraft.orgYaw   = yaw;
                        newCraft.maxSpeed = maxSpeed;

                        crafts[availableCrafts] = newCraft;
                        availableCrafts++;
                        
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println(" NumberFormatException in loadCraft!\n");
                    } catch (java.lang.NullPointerException e) {
                        System.out.println(" NullPointerException in loadCraft!\n");
                    }
        
                } // end if AirCraft

                // Car startX startY startZ orientationDeg MaxSpeed
                else if (objectName.compareTo("Car") == 0)
                {
                    int startX, startY, startZ, orientationDeg, maxSpeed;
            
                    try {
                        startX   = Integer.parseInt(st.nextToken());
                        startY   = Integer.parseInt(st.nextToken());
                        startZ   = Integer.parseInt(st.nextToken());
                        orientationDeg      = Integer.parseInt(st.nextToken());
                        maxSpeed = Integer.parseInt(st.nextToken());

                        craft.orgX = startX;
                        craft.orgY = startY;
                        craft.orgZ = startZ;

                        // create a new Craft
                        Craft newCraft = new Craft();

                        newCraft.type     = objectName;
                        newCraft.orgX     = startX;
                        newCraft.orgY     = startY;
                        newCraft.orgZ     = startZ;
                        newCraft.orgYaw   = orientationDeg;
                        newCraft.maxSpeed = maxSpeed;

                        crafts[availableCrafts] = newCraft;
                        availableCrafts++;
                        
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println(" NumberFormatException in loadCraft!\n");
                    }

                } // end if Car

                else
                {
                    System.out.println("Unknown craft type " + objectName + " in file ");
                }
                
            } // end if not '#'

            // get poly list
            str = in.readLine();
            st = new StringTokenizer(str);

            tokenStr = st.nextToken();
            objectName = tokenStr;
        }



         in.close();
     
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException at end of loadCraft!\n");
    } catch (java.io.IOException e) {
        System.out.println("IOException at end of loadCraft\n");
        } catch (java.lang.NullPointerException e) {
                System.out.println(" NullPointerException at end of loadCraft!\n");
    }

    System.out.print(availableCrafts + " Crafts loaded. ");

} // end loadCraft


/**
 * 
 * @param viewpoint
 * @param sourceObject
 * @return
 */
public static WorldItem copyObject(Craft viewpoint, WorldItem sourceObject){
    WorldItem newItem = new WorldItem();
    
    newItem.objectName = sourceObject.objectName;
    newItem.vertices = sourceObject.vertices;
    newItem.polys = sourceObject.polys;


    // get vertex information
    for (int vertex=0; vertex<newItem.vertices; vertex++)
    {
         newItem.RealX[vertex] = sourceObject.RealX[vertex];
        newItem.RealY[vertex] = sourceObject.RealY[vertex];
        newItem.RealZ[vertex] = sourceObject.RealZ[vertex];
        newItem.RealH[vertex] = sourceObject.RealH[vertex];
    }

    // get poly information
    for (int polycount=0; polycount<newItem.polys; polycount++)
    {
        newItem.polylist[polycount][0] = sourceObject.polylist[polycount][0];    // number of points
        newItem.polyColour[polycount] = sourceObject.polyColour[polycount];        // poly colour

        // get each poly point
        for (int poly=0; poly<newItem.polylist[polycount][0]; poly++)
        {
            newItem.polylist[polycount][poly+1] = sourceObject.polylist[polycount][poly+1];
        }
    }
    
    // sort poly list
//    SortPolys(newItem, viewpoint);

    return(newItem);    

} // end copyObject()



/**
 *  load an object and its colour
 *   uses ->
 *       WorldItem :    input file
 *    modifies ->
 *        WorldItem :
 *                posx, posy, posz, rotx, preRoty, rotz
 *                newX[], newY[], newZ[],
 *                RealX[], RealY[], RealZ[]
 *    
 *     Calls Funtions ->
 *         threeDcross14()
 * 
 * @param viewpoint
 * @param objectfile
 * @return
 */
public static WorldItem loadObjectURL(Craft viewpoint, String objectfile) {
    if (debug)
        System.out.println(" Run loadObjectURL - Begin " + objectfile);

    WorldItem newItem = new WorldItem();
    int vertices = 0;
    int polys  = 0;
    int polycount = 0 ;
    int tempX, tempY, tempZ, tempH;
    int red, green, blue;
    int polypoints, colour, i;
//    boolean debug = true, verboseDebug = true;
//    boolean debug = false, verboseDebug = false;

    // get Hostname for the HTML file
//    String worldLocation = new String(graph.hostURLparameter.concat("world/"));    
//    String serverObjects = new String(graph.worldLocation.concat("objectsnew/"));    
    String objectName = new String(graphDeanFly.serverObjects.concat(objectfile));

    // name the object so it can be found and copied later
    newItem.objectName = objectfile;

    if(debug)
    {
        if(newItem.objectName == null)
            System.out.println("loadObjectURL: New object name was not copied to the object array object name element");
        else
            System.out.println("loadObjectURL: New object name was copied to the object array object name element " + newItem.objectName);
    }
    
    BufferedReader in =     readUrlOrLocalFile(objectName);
    try {
     
        String str;
        str = in.readLine();

        
        StringTokenizer st = new StringTokenizer(str);
        String tokenStr = st.nextToken();
        newItem.vertices = Integer.parseInt(tokenStr);
        newItem.polys = Integer.parseInt(st.nextToken());


        // if there are no vertex points in the file exit with error 1
        if (newItem.vertices <= 0) {
            System.out.println("Bad vertex count reading file " + objectfile);
        }
        if (debug)
            /* Read first values into array */
//            System.out.println("LoadObjectURL objectName:" + objectName);
            System.out.println("LoadObjectURL objectName:" + objectName + " comprises of " + newItem.vertices + " vertices " + " defining " + newItem.polys + " ploys");

        // get vertex information
        for (i=0; i<newItem.vertices; i++)
        {
            str = in.readLine();
            st = new StringTokenizer(str);

            newItem.RealX[i] = Integer.parseInt(st.nextToken());
            newItem.RealY[i] = Integer.parseInt(st.nextToken());
            newItem.RealZ[i] = Integer.parseInt(st.nextToken());
            newItem.RealH[i] = Integer.parseInt(st.nextToken());

            if (verboseDebug)
                System.out.println(" x=" + newItem.RealX[i] + " y=" + newItem.RealY[i] + " z=" + newItem.RealZ[i] + " h=" + newItem.RealH[i]);
        }
    
        tempX = (int)newItem.RealX[i];
        polypoints = 1;
     
        while ((tempX != 999) && (polypoints != 999))
        {
            // get poly list
            str = in.readLine();
            st = new StringTokenizer(str);
//            polypoints  = Integer.parseInt(st.nextToken());
//            colour = Integer.parseInt(st.nextToken());

            newItem.polylist[polycount][0] = Integer.parseInt(st.nextToken());    // number of points for this poly
            red   = Integer.parseInt(st.nextToken());
            green = Integer.parseInt(st.nextToken());
            blue  = Integer.parseInt(st.nextToken());
            newItem.polyColour[polycount] = new Color(red, green, blue);        // poly colour
//            newItem.polyColour[polycount] = new Color(Integer.parseInt(st.nextToken()));
//            newItem.polyColour[polycount] = new Color(1048575*Integer.parseInt(st.nextToken()));

            if (verboseDebug)
            {
                System.out.println(" colour-" + red + " " + green + " " + blue);
                System.out.print(" points-" + newItem.polylist[polycount][0] + " ");
            }

            // get each poly point
            for (i=0; i<newItem.polylist[polycount][0]; i++)
            {
                tempX = Integer.parseInt(st.nextToken());
    
                newItem.polylist[polycount][i+1] = tempX;

                if (verboseDebug)
                    System.out.print(" point-" + i + ":" + tempX + " ");
            }

            if (verboseDebug)
                System.out.println("");

            // increment polys loaded
            ++polycount;

        } // end while ((tempX != 999) && (polypoints != 999))

        // sort poly list
// DCDEBUG
//        SortPolys(newItem, viewpoint);

     in.close();
     
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadObjectURL!\n");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadObjectURL " + objectName + "\n");
                newItem.objectName = null;
                return null;
    }

    if (debug)
        System.out.println(" Run loadObjectURL - Exit");
    
    return (newItem);
} // end loadObjectURL


/**
 *  load an object and its colour
 *   uses ->
 *       WorldItem :    input file
 *   modifies ->
 *       WorldItem :
 *               posx, posy, posz, rotx, preRoty, rotz
 *               newX[], newY[], newZ[],
 *               RealX[], RealY[], RealZ[]
 *    Calls Funtions ->
 *        threeDcross14()
 *        
 *  Attempt to import a Wavefront .obj file see: https://en.wikipedia.org/wiki/Wavefront_.obj_file
 *  # this is a comment
 *   # List of geometric vertices, with (x,y,z[,w]) coordinates, w is optional and defaults to 1.0.
 *   v 0.123 0.234 0.345 1.0
 *   
 *   # List of texture coordinates, in (u, v [,w]) coordinates, these will vary between 0 and 1, w is optional and defaults to 0.
 *   vt 0.500 1 [0]
 *   
 *   # List of vertex normals in (x,y,z) form; normals might not be unit vectors.
 *   vn 0.707 0.000 0.707
 *   vn ...
 *   
 *   # Parameter space vertices in ( u [,v] [,w] ) form; free form geometry statement ( see below )
 *   vp 0.310000 3.210000 2.100000
 *   vp ...
 *   
 *   # Polygonal face element (see below)
 *   f 1 2 3
 *   f 3/1 4/2 5/3
 *   f 6/4/1 3/5/3 7/6/5
 *  
 *  v -1 1499 0
 *  f 4 64 128 128 0 1 3 2
 * 
 * @param viewpoint
 * @param objectfile
 * @return
 */
public static WorldItem loadObjectURLdat(Craft viewpoint, String objectfile) {
    WorldItem newItem = new WorldItem();
    int linecount = 0;
/*    int vertices = 0;
    int polys  = 0;
    int tempX, tempY, tempZ, tempH;
*/
//    int polycount = 0 ;
    int red   = 255;
    int green = 0;
    int blue  = 0;
/*    int polypoints, colour, i;
    boolean debug = false, verboseDebug = false;
    String lineFlag;
*/
    // get Hostname for the HTML file
    String objectName = new String(graphDeanFly.serverObjects.concat(objectfile));

    // name the object so it can be found and copied later
    newItem.objectName = objectfile;
    newItem.vertices = 0;
    newItem.polys = 0;

//    if(debug)
//    {
        if(newItem.objectName == null)
            System.out.println("loadObjectURL: New object name was not copied to the object array object name element");
        else
            System.out.println("loadObjectURL: New object name was copied to the object array object name element " + newItem.objectName);
//    }

    BufferedReader in =     readUrlOrLocalFile(objectName);
    try {

     
        String str;
        str = in.readLine();
        linecount++;
        if(!str.isEmpty())
        {
            
            StringTokenizer st = new StringTokenizer(str);
            String tokenStr = st.nextToken();
        
    
            while ((tokenStr.compareTo("endOfFile") != 0) && str != null) 
            {
                // ignore lines starting with #
                if ( (tokenStr!= null) && (!tokenStr.startsWith("#")) && (!tokenStr.equals("")) )
                {
                                    
                    // mtllib [external .mtl file name]
                    if (tokenStr.startsWith("mtllib")  || tokenStr.startsWith("usemtl"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront .obj line:" + linecount + " " + str);
                        
                        if(tokenStr.startsWith("usemtl"))
                        {
                            String colour = st.nextToken();
                            
                            if(colour.compareToIgnoreCase("red") == 0)
                            {
                                red   = 255;    green = 0;    blue  = 0;
                            }
                           
                            if(colour.compareToIgnoreCase("green") == 0) {
                                red   = 0;    green = 255;    blue  = 0;
                            }
                           
                            if(colour.compareToIgnoreCase("blue") == 0) {
                                red   = 0;    green = 0;    blue  = 255;
                            }
                                   
                            if(colour.compareToIgnoreCase("yellow") == 0) {
                                red   = 255;    green = 255;    blue  = 0;
                            }
                                   
                        }
                    }
    
                    // Smooth shading can be disabled
                    else if (tokenStr.startsWith("s"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront file:" + objectfile + " line:" + linecount + " " + str);
                    }
    
                    // Named objects and polygon groups are specified via the following tags
                    else if (tokenStr.startsWith("g") || tokenStr.startsWith("o"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront file:" + objectfile + " line:" + linecount + " " + str);
                    }
    
                    // get vertex list List of texture coordinates
                    else if (tokenStr.startsWith("vt"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront file:" + objectfile + " line:" + linecount + " " + str);
                    }
    
                    // get vertex list  Parameter space vertices
                    else if (tokenStr.startsWith("vp"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront file:" + objectfile + " line:" + linecount + " " + str);
                    }
    
                    // get vertex list List of vertex normals
                    else if (tokenStr.startsWith("vn"))
                    {
                        // option not supported
                        System.out.println("unsupported tag in Wavefront file:" + objectfile + " line:" + linecount + " " + str);
                    }
    
                    // get vertex list
                    else if (tokenStr.startsWith("v"))
                    {
                        try {

                            if(!objectfile.endsWith(".obj"))
                            {
                                // .dat file
                                newItem.RealX[newItem.vertices] = (Double.valueOf(st.nextToken())).intValue();
                                newItem.RealZ[newItem.vertices] = (Double.valueOf(st.nextToken())).intValue();
                                newItem.RealY[newItem.vertices] = (Double.valueOf(st.nextToken())).intValue();
                            }
                            else
                            {
                                // Wavefront .obj
                                newItem.RealX[newItem.vertices] = Double.valueOf(st.nextToken()).intValue();  // TODO This should be double 
                                newItem.RealY[newItem.vertices] = Double.valueOf(st.nextToken()).intValue();
                                newItem.RealZ[newItem.vertices] = Double.valueOf(st.nextToken()).intValue();

                                //System.out.println("Wavefront vertex(" + newItem.vertices+1 + ") x:" + newItem.RealX[newItem.vertices] + " y:" + newItem.RealY[newItem.vertices] + " z:" + newItem.RealZ[newItem.vertices]);
                            }
                            newItem.RealH[newItem.vertices] = 1;  // common
                            
                            newItem.vertices++;
                            
                        } catch (java.lang.NumberFormatException e) {
                            System.out.println("NumberFormatException in vertex loadObjectURLdat!");
  //                        skipItem = true;
                        } catch (java.util.NoSuchElementException e) {
                            System.out.println("NoSuchElementException in loadObjectURLdat vertex " + newItem.vertices + " line " + linecount);
                        }
                    } // end if 'v '
    
                    // get poly face list
                    else if (tokenStr.startsWith("f"))
                    {
                        try {
                            // get poly list
    
                            if(!objectfile.endsWith(".obj"))
                            {
                                newItem.polylist[newItem.polys][0] = Integer.parseInt(st.nextToken());    // number of points for this poly
                                red   = Integer.parseInt(st.nextToken());
                                green = Integer.parseInt(st.nextToken());
                                blue  = Integer.parseInt(st.nextToken());
                            }
                            newItem.polyColour[newItem.polys] = new Color(red, green, blue);        // poly colour
    
                            // get each poly point
                            if(!objectfile.endsWith(".obj"))
                            {
                                for (int i=0; i<newItem.polylist[newItem.polys][0]; i++)
                                {
                                    newItem.polylist[newItem.polys][i+1] = Integer.parseInt(st.nextToken());
                                }
    
                                // increment polys loaded
                                ++newItem.polys;
                            }
                            else
                            {
                                int numberOfPointsForThisPoly=0;
                                if(debug)
                                    System.out.print("Wavefront poly  :" );
                                
                                while(st.hasMoreTokens())
                                {
                                    newItem.polylist[newItem.polys][numberOfPointsForThisPoly+1] = Integer.parseInt(st.nextToken()) -1;

                                    if(debug)
                                        System.out.print(" ,v" + newItem.polylist[newItem.polys][numberOfPointsForThisPoly+1] + "("+ newItem.RealX[newItem.polylist[newItem.polys][numberOfPointsForThisPoly]] + "," + newItem.RealY[newItem.polylist[newItem.polys][numberOfPointsForThisPoly]] + "," + newItem.RealZ[newItem.polylist[newItem.polys][numberOfPointsForThisPoly]] +")");

                                    numberOfPointsForThisPoly++;
                                    
                                    // keep the poly vertex count up to date
                                    newItem.polylist[newItem.polys][0] = numberOfPointsForThisPoly;    // number of points for this poly
                                
                                    if(newItem.polylist[newItem.polys][0] <= 0 ){
                                        System.out.println("NumberFormatException in poly loadObjectURLdat! " + newItem.objectName + " *" + str + "* line " + linecount);
                                    }
                                }
                                if(debug)
                                    System.out.println(", points:" + newItem.polylist[newItem.polys][0] );
    
                                    
                                // increment polys loaded
                                if(numberOfPointsForThisPoly >0)
                                    ++newItem.polys;
                                else
                                    System.out.println("ERROR polys:" + newItem.polys );

                            }
    
                            
                        } catch (java.lang.NumberFormatException e) {
                            System.out.println("NumberFormatException in poly loadObjectURLdat! " + newItem.objectName + " *" + str + "* line " + linecount);
                        } catch (java.util.NoSuchElementException e) {
                            System.out.println("NoSuchElementException in loadObjectURLdat poly " + newItem.polys + " line " + linecount + " total verticies:" + newItem.vertices);
                        }
                    } // end if 'f '
                    else
                    {
                            System.out.println("Unknown line in loadObjectURLdat " + str);
                    }
                    
                } // end if '#'
                
                str = in.readLine();
                linecount++;
                if(str != null && str.length() > 0)
                {
                    st = new StringTokenizer(str);
                    tokenStr = st.nextToken();
                }
    
            } // end while (!endOfObject)
        }

        // sort poly list
        SortPolys(newItem, viewpoint);

        in.close();
     
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadObjectURLdat!");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadObjectURLdat " + objectName + " check hostURLparameter");
                newItem.objectName = null;
                return null;
    }
    

    if (debug)
        System.out.println(" Run loadObjectURLdat!\n");
    
    return (newItem);
} // end loadObjectURLdat


public static BufferedReader readUrlOrLocalFile(String objectName)
{
//    String serverObjects = new String(graphDeanFly.hostURLparameter.concat("objects/"));    
//    String objectName = new String(serverObjects.concat(objectfile));

    BufferedReader in = null;
    try {
        
        if(objectName.contains("http"))
        {
                URL url = new URL(objectName);
                in = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        else
        {
            try {
                // Open File for Reading
                FileReader fstream = new FileReader(objectName);
                in = new BufferedReader(fstream);

            }
            catch (Exception e) {// Catch exception if any
                System.err.println("Error: readLocalFile() " + e.getMessage());
            }
        }
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadObject!\n");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadObject!\n");
    }
    return in;
}

/**
 *  load an object and its colour
 *   uses ->
 *       WorldItem :    input file
 *    modifies ->
 *        WorldItem :
 *                posx, posy, posz, rotx, preRoty, rotz
 *                newX[], newY[], newZ[],
 *                RealX[], RealY[], RealZ[]
 *    
 *     Calls Funtions ->
 *         threeDcross14()
 * 
 * @param viewpoint
 * @param objectfile
 * @return
 */
public WorldItem loadTextureURL(Craft viewpoint, String objectfile) {
    WorldItem newItem = new WorldItem();
    int vertices = 0;
    int polys  = 0;
    int polycount = 0 ;
    int tempX, tempY, tempZ, tempH;
    int polypoints, colour, i;
//    boolean debug = false, verboseDebug = false;

    // get Hostname for the HTML file
    String serverObjects = new String(graphDeanFly.hostURLparameter.concat("objects/"));    
    String objectName = new String(serverObjects.concat(objectfile));
    BufferedReader in =     readUrlOrLocalFile(objectName);

    try {
        String str;
        str = in.readLine();
        
        StringTokenizer st = new StringTokenizer(str);
        String tokenStr = st.nextToken();
        newItem.vertices = Integer.parseInt(tokenStr);
        newItem.polys = Integer.parseInt(st.nextToken());
 

        // if there are no vertex points in the file exit with error 1
        if (newItem.vertices <= 0) {
            System.out.println("Bad vertex count reading file " + objectfile);
        }
        if (debug)
        {
            /* Read first values into array */
            System.out.println("loadTextureURL objectName:" + objectName);
            System.out.println(newItem.vertices + " vertices to read from " + objectfile + " ploys to read " + newItem.polys);
        }

        // get vertex information
        for (i=0; i<newItem.vertices; i++)
        {
            str = in.readLine();
            st = new StringTokenizer(str);

            newItem.RealX[i] = Integer.parseInt(st.nextToken());
            newItem.RealY[i] = Integer.parseInt(st.nextToken());
            newItem.RealZ[i] = Integer.parseInt(st.nextToken());
            newItem.RealH[i] = Integer.parseInt(st.nextToken());

            if (debug)
            {
                System.out.println(" x=" + newItem.RealX[i] + " y=" + newItem.RealY[i] + " z=" + newItem.RealZ[i] + " h=" + newItem.RealH[i]);
            }
        }
    
        tempX = (int)newItem.RealX[i];
        polypoints = 1;
     
        while ((tempX != 999) && (polypoints != 999))
        {
            // get poly list
            str = in.readLine();
            st = new StringTokenizer(str);
//            polypoints  = Integer.parseInt(st.nextToken());
//            colour = Integer.parseInt(st.nextToken());

            newItem.polylist[polycount][0] = Integer.parseInt(st.nextToken());
            newItem.polyColour[polycount] = new Color(Integer.parseInt(st.nextToken()));
//            newItem.polyColour[polycount] = new Color(1048575*Integer.parseInt(st.nextToken()));

            // debug
            if (debug)
            {
                System.out.print(" points-" + newItem.polylist[polycount][0] + " ");
            }

            // get each poly point
            for (i=0; i<newItem.polylist[polycount][0]; i++)
            {
                tempX = Integer.parseInt(st.nextToken());
    
                newItem.polylist[polycount][i+1] = tempX;

                // debug
                if (debug)
                {
                    System.out.print(" point" + i + ":" + tempX + " ");
                }
            }

            if (debug)
                System.out.println("");

            // increment polys loaded
            ++polycount;

        }

        // sort poly list
        //SortPolys(newItem, viewpoint);
        // clear current sorted poly list
/*
        for (int polys=0; polys<newItem.polys; polys++)
        {
            newItem.sorted_list[polys] = polys;
        }
*/

     in.close();
     
    } catch (java.net.MalformedURLException e) {
        System.out.println(" MalformedURLException in loadObject!\n");
    } catch (java.io.IOException e) {
        System.out.println("IOException in loadObject!\n");
}

    if (debug)
        System.out.println(" Run loadTextureURL!\n");
    
    return (newItem);
} // end loadTextureURL


/**
 *  load an object and its colour
 *   uses ->
 *       WorldItem :    input file
 *    modifies ->
 *        WorldItem :
 *                posx, posy, posz, rotx, preRoty, rotz
 *                newX[], newY[], newZ[],
 *                RealX[], RealY[], RealZ[]
 *    
 *     Calls Funtions ->
 *         threeDcross14()
 * 
 * @param viewpoint
 * @param objectfile
 * @return
 */
public static WorldItem loadObject(Craft viewpoint, String objectfile) {
    WorldItem newItem = new WorldItem();
    //FILE *fp;
    int vertices = 0;
    int polys  = 0;
    int polycount = 0 ;
    int tempX, tempY, tempZ, tempH;
    int polypoints, colour;

    try {
        RandomAccessFile fp = new RandomAccessFile(objectfile, "r");
    
/*
    // open object text file for reading
    if ((fp=fopen(objectfile,"rt")) == NULL)
    {
        System.out.println("cannot open file " + objectfile);
        exit(1);
    }
*/

    /* Read first values into array */
//    System.out.println("reading values from object file " + objectfile + "  please wait\n");
//    fscanf(fp, "%i %i",&vertices, &polys);
        newItem.vertices = fp.readInt();

    newItem.polys = fp.readInt();
    
/*
    newItem.vertices = vertices;
    newItem.polys = polys;
*/
    System.out.println("object vertices=" + newItem.vertices + ", polys=" + newItem.polys);
    
    // if there are no vertex points in the file exit with error 1
    if (vertices <= 0) {
        System.out.println("Bad vertex count reading file " + objectfile);
    }

    if (debug)
    {
        System.out.println(" vertices to read from " + vertices + ", " + objectfile + " ploys to read " + polys);
    }
        
    // get vertex information
    for (int i=0; i<vertices; i++)
    {
    newItem.RealX[i] = fp.readInt();
    newItem.RealY[i] = fp.readInt();
    newItem.RealX[i] = fp.readInt();
    newItem.RealH[i] = fp.readInt();

//        ,"%i %i %i %i",&tempX,&tempY,&tempZ,&tempH);
//        fscanf(fp,"%i %i %i %i",&tempX,&tempY,&tempZ,&tempH);
/*        newItem.RealX[i] = tempX;
        newItem.RealY[i] = tempY;
        newItem.RealZ[i] = tempZ;
        newItem.RealH[i] = tempH;
*/

        if (debug)
        {
            System.out.println(" x=" + newItem.RealX[i] + " y=" + newItem.RealY[i] + " z=" + newItem.RealZ[i]);
        }
    }
    
    tempX = (int)newItem.RealX[iii];
    polypoints = 1;

    // get poly information
    while ((tempX != 999) && (polypoints != 999))
    {
        // get poly list
    polypoints = fp.readInt();
    colour = fp.readInt();

        newItem.polylist[polycount][0] = polypoints;
        newItem.polyColour[polycount] = Color.green;


        // debug
        if (debug)
        {
        System.out.println(" points " + polypoints);
        }

        // get each poly point
        for (int i=0; i<polypoints; i++)
        {
        tempX = fp.readInt();
    
//            fscanf(fp,"%i",&tempX);
            newItem.polylist[polycount][i+1] = tempX;

            // debug
            if (debug)
            {
                System.out.println(" point " + tempX);
            }
        }


        if (debug)
            System.out.println("");

        // increment polys loaded
        ++polycount;
    }


    // sort poly list
// DCDEBUG
//    SortPolys(newItem, viewpoint);
    // clear current sorted poly list
/*
    for (int polys=0; polys<newItem.polys; polys++)
    {
        newItem.sorted_list[polys] = polys;
    }
*/

    /* Close file */
    fp.close();
//    fclose(fp);

    } catch (java.io.FileNotFoundException e) {
        System.out.println("cannot open file " + objectfile);
    } catch (java.io.EOFException e) {
        System.out.println("EOF error " + e);
    } catch (java.io.IOException e) {
        System.out.println("Input/Output error " + e);
    }

    if (debug)
        System.out.println(" Run loadObject!\n");
    
    return (newItem);
} // end loadObject



/**
 * 
 * @param radius
 * @param colour
 * @param points
 * @return
 */
    public static WorldItem CircleObject (float radius, Color colour, int points) {
        double my_sin, my_cos;
        WorldItem newItem = new WorldItem();

        my_sin = Math.sin((2*Math.PI)/points);
        my_cos = Math.cos((2*Math.PI)/points);

        newItem.vertices = points;
        newItem.polys = 1;

        // clear object Real values
        for(int i=0; i<= newItem.vertices; i++)
        {
            newItem.RealX[i] = 0;     newItem.RealY[i] = 0;    newItem.RealZ[i] = 0;      newItem.RealH[i] = 0;
        }

        newItem.RealX[0] = radius;
        newItem.RealY[0] = 0;
        newItem.RealZ[0] = 0;

        // for each point on the circle
        for(int i=0; i<(points-1) ;i++)
        {
            newItem.RealX[i+1] = ( (newItem.RealX[i] * my_cos) - (newItem.RealZ[i] * my_sin) );
            newItem.RealZ[i+1] = ( (newItem.RealX[i] * my_sin) + (newItem.RealZ[i] * my_cos) );
        }

        if (debug && verboseDebug)
        {
        //    System.out.println("point:" + 0 + " x=" + (int)newItem.RealX[1] + " y=" + (int)newItem.RealY[1] + " z=" + (int)newItem.RealZ[1]);
        } // end if 

        // there is only one poly in a circle object
        newItem.polyColour[0]    = colour;
        newItem.polylist[0][0]    = points;
        newItem.polys = 1;

        // create polylist information for each point on the new poly
        // this is just the index of the RealX[] points
        for(int i=0; i<points; i++)
        {
            newItem.polylist[0][i+1] = i;
        }


        if (debug && verboseDebug)
        {
            System.out.println("Run CircleObject ");
        }

        return(newItem);
    } // end CircleObject
    
    
/**
 * 
 * @param craft
 */
    /*
void initAircraftToRunway1(Craft craft)
// set the start possition of a craft, height above the ground should not be 0 as this would
// un realistic
{
        // start at runway 1
        craft.Roll  = 0.0;     craft.Pitch = -0.034;  craft.Yaw   = 0.0; 
        craft.eyeX  = craft.orgX;  craft.eyeY  = craft.orgY;    craft.eyeZ  = craft.orgZ; 
        craft.Speed = 0.1;
} // end initAircraftToRunway1
*/


/**
 * 
 * @param craft
 */
/*
void initAircraft(Craft craft)
// set the start possition of a craft, height above the ground should not be 0 as this would
// un realistic
{

    craft.eyeX  = 0; // was 0
    craft.eyeY  = 2000;
    craft.eyeZ  = 0;  // was 0
    craft.Speed = 0.1;
    craft.Yaw   = 0;
    craft.Pitch = 0;
    craft.Roll  = 0;
} // end initAircraft
*/


/**
 * set he start possition of a craft, height above the ground should not be 0 as this would unrealistic 
 * @param craft
 */
static void initAircraftNew(Craft craft) {
    craft.eyeX  = craft.orgX;
    craft.eyeY  = craft.orgY;
    craft.eyeZ  = craft.orgZ;
    craft.Speed = 0.1;
    craft.Yaw   = craft.orgYaw;
    craft.Pitch = craft.orgPitch;
    craft.Roll  = craft.orgRoll;
} // end initAircraftNew


/**
 *  set the start point for an object and the direction around the Y that the object is located.
 *   Performs a rotation of the object before Translating to the world co-ordinates
 *    uses ->
 *        WorldItem :    vertices, RealX[], RealY[], RealZ[]
 *     modifies ->
 *         WorldItem :    posx, posy, posz, rotx, preRoty, rotz
 *                 newX[], newY[], newZ[],
 *                 RealX[], RealY[], RealZ[]
 *     
 *      Calls Funtions ->
 *          threeDcross14()
 * 
 * @param anObject
 * @param posx
 * @param posy
 * @param posz
 * @param preRoty
 * @param scale
 */
static void initObject(WorldItem anObject, int posx, int posy, int posz, double preRoty, int scale) {
    double mat1[] = new double[] {0,0,0,1};         // matrix (x,y,z)
    double mat2[] = new double[] {1,0,0,0,          // rotation matrix
                       0,1,0,0,
                       0,0,1,0,
                       0,0,0,1};
                        
    double mat3[] = new double[] {0,0,0,1};         // result matrix
    double angle = preRoty;
    
    anObject.posx = posx;    anObject.posy = posy;    anObject.posz = posz;
    anObject.rotx = 0;    anObject.roty = preRoty;    anObject.rotz = 0;

    anObject.scaleFactor = scale;

    // scale object
//    if (anObject.scaleFactor > 1) {
        // for each point in the object, obtain the homogeneous coordinates.
        // System.out.println("Scaling object by " + anObject.scaleFactor);
        for (int i=0; i<anObject.vertices; i++) {
            anObject.RealX[i] *= anObject.scaleFactor;
            anObject.RealY[i] *= anObject.scaleFactor;
            anObject.RealZ[i] *= anObject.scaleFactor;
        }
//    }
    

    // clear new xyz values
    for (int i=0; i<anObject.vertices; i++)
    {
        anObject.newX[i] = 0;
        anObject.newY[i] = 0;
        anObject.newZ[i] = 0;
    }
        
    // Rotation matrix for rotation about y axis.
    mat2[0]  = Math.cos(angle);    // rotate x points (x,-,-,0)  x
    mat2[2]  = -Math.sin(angle);   // rotate z points (-,-,z,0)  z
    mat2[8]  = Math.sin(angle);    // rotate x points (x,-,-,0)  x
    mat2[10] = Math.cos(angle);    // rotate z points (-,-,z,0)  z

    // for each point in the object, obtain the homogeneous coordinates.
    for (int i=0; i<anObject.vertices; i++) {

        mat1[0] = ( (double)anObject.RealX[i]);                // copy xyz for each point to homogeneous array (x,y,x,1)
        mat1[1] = ( (double)anObject.RealY[i]);
        mat1[2] = ( (double)anObject.RealZ[i]);
        threeDcross14(mat1, mat2, mat3);          // multiply mat1 with mat2 = mat3.
        anObject.RealX[i] = (int) mat3[0];             // copy homogeneous array to new xyz
        anObject.RealY[i] = (int) mat3[1];
        anObject.RealZ[i] = (int) mat3[2];
    }


    // move to real world position
    for (int i=0; i<anObject.vertices; i++)
    {
        anObject.RealX[i] += anObject.posx;
        anObject.RealY[i] += anObject.posy;
        anObject.RealZ[i] += anObject.posz;
    }

//    if(debug)
        System.out.println("Run initObject RealX:" + anObject.RealX[0] + " RealY:" + anObject.RealY[0] + " RealZ:" + anObject.RealZ[0] + " Name: " + anObject.objectName );

    if (debug && verboseDebug) {
        System.out.println("Run initObject");
    }
} // end initObject




/**
 *  make a number larger so that infinity is overted
 * 
 * @param smallNumber
 * @return
 */
double makeBigger(double smallNumber) {

    if (smallNumber >= 0.0)            // positive or 0
    {
        if (smallNumber < 1.0)        // small positive number
            smallNumber = 1.0;
    }
    else                            // negative
    {
        if (smallNumber > -1.0)        // small negative number
            smallNumber = -1.0;
    }

    return(smallNumber);
} // end makeBigger



/**
 * The Sutherland-Hodgman Polygon-Clipping Algorithm goes through each point in the poly and clips
 *  against the screen limits adding new points where required
 * 
 * @param anItem
 * @param craft
 * @param screen
 */
void ClipPolyXmin(WorldItem anItem, Craft craft, Screen screen) {
//
//    anObject.xpoly[i]
//    anObject.ypoly[i]
//    anObject.points
//
// Calls:
//            getIntersectPointScreen()
    
    //final int POLY_POINTS = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    boolean outBounds = false;                // init non-visable
//    boolean turningPoint = false;                    // was a point added for the corner of the screen
    int[] tempx = new int[WorldItem.POLY_POINTS];  
    int[] tempy = new int[WorldItem.POLY_POINTS];
    int vertexCount = 0;                    // count of vertices in new poly
    int clipAxis = 0, clipAxisPrevious;        // leftY, rightY, lowerX, upperX
//    final int Y1min = 1, Y1max = 2, X1min = 4, X1max = 8, Y2min = 16, Y2max = 32, X2min = 64, X2max = 128;    // axis bounds flag
//    Note: Y1=0 y2=max X1=0 X2=max  top left (x,y)=(0,0)


    if (anItem.points > 0)
    {
    // CLIP AGANST Xmin
    // first poly vertex to last-1 vertex
    for (int vertex=0; vertex<(anItem.points-1); vertex++)
    {
        if (vertexCount >= WorldItem.POLY_POINTS)
            System.out.println("ClipPolyXmin reached maximum POLY_POINTS");

        clipAxisPrevious = clipAxis;
        clipAxis = 0;

        // current point is inside Bounds of X min
        if (anItem.xpoly[vertex] >= screen.xmin)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[vertex];
            tempy[vertexCount] = anItem.ypoly[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X min create a new point at the intersection
            if (anItem.xpoly[vertex+1] < screen.xmin)
            {
                clipAxis = X1min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
/*                vertexCount++;                          // increment number of vertices in new poly

                if (turningPoint) {
                    vertexCount++;                          // increment number of vertices for corner point
                }
*/                
            }
        }
        else if (anItem.xpoly[vertex] < screen.xmin)
        {
            // if the X part of the second point is out of bounds X min
            if (anItem.xpoly[vertex+1] >= screen.xmin)
            {
                clipAxis = X2min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
            
    } // end for        


    // The last poly vertex and first poly vertex
        // current point is inside Bounds of X min
        if (anItem.xpoly[anItem.points-1] >= screen.xmin)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[anItem.points-1];
            tempy[vertexCount] = anItem.ypoly[anItem.points-1];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X min
            if (anItem.xpoly[0] < screen.xmin)
            {
                clipAxis = X1min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.xpoly[anItem.points-1] < screen.xmin)
        {
            // if the X part of the second point is out of bounds X min
            if (anItem.xpoly[0] >= screen.xmin)
            {
                clipAxis = X2min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }            
        }


    // copy new points to point arrays
    for (int i=0; i<vertexCount; i++)
    {
        anItem.xpoly[i] = tempx[i];
        anItem.ypoly[i] = tempy[i];
    } // end for

    // new vertex count
    anItem.points = vertexCount;
    } // end (anItem.points > 0)

} // end ClipPolyXmin


////////////////////////////////////////////////////////////////////////////////////


/**
 *  The Sutherland-Hodgman Polygon-Clipping Algorithm goes through each point in the poly and clips
 *   against the screen limits adding new points where required
 * 
 * @param anItem
 * @param craft
 * @param screen
 */
void ClipPolyXmax(WorldItem anItem, Craft craft, Screen screen) {
//
//    anObject.xpoly[i]
//    anObject.ypoly[i]
//    anObject.points
    
    final int POLY_POINTS    = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    boolean outBounds = false;                // init non-visable
//    boolean turningPoint = false;                    // was a point added for the corner of the screen
    int[] tempx = new int[POLY_POINTS];
    int[] tempy = new int[POLY_POINTS];
    int vertexCount = 0;                    // count of vertices in new poly
    int clipAxis = 0, clipAxisPrevious;        // leftY, rightY, lowerX, upperX
//    final int Y1min = 1, Y1max = 2, X1min = 4, X1max = 8, Y2min = 16, Y2max = 32, X2min = 64, X2max = 128;    // axis bounds flag
//    Note: Y1=0 y2=max X1=0 X2=max  top left (x,y)=(0,0)

    if (anItem.points > 0)
    {
    // CLIP AGANST Xmax
    // first poly vertex to last-1 vertex
    for (int vertex=0; vertex<(anItem.points-1); vertex++)
    {
        if (vertexCount >= POLY_POINTS)
            System.out.println("ClipPolyXmax reached maximum POLY_POINTS");

        clipAxisPrevious = clipAxis;
        clipAxis = 0;

        // current point is inside Bounds of X min
        if (anItem.xpoly[vertex] <= screen.xmax)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[vertex];
            tempy[vertexCount] = anItem.ypoly[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X min create a new point at the intersection
            if (anItem.xpoly[vertex+1] > screen.xmax)
            {
                clipAxis = X1max; // was X1max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.xpoly[vertex] > screen.xmax)
        {
            // if the X part of the second point is out of bounds X min
            if (anItem.xpoly[vertex+1] <= screen.xmax)
            {
                clipAxis = X2max; //was X2max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
            
    } // end for        


    // The last poly vertex and first poly vertex
        // current point is inside Bounds of X max
        if (anItem.xpoly[anItem.points-1] <= screen.xmax)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[anItem.points-1];
            tempy[vertexCount] = anItem.ypoly[anItem.points-1];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X max
            if (anItem.xpoly[0] > screen.xmax)
            {
                clipAxis = X1max; // was X1max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.xpoly[anItem.points-1] > screen.xmax)
        {
            // if the X part of the second point is out of bounds X max
            if (anItem.xpoly[0] <= screen.xmax)
            {
                clipAxis = X2max; // was X2max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }            
        }


    // copy new points to point arrays
    for (int i=0; i<vertexCount; i++)
    {
        anItem.xpoly[i] = tempx[i];
        anItem.ypoly[i] = tempy[i];
    } // end for

    // new vertex count
    anItem.points = vertexCount;

    } // end (anItem.points > 0)

} // end ClipPolyXmax




/**
 * The Sutherland-Hodgman Polygon-Clipping Algorithm goes through each point in the poly and clips 
 * against the screen limits adding new points where required
 * 
 * @param anItem
 * @param craft
 * @param screen
 */
void ClipPolyYmin(WorldItem anItem, Craft craft, Screen screen){
//
//    anObject.xpoly[i]
//    anObject.ypoly[i]
//    anObject.points

    
    final int POLY_POINTS    = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    boolean outBounds = false;                // init non-visable
//    boolean turningPoint = false;                    // was a point added for the corner of the screen
    int[] tempx = new int[POLY_POINTS];
    int[] tempy = new int[POLY_POINTS];
    int vertexCount = 0;                    // count of vertices in new poly
    int clipAxis = 0, clipAxisPrevious;        // leftY, rightY, lowerX, upperX
//    final int Y1min = 1, Y1max = 2, X1min = 4, X1max = 8, Y2min = 16, Y2max = 32, X2min = 64, X2max = 128;    // axis bounds flag
//    Note: Y1=0 y2=max X1=0 X2=max  top left (x,y)=(0,0)


    if (anItem.points >= POLY_POINTS)
        System.out.println("ClipPolyYmin reached maximum object points: " + anItem.points);

    if (anItem.points > 0)
    {

    // CLIP AGANST Ymin
    // first poly vertex to last-1 vertex
    for (int vertex=0; vertex<(anItem.points-1); vertex++)
    {
        if (vertexCount >= POLY_POINTS)
            System.out.println("ClipPolyYmin reached maximum POLY_POINTS");
            
        clipAxisPrevious = clipAxis;
        clipAxis = 0;

        // current point is inside Bounds of X min
        if (anItem.ypoly[vertex] >= screen.ymin)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[vertex];
            tempy[vertexCount] = anItem.ypoly[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            // if the Y part of the second point is out of bounds Y min create a new point at the intersection
            if (anItem.ypoly[vertex+1] < screen.ymin)
            {
                // going out of bounds
                clipAxis = Y1min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.ypoly[vertex] < screen.ymin)
        {
            // if the Y part of the second point is out of bounds Y min
            if (anItem.xpoly[vertex+1] >= screen.ymin)
            {
                // comeing into bounds
                clipAxis = Y2min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
            
    } // end for        


    // The last poly vertex and first poly vertex
        // current point is inside Bounds of X min
        if (anItem.ypoly[anItem.points-1] >= screen.ymin)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[anItem.points-1];
            tempy[vertexCount] = anItem.ypoly[anItem.points-1];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X min
            if (anItem.ypoly[0] < screen.ymin)
            {
                // going out of bounds
                clipAxis = Y1min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.ypoly[anItem.points-1] < screen.ymin)
        {
            // if the Y part of the second point is out of bounds Y min
            if (anItem.ypoly[0] >= screen.ymin)
            {
                // comeing into bounds
                clipAxis = Y2min;

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }            
        }


    // copy new points to point arrays
    for (int i=0; i<vertexCount; i++)
    {
        anItem.xpoly[i] = tempx[i];
        anItem.ypoly[i] = tempy[i];
    } // end for

    // new vertex count
    anItem.points = vertexCount;

    } // end (anItem.points > 0)


} // end ClipPolyYmin



/**
 * The Sutherland-Hodgman Polygon-Clipping Algorithm goes through each point in the poly and clips
 *  against the screen limits adding new points where required
 * 
 * @param anItem
 * @param craft
 * @param screen
 */
void ClipPolyYmax(WorldItem anItem, Craft craft, Screen screen) {
//
//    anObject.xpoly[i]
//    anObject.ypoly[i]
//    anObject.points
    
    final int POLY_POINTS    = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    boolean outBounds = false;                // init non-visable
//    boolean turningPoint = false;                    // was a point added for the corner of the screen
    int[] tempx = new int[POLY_POINTS];
    int[] tempy = new int[POLY_POINTS];
    int vertexCount = 0;                    // count of vertices in new poly
    int clipAxis = 0, clipAxisPrevious;        // leftY, rightY, lowerX, upperX
//    final int Y1min = 1, Y1max = 2, X1min = 4, X1max = 8, Y2min = 16, Y2max = 32, X2min = 64, X2max = 128;    // axis bounds flag
//    Note: Y1=0 y2=max X1=0 X2=max  top left (x,y)=(0,0)

    if (anItem.points > 0)
    {

    // CLIP AGANST Xmax
    // first poly vertex to last-1 vertex
    for (int vertex=0; vertex<(anItem.points-1); vertex++)
    {
        if (vertexCount >= POLY_POINTS)
            System.out.println("ClipPolyYmax reached maximum POLY_POINTS");

        clipAxisPrevious = clipAxis;
        clipAxis = 0;

        // current point is inside Bounds of X min
        if (anItem.ypoly[vertex] <= screen.ymax)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[vertex];
            tempy[vertexCount] = anItem.ypoly[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X min create a new point at the intersection
            if (anItem.ypoly[vertex+1] > screen.ymax)
            {
                clipAxis = Y1max; // was Y1max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.ypoly[vertex] > screen.ymax)
        {
            // if the X part of the second point is out of bounds X min
            if (anItem.ypoly[vertex+1] <= screen.ymax)
            {
                clipAxis = Y2max; //was X2max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, vertex, vertex+1, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
            
    } // end for        


    // The last poly vertex and first poly vertex
        // current point is inside Bounds of X max
        if (anItem.ypoly[anItem.points-1] <= screen.ymax)
        {
            // if the first point is inside the bounds copy this point straight into the temp[]
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoly[anItem.points-1];
            tempy[vertexCount] = anItem.ypoly[anItem.points-1];
            vertexCount++;                          // increment number of vertices in new poly

            // if the X part of the second point is out of bounds X max
            if (anItem.xpoly[0] > screen.xmax)
            {
                clipAxis = Y1max; // was Y1max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }
        }
        else if (anItem.ypoly[anItem.points-1] > screen.ymax)
        {
            // if the X part of the second point is out of bounds X max
            if (anItem.ypoly[0] <= screen.ymax)
            {
                clipAxis = Y2max; // was X2max

                // get the new point where the craft is intersected
                vertexCount = getIntersectPointScreen(anItem, craft, anItem.points-1, 0, vertexCount, tempx, tempy, clipAxis, screen);
//                vertexCount++;                          // increment number of vertices in new poly
            }            
        }

        // check for turning point between last and first points
        if (vertexCount > 1)
            vertexCount = checkForTurningPoint(anItem, vertexCount-1, 0, vertexCount, tempx, tempy, screen);


    // copy new points to point arrays
    for (int i=0; i<vertexCount; i++)
    {
        anItem.xpoly[i] = tempx[i];
        anItem.ypoly[i] = tempy[i];
    } // end for

    // new vertex count
    anItem.points = vertexCount;

    } // end (anItem.points > 0)

} // end ClipPolyYmax


////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////
// CHECKED ABOVE THIS POINT
////////////////////////////////////////////////////////////////////////////////////


// clip a poly against the screen craft
// uses ->
//    WorldItem :    polylist[polyToClip][0]
//            zpoints[vertex]
// modifies ->
//    WorldItem :
//            posx, posy, posz, rotx, preRoty, rotz
//            newX[], newY[], newZ[],
//            RealX[], RealY[], RealZ[]
//
// Calls Funtions ->
//    getIntersectPoint()
boolean ClipPoly3D(WorldItem anItem, double ClipPoint, int polyToClip) {
    final int POLY_POINTS    = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    boolean visable = false;    // init non-visable
    // int i;
    // int vertex;
    double tempx[] = new double[POLY_POINTS];
    double tempy[] = new double[POLY_POINTS];
    double tempz[] = new double[POLY_POINTS];
    int vertexCount = 0;                   // count of vertices in new poly
//                    0????

    // first to last-1 vertices (each point except the last, this is done later)
    for (int vertex=0; vertex<(anItem.polylist[polyToClip][0])-1; vertex++)
    {
        // if the two points do not intersect the plane copy the point straight into the temp[]
        if ((anItem.zpoints[vertex] >= ClipPoint) && (anItem.zpoints[vertex+1] >= ClipPoint))
        {
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoints[vertex];
            tempy[vertexCount] = anItem.ypoints[vertex];
            tempz[vertexCount] = anItem.zpoints[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            visable = true;    // one point is visable
            
        } // end if
        // if the second point is behind the plane of view, copy the first point into temp[]
        // and create a point where the plane is intersected and copy to temp[]
        else if ((anItem.zpoints[vertex] >= ClipPoint) && (anItem.zpoints[vertex+1] < ClipPoint))
        {
            // copy existing point to temp point array
            tempx[vertexCount] = anItem.xpoints[vertex];
            tempy[vertexCount] = anItem.ypoints[vertex];
            tempz[vertexCount] = anItem.zpoints[vertex];
            vertexCount++;                          // increment number of vertices in new poly

            /* get the new point where the plane is intersected */
            getIntersectPoint(anItem, vertex, vertex+1, vertexCount, tempx, tempy, tempz, ClipPoint);
//            getIntersectPoint(anItem, vertex, vertex+1,    tempx[vertexCount], tempy[vertexCount], tempz[vertexCount], ClipPoint);
//            getIntersectPoint(xpoints[vertex], ypoints[vertex], zpoints[vertex], xpoints[vertex+1], ypoints[vertex+1], zpoints[vertex+1], &tempx[vertexCount], &tempy[vertexCount], &tempz[vertexCount], ClipPoint);
            vertexCount++;                          // increment number of vertices in new poly

            visable = true;    // one point is visable
            
        } // end else if
        // if the first point is behind the plane of view, create a point where the
        // plane is intersected and copy to temp[]
        // ignoring the first point and saving the second point for the next pass
        else if ((anItem.zpoints[vertex] < ClipPoint) && (anItem.zpoints[vertex+1] >= ClipPoint))
        {
            // create new point at the intersection of the z plane
            getIntersectPoint(anItem, vertex, vertex+1, vertexCount, tempx, tempy, tempz, ClipPoint);
//            getIntersectPoint(xpoints[vertex], ypoints[vertex], zpoints[vertex], xpoints[vertex+1], ypoints[vertex+1], zpoints[vertex+1], &tempx[vertexCount], &tempy[vertexCount], &tempz[vertexCount], ClipPoint);
            vertexCount++;                          // increment number of vertices in new poly

            visable = true;    // one point is visable

        } // end else if
    } // end for


    // last point and first point
    if ((anItem.zpoints[(anItem.polylist[polyToClip][0]-1)] >= ClipPoint) && (anItem.zpoints[0] >= ClipPoint))
    {
        // copy existing point to temp point array
        tempx[vertexCount] = anItem.xpoints[(anItem.polylist[polyToClip][0]-1)];
        tempy[vertexCount] = anItem.ypoints[(anItem.polylist[polyToClip][0]-1)];
        tempz[vertexCount] = anItem.zpoints[(anItem.polylist[polyToClip][0]-1)];
        vertexCount++;        // increment number of vertices in new poly

        visable = true;    // one point is visable
        
    } // end if
    else if (( anItem.zpoints[(anItem.polylist[polyToClip][0]-1)] >= ClipPoint) && (anItem.zpoints[0] < ClipPoint))
    {
        // copy existing point to temp point array
        tempx[vertexCount] = anItem.xpoints[(anItem.polylist[polyToClip][0]-1)];
        tempy[vertexCount] = anItem.ypoints[(anItem.polylist[polyToClip][0]-1)];
        tempz[vertexCount] = anItem.zpoints[(anItem.polylist[polyToClip][0]-1)];
        vertexCount++;                          // increment number of vertices in new poly

        // create new point at the intersection of the z plane
        getIntersectPoint(anItem, anItem.polylist[polyToClip][0]-1, 0, vertexCount, tempx, tempy, tempz, ClipPoint);
        vertexCount++;                          // increment number of vertices in new poly

        visable = true;    // one point is visable
        
    } // end else if
    
    else if ( (anItem.zpoints[(anItem.polylist[polyToClip][0]-1)] < ClipPoint) && (anItem.zpoints[0] >= ClipPoint))
    {
        // create new point at the intersection of the z plane
        getIntersectPoint(anItem, anItem.polylist[polyToClip][0]-1, 0, vertexCount, tempx, tempy, tempz, ClipPoint);
        vertexCount++;                          // increment number of vertices in new poly

        visable = true;    // one point is visable
        
    } // end else if


    // copy new points to point arrays
    if (visable)
    {
        for (int i=0; i<vertexCount; i++)
        {
            // SHOULD THESE BE MODIFIED? some points are shared with other polys
            if (anItem.updated[i] != true) {
                anItem.xpoints[i] = tempx[i];
                anItem.ypoints[i] = tempy[i];
                anItem.zpoints[i] = tempz[i];

                // new vertex count
                anItem.points = vertexCount;
                
                // don't let this point get updated again
                anItem.updated[i] = true;
            }
//System.out.println("Run ClipPoly3D points=" + anItem.points);
            
        } // end for
    } // end if

    return(visable);        // is visable or not

} // end ClipPoly3D


////////////////////////////////////////////////////////////////////////////////////


// clip the object if one or more points are outside the Z clip plane
boolean basicClipPoly3D(WorldItem anItem, double ClipPoint, int polyToClip) {
    boolean visable = true;    // init non-visable

    // first to last-1 vertices (each point except the last, this is done later)
    for (int vertex=0; vertex<(anItem.polylist[polyToClip][0]); vertex++)
    {
        // if the two points do not intersect the plane copy the point straight into the temp[]
        if ((anItem.zpoints[vertex] < ClipPoint))
        {
            visable = false;    // one point is visable
        } // end if
    } // end for

    return(visable);        // is visable or not

} // end basicClipPoly3D

/**
 * Calculate the Haversine distance between two GPS coordinates.
 * All the input latitude and longitudes are in degrees.
 *
 * This method is based on: {@link http://www.movable-type.co.uk/scripts/latlong.html}
 *
 * See {@link http://reference.wolfram.com/mathematica/ref/Haversine.html} for more details.
 *
 * @param lat1 Latitude of point 1.
 * @param lon1 Longitude of point 1.
 * @param lat2 Latitude of point 2.
 * @param lon2 Longitude of point 2.
 * @param radius Radius of the sphere (earth) in metres
 * @return The distance between point 1 and point 2.
 */
public static double haversineDistance(double lat1, double lon1, double lat2, double lon2, double radius) {
    // Convert to radians
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    lat1 = Math.toRadians(lat1);
    lon1 = Math.toRadians(lon1);
    lat2 = Math.toRadians(lat2);
    lon2 = Math.toRadians(lon2);

    double a = Math.pow(Math.sin(dLat/2),2)+Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(dLon/2),2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); // y,x
    return radius * c;
}



////////////////////////////////////////////////////////////////////////////////////
    /*
    ** modifies the xpoints[i] ypoints[i] zpoints[i] arrays
    */    
    void WorldPerspective(WorldItem anObject, Craft viewer, int polyToPersp, boolean flip){
        //Dimension d = getSize();
//      boolean debug = false;
//      debug = true;            // debug on
    
        // perform perspective transformation for all points on given the object.
        for (int i=0; i<anObject.polylist[polyToPersp][0]; i++)
        {
            if ((anObject.zpoints[i] < 1))
            {
                anObject.zpoints[i] = 1;
            }

            // perform perspective
            anObject.xtemp[i] =    ((FOCAL_DISTANCE * anObject.xpoints[i]) / anObject.zpoints[i] + 160);
            if (flip)                                    // was 100 below
                anObject.ytemp[i] = viewer.windowHeight -    (100 - ((FOCAL_DISTANCE * anObject.ypoints[i]) / anObject.zpoints[i]));
            else
                anObject.ytemp[i] =                            (100 - ((FOCAL_DISTANCE * anObject.ypoints[i]) / anObject.zpoints[i]));

            if (verboseDebug)
                System.out.println("flip with = " + (viewer.windowHeight -    (100 - ((FOCAL_DISTANCE * anObject.ypoints[i]) / anObject.zpoints[i]))) + " without = " + (100 - ((FOCAL_DISTANCE * anObject.ypoints[i]) / anObject.zpoints[i])));

        } // end for

        //System.out.println("screenHeight = " + viewer.windowHeight);
    } // end WorldPerspective



////////////////////////////////////////////////////////////////////////////////////
// draws the given object to the screen
// This function Rotates the object into world orientation using RotateObject
// and sorts the object using the SortObject function, The object is Cliped against the
// Z plane using ClipPoly3D, Each poly in the object is clipped against the screen using
// ClipPoly2D
// uses ->
//    WorldItem :    sorted_list[], poly[], newX[], newY[], newZ[]
// modifies ->
//    WorldItem :    
//
// Calls Funtions ->
//    RotateObjectXYZ(), SortPolys(), ClipPoly3D(), ClipPoly2D(), WorldPerspective()
void drawObject(Graphics g, WorldItem anObject, Craft viewer, boolean flip) {
    //final int POLYS        = 300;  // max number of polygons per object
    //final int POLY_POINTS    = 200;  // max number of points for each polygon    Polygon polygonStorage[] = new Polygon[POLYS];
    final double ZClipPoint = 50.0;  // z distance to clip against.    WAS 50.0

    int poly;
    int points;
//    double xpoints[] = new double[POLY_POINTS];
//    double ypoints[] = new double[POLY_POINTS];
//    double zpoints[] = new double[POLY_POINTS];
//    int xpoly[] = new int[POLY_POINTS];
//    int ypoly[] = new int[POLY_POINTS];
    //int polycount, verts;
    boolean visible = false;
//    int i;
    Polygon clipMap[] = new Polygon[WorldItem.POLYS];
    Polygon tempPoly = new Polygon();
//    boolean debug = true;


    if (debug && verboseDebug)
        System.out.println("Start DrawObject");

    // translate and rotate objects into player view
    RotateObjectXYZ(anObject, viewer);

    // sort poly list by z distance
    SortPolys(anObject, viewer);        // resort polys using new viewer coords


    // for each poly in the object structure
    for (int polycount=0; polycount<anObject.polys; polycount++)
    {
        poly = anObject.sorted_list[polycount];        // draw poly in Z order
        points = anObject.polylist[poly][0];

        // for each vertex in the current poly
        for (int verts=1; verts<=points; verts++)
        {
            anObject.xpoints[verts-1] = anObject.newX[anObject.polylist[poly][verts]];
            if (flip == true)
                anObject.ypoints[verts-1] = viewer.windowHeight - anObject.newY[anObject.polylist[poly][verts]];
            else
                anObject.ypoints[verts-1] =                     anObject.newY[anObject.polylist[poly][verts]];


            anObject.zpoints[verts-1] = anObject.newZ[anObject.polylist[poly][verts]];
            anObject.updated[verts-1] = false;
        } // end for

        // Clip polygons against Z plane
        if (clip)
        {
            // if the object is not hidden
            if (anObject.hiddenObject != true)
            {
                // clip against the Z front plane
                visible = ClipPoly3D(anObject, ZClipPoint, poly);
            }
        }
        else
        {
            // hide polys that intersect the Z front plane
            visible = basicClipPoly3D(anObject, ZClipPoint, poly);
            //visible = true;        // draw everything
        }


        // Draw 2D Clip Map
        if (displayClipMap)
        {
            // draw line at clip plane
            g.setColor(Color.pink);
            g.drawLine(130, 14, 148, 14);
            g.drawLine(152, 14, 170, 14);

            if (visible)
            {

                // for each vertex in the poly
                for (int i=0; i<anObject.points; i++)
                {                    // was 70            // was 200
                    anObject.xpoly[i] = (int)Math.round(150 + (anObject.xpoints[i] / 100) + 0.5);
                    anObject.ypoly[i] = (int)Math.round(15 + (anObject.zpoints[i] / 100) + 0.5);
                    
                } // end for
                clipMap[poly] = new Polygon(anObject.xpoly, anObject.ypoly, anObject.points);

                // set poly colour
                g.setColor(anObject.polyColour[poly]);

                // test Clipping in 2d
                g.drawPolygon(clipMap[poly]);
                
            } // end if
        } // end if (Draw 2D Clip Map)


        // if the poly is visable display poly
        if (visible)
        {
            // set poly colour
            g.setColor(anObject.polyColour[poly]);

            // perspective bit goes here. Convert 3D into 2D with perspective
            if ( perspective == true )
                WorldPerspective(anObject, viewer, poly, flipped);
            



            // convert arrays to Polygon
            // for each vertex in the poly
            for (int i=0; i<anObject.points; i++)
            {
                anObject.xpoly[i] = (int)Math.round(anObject.xtemp[i] + 0.5);
                anObject.ypoly[i] = (int)Math.round(anObject.ytemp[i] + 0.5);
            } // end for


            // clip the poly against the rectangular screen limits
            if (clipScreen == true)
            {

                if ( (graphDeanFly.globalDebug.compareTo("true") == 0) && (debug) )
                {
                    System.out.println("pre  ClipPoly2d points = " + anObject.points);
                    for (int i=0; i<anObject.points; i++)
                    {
                        System.out.println("pre   point:" + i + "of" + anObject.points + " x=" + (int)anObject.xpoly[i] + " y=" + (int)anObject.ypoly[i]);
                    }
                }

                ClipPolyXmin(anObject, viewer, screen);
                ClipPolyYmin(anObject, viewer, screen);
                ClipPolyXmax(anObject, viewer, screen);
                ClipPolyYmax(anObject, viewer, screen);

                if ( (graphDeanFly.globalDebug.compareTo("true") == 0) && (debug) )
                {
                    System.out.println("post ClipPoly2d points = " + anObject.points);

                    for (int i=0; i<anObject.points; i++)
                    {
                        System.out.println("post  point:" + i + "of" + anObject.points + " x=" + (int)anObject.xpoly[i] + " y=" + (int)anObject.ypoly[i]);
                    }
                }
            }



            if (anObject.points > 0 ) {
                tempPoly = new Polygon(anObject.xpoly, anObject.ypoly, anObject.points);

                g.setColor(anObject.polyColour[poly]);

                // draw polygon
                if (wireframe)
                    g.drawPolygon(tempPoly);
                else
                    g.fillPolygon(tempPoly);
            } // end if visible

            } // end if (anObject.points > 0)

        if (instumentDisplay)
            whereAmI(g, viewer);

        if (keyHelpDisplay)
            keyHelp(g, viewer);

    } // end for


    // debug
    if (debug && verboseDebug)
        System.out.println("End DrawObject");

} // end drawObject


////////////////////////////////////////////////////////////////////////////////////

    
    public void quitBox(Graphics g, WorldItem o, FontMetrics fm) {
        int width;
        String testString = new String("Quit: q/Q");
        int w = fm.stringWidth(testString);
        int h = fm.getHeight();

        if (flipped == true)
            g.setColor(Color.blue);
        else
            g.setColor(Color.red);

        g.fillRect(750, 10, w, h);        // filled rectangle
        g.setColor(Color.black);
        g.drawRect(750, 10, w, h);        // outline
        
        g.drawString(testString, 750, 10 + fm.getAscent());    // text
        
    } // end quitBox


////////////////////////////////////////////////////////////////////////////////////


    public static void displaySplash(Graphics g, Craft craft) {
        String stringBox[] = new String[10];

        stringBox[0] = new String("Keys:");
        stringBox[1] = new String(" 'S' to start");
        stringBox[2] = new String(" 'Q' to quit (at any time)");

        if(g != null)
            displayCentreBox(g, craft, stringBox, 3, Color.green, offgraphics.getFontMetrics());
        else
            System.out.println("displaySplash attempt to displayCentreBox when Graphics g is null");
        
    } // end displaySplash


////////////////////////////////////////////////////////////////////////////////////


    public void displaySplashContinue(Graphics g, Craft craft) {
        String stringBox[] = new String[10];

        stringBox[0] = new String("Keys:");
        stringBox[1] = new String(" 'S' to start");
        stringBox[2] = new String(" 'C' to continue");
        stringBox[3] = new String(" 'Q' to quit (at any time)");

        if(g != null)
            displayCentreBox(g, craft, stringBox, 4, Color.green, offgraphics.getFontMetrics());
        else
            System.out.println("displaySplashContinue attempt to displayCentreBox when Graphics g is null");
        
    } // end displaySplashContinue


////////////////////////////////////////////////////////////////////////////////////


    public void displayExit(Graphics g, Craft craft) {
        String stringBox[] = new String[10];

        stringBox[0] = new String(graphDeanFly.worldName);
        stringBox[1] = new String("Applet ended");
        stringBox[2] = new String("You will need to hit the 'Refresh' button to play again");

        if(g != null)
            displayCentreBox(g, craft, stringBox, 3, Color.green, offgraphics.getFontMetrics());
        else
            System.out.println("displayExit attempt to displayCentreBox when Graphics g is null");
    } // end displayExit


////////////////////////////////////////////////////////////////////////////////////


    public void displayCrash(Graphics g, Craft craft) {
        String stringBox[] = new String[10];

        stringBox[0] = new String("You Crashed!");
        stringBox[1] = new String("");
        stringBox[2] = new String("Better luck next time");
        stringBox[3] = new String(" 'S' to start");
        stringBox[4] = new String(" 'Q' to quit (at any time)");

        if(g != null)
            displayCentreBox(g, craft, stringBox, 5, Color.red, offgraphics.getFontMetrics());
        else
            System.out.println("displayCrash attempt to displayCentreBox when Graphics g is null");
    } // end displayCrash


////////////////////////////////////////////////////////////////////////////////////


    public static void displayCentreBox(Graphics g, Craft craft, String stringBox[], int lines, Color boxColour, FontMetrics fm) {

        if(g!=null)
        {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            crafts[currentCraft].windowHeight = d.height / GraphPnl.SCREEN_SCALER;        // screen size height
            crafts[currentCraft].windowWidth  = d.width / GraphPnl.SCREEN_SCALER;        // screen size width
            
            craft.windowHeight = d.height / GraphPnl.SCREEN_SCALER;        // screen size height
            craft.windowWidth  = d.width / GraphPnl.SCREEN_SCALER;        // screen size width
            
            int centreHeight = craft.windowHeight / 2;            // screen size height
            int centreWidth  = craft.windowWidth  / 2;            // screen size width

            g.setFont(screenFont);
            int w = fm.stringWidth("DEFAULT LENGTH");
            int h = fm.getHeight();
    
            for (int eachLine=0; eachLine<lines; eachLine++)
            {
                if ( w < fm.stringWidth(stringBox[eachLine]) )
                    w = fm.stringWidth(stringBox[eachLine]);
            }
            h = (fm.getHeight() * lines);
    
            g.setColor(boxColour);
            g.fillRect( (centreWidth - (w/2)), (centreHeight - (h/2)), w, h);        // filled rectangle
            g.setColor(Color.black);
            g.drawRect( (centreWidth - (w/2)), (centreHeight - (h/2)), w, h);        // outline
            
            for (int eachLine=0; eachLine<lines; eachLine++)
            {
                g.drawString(stringBox[eachLine], (centreWidth - (w/2)), (centreHeight - (h/2)) + fm.getAscent() + (fm.getHeight() * eachLine));    // text
            }
            g.setFont(screenFont);
        }
        else
            if (debug && verboseDebug)
                System.out.println("displayCentreBox attempt to use Graphics object g when null");
            
        
    } // end displayCentreBox


////////////////////////////////////////////////////////////////////////////////////

        // Keyboard isFocusable implements KeyListener 
    public void keyPressed(KeyEvent evt) {
            setFocusable(true);
            debug = false;
        if(debug)
            System.out.println("keyPressed - Begin");

        // Quit
        if ((evt.getKeyChar() == 'Q') || (evt.getKeyChar() == 'q')) {
            if (quit == true)
                exit = true;
            else
            {
                quit = true;
                continueAllowed = true;
            }
        }

        // Start or Continue
        if ( quit == true ) {
            if ((evt.getKeyChar() == 'S') || (evt.getKeyChar() == 's')) {
                if(debug)
                    System.out.println("keyPressed - 'S' pressed");
                
                // RESET CRAFT START POSITION
                initAircraftNew(crafts[currentCraft]);
                
                quit = false;
                continueAllowed = false;
                crash = false;
                demoMode = false;

            }
            // Continue
            if ( (evt.getKeyChar() == 'C') || (evt.getKeyChar() == 'c') ) {
                quit = false;
                continueAllowed = true;
                demoMode = false;
            }
        }
        else // if ( quit == false ) 
        {
            if (evt.getKeyChar() == 'c') clip = !clip; // 'c' clipping on/off
            if (evt.getKeyChar() == 'C') clipScreen = !clipScreen; // 'C' clipping on/off

            if (crafts[currentCraft] != null)
            {
    
                if (evt.getKeyChar() == '+' || evt.getKeyChar() == '=') {
                    crafts[currentCraft].Speed += 5.0;         // '+' = +ve 20 knots
                    if (crafts[currentCraft].Speed > 700.1)
                        crafts[currentCraft].Speed = 700.1;
                }
                
                if (evt.getKeyChar() == '-' || evt.getKeyChar() == '_') {
                    crafts[currentCraft].Speed -= 5.0;         // '-' = -ve 20 knots
                    if (crafts[currentCraft].Speed < 0)
                        crafts[currentCraft].Speed = 0.1;
                }
    
                // ASDW classic game controls

                // Walker Controls
                if (crafts[currentCraft].type.compareTo("Walker") == 0)
                {
                    // step back
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD2 || evt.getKeyChar() == 'w') { // Pitch -
    //                    crafts[currentCraft].Pitch -= DEGREE_CHANGE*5;    // '2' = +ve 5 degrees
    //                    if (crafts[currentCraft].Pitch < 0)             // under-flow one revalution 
    //                        crafts[currentCraft].Pitch += 2*PI;
                        crafts[currentCraft].eyeX -= 10 * Math.sin(-crafts[currentCraft].Yaw);
                        crafts[currentCraft].eyeZ -= 10 * Math.cos(crafts[currentCraft].Yaw);
                    }
    
                    // step forward
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD8 || evt.getKeyChar() == 's') { // Pitch +
    //                    crafts[currentCraft].Pitch += DEGREE_CHANGE*5;    // '8' = -ve 5 degrees
    //                    if (crafts[currentCraft].Pitch > (2*PI))        // over-flow one revalution 
    //                        crafts[currentCraft].Pitch -= 2*PI;
                        // crafts[currentCraft].eyeZ += 20 crafts[currentCraft].Yaw
                        crafts[currentCraft].eyeX += 10 * Math.sin(-crafts[currentCraft].Yaw);
                        crafts[currentCraft].eyeZ += 10 * Math.cos(crafts[currentCraft].Yaw);
    //                crafts[currentCraft].eyeY += distance * Math.sin(-crafts[currentCraft].Pitch);
                    }
    
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD4 || evt.getKeyChar() == 'a') {    // Roll +
                        crafts[currentCraft].Yaw  += DEGREE_CHANGE*2;    // '4' = +ve 5 degrees
                        if (crafts[currentCraft].Yaw > (2*PI))         // over-flow one revalution 
                            crafts[currentCraft].Yaw -= 2*PI;
                    }
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD6 || evt.getKeyChar() == 'd') { // Roll -
                        crafts[currentCraft].Yaw  -= DEGREE_CHANGE*2;    // '6' = -ve 5 degrees
                        if (crafts[currentCraft].Yaw < 0)              // under-flow one revalution 
                            crafts[currentCraft].Yaw += 2*PI;
                    }
    
    /*
                    if (evt.getKeyChar() == KeyEvent.VK_NUMPAD3) {
                        crafts[currentCraft].Yaw   -= DEGREE_CHANGE;    // '3' = +ve 5 degrees
                        if (crafts[currentCraft].Yaw > (2*PI))          // over-flow one revalution 
                            crafts[currentCraft].Yaw += 2*PI;
                    }
    
                    if (evt.getKeyChar() == KeyEvent.VK_NUMPAD1) {
                        crafts[currentCraft].Yaw  += DEGREE_CHANGE;    // '1' = -ve 5 degrees
                        if (crafts[currentCraft].Yaw < 0)               // under-flow one revalution 
                            crafts[currentCraft].Yaw -= 2*PI;
                    }
    */
    
                } // end if Walker
    
                // AirCraft Controls
                if (crafts[currentCraft].type.compareTo("AirCraft") == 0)
                {
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD2 || evt.getKeyChar() == 's') { // Pitch -
                        crafts[currentCraft].Pitch -= DEGREE_CHANGE;    // '2' = +ve 5 degrees
                        if (crafts[currentCraft].Pitch < 0)             // under-flow one revalution 
                            crafts[currentCraft].Pitch += 2*PI;
                    }
    
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD8 || evt.getKeyChar() == 'w') { // Pitch +
                        crafts[currentCraft].Pitch += DEGREE_CHANGE;    // '8' = -ve 5 degrees
                        if (crafts[currentCraft].Pitch > (2*PI))        // over-flow one revalution 
                            crafts[currentCraft].Pitch -= 2*PI;       
                    }
    
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD4 || evt.getKeyChar() == 'a') { // Roll -
                        crafts[currentCraft].Roll  -= DEGREE_CHANGE;    // '4' = -ve 5 degrees
                        if (crafts[currentCraft].Roll < 0)              // under-flow one revalution 
                            crafts[currentCraft].Roll += 2*PI;
                    }
    
                    if (evt.getKeyCode() == KeyEvent.VK_NUMPAD6 || evt.getKeyChar() == 'd') {    // Roll +
                        crafts[currentCraft].Roll  += DEGREE_CHANGE;    // '6' = +ve 5 degrees
                        if (crafts[currentCraft].Roll > (2*PI))         // over-flow one revalution 
                            crafts[currentCraft].Roll -= 2*PI;
                    }
    /*
                    if (evt.getKeyChar() == KeyEvent.VK_NUMPAD3) {
                        crafts[currentCraft].Yaw   -= DEGREE_CHANGE;    // '3' = +ve 5 degrees
                        if (crafts[currentCraft].Yaw > (2*PI))          // over-flow one revalution 
                            crafts[currentCraft].Yaw += 2*PI;
                    }
                    if (evt.getKeyChar() == KeyEvent.VK_NUMPAD1) {
                        crafts[currentCraft].Yaw  += DEGREE_CHANGE;    // '1' = -ve 5 degrees
                        if (crafts[currentCraft].Yaw < 0)               // under-flow one revalution 
                            crafts[currentCraft].Yaw -= 2*PI;
                    }
    */
                } // end if AirCraft

                // when the speed is low any roll will results in a flat spin
                if (crafts[currentCraft].Speed < 10.0) crafts[currentCraft].Roll = 0; //  Math.min()

                // go to runway 1
                if (evt.getKeyChar() == '[') initAircraftNew(crafts[currentCraft]);

                // go to runway 2
                if (evt.getKeyChar() == ']') swapToNextVehicle(); // initAircraftNew(crafts[currentCraft]);
                
            }
        } // end if (quit == false)


        // Keys that can be used at any time
        if (evt.getKeyChar() == 'f') flipped = !flipped;
        if (evt.getKeyChar() == 'p') perspective = !perspective;
        
        if (evt.getKeyChar() == 'i') {
            keyHelpDisplay = false;
            instumentDisplay = !instumentDisplay;
        }

        if ((evt.getKeyChar() == 'K') || (evt.getKeyChar() == 'k')) {
            instumentDisplay = false;
            keyHelpDisplay = !keyHelpDisplay;
        }

        // hidden surface removal
        if ((evt.getKeyChar() == 'H') || (evt.getKeyChar() == 'h'))  hiddenSurfaces = !hiddenSurfaces;

        // Teleport go to next craft or vehicle
        if ((evt.getKeyChar() == 'v') || (evt.getKeyChar() == 'V')) swapToNextVehicle();

        // go to somewhere high above the islands
        if (evt.getKeyChar() == '.') {
            crafts[currentCraft].Speed =200.1;          // move to somewhere in flight
            crafts[currentCraft].Roll  = 0.0;     crafts[currentCraft].Pitch = 45.034;  crafts[currentCraft].Yaw   = 2.356;
            crafts[currentCraft].eyeX  = 4500.0;  crafts[currentCraft].eyeY  = 10000.0;    crafts[currentCraft].eyeZ  = 13500.0;
        }

        if (evt.getKeyChar() == 'm') {
            displayClipMap = false;  // turn off clip map before displaying the 2D map
            mapOn =!mapOn;        // 'm' 2D map on/off
        }
        if (evt.getKeyChar() == 'M') {
            mapOn = false; // turn off clip map before displaying the 2D map
            displayClipMap = !displayClipMap; // 'M' 2D Clipping map on/off
        }

        if (evt.getKeyChar() == 'b') wireframe = !wireframe; // 'w' wireframe polys on/off

        // cheat keys
        if (evt.getKeyChar() == 'N') crafts[currentCraft].eyeZ += 20.0;    // 'N' increase z direction by 100 meters
        if (evt.getKeyChar() == 'S') crafts[currentCraft].eyeZ -= 20.0;    // 'S' decrease z direction by 100 meters
        if (evt.getKeyChar() == 'E') crafts[currentCraft].eyeX += 20.0;    // 'E' decrease x direction by 100 meters
        if (evt.getKeyChar() == 'W') crafts[currentCraft].eyeX -= 20.0;    // 'W' increase x direction by 100 meters
        if (evt.getKeyChar() == '(') crafts[currentCraft].eyeY -= 20.0;    // '(' decrease height by 100 meters
        if (evt.getKeyChar() == ')') crafts[currentCraft].eyeY += 20.0;    // ')' increase height by 100 meters

        if (evt.getKeyChar() == 'i') Instuments = !Instuments;    // 'i' instuments on/off
        if (evt.getKeyChar() == 'I') Dials = !Dials;// 'I' instuments on/off

        if(debug)
            System.out.println("keyPressed - Exit");
    }

    public void swapToNextVehicle()
    {
        // 
        if (currentCraft < (availableCrafts - 1))
        {
            currentCraft++;
        }
        else
        {
            currentCraft = 0;
        }
    }


////////////////////////////////////////////////////////////////////////////////////

    public void keyHelp(Graphics g, Craft viewer) {
        g.setColor(Color.black);

        // Quit
        g.drawString("Quit", 400,10);            g.drawString(": Q/q",460,10);    // text
        
        if (instumentDisplay)
            g.setColor(Color.red);
        // Instruments
        g.drawString("Instumentation",600,10);    g.drawString(": i",660,10);        // text
        g.setColor(Color.black);
        
        // Pitch
        g.drawString("Pitch",400,20);            g.drawString(": 2/8",460,20);    // text

        // Yaw
        g.drawString("Yaw",600,20);                g.drawString(": 1/3",660,20);    // text

        // Roll
        g.drawString("Roll",400,30);            g.drawString(": 4/6",460,30);    // text

        // Altitude
        g.drawString("Runway 1/2",600,30);        g.drawString(": [/]",660,30);    // text

        //  Speed
        g.drawString("Speed", 400,40);            g.drawString(": +/-", 460,40);    // text

        if (flipped)
            g.setColor(Color.red);
        //  Flip
        g.drawString("Flip", 600,40);            g.drawString(": f", 660,40);    // text
        g.setColor(Color.black);

        // Altitude
        g.drawString("Altitude", 400,50);        g.drawString(": (/)", 460,50);    // text

        if (perspective)
            g.setColor(Color.red);
        //  Perspective
        g.drawString("Perspective", 600,50);    g.drawString(": p", 660,50);        // text
        g.setColor(Color.black);

        if (mapOn)
            g.setColor(Color.red);
        //  2D Map on/off
        g.drawString("2D Map", 400,60);            g.drawString(": m", 460,60);        // text
        g.setColor(Color.black);

        if (displayClipMap)
            g.setColor(Color.red);
        //  Clipping Map
        g.drawString("Clipping Map", 600,60);    g.drawString(": M", 660,60);        // text
        g.setColor(Color.black);

        if (wireframe)
            g.setColor(Color.red);
        //  This Help Menu
        g.drawString("Wireframe", 400,70);        g.drawString(": w", 460,70);    // text
        g.setColor(Color.black);

        if (!clip)
            g.setColor(Color.red);
        //  This Help Menu
        g.drawString("Z Clipping", 600,70);        g.drawString(": c", 660,70);    // text
        g.setColor(Color.black);

        if (!clipScreen)
            g.setColor(Color.red);
        //  This Help Menu
        g.drawString("Screen Clip", 400,80);        g.drawString(": C", 460,80);    // text
        g.setColor(Color.black);

        //  Scroll through craft
        g.drawString("Scroll craft", 600,80);        g.drawString(":v", 660,80);    // text
        g.setColor(Color.black);

        if (!hiddenSurfaces)
            g.setColor(Color.red);
        //  This Help Menu
        g.drawString("Surface removal", 400,90);        g.drawString(": H", 460,90);    // text
        g.setColor(Color.black);

        if (keyHelpDisplay)
            g.setColor(Color.red);
        //  This Help Menu
        g.drawString("This menu", 600,90);        g.drawString(": H/h/K/k", 660,90);    // text
        g.setColor(Color.black);

    } // end keyHelp


////////////////////////////////////////////////////////////////////////////////////

    public void whereAmI(Graphics g, Craft viewer) {
        g.setColor(Color.black);
        
        // Pitch
        g.drawString(("Pitch   : ".concat(Integer.toString((int) (viewer.Pitch*(180/PI))))), 400,10);    // text

        // Yaw
        g.drawString(("Yaw     : ".concat(Integer.toString((int) (viewer.Yaw*(180/PI))))), 600,10);    // text

        // Roll
        g.drawString(("Roll    : ".concat(Integer.toString((int) (viewer.Roll*(180/PI))))), 400,20);    // text

        // Altitude
        g.drawString(("Altitude: ".concat(Integer.toString((int) viewer.eyeY))), 600,20);    // text

        //  Distance from Base
        g.drawString(("Base    : ".concat(Integer.toString((int) viewer.eyeX))), 400,30);    // text

        //  Distance from Base
        g.drawString(("Distance: ".concat(Integer.toString((int) viewer.eyeZ))), 600,30);    // text

        //  Speed
        g.drawString(("Speed   : ".concat(Integer.toString((int) viewer.Speed))), 400,40);    // text

        // Craft selection
        g.drawString(("Craft   : ".concat(Integer.toString((int) (currentCraft+1))).concat(" of ").concat(Integer.toString((int) availableCrafts)).concat(" (").concat(crafts[currentCraft].type).concat(")") ), 600,40);    // text
//currentCraft < (availableCrafts 
        //  Flip
        g.drawString(("Window  : ".concat(Integer.toString((int) viewer.windowHeight))), 400,50);    // text/
    } // end whereAmI


////////////////////////////////////////////////////////////////////////////////////


    // display all message arrows for a given Action
    public void paintArrowHead(Graphics g, int startx, int starty, double angle, Color headColour) {
        int topX, topY, leftX, leftY, rightX, rightY;
        Polygon arrowHead = new Polygon();
        int height = 20, width = 12;

        topX = startx;
        topY = starty;

        // rotate arrow head about topX,topY by the angle parameter

        double modulus = 26.0;
        double angleSpread = 10.0;

        leftX  = (int) XfromPolar(modulus, angle-angleSpread);
        leftY  = (int) YfromPolar(modulus, angle-angleSpread);
        rightX = (int) XfromPolar(modulus, angle+angleSpread);
        rightY = (int) YfromPolar(modulus, angle+angleSpread);
        
        leftX += startx;
        leftY += starty;
        rightX += startx;
        rightY += starty;

        arrowHead.addPoint(topX,    topY);
        arrowHead.addPoint(leftX,    leftY);
        arrowHead.addPoint(rightX,    rightY);

        g.setColor(headColour);

        // draw poly
        offgraphics.fillPolygon(arrowHead);
    } // end paintArrowHead


////////////////////////////////////////////////////////////////////////////////////


public void draw2DMap(WorldItem allObjects[], Graphics g, Craft viewer) {
//void draw2DMap(struct object *allObjects[MAX_OBJECTS], int objectCount, struct aircraft *craft)
    int mapScale = 120;
    //final int POLYS            = 300;  // max number of polygons per object
    final int CRAFT_SCALE    = 10;  // max number of polygons per object

    int object;
    int point;
    int poly, i;
    int mapXpoints[] = new int[100];
    int mapYpoints[] = new int[100];
    double tempx, tempy;
    Polygon map[] = new Polygon[WorldItem.POLYS];

    // draw an arrow in the direction of the craft on the map
    paintArrowHead(g, (int)(((11000 + viewer.eyeX)  / mapScale)), (int)((viewer.eyeZ / mapScale)), viewer.Yaw * (180/Math.PI) - 90, Color.pink);

    // Draw 2d Map in corner of the screen

    // Draw LandMass items
    for (object=0; object<nLandMassObjects; object++)
    {
        // for each poly
        for (poly=0; poly<allObjects[object].polys; poly++)
        {

            // for each vertex in the poly
            // create 2DMap xycoord structure used by _polygon funtion
            for (int vertex=0; vertex<allObjects[object].polylist[poly][0]; vertex++)
            {                    // was 70            // was 200
                mapXpoints[vertex] = (int) (11000 + (allObjects[object].RealX[(int)(allObjects[object].polylist[poly][vertex+1])]) ) / mapScale;
                mapYpoints[vertex] = (int) (        (allObjects[object].RealZ[(int)(allObjects[object].polylist[poly][vertex+1])]) ) / mapScale;
                    
            } // end for
            map[poly] = new Polygon(mapXpoints, mapYpoints, allObjects[object].polylist[poly][0]);

            // set poly colour
            g.setColor(allObjects[object].polyColour[poly]);

            // test Clipping in 2d
            g.drawPolygon(map[poly]);
        } // end for
    } // end for


    // Draw LandItem items
    for (object=nLandMassObjects; object<nWorldObjects; object++)
    {
        // for each poly
        for (poly=0; poly<allObjects[object].polys; poly++)
        {

            // for each vertex in the poly
            // create 2DMap xycoord structure used by _polygon funtion
            for (int vertex=0; vertex<allObjects[object].polylist[poly][0]; vertex++)
            {                    // was 70            // was 200
                mapXpoints[vertex] = (int) (11000 + (allObjects[object].RealX[(int)(allObjects[object].polylist[poly][vertex+1])]) ) / mapScale;
                mapYpoints[vertex] = (int) (        (allObjects[object].RealZ[(int)(allObjects[object].polylist[poly][vertex+1])]) ) / mapScale;
                    
            } // end for
            map[poly] = new Polygon(mapXpoints, mapYpoints, allObjects[object].polylist[poly][0]);

            // set poly colour
            g.setColor(allObjects[object].polyColour[poly]);

            // test Clipping in 2d
            g.drawPolygon(map[poly]);
        } // end for
    } // end for

} // end draw2DMap


////////////////////////////////////////////////////////////////////////////////////

    public void keyReleased(KeyEvent evt) {
        if (debug)
            System.out.println("keyReleased - Begin/Exit");
        
        repaint();

    }

    public void keyTyped(KeyEvent evt) {
        if (debug)
            System.out.println("keyTyped - Begin/Exit");
        
        repaint();
    }

    //1.1 event handling
    public void mouseClicked(MouseEvent e) {
        if (debug)
            System.out.println("mouseClicked - Begin/Exit");
        
        setFocusable(true);
        requestFocusInWindow();
    }


    public void mousePressed(MouseEvent e) {
        if (debug)
            System.out.println("mousePressed - Begin");
        
        addMouseMotionListener(this);
        int modifiers = e.getModifiers();

//        if (debug)
//            System.out.println(MouseEvent.MOUSE_PRESSED + " Button1:" + MouseEvent.BUTTON1_MASK + " Button2:" + MouseEvent.BUTTON2_MASK + modifiers);

        int x = e.getX();
        int y = e.getY();
        // find the closest Action to this mouse event

        // left button    
        if ((modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
            if(crafts[currentCraft] != null)
            {
                initAircraftNew(crafts[currentCraft]);
                crafts[currentCraft].Speed = 0.0;
            }
            // move the chosen node to the mouse pointer
        }

        // middle button    
        if ((modifiers & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK) {
            if(crafts[currentCraft] != null)
            {
                crafts[currentCraft].Speed =100.1;          // move to somewhere in flight
                crafts[currentCraft].Roll  = 0.0;     crafts[currentCraft].Pitch = 45.034;  crafts[currentCraft].Yaw   = 2.356;
                crafts[currentCraft].eyeX  = 4500.0;  crafts[currentCraft].eyeY  = 10000.0;    crafts[currentCraft].eyeZ  = 13500.0;
            }
        }
        
        // right button    
        if ((modifiers & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
            if(crafts[currentCraft] != null)
            {
                // re-init craft
                initAircraftNew(crafts[currentCraft]);
                crafts[currentCraft].Speed = 400.0;
            }
            // move the chosen node to the mouse pointer
        }
        repaint();
        e.consume();
        System.out.println("mousePressed - Exit");
    } // end mousePressed


    public void mouseReleased(MouseEvent e) {
        removeMouseMotionListener(this);
        int modifiers = e.getModifiers();

        if ((modifiers & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK) {
            //optionsMenu.hide();
        }

        repaint();
        e.consume();
    }


    public void mouseEntered(MouseEvent e) {
        repaint();
        e.consume();
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mouseDragged(MouseEvent e) {
        int modifiers = e.getModifiers();

        // left button    
        if ((modifiers & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
            // move the chosen node to the mouse pointer
        }
        repaint();
        e.consume();
    }


    public void mouseMoved(MouseEvent e) {
    }


    public void start() {
        relaxer = new Thread(this);
        relaxer.start();
    }


    public void stop() {
        relaxer = null;
    }


    ////////////////////////////////////////////
    // should be in a seperate file
    ////////////////////////////////////////////
        
    public static double XfromPolar (double modulus, double argument) {
        argument *= (Math.PI / 180);    // degrees to radians.
        double x = (modulus * (Math.cos (argument )));
        return (x);
    }

    public static double YfromPolar (double modulus, double argument) {
        argument *= (Math.PI / 180);    // degrees to radians.
        double y = (modulus * (Math.sin (argument )));
        return (y);
    }

    public double Modulus (double x, double y) {
        double modulus;
        modulus = Math.sqrt( (x * x) + (y * y) );
        return (modulus);
    }

    public double Argument (double x, double y)     {
        double argument = 0.0;

        if ( (x > 0.0) && (y > 0.0) )        // first quad
        {
            argument = Math.atan (y / x);
            argument = argument / (Math.PI / 180);    // radians to degrees.
        }
        if ( (x < 0) && (y > 0) )        // second quad
        {
            argument = Math.atan (y / -x);
            argument = argument / (Math.PI / 180);    // radians to degrees.
            argument = 180 - argument;
        }
        if ( (x < 0) && (y < 0) )        // third quad
        {
            argument = Math.atan (-y / -x);
            argument = argument / (Math.PI / 180);    // radians to degrees.
            argument += 180;
        }
        if ( (x > 0) && (y < 0) )        // forth quad
        {
            argument = Math.atan (y / x);
            argument = argument / (Math.PI / 180);    // radians to degrees.
            argument += 360;
        }
        if ( (x > 0) && (y == 0) )
        {
            argument = 0;
        }
        if ( (x == 0) && (y > 0) )
        {
            argument = 90;
        }
        if ( (x < 0) && (y == 0) )
        {
            argument = 180;
        }
        if ( (x == 0) && (y < 0) )
        {
            argument = 270;
        }
        return (argument);
    }

} // end GraphPnl