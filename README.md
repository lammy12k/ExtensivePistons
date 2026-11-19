# ExtensivePistons

A Paper 26.3 plugin powered by [MixinLoader](https://github.com/Chest-Solutions/MixinLoader) that enhances piston mechanics with pushable tile entities, configurable push limits, Bedrock-style double chest splitting/rejoining, quasi-connectivity toggles, and custom block whitelists/blacklists.

---

## Features

- **Pushable & Retractable Tile Entities:** Move chests, barrels, hoppers, furnaces, dispensers, droppers, shulker boxes, and other block entities without losing inventory contents or NBT data.
- **Bedrock-Style Double Chests:** Automatically kicks viewing players before moving, cleanly splits double chests when one half is pushed or pulled, and rejoins adjacent single chests facing the same direction.
- **Configurable Push Limit:** Increase or decrease the maximum piston push/pull block limit (default `64`, vanilla `12`).
- **Anti-Lag Tile Entity Cap:** Limit how many tile entities can be moved in a single piston action (`max-tile-entities-per-push`).
- **Custom Whitelist & Blacklist:** Force normally immovable blocks (such as obsidian, reinforced deepslate, or bedrock) to be pushable, or protect sensitive blocks (such as spawners, portal frames, and command blocks).
- **Quasi-Connectivity Toggle:** Optionally disable piston quasi-connectivity (`disable-qc`) for Bedrock-like redstone behavior.
- **World Height & Border Safety:** Prevents pistons from pushing blocks past build limits or outside the world border.

---

## Requirements

- **Server:** Paper 26.3 (Java 25+)
- **Dependency:** [MixinLoader](https://github.com/Chest-Solutions/MixinLoader)

---

## Installation

1. Download the latest `MixinLoader-<version>.jar` from the [MixinLoader Releases](https://github.com/releases) page.
2. Download `ExtensivePistons-<version>.jar`.
3. Place both JAR files into your server's `plugins/` folder.
4. Start your server normally (`java -jar server.jar`).
   - *Note: On first startup without a `-javaagent` flag, MixinLoader will automatically restart the JVM once to attach its Mixin agent.*
   - *(Optional)* To skip the automatic restart, add `-javaagent:plugins/MixinLoader-<version>.jar` before `-jar server.jar` in your startup script.

---

## Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/extensivepistons reload` | Reloads `config.yml` from disk | `extensivepistons.admin` (default: `op`) |
| `/extensivepistons info` | Displays all active settings | `extensivepistons.admin` (default: `op`) |
| `/extensivepistons set <setting> <value>` | Updates `push-limit` or `max-tile-entities-per-push` | `extensivepistons.admin` (default: `op`) |
| `/extensivepistons toggle <setting>` | Toggles `pushable-tile-entities`, `disable-qc`, `whitelist-on`, or `blacklist-on` | `extensivepistons.admin` (default: `op`) |

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
git clone https://github.com/lammy12k/ExtensivePistons
cd ExtensivePistons
./gradlew build
```

The compiled plugin JAR will be output to `build/libs/`.
