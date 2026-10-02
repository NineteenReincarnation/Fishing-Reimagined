# Validation status

## Implemented

- Fabric/NeoForge project split for Minecraft 26.2.
- Java 25 build target.
- Shared loader-neutral fight engine.
- Configurable fish and line profiles.
- Pluggable per-fight fish behavior API.
- Vanilla bite-to-fight interception.
- Server-authoritative reel input on Fabric and NeoForge.
- Left-mouse reel-in and right-mouse pay-out input mapping.
- Catch, escape and line-break terminal states.
- Lightweight temporary `HookedFish` fight object.
- Synced active-fight, fish-kind, tension, progress, stamina and fish-state data.
- Client-side hooked-fish visual proxies using vanilla fish models.
- Fight HUD with line tension, landing progress, control state and fish condition.
- Dynamic fishing-line sag driven by live tension.
- Successful conversion into a persistent live vanilla fish entity.
- Vanilla fish land-flop behavior after the catch is launched out of the water.
- Unit tests for basic line-state transitions.

## Build validation

GitHub Actions compiles and packages both loader targets and runs the Fabric-hosted JUnit suite.

## Runtime validation still required

- Client launch on Fabric.
- Client launch on NeoForge.
- Dedicated-server connection on both loaders.
- Visual hooked-fish lifecycle and cleanup.
- HUD placement at different GUI scales.
- Continuous left/right mouse input under normal latency.
- Dynamic line rendering under shader/resource-pack combinations.
- Hook movement around shorelines and small pools.
- Live-fish launch trajectory and landing behavior.
- Multiplayer ownership and disconnect edge cases.
- In-game balance and feel.

## Presentation still incomplete

- Rod bending is not implemented yet.
- Fishing-line block collision is not implemented yet.
- Species-specific fight animation is still limited to vanilla model animation plus movement state.
- Audio feedback for slack, strain and burst runs is not implemented yet.

A successful compile/package run is not evidence that the in-game loop or presentation is complete or balanced.
