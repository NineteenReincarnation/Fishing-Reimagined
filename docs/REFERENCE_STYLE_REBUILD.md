# Reference-style fishing rebuild

Version 1.1.18+26.2 intentionally removes the overcomplicated physical fight loop introduced in 1.1.17.

The goal is to match the publicly observable interaction pattern of Better Fishing while keeping Fishing Reimagined's own code, fish entities, and line renderer.

## Flow

1. Cast normally.
2. Wait for the vanilla bite.
3. The fight starts automatically when the fish bites.
4. Hold the normal use key to reel.
5. Release the use key to lower tension.
6. Reeling in the safe tension range fills Catch Progress.
7. A burst pushes tension upward quickly and encourages releasing.
8. Staying in the red zone long enough snaps the line.
9. Reaching 100% triggers the existing physical landing sequence and creates a normal live vanilla fish.

There is no separate "set hook" click after the bite.

## Design changes from 1.1.17

Removed from the core gameplay loop:

- automatic drag
- reel stall simulation
- spring/damper tension as the primary control model
- slack-line escape as a normal failure condition
- distance-driven progress

Retained for presentation:

- large fish movement
- persistent rope rendering and terrain collision
- rod animation
- sound feedback
- live fish landing

The result is intentionally simpler and more responsive. The player should understand the whole fight from one rule: hold to reel, release to lower tension, avoid the red zone.
