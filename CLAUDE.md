# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Lunacy is a Fabric mod for Minecraft **1.21.1** (Java 21, Fabric Loader 0.20.1). Mod id `lunacy`, base package `maihatsuu.lunacy`. Full moon nights bring more surface monsters; new moon nights bring shooting stars.

The design spec is `../Lunacy.md`, one directory up from this repo (not committed). Decisions made on its open questions:
- **Star drop:** loot table `lunacy:gameplay/shooting_star`, which is just 1 echo shard (no XP orbs). **Any** echo shard can be right-clicked for `echoShardXp` (25) XP, one per click.
- **Full moon:** only a monster cap multiplier (×2), with extra spawns limited to sky-visible spots. Mob gear and effects stay vanilla.
- **Moonrise messages:** separate full moon and new moon toggles, both **off** by default.
- **Overworld only.** "Night" is time of day 13000–23000. This matches Rebalanced Core's full-moon vault recharge (`NIGHT_START = 13000`).
- **Server-side gameplay:** unmodded clients can join. All gameplay is in `src/main`, and the only client code is the optional config screen. Player-facing text uses `Component.translatableWithFallback` so unmodded clients still see English.

## How the pieces fit

- `MoonPhase`: `isNight` / `isFullMoonNight` / `isNewMoonNight` (each Overworld-only) and `nightIndex` (= day number). Everything uses these.
- **Full moon surge:** `mixin/NaturalSpawnerMixin` + `mixin/SpawnStateMixin` + `mixin/MobCountsMixin`, with shared static state in `spawn/FullMoonSurge`.
  1. In `NaturalSpawner.spawnForChunk`, a `@WrapOperation` on `SpawnState.canSpawnForCategory` first runs the vanilla check.
  2. If that fails for MONSTER during a full moon, it sets `FullMoonSurge.capMultiplier` and runs the check again. The other two mixins scale both `getMaxInstancesPerChunk()` reads: the global cap and the per-player `LocalMobCapCalculator$MobCounts` cap.
  3. If only the second check passes, `skyOnly` is set. A second `@WrapOperation` on `spawnCategoryForChunk` then wraps the `SpawnPredicate` with `level.canSeeSky(pos)`.

  As a result, caves and roofed areas spawn exactly as in vanilla. Static state is safe because natural spawning runs on the server thread.
- `LunarEvents`: `END_WORLD_TICK` edge detection for the moonrise action-bar messages.
- **Shooting stars:** `star/ShootingStars` + `star/FallingStar`, ticked from `END_WORLD_TICK` in the Overworld.
  - **Spawn chance:** each player rolls `starsPerNight / 10000 / (players within starMaxDistance)` every tick. That's how nearby players share stars.
  - **The star is the drop:** the loot table is rolled at launch, and each stack spawns as a glowing, never-despawning `ItemEntity` 120 blocks above the landing spot. It starts up to 20 blocks to the side so item drag drifts it onto the spot. Vanilla item physics make the fall take about 4–5 s.
  - **While falling:** it has the `lunacy.star_falling` tag and `setNeverPickUp()`. Each tick a particle trail is drawn behind it with `sendParticles(player, …, force = true, …)`, which reaches 512 blocks. Drops that reload mid-fall are re-tracked through `ServerEntityEvents.ENTITY_LOAD`.
  - **Landing:** on ground or fluid, it gets a normal pickup delay plus the impact flash and sounds.
  - **Fade at dawn:** drops carry `lunacy.star_drop` + `lunacy.star_night.<nightIndex>`, and a drop is stale unless it's night with the same index. A sweep every 20 ticks, plus `ENTITY_LOAD` for chunks that load later, puff them away. Discards found during entity load are queued for the next tick.
- `LunacyCommand`: `/lunacy star` (permission level 2) drops a star near the caller right away, for testing.
- `EchoShardXp`: `UseItemCallback` (1.21.1 signature returning `InteractionResultHolder`) gives XP and consumes one shard on the server.
- `config/LunacyConfig`: a Gson POJO at `config/lunacy.json`, loaded in `onInitialize` and read live through `LunacyConfig.get()`. `save()` sanitizes the values and writes them out.
- **Client config screen (optional):**
  - `client/compat/ModMenuIntegration` (the `modmenu` entrypoint) opens `client/config/LunacyConfigScreen` (Cloth Config).
  - Cloth Config and Mod Menu are `modCompileOnly` + `modLocalRuntime` and listed under `suggests`. The integration checks `isModLoaded("cloth-config")` before touching any Cloth class.
  - Labels and tooltips are in `assets/lunacy/lang/en_us.json` under `config.lunacy.*`.
  - Editing the screen on a client only changes that client's (or singleplayer's) config.

## Commands

```sh
./gradlew build          # compile + remap; jar in build/libs/
./gradlew runClient      # dev client (Cloth Config + Mod Menu included)
./gradlew runServer      # dev dedicated server; join with an unmodded 1.21.1 client to check compatibility
./gradlew genSources     # decompile Minecraft sources for browsing
```

There is no test suite, and CI only runs `./gradlew build`. Test in-game:
- Full moon night: `/time set 13000`.
- New moon night: `/time set 109000`. Raise `starsPerNight` to see stars quickly.
- Sunrise after the new moon: `/time set 119500`.

## Build and layout notes

- **Mappings are official Mojang mappings**, not Yarn. 1.21.1 still uses `MobSpawnType` and `getMinBuildHeight`/`getMaxBuildHeight`. Data folders use singular names (`loot_table/`).
- Source sets are split (`splitEnvironmentSourceSets()`): `main` must never reference `net.minecraft.client.*`.
- Mixins are listed in `src/main/resources/lunacy.mixins.json`. `defaultRequire: 1` means an injection that fails to match crashes the game at load. MixinExtras (`@WrapOperation`, `@ModifyExpressionValue`, `@Local`) comes bundled with Fabric Loader.
- Use `Lunacy.id(path)` for `lunacy:` ids and `Lunacy.LOGGER` for logging. Versions live in `gradle.properties`.
