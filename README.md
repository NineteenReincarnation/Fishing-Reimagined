# Fishing Reimagined

Fishing Reimagined is a vanilla-oriented fishing rework for Minecraft Java Edition.

## Development line

- Minecraft: **26.2**
- Loaders: **Fabric + NeoForge**
- Java: **25**
- Mod version: **1.1.0+26.2**
- Development branch: **main**

The project is currently in prototype development. The shared fight engine is implemented first; Minecraft hook/entity integration and presentation layers follow on top of that stable core.

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

- `common/` — loader-independent gameplay model and shared code
- `fabric/` — Fabric bootstrap and packaging
- `neoforge/` — NeoForge bootstrap and packaging
- `docs/` — architecture, fight-engine and versioning notes

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Fight engine](docs/FIGHT_ENGINE.md)
- [Versioning](docs/VERSIONING.md)
- [Validation status](docs/VALIDATION_STATUS.md)

## License

MIT. See [LICENSE](LICENSE).
