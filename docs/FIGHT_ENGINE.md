# Fight engine

## Core model

`FishingFight` owns one server-authoritative fight session. Each tick receives one `ReelAction`: `REEL_IN`, `PAY_OUT`, or `HOLD`.

It exposes a `FightSnapshot` containing stamina, distance, line length, tension, failure pressure and the latest fish intent.

## Hooking flow

Vanilla still handles casting, waiting and the bite window. Using the fishing rod during a valid bite starts a `HookedFish` session instead of immediately rolling the vanilla fishing loot table.

While the fight is active, vanilla retrieval is suppressed. The bite window is kept alive internally until the custom fight terminates.

## Temporary hooked fish

`HookedFish` is intentionally not a normal mob. It combines:

- one `FishingFight`
- a selected fish kind
- a lightweight bearing used for lateral movement
- the original water height

The vanilla hook acts as its synchronized world anchor for the prototype. The fish is autonomous through `FishBehavior`, while line length and tension constrain how far the fight can move.

On `CAUGHT`, a normal vanilla fish entity is created at the water anchor and launched toward the player. Vanilla fish already have normal health, bucket interaction and land-flop behavior.

## Failure

- Sustained slack reaches `ESCAPED`.
- Sustained overload reaches `LINE_BROKEN`.
- Both outcomes end the fight without creating a fish.

## Extension points

`FishBehavior` creates a per-fight `FishBehaviorSession`. A session emits `FishIntent` values without loader knowledge.

`FishIntent` contains radial movement, lateral turning, effort and burst state.

`FishProfile` and `LineProfile` keep balance values outside the state machine so later species, sizes, rods and lines can use the same engine.
