import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** The on screen text and Quit box must fit inside the view (they used to run off the right hand edge). */
class HudLayoutTest {

    /** Draws onto a white image the size of the view and returns the bounding box {minX, minY, maxX, maxY} of the ink. */
    static int[] inkBounds(Consumer<Graphics2D> draw) {
        BufferedImage img = new BufferedImage(WorldHarness.IMG_W, WorldHarness.IMG_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setFont(GraphPnl.screenFont);
        draw.accept(g);
        g.dispose();

        int[] box = {Integer.MAX_VALUE, Integer.MAX_VALUE, -1, -1};
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    box[0] = Math.min(box[0], x);
                    box[1] = Math.min(box[1], y);
                    box[2] = Math.max(box[2], x);
                    box[3] = Math.max(box[3], y);
                }
        return box;
    }

    static void assertInsideView(int[] box, String what) {
        assertTrue(box[2] >= 0, what + " drew nothing");
        assertTrue(box[0] >= 0 && box[1] >= 0, what + " starts outside the view: " + box[0] + "," + box[1]);
        assertTrue(box[2] < WorldHarness.IMG_W, what + " runs off the right edge: x=" + box[2] + " of " + WorldHarness.IMG_W);
        assertTrue(box[3] < WorldHarness.IMG_H, what + " runs off the bottom edge: y=" + box[3] + " of " + WorldHarness.IMG_H);
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void instrumentsFitInsideTheView(String world) {
        WorldHarness.load(world);
        int[] box = inkBounds(g -> WorldHarness.panel.whereAmI(g, WorldHarness.craft()));
        assertInsideView(box, "instruments");
        assertTrue(box[2] > WorldHarness.IMG_W * 3 / 4, "instruments should sit towards the right hand side");
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void keyHelpFitsInsideTheView(String world) {
        WorldHarness.load(world);
        int[] box = inkBounds(g -> WorldHarness.panel.keyHelp(g, WorldHarness.craft()));
        assertInsideView(box, "key help");
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void quitBoxIsInsideTheViewAndClearOfTheText(String world) {
        WorldHarness.load(world);
        int[] box = inkBounds(g -> WorldHarness.panel.quitBox(g, GraphPnl.box, g.getFontMetrics()));
        assertInsideView(box, "quit box");

        int[] text = inkBounds(g -> {
            WorldHarness.panel.whereAmI(g, WorldHarness.craft());
            WorldHarness.panel.keyHelp(g, WorldHarness.craft());
        });
        assertTrue(box[1] > text[3], "quit box overlaps the text (box top " + box[1] + ", text bottom " + text[3] + ")");
    }
}
