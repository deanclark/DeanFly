import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Pins the keys that the key help menu and the readme document. */
class KeyBindingsTest {

    @Test
    void wireframeIsOnB() {
        WorldHarness.load("cubeWorld");
        WorldHarness.start();
        boolean before = GraphPnl.wireframe;
        WorldHarness.key('b');
        assertNotEquals(before, GraphPnl.wireframe);
        WorldHarness.key('b');
        assertEquals(before, GraphPnl.wireframe);
    }

    @Test
    void wDoesNotToggleWireframe() {
        WorldHarness.load("islandWorld");
        WorldHarness.start();
        boolean before = GraphPnl.wireframe;
        WorldHarness.key('w');
        assertEquals(before, GraphPnl.wireframe);
    }

    @Test
    void displayAndRenderingToggles() {
        WorldHarness.load("islandWorld");
        WorldHarness.start();

        boolean hidden = GraphPnl.hiddenSurfaces;
        WorldHarness.key('h');
        assertNotEquals(hidden, GraphPnl.hiddenSurfaces, "'h' toggles hidden surface removal");
        WorldHarness.key('H');
        assertEquals(hidden, GraphPnl.hiddenSurfaces, "'H' toggles hidden surface removal");

        boolean help = GraphPnl.keyHelpDisplay;
        WorldHarness.key('k');
        assertNotEquals(help, GraphPnl.keyHelpDisplay, "'k' toggles the key help menu");
        WorldHarness.key('K');
        assertEquals(help, GraphPnl.keyHelpDisplay, "'K' toggles the key help menu");

        boolean hiddenAfterHelp = GraphPnl.hiddenSurfaces;
        WorldHarness.key('k');
        assertEquals(hiddenAfterHelp, GraphPnl.hiddenSurfaces, "'k' must not touch hidden surface removal");

        boolean flip = GraphPnl.flipped;
        WorldHarness.key('f');
        assertNotEquals(flip, GraphPnl.flipped);

        boolean perspective = GraphPnl.perspective;
        WorldHarness.key('p');
        assertNotEquals(perspective, GraphPnl.perspective);

        boolean zClip = GraphPnl.clip;
        WorldHarness.key('c');
        assertNotEquals(zClip, GraphPnl.clip, "'c' toggles Z clipping");

        boolean screenClip = GraphPnl.clipScreen;
        WorldHarness.key('C');
        assertNotEquals(screenClip, GraphPnl.clipScreen, "'C' toggles screen clipping");

        boolean map = GraphPnl.mapOn;
        WorldHarness.key('m');
        assertNotEquals(map, GraphPnl.mapOn, "'m' toggles the 2D map");
        WorldHarness.key('M');
        assertTrue(GraphPnl.displayClipMap, "'M' shows the clip map (and hides the 2D map)");
        assertFalse(GraphPnl.mapOn);
    }

    @Test
    void walkerStepsWithWAndSAndTurnsWithAAndD() {
        WorldHarness.load("cubeWorld");
        WorldHarness.start();
        assertEquals("Walker", WorldHarness.craft().type);

        double z = WorldHarness.craft().eyeZ;
        WorldHarness.key('s');
        assertTrue(WorldHarness.craft().eyeZ > z, "'s' steps forward");
        z = WorldHarness.craft().eyeZ;
        WorldHarness.key('w');
        assertTrue(WorldHarness.craft().eyeZ < z, "'w' steps back");

        double yaw = WorldHarness.craft().Yaw;
        WorldHarness.key('a');
        assertNotEquals(yaw, WorldHarness.craft().Yaw, "'a' turns");
        yaw = WorldHarness.craft().Yaw;
        WorldHarness.key('d');
        assertNotEquals(yaw, WorldHarness.craft().Yaw, "'d' turns");
    }
}
