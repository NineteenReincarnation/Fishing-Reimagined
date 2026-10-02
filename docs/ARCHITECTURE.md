# Architecture

## Source ownership

Fishing Reimagined uses a small multi-loader layout.

- `common/` contains loader-independent fight rules plus Minecraft-facing code that is identical on both loaders.
- `fabric/` contains Fabric bootstrap, packet registration and Fabric-specific client sending.
- `neoforge/` contains NeoForge bootstrap, packet registration and NeoForge-specific client sending.

Shared code must not import Fabric or NeoForge APIs.

## Fight boundary

The numerical fight simulation remains in `fight/`. It has no dependency on loader APIs and does not own rendering.

`HookedFish` is the temporary fishing object selected after a bite. It owns the fight session and lightweight spatial intent. A successful terminal state materializes a normal vanilla fish entity; the temporary object is then discarded with the fishing hook.

The vanilla `FishingHook` is used as the synchronized anchor and lifecycle carrier through a mixin. This avoids introducing a permanent custom mob before the temporary-fish presentation layer needs one.

## Input and authority

Client mouse state is reduced to three actions: `REEL_IN`, `PAY_OUT`, and `HOLD`. Loader-specific networking sends only action changes.

The server advances the fight and owns catch, escape, line-break, rod damage and live-fish creation. Client-side code only suppresses conflicting vanilla attack/use handling and sends intent.

## Extension surfaces

- `FishBehavior` / `FishBehaviorSession` — replace or extend fish fight behavior.
- `FishProfile` — per-fish tuning.
- `LineProfile` — line and reel tuning.
- `HookedFishKind` — current vanilla-fish materialization boundary; replaceable by a data-driven registry later.
- `FishingHookFightAccess` — narrow bridge used by rod and client integration.

Rendering, rod flex, physical-line presentation and species-specific animation remain presentation work on top of the synchronized fight state.
