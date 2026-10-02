# Horizontal catch-bar design

Version 1.1.21+26.2 replaces the tension/red-zone minigame with a horizontal tracking game inspired by the interaction structure of Stardew Valley fishing while retaining Fishing Reimagined's own implementation and world presentation.

## Player rule

Keep the fish inside the moving catch zone.

There is no red tension zone in the basic minigame.

## Horizontal control

The HUD contains one horizontal track.

- Hold the normal use key to accelerate the catch zone to the right.
- Release the use key to accelerate the catch zone to the left.
- The catch zone has velocity, damping, inertia, and a small edge bounce.
- Switching from hold to release does not reverse the zone instantly.

This preserves timing skill without requiring extra buttons.

## Fish movement

The fish marker moves independently across the same track.

The existing internal fish modes still exist, but they are not shown as text:

- probing creates smaller wandering targets
- pulling creates longer side-to-side travel
- burst creates a fast run toward one side
- recovering trends back toward the middle
- tired movement is slower and shorter

Species profiles scale movement speed and catch-zone width, so fish can feel different without changing the player's rules.

## Progress

When the fish overlaps the catch zone, Catch Progress rises.

When the fish leaves the zone:

1. a short grace period prevents instant punishment;
2. positive progress momentum fades;
3. if the fish remains outside, progress begins falling smoothly.

This avoids both extremes: progress never being lost, and tiny mistakes deleting progress immediately.

## Escape

Progress reaching zero is not an instant failure.

Only after progress is empty and the fish remains uncontrolled for an additional period does escape pressure accumulate. Regaining control quickly clears that pressure.

## World coupling

The horizontal fish position also drives the water-world fish anchor.

A fish moving right on the HUD moves to the corresponding side of the cast direction in the world. Bursts create larger and faster world movement. The existing rope renderer, rod animation, water effects, and live-fish landing sequence remain in place.

## HUD

The fight HUD contains only:

- the horizontal fish/catch-zone track
- Catch Progress
- a minimal hold-right / release-left hint

No fish-state labels, stamina meter, tension meter, red zone, drag meter, or reel-efficiency display is required.
