# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.21+26.2**
- Development branch: **main**

The current prototype uses a centered horizontal catch-bar fight, large world-space fish movement, a persistent rope renderer, and a live-fish landing sequence.

## Controls

Cast normally and wait for the vanilla bite. The fight starts automatically when the fish bites.

- **Hold use (default right mouse):** accelerate the catch zone to the right.
- **Release use:** accelerate the catch zone to the left.
- Keep the fish marker inside the catch zone to build Catch Progress.
- Brief misses are tolerated; staying outside the zone causes progress to fall.
- If progress reaches zero and control is not recovered for a while, the fish escapes.

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
