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
- 2026-11-01: Fixed deprecations in `GraphPnl`: `new URL(String)` -> `new URI(..).toURL()`; mouse handlers use `getButton()` / `getModifiersEx()` + `BUTTON1_DOWN_MASK` instead of `getModifiers()`/`BUTTON*_MASK`. Both worlds still launch cleanly. Mouse-button cheat controls not manually clicked-tested.
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
- Optional: add smoke test that loads both worlds headless.
