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

The vanilla `FishingHook` is used as the synchronized anchor and lifecycle carrier through a mixin.

## Visual boundary

The temporary fish remains server-side logic rather than a normal persistent mob. The client creates a short-lived visual proxy from the synchronized fish kind and positions it below the fight anchor. This keeps presentation replaceable without moving normal fish AI into the fight simulation.

The hook synchronizes:

- active fight state
- fish kind
- line-tension ratio
- landing progress
- fish stamina ratio
- coarse fish state (fighting, burst, tired)

The HUD and line renderer consume only those synchronized values.

Dynamic line sag is a client presentation effect. Block collision is deliberately not part of this iteration.

## Input and authority

Client mouse state is reduced to three actions: `REEL_IN`, `PAY_OUT`, and `HOLD`. Loader-specific networking sends only action changes.

The server advances the fight and owns catch, escape, line-break, rod damage and live-fish creation. Client-side code suppresses conflicting vanilla attack/use handling, sends intent and renders feedback.

## Extension surfaces

- `FishBehavior` / `FishBehaviorSession` — replace or extend fish fight behavior.
- `FishProfile` — per-fish tuning.
- `LineProfile` — line and reel tuning.
- `HookedFishKind` — current vanilla-fish materialization boundary; replaceable by a data-driven registry later.
- `FishingHookFightAccess` — synchronized bridge used by gameplay and presentation.

Rod flex, physical line collision, species-specific fight animation and audio feedback remain later presentation layers.
