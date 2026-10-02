# Horizontal catch-bar design

Version 1.1.23+26.2 keeps the horizontal Stardew-like catch game and restructures the world presentation so the HUD fish and the water-world fish are one authoritative motion.

## One fish, one motion source

`fishTrackPosition` and `fishVelocity` are the authoritative lateral motion.

They drive:

- the HUD fish marker
- the server-side hook/fish anchor
- the visible fish's movement direction
- world-space lateral travel
- rod and rope feedback

The client visual fish no longer adds a separate large sine-wave translation around the server anchor. Client code may smooth depth and orientation, but it must not invent a second horizontal path.

## Fish-line connection

During a fight the server hook anchor follows the authoritative fish position. The visible fish is placed slightly behind that anchor according to its facing direction, so the hook/rope endpoint sits at the head/mouth area instead of next to a separately animated fish.

The special-fight rope endpoint is also lowered from the vanilla bobber-height endpoint toward the fish head.

## Continuous animation

State changes no longer swap between unrelated positional sine formulas.

The visible fish now uses:

- smoothed yaw
- slower body-yaw follow for readable turning
- smoothly interpolated depth
- speed-dependent small pitch motion
- state-dependent response speed

This removes the large teleport-like changes caused by switching sine frequency, amplitude, and depth in one tick.

## Visibility and particles

Normal fish stay in a shallower layer, tired fish no longer sit almost a full block below the surface, and burst particles are reduced.

Particles are emitted mainly behind the fish as a wake, so they reinforce movement instead of obscuring the model.

## Progress balance

Catch Progress now starts at 0%.

- Outside-zone grace is shorter.
- Edge contact gives only about 30% of maximum gain instead of roughly 72%.
- Centered tracking gives the best gain.
- Sustained misses remove progress more strongly.

## Fish behavior fairness

Burst direction no longer reads the player's catch-zone position.

A burst normally continues the fish's current velocity/target direction, falling back to randomness only when there is no meaningful movement direction.

## World distance

Catch Progress still provides the long-term approach toward the player, but fish intent now adds a smoothed radial tug. A pulling or bursting fish can briefly take distance back even late in the fight.

## Landing

Landing now lasts 12 ticks and targets a point roughly two blocks from the player with a deterministic side offset. The live fish receives only a small residual motion away from the player, avoiding the previous camera-filling fly-through.
