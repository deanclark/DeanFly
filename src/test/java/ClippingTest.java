import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Clipping tests: unit tests for the near-plane (3D) and screen (2D) clippers, and a scene
 * regression for the cubeWorld glitch seen after a few 'w' (forward) then 'd' (turn right) presses.
 */
class ClippingTest {
    static final double Z_CLIP = 50.0;
    static GraphPnl panel;

    @BeforeAll
    static void setUp() {
        WorldHarness.load("cubeWorld");
        panel = WorldHarness.panel;
    }

    static void setVertices(WorldItem item, double[][] xyz) {
        item.polylist[0][0] = xyz.length;
        for (int i = 0; i < xyz.length; i++) {
            item.xpoints[i] = xyz[i][0];
            item.ypoints[i] = xyz[i][1];
            item.zpoints[i] = xyz[i][2];
        }
    }

    /** Builds a one-poly item whose scratch arrays hold the given vertices. */
    static WorldItem poly(double[][] xyz) {
        WorldItem item = new WorldItem();
        setVertices(item, xyz);
        return item;
    }

    static void assertAllInFront(WorldItem item) {
        for (int i = 0; i < item.points; i++)
            assertTrue(item.zpoints[i] >= Z_CLIP - 1e-9, "vertex " + i + " is behind the clip plane");
    }

    // ---- near plane (3D) clipping -------------------------------------------------------------

    @Test
    void nearPlaneRemovesPolyBehindViewer() {
        WorldItem item = poly(new double[][] {{0, 0, 10}, {100, 0, 20}, {0, 100, 30}});
        assertFalse(panel.ClipPoly3D(item, Z_CLIP, 0));
    }

    @Test
    void nearPlaneLeavesPolyInFrontUntouched() {
        WorldItem item = poly(new double[][] {{0, 0, 100}, {100, 0, 200}, {0, 100, 300}});
        assertTrue(panel.ClipPoly3D(item, Z_CLIP, 0));
        assertEquals(3, item.points);
        assertEquals(200, item.zpoints[1], 1e-9);
    }

    @Test
    void nearPlaneIntersectionsLieOnTheClipPlaneAndOnTheEdge() {
        // triangle with one vertex (index 1) behind the plane: becomes a quad
        WorldItem item = poly(new double[][] {{0, 0, 150}, {100, 0, 0}, {0, 100, 150}});
        assertTrue(panel.ClipPoly3D(item, Z_CLIP, 0));
        assertEquals(4, item.points);
        assertAllInFront(item);

        // edge (0,0,150)->(100,0,0) crosses z=50 at (66.67, 0); edge (100,0,0)->(0,100,150) at (66.67, 33.33)
        int onPlane = 0;
        for (int i = 0; i < item.points; i++) {
            if (Math.abs(item.zpoints[i] - Z_CLIP) < 1e-9) {
                onPlane++;
                boolean onFirstEdge = Math.abs(item.xpoints[i] - 200.0 / 3) < 1e-6 && Math.abs(item.ypoints[i]) < 1e-6;
                boolean onSecondEdge = Math.abs(item.xpoints[i] - 200.0 / 3) < 1e-6 && Math.abs(item.ypoints[i] - 100.0 / 3) < 1e-6;
                assertTrue(onFirstEdge || onSecondEdge,
                        "intersection (" + item.xpoints[i] + "," + item.ypoints[i] + ") is not on the polygon edge");
            }
        }
        assertEquals(2, onPlane);
    }

    @Test
    void nearPlaneClipAddingAVertexSurvivesStaleScratchState() {
        // regression: a previous poly left updated[] set, which stopped the extra vertex being stored
        WorldItem item = poly(new double[][] {{0, 0, 100}, {10, 0, 110}, {20, 10, 120}, {10, 20, 110}, {0, 10, 100}});
        assertTrue(panel.ClipPoly3D(item, Z_CLIP, 0));
        assertEquals(5, item.points);

        // a triangle that gains a vertex when one corner is cut off
        setVertices(item, new double[][] {{0, 0, 150}, {100, 0, 0}, {0, 100, 150}});
        assertTrue(panel.ClipPoly3D(item, Z_CLIP, 0));
        assertEquals(4, item.points);
        assertAllInFront(item);
    }

    @Test
    void perspectiveProjectsEveryClippedVertex() {
        // regression: only the original vertex count was projected, leaving the added vertex with stale co-ordinates
        WorldItem item = poly(new double[][] {{0, 0, 150}, {100, 0, 0}, {0, 100, 150}});
        assertTrue(panel.ClipPoly3D(item, Z_CLIP, 0));
        Arrays.fill(item.xtemp, Double.NaN);
        Arrays.fill(item.ytemp, Double.NaN);
        panel.WorldPerspective(item, WorldHarness.craft(), 0, false);
        for (int i = 0; i < item.points; i++) {
            assertFalse(Double.isNaN(item.xtemp[i]), "x of vertex " + i + " not projected");
            assertFalse(Double.isNaN(item.ytemp[i]), "y of vertex " + i + " not projected");
        }
    }

    // ---- screen (2D) clipping ------------------------------------------------------------------

    static WorldItem screenPoly(int[][] xy) {
        WorldItem item = new WorldItem();
        item.points = xy.length;
        for (int i = 0; i < xy.length; i++) {
            item.xpoly[i] = xy[i][0];
            item.ypoly[i] = xy[i][1];
        }
        return item;
    }

    static void clipToScreen(WorldItem item, Screen screen) {
        panel.ClipPolyXmin(item, WorldHarness.craft(), screen);
        panel.ClipPolyYmin(item, WorldHarness.craft(), screen);
        panel.ClipPolyXmax(item, WorldHarness.craft(), screen);
        panel.ClipPolyYmax(item, WorldHarness.craft(), screen);
    }

    static Screen screen(int xmin, int ymin, int xmax, int ymax) {
        Screen s = new Screen();
        s.xmin = xmin;
        s.ymin = ymin;
        s.xmax = xmax;
        s.ymax = ymax;
        return s;
    }

    @Test
    void screenClipLeavesInsidePolyUntouched() {
        WorldItem item = screenPoly(new int[][] {{20, 20}, {80, 20}, {50, 70}});
        clipToScreen(item, screen(2, 2, 100, 100));
        assertEquals(3, item.points);
        assertArrayEquals(new int[] {20, 80, 50}, Arrays.copyOf(item.xpoly, 3));
        assertArrayEquals(new int[] {20, 20, 70}, Arrays.copyOf(item.ypoly, 3));
    }

    @Test
    void screenClipCutsPolyCoveringTheWholeScreenToTheScreenRectangle() {
        WorldItem item = screenPoly(new int[][] {{-500, -500}, {500, -500}, {500, 500}, {-500, 500}});
        clipToScreen(item, screen(2, 2, 100, 100));
        assertEquals(4, item.points);
        for (int i = 0; i < item.points; i++) {
            assertTrue(item.xpoly[i] == 2 || item.xpoly[i] == 100, "x=" + item.xpoly[i]);
            assertTrue(item.ypoly[i] == 2 || item.ypoly[i] == 100, "y=" + item.ypoly[i]);
        }
    }

    @Test
    void screenClipKeepsTheCornerWhenAPolyWrapsAroundIt() {
        // triangle poking out past the top-left corner: the clipped poly must include the (xmin,ymin) corner
        WorldItem item = screenPoly(new int[][] {{-50, -50}, {60, -50}, {-50, 60}});
        clipToScreen(item, screen(0, 0, 100, 100));
        boolean hasCorner = false;
        for (int i = 0; i < item.points; i++)
            hasCorner |= item.xpoly[i] == 0 && item.ypoly[i] == 0;
        assertTrue(hasCorner, "corner vertex missing from clipped poly");
    }

    @Test
    void screenClipOfRandomPolysStaysInsideTheScreen() {
        Random random = new Random(42);
        Screen screen = screen(2, 2, 638, 358);
        for (int n = 0; n < 2000; n++) {
            int count = 3 + random.nextInt(4);
            int[][] pts = new int[count][2];
            for (int i = 0; i < count; i++) {
                pts[i][0] = random.nextInt(4000) - 2000;
                pts[i][1] = random.nextInt(4000) - 2000;
            }
            WorldItem item = screenPoly(pts);
            clipToScreen(item, screen);
            for (int i = 0; i < item.points; i++) {
                assertTrue(item.xpoly[i] >= screen.xmin && item.xpoly[i] <= screen.xmax, "x out of range: " + item.xpoly[i]);
                assertTrue(item.ypoly[i] >= screen.ymin && item.ypoly[i] <= screen.ymax, "y out of range: " + item.ypoly[i]);
            }
        }
    }

    // ---- view centring -------------------------------------------------------------------------

    @Test
    void screenClipRectangleMatchesTheViewAndIsCentred() {
        WorldHarness.load("cubeWorld");
        Screen screen = GraphPnl.screen;
        assertEquals(WorldHarness.IMG_W - 2, screen.xmax, "clip rectangle should end at the view's right edge");
        assertEquals(WorldHarness.IMG_H - 2, screen.ymax, "clip rectangle should end at the view's bottom edge");
        assertEquals(WorldHarness.IMG_W - screen.xmax, screen.xmin, "left and right borders differ");
        assertEquals(WorldHarness.IMG_H - screen.ymax, screen.ymin, "top and bottom borders differ");
        assertEquals(WorldHarness.IMG_W / 2, screen.xmid);
        assertEquals(WorldHarness.IMG_H / 2, screen.ymid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void aPointStraightAheadProjectsToTheMiddleOfTheView(String world) {
        WorldHarness.load(world);
        WorldItem item = poly(new double[][] {{0, 0, 500}, {0, 0, 900}, {0, 0, 2000}});
        item.points = 3;
        WorldHarness.panel.WorldPerspective(item, WorldHarness.craft(), 0, false);
        for (int i = 0; i < 3; i++) {
            assertEquals(WorldHarness.IMG_W / 2.0, item.xtemp[i], 1e-9, "x of point " + i);
            assertEquals(WorldHarness.IMG_H / 2.0, item.ytemp[i], 1e-9, "y of point " + i);
        }
    }

    @Test
    void pointsEitherSideOfTheViewerProjectSymmetricallyAboutTheCentre() {
        WorldHarness.load("cubeWorld");
        WorldItem item = poly(new double[][] {{-300, 200, 1000}, {300, -200, 1000}});
        item.points = 2;
        WorldHarness.panel.WorldPerspective(item, WorldHarness.craft(), 0, false);
        assertEquals(WorldHarness.IMG_W, item.xtemp[0] + item.xtemp[1], 1e-9, "x not mirrored about the centre");
        assertEquals(WorldHarness.IMG_H, item.ytemp[0] + item.ytemp[1], 1e-9, "y not mirrored about the centre");
    }

    // ---- whole scene -------------------------------------------------------------------------

    static int diffPixels(BufferedImage a, BufferedImage b) {
        int n = 0;
        for (int y = 3; y < a.getHeight() - 3; y++)
            for (int x = 3; x < a.getWidth() - 3; x++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) n++;
        return n;
    }

    /** Frame settled (the engine sorts using the previous frame) with the given screen clipping state. */
    static BufferedImage settledFrame(boolean clipScreen) {
        GraphPnl.clipScreen = clipScreen;
        WorldHarness.render();
        BufferedImage frame = WorldHarness.render();
        GraphPnl.clipScreen = true;
        return frame;
    }

    /**
     * Screen clipping must only trim geometry that is off screen, so with it on or off the visible pixels
     * should match apart from a few rounding pixels on the clip edges. Before the clipping fixes, turning
     * right after walking forward in cubeWorld produced streaks across the ceiling (thousands of pixels).
     */
    @Test
    void cubeWorldWalkForwardThenTurnRightClipsCleanly() {
        WorldHarness.load("cubeWorld");
        WorldHarness.start();

        final int allowedDifferentPixels = WorldHarness.IMG_W * WorldHarness.IMG_H / 400;   // 0.25%
        WorldHarness.keys('w', 6);
        for (int turn = 0; turn <= 30; turn++) {
            if (turn > 0) WorldHarness.key('d');
            int diff = diffPixels(settledFrame(true), settledFrame(false));
            assertTrue(diff <= allowedDifferentPixels,
                    "after 6 x 'w' and " + turn + " x 'd' the clipped view differs from the unclipped view by "
                            + diff + " pixels (limit " + allowedDifferentPixels + ")");
        }
    }

    @Test
    void cubeWorldStaysCleanWhilePressingWAndDInTurn() {
        WorldHarness.load("cubeWorld");
        WorldHarness.start();
        final int allowedDifferentPixels = WorldHarness.IMG_W * WorldHarness.IMG_H / 400;
        for (int step = 0; step < 25; step++) {
            WorldHarness.keys('w', 1 + step % 4);
            WorldHarness.keys('d', 1 + step % 3);
            int diff = diffPixels(settledFrame(true), settledFrame(false));
            assertTrue(diff <= allowedDifferentPixels, "step " + step + ": " + diff + " pixels differ");
        }
    }
}
