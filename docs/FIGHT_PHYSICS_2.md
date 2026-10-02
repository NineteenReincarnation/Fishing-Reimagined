# Fight physics 2.0

This system replaces the earlier direct-tension minigame model with a server-authoritative physical fight.

## Reference principles

The public descriptions and launch material for Better Fishing emphasize a responsive tension minigame, catch progress, visible fish, and procedural rod feedback. Enchanted Fishing Line emphasizes a gravity-driven simulated line with dynamic slack and terrain collision.

Both reference mods are All Rights Reserved. Fishing Reimagined uses those observable design principles only; no proprietary source code or assets are copied or decompiled.

## Server state

The fight now owns continuous physical state:

- fish radial velocity
- fish drive target from the behavior state machine
- spooled line length
- spool velocity
- spring extension
- spring-damper tension
- reel efficiency under load
- automatic drag slip
- fish stamina
- accumulated line-break heat
- actual fish distance from the player

The simulation runs four substeps per Minecraft tick for stability.

## Spring and damping

The line only pulls; it never pushes.

Tension is calculated from:

1. positive line extension: fish distance minus spooled line length
2. relative outward velocity between fish and spool
3. line stiffness and velocity damping

A fish that is already running outward therefore keeps the line loaded for a short time even after the player releases. This is intentional inertia rather than an instant HUD value change.

## Reel under load

Reel-in speed is not constant.

Low load allows almost full retrieve speed. As tension approaches the drag threshold, the reel becomes progressively less effective. At high load the player can hold the reel key while recovering almost no line.

This creates the intended feeling that a strong fish can temporarily overpower the reel.

## Automatic drag

Above the drag threshold the reel automatically pays line out. The amount of slip rises smoothly with tension.

This provides a warning and buffer before line failure, but does not make the line unbreakable. Sustained overload, high relative velocity, and continued hard reeling accumulate break heat until the line snaps.

The client receives drag-slip and spool-velocity state so rod motion, HUD and sound all describe the same physical event.

## Fish rhythm

The default behavior is no longer a binary calm/burst switch.

A fish cycles through:

- probing
- sustained pulling
- burst run
- recovery
- tired fighting

The state machine changes target speed, effort and lateral behavior. Species profiles still scale the resulting strength, stamina, burst duration and visual movement.

## Progress

Catch progress is derived from actual fish distance.

When the fish takes line and runs farther away, progress can fall. When the player physically retrieves line and tension pulls the fish inward, progress increases.

The bar is therefore only a readable representation of the world-space fight; it is not a separate success meter.

## Presentation coupling

The same synchronized physical values drive:

- rod pullback and vibration
- reel pumping animation
- drag ratchet motion
- drag/reel/strain audio
- fish model surge
- fish world movement
- HUD distance and recovery progress
- line tension and rope straightening

The goal is that the player can understand the fight without trusting an unrelated UI simulation.
