# Better-Fishing-style fight rebuild

This iteration rebuilds the post-bite fight around the externally visible interaction principles of Better Fishing and Enchanted Fishing Line.

The reference mods are All Rights Reserved. Fishing Reimagined does not copy or decompile their source code or assets. This implementation is independent and uses only publicly observable behavior as a design reference.

## Interaction target

The fight is now a tension-balancing game first.

- Hold the normal **Use** key to reel.
- Release the **Use** key to pay line out.
- The fish constantly pulls against the player.
- Keep the tension marker inside the green region to gain catch progress.
- Too little tension loses progress and can let the fish escape.
- Too much tension enters an orange/red danger region and accumulates snap risk.
- Releasing the key quickly reduces tension and recovers snap risk.
- A burst run sharply increases fish pull and should make the player release.

This replaces the previous model where progress depended on a mixture of world distance, reel input and hidden fatigue calculations.

## Progress semantics

Catch Progress is now explicit control progress.

It increases only while tension is inside the green sweet spot. It is fastest near the center marker.

World-space fish distance follows catch progress for presentation. The world position no longer decides whether the progress bar is allowed to move.

## Line break feedback

Line break is intentionally hard to miss:

- the tension bar enters a dedicated red zone
- snap risk is shown as a percentage while pressure accumulates
- line strain audio escalates
- the server emits a strong snap sound and particles
- an action-bar message says that the line snapped

## Reference boundary

Public reference behavior:

- Better Fishing describes balancing line tension to fill a progress bar and snapping the line in the red zone.
- Its 26.2 hotfix explicitly notes that holding the use button advances the catch.
- Enchanted Fishing Line describes gravity sag, dynamic slack and block collision.

The next iteration applies these principles to a persistent multi-segment client-side rope simulation rather than copying any proprietary implementation.
