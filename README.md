# ExtensivePistons

A Paper 26.3 plugin powered by [MixinLoader](https://github.com) that enhances piston mechanics with pushable tile entities, configurable push limits, Bedrock-style double chest splitting/rejoining, quasi-connectivity and block-spitting toggles, animation speed multipliers, claim border protection, and custom block whitelists/blacklists.

---

## Features

- **Pushable & Retractable Tile Entities:** Move chests, barrels, hoppers, furnaces, dispensers, droppers, shulker boxes, and other block entities without losing inventory contents or NBT data.
- **Bedrock-Style Double Chests:** Automatically kicks viewing players before moving, cleanly splits double chests when one half is pushed or pulled, and rejoins adjacent single chests facing the same direction.
- **Configurable Push Limit:** Increase or decrease the maximum piston push/pull block limit (default `64`, vanilla `12`).
- **Anti-Lag Tile Entity Cap:** Limit how many tile entities can be moved in a single piston action (`max-tile-entities-per-push`).
- **Quasi-Connectivity & Block-Spitting Toggles:** Optionally disable piston quasi-connectivity (`disable-qc`) or sticky piston 1-tick block spitting (`spit-block`) for Bedrock-like redstone behavior.
- **Piston Animation Speed:** Adjust piston extension and retraction speed (`piston-speed`).
- **Claim & Spawn Border Protection:** Prevent pistons from pushing or pulling blocks across spawn protection or claim boundaries (`respect-claim-borders`).
- **Custom Whitelist & Blacklist:** Force normally immovable blocks (such as obsidian, reinforced deepslate, or bedrock) to be pushable, or protect sensitive blocks (such as spawners, portal frames, and command blocks).
- **World Height & Border Safety:** Prevents pistons from pushing blocks past build limits or outside the world border.

---

## Requirements

- **Server:** Paper 26.3 (Java 25+)
- **Dependency:** [MixinLoader](https://github.com)

---

## Installation

1. Download the latest `MixinLoader-<version>.jar` from the [MixinLoader Releases](https://github.com/releases) page.
2. Download **`ExtensivePistons-<version>-all.jar`** (make sure to use the `-all` JAR).
3. Place both JAR files into your server's `plugins/` folder.
4. Start your server normally (`java -jar server.jar`).
   - *Note: On first startup without a `-javaagent` flag, MixinLoader will automatically restart the JVM once to attach its Mixin agent.* (On some versions of MixinLoader, you may have to put MixinLoader in the plugins/ AND launch the server.jar manually with -javaagent:MixinLoader-\<version\>.jar)
   - *(Optional)* To skip the automatic restart, add `-javaagent:plugins/MixinLoader-<version>.jar` before `-jar server.jar` in your startup script.

---

## Commands & Permissions

**Base Command:** `/extensivepistons` (Alias: `/ep`)  
**Permission:** `extensivepistons.admin` (default: `op`)

| Command | Description |
| :--- | :--- |
| `/ep reload` | Reloads `config.yml` from disk |
| `/ep info` | Displays all active settings |
| `/ep set <setting> <value>` | Updates `push-limit`, `max-tile-entities-per-push`, or `piston-speed` |
| `/ep toggle <setting>` | Toggles `pushable-tile-entities`, `disable-qc`, `spit-block`, `respect-claim-borders`, `whitelist-on`, or `blacklist-on` |
| `/ep whitelist <add\|remove\|info> [block]` | Manages or views force-pushable block overrides |
| `/ep blacklist <add\|remove\|info> [block]` | Manages or views immovable block overrides |

---

## Configuration (`config.yml`)

```yaml
# ==============================================================================
# ExtensivePistons Configuration
# ==============================================================================
# Reload changes in-game with: /extensivepistons reload
#
# Vanilla Reference Settings:
#   pushable-tile-entities: false
#   push-limit: 12
#   max-tile-entities-per-push: 0
#   disable-qc: false
#   spit-block: true
#   piston-speed: 1.0
#   respect-claim-borders: false
#   whitelist-on: false
#   blacklist-on: true
# ==============================================================================

# Allows pistons to push and pull blocks that store data (chests, barrels,
# hoppers, furnaces, shulker boxes, etc.) while preserving all contents and NBT.
# Double chests automatically split when one half is moved and rejoin when pushed back.
pushable-tile-entities: true

# Maximum number of blocks a single piston can push or pull at once.
# Vanilla default: 12
push-limit: 64

# Maximum number of tile entities (chests, hoppers, shulker boxes, etc.) allowed
# in a single piston push or pull. Prevents server lag spikes from massive storage movers.
# Set to 0 to disallow moving tile entities while keeping the increased push-limit.
max-tile-entities-per-push: 16

# Disables Quasi-Connectivity (QC) on pistons so they only react to direct redstone power
# (similar to Minecraft Bedrock Edition).
# Vanilla Java default: false
disable-qc: false

# Whether sticky pistons drop ("spit") their block when given a 1-tick redstone pulse.
# Set to false for Bedrock-style behavior where sticky pistons always pull their block back.
# Vanilla Java default: true
spit-block: true

# Multiplier for piston extension/retraction animation speed.
# Higher values (e.g., 2.0) complete piston movements faster; lower values slow them down.
# Vanilla default: 1.0
piston-speed: 1.0

# Prevents pistons from pushing or pulling blocks across spawn protection or
# claim boundaries (WorldGuard, GriefPrevention, Towny, etc.).
# Vanilla default: false
respect-claim-borders: true

# ------------------------------------------------------------------------------
# Block Whitelist (Force-Pushable Overrides)
# ------------------------------------------------------------------------------
# When enabled, any block listed below can be pushed and pulled by pistons—even
# if vanilla Minecraft normally marks it as immovable or unbreakable.
# Whitelisted blocks take priority over the blacklist below.
whitelist-on: true
whitelist:
  - minecraft:obsidian
  - minecraft:reinforced_deepslate
  - minecraft:bedrock

# ------------------------------------------------------------------------------
# Block Blacklist (Immovable Overrides)
# ------------------------------------------------------------------------------
# When enabled, any block listed below is strictly prevented from being pushed
# or pulled by pistons (unless it is also present in the whitelist above).
# By default, this protects operator/creative blocks, spawners, and portal frames.
blacklist-on: true
blacklist:
  - minecraft:spawner
  - minecraft:trial_spawner
  - minecraft:vault
  - minecraft:end_portal_frame
  - minecraft:command_block
  - minecraft:repeating_command_block
  - minecraft:chain_command_block
  - minecraft:structure_block
  - minecraft:jigsaw
```

---

## Building from Source

```bash
git clone https://github.com
cd ExtensivePistons
./gradlew build
```

The compiled plugin JARs will be output to `build/libs/` (use the `-all.jar` file on your server).