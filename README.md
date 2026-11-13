# Lunacy

A Fabric mod for Minecraft 1.21.1 that makes the moon matter. Full moon nights are a hunt: more monsters, and they all know where you are. New moon nights bring shooting stars worth going out for.

Lunacy is **server-side only**. Install it on a server and vanilla clients can join. It also works in singleplayer. The moon phase is vanilla's 8-day cycle (day number mod 8), and everything happens in the Overworld only.

## Features

### Full moon: the hunt
From dusk to dawn on a full moon night (moon phase 0), the surface becomes dangerous.

- **More monsters under the open sky.** The monster mob cap is doubled. The extra mobs only spawn where the sky is visible. Caves, roofed bases and lit areas spawn exactly as in vanilla, and torches still protect.
- **They know where you are.** Zombies, skeletons, creepers, spiders and witches out in the open within 64 blocks of you come straight for you, whether or not they can see you.
  - They don't give up or lose track over the distance.
  - Walls still protect you: skeletons and witches need line of sight to shoot, and creepers need it to explode. A sealed base keeps you safe, but expect a crowd at the door.
  - Mobs follow you into caves you can walk into. Mobs already underground don't join the hunt.
  - Creative and spectator players are left alone. Nothing happens on Peaceful.

### New moon: shooting stars
On new moon nights (moon phase 4), shooting stars fall from the sky.

- **About 3 stars per player per night,** at random times. Players close together share stars rather than multiplying them.
- **The star is the drop.** A glowing echo shard falls from about 120 blocks up, trailing sparks, and takes about 4–5 seconds to come down. It lands 32–96 blocks from you, on the surface.
- **Visible from far away.** The trail can be seen from up to 512 blocks, and the glowing outline shows the shard through terrain, so you can find it in the dark.
- **Impact:** a flash, a burst of sparks, and a sonic boom with a crack and a chime.
- **Collecting it:** the shard can't be picked up until it lands, and it doesn't despawn.
- **It fades at sunrise.** Shards still on the ground vanish with a puff when day breaks. A shard you've picked up is yours to keep.

### Echo shards for XP
Right-click any echo shard to break it for **25 experience points**, one shard per click. Creative players keep the shard. It breaks with a crystal shatter and a burst of shard pieces. Recovery compass crafting is unaffected, so keep a few if you want one.

### Creepers ignore monsters
Creepers never target other monsters. In vanilla, a creeper hit by a stray skeleton arrow turns on the skeleton and blows up everything nearby. This applies every night, not just full moons. All other infighting works as in vanilla.

### Moonrise messages (optional)
An action bar line can greet each full moon ("The full moon rises. Something stirs in the dark.") and new moon ("The sky goes dark. Stars begin to fall."). Both are off by default.

## Configuration

The settings live in `config/lunacy.json`, which is created with the defaults on first launch. With [Cloth Config](https://modrinth.com/mod/cloth-config) and [Mod Menu](https://modrinth.com/mod/modmenu) installed, you can also edit them in-game, and changes apply right away. Both mods are optional. The in-game screen changes the config of the game it runs in: your singleplayer world, not a remote server.

| Option | Default | Description |
|---|---|---|
| `fullMoonEvents` | `true` | Turn all full moon features on or off. |
| `fullMoonMonsterCapMultiplier` | `2.0` | Monster cap multiplier on full moon nights. |
| `fullMoonSkyOnly` | `true` | Extra spawns only where the sky is visible. |
| `fullMoonHunt` | `true` | Hunting mobs always know where nearby players are. |
| `fullMoonHuntRadius` | `64` | How far away, in blocks, hunting mobs are drawn in from. |
| `creepersIgnoreMonsters` | `true` | Creepers never target other monsters (every night). |
| `fullMoonMessage` | `false` | Action bar message when a full moon rises. |
| `newMoonEvents` | `true` | Turn shooting stars on or off. |
| `newMoonMessage` | `false` | Action bar message when a new moon night starts. |
| `starsPerNight` | `3.0` | Average shooting stars per player per new moon night. |
| `starMinDistance` / `starMaxDistance` | `32` / `96` | Landing distance from the player. |
| `starDropsFadeAtDawn` | `true` | Unclaimed star drops vanish at sunrise. |
| `echoShardXp` | `25` | XP from right-clicking an echo shard. `0` turns it off. |

### Datapacks
- **Star loot:** `lunacy:gameplay/shooting_star` (loot table). By default it gives 1 echo shard.
- **Which mobs hunt on full moons:** `#lunacy:full_moon_hunters` (entity type tag). By default: zombie, zombie villager, husk, skeleton, stray, bogged, creeper, spider and witch.

## Commands

- `/lunacy star` drops a shooting star near you right away. Ops only; useful for testing.

To test the moon nights:
- Full moon night: `/time set 13000`.
- New moon night: `/time set 109000`.
- Sunrise after it: `/time set 119500`.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.20.1 or later
- Fabric API
- Java 21
- Optional: Cloth Config and Mod Menu, for the in-game config screen

## Building

```bash
./gradlew build      # the jar ends up in build/libs/
./gradlew runClient  # dev client (includes Cloth Config and Mod Menu)
./gradlew runServer  # dev server
```

## License

CC0-1.0. See [LICENSE](LICENSE).
