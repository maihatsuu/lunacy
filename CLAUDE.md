# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Lunacy is a Fabric mod for Minecraft 1.21.1 that makes the moon phase matter: full moon nights raise surface hostile spawning, new moon nights spawn shooting stars that drop loot. The project is currently the unmodified Fabric example-mod template (`ExampleMixin`, `ExampleClientMixin`, placeholder `fabric.mod.json` metadata); the feature work has not started yet.

**The design spec lives outside this repo at `../Lunacy.md`.** Read it before implementing features. It defines the behavior, the config options and defaults (`config/lunacy.json`), the loot table id (`lunacy:gameplay/shooting_star`), the open design questions, and the manual test plan.

Key constraints from the spec:
- Server-side gameplay must work with **vanilla clients**. Only optional polish may need the client mod, so keep gameplay logic in `src/main`, not `src/client`.
- Overworld only by default. Moon phase comes from `level.getMoonPhase()` (0 = full, 4 = new; phase = day number mod 8).
- Full moon: the extra monster cap only allows spawns above the vanilla cap at `level.canSeeSky(pos)` positions. Normal light rules still apply.
- New moon: particles are sent with `force = true`. Star drops glow, don't despawn normally, and vanish at sunrise if no one has picked them up. Stars don't damage blocks.

## Build & run

Requires Java 21 (CI builds on JDK 25 targeting release 21).

```sh
./gradlew build          # compile + remap; jar in build/libs/
./gradlew runClient      # launch dev client
./gradlew runServer      # launch dev dedicated server (use this to confirm vanilla-client compatibility)
./gradlew genSources     # decompile Minecraft sources for browsing
```

There are no tests configured. Verify manually in-game using the spec's Testing section, e.g. full moon night `/time set 13000`, new moon night `/time set 109000`.

## Architecture / conventions

- **Mappings: official Mojang mappings** (`loom.officialMojangMappings()`), not Yarn. Use Mojang names (`ServerLevel`, `ResourceLocation`, `Level#canSeeSky`, etc.).
- **Split source sets** (`splitEnvironmentSourceSets()`): `src/main` is common/server code and `src/client` is client-only code. Client code can reference main, but main must never reference client classes.
- Entrypoints: `maihatsuu.lunacy.Lunacy` (main) and `maihatsuu.lunacy.client.LunacyClient` (client), registered in `src/main/resources/fabric.mod.json`.
- Mixins: common ones go in `maihatsuu.lunacy.mixin`, registered in `lunacy.mixins.json`. Client ones go in `maihatsuu.lunacy.client.mixin`, registered under the `"client"` array of `lunacy.client.mixins.json`. `defaultRequire: 1` means an injector that fails to match crashes at load.
- Use `Lunacy.id(path)` for `lunacy:` resource locations and `Lunacy.LOGGER` for logging.
- Versions (Minecraft, loader, Loom, Fabric API) are in `gradle.properties`. `fabric.mod.json`'s `${version}` is filled in from there by `processResources`.
