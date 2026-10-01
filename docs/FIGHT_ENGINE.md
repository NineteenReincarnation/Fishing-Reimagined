# Fight engine

The prototype engine is deliberately small and loader-neutral.

## Public model

`FishingFight` owns one fight session. Each tick receives one `ReelAction`: `REEL_IN`, `PAY_OUT`, or `HOLD`.

It exposes a `FightSnapshot` for Minecraft-side adapters and presentation code.

## State

The prototype tracks fish stamina, fish distance, current line length, line tension, consecutive slack exposure, consecutive overload exposure, the fight phase, and the latest fish movement intent.

Stamina is an internal endurance model, not a damage/health system.

## Extension points

`FishBehavior` creates a per-fight `FishBehaviorSession`. A behavior session emits `FishIntent` values without knowing anything about Minecraft entities.

`FishIntent` already contains radial movement and lateral turning information. The first fight engine consumes the radial component; the spatial hooked-fish adapter can consume the lateral component later without replacing the behavior API.

`FishProfile` and `LineProfile` keep tuning values separate from the simulation.

## Prototype behavior

`BasicFishBehavior` provides steady movement with occasional bursts. It is a validation behavior, not a final species implementation.

## Terminal outcomes

The core exposes `CAUGHT`, `ESCAPED`, and `LINE_BROKEN`. The Minecraft bridge decides what each outcome does to the hook, temporary hooked-fish object and final live fish entity.
