# Changelog

All notable changes to **ExtensivePistons** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com),
and this project adheres to [Semantic Versioning](https://semver.org).

## - 2026-11-20

### Added
- **Pushable & Retractable Tile Entities (`pushable-tile-entities`):** Support for pushing and pulling block entities (chests, barrels, hoppers, furnaces, dispensers, droppers, shulker boxes, etc.) while preserving full inventory contents and NBT data.
- **Bedrock-Style Double Chest Mechanics:**
  - Automatically closes open container inventories for all viewing players prior to movement.
  - Cleanly splits double chests when one half is moved without spilling or deleting items.
  - Automatically rejoins adjacent single chests facing the same direction into a double chest.
- **Customizable Push Limit (`push-limit`):** Configurable piston push and pull block limit (defaults to `64`).
- **Anti-Lag Tile Entity Cap (`max-tile-entities-per-push`):** Configurable limit on the number of tile entities moved in a single piston action (defaults to `16`).
- **Block Whitelist & Blacklist (`whitelist-on` / `blacklist-on`):**
  - Force-pushable block overrides (defaults to `minecraft:obsidian`, `minecraft:reinforced_deepslate`, and `minecraft:bedrock`).
  - Immovable block protections (defaults to spawners, trial spawners, vaults, end portal frames, command blocks, structure blocks, and jigsaws).
- **Quasi-Connectivity Toggle (`disable-qc`):** Option to disable piston quasi-connectivity for Bedrock-style redstone behavior.
- **Block-Spitting Toggle (`spit-block`):** Option to prevent sticky pistons from dropping their attached block on 1-tick redstone pulses.
- **Piston Animation Speed (`piston-speed`):** Configurable multiplier for piston extension and retraction speed.
- **Claim & Spawn Border Protection (`respect-claim-borders`):** Prevents pistons from pushing or pulling blocks across spawn protection or region/claim boundaries.
- **World Height & Border Safety:** Blocks pistons from pushing structures past build limits or outside the world border.
- **Cloud Paper Command Suite (`/extensivepistons`, `/ep`):** Added `reload`, `info`, `set`, `toggle`, `whitelist`, and `blacklist` commands with tab-completion (`extensivepistons.admin`).
