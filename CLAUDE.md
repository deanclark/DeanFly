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
- `./gradlew doxygen` — docs into `build/doxygen` (needs `doxygen` on PATH; Graphviz `dot` optional)
- VS Code: `.vscode/launch.json` has one launch config per world; `.vscode/tasks.json` wraps the Gradle tasks.
- `DeanFly.main` takes an optional first arg = world name (added during migration).

## Decisions log
- 2026-11-01: Replaced Ant/NetBeans (`build.xml`, `nbproject/`) with Gradle (`build.gradle`, `settings.gradle`, wrapper). Eclipse `.classpath/.project` left in place.
- Toolchain set to Java 25 (latest LTS, installed locally). Source compiled unchanged on JDK 25 — no code changes needed except the world arg.
- Both worlds launch and run 15s+ with no exceptions in console (GUI play not visually verified by Claude).
- 2026-11-01: Removed the Applet API dependency: `DeanFly` now extends `JPanel` (was `JApplet`); dropped the unused applet `getParameter` branch and `appletLanuch` flag. `web/*.html` applet pages are now obsolete (applets have not run in browsers for years). Both worlds still launch cleanly; `[removal]` warning gone.
- Doxygen 1.18 (winget) builds the docs; Gradle `doxygen` task writes to `build/doxygen` so tracked `Doxygen/html` isn't churned.

## Blockers / obsolete items for newer Java
- **Applet API: resolved.** No longer blocks JDK 26+. Remaining javac warnings are plain deprecations (`new URL(String)`, `InputEvent.getModifiers`/`BUTTON*_MASK`) in `GraphPnl`; not removals, but worth modernising.
- `GraphPnl extends java.awt.Panel` — works, but legacy AWT; fine for now.
- No third-party libraries, so nothing to upgrade. README mentioned JUnit/Maven (`mvn eclipse:eclipse`) — no tests or pom exist.
- `DeanFly.mainInit()` points at dead host `www.dean.seetech.com` when `GraphPnl.runlocal` is false (it is true by default).
- Doxyfile (v1.8.7 format) has obsolete tags and malformed `ALIASES`; still builds. `Doxygen/warning.log` is stale (old Windows path).
- Duplicate world data under `web/` and `web/WEB-INF` plus many `.bak` files.

## Next steps / ideas
- Optional: `doxygen -u` to modernise `lld.doxyfile`.
- Optional: add smoke test that loads both worlds headless.
