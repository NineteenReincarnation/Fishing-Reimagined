# Architecture

## Source ownership

Fishing Reimagined uses a small multi-loader layout.

- `common/` contains loader-independent gameplay state, rules and extension points.
- `fabric/` contains Fabric bootstrap, metadata and Fabric-specific integration.
- `neoforge/` contains NeoForge bootstrap, metadata and NeoForge-specific integration.

Both loader projects compile the shared `common` source directly. Shared code must not import Fabric or NeoForge APIs.

## Integration boundary

Minecraft-facing code adapts game state into the shared model instead of moving loader details into the fight engine.

Dependency direction: `Minecraft / loader bridge -> shared fight engine`.

The shared fight engine does not own rendering, networking, entity registration or input hooks.

## Planned bridge surfaces

The implementation should grow through narrow adapters for player reel input, fishing-hook lifecycle, temporary hooked-fish spatial state, line presentation state, and successful conversion into a normal fish entity.

The current code establishes the loader split and deterministic fight-state core. It does not claim in-game integration yet.
