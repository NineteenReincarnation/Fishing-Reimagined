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
- Lightweight temporary `HookedFish` object.
- Synced active-fight, fish-kind and line-tension state on the vanilla hook.
- Successful conversion into a persistent live vanilla fish entity.
- Vanilla fish land-flop behavior after the catch is launched out of the water.
- Unit tests for basic line-state transitions.

## Build validation

GitHub Actions compiles and packages both loader targets and runs the Fabric-hosted JUnit suite.

## Runtime validation still required

- Client launch on Fabric.
- Client launch on NeoForge.
- Dedicated-server connection on both loaders.
- Bite interception and transition into a fight in a real world.
- Continuous left/right mouse input under normal latency.
- Hook movement around shorelines and small pools.
- Live-fish launch trajectory and landing behavior.
- Multiplayer ownership and disconnect edge cases.
- In-game balance and feel.

## Presentation not implemented yet

- Hooked-fish model during the fight.
- Species-specific fight animation.
- Rod bending.
- Physical line sag/tension animation and sound feedback.

A successful compile/package run is not evidence that the in-game loop or presentation is complete or balanced.
