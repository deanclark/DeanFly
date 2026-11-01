# CLAUDE.md — DeanFly

Educational 3D flight simulator (clipping, perspective, hidden-surface removal, double buffering).
Originally C++ (Watcom), ported to Java as a learning exercise. Plain Swing/AWT, **no external libraries**.

## Working rules
- **Commit and push to GitHub regularly** with clean, descriptive commit messages so status/work is never lost.
- **Update this file at every checkpoint** with decisions, thinking and next steps so work can be resumed exactly.
- Keep `readme.md` up to date with build/run instructions.
- `PROJECT_SUMMARY.local.md` is a local-only conversation summary — **never commit it** (listed in `.git/info/exclude`).
- Source files use CRLF line endings; preserve them when editing (avoid tools that normalise EOLs).

## Layout
- `src/java/` — all source (default package): `DeanFly` (JPanel + `main`), `GraphPnl` (4.8k-line engine/renderer/loader), `Craft`, `WorldItem`, `Picture`, `Screen`, `TestPanel`.
- `islandWorld/`, `cubeWorld/` — world data (craftType, landMass, landItems, objects). Loaded **relative to the working directory**, so run from the repo root.
- `Doxygen/` — `lld.doxyfile`, `lld.dox`, `images/` (images used by `lld.dox`), checked-in generated `html/` and `rtf/` plus `warning.log`. Refresh them with `cd Doxygen && doxygen lld.doxyfile` (delete `html`/`rtf` first so removed files go). `latex/` is stale (LaTeX output is switched off in the config) and was left alone.

## Build / run (Gradle 9.8.2 wrapper, JDK 25 toolchain)
- `./gradlew build` — compile + `build/libs/DeanFly.jar`
- `./gradlew run -Pworld=islandWorld|cubeWorld` — play a world (default islandWorld)
- `./gradlew test` — headless smoke + clipping tests (also run by `build`)
- `./gradlew doxygen` — docs into `build/doxygen` (needs `doxygen` on PATH; Graphviz `dot` optional)
- VS Code: `.vscode/launch.json` has one launch config per world; `.vscode/tasks.json` wraps the Gradle tasks.
- `DeanFly.main` takes an optional first arg = world name (added during migration).

## Decisions log
- 2026-11-01: Replaced Ant/NetBeans (`build.xml`, `nbproject/`) with Gradle (`build.gradle`, `settings.gradle`, wrapper). Eclipse `.classpath/.project` left in place.
- Toolchain set to Java 25 (latest LTS, installed locally). Source compiled unchanged on JDK 25 — no code changes needed except the world arg.
- Both worlds launch and run 15s+ with no exceptions in console (GUI play not visually verified by Claude).
- 2026-11-01: Removed the Applet API dependency: `DeanFly` now extends `JPanel` (was `JApplet`); dropped the unused applet `getParameter` branch and `appletLanuch` flag. `web/*.html` applet pages are now obsolete (applets have not run in browsers for years). Both worlds still launch cleanly; `[removal]` warning gone.
- 2026-11-01: Fixed deprecations in `GraphPnl`: `new URL(String)` -> `new URI(..).toURL()`; mouse handlers use `getButton()` / `getModifiersEx()` + `BUTTON1_DOWN_MASK` instead of `getModifiers()`/`BUTTON*_MASK`. Both worlds still launch cleanly. Mouse-button cheat controls not manually clicked-tested.
- 2026-11-01: Added headless tests (JUnit 5, `./gradlew test`, `src/test/java`): `WorldSmokeTest` (both worlds load, render, survive controls) and `ClippingTest` (near-plane + screen clip unit tests, plus cubeWorld w-then-d scene regression comparing screen clipping on vs off pixel-by-pixel). `WorldHarness` drives the static-heavy engine without a window. Needed small refactors: `DeanFly.loadWorld()` (extracted from `mainInit`) and `GraphPnl.drawScene(Graphics)` (extracted from `update`). Test JVM needs 2g heap.
- 2026-11-01: Fixed the cubeWorld visual clipping glitch (streaks after `w` then `d`). Root causes in `GraphPnl`: (1) `getIntersectPoint` extrapolated to z=0 not the clip plane (and `makeBigger` distorted slopes); (2) `ClipPoly3D` skipped copying vertices whose stale `updated[]` flag was set, so an added vertex was lost; (3) `WorldPerspective` projected only the original vertex count, leaving added vertices stale; (4) `basicClipPoly3D` never set `points`; (5) screen clipper (`getIntersectPointScreen` + four `ClipPoly*` copies with corner heuristics) was replaced by one Sutherland-Hodgman `clipPolyAgainstEdge`. Screen-clip-on vs off now differs by ~20-200 rounding pixels instead of up to ~9000.
- 2026-11-01: User confirmed the mouse cheat controls (left/middle/right) still work after the `getButton()` change.
- 2026-11-01: Fixed the key-help menu (`keyHelp`): Wireframe is `b` (was shown as `w`), Surface removal `h`, This menu `K/k` (was `H/h/K/k`), Pitch/Walk `w/s`, Roll/Turn `a/d`; the non-existent Yaw `1/3` row became Start/Continue `S/C`. Readme Controls section rewritten from the real bindings (Aircraft vs Walker; removed unimplemented `?` and the `h` = help claim). `KeyBindingsTest` pins the documented keys; `WorldHarness.load` now resets all display toggles.
- 2026-11-01: Applet question: running as an applet is NOT an option. Browsers dropped Java plugins years ago, `appletviewer` went in JDK 11, and JEP 504 removed `java.applet`/`JApplet` in JDK 26 (JDK 25 is the last with it). `web/` (applet HTML, JSP, WEB-INF copies of the world data) is therefore dead; it was deleted on 2026-11-01 (see below). Possible web route: CheerpJ (WebAssembly JVM, runs Swing/AWT jars in the browser; supports Java 8/11/17 so the jar would need `--release 17`; free for personal use) - not implemented.
- 2026-11-01: Regenerated the checked-in `Doxygen/html`, `Doxygen/rtf` and `warning.log` with Doxygen 1.18 (now also documents the test classes; 0 warnings, stable across repeat runs). Fixes needed: `IMAGE_PATH` was `./` and, with `RECURSIVE = YES`, also scanned the generated `html/` copies, giving 'ambiguous image' warnings and a 'could not copy graph_legend.png' error on re-runs, so the three images used by `lld.dox` moved to `Doxygen/images/` and `IMAGE_PATH = ./images` (`EXCLUDE` does not apply to image lookup). `logo.jpg` (the crystal logo) only existed as a stray copy in `html/`, so it was restored to `Doxygen/images/`. The Screenshot-*.png files stay in `Doxygen/` because the readme links to them on GitHub. Old stray `html/` files (FreeSans.ttf, tab_*.gif) are gone. `.gitignore` reduced to `/build/`, `/.gradle/`, `/bin/` and the `.vscode` rules (dropped `/OBJECTS/`, `/dist/`).
- 2026-11-01: Deleted `web/` (285 files: applet HTML/JSP, `WEB-INF`, duplicated world data and `.bak` files) at the user's request. Root `islandWorld/` and `cubeWorld/` are the only world data; the `web/` copies were older (craftType start altitudes 30/10, below `Craft.MINIMUM_ALTITUDE` 60). Recoverable from git history (commit before this one). `.gitignore` still lists `/OBJECTS/`, harmless.
- 2026-11-01: Anchored the on-screen text to the view: instruments and key-help columns now start at `hudColumn(viewer)` (right aligned, was fixed x=400/600 so the right column was cut off at 640 wide); the Quit box moved from fixed (750,10), which was off screen, to the bottom-right corner. `HudLayoutTest` renders each overlay alone and checks the ink stays inside the view.
- 2026-11-01: Fixed the off-centre view. `WorldPerspective` centred on a hard-coded (160,100) (original 320x200 screen) and the clip rectangle used the full monitor size, while the view is monitor/`SCREEN_SCALER`. New `GraphPnl.configureViewport(craft, monitorW, monitorH)` (used by `update()` and the tests) sets the view size, clip rect (view less 2px border) and the projection now centres on view W/2,H/2. `FOCAL_DISTANCE` (400) unchanged, so the field of view is wider than the original 320x200 design. Tests: clip rect matches view, straight-ahead point projects to the centre, symmetric projection.
- 2026-11-01: Ran `doxygen -u` on `lld.doxyfile` (obsolete tags removed, now 1.18 format), hand-repaired the mangled `ALIASES`, set `PAPER_TYPE = a4`, escaped `<Subject>` in `lld.dox`. Doxygen config produces no config warnings; 2 harmless 'image file name is ambiguous' warnings remain (images also exist under the checked-in `Doxygen/html`). 
- Doxygen 1.18 (winget) builds the docs; Gradle `doxygen` task writes to `build/doxygen` so tracked `Doxygen/html` isn't churned.

## Blockers / obsolete items for newer Java
- **Applet API: resolved.** No longer blocks JDK 26+. Deprecation warnings also cleaned up (build is warning-free with `-Xlint:deprecation -Xlint:removal`).
- `GraphPnl extends java.awt.Panel` — works, but legacy AWT; fine for now.
- No third-party libraries, so nothing to upgrade. README mentioned JUnit/Maven (`mvn eclipse:eclipse`) — no tests or pom exist.
- `DeanFly.mainInit()` points at dead host `www.dean.seetech.com` when `GraphPnl.runlocal` is false (it is true by default).

## Next steps / ideas
- Optional: the 2D map and clip-map overlays use fixed positions (they fit at the default view size, so left alone).
