# Fishing process roadmap

This document covers the post-bite fishing process only. Work is intentionally split into visible, testable stages and should be completed one stage at a time.

## Design target

The player should understand the fight from the world first and the HUD second.

A good fight should communicate four things continuously:

1. Where the fish is and what it is doing.
2. Whether the line is slack, controlled, high, or critical.
3. Whether the player is reeling in, paying out, or holding.
4. Whether the player is actually gaining control and bringing the fish closer.

The reference bar is the clarity of Better Fishing plus the physical readability of Enchanted Fishing Line, while keeping Fishing Reimagined's own live-fish and world-space fight model.

## Stage table

| ID | Stage | Goal | Key work | Acceptance criteria | Status |
| --- | --- | --- | --- | --- | --- |
| F0 | Fight feedback logic | Make correct play visibly produce progress | Separate landing progress from raw distance; progress-linked inward pull; burst/fatigue modifiers | Controlled green-zone reeling advances progress and visibly brings fish closer; slack loses progress; overload remains dangerous | Done |
| F1 | Hooked fish visual 2.0 | Make the fish itself readable during the fight | Stronger lateral motion; burst/tired motion profiles; orientation; bubbles/splash cues; clearer depth offset | Player can identify fish position and distinguish normal fighting, burst, and tired states without reading the state text | Ready for playtest |
| F2 | Rod and arm feedback | Make reel-in/pay-out readable from first-person animation | Rod bend from tension; backward pull under load; reel-in motion; release posture; third-person follow-up | Left/right input and dangerous tension are visually obvious even with HUD hidden | Ready for playtest |
| F3 | Fishing line physics 2.0 | Make the line a primary state indicator | Segmented line simulation; gravity sag; tension smoothing; high-tension vibration; block collision; shoreline handling | Slack line visibly hangs, controlled line carries slight sag, critical line is taut; line does not clip straight through terrain | Ready for playtest |
| F4 | HUD 2.0 | Reduce UI dependence while improving clarity | Compact tension indicator; progress treatment; transient action cue; burst warning; HUD scale/config | HUD can be understood at a glance and does not dominate the screen | Ready for playtest |
| F5 | Audio feedback | Add non-visual tension and fish-state information | Reel sound, drag/strain loop, burst splash, slack cue, line snap, landing hit/flop | Player can react to burst and critical tension without staring at the HUD | Ready for playtest |
| F6 | Landing sequence | Make the successful catch feel physical | Final pull; water exit; airborne arc; line release; live fish landing and flop transition | Catch ends with one continuous physical sequence instead of an abrupt state swap | Ready for playtest |
| F7 | Species fight profiles | Make fish types feel mechanically different | Data-driven strength, stamina, burst style, lateral style, size and landing mass | At least four vanilla fish have distinct but learnable fight behavior without changing the core rules | Ready for playtest |
| F8 | Input and accessibility pass | Make the system robust outside the default setup | Remapping strategy, controller path, HUD options, latency smoothing, left-handed/third-person checks | No core action conflicts; feedback remains usable across common control and GUI configurations | Ready for playtest |
| F9 | Compatibility and runtime validation | Validate the full loop rather than only compilation | Fabric + NeoForge runtime; multiplayer; shaders/resource packs; performance; edge cases; recorded playtests | Both loaders pass real-world fishing sessions and no known blocker remains in the core fight loop | Runtime validation required |

## F1 specification — Hooked fish visual 2.0

F1 is the current stage and implementation is complete pending runtime readability validation.

The temporary fish remains a client-side presentation proxy; the server remains authoritative. F1 does not introduce normal fish AI into the fight.

### Required visual states

**Fighting**
- Fish stays visibly below the surface rather than being hidden inside the bobber.
- It performs a readable side-to-side swimming motion.
- Its facing follows the actual fight motion.

**Burst**
- Lateral amplitude and turn rate increase sharply.
- The fish rides slightly higher in the water.
- Bubble and splash cues appear near the fish.
- The motion should read as a run, not as idle animation sped up.

**Tired**
- Motion amplitude and pitch reduce.
- Fish sits slightly deeper and moves more slowly.
- It should look controllable without appearing dead.

### Constraints

- No additional server entity.
- No extra pathfinding.
- No persistent client entity after the fight ends.
- Particle use must be sparse and state-driven.
- F1 should not solve rod animation, full line collision, or audio; those belong to later stages.

## Work order rule

Do not implement multiple stages in one pass unless a later stage is required to unblock the current one.

Each stage should follow this loop:

```text
implement
→ compile both loaders
→ runtime test
→ record/playtest
→ fix readability or bugs
→ mark stage complete
→ start next stage
```


### F1 implementation checkpoint

Implemented in the 1.1.5+26.2 iteration:

- fish is offset from the bobber instead of sitting directly inside it
- stronger side-to-side motion during ordinary fighting
- much wider and faster movement during burst runs
- slower, deeper motion when tired
- facing and pitch follow the visible movement
- burst transitions emit a short splash/bubble event
- burst motion leaves bubble trails
- fish rises slightly as landing progress increases

Do not start F2 until a recorded playtest confirms that the fish can be tracked and its three states can be distinguished without relying on the HUD text.


## F2 implementation checkpoint

Implemented in 1.1.6+26.2:

- first-person fishing rod pose now reacts continuously to line tension
- controlled/high tension pulls the rod backward and down
- reeling adds a visible rhythmic hand/rod pulse
- paying line out relaxes the rod forward
- critical tension adds a small tremor instead of relying only on the HUD
- the transform is presentation-only; server fight authority is unchanged

Third-person posture remains a follow-up validation item if the first-person motion is accepted in playtest.


## F3 implementation checkpoint

Implemented in 1.1.7+26.2:

- slack line now uses a stronger nonlinear sag curve
- increasing tension continuously straightens the line
- high and critical tension add a small visible vibration to the line itself
- intermediate line points test block collision and are lifted to the top of the encountered collision shape
- endpoint collision is intentionally skipped to avoid the player's hand and the hook creating false corrections
- the implementation remains rendering-only and does not move server-authoritative fight positions

This is a first terrain-collision pass, not a full rope solver. Runtime testing should focus on shore edges, fences/slabs, and whether collision correction looks stable while the fish moves.


## F4 implementation checkpoint

Implemented in 1.1.8+26.2:

- removed the large developer-style fight panel
- tension and landing progress are now compact centered bars above the hotbar area
- the current action is shown as a short label: reel, pay out, or hold
- normal fighting no longer adds extra state text
- burst and tired states show short actionable hints only when they matter
- tension labels use state-specific text color so danger can be read without studying the bar

HUD scale/config remains an accessibility follow-up; the visual hierarchy itself is ready for playtest.


## F5 implementation checkpoint

Implemented in 1.1.9+26.2:

- burst transitions play a short splash cue
- tired transitions play a quieter fish cue
- entering high/critical tension produces distinct line-strain clicks
- becoming slack produces a release cue
- controlled reeling produces a restrained repeating retrieve sound
- line break, escape and successful catch each have distinct server-authoritative outcome sounds
- all cues use vanilla sound events so no new asset pack is required yet

The sound mix is intentionally conservative. Playtest should focus on whether the cues are informative without becoming repetitive.


## F6 implementation checkpoint

Implemented in 1.1.10+26.2:

- reaching 100% no longer instantly swaps the fight object into a live fish
- the fight enters a short landing state
- the hook/fish anchor follows a smooth ten-tick arc from the water toward a point in front of the player
- a splash is emitted as the landing sequence begins
- the client visual fish follows that arc rather than remaining under the bobber
- only at the end of the arc is the normal live fish entity created
- the live fish retains vanilla health and land-flop behavior

The next playtest should verify that the arc clears common shore edges and does not place the fish inside the player or blocks.


## F7 implementation checkpoint

Implemented in 1.1.11+26.2:

- cod is the baseline fish: moderate stamina, low burst frequency, steady turning
- salmon has the highest pull strength and stamina, with longer and stronger burst runs
- pufferfish is slower and easier to control, with short weak bursts
- tropical fish is light but erratic, with frequent short bursts and the strongest lateral turning
- HookedFish now builds its fight from the selected species profile instead of one universal prototype profile
- lateral steering response is also species-specific

The four fish still use the same core rules, so learning the system transfers between species while their rhythm and difficulty differ.


## F8 implementation checkpoint

Implemented in 1.1.12+26.2:

- fishing input continues to use Minecraft's attack/use key mappings rather than hard-coded mouse button codes
- HUD action labels now display the player's actual remapped attack/use keys
- burst/tired hints also display the current remapped key
- first-person rod transforms already handle right- and left-hand item display contexts
- HUD placement uses scaled GUI coordinates and therefore follows Minecraft GUI scale

Controller support remains dependent on whatever input layer maps controller actions into Minecraft's attack/use mappings; a dedicated controller API is intentionally not introduced into common code.


## F9 automated validation checkpoint

Prepared in 1.1.13+26.2:

- unit coverage now checks that burst reeling gives much less progress than calm controlled reeling
- unit coverage checks that overloaded line tension does not reward landing progress
- a dedicated runtime playtest protocol now covers both loaders, multiplayer, remapped controls, GUI scale, shoreline collision, shaders and landing behavior
- CI continues to build both loader artifacts on every main push

F9 cannot be marked complete from CI alone. It remains open until the runtime matrix in PLAYTEST_PROTOCOL.md is executed.


## F1.1 large-amplitude fish motion pass

Implemented in 1.1.16+26.2 after video comparison against the reference presentation:

- the server-side fish/hook anchor now sweeps across the water in broad arcs instead of mostly staying on one bearing
- ordinary fighting gains visible lateral travel and radial pulsing
- burst runs can move several blocks laterally at common casting distances
- burst turning accelerates sharply and then eases back instead of only speeding up a tiny sine wave
- tired fish motion contracts heavily
- the client fish model adds a second layer of body-scale movement, stronger vertical travel and larger surge motion
- species now scale visual motion differently: tropical fish is the most erratic, salmon is forceful, cod is baseline, and pufferfish is restrained
- burst water effects were strengthened to match the larger world motion

The important acceptance criterion is no longer merely "the fish animates." A normal fight should visibly travel across the water, while a burst should be readable from the fish alone even if the HUD is ignored.


## F10 — Physical fight core 2.0

Implemented in 1.1.17+26.2.

The previous direct-tension controller has been replaced by a multi-state physical fight:

- four simulation substeps per game tick
- spring + velocity damping tension
- fish radial velocity and inertia
- load-dependent reel efficiency
- automatic drag slip above the drag threshold
- drag recovery before line break
- break heat that accounts for overload and relative velocity
- distance-derived catch progress
- probing, pulling, burst, recovery and tired fish rhythm
- synchronized fish speed, spool speed, drag slip and distance
- rod, HUD, audio and fish visuals driven from the synchronized physical state

This stage deliberately changes the feel of the fight rather than preserving the 1.1.16 balance. Runtime tuning should now focus on reel speed, drag threshold, spring stiffness, damping, burst force and typical fight duration.
