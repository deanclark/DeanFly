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
- `web/` — legacy NetBeans web app (applet HTML/JSP, copy of world data). Not used by the Gradle build.
- `Doxygen/` — `lld.doxyfile`, `lld.dox`, checked-in generated `html/` and `rtf/`.

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
- 2026-11-01: Fixed the off-centre view. `WorldPerspective` centred on a hard-coded (160,100) (original 320x200 screen) and the clip rectangle used the full monitor size, while the view is monitor/`SCREEN_SCALER`. New `GraphPnl.configureViewport(craft, monitorW, monitorH)` (used by `update()` and the tests) sets the view size, clip rect (view less 2px border) and the projection now centres on view W/2,H/2. `FOCAL_DISTANCE` (400) unchanged, so the field of view is wider than the original 320x200 design. Tests: clip rect matches view, straight-ahead point projects to the centre, symmetric projection.
- 2026-11-01: Ran `doxygen -u` on `lld.doxyfile` (obsolete tags removed, now 1.18 format), hand-repaired the mangled `ALIASES`, set `PAPER_TYPE = a4`, escaped `<Subject>` in `lld.dox`. Doxygen config produces no config warnings; 2 harmless 'image file name is ambiguous' warnings remain (images also exist under the checked-in `Doxygen/html`). Checked-in `Doxygen/html`/`rtf` output was NOT regenerated.
- Doxygen 1.18 (winget) builds the docs; Gradle `doxygen` task writes to `build/doxygen` so tracked `Doxygen/html` isn't churned.

## Blockers / obsolete items for newer Java
- **Applet API: resolved.** No longer blocks JDK 26+. Deprecation warnings also cleaned up (build is warning-free with `-Xlint:deprecation -Xlint:removal`).
- `GraphPnl extends java.awt.Panel` — works, but legacy AWT; fine for now.
- No third-party libraries, so nothing to upgrade. README mentioned JUnit/Maven (`mvn eclipse:eclipse`) — no tests or pom exist.
- `DeanFly.mainInit()` points at dead host `www.dean.seetech.com` when `GraphPnl.runlocal` is false (it is true by default).
- Doxyfile modernised (see decisions log); `Doxygen/warning.log` is stale (old Windows path).
- Duplicate world data under `web/` and `web/WEB-INF` plus many `.bak` files.

## Next steps / ideas
- Optional: HUD text uses fixed x positions (400, ~600) so the right column (Yaw/Altitude/...) is cut off at the view edge; 2D map and clip-map overlays also use fixed positions.
