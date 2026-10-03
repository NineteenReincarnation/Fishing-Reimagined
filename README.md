# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.24+26.2**
- Development branch: **main**

The current prototype uses a centered horizontal catch-bar fight, one authoritative HUD/world fish motion, a persistent rope renderer, and a post-catch hanging presentation that keeps the fish on the rod line until the player takes it down.

## Special fishing mode

Special fishing is **off by default**. With it disabled, fishing uses vanilla behavior.

- **Toggle key:** `.` (period) by default. It is a normal Minecraft key mapping and can be rebound in Controls.
- The toggle is persisted in `config/fishing_reimagined.json` as `specialFishingEnabled`.
- The client synchronizes the setting to the server for the current player.

When special fishing is enabled, cast normally and wait for the vanilla bite. The horizontal fight starts automatically when the fish bites.

- **Hold use (default right mouse):** accelerate the catch zone to the right.
- **Release use:** accelerate the catch zone to the left.
- Keep the fish marker inside the catch zone to build Catch Progress.
- Brief misses are tolerated; staying outside the zone causes progress to fall.
- If progress reaches zero and control is not recovered for a while, the fish escapes.
- A successful special catch awards the vanilla-style **1–6 experience points**. The fish is first pulled out of the water and hangs from the rod line; use the rod again to take it off the line and materialize the normal live fish.

The player is not expected to read fish states. Fish behavior only changes the movement rhythm.

## Build

Build both loader targets:

```bash
./gradlew buildAll
```

Windows PowerShell:

```powershell
.\gradlew.bat buildAll
```

Build one loader:

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
```

Artifacts are written to `fabric/build/libs/` and `neoforge/build/libs/`.

## Project layout

- `common/` — shared gameplay model, Minecraft bridge, mixins, visual state and network payloads
- `fabric/` — Fabric bootstrap, networking and packaging
- `neoforge/` — NeoForge bootstrap, networking and packaging
- `docs/` — architecture, fight-engine and versioning notes

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Fight engine](docs/FIGHT_ENGINE.md)
- [Horizontal catch-bar design](docs/RHYTHM_FISHING_DESIGN.md)
- [Versioning](docs/VERSIONING.md)
- [Validation status](docs/VALIDATION_STATUS.md)
- [Playtest protocol](docs/PLAYTEST_PROTOCOL.md)

## License

MIT. See [LICENSE](LICENSE).


## Animation resource-pack compatibility

Fishing Reimagined deliberately leaves player and held-item pose animation to external animation systems and resource packs. The mod no longer injects its own first-person fishing-rod transform.

The hooked fish remains a vanilla cod, salmon, pufferfish or tropical-fish entity on the client, so entity-model packs can replace and animate those vanilla entity models. Fishing Reimagined controls only the authoritative world-space anchor, movement direction and a small whole-entity orientation needed to keep the fish attached to the line.

This is intended to cooperate with EMF/ETF-based packs such as Fresh Animations and player-animation extensions instead of overriding their model-part animations.
