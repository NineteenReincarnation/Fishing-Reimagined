# Fishing Reimagined playtest protocol

This protocol is for validating the post-bite fight loop on real clients. Compilation and unit tests are not substitutes for these checks.

## Test matrix

Run each core scenario on both Fabric and NeoForge before marking F9 complete.

| Area | Scenario | Pass condition |
| --- | --- | --- |
| Hook transition | Vanilla bite -> set hook | Fight begins once, vanilla loot is not created immediately |
| Fish readability | Observe without reading HUD text | Fish position is easy to find; normal, burst and tired motion are distinguishable |
| Reel input | Hold the configured attack key | Reel state activates continuously and rod pose reacts |
| Pay-out input | Hold the configured use key | Pay-out state activates continuously and tension falls |
| Progress | Reel in the controlled tension zone | Progress increases and the visible fish moves closer |
| Slack failure | Intentionally pay out too much | Progress falls, slack cue appears, then fish escapes if slack is sustained |
| High tension | Keep reeling into yellow/red | Rod/line/audio show danger before line break |
| Burst | Keep reeling, then respond to a burst | Burst is visible/audible; paying out is an understandable response |
| Tired state | Maintain controlled pressure | Fish visibly calms and becomes easier to land |
| Landing | Reach 100% | Fish follows the pull-out arc, leaves water, becomes a normal live entity and flops on land |
| Shore collision | Fight beside blocks/slabs/fences | Line collision correction is stable and does not visibly teleport the line |
| GUI scale | Test small/normal/large GUI scale | HUD remains centered, readable and does not cover the crosshair/hotbar |
| Remapped controls | Rebind attack/use | HUD displays the new bindings and gameplay uses them |
| Left hand | Set main hand to left | First-person rod feedback mirrors correctly |
| Multiplayer | Two players fish simultaneously | Fight/input/visual state never leaks between players |
| Disconnect | Leave during a fight | No client visual proxy or server input state remains |
| Shader/resource pack | Test at least one common shader/resource pack | Line and HUD remain visible and no renderer crash occurs |

## Recording guidance

For feedback videos, capture at least one complete fight from bite to outcome. Include the HUD and the fish in frame.

When reporting a problem, note:

- loader
- exact mod version
- fish species
- whether shaders/resource packs were active
- approximate GUI scale
- what input was held at the moment of the problem
- timestamp in the recording

## F9 completion rule

F9 is complete only after:

1. Fabric client playtest passes the core loop.
2. NeoForge client playtest passes the core loop.
3. At least one multiplayer session passes ownership/disconnect checks.
4. No blocker remains in fish visibility, input readability, line rendering, landing sequence or outcome handling.
