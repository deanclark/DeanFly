import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Headless driver for the simulator. The engine keeps its state in statics, so
 * every {@link #load(String)} resets that state before loading a world.
 */
final class WorldHarness {
    static final int SCREEN_W = 1920, SCREEN_H = 1080;      // the "monitor" the view size is derived from
    static final int IMG_W = SCREEN_W / GraphPnl.SCREEN_SCALER;
    static final int IMG_H = SCREEN_H / GraphPnl.SCREEN_SCALER;

    static GraphPnl panel;

    private WorldHarness() {}

    static void load(String world) {
        System.setProperty("java.awt.headless", "true");
        GraphPnl.nWorldObjects = 0;
        GraphPnl.nLandMassObjects = 0;
        GraphPnl.availableCrafts = 0;
        GraphPnl.currentCraft = 0;
        java.util.Arrays.fill(GraphPnl.crafts, null);
        GraphPnl.exit = false;
        GraphPnl.quit = false;
        GraphPnl.crash = false;
        GraphPnl.clip = true;
        GraphPnl.clipScreen = true;
        GraphPnl.mapOn = false;
        GraphPnl.displayClipMap = false;

        DeanFly.worldName = world;
        DeanFly deanFly = new DeanFly();
        deanFly.loadWorld();
        panel = new GraphPnl(deanFly);

        GraphPnl.configureViewport(craft(), SCREEN_W, SCREEN_H);   // same set up update() does
    }

    static Craft craft() {
        return GraphPnl.crafts[GraphPnl.currentCraft];
    }

    static void key(char c) {
        panel.keyPressed(new KeyEvent(panel, KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_UNDEFINED, c));
    }

    static void keys(char c, int times) {
        for (int i = 0; i < times; i++) key(c);
    }

    /** Presses 'S' to leave the title screen if needed. */
    static void start() {
        if (GraphPnl.quit) key('S');
    }

    static BufferedImage render() {
        BufferedImage img = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setFont(GraphPnl.screenFont);
            panel.setBackground(Color.white);
            panel.drawScene(g);
        } finally {
            g.dispose();
        }
        return img;
    }
}
