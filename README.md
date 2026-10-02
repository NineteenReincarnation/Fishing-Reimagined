# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.20+26.2**
- Development branch: **main**

The current prototype uses a centered two-bar fight HUD, large fish movement, a persistent rope renderer, and a rhythm-based tension fight.

## Controls

Cast normally and wait for the vanilla bite. The fight starts automatically when the fish bites.

- **Hold use (default right mouse):** reel in and raise tension.
- **Release use:** let tension fall.
- **Useful middle tension:** Catch Progress grows.
- **Too loose for too long:** Catch Progress slips backward.
- **Red tension zone:** snap pressure builds; release to recover before the line breaks.

The player is not expected to read fish states. Fish behavior changes the timing of the tension movement, while the rule stays the same: judge the rhythm and choose hold or release.

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
- [Reference-style rebuild](docs/REFERENCE_STYLE_REBUILD.md)
- [Rhythm fishing design](docs/RHYTHM_FISHING_DESIGN.md)
- [Versioning](docs/VERSIONING.md)
- [Validation status](docs/VALIDATION_STATUS.md)
- [Playtest protocol](docs/PLAYTEST_PROTOCOL.md)

## License

MIT. See [LICENSE](LICENSE).
