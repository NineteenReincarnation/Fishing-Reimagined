# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.19+26.2**
- Development branch: **main**

The current prototype connects the shared fight simulation to vanilla fishing on both supported loaders and exposes the fight state through a centered, compact HUD and large fish movement.

## Controls

Cast normally and wait for the vanilla bite. The fight starts automatically when the fish bites.

- **Hold use (default right mouse):** reel in and build catch progress.
- **Release use:** lower line tension.
- **Red tension zone:** sustained overload snaps the line.

During a fight the centered HUD shows line tension and catch progress. The fish visual, rod pose, rope rendering and sound feedback continue to react to the fight state.

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
- [Versioning](docs/VERSIONING.md)
- [Validation status](docs/VALIDATION_STATUS.md)
- [Playtest protocol](docs/PLAYTEST_PROTOCOL.md)

## License

MIT. See [LICENSE](LICENSE).
