# Animation compatibility layer

Fishing Reimagined 1.1.25+26.2 uses a two-layer animation system so the mod behaves correctly both with and without animation resource packs.

## Goals

The compatibility layer follows three rules:

1. world-space fishing physics remain authoritative;
2. external packs keep ownership of model-part animation when they provide it;
3. Fishing Reimagined supplies a procedural fallback when no compatible animation resources are detected.

This avoids both extremes: losing all custom motion without a pack, or fighting Fresh Animations / player-animation packs by overriding the same bones and poses twice.

## Automatic detection

The client periodically scans the active resource manager.

Fish animation overlay mode is enabled when fish-related custom entity model resources are found under common EMF/OptiFine CEM locations such as:

- `optifine/cem`
- `emf/cem`

The scan recognizes fish resources by names such as cod, salmon, puffer, tropical and fish.

Player/rod overlay mode is detected independently. It looks for:

- player/arm/fishing-rod CEM resources;
- resources under common `animations` or `player_animations` directories.

Detection is intentionally loader-neutral and does not hard-depend on EMF, ETF, Fresh Animations, Punchy, or another animation mod.

## No animation pack: built-in mode

When no external animation resources are detected, Fishing Reimagined supplies its full fallback layer.

### Fish

The mod controls:

- authoritative world position;
- smooth yaw/body-yaw follow;
- physical pitch from vertical movement;
- a small speed-driven procedural swim pitch;
- a subtle vertical swim bob;
- hanging sway after a successful catch.

The procedural additions are deliberately small and never create a second horizontal path. The horizontal catch-bar position remains the single movement source.

### First-person rod

The mod applies its full procedural rod response:

- pullback from fish load;
- control/reel pulse;
- high-energy tremor;
- hanging-catch weight and sway.

## Animation resource pack detected: overlay mode

External animation resources remain the base animation.

Fishing Reimagined then adds only physical information that the resource pack cannot know about the custom fishing fight.

### Fish

The resource pack owns tail, fin and body-part animation.

Fishing Reimagined keeps:

- world position;
- travel direction;
- smooth whole-entity yaw;
- a reduced physical pitch needed to follow actual movement;
- the fixed hanging orientation needed to attach the catch to the line.

The mod disables its procedural swim bob and periodic swim pitch in this mode.

### Player and rod

The resource pack/player-animation system remains the primary pose.

Fishing Reimagined applies only about 30% of its normal rod transform as an additive physics overlay. This preserves the pack's hand/rod animation while still communicating line load, reel movement and the weight of a hanging fish.

## Manual override

The same config file used for special fishing also accepts:

```json
{
  "specialFishingEnabled": false,
  "animationCompatibility": "auto"
}
```

Supported values:

- `auto` — detect external animation resources automatically;
- `builtin` — always use Fishing Reimagined's full fallback animation;
- `resource_pack` — always use resource-pack overlay behavior.

`overlay` is accepted as an alias for `resource_pack`.

The default is `auto`.

## Compatibility boundary

Fishing Reimagined does not animate external model bones directly.

It treats a hooked fish as the normal vanilla fish entity type on the client. EMF/ETF/CEM packs can therefore replace that entity model normally. The mod controls the entity's world transform and movement, while the animation pack remains free to animate its model parts.

This separation is the core compatibility contract.
