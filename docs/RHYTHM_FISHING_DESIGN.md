# Rhythm fishing design

Version 1.1.20+26.2 simplifies the player's decision to one repeated question:

> Hold the use key, or release it?

The implementation can stay rich internally, but the player should not need to read fish-state labels, stamina values, drag state, distance, or reel efficiency.

## Core loop

- Hold use to reel and push tension upward.
- Release use to let tension fall.
- Keep tension moving through the useful middle range.
- Good rhythm grows Catch Progress.
- Let tension fall too low for too long and progress starts slipping backward.
- Push tension too high and progress stops being useful while snap pressure accumulates.
- Leave the red zone quickly and snap pressure recovers.
- Stay in the red zone and the line eventually breaks.

The fish's internal behavior still changes the speed of the tension movement. It changes the rhythm, not the rules.

## Tension regions

The tension bar is intentionally divided into broad regions rather than many named mechanics.

| Region | Approx. ratio | Progress behavior | Risk |
| --- | ---: | --- | --- |
| Very low | 0.00-0.12 | noticeable rollback | none |
| Low | 0.12-0.28 | slow rollback | none |
| Useful | 0.28-0.68 | fastest gain | none |
| High | 0.68-0.90 | slower gain | none |
| Red | 0.90+ | rollback | snap pressure |

The player only needs to see the colored tension bar.

## Progress momentum

Catch Progress no longer changes as a hard on/off switch.

A hidden progress velocity is smoothed toward the current region's target rate.

This creates two important effects:

1. after a good reel phase, releasing briefly can still carry a little positive progress;
2. after staying loose too long, returning to good tension takes a short moment to reverse the lost momentum.

The result should feel like preserving or losing control of the fish, not toggling a UI condition.

## Red-zone pressure

Entering red is not an instant failure.

Snap pressure accumulates over time. At the start of the red zone it takes roughly a couple of seconds of sustained abuse to break the line; deeper overload accumulates faster.

Dropping tension below the danger range removes snap pressure substantially faster than it accumulates.

This creates the intended pattern:

```text
reel
→ tension rises
→ touch danger
→ release
→ tension falls
→ reel again
```

The HUD only shows an explicit warning while danger is active.

## Fish behavior

Fish states remain internal because they are useful for motion, sound, and tension pacing.

They should not be required reading.

A burst may make tension rise faster. A recovery phase may make it easier to hold the useful region. The player's rule never changes: judge the bar and choose hold or release.

## HUD rule

The fight HUD should contain only:

- Line Tension
- Catch Progress
- current hold/release hint
- temporary snap warning

Fish-state text is deliberately omitted.
