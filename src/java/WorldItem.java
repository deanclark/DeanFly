import java.awt.Color;
import java.awt.Polygon;

/**
 * 
 * @author dean.clark
 *
 */
class WorldItem {

	final Color fixedColor = new Color(250, 220, 100);
	public	final static int OBJECT_POINTS	= 5000;  // max number of points per object
	public	final static int POLYS		    = 5000;  // max number of polygons per object
	public	final static int POLY_POINTS	= 200;  // max number of points for each polygon

	// real world co-ords
	String objectName;
	double RealX[] = new double[OBJECT_POINTS];   double RealY[] = new double[OBJECT_POINTS];
	double RealZ[] = new double[OBJECT_POINTS];   double RealH[] = new double[OBJECT_POINTS];  // object points in the real measures
	int scaleFactor;
	Polygon	poly[]		= new Polygon[POLYS];
	Color	polyColour[]	= new Color[POLYS];
	int	polys;                                                 // number of polygons
	boolean	hiddenObject;
	boolean	hiddenPolys[] = new boolean[POLYS];
	int      polylist[][] = new int[POLYS][POLY_POINTS];		// list of object faces
	int      sorted_list[] = new int[POLYS];			// sorted list of polys
	int      vertices;						// number of polygons
	double   rotx;       double roty;      double rotz;		// object rotation
	int      posx;       int     posy;      int   posz;		// world possition
	double   newX[] = new double[OBJECT_POINTS];     double newY[] = new double[OBJECT_POINTS];
	double   newZ[] = new double[OBJECT_POINTS];
	double   averageZposition;		// holds the average Z value for all points in the object, this is used to sort objects
	double   averageXposition;
	double   averageDistanceFromViewer;
	// Array of points used to create the polys of a WorldItem.  These will change if Clipped
	int points;
	double xtemp[] = new double[POLY_POINTS];
	double ytemp[] = new double[POLY_POINTS];
	double xpoints[] = new double[POLY_POINTS];
	double ypoints[] = new double[POLY_POINTS];
	double zpoints[] = new double[POLY_POINTS];
	boolean updated[] = new boolean[POLY_POINTS];
	int xpoly[] = new int[POLY_POINTS];
	int ypoly[] = new int[POLY_POINTS];

	// Initial coordinates	
	int discardDegRotation = 0;
	int initX = 0, initY = 0, initZ = 0;
	double preRotation = 0.0;
	int scale = 0;
}