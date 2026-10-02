# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.1+26.2**
- Development branch: **main**

The current prototype connects the shared fight simulation to vanilla fishing on both supported loaders.

## Controls

After the vanilla bite, use the rod once to set the hook and enter the fight.

- **Left mouse:** reel in
- **Right mouse:** pay line out
- **Neither / both:** hold

The server owns the fight state. Excess slack can let the fish escape and sustained overload can break the line.

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

- `common/` — shared gameplay model, Minecraft bridge, mixins and network payloads
- `fabric/` — Fabric bootstrap, networking and packaging
- `neoforge/` — NeoForge bootstrap, networking and packaging
- `docs/` — architecture, fight-engine and versioning notes

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Fight engine](docs/FIGHT_ENGINE.md)
- [Versioning](docs/VERSIONING.md)
- [Validation status](docs/VALIDATION_STATUS.md)

## License

MIT. See [LICENSE](LICENSE).
