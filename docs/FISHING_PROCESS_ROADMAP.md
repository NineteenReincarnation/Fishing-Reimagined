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
| F2 | Rod and arm feedback | Make reel-in/pay-out readable from first-person animation | Rod bend from tension; backward pull under load; reel-in motion; release posture; third-person follow-up | Left/right input and dangerous tension are visually obvious even with HUD hidden | Planned |
| F3 | Fishing line physics 2.0 | Make the line a primary state indicator | Segmented line simulation; gravity sag; tension smoothing; high-tension vibration; block collision; shoreline handling | Slack line visibly hangs, controlled line carries slight sag, critical line is taut; line does not clip straight through terrain | Planned |
| F4 | HUD 2.0 | Reduce UI dependence while improving clarity | Compact tension indicator; progress treatment; transient action cue; burst warning; HUD scale/config | HUD can be understood at a glance and does not dominate the screen | Planned |
| F5 | Audio feedback | Add non-visual tension and fish-state information | Reel sound, drag/strain loop, burst splash, slack cue, line snap, landing hit/flop | Player can react to burst and critical tension without staring at the HUD | Planned |
| F6 | Landing sequence | Make the successful catch feel physical | Final pull; water exit; airborne arc; line release; live fish landing and flop transition | Catch ends with one continuous physical sequence instead of an abrupt state swap | Planned |
| F7 | Species fight profiles | Make fish types feel mechanically different | Data-driven strength, stamina, burst style, lateral style, size and landing mass | At least four vanilla fish have distinct but learnable fight behavior without changing the core rules | Planned |
| F8 | Input and accessibility pass | Make the system robust outside the default setup | Remapping strategy, controller path, HUD options, latency smoothing, left-handed/third-person checks | No core action conflicts; feedback remains usable across common control and GUI configurations | Planned |
| F9 | Compatibility and runtime validation | Validate the full loop rather than only compilation | Fabric + NeoForge runtime; multiplayer; shaders/resource packs; performance; edge cases; recorded playtests | Both loaders pass real-world fishing sessions and no known blocker remains in the core fight loop | Planned |

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
