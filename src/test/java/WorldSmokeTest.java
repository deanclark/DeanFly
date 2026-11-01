import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Headless smoke test: each world loads, renders something, and survives the flight controls. */
class WorldSmokeTest {

    static int distinctColours(BufferedImage img) {
        Set<Integer> colours = new HashSet<>();
        for (int y = 0; y < img.getHeight(); y += 2)
            for (int x = 0; x < img.getWidth(); x += 2)
                colours.add(img.getRGB(x, y));
        return colours.size();
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void worldLoadsAndRenders(String world) {
        WorldHarness.load(world);

        assertTrue(GraphPnl.availableCrafts > 0, world + " should define at least one craft");
        assertTrue(GraphPnl.nLandMassObjects > 0, world + " should load land mass objects");
        assertTrue(GraphPnl.nWorldObjects >= GraphPnl.nLandMassObjects, world + " object count");
        assertNotNull(WorldHarness.craft());

        WorldHarness.start();
        BufferedImage frame = WorldHarness.render();
        assertTrue(distinctColours(frame) > 3, world + " first frame looks blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"islandWorld", "cubeWorld"})
    void worldSurvivesFlightControls(String world) {
        WorldHarness.load(world);
        WorldHarness.start();

        for (char c : "+wwwdddsssaaa-=_".toCharArray()) {
            WorldHarness.key(c);
            BufferedImage frame = WorldHarness.render();
            assertTrue(distinctColours(frame) > 3, world + " frame blank after key '" + c + "'");
        }

        // educational toggles
        for (char c : new char[] {'b', 'b', 'c', 'c', 'C', 'C'}) {
            WorldHarness.key(c);
            WorldHarness.render();
        }
    }
}
